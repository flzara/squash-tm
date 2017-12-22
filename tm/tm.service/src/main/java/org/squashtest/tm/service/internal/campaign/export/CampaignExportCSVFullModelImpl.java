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
package org.squashtest.tm.service.internal.campaign.export;

import org.apache.commons.collections.map.MultiValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.campaign.export.CampaignExportCSVModel;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.domain.customfield.InputType;
import org.squashtest.tm.domain.denormalizedfield.DenormalizedFieldValue;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStep;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.testcase.*;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.service.bugtracker.BugTrackersLocalService;
import org.squashtest.tm.service.customfield.CustomFieldHelper;
import org.squashtest.tm.service.customfield.CustomFieldHelperService;
import org.squashtest.tm.service.customfield.DenormalizedFieldHelper;
import org.squashtest.tm.service.feature.FeatureManager;
import org.squashtest.tm.service.feature.FeatureManager.Feature;
import org.squashtest.tm.service.internal.dto.NumericCufHelper;

import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.*;

/*
 * TODO :
 *
 * omg the amount of data processed here can become huge quickly and holding the whole model in memory might not be
 * workable anymore in the future. I suggest a more low level implementation using specific services, instead of being
 * lazy like here :
 *
 * - using hibernate cursors to maintain the size of the cache to an acceptable level, - iterate over the execution
 * steps directly instead of the clumsy iterator mechanics, - The datacells should return data only when requested -
 * fetch the number of issues for itp and test steps more efficiently !!
 */
@Component
@Scope("prototype")
public class CampaignExportCSVFullModelImpl implements WritableCampaignCSVModel {

	private static final Logger LOGGER = LoggerFactory.getLogger(CampaignExportCSVModel.class);

	@Inject
	private CustomFieldHelperService cufHelperService;

	@Inject
	private BugTrackersLocalService bugTrackerService;

	@Inject
	private FeatureManager featureManager;

	private char separator = ';';

	private Campaign campaign;

	private List<CustomField> campCUFModel;
	private List<CustomField> iterCUFModel;
	private List<CustomField> tcCUFModel;
	private List<CustomField> execCUFModel;
	private List<CustomField> execDenormalizedCUFModel;
	private List<CustomField> esCUFModel;
	private List<CustomField> esDenormalizedCUFModel;

	private List<CustomFieldValue> campCUFValues;
	private MultiValueMap iterCUFValues; // <Long, Collection<CustomFieldValue>>
	private MultiValueMap tcCUFValues; // same here
	private MultiValueMap execCUFValues; // same here
	private MultiValueMap execDenormalizedCUFValues; // same here
	private MultiValueMap esCUFValues; // same here
	private MultiValueMap esDenormalizedCUFValues; // same here

	private int nbColumns;
	private int nbRows;
	private boolean milestonesEnabled;

	public CampaignExportCSVFullModelImpl() {
		super();

	}

	@Override
	public void setCampaign(Campaign campaign) {
		this.campaign = campaign;
	}

	@Override
	public void setSeparator(char separator) {
		this.separator = separator;
	}

	@Override
	public char getSeparator() {
		return separator;
	}

	@Override
	public void init() {
		initCustomFields();
		milestonesEnabled = featureManager.isEnabled(Feature.MILESTONE);
	}

