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

import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.core.dynamicmanager.annotation.QueryParam;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.TestPlanStatistics;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.service.statistics.campaign.CampaignStatisticsBundle;
import org.squashtest.tm.service.statistics.campaign.ManyCampaignStatisticsBundle;

@Transactional
public interface CustomCampaignModificationService {

	void rename(long campaignId, String newName);

	Collection<Campaign> findCampaignsByMilestoneId(long milestoneId);

	List<Iteration> findIterationsByCampaignId(long campaignId);


	Integer countIterations(@QueryParam("campaignId") Long campaignId);


	/**
	 *
	 * @param campaignId the id of the concerned campaign
	 * @return the computed {@link TestPlanStatistics} out of each test-plan-item of each campaign's iteration
	 */
	TestPlanStatistics findCampaignStatistics(long campaignId);

	CampaignStatisticsBundle gatherCampaignStatisticsBundle(long campaignId);


	// TODO : move this method to CampaignFolderModificationService. If it exists one day.
	// TODO : move this to CampaignLibraryNavigationSercice. If the inconsistencies in the
	// returned data are solved one day (see comment on CampaignStatisticsService).
	ManyCampaignStatisticsBundle gatherFolderStatisticsBundle(Long folderId);


	/* ********************** milestones section ******************* */

	/**
	 * Bind a milestone to a campaign. Any previous milestone will be unbound because a campaign can be bound to only
	 * one milestone.
	 *
	 * @param campaignId
	 * @param milestoneId
	 */
	void bindMilestone(long campaignId, long milestoneId);

	void unbindMilestones(long campaignId, Collection<Long> milestoneIds);

	Collection<Milestone> findAssociableMilestones(long campaignId);

	Collection<Milestone> findAllMilestones(long campaignId);

	/**
	 * This method retrieves a {@link Campaign} with the given id.
	 * @param campaignId The id of the campaign to retrieve
	 * @return The campaign with the given id or null if it does not exist
	 */
	Campaign findCampaigWithExistenceCheck(long campaignId);

}
