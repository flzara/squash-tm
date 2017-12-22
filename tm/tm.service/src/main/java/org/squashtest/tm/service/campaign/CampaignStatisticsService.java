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

import java.util.Collection;
import java.util.List;

import org.squashtest.tm.service.statistics.campaign.CampaignNonExecutedTestCaseImportanceStatistics;
import org.squashtest.tm.service.statistics.campaign.CampaignProgressionStatistics;
import org.squashtest.tm.service.statistics.campaign.CampaignStatisticsBundle;
import org.squashtest.tm.service.statistics.campaign.CampaignTestCaseStatusStatistics;
import org.squashtest.tm.service.statistics.campaign.CampaignTestCaseSuccessRateStatistics;
import org.squashtest.tm.service.statistics.campaign.CampaignTestInventoryStatistics;
import org.squashtest.tm.service.statistics.campaign.IterationTestInventoryStatistics;
import org.squashtest.tm.service.statistics.campaign.ManyCampaignStatisticsBundle;

/*
 * TODO : Some statistics here are computed on a set of campaigns (namely TestCaseStatusStatistics, TestCaseSuccessRateStatistics, NonExecutedTestCaseImportanceStatistics)
 * and hopefully some days TestInventoryStatistics). This set of campaign can be :
 * 	- reduced to one campaign,
 *  - all campaigns that belong to a milestone,
 *  - all campaigns that belong to a folder and a milestone if any.
 * 
 *  and the list can go on maybe.
 * 
 *  Undoubtfully many methods are triplicate and would benefit a refactor as with a single arguments List<Long> campaignIds.
 *  The problem for now is that 1) I'm getting lazy and 2) depending on the context some statistics aren't exactly the same
 *  (for instance the granularity of TestInventoryStatistics : they can be computed at campaign level or iteration level).
 *  (also the ProgressionStatistics are undefined for more than one campaign).
 * 
 *  So I leave it there but if some day we have yet another set of methods to add (like, dashboard for Project), please do that refactoring.
 * 
 *  Note : CampaignLibraryFinderService#findCampaignIdsFromSelection(Collection<Long> libraryIds, Collection<Long> nodeIds);
 *  and CampaignDao#filterByMilestone(Collection<Long> campaignIds, Long milestoneId) might be useful to that end. If you can
 *  solve :
 *  - the inconsistencies regarding the TestInventory (that depends on the the context)
 *  - and the possible cyclic dependencies when injecting CampaignLibraryFinderService.
 */
public interface CampaignStatisticsService {


	/* *********************************** all-in-one methods ************************************ */


	/**
	 * Given a campaign id, gathers all of the following in one package.
	 * 
	 * @param campaignId
	 * @return
	 */
	CampaignStatisticsBundle gatherCampaignStatisticsBundle(long campaignId);


	/**
	 * <p>
	 * For activeMilestone, gathers all of the following in one package for a milestone.
	 * </p>
	 * 
	 * 
	 * @return
	 */
	// functional FIXME : ask a spec writer why statistics vary so subtly depending on
	// whether we ask a dashboard for a bunch of campaigns belonging to the same milestone,
	// or a bunch of campaigns belonging to the same folder.
	CampaignStatisticsBundle gatherMilestoneStatisticsBundle();


	/**
	 * Given a campaign folder id, gathers all of the following in one package for a milestone.
	 * If milestoneId is non null, only campaigns that belong to that milestone will be accounted for.
	 * 
	 * @param folderId
	 * @param milestoneId
	 * @return
	 */
	// functional FIXME : ask a spec writer why statistics vary so subtly depending on
	// whether we ask a dashboard for a bunch of campaigns belonging to the same milestone,
	// or a bunch of campaigns belonging to the same folder
	ManyCampaignStatisticsBundle gatherFolderStatisticsBundle(Long folderId);

	/* *********************************** common statistics methods ************************************ */

	/**
	 * Given a list of campaign id, gathers and returns the number of test cases grouped by execution status.
	 * 
	 * @param campaignIds
	 * @return
	 */
	CampaignTestCaseStatusStatistics gatherTestCaseStatusStatistics(List<Long> campaignIds);


	/**
	 * Given a list of campaign id, gathers and returns the number of passed and failed test cases grouped by weight.
	 * 
	 * @param campaignIds
	 * @return
	 */
	CampaignTestCaseSuccessRateStatistics gatherTestCaseSuccessRateStatistics(List<Long> campaignIds);

	/**
	 * Given a list of campaign id, gathers and returns the number of non-executed test cases grouped by weight.
	 * 
	 * @param campaignIds
	 * @return
	 */
	CampaignNonExecutedTestCaseImportanceStatistics gatherNonExecutedTestCaseImportanceStatistics(List<Long> campaignIds);


	/* ************************************* statistics specific to one lone campaign************************************** */



	/**
	 * <p>Given a campaignId, gathers and return the theoretical and actual cumulative test count by iterations.
	 * The theoretical cumulative test count by iterations means how many tests should have been executed per day on
	 * the basis of the scheduled start and end of an iteration. The actual cumulative test count means how many tests
	 * have been executed so far, each days, during the same period.</p>
	 * 
	 * <p>This assumes that the scheduled start and end dates of each iterations are square : they must all be defined,
	 * and must not overlap. In case of errors appropriate messages will be filled instead and data won't be returned.</p>
	 * 
	 * 
	 * @param campaignId
	 * @return
	 */
	CampaignProgressionStatistics gatherCampaignProgressionStatistics(long campaignId);

	/**
	 * Given a campaign id, gathers and returns how many tests and at which status are planned in this campaign.
	 * Only tests part of an iteration count. Those statistics are grouped and sorted by Iteration.
	 * 
	 * @param campaignId
	 * @return
	 */
	List<IterationTestInventoryStatistics> gatherCampaignTestInventoryStatistics(long campaignId);



	/* ************************ statistics specific to all campaigns of one milestone******************************** */


	/**
	 * <p>
	 * 	Given a milestone id (and so campaign ids), gathers and returns how many tests and at which status are planned in
	 * 	this campaign. Only tests part of an iteration count. Those statistics are grouped and sorted by Iteration.
	 * </p>
	 * <p>
	 * 	Note : this method differs slightly from #gatherCampaignTestInventoryStatistics() because the name of each entry
	 * 	is different.
	 * </>
	 * 
	 * @param milestoneId
	 * @return
	 */
	List<IterationTestInventoryStatistics> gatherMilestoneTestInventoryStatistics();




	/* ************************************ statistics specific to campaign folders ****************************** */

	/**
	 * Given a list of campaigns, gathers and returns how many tests and at which status are planned in
	 * this campaign. Only tests part of an iteration count. Those statistics are grouped and sorted by Campaign.
	 * @param folderId
	 * @return
	 */
	List<CampaignTestInventoryStatistics> gatherFolderTestInventoryStatistics(Collection<Long> campaignIds);




}
