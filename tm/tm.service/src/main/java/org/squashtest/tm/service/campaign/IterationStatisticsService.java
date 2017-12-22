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
package org.squashtest.tm.service.campaign;

import java.util.List;

import org.squashtest.tm.service.statistics.campaign.CampaignNonExecutedTestCaseImportanceStatistics;
import org.squashtest.tm.service.statistics.campaign.CampaignTestCaseStatusStatistics;
import org.squashtest.tm.service.statistics.campaign.CampaignTestCaseSuccessRateStatistics;
import org.squashtest.tm.service.statistics.iteration.IterationProgressionStatistics;
import org.squashtest.tm.service.statistics.iteration.IterationStatisticsBundle;
import org.squashtest.tm.service.statistics.iteration.TestSuiteTestInventoryStatistics;

public interface IterationStatisticsService {

	/**
	 * Given a iteration id, gathers and returns the number of test cases grouped by execution status.
	 * 
	 * @param iterationId
	 * @return
	 */
	CampaignNonExecutedTestCaseImportanceStatistics gatherIterationNonExecutedTestCaseImportanceStatistics(long iterationId);

	/**
	 * Given a iteration id, gathers and returns the number of non-executed test cases grouped by weight.
	 * 
	 * @param iterationId
	 * @return
	 */
	CampaignTestCaseStatusStatistics gatherIterationTestCaseStatusStatistics(long iterationId);
	
	
	/**
	 * Given an iteration id, gathers and returns the number of passed and failed test cases grouped by weight.
	 * 
	 * @param iterationId
	 * @return
	 */
	CampaignTestCaseSuccessRateStatistics gatherIterationTestCaseSuccessRateStatistics(long iterationId);

	
	List<TestSuiteTestInventoryStatistics> gatherTestSuiteTestInventoryStatistics(long iterationId);


	
	/**
	 * Given an iteration id, gathers all of the above in one package. 
	 * 
	 * @param iterationId
	 * @return
	 */
	IterationStatisticsBundle gatherIterationStatisticsBundle(long iterationId);


	IterationProgressionStatistics 	gatherIterationProgressionStatistics(long iterationId);

}
