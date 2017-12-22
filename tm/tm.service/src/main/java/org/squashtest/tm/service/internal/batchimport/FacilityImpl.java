/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.service.internal.batchimport;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.customfield.RawValue;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.domain.testcase.CallTestStep;
import org.squashtest.tm.domain.testcase.Dataset;
import org.squashtest.tm.domain.testcase.DatasetParamValue;
import org.squashtest.tm.domain.testcase.Parameter;
import org.squashtest.tm.domain.testcase.ParameterAssignationMode;
import org.squashtest.tm.domain.testcase.RequirementVersionCoverage;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestStep;
import org.squashtest.tm.service.importer.ImportStatus;
import org.squashtest.tm.service.importer.LogEntry;
import org.squashtest.tm.service.internal.batchimport.testcase.excel.CoverageInstruction;
import org.squashtest.tm.service.internal.batchimport.testcase.excel.CoverageTarget;
import org.squashtest.tm.service.internal.repository.DatasetDao;
import org.squashtest.tm.service.internal.repository.DatasetParamValueDao;
import org.squashtest.tm.service.internal.repository.ParameterDao;
import org.squashtest.tm.service.internal.repository.RequirementVersionCoverageDao;
import org.squashtest.tm.service.requirement.RequirementLibraryFinderService;
import org.squashtest.tm.service.requirement.RequirementLibraryNavigationService;
import org.squashtest.tm.service.testcase.CallStepManagerService;
import org.squashtest.tm.service.testcase.DatasetModificationService;
import org.squashtest.tm.service.testcase.ParameterModificationService;
import org.squashtest.tm.service.testcase.TestCaseLibraryNavigationService;
import org.squashtest.tm.service.testcase.TestCaseModificationService;

/**
 * Implementation of batch import methods that will actually update the
 * database.
 */
@Component
@Scope("prototype")
public class FacilityImpl extends EntityFacilitySupport implements Facility {

	private static final Logger LOGGER = LoggerFactory.getLogger(FacilityImpl.class);


	@Inject
	private TestCaseLibraryNavigationService navigationService;

	@Inject
	private TestCaseModificationService testcaseModificationService;

	@Inject
	private CallStepManagerService callstepService;

	@Inject
	private ParameterModificationService parameterService;

	@Inject
	private DatasetModificationService datasetService;

	@Inject
	private RequirementLibraryNavigationService reqLibNavigationService;

	@Inject
	private DatasetDao datasetDao;

	@Inject
	private DatasetParamValueDao paramvalueDao;

	@Inject
	private ParameterDao paramDao;

	@Inject
	private RequirementLibraryFinderService reqFinderService;

	@Inject
	private RequirementVersionCoverageDao coverageDao;

	@Inject
	private RequirementFacility requirementFacility;

	@Inject
	private TestCaseFacility testCaseFacility;

        
    // the following attributes shadow the same attributes defined in EntityFacilitySupport
    // here we can inject prototype-scoped instances and configure the RequirementFacility and TestCaseFacility with them.
	@Inject
	private CustomFieldTransator customFieldTransator;
        
	@Inject
	private ValidationFacility validator;

	private final FacilityImplHelper helper = new FacilityImplHelper(this);


	// ************************ public (and nice looking) code
	// **************************************


	/**
	 * @see org.squashtest.tm.service.internal.batchimport.Facility#createTestCase(org.squashtest.tm.service.internal.batchimport.TestCaseInstruction)
	 */
	@Override
	public LogTrain createTestCase(TestCaseInstruction instr) {
		return testCaseFacility.createTestCase(instr);
	}


	/**
	 * @see org.squashtest.tm.service.internal.batchimport.Facility#updateTestCase(org.squashtest.tm.service.internal.batchimport.TestCaseInstruction)
	 */
	@Override
	public LogTrain updateTestCase(TestCaseInstruction instr) {
		return testCaseFacility.updateTestCase(instr);
	}


	@Override
	public LogTrain deleteTestCase(TestCaseTarget target) {
		return testCaseFacility.deleteTestCase(target);
	}

