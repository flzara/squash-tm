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
import java.util.List;

import org.squashtest.tm.domain.milestone.Milestone;


public interface MilestoneMembershipFinder {


	/**
	 * Returns the milestones of which the test case is directly a member,
	 * plus all the milestones of which its verified requirement are members.
	 * 
	 * @param testCase
	 * @return
	 */
	Collection<Milestone> findAllMilestonesForTestCase(long testCaseId);

	/**
	 * Says whether a test case cannot be deleted because of milestone
	 * it belongs directly, and also because of those of the requirements
	 * this test case verifies.
	 * 
	 * @param testCaseId
	 * @return
	 */
	boolean isTestCaseMilestoneDeletable(long testCaseId);

	/**
	 * Says whether a test case cannot be modified because of milestone
	 * it belongs directly, and also because of those of the requirements
	 * this test case verifies.
	 * 
	 * @param testCaseId
	 * @return
	 */
	boolean isTestCaseMilestoneModifiable(long testCaseId);



	Collection<Milestone> findAllMilestonesForUser(long userId);
	Collection<Milestone> findMilestonesForRequirementVersion(long versionId);

	Collection<Milestone> findMilestonesForCampaign(long campaignId);

	Collection<Milestone> findMilestonesForIteration(long iterationId);

	Collection<Milestone> findMilestonesForTestSuite(long testSuiteId);

	boolean isMilestoneBoundToACampainInProjects(Long milestoneId, List<Long> projectIds);

}