	private void initCustomFields() {

		LOGGER.info("campaign full export : processing model");

		List<Iteration> iterations = campaign.getIterations();
		List<TestCase> allTestCases = collectAllTestCases(iterations);
		List<Execution> allExecs = collectAllExecs(iterations);
		List<ExecutionStep> allExecSteps = collectLatestExecutionStep(iterations);
		nbRows = allExecSteps.size();

		// cufs for the campaign
		CustomFieldHelper<Campaign> campHelper = cufHelperService.newHelper(campaign);
		campCUFModel = campHelper.getCustomFieldConfiguration();
		campCUFValues = campHelper.getCustomFieldValues();

		// cufs for the iterations
		CustomFieldHelper<Iteration> iterHelper = cufHelperService.newHelper(iterations).includeAllCustomFields();
		iterCUFModel = iterHelper.getCustomFieldConfiguration();
		List<CustomFieldValue> iterValues = iterHelper.getCustomFieldValues();

		// cufs for the test cases
		CustomFieldHelper<TestCase> tcHelper = cufHelperService.newHelper(allTestCases).includeAllCustomFields();
		tcCUFModel = tcHelper.getCustomFieldConfiguration();
		List<CustomFieldValue> tcValues = tcHelper.getCustomFieldValues();

		// cufs for the executions
		CustomFieldHelper<Execution> execHelper = cufHelperService.newHelper(allExecs).includeAllCustomFields();
		execCUFModel = execHelper.getCustomFieldConfiguration();
		List<CustomFieldValue> execValues = execHelper.getCustomFieldValues();

		// denormalized cufs for the executions
		DenormalizedFieldHelper<Execution> execDenormalizedHelper = cufHelperService.newDenormalizedHelper(allExecs);
		execDenormalizedCUFModel = execDenormalizedHelper.getCustomFieldConfiguration();
		List<DenormalizedFieldValue> execDenormalizedValues = execDenormalizedHelper.getDenormalizedFieldValues();

		// cufs for the execution steps
		CustomFieldHelper<ExecutionStep> esHelper = cufHelperService.newHelper(allExecSteps).includeAllCustomFields();
		esCUFModel = esHelper.getCustomFieldConfiguration();
		List<CustomFieldValue> esValues = esHelper.getCustomFieldValues();

		// denormalized cuffs for the execution steps
		DenormalizedFieldHelper<ExecutionStep> esDenormalizedHelper = cufHelperService.newDenormalizedHelper(allExecSteps);
		esDenormalizedCUFModel = esDenormalizedHelper.getCustomFieldConfiguration();
		List<DenormalizedFieldValue> esDenormalizedValues = esDenormalizedHelper.getDenormalizedFieldValues();

		nbColumns = 35 + campCUFModel.size() + iterCUFModel.size() + tcCUFModel.size() + execCUFModel.size() +
			execDenormalizedCUFModel.size() + esCUFModel.size() + esDenormalizedCUFModel.size();

		// index the custom field values with a map for faster reference later
		createCustomFieldValuesIndex(iterValues, tcValues, execValues, execDenormalizedValues, esValues, esDenormalizedValues);

		LOGGER.info("campaign full export : model processed");

	}

	private List<TestCase> collectAllTestCases(List<Iteration> iterations) {
		// aggregate the test cases in one collection
		List<TestCase> allTestCases = new ArrayList<>();
		for (Iteration iteration : iterations) {
			addIterationTestCases(iteration, allTestCases);
		}
		return allTestCases;
	}

	private void addIterationTestCases(Iteration iteration, List<TestCase> allTestCases) {
		for (IterationTestPlanItem item : iteration.getTestPlans()) {
			if (!item.isTestCaseDeleted()) {
				allTestCases.add(item.getReferencedTestCase());
			}
		}
	}

	private List<Execution> collectAllExecs(List<Iteration> iterations) {
		// aggregate the executions in one collection
		List<Execution> allExecs = new ArrayList<>();
		for (Iteration iteration : iterations) {
			allExecs.addAll(iteration.getExecutions());
		}
		return allExecs;
	}

	private List<ExecutionStep> collectLatestExecutionStep(List<Iteration> iterations) {
		List<ExecutionStep> execSteps = new ArrayList<>();
		for (Iteration iteration : iterations) {
			for (IterationTestPlanItem item : iteration.getTestPlans()) {
				if (!item.isTestCaseDeleted() && item.getLatestExecution() != null) {
					execSteps.addAll(item.getLatestExecution().getSteps());
				}
			}
		}
		return execSteps;
	}

