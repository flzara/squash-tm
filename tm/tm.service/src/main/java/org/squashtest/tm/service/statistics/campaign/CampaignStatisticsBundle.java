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
package org.squashtest.tm.service.statistics.campaign;

import java.util.List;


public final class CampaignStatisticsBundle {

	private List<IterationTestInventoryStatistics> iterationTestInventoryStatisticsList;

	private CampaignProgressionStatistics campaignProgressionStatistics;

	private CampaignTestCaseStatusStatistics campaignTestCaseStatusStatistics;

	private CampaignNonExecutedTestCaseImportanceStatistics campaignNonExecutedTestCaseImportanceStatistics;

	private CampaignTestCaseSuccessRateStatistics campaignTestCaseSuccessRateStatistics;

	private Long selectedId;

	public List<IterationTestInventoryStatistics> getIterationTestInventoryStatisticsList() {
		return iterationTestInventoryStatisticsList;
	}

	public void setIterationTestInventoryStatisticsList(
			List<IterationTestInventoryStatistics> iterationTestInventoryStatisticsList) {
		this.iterationTestInventoryStatisticsList = iterationTestInventoryStatisticsList;
	}


	public CampaignProgressionStatistics getCampaignProgressionStatistics() {
		return campaignProgressionStatistics;
	}


	public void setCampaignProgressionStatistics(
			CampaignProgressionStatistics campaignProgressionStatistics) {
		this.campaignProgressionStatistics = campaignProgressionStatistics;
	}


	public CampaignTestCaseStatusStatistics getCampaignTestCaseStatusStatistics() {
		return campaignTestCaseStatusStatistics;
	}


	public void setCampaignTestCaseStatusStatistics(
			CampaignTestCaseStatusStatistics campaignTestCaseStatusStatistics) {
		this.campaignTestCaseStatusStatistics = campaignTestCaseStatusStatistics;
	}


	public CampaignNonExecutedTestCaseImportanceStatistics getCampaignNonExecutedTestCaseImportanceStatistics() {
		return campaignNonExecutedTestCaseImportanceStatistics;
	}


	public void setCampaignNonExecutedTestCaseImportanceStatistics(
			CampaignNonExecutedTestCaseImportanceStatistics campaignNonExecutedTestCaseImportanceStatistics) {
		this.campaignNonExecutedTestCaseImportanceStatistics = campaignNonExecutedTestCaseImportanceStatistics;
	}


	public CampaignTestCaseSuccessRateStatistics getCampaignTestCaseSuccessRateStatistics() {
		return campaignTestCaseSuccessRateStatistics;
	}


	public void setCampaignTestCaseSuccessRateStatistics(
			CampaignTestCaseSuccessRateStatistics campaignTestCaseSuccessRateStatistics) {
		this.campaignTestCaseSuccessRateStatistics = campaignTestCaseSuccessRateStatistics;
	}

	public Long getSelectedId() {
		return selectedId;
	}

	public void setSelectedId(Long selectedId) {
		this.selectedId = selectedId;
	}

}
