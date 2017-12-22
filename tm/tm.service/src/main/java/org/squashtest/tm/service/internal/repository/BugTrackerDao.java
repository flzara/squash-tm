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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.tm.domain.campaign.CampaignLibraryNode;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStep;
import org.squashtest.tm.service.internal.bugtracker.Pair;

import java.util.Collection;
import java.util.List;

public interface BugTrackerDao extends JpaRepository<BugTracker, Long> {
	/**
	 * @return a page of bugtrackers according to the filter
	 */
	@Override
	Page<BugTracker> findAll(Pageable pageable);

	List<BugTracker> findByKind(String kind);

	/**
	 *
	 * @return the list of distinct BugTrackers concerned by the given projects;
	 */
	@Query
	List<BugTracker> findDistinctBugTrackersForProjects(@Param("projects") List<Long> projectIds);

	/**
	 * Given its name, returns a bugtracker
	 */
	BugTracker findByName(String bugtrackerName);

	/**
	 *
	 * @return the bugtracker bound to the campaign's project
	 */
	@Query
	BugTracker findByCampaignLibraryNode(@Param("node") CampaignLibraryNode node);

	/**
	 *
	 * @return the bugtracker bound to the excution's project
	 */
	@Query
	BugTracker findByExecution(@Param("execution") Execution execution);

	/**
	 *
	 * @return the bugtracker bound to the iteration's project
	 */
	@Query
	BugTracker findByIteration(@Param("iteration") Iteration iteration);

	@Query
	BugTracker findByTestSuite(@Param("testSuite") TestSuite testSuite);

	@Query
	List<Pair<Execution, BugTracker>> findAllPairsByExecutions(@Param("executions") Collection<Execution> executions);

	@Query
	BugTracker findByExecutionStep(@Param("step") ExecutionStep executionStep);
}
