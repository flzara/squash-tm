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
package org.squashtest.tm.web.internal.model.rest;

import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.domain.testcase.CallTestStep;
import org.squashtest.tm.domain.testcase.TestStep;
import org.squashtest.tm.domain.testcase.TestStepVisitor;


public class RestTestStep {

	private String action;

	private String expectedResult;

	private RestTestCaseStub calledTestCase;

	public RestTestStep() {
		super();
	}

	public RestTestStep(TestStep testStep) {
		Visitor visitor = new Visitor();
		testStep.accept(visitor);
	}

	private class Visitor implements TestStepVisitor{

		@Override
		public void visit(ActionTestStep visited) {
			setAction(visited.getAction());
			setExpectedResult(visited.getExpectedResult());
		}

		@Override
		public void visit(CallTestStep visited) {
			setCalledTestCase(new RestTestCaseStub(visited.getCalledTestCase()));
		}

	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getExpectedResult() {
		return expectedResult;
	}

	public void setExpectedResult(String expectedResult) {
		this.expectedResult = expectedResult;
	}

	public RestTestCaseStub getCalledTestCase() {
		return calledTestCase;
	}

	public void setCalledTestCase(RestTestCaseStub calledTestCase) {
		this.calledTestCase = calledTestCase;
	}
}
