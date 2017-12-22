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

import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.milestone.MilestoneHolder;

import java.util.Collection;
import java.util.List;

public interface CustomMilestoneDao {
	interface HolderConsumer {
		void consume(MilestoneHolder holder);
	}

	List<Long> findAllMilestoneIds();
	
	List<Long> findMilestoneIdsForUsers(Collection<Long> partyIds);
	


	Collection<Milestone> findAssociableMilestonesForTestCase(long testCaseId);

	Collection<Milestone> findAllMilestonesForTestCase(long testCaseId);

	// check whether some milestone could block the deletion of this test case
	// the said milestone could also be inherited by verified requirements
	// hence this specific method
	boolean isTestCaseMilestoneDeletable(long testCaseId);

	// check whether some milestone could block the deletion of this test case
	// the said milestone could also be inherited by verified requirements
	// hence this specific method
	boolean isTestCaseMilestoneModifiable(long testCaseId);

	Collection<Milestone> findAssociableMilestonesForRequirementVersion(long versionId);

	Collection<Milestone> findAssociableMilestonesForCampaign(long campaignId);

	boolean isMilestoneBoundToOneObjectOfProject(Long milestoneId, Long projectId);

	/**
	 * Warning : This method may clear your session. Be carefull !
	 */
	void unbindAllObjectsForProjects(Long milestoneId, List<Long> projectIds);

	/**
	 * Warning : This method may clear your session. Be carefull !
	 */
	void unbindAllObjectsForProject(Long id, Long projectId);

	boolean isOneMilestoneAlreadyBindToAnotherRequirementVersion(List<Long> reqVIds, List<Long> milestoneIds);

	boolean isMilestoneBoundToACampainInProjects(Long milestoneId, List<Long> projectIds);

	Collection<Long> findTestCaseIdsBoundToMilestones(Collection<Long> milestoneIds);

	Collection<Long> findRequirementVersionIdsBoundToMilestones(Collection<Long> milestoneIds);

	/**
	 * Warning : This method may clear your session. Be carefull !
	 */
	void performBatchUpdate(HolderConsumer consumer);

	boolean isBoundToAtleastOneObject(long milestoneId);

	/**
	 * Warning : This method may clear your session. Be carefull !
	 */
	void unbindAllObjects(long milestoneId);

	void synchronizeRequirementVersions(long source, long target, List<Long> projectIds);

	void synchronizeTestCases(long source, long target, List<Long> projectIds);

	void bindMilestoneToProjectTestCases(long projectId, long milestoneId);

	void bindMilestoneToProjectRequirementVersions(long projectId, long milestoneId);

}
