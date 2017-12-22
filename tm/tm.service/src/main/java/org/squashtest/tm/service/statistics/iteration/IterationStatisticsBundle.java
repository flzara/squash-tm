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
package org.squashtest.tm.service.statistics.iteration;

import java.util.List;

import org.squashtest.tm.service.statistics.campaign.CampaignNonExecutedTestCaseImportanceStatistics;
import org.squashtest.tm.service.statistics.campaign.CampaignTestCaseStatusStatistics;
import org.squashtest.tm.service.statistics.campaign.CampaignTestCaseSuccessRateStatistics;

public class IterationStatisticsBundle {


	private CampaignTestCaseStatusStatistics iterationTestCaseStatusStatistics;
	
	private CampaignNonExecutedTestCaseImportanceStatistics iterationNonExecutedTestCaseImportanceStatistics;

	private CampaignTestCaseSuccessRateStatistics iterationTestCaseSuccessRateStatistics;
	
	private List<TestSuiteTestInventoryStatistics> testsuiteTestInventoryStatisticsList; 
	
	private IterationProgressionStatistics iterationProgressionStatistics;
	
	private Long selectedId;

	public IterationProgressionStatistics getIterationProgressionStatistics() {
		return iterationProgressionStatistics;
	}

	public void setIterationProgressionStatistics(IterationProgressionStatistics iterationProgressionStatistics) {
		this.iterationProgressionStatistics = iterationProgressionStatistics;
	}

	public CampaignTestCaseStatusStatistics getIterationTestCaseStatusStatistics() {
		return iterationTestCaseStatusStatistics;
	}

	public void setIterationTestCaseStatusStatistics(
			CampaignTestCaseStatusStatistics iterationTestCaseStatusStatistics) {
		this.iterationTestCaseStatusStatistics = iterationTestCaseStatusStatistics;
	}

	public CampaignNonExecutedTestCaseImportanceStatistics getIterationNonExecutedTestCaseImportanceStatistics() {
		return iterationNonExecutedTestCaseImportanceStatistics;
	}

	public void setIterationNonExecutedTestCaseImportanceStatistics(
			CampaignNonExecutedTestCaseImportanceStatistics iterationNonExecutedTestCaseImportanceStatistics) {
		this.iterationNonExecutedTestCaseImportanceStatistics = iterationNonExecutedTestCaseImportanceStatistics;
	}

	public CampaignTestCaseSuccessRateStatistics getIterationTestCaseSuccessRateStatistics() {
		return iterationTestCaseSuccessRateStatistics;
	}

	public void setIterationTestCaseSuccessRateStatistics(
			CampaignTestCaseSuccessRateStatistics iterationTestCaseSuccessRateStatistics) {
		this.iterationTestCaseSuccessRateStatistics = iterationTestCaseSuccessRateStatistics;
	}

	public List<TestSuiteTestInventoryStatistics> getTestsuiteTestInventoryStatisticsList() {
		return testsuiteTestInventoryStatisticsList;
	}

	public void setTestsuiteTestInventoryStatisticsList(
			List<TestSuiteTestInventoryStatistics> testSuiteTestInventoryStatisticsList) {
		this.testsuiteTestInventoryStatisticsList = testSuiteTestInventoryStatisticsList;
	}
	
	public Long getSelectedId() {
		return selectedId;
	}

	public void setSelectedId(Long selectedId) {
		this.selectedId = selectedId;
	}

}
