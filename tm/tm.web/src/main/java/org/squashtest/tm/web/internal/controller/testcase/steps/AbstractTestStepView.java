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
import org.squashtest.tm.domain.testcase.TestCase;

public class AbstractTestStepView<T> {
	protected long id;
	protected TestCase testCase;
	protected int totalNumberOfSteps;
	protected int order;
	protected T previousStep;
	protected T nextStep;
	protected ActionTestStep actionStep;
	protected CallTestStep callStep;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public TestCase getTestCase() {
		return testCase;
	}

	public void setTestCase(TestCase testCase) {
		this.testCase = testCase;
	}

	public ActionTestStep getActionStep() {
		return actionStep;
	}

	public void setActionStep(ActionTestStep actionStep) {
		this.actionStep = actionStep;
	}

	public CallTestStep getCallStep() {
		return callStep;
	}

	public void setCallStep(CallTestStep callStep) {
		this.callStep = callStep;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public T getPreviousStep() {
		return previousStep;
	}

	public void setPreviousStep(T previousStep) {
		this.previousStep = previousStep;
	}

	public T getNextStep() {
		return nextStep;
	}

	public void setNextStep(T nextStep) {
		this.nextStep = nextStep;
	}

	public int getTotalNumberOfSteps() {
		return totalNumberOfSteps;
	}

	public void setTotalNumberOfSteps(int totalNumberOfSteps) {
		this.totalNumberOfSteps = totalNumberOfSteps;
	}

}