	private void createCustomFieldValuesIndex(List<CustomFieldValue> iterValues, List<CustomFieldValue> tcValues,
											  List<CustomFieldValue> execValues, List<DenormalizedFieldValue> execDenormalizedValues,
											  List<CustomFieldValue> esValues, List<DenormalizedFieldValue> esDenormalizedValues) {

		iterCUFValues = new MultiValueMap();
		tcCUFValues = new MultiValueMap();
		execCUFValues = new MultiValueMap();
		execDenormalizedCUFValues = new MultiValueMap();
		esCUFValues = new MultiValueMap();
		esDenormalizedCUFValues = new MultiValueMap();

		for (CustomFieldValue value : iterValues) {
			iterCUFValues.put(value.getBoundEntityId(), value);
		}

		for (CustomFieldValue value : tcValues) {
			tcCUFValues.put(value.getBoundEntityId(), value);
		}

		for (CustomFieldValue value : execValues) {
			execCUFValues.put(value.getBoundEntityId(), value);
		}

		for (DenormalizedFieldValue value : execDenormalizedValues) {
			execDenormalizedCUFValues.put(value.getDenormalizedFieldHolderId(), value);
		}

		for (CustomFieldValue value : esValues) {
			esCUFValues.put(value.getBoundEntityId(), value);
		}

		for (DenormalizedFieldValue value : esDenormalizedValues) {
			esDenormalizedCUFValues.put(value.getDenormalizedFieldHolderId(), value);
		}
	}

	@Override
	public Row getHeader() {

		List<CellImpl> headerCells = new ArrayList<>(nbColumns);

		// campaign fixed fields (4)
		headerCells.add(new CellImpl("CPG_SCHEDULED_START_ON"));
		headerCells.add(new CellImpl("CPG_SCHEDULED_END_ON"));
		headerCells.add(new CellImpl("CPG_ACTUAL_START_ON"));
		headerCells.add(new CellImpl("CPG_ACTUAL_END_ON"));

		// iteration fixed fields (7)
		headerCells.add(new CellImpl("IT_ID"));
		headerCells.add(new CellImpl("IT_NUM"));
		headerCells.add(new CellImpl("IT_NAME"));
		if (milestonesEnabled) {
			headerCells.add(new CellImpl("IT_MILESTONE"));
		}
		headerCells.add(new CellImpl("IT_SCHEDULED_START_ON"));
		headerCells.add(new CellImpl("IT_SCHEDULED_END_ON"));
		headerCells.add(new CellImpl("IT_ACTUAL_START_ON"));
		headerCells.add(new CellImpl("IT_ACTUAL_END_ON"));

		// test case fixed fields (17)
		headerCells.add(new CellImpl("TC_ID"));
		headerCells.add(new CellImpl("TC_NAME"));
		headerCells.add(new CellImpl("TC_PROJECT_ID"));
		headerCells.add(new CellImpl("TC_PROJECT"));
		if (milestonesEnabled) {
			headerCells.add(new CellImpl("TC_MILESTONE"));
		}
		headerCells.add(new CellImpl("TC_WEIGHT"));
		headerCells.add(new CellImpl("TEST_SUITE"));
		headerCells.add(new CellImpl("#_EXECUTIONS"));
		headerCells.add(new CellImpl("#_REQUIREMENTS"));
		headerCells.add(new CellImpl("#_ISSUES"));
		headerCells.add(new CellImpl("DATASET"));
		headerCells.add(new CellImpl("EXEC_STATUS"));
		headerCells.add(new CellImpl("EXEC_USER"));
		headerCells.add(new CellImpl("EXECUTION_DATE"));
		headerCells.add(new CellImpl("TC_REF"));
		headerCells.add(new CellImpl("TC_NATURE"));
		headerCells.add(new CellImpl("TC_TYPE"));
		headerCells.add(new CellImpl("TC_STATUS"));

		// test step fixed fields (8)
		headerCells.add(new CellImpl("STEP_ID"));
		headerCells.add(new CellImpl("STEP_NUM"));
		headerCells.add(new CellImpl("STEP_#_REQ"));
		headerCells.add(new CellImpl("EXEC_STEP_STATUS"));
		headerCells.add(new CellImpl("EXEC_STEP_DATE"));
		headerCells.add(new CellImpl("EXEC_STEP_USER"));
		headerCells.add(new CellImpl("EXEC_STEP_#_ISSUES"));
		headerCells.add(new CellImpl("EXEC_STEP_COMMENT"));

		// campaign custom fields
		for (CustomField cufModel : campCUFModel) {
			headerCells.add(new CellImpl("CPG_CUF_" + cufModel.getCode()));
		}

		// iteration custom fields
		for (CustomField cufModel : iterCUFModel) {
			headerCells.add(new CellImpl("IT_CUF_" + cufModel.getCode()));
		}

		// test case custom fields
		for (CustomField cufModel : tcCUFModel) {
			headerCells.add(new CellImpl("TC_CUF_" + cufModel.getCode()));
		}

		// execution custom fields
		for (CustomField cufModel : execCUFModel) {
			headerCells.add(new CellImpl("EXEC_CUF_" + cufModel.getCode()));
		}

		// execution denormalized custom fields
		for (CustomField cufModel : execDenormalizedCUFModel) {
			headerCells.add(new CellImpl("EXEC_CUF_FROM_TC_" + cufModel.getCode()));
		}

		// execution steps custom fields
		for (CustomField cufModel : esCUFModel) {
			headerCells.add(new CellImpl("STEP_CUF_" + cufModel.getCode()));
		}

		// execution steps denormalized custom fields
		for (CustomField cufModel : esDenormalizedCUFModel) {
			headerCells.add(new CellImpl("STEP_CUF_FROM_TC_" + cufModel.getCode()));
		}

		return new RowImpl(headerCells, separator);

	}

