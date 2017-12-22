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
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStep;
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.domain.testcase.CallTestStep;
import org.squashtest.tm.domain.testcase.TestStep;
import org.squashtest.tm.domain.testcase.TestStepVisitor;


class TestStepViewFromExecBuilder implements TestStepVisitor {
	private TestStepViewFromExec testStepView;
	private ExecutionStep execStep;

	public TestStepViewFromExecBuilder() {
	}


	public TestStepViewFromExec buildTestStepViewFromExec(ExecutionStep execStep) {

		this.execStep = findExistingStep(execStep);


		TestStep testStep = this.execStep.getReferencedTestStep();

		testStep.accept(this);

		return testStepView;
	}


	/**
	 * Creates a model row from the visited item and stores it as {@link #lastBuiltItem}
	 */
	@Override
	public void visit(ActionTestStep visited) {
		testStepView = new TestStepViewFromExec(visited, execStep);

	}

	@Override
	public void visit(CallTestStep visited) {
		// not possible. The step is an ActionTestStep even if it's in fact a call step.

	}

	private ExecutionStep findExistingStep(ExecutionStep execStep) {

		Execution exec = execStep.getExecution();
		List<ExecutionStep> execSteps = new ArrayList<>(exec.getSteps());
		int stepIndex = exec.getStepIndex(execStep.getId());
		List<ExecutionStep> after = execSteps.subList(stepIndex, execSteps.size());

		// search for next existing step
		ExecutionStep result = (ExecutionStep) CollectionUtils.find(after, TestStepViewFromExec.NOT_DELETED);

		// if no step exist next, search the nearest previous step that still exist
		if (result == null) {
			List<ExecutionStep> before = execSteps.subList(0, stepIndex);
			// reverse the list so the first element found is the nearest
			Collections.reverse(before);
			result = (ExecutionStep) CollectionUtils.find(before, TestStepViewFromExec.NOT_DELETED);
		}


		return result;
	}


}
