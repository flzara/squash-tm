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
package org.squashtest.tm.web.internal.controller.testcase.steps;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStep;
import org.squashtest.tm.domain.testcase.ActionTestStep;

public class TestStepViewFromExec extends AbstractTestStepView<ExecutionStep> {

	public static final Predicate NOT_DELETED = new Predicate() {
		@Override
		public boolean evaluate(Object step) {
			return ((ExecutionStep) step).getReferencedTestStep() != null;
		}
	};
	private boolean isCallStep = false;

	private String callStepName;

	public String getCallStepName() {
		return callStepName;
	}

	public boolean isCallStep() {
		return isCallStep;
	}

	public TestStepViewFromExec(ActionTestStep visited, ExecutionStep execStep) {
		genericSettings(execStep);
		actionStep = visited;
	}


	private void genericSettings(ExecutionStep execStep) {
		Execution exec = execStep.getExecution();
		List<ExecutionStep> execSteps = getNonDeletedSteps(execStep);
		int size = execSteps.size();
		testCase = exec.getReferencedTestCase();
		if (!testCase.getId().equals(execStep.getReferencedTestStep().getTestCase().getId())) {
			isCallStep = true;
			callStepName = execStep.getReferencedTestStep().getTestCase().getName();
		}
		setTotalNumberOfSteps(size);
		int stepIndex = getStepIndex(execSteps, execStep.getId());

		order = stepIndex + 1;

		if (stepIndex > 0) {
			previousStep = execSteps.get(stepIndex - 1);
		}

		if (order < size) {
			nextStep = execSteps.get(stepIndex + 1);
		}

		id = execStep.getReferencedTestStep().getId();

	}


	private int getStepIndex(List<ExecutionStep> steps, Long stepId) {

		for (ExecutionStep step : steps) {
			if (step.getId().equals(stepId)) {
				return steps.indexOf(step);
			}
		}

		return -1;
	}

	private List<ExecutionStep> getNonDeletedSteps(ExecutionStep execStep) {
		Execution exec = execStep.getExecution();
		List<ExecutionStep> execSteps = new ArrayList<>(exec.getSteps());
		removeDeletedSteps(execSteps);
		return execSteps;
	}



	private void removeDeletedSteps(List<ExecutionStep> allSteps) {

		CollectionUtils.filter(allSteps, NOT_DELETED);
	}
}
