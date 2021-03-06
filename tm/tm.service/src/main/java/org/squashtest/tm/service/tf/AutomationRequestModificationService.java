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
package org.squashtest.tm.service.tf;

import org.squashtest.tm.domain.tf.automationrequest.AutomationRequestStatus;

import java.util.List;
import java.util.Map;

public interface AutomationRequestModificationService extends AutomationRequestFinderService{

	void deleteRequestByProjectId(long projectId);

	/**
	 * Will unassign the automation requests, identified by their ids, from the user that handle them.
	 * @param tcIds
	 */
	void unassignRequests(List<Long> tcIds);

	void changeStatus(List<Long> tcIds, AutomationRequestStatus automationRequestStatus);

	void changePriority(List<Long> tcIds, Integer priority);

	void assignedToRequest(List<Long> tcIds);

	/**
	 * Given a list of test case's id, will try to update value of automation script for each test case
	 * @param tcIds : a list of test case ids
	 */
	Map<Long, String> updateTAScript(List<Long> tcIds);

	/**
	 * Given an {@link org.squashtest.tm.domain.campaign.Iteration}'s id,
	 * will try to update value of TA script of ITPI whom test case is automated and is part of a project allowing automation workflow
	 * @param iterationId : an {@link org.squashtest.tm.domain.campaign.Iteration}'s id
	 * @return a {@link Map} whom keys are ids of {@link org.squashtest.tm.domain.campaign.IterationTestPlanItem} which "losed" their TA script in the process and values are the corresponding {@link org.squashtest.tm.domain.testcase.TestCase}'s name.
	 */
	Map<Long, String> updateTAScriptForIteration(Long iterationId);

	/**
	 * Given an {@link org.squashtest.tm.domain.campaign.TestSuite}'s id,
	 * will try to update value of TA script of ITPI whom test case is automated and is part of a project allowing automation workflow
	 * @param testSuiteId : a {@link org.squashtest.tm.domain.campaign.TestSuite}'s id
	 * @return a {@link Map} whom keys are ids of {@link org.squashtest.tm.domain.campaign.IterationTestPlanItem} which "losed" their TA script in the process and values are the corresponding {@link org.squashtest.tm.domain.testcase.TestCase}'s name.
	 */
	Map<Long, String> updateTAScriptForTestSuite(Long testSuiteId);

	/**
	 * Given alist of {@link org.squashtest.tm.domain.campaign.IterationTestPlanItem}'s id,
	 * will try to update value of TA script of ITPI whom test case is automated and is part of a project allowing automation workflow
	 * @param testPlanIds : a list of {@link org.squashtest.tm.domain.campaign.IterationTestPlanItem}'s id
	 * @return a {@link Map} whom keys are ids of {@link org.squashtest.tm.domain.campaign.IterationTestPlanItem} which "lost" their TA script in the process and values are the corresponding {@link org.squashtest.tm.domain.testcase.TestCase}'s name.
	 */
	Map<Long, String> updateTAScriptForItems(List<Long> testPlanIds);

}