	@Override
	public Iterator<Row> dataIterator() {
		return new DataIterator();
	}

	// ********************************** nested classes ********************************************

	private class DataIterator implements Iterator<Row> {

		private static final String N_A = "n/a";
		// initial state : null is a meaningful value here.
		private Iteration iteration = null;
		private IterationTestPlanItem itp = null;
		private Execution exec = null;
		private ExecutionStep execStep = null;
		private ActionTestStep actionTestStep = null;

		private int iterIndex = -1;
		private int itpIndex = -1;
		private int stepIndex = -1;
		private int actionTestStepIndex = -1;

		private boolean globalHasNext = true;

		private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

		// ** caching **
		// slight optimization, but will not make up for the need for refactoring (see comments on top)

		private List<CellImpl> cachedItpcellFixed = new ArrayList<>(17);
		private List<CellImpl> cachedItpcellCuf = new ArrayList<>(iterCUFModel.size());
		private boolean cachedItpcellReady = false;

		private int logcount = 0;

		public DataIterator() {

			super();
			moveToNextStep();

		}

		// ************************************** model population ******************************

		// See getHeader() for reference
		@Override
		public Row next() {

			List<CellImpl> dataCells = new ArrayList<>(nbColumns);

			// the campaign
			populateCampaignFixedRowData(dataCells);

			// the iteration
			populateIterationFixedRowData(dataCells);

			// the test case
			populateTestCaseFixedRowData(dataCells);

			// the step
			populateTestStepFixedRowData(dataCells);

			// the campaign custom fields
			populateCampaignCUFRowData(dataCells);

			// the iteration custom fields
			populateIterationCUFRowData(dataCells);

			// the test case custom fields
			populateTestCaseCUFRowData(dataCells);

			// the execution steps custom fields
			populateExecutionCUFRowData(dataCells);

			// the execution steps custom fields
			populateExecutionDenormalizedCUFRowData(dataCells);

			// the execution steps custom fields
			populateExecutionStepCUFRowData(dataCells);

			// the execution steps custom fields
			populateExecutionStepDenormalizedCUFRowData(dataCells);

			// move to the next occurence
			moveToNextStep();

			// for logging purposes
			logcount++;
			if (logcount % 99 == 0) {
				LOGGER.info("campaign full export : processed " + (logcount + 1) + " lines out of " + (nbRows + 1)
					+ " (maximum estimate)");
			}

			return new RowImpl(dataCells, separator);

		}

