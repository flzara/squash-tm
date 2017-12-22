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

import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.domain.testcase.CallTestStep;
import org.squashtest.tm.domain.testcase.TestStep;

// made "final" because SONAR wants to be sure that subclasses wont mess with overrides and constructors
public final class TestStepView extends AbstractTestStepView<TestStep> {

	public TestStepView(ActionTestStep step) {
		genericSettings(step);
		actionStep = step;

	}

	public TestStepView(CallTestStep step) {
		genericSettings(step);
		callStep = step;

	}

	private void genericSettings(TestStep step) {
		testCase = step.getTestCase();
		setTotalNumberOfSteps(testCase.getSteps().size());
		int stepIndex = testCase.getPositionOfStep(step.getId());

		order = stepIndex +1;

		if(stepIndex > 0){
			previousStep = testCase.getSteps().get(stepIndex - 1);
		}

		if(order < testCase.getSteps().size()){
			nextStep = testCase.getSteps().get(stepIndex + 1);
		}

		id = step.getId();
	}



}
