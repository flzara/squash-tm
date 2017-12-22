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
package org.squashtest.tm.service.internal.execution;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.attachment.Attachment;
import org.squashtest.tm.domain.customfield.*;
import org.squashtest.tm.domain.denormalizedfield.DenormalizedFieldValue;
import org.squashtest.tm.domain.denormalizedfield.DenormalizedSingleSelectField;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.execution.ExecutionStep;
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.domain.testcase.Dataset;
import org.squashtest.tm.domain.testcase.DatasetParamValue;
import org.squashtest.tm.service.denormalizedfield.DenormalizedFieldValueManager;
import org.squashtest.tm.service.execution.ExecutionProcessingService;
import org.squashtest.tm.service.internal.denormalizedField.PrivateDenormalizedFieldValueService;
import org.squashtest.tm.service.internal.repository.AttachmentDao;
import org.squashtest.tm.service.internal.repository.CustomFieldValueDao;
import org.squashtest.tm.service.internal.repository.ExecutionStepDao;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class ExecutionStepModificationHelper {

	@Inject
	private AttachmentDao attachmentDao;

	@Inject
	private DenormalizedFieldValueManager denormalizedFieldValueManager;

	@Inject
	private CustomFieldValueDao customFieldValueDao;

	@Inject
	private ExecutionStepDao executionStepDao;

	@Inject
	private PrivateDenormalizedFieldValueService privateDenormalizedFieldValueService;

	@Inject
	private ExecutionProcessingService executionProcessingService;


	public long doUpdateStep(List<ExecutionStep> toBeUpdated, Execution execution) {

		long firstModifiedIndex = -1;

		for (ExecutionStep execStep : toBeUpdated) {
			ActionTestStep step = (ActionTestStep) execStep.getReferencedTestStep();

			if (step == null) {
				execution.removeStep(execStep.getId());
				continue;
			}

			firstModifiedIndex = firstModifiedIndex < 0 ? execution.getStepIndex(execStep.getId()) : firstModifiedIndex;

			Dataset dataset = execution.getTestPlan().getReferencedDataset();
			if (dataset != null) {
				execStep.fillParameterMap(dataset);
			}
			step.accept(execStep);
			executionProcessingService.changeExecutionStepStatus(execStep.getId(), ExecutionStatus.READY);

			privateDenormalizedFieldValueService.deleteAllDenormalizedFieldValues(execStep);
			privateDenormalizedFieldValueService.createAllDenormalizedFieldValues(step, execStep);

			// We need to remove attachment first, then clear the list.
			// All attachment are removed then added, this may be suboptimal.
			// Maybe some optimization may be required later.
			attachmentDao.removeAll(new ArrayList<>(execStep.getAttachmentList().getAllAttachments()));
			execStep.getAttachmentList().getAllAttachments().clear();

			for (Attachment actionStepAttach : step.getAllAttachments()) {
				Attachment clone = actionStepAttach.hardCopy();
				execStep.getAttachmentList().addAttachment(clone);
			}
			executionStepDao.persist(execStep);
		}
		return firstModifiedIndex;

	}

	public List<ExecutionStep> findStepsToUpdate(Execution execution) {
		List<ExecutionStep> execSteps = execution.getSteps();
		List<ExecutionStep> toBeUpdated = new ArrayList<>();

		for (ExecutionStep eStep : execSteps) {
			ActionTestStep aStep = (ActionTestStep) eStep.getReferencedTestStep();
			// 6797 : Status of executionStep was lost if step was containing parameters.
			// eStep ==> paramteres' value but aStep ==> ${param}, then eStep was adding to the 'toBeUpdated' list.
			if (aStep != null) {
				if (isExecutionWithParameters(aStep) && isExecutionWithDataset(execution)) {
					Dataset dataset = execution.getTestPlan().getReferencedDataset();
					changeStepParamsByValue(eStep, aStep, dataset, toBeUpdated);
				} else {
					if (!isStepEqual(eStep, aStep)) {
						toBeUpdated.add(eStep);
					}
				}
			}
		}

		return toBeUpdated;
	}

	private boolean isExecutionWithParameters(ActionTestStep aStep) {
		return aStep.findUsedParametersNames() != null;
	}

	private boolean isExecutionWithDataset(Execution execution) {
		if (execution.getTestPlan() != null) {
			if (execution.getTestPlan().getReferencedDataset() != null) {
				return true;
			}
		}
		return false;
	}

	private void changeStepParamsByValue(ExecutionStep eStep, ActionTestStep aStep, Dataset dataset, List<ExecutionStep> toBeUpdated) {
		String action = aStep.getAction();
		String expectedResult = aStep.getExpectedResult();
		Set<String> params = aStep.findUsedParametersNames();
		for (String param : params) {
			for (DatasetParamValue dpv : dataset.getParameterValues()) {
				changeStepParamByValue(aStep, param, dpv);
			}
		}
		if (!isStepEqual(eStep, aStep)) {
			toBeUpdated.add(eStep);
		}
		reinitiateActionTestStep(aStep, action, expectedResult);
	}

	private void changeStepParamByValue(ActionTestStep aStep, String param, DatasetParamValue dpv) {
		String paramValue = "";
		if (dpv.getParameter().getName().equals(param)) {
			paramValue = dpv.getParamValue();
			if (aStep.getExpectedResult().contains(param)) {
				aStep.setExpectedResult(aStep.getExpectedResult().replace("${" + param + "}", paramValue));
			}
			aStep.setAction(aStep.getAction().replace("${" + param + "}", paramValue));
		}
	}

	private void reinitiateActionTestStep(ActionTestStep aStep, String action, String expectedResult) {
		aStep.setAction(action);
		aStep.setExpectedResult(expectedResult);
	}

	private boolean isStepEqual(ExecutionStep eStep, ActionTestStep aStep) {
		return actionStepExist(aStep) && sameAction(eStep, aStep) && sameResult(eStep, aStep)
			&& sameAttach(eStep, aStep) && sameCufs(eStep, aStep);
	}

	private boolean sameCufs(ExecutionStep eStep, ActionTestStep aStep) {

		List<DenormalizedFieldValue> denormalizedFieldValues = denormalizedFieldValueManager.findAllForEntity(eStep);

		List<CustomFieldValue> originalValues = customFieldValueDao.findAllCustomValues(aStep.getId(),
			BindableEntity.TEST_STEP);

		// different number of CUF
		if (originalValues.size() != denormalizedFieldValues.size()) {
			return false;
		}

		for (DenormalizedFieldValue denormVal : denormalizedFieldValues) {

			CustomFieldValue origVal = denormVal.getCustomFieldValue();
			if (origVal == null || hasChanged(denormVal, origVal)) {
				return false;
			}

		}

		return true;
	}


	private boolean hasChanged(final DenormalizedFieldValue denormVal, final CustomFieldValue origVal) {

		final boolean[] hasChanged = {false};

		origVal.getCustomField().accept(new CustomFieldVisitor() {

			private void testValChange() {
				if (valueHasChanged(denormVal, origVal)) {
					hasChanged[0] = true;
				}
			}

			@Override
			public void visit(MultiSelectField multiSelect) {
				testValChange();
			}

			@Override
			public void visit(RichTextField richField) {
				testValChange();
			}

			@Override
			public void visit(NumericField numericField) {
				testValChange();
			}

			@Override
			public void visit(CustomField standardValue) {
				testValChange();
			}

			@Override
			public void visit(SingleSelectField selectField) {
				testValChange();
				testOptionsChange(selectField);
			}

			private void testOptionsChange(SingleSelectField selectField) {

				DenormalizedSingleSelectField denormSSF = (DenormalizedSingleSelectField) denormVal;

				if (!CollectionUtils.isEqualCollection(denormSSF.getOptions(), selectField.getOptions())) {
					hasChanged[0] = true;
				}
			}
		});


		return hasChanged[0];
	}

	private boolean valueHasChanged(DenormalizedFieldValue denormVal, CustomFieldValue origVal) {
		return !denormVal.getValue().equals(origVal.getValue());
	}

	private boolean actionStepExist(ActionTestStep aStep) {
		return aStep != null;
	}

	private boolean sameAction(ExecutionStep eStep, ActionTestStep aStep) {
		return eStep.getAction().equals(aStep.getAction());
	}

	private boolean sameResult(ExecutionStep eStep, ActionTestStep aStep) {
		return eStep.getExpectedResult().equals(aStep.getExpectedResult());
	}

	private boolean sameAttach(ExecutionStep eStep, ActionTestStep aStep) {

		Set<Attachment> eStepAttach = eStep.getAttachmentList().getAllAttachments();
		Set<Attachment> aStepAttach = aStep.getAllAttachments();

		if (eStepAttach.size() != aStepAttach.size()) {
			return false;
		}

		for (final Attachment aAttach : aStepAttach) {

			boolean exist = CollectionUtils.exists(eStepAttach, new Predicate() {

				@Override
				public boolean evaluate(Object eAttach) {
					Attachment toCompare = (Attachment) eAttach;
					boolean sameName = toCompare.getName().equals(aAttach.getName());
					boolean sameSize = toCompare.getSize().equals(aAttach.getSize());
					return sameName && sameSize;
				}
			});

			if (!exist) {
				return false;
			}

		}

		return true;
	}

}
