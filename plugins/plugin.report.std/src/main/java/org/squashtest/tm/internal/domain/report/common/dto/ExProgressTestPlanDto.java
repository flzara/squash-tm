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
package org.squashtest.tm.internal.domain.report.common.dto;

import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.execution.ExecutionStatus;

public class ExProgressTestPlanDto {

	private String testCaseName;
	private ExecutionStatus executionStatus;
	private ExProgressIterationDto iteration;
	private String testSuitesNames;

	public ExProgressTestPlanDto() {
		super();
	}

	public ExProgressIterationDto getIteration() {
		return iteration;
	}

	public void setIteration(ExProgressIterationDto iteration) {
		this.iteration = iteration;
	}

	public ExProgressTestPlanDto(String testCaseName, ExecutionStatus executionStatus) {
		super();
		this.testCaseName = testCaseName;
		this.executionStatus = executionStatus;
	}

	public String getTestCaseName() {
		return testCaseName;
	}

	public void setTestCaseName(String testCaseName) {
		this.testCaseName = testCaseName;
	}

	public ExecutionStatus getExecutionStatus() {
		return executionStatus;
	}

	public void setExecutionStatus(ExecutionStatus executionStatus) {
		this.executionStatus = executionStatus;
	}

	public String getTestSuitesNames() {
		return testSuitesNames;
	}

	public void setTestSuitesNames(String testSuitesNames) {
		this.testSuitesNames = testSuitesNames;
	}

	public ExProgressTestPlanDto fillBasicInfo(IterationTestPlanItem testPlan) {

		if(testPlan.isTestCaseDeleted()){
			this.testCaseName = null;
		} else {
			String reference = testPlan.getReferencedTestCase().getReference();
			if(reference != null && reference.isEmpty()){
				this.testCaseName = testPlan.isTestCaseDeleted() ? null : testPlan.getReferencedTestCase().getName();
			} else {
				this.testCaseName = testPlan.isTestCaseDeleted() ? null : reference + " - " + testPlan.getReferencedTestCase().getName();
			}
		}
		this.testSuitesNames = buildTestSuitesNames(testPlan);
		this.executionStatus = testPlan.getExecutionStatus();
		return this;
	}

	/* Feat #6745 */
	private String buildTestSuitesNames(IterationTestPlanItem testPlanItem) {
		String prefix = "";
		StringBuilder result = new StringBuilder();
		for(TestSuite testSuite : testPlanItem.getTestSuites()) {
			result.append(prefix);
			prefix = ", ";
			result.append(testSuite.getName());
		}
		return result.toString();
	}

}
