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

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.milestone.Milestone;

import java.util.Collection;
import java.util.List;

public interface MilestoneDao extends JpaRepository<Milestone, Long>, CustomMilestoneDao {

	@Query
	Collection<Milestone> findAssociableMilestonesForUser(@Param("userId") long UserId);

	@Query
	Collection<Milestone> findMilestonesForRequirementVersion(@Param("versionId") long versionId);

	@Query
	Collection<Milestone> findMilestonesForCampaign(@Param("campaignId") long campaignId);

	@Query
	Collection<Milestone> findMilestonesForIteration(@Param("iterationId") long iterationId);

	@Query
	Collection<Milestone> findMilestonesForTestSuite(@Param("suiteId") long suiteId);

	@Query
	Collection<Campaign> findCampaignsForMilestone(@Param("milestoneId") long milestoneId);

	@Query
	Milestone findByLabel(@Param("label") String label);

	/**
	 * alias to #findByLabel
	 */
	@Query(name = "Milestone.findByLabel")
	Milestone findByName(@Param("label") String name);

	@Query
	long countMilestonesForUsers(@Param("userIds") List<Long> userIds);

}
