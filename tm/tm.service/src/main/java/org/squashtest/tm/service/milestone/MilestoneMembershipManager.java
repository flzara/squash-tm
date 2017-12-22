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
package org.squashtest.tm.service.milestone;

import java.util.Collection;

import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.milestone.Milestone;

public interface MilestoneMembershipManager extends MilestoneMembershipFinder {

	Collection<Campaign> findCampaignsByMilestoneId(long milestoneId);

	Collection<Milestone> findAssociableMilestonesToTestCase(long testCaseId);

	void bindTestCaseToMilestones(long testCaseId, Collection<Long> milestoneIds);

	void unbindTestCaseFromMilestones(long testCaseId, Collection<Long> milestoneIds);

	Collection<Milestone> findAssociableMilestonesToRequirementVersion(long versionId);

	void bindRequirementVersionToMilestones(long versionId, Collection<Long> milestoneIds);

	void unbindRequirementVersionFromMilestones(long versionId, Collection<Long> milestoneIds);

	Collection<Milestone> findAssociableMilestonesToCampaign(long campaignId);

	/**
	 * unlike other entities, a campaign can belong to one milestone only. When a milestone is bound to
	 * a campaign, any previous milestone will be unbound in the process.
	 * 
	 * @param campaignId
	 * @param milestoneId
	 */
	void bindCampaignToMilestone(long campaignId, Long milestoneId);

	void unbindCampaignFromMilestones(long campaignId, Collection<Long> milestoneIds);



}
