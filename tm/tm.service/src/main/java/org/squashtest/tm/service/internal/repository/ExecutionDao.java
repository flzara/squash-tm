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
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.execution.ExecutionStep;
import org.squashtest.tm.domain.testcase.ActionTestStep;

import java.util.Collection;
import java.util.List;

public interface ExecutionDao extends JpaRepository<Execution, Long>, CustomExecutionDao {

	@Query
	List<ExecutionStep> findSteps(@Param("executionId") long executionId);

	@Query
	List<ExecutionStep> findStepsForAllExecutions(@Param("executionIds") Collection<Long> executionIds);

	@Query
	List<ActionTestStep> findOriginalSteps(@Param("executionId") long executionId);

	@Query
	List<Long> findOriginalStepIds(@Param("executionId") long executionId);

	@Query
	long countStatus(@Param("execId") long executionId, @Param("status") ExecutionStatus status);

	// ************** special execution status deactivation section ***************

	@Query
	List<Long> findExecutionIdsHavingStepStatus(@Param("projectId") Long projectId, @Param("status") ExecutionStatus source);

	@Modifying
	@Query(name="ExecutionStep.replaceStatus")
	void replaceExecutionStepStatus(@Param("projectId") long projectId, @Param("oldStatus") ExecutionStatus oldStatus, @Param("newStatus") ExecutionStatus newStatus);

	@Modifying
	@Query(name = "IterationTestPlanItem.replaceStatus")
	void replaceTestPlanStatus(@Param("projectId") long projectId, @Param("oldStatus") ExecutionStatus oldStatus, @Param("newStatus") ExecutionStatus status);


	// ************* /special execution status deactivation section ***************
	@Query
	long countSteps(@Param("executionId") long executionId);

	/**
	 * Returns the count of executions which ran a given test case.
	 *
	 * @param testCaseId
	 * @return
	 */
	@Query
	long countByTestCaseId(@Param("testCaseId") long testCaseId);


}
