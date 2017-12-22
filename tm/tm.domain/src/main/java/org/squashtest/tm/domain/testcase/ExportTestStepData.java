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
package org.squashtest.tm.domain.testcase;

public class ExportTestStepData {
	private String action;
	private String expectedResult;
	private ExportTestCaseData testCase;

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		doSetAction(action);
	}
	private void doSetAction(String action){
		if(action != null){
			this.action = action;
		}
	}

	public String getExpectedResult() {
		return expectedResult;
	}

	public void setExpectedResult(String expectedResult) {
		this.expectedResult = expectedResult;
	}
	private void doSetExpectedResult(String expectedResult){
		if(expectedResult != null){
			this.expectedResult = expectedResult;
		}
	}

	public ExportTestStepData(String action, String expectedResult) {
		super();
		doSetAction(action);
		doSetExpectedResult(expectedResult);
	}

	public ExportTestCaseData getTestCase() {
		return testCase;
	}

	public void setTestCase(ExportTestCaseData testCase) {
		this.testCase = testCase;
	}

}