		@SuppressWarnings("unchecked")
		private void populateExecutionCUFRowData(List<CellImpl> dataCells) {
			Execution exe = exec;
			if (exe != null) {

				Collection<CustomFieldValue> execValues = (Collection<CustomFieldValue>) execCUFValues
					.get(exec.getId());
				for (CustomField model : execCUFModel) {
					String strValue = getValue(execValues, model);
					dataCells.add(new CellImpl(strValue));
				}
			}
		}

		@SuppressWarnings("unchecked")
		private void populateExecutionDenormalizedCUFRowData(List<CellImpl> dataCells) {
			Execution exe = exec;
			if (exe != null) {

				Collection<DenormalizedFieldValue> execDenormalizedValues = (Collection<DenormalizedFieldValue>) execDenormalizedCUFValues
					.get(exec.getId());
				for (CustomField model : execDenormalizedCUFModel) {
					String strValue = getDenormalizedValue(execDenormalizedValues, model);
					dataCells.add(new CellImpl(strValue));
				}
			}
		}

		@SuppressWarnings("unchecked")
		private void populateExecutionStepCUFRowData(List<CellImpl> dataCells) {
			ExecutionStep eStep = execStep;
			if (eStep != null) {
				Collection<CustomFieldValue> esValues = (Collection<CustomFieldValue>) esCUFValues
					.get(execStep.getId());
				for (CustomField model : esCUFModel) {
					String strValue = getValue(esValues, model);
					dataCells.add(new CellImpl(strValue));
				}
			}
		}

		@SuppressWarnings("unchecked")
		private void populateExecutionStepDenormalizedCUFRowData(List<CellImpl> dataCells) {
			ExecutionStep eStep = execStep;
			if (eStep != null) {

				Collection<DenormalizedFieldValue> esDenormalizedValues = (Collection<DenormalizedFieldValue>) esDenormalizedCUFValues
					.get(execStep.getId());
				for (CustomField model : esDenormalizedCUFModel) {
					String strValue = getDenormalizedValue(esDenormalizedValues, model);
					dataCells.add(new CellImpl(strValue));
				}
			}
		}

		@SuppressWarnings("unchecked")
		private void populateTestCaseCUFRowData(List<CellImpl> dataCells) {

			if (cachedItpcellReady) {
				dataCells.addAll(cachedItpcellCuf);
			} else {
				TestCase testCase = itp.getReferencedTestCase();

				Collection<CustomFieldValue> tcValues = (Collection<CustomFieldValue>) tcCUFValues
					.get(testCase.getId());

				for (CustomField model : tcCUFModel) {
					String strValue = getValue(tcValues, model);
					CellImpl cell = new CellImpl(strValue);
					dataCells.add(cell);
					cachedItpcellCuf.add(cell);
				}
				cachedItpcellReady = true;
			}
		}

		@SuppressWarnings("unchecked")
		private void populateIterationCUFRowData(List<CellImpl> dataCells) {
			Collection<CustomFieldValue> iValues = (Collection<CustomFieldValue>) iterCUFValues.get(iteration.getId());
			for (CustomField model : iterCUFModel) {
				String strValue = getValue(iValues, model);
				dataCells.add(new CellImpl(strValue));
			}
		}

		private void populateCampaignCUFRowData(List<CellImpl> dataCells) {
			List<CustomFieldValue> cValues = campCUFValues;
			// ensure that the CUF values are processed in the correct order
			for (CustomField model : campCUFModel) {
				String strValue = getValue(cValues, model);
				dataCells.add(new CellImpl(strValue));
			}
		}

