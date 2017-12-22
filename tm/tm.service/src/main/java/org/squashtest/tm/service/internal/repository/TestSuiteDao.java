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
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.service.annotation.EmptyCollectionGuard;

import java.util.List;

/**
 *
 */
public interface TestSuiteDao extends JpaRepository<TestSuite, Long>, CustomTestSuiteDao {
	List<TestSuite> findAllByIterationId(long iterationId);

	@Query
	@EmptyCollectionGuard
	List<IterationTestPlanItem> findTestPlanPartition(@Param("suiteId") long testSuiteId, @Param("itemIds") List<Long> testPlanItemIds);

	@Query
	long findProjectIdBySuiteId(long suiteId);


	/**
	 * Will find the distinct ids of the test cases referenced in the suite matching the given id
	 *
	 * @param suiteId : the id of the concerned TestSuite
	 * @return the distinct ids of the TestCases referenced in the suite's test plan.
	 */
	@Query
	List<Long> findPlannedTestCasesIds(Long suiteId);
}