	@Override
	public LogTrain addActionStep(TestStepTarget target, ActionTestStep testStep, Map<String, String> cufValues) {

		LogTrain train = validator.addActionStep(target, testStep, cufValues);

		if (!train.hasCriticalErrors()) {
			try {
				helper.fillNullWithDefaults(testStep);
				helper.truncate(testStep, cufValues);

				doAddActionStep(target, testStep, cufValues);
				validator.getModel().addActionStep(target);

				LOGGER.debug(EXCEL_ERR_PREFIX + "Created Action Step \t'" + target + "'");

			} catch (Exception ex) {
				train.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_UNEXPECTED_ERROR,
					new Object[]{ex.getClass().getName()}));

				LOGGER.error(EXCEL_ERR_PREFIX + "unexpected error while creating step " + target + " : ", ex);
			}
		}

		return train;
	}

	@Override
	public LogTrain addCallStep(TestStepTarget target, CallTestStep testStep, TestCaseTarget calledTestCase,
		CallStepParamsInfo paramInfo, ActionTestStep actionStepBackup) {

		LogTrain train = validator.addCallStep(target, testStep, calledTestCase, paramInfo, actionStepBackup);

		if (!train.hasCriticalErrors()) {
			String mustImportCallAsActionStepErrorI18n = FacilityUtils.mustImportCallAsActionStep(train);
			try {
				if (mustImportCallAsActionStepErrorI18n != null) {
					doAddActionStep(target, actionStepBackup, new HashMap<String, String>(0));
					validator.getModel().addActionStep(target);
				} else {

					doAddCallStep(target, calledTestCase, paramInfo);

					validator.getModel().addCallStep(target, calledTestCase, paramInfo);

					LOGGER.debug(EXCEL_ERR_PREFIX + "Created Call Step \t'" + target + "' -> '" + calledTestCase + "'");
				}
			} catch (Exception ex) {
				train.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_UNEXPECTED_ERROR,
					new Object[]{ex.getClass().getName()}));
				LOGGER.error(EXCEL_ERR_PREFIX + "unexpected error while creating step " + target + " : ", ex);
			}
		}

		return train;
	}

	@Override
	public LogTrain updateActionStep(TestStepTarget target, ActionTestStep testStep, Map<String, String> cufValues) {

		LogTrain train = validator.updateActionStep(target, testStep, cufValues);

		if (!train.hasCriticalErrors()) {
			try {
				helper.truncate(testStep, cufValues);
				doUpdateActionStep(target, testStep, cufValues);

				LOGGER.debug(EXCEL_ERR_PREFIX + "Updated Action Step \t'" + target + "'");
			} catch (Exception ex) {
				train.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_UNEXPECTED_ERROR,
					new Object[]{ex.getClass().getName()}));
				LOGGER.error(EXCEL_ERR_PREFIX + "unexpected error while updating step " + target + " : ", ex);
			}
		}

		return train;
	}

	@Override
	public LogTrain updateCallStep(TestStepTarget target, CallTestStep testStep, TestCaseTarget calledTestCase,
		CallStepParamsInfo paramInfo, ActionTestStep actionStepBackup) {

		LogTrain train = validator.updateCallStep(target, testStep, calledTestCase, paramInfo, actionStepBackup);

		if (!train.hasCriticalErrors()) {
			try {
				doUpdateCallStep(target, calledTestCase, paramInfo);
				validator.getModel().updateCallStepTarget(target, calledTestCase, paramInfo);

				LOGGER.debug(EXCEL_ERR_PREFIX + "Created Call Step \t'" + target + "' -> '" + calledTestCase + "'");
			} catch (Exception ex) {
				train.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_UNEXPECTED_ERROR,
					new Object[]{ex.getClass().getName()}));
				LOGGER.error(EXCEL_ERR_PREFIX + "unexpected error while updating step " + target + " : ", ex);
			}
		}

		return train;
	}

	@Override
	public LogTrain deleteTestStep(TestStepTarget target) {

		LogTrain train = validator.deleteTestStep(target);

		if (!train.hasCriticalErrors()) {
			try {
				doDeleteTestStep(target);
				validator.getModel().remove(target);

				LOGGER.debug(EXCEL_ERR_PREFIX + "Deleted Step \t'" + target + "'");

			} catch (Exception ex) {
				train.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_UNEXPECTED_ERROR,
					new Object[]{ex.getClass().getName()}));
				LOGGER.error(EXCEL_ERR_PREFIX + "unexpected error while deleting step " + target + " : ", ex);
			}
		}

		return train;
	}

	@Override
	public LogTrain createParameter(ParameterTarget target, Parameter param) {

		LogTrain train = validator.createParameter(target, param);

		if (!train.hasCriticalErrors()) {
			try {
				doCreateParameter(target, param);
				validator.getModel().addParameter(target);

				LOGGER.debug(EXCEL_ERR_PREFIX + "Created Parameter \t'" + target + "'");
			} catch (Exception ex) {
				train.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_UNEXPECTED_ERROR,
					new Object[]{ex.getClass().getName()}));
				LOGGER.error(EXCEL_ERR_PREFIX + "unexpected error while adding parameter " + target + " : ", ex);
			}
		}

		return train;
	}

	@Override
	public LogTrain updateParameter(ParameterTarget target, Parameter param) {

		LogTrain train = validator.updateParameter(target, param);

		if (!train.hasCriticalErrors()) {
			try {
				doUpdateParameter(target, param);
				validator.getModel().addParameter(target); // create the parameter if didn't exist already.
				// Double-insertion proof.

				LOGGER.debug(EXCEL_ERR_PREFIX + "Updated Parameter \t'" + target + "'");
			} catch (Exception ex) {
				train.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_UNEXPECTED_ERROR,
					new Object[]{ex.getClass().getName()}));
				LOGGER.error(EXCEL_ERR_PREFIX + "unexpected error while updating parameter " + target + " : ", ex);
			}
		}

		return train;
	}

	@Override
	public LogTrain deleteParameter(ParameterTarget target) {

		LogTrain train = validator.deleteParameter(target);

		if (!train.hasCriticalErrors()) {
			try {
				doDeleteParameter(target);
				validator.getModel().removeParameter(target);

				LOGGER.debug(EXCEL_ERR_PREFIX + "Deleted Parameter \t'" + target + "'");
			} catch (Exception ex) {
				train.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_UNEXPECTED_ERROR,
					new Object[]{ex.getClass().getName()}));

				LOGGER.error(EXCEL_ERR_PREFIX + "unexpected error while deleting parameter " + target + " : ", ex);
			}
		}

		return train;
	}

	@Override
	public LogTrain failsafeUpdateParameterValue(DatasetTarget dataset, ParameterTarget param, String value,
		boolean isUpdate) {

		LogTrain train = validator.failsafeUpdateParameterValue(dataset, param, value, isUpdate);

		if (!train.hasCriticalErrors()) {
			try {
				doFailsafeUpdateParameterValue(dataset, param, value);

				validator.getModel().addDataset(dataset);

				LOGGER.debug(EXCEL_ERR_PREFIX + "Updated Param Value for param \t'" + param + "' in dataset '"
					+ dataset + "'");
			} catch (Exception ex) {
				train.addEntry(new LogEntry(dataset, ImportStatus.FAILURE, Messages.ERROR_UNEXPECTED_ERROR,
					new Object[]{ex.getClass().getName()}));
				LOGGER.error(EXCEL_ERR_PREFIX + "unexpected error while setting parameter " + param + " in dataset "
					+ dataset + " : ", ex);
			}
		}

		return train;
	}

	@Override
	public LogTrain createDataset(DatasetTarget dataset) {

		LogTrain train = validator.createDataset(dataset);

		if (!train.hasCriticalErrors()) {
			try {
				findOrCreateDataset(dataset);

				validator.getModel().addDataset(dataset);

				LOGGER.debug(EXCEL_ERR_PREFIX + "Created Dataset '" + dataset + "'");

			} catch (Exception ex) {
				train.addEntry(new LogEntry(dataset, ImportStatus.FAILURE, Messages.ERROR_UNEXPECTED_ERROR,
					new Object[]{ex.getClass().getName()}));
				LOGGER.error(EXCEL_ERR_PREFIX + "unexpected error while creating dataset " + dataset + " : ", ex);
			}
		}

		return train;
	}

	@Override
	public LogTrain deleteDataset(DatasetTarget dataset) {

		LogTrain train = validator.deleteDataset(dataset);

		if (!train.hasCriticalErrors()) {
			try {
				doDeleteDataset(dataset);

				validator.getModel().removeDataset(dataset);

				LOGGER.debug(EXCEL_ERR_PREFIX + "Deleted Dataset '" + dataset + "'");

			} catch (Exception ex) {
				train.addEntry(new LogEntry(dataset, ImportStatus.FAILURE, Messages.ERROR_UNEXPECTED_ERROR,
					new Object[]{ex.getClass().getName()}));
				LOGGER.error(EXCEL_ERR_PREFIX + "unexpected error while deleting dataset " + dataset + " : ", ex);
			}
		}

		return train;
	}

	@Override
	public LogTrain createRequirementVersion(RequirementVersionInstruction instr) {
		return requirementFacility.createRequirementVersion(instr);
	}

	@Override
	public LogTrain updateRequirementVersion(RequirementVersionInstruction instr) {
		return requirementFacility.updateRequirementVersion(instr);
	}


	@Override
	public LogTrain deleteRequirementVersion(RequirementVersionInstruction instr) {
		return requirementFacility.deleteRequirementVersion(instr);
	}

	@Override
	public LogTrain createRequirementLink(RequirementLinkInstruction instr) {
		return requirementFacility.createRequirementLink(instr);
	}
	
	@Override
	public LogTrain updateRequirementLink(RequirementLinkInstruction instr) {
		return requirementFacility.updateRequirementLink(instr);
	}
	
	@Override
	public LogTrain deleteRequirementLink(RequirementLinkInstruction instr) {
		return requirementFacility.deleteRequirementLink(instr);
	}
	
	
	
	
	/**
	 * for all other stuffs that need to be done afterward
	 */
	public void postprocess(List<Instruction<?>> instructions) {
		requirementFacility.postprocess(instructions);
	}

	// ************************* private (and hairy) code
	// *********************************


	private void doAddActionStep(TestStepTarget target, ActionTestStep testStep, Map<String, String> cufValues) {

		Map<Long, RawValue> acceptableCufs = toAcceptableCufs(cufValues);

		// add the step
		TestCase tc = validator.getModel().get(target.getTestCase());
		testcaseModificationService.addActionTestStep(tc.getId(), testStep, acceptableCufs);

		// move it if the index was specified. Perf optim : don't move it if already in the good place
		Integer index = target.getIndex();
		if (index != null && index >= 0 && index < tc.getSteps().size()
				&& tc.getPositionOfStep(testStep.getId()) != index) {
			testcaseModificationService.changeTestStepsPosition(tc.getId(), index, Collections.singletonList(testStep.getId()));
		}


	}

	private void doAddCallStep(TestStepTarget target, TestCaseTarget calledTestCase, CallStepParamsInfo paramInfo) {

		// add the step
		TestCase tc = validator.getModel().get(target.getTestCase());
		TestCase called = validator.getModel().get(calledTestCase);

		callstepService.addCallTestStep(tc.getId(), called.getId());
		CallTestStep created = (CallTestStep) tc.getSteps().get(tc.getSteps().size() - 1);

		// handle the parameter assignation
		changeParameterAssignation(created.getId(), calledTestCase, paramInfo);

		// change position if possible and required
		Integer index = target.getIndex();
		if (index != null && index >= 0 && index < tc.getSteps().size()) {
			testcaseModificationService.changeTestStepsPosition(tc.getId(), index, Collections.singletonList(created.getId()));
		}

	}

	private void doUpdateActionStep(TestStepTarget target, ActionTestStep testStep, Map<String, String> cufValues) {

		// update the step
		ActionTestStep orig = (ActionTestStep) validator.getModel().getStep(target);

		String newAction = testStep.getAction();
		if (!StringUtils.isBlank(newAction) && !newAction.equals(orig.getAction())) {
			orig.setAction(newAction);
		}

		String newResult = testStep.getExpectedResult();
		if (!StringUtils.isBlank(newResult) && !newResult.equals(orig.getExpectedResult())) {
			orig.setExpectedResult(newResult);
		}

		// the custom field values now
		doUpdateCustomFields(cufValues, orig);

	}

	private void doUpdateCallStep(TestStepTarget target, TestCaseTarget calledTestCase, CallStepParamsInfo paramInfo) {

		// update the step
		TestStep actualStep = validator.getModel().getStep(target);
		TestCase newCalled = validator.getModel().get(calledTestCase);
		callstepService.checkForCyclicStepCallBeforePaste(actualStep.getTestCase().getId(), newCalled.getId());
		((CallTestStep) actualStep).setCalledTestCase(newCalled);

		// update the parameter assignation
		changeParameterAssignation(actualStep.getId(), calledTestCase, paramInfo);

	}

	private void doDeleteTestStep(TestStepTarget target) {
		TestCase tc = validator.getModel().get(target.getTestCase());
		testcaseModificationService.removeStepFromTestCaseByIndex(tc.getId(), target.getIndex());
	}

	private void doCreateParameter(ParameterTarget target, Parameter param) {

		// according to the spec this is exactly the same thing
		doUpdateParameter(target, param);
	}

	private void doUpdateParameter(ParameterTarget target, Parameter param) {
		if (!validator.getModel().doesParameterExists(target)) {
			Long testcaseId = validator.getModel().getId(target.getOwner());
			helper.fillNullWithDefaults(param);
			helper.truncate(param);
			parameterService.addNewParameterToTestCase(param, testcaseId);
		} else {
			String description = param.getDescription();
			if (description != null) {
				findParameter(target).setDescription(description);
			}
		}

	}

	private void doDeleteParameter(ParameterTarget target) {
		Long testcaseId = validator.getModel().getId(target.getOwner());
		List<Parameter> allparams = parameterService.findAllParameters(testcaseId);

		Parameter param = null;
		for (Parameter p : allparams) {
			if (p.getName().equals(target.getName())) {
				param = p;
				break;
			}
		}

		parameterService.remove(param);
	}

	private void doFailsafeUpdateParameterValue(DatasetTarget dataset, ParameterTarget param, String value) {
		DatasetParamValue dpv = findParamValue(dataset, param);
		String trValue = helper.truncate(value);
		dpv.setParamValue(trValue);
	}

	private void doDeleteDataset(DatasetTarget dataset) {
		Dataset ds = findOrCreateDataset(dataset);
		TestCase tc = ds.getTestCase();
		tc.removeDataset(ds);
		datasetService.remove(ds);
	}

	// ******************************** support methods ***********************

	private Parameter findParameter(ParameterTarget param) {
		Long testcaseId = validator.getModel().getId(param.getOwner());

		Parameter found = paramDao.findOwnParameterByNameAndTestCase(param.getName(), testcaseId);

		if (found != null) {
			return found;
		} else {
			throw new NoSuchElementException("parameter " + param + " could not be found");
		}
	}

	/**
	 * @return the found Dataset or a new one (non null value)
	 */
	private Dataset findOrCreateDataset(DatasetTarget dataset) {
		Long tcid = validator.getModel().getId(dataset.getTestCase());

		String truncated = helper.truncate(dataset.getName());
		Dataset found = datasetDao.findByTestCaseIdAndName(tcid, truncated);

		if (found != null) {
			return found;
		} else {
			Dataset newds = new Dataset();
			newds.setName(dataset.getName());
			helper.fillNullWithDefaults(newds);
			helper.truncate(newds);
			datasetService.persist(newds, tcid);

			LOGGER.debug(EXCEL_ERR_PREFIX + "Created Dataset \t'" + dataset + "'");

			return newds;
		}
	}

	private void changeParameterAssignation(Long stepId, TestCaseTarget tc, CallStepParamsInfo paramInfo) {
		Long dsId = null;
		ParameterAssignationMode mode = paramInfo.getParamMode();

		if (paramInfo.getParamMode() == ParameterAssignationMode.CALLED_DATASET) {

			Long tcid = validator.getModel().getId(tc);
			String dsname = helper.truncate(paramInfo.getCalledDatasetName());
			Dataset ds = datasetDao.findByTestCaseIdAndName(tcid, dsname);

			// if the dataset exists we can actually bind the step to it.
			// otherwise we fallback to the default mode (nothing).
			// This later case has been dutifully reported by the
			// validator facility of course.
			if (ds != null) {
				dsId = ds.getId();
			} else {
				mode = ParameterAssignationMode.NOTHING;
			}

		}
		callstepService.setParameterAssignationMode(stepId, mode, dsId);
	}

	private DatasetParamValue findParamValue(DatasetTarget dataset, ParameterTarget param) {

		Dataset dbDs = findOrCreateDataset(dataset);
		Parameter dsParam = findParameter(param);

		for (DatasetParamValue dpv : dbDs.getParameterValues()) {
			if (dpv.getParameter().equals(dsParam)) {
				return dpv;
			}
		}

		// else we have to create it. Note that the services do not provide any
		// facility for that
		// so we have to do it from scratch here. Tsss, lazy conception again.
		DatasetParamValue dpv = new DatasetParamValue(dsParam, dbDs);
		paramvalueDao.save(dpv);
		dbDs.addParameterValue(dpv);

		return dpv;
	}

	@Override
	public LogTrain createCoverage(CoverageInstruction instr) {

		LogTrain train = validator.createCoverage(instr);

		if (!train.hasCriticalErrors()) {
			CoverageTarget target = instr.getTarget();
			Long reqId = reqFinderService.findNodeIdByPath(target.getReqPath());
			Requirement req = reqLibNavigationService.findRequirement(reqId);
			RequirementVersion reqVersion = req.findRequirementVersion(target.getReqVersion());

			Long tcId = navigationService.findNodeIdByPath(target.getTcPath());
			TestCase tc = testcaseModificationService.findById(tcId);

			RequirementVersionCoverage coverage = instr.getCoverage();
			coverage.setVerifiedRequirementVersion(reqVersion);
			coverage.setVerifyingTestCase(tc);

			coverageDao.persist(coverage);
		}

		return train;
	}

	@PostConstruct
	public void initializeFactories() {
		this.initializeCustomFieldTransator(customFieldTransator);
		testCaseFacility.initializeCustomFieldTransator(customFieldTransator);
		requirementFacility.initializeCustomFieldTransator(customFieldTransator);
                
        this.initializeValidator(validator);
        testCaseFacility.initializeValidator(validator);
        requirementFacility.initializeValidator(validator);
	}

}