		private void populateTestStepFixedRowData(List<CellImpl> dataCells) {


			if (execStep == null && actionTestStep != null) {
				dataCells.add(new CellImpl(N_A));
				dataCells.add(new CellImpl(String.valueOf(actionTestStepIndex + 1)));
				dataCells.add(new CellImpl(formatStepRequirements()));
				dataCells.add(new CellImpl(N_A));
				dataCells.add(new CellImpl(N_A));
				dataCells.add(new CellImpl(N_A));
				dataCells.add(new CellImpl(N_A));
				dataCells.add(new CellImpl(N_A));

			} else if (execStep != null) {
				dataCells.add(new CellImpl(Long.toString(execStep.getId())));
				dataCells.add(new CellImpl(String.valueOf(stepIndex + 1)));
				dataCells.add(new CellImpl(formatStepRequirements()));
				dataCells.add(new CellImpl(execStep.getExecutionStatus().toString()));
				dataCells.add(new CellImpl(formatDate(execStep.getLastExecutedOn())));
				dataCells.add(new CellImpl(formatUser(execStep.getLastExecutedBy())));
				dataCells.add(new CellImpl(Integer.toString(getNbIssues(execStep)))); // XXX THIS IS WAAAAAAAY TOO
				// EXPENSIVE !
				dataCells.add(new CellImpl(formatLongText(execStep.getComment())));

			} else {
				/* Issue 6351: We also have to import ITPI without any Test Step. */
				dataCells.add(new CellImpl(N_A));
				dataCells.add(new CellImpl(N_A));
				dataCells.add(new CellImpl(N_A));
				dataCells.add(new CellImpl(N_A));
				dataCells.add(new CellImpl(N_A));
				dataCells.add(new CellImpl(N_A));
				dataCells.add(new CellImpl(N_A));
				dataCells.add(new CellImpl(N_A));
			}
		}

		private String formatLongText(String text) {
			return text == null ? "" : text.trim();
		}

		private int getNbIssues(ExecutionStep execStep) {

			return bugTrackerService.findNumberOfIssueForExecutionStep(execStep.getId());
		}

		private void populateTestCaseFixedRowData(List<CellImpl> dataCells) {

			if (cachedItpcellReady) {
				dataCells.addAll(cachedItpcellFixed);
			} else {

				TestCase testCase = itp.getReferencedTestCase();

				cachedItpcellFixed.add(new CellImpl(testCase.getId().toString()));
				cachedItpcellFixed.add(new CellImpl(testCase.getName()));
				cachedItpcellFixed.add(new CellImpl(testCase.getProject().getId().toString()));
				cachedItpcellFixed.add(new CellImpl(testCase.getProject().getName()));
				if (milestonesEnabled) {
					cachedItpcellFixed.add(new CellImpl(formatMilestone(testCase.getMilestones())));
				}
				cachedItpcellFixed.add(new CellImpl(testCase.getImportance().toString()));
				cachedItpcellFixed.add(new CellImpl(itp.getTestSuiteNames().replace(", ", ",").replace("<", "&lt;")
					.replace(">", "&gt;")));

				cachedItpcellFixed.add(new CellImpl(Integer.toString(itp.getExecutions().size())));
				cachedItpcellFixed
					.add(new CellImpl(Integer.toString(testCase.getRequirementVersionCoverages().size())));
				cachedItpcellFixed.add(new CellImpl(Integer.toString(getNbIssues(itp))));
				cachedItpcellFixed.add(new CellImpl((itp.getReferencedDataset() == null) ? "" : itp.getReferencedDataset().getName()));

				cachedItpcellFixed.add(new CellImpl(itp.getExecutionStatus().toString()));
				cachedItpcellFixed.add(new CellImpl(formatUser(itp.getUser())));
				cachedItpcellFixed.add(new CellImpl(formatDate(itp.getLastExecutedOn())));

				cachedItpcellFixed.add(new CellImpl(testCase.getReference()));
				cachedItpcellFixed.add(new CellImpl(testCase.getNature().getCode()));
				cachedItpcellFixed.add(new CellImpl(testCase.getType().getCode()));
				cachedItpcellFixed.add(new CellImpl(testCase.getStatus().toString()));

				dataCells.addAll(cachedItpcellFixed);

			}

		}

