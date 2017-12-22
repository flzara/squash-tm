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
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.testcase.TestCase;

public interface IterationFinder {

	List<Iteration> findIterationsByCampaignId(long campaignId);

	Iteration findById(long iterationId);

	List<Iteration> findAllByIds(List<Long> iterationIds);

	/**
	 * @param iterationId
	 * @return the list of iteration's executions ordered by their test plan order.
	 */
	List<Execution> findAllExecutions(long iterationId);

	List<Execution> findExecutionsByTestPlan(long iterationId, long testPlanId);

	List<TestCase> findPlannedTestCases(long iterationId);

	List<Iteration> findIterationContainingTestCase(long testCaseId);


	/* ********************** milestones section ******************* */

	@Transactional(readOnly=true)
	Collection<Milestone> findAllMilestones(long iterationId);
}
