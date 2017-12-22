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
package org.squashtest.tm.service.internal.repository;

import java.util.Collection;
import java.util.List;

import org.squashtest.tm.core.foundation.collection.ColumnFiltering;
import org.squashtest.tm.core.foundation.collection.PagingAndMultiSorting;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.CampaignLibraryNode;
import org.squashtest.tm.domain.campaign.CampaignTestPlanItem;
import org.squashtest.tm.domain.campaign.TestPlanStatistics;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.service.campaign.IndexedCampaignTestPlanItem;
import org.squashtest.tm.service.campaign.IndexedIterationTestPlanItem;

public interface CampaignDao extends EntityDao<Campaign> {


	List<Long> findAllCampaignIdsByLibraries(Collection<Long> libraryIds);

	List<Long> findAllCampaignIdsByNodeIds(Collection<Long> nodeIds);

	/**
	 * For a given collection of campaign ids, will return only those that belong
	 * to a milestone (given its id). If milestoneId is null, the initial list
	 * will be returned.
	 *
	 * @param campaignIds
	 * @param milestoneId
	 * @return
	 */
	List<Long> filterByMilestone(Collection<Long> campaignIds, Long milestoneId);

	List<Long> findAllIdsByMilestone(Long milestoneId);


	Campaign findByIdWithInitializedIterations(long campaignId);

	List<CampaignTestPlanItem> findAllTestPlanByIdFiltered(long campaignId, PagingAndSorting filter);


	List<CampaignTestPlanItem> findTestPlan(long campaignId, PagingAndMultiSorting sorting);


	/**
	 * Returns the paged list of [index, CampaignTestPlanItem] wrapped in an {@link IndexedIterationTestPlanItem}
	 *
	 * @param campaignId
	 * @param sorting
	 * @return
	 */
	List<IndexedCampaignTestPlanItem> findIndexedTestPlan(long campaignId, PagingAndMultiSorting sorting);

	/**
	 * Returns the paged list of [index, CampaignTestPlanItem] wrapped in an {@link IndexedIterationTestPlanItem}
	 */
	List<IndexedCampaignTestPlanItem> findIndexedTestPlan(long campaignId, PagingAndSorting sorting);


	long countTestPlanById(long campaignId);


	/**
	 * Returns how many iterations this campaign have
	 */
	int countIterations(long campaignId);

	List<String> findNamesInFolderStartingWith(long folderId, String nameStart);

	List<String> findNamesInLibraryStartingWith(long libraryId, String nameStart);

	List<String> findNamesInCampaignStartingWith(long campaignId, String nameStart);

	List<String> findAllNamesInCampaign(long campaignId);

	/**
	 * Finds all {@link CampaignLibraryNode} which name contains the given token.
	 */
	List<CampaignLibraryNode> findAllByNameContaining(String tokenInName, boolean groupByProject);

	/**
	 * find all the campaign's iterations, and return all iteration's executions regardless of the campaign test-plan
	 *
	 * @return list of executions of all iterations
	 */
	List<Execution> findAllExecutionsByCampaignId(Long campaignId);

	/**
	 *
	 * @param campaignId the id of the concerned campaign
	 * @return the computed {@link TestPlanStatistics} out of each test-plan-item of each campaign's iteration
	 */
	TestPlanStatistics findCampaignStatistics(long campaignId);


	long countRunningOrDoneExecutions(long campaignId);

	List<IndexedCampaignTestPlanItem> findFilteredIndexedTestPlan(long campaignId, PagingAndMultiSorting sorting, ColumnFiltering filtering);

	long countFilteredTestPlanById(long campaignId, ColumnFiltering filtering);

	List<Long> findCampaignIdsHavingMultipleMilestones(List<Long> nodeIds);

	List<Long> findNonBoundCampaign(Collection<Long> nodeIds, Long milestoneId);

}
