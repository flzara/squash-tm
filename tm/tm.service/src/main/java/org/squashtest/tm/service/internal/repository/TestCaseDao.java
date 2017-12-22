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

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.annotation.EmptyCollectionGuard;

import java.util.Collection;
import java.util.List;


public interface TestCaseDao extends Repository<TestCase, Long>, CustomTestCaseDao {


	/**
	 * Counts the calling test steps of a test case
	 *
	 */
	@Query
	long countCallingTestSteps(long testCaseId);

	@Query
	List<Long> findAllDistinctTestCasesIdsCalledByTestCase(long testCaseId);

	@Query
	List<Long> findAllDistinctTestCasesIdsCallingTestCase(long testCaseId);

	@Query
	@EmptyCollectionGuard
	List<Long> findAllTestCaseIdsByLibraries(@Param("libraryIds") Collection<Long> libraryIds);

	@Query
	@EmptyCollectionGuard
	List<Long> findNodeIdsHavingMultipleMilestones(@Param("nodeIds") Collection<Long> nodeIds);

	@Query
	@EmptyCollectionGuard
	List<Long> findNonBoundTestCases(@Param("nodeIds") Collection<Long> nodeIds, @Param("milestoneId") Long milestoneId);

	@Query
	List<Long> findAllTestCasesLibraryForMilestone(@Param("milestoneId") Collection<Long> milestoneIds);

	@Query
	@EmptyCollectionGuard
	List<Long> findAllTestCasesLibraryNodeForMilestone(@Param("milestoneIds") Collection<Long> milestoneIds);
}