		private void populateIterationFixedRowData(List<CellImpl> dataCells) {

			dataCells.add(new CellImpl(iteration.getId().toString()));
			dataCells.add(new CellImpl(String.valueOf(iterIndex + 1)));
			dataCells.add(new CellImpl(iteration.getName()));
			if (milestonesEnabled) {
				dataCells.add(new CellImpl(formatMilestone(iteration.getMilestones())));
			}
			dataCells.add(new CellImpl(formatDate(iteration.getScheduledStartDate())));
			dataCells.add(new CellImpl(formatDate(iteration.getScheduledEndDate())));
			dataCells.add(new CellImpl(formatDate(iteration.getActualStartDate())));
			dataCells.add(new CellImpl(formatDate(iteration.getActualEndDate())));

		}

		private void populateCampaignFixedRowData(List<CellImpl> dataCells) {

			dataCells.add(new CellImpl(formatDate(campaign.getScheduledStartDate())));
			dataCells.add(new CellImpl(formatDate(campaign.getScheduledEndDate())));
			dataCells.add(new CellImpl(formatDate(campaign.getActualStartDate())));
			dataCells.add(new CellImpl(formatDate(campaign.getActualEndDate())));

		}

		private String formatMilestone(Set<Milestone> milestones) {

			StringBuilder sb = new StringBuilder();
			for (Milestone m : milestones) {
				sb.append(m.getLabel());
				sb.append("|");
			}
			sb.setLength(Math.max(sb.length() - 1, 0));

			return sb.toString();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		// ******************************** data formatting ***************************

		// returns the correct value if found, or "" if not found
		private String getValue(Collection<CustomFieldValue> values, CustomField model) {

			if (values != null) {
				for (CustomFieldValue value : values) {
					CustomField customField = value.getBinding().getCustomField();
					if (customField.getCode().equals(model.getCode())) {
						if (customField.getInputType().equals(InputType.NUMERIC)) {
							return NumericCufHelper.formatOutputNumericCufValue(value.getValue());
						}
						return value.getValue();
					}
				}
			}

			return "";
		}

		private String getDenormalizedValue(Collection<DenormalizedFieldValue> values, CustomField model) {

			if (values != null) {
				for (DenormalizedFieldValue value : values) {
					if (value.getCode().equals(model.getCode())) {
						return value.getValue();
					}
				}
			}

			return "";
		}

		private int getNbIssues(IterationTestPlanItem itp) {

			return bugTrackerService.findNumberOfIssueForItemTestPlanLastExecution(itp.getId());

		}

		private String formatDate(Date date) {

			return date == null ? "" : dateFormat.format(date);

		}

		private String formatUser(User user) {
			return user == null ? "" : user.getLogin();

		}

		private String formatUser(String username) {
			return username == null ? "" : username;
		}

		private String formatStepRequirements() {
			String res;
			try {
				if (execStep != null && execStep.getReferencedTestStep() != null) {
					/*
					 * should fix the mapping of execution steps -> action step : an execution step cannot reference a
					 * call step by design. For now we'll just downcast the TestStep instance.
					 */
					ActionTestStep aStep = (ActionTestStep) execStep.getReferencedTestStep();
					res = Integer.toString(aStep.getRequirementVersionCoverages().size());
				} else if (actionTestStep != null) {

					res = Integer.toString(actionTestStep.getRequirementVersionCoverages().size());

				} else {
					res = "?";
				}

			} catch (NullPointerException npe) {
				res = "?";
			}

			return res;
		}

		// ****************** hairy iterator mechanics here ****************

		@Override
		public boolean hasNext() {
			return globalHasNext;
		}

		private void moveToNextStep() {

			boolean foundNextStep = false;
			boolean nextTCSucc;

			do {
				// test if we must move to the next test case
				if (execStep == null && actionTestStep == null) {
					nextTCSucc = moveToNextTestCase();
					if (!nextTCSucc) {
						// that was the last test case and we cannot iterate further more : we break the loop forcibly
						globalHasNext = false;
						return;
					} else {
						resetCachedItpcell();
						resetStepIndex();
						resetActionTestStepIndex();
					}
				}

				// find a suitable execution step
				if (itp.getLatestExecution() != null) {
					exec = itp.getLatestExecution();
					List<ExecutionStep> steps = itp.getLatestExecution().getSteps();

					int stepsSize = steps.size();
					stepIndex++;

					if (stepIndex < stepsSize) {
						execStep = steps.get(stepIndex);
						actionTestStep = null;
						foundNextStep = true;
					} else {
						execStep = null;
						actionTestStep = null;
					}

				} else {
					exec = null;
					TestCase testCase = itp.getReferencedTestCase();
					List<ActionTestStep> actiontestSteps = getActionTestStepList(testCase);

					int actionTestStepSize = actiontestSteps.size();
					actionTestStepIndex++;

					if (actionTestStepIndex < actionTestStepSize) {
						actionTestStep = actiontestSteps.get(actionTestStepIndex);
						execStep = null;
						foundNextStep = true;
					/* Issue 6351: We also have to import ITPI without any Test Step. */
					} else if (actionTestStepSize == 0) {
						actionTestStep = null;
						execStep = null;
						foundNextStep = true;
					} else {
						execStep = null;
						actionTestStep = null;
					}

					execStep = null;
				}

			} while (!foundNextStep);

		}

		private List<ActionTestStep> getActionTestStepList(TestCase testCase) {

			List<ActionTestStep> result = new ArrayList<>();
			List<TestStep> steps = testCase.getSteps();

			result.addAll(getActionTestStepListRec(steps));

			return result;
		}

		private List<ActionTestStep> getActionTestStepListRec(List<TestStep> steps) {

			List<ActionTestStep> result = new ArrayList<>();
			TestStepExaminer examiner = new TestStepExaminer();

			for (TestStep step : steps) {
				step.accept(examiner);
				if (examiner.isActionStep()) {
					result.add((ActionTestStep) step);
				} else {
					CallTestStep callStep = (CallTestStep) step;
					TestCase testCase = callStep.getCalledTestCase();
					result.addAll(getActionTestStepList(testCase));
				}
			}

			return result;
		}

		private final class TestStepExaminer implements TestStepVisitor {

			private boolean isActionStep;

			public boolean isActionStep() {
				return isActionStep;
			}

			@Override
			public void visit(ActionTestStep visited) {
				this.isActionStep = true;
			}

			@Override
			public void visit(CallTestStep visited) {
				this.isActionStep = false;
			}
		}

		private boolean moveToNextTestCase() {

			boolean foundNextTC;
			boolean nextIterSucc;

			do {
				// test if we must move to the next iteration
				if (itp == null) {
					nextIterSucc = moveToNextIteration();
					if (!nextIterSucc) {
						return false;
					} else {
						resetTCIndex();
					}
				}

				// find a suitable execution step
				List<IterationTestPlanItem> items = iteration.getTestPlans();
				int itemSize = items.size();
				itpIndex++;

				// see if we reached the end of the collection
				if (itpIndex >= itemSize) {
					itp = null;
					foundNextTC = false;
				}
				// check that the test case wasn't deleted
				else if (items.get(itpIndex).isTestCaseDeleted()) {
					foundNextTC = false;
				} else {
					itp = items.get(itpIndex);
					foundNextTC = true;
				}

			} while (!foundNextTC);

			return foundNextTC;
		}

		private boolean moveToNextIteration() {

			boolean foundIter = false;
			iterIndex++;

			List<Iteration> iterations = campaign.getIterations();
			int iterSize = iterations.size();

			if (iterIndex < iterSize) {
				iteration = iterations.get(iterIndex);
				foundIter = true;
			}

			return foundIter;

		}

		private void resetStepIndex() {
			stepIndex = -1;
		}

		private void resetActionTestStepIndex() {
			actionTestStepIndex = -1;
		}

		private void resetTCIndex() {
			itpIndex = -1;
		}

		private void resetCachedItpcell() {
			cachedItpcellFixed.clear();
			cachedItpcellCuf.clear();
			cachedItpcellReady = false;
		}

	}

	// ******************** implementation for the rows and cells **********************

}
