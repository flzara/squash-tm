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
import org.squashtest.tm.domain.bugtracker.Issue;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.CampaignFolder;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.TestCase;

import java.util.Collection;
import java.util.List;

public interface IssueDao extends JpaRepository<Issue, Long>, CustomIssueDao {

	/**
	 * Will count the total number of issues related to the given IssueList.
	 *
	 * TODO looks like it's only used in tests. Assess and remove
	 *
	 * @param issueListIds
	 *            the id of the issue lists.
	 * @return how many issues they hold.
	 */
	@Query(name = "issueList.countIssues")
	Integer countIssuesfromIssueList(@Param("issueListIds") List<Long> issueListIds);

	/**
	 * Will count the total number of issues related to the given IssueList, for the given bugtracker
	 *
	 * TODO looks like it's only used in tests. Assess and remove
	 *
	 * @param issueListIds
	 *            the id of the issue lists.
	 * @param bugTrackerId
	 *            the id of the bug-tracker we are filtering on
	 * @return how many issues they hold.
	 */
	@Query(name = "issueList.countIssuesByTracker")
	Integer countIssuesfromIssueList(@Param("issueListIds") Collection<Long> issueListIds, @Param("bugTrackerId") Long bugTrackerId);

	/**
	 * Will count all Issues from the given executions and execution-steps <b>concerned by the active bug-tracker</b> for each
	 * execution/execution-step's project.
	 *
	 * WARNING : Will crash on MySQL if the executionStepsIds is empty... use countIssuesfromEmptyExecutions in that case
	 *
	 * @return the number of Issues detected by the given execution / execution Steps
	 */
	@Query
	Integer countIssuesfromExecutionAndExecutionSteps(@Param("executionsIds") List<Long> executionsIds, @Param("executionStepsIds") List<Long> executionStepsIds);

	/**
	 * Will count all Issues from the given executions if they have no steps at all
	 * Used to prevent empty list exception from MySQL with the request countIssuesfromExecutionAndExecutionSteps
	 * @return the number of Issues detected by the given executions
	 */
	@Query
	Integer countIssuesfromEmptyExecutions(@Param("executionsIds") List<Long> executionsIds);


	/**
	 * Counts all issues for a campaign
	 */
	@Query
	long countByCampaign(@Param("campaign") Campaign campaign);

	/**
	 * Will find all issues declared in the iteration of the given id.
	 * @param id : the id of the concerned {@linkplain Iteration}
	 * @return the list of the iteration's {@link Issue}s
	 */
	@Query
	List<Issue> findAllForIteration(@Param("id") Long id);

	/**
	 * Will find all issues declared in the test suite of the given id.
	 * @param id : the id of the concerned TestSuite
	 * @return the list of the suite's {@link Issue}s
	 */
	@Query
	List<Issue> findAllForTestSuite(@Param("id") Long id);

	@Query
	Integer countIssuesfromExecutionSteps(@Param("executionStepsIds") List<Long> executionStepsIds);

	/**
	 * Counts all issues for an execution and its steps
	 */
	@Query
	long countByExecutionAndSteps(@Param("execution") Execution execution);

	@Query
	long countByIteration(@Param("iteration") Iteration iteration);

	@Query
	long countByTestSuite(@Param("testSuite") TestSuite testSuite);

	@Query
	long countByCampaignFolder(@Param("folder") CampaignFolder folder);

	@Query
	long countByTestCase(@Param("testCase") TestCase testCase);

	@Query
	long countByRequirementVersion(@Param("requirementVersion") RequirementVersion requirementVersion);

}
