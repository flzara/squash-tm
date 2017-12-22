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

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.annotation.EmptyCollectionGuard;


public interface IterationTestPlanDao extends JpaRepository<IterationTestPlanItem, Long>{

	IterationTestPlanItem findById(long itemTestPlanId);

	@EmptyCollectionGuard
	List<IterationTestPlanItem> findAllByIdIn(Collection<Long> ids);

	/**
	 * Fetches the test plan items which match the given ids ordered according to their iteration's test plan.
	 * @param testPlanIds
	 * @return
	 */
	@EmptyCollectionGuard
	List<IterationTestPlanItem> findAllByIdsOrderedByIterationTestPlan(@Param("testPlanIds") List<Long> testPlanIds);

	/**
	 * Fetches the test plan items which match the given ids ordered according to the given test suite's test plan.
	 * @param testPlanIds
	 * @param testSuiteId
	 * @return
	 */
	@EmptyCollectionGuard
	List<IterationTestPlanItem> findAllByIdsOrderedBySuiteTestPlan(@Param("testPlanIds") List<Long> testPlanIds, @Param("suiteId") long testSuiteId);

	List<IterationTestPlanItem> findByReferencedTestCase(TestCase referencedTestCase);
	
	@EmptyCollectionGuard
	List<Long> findAllForMilestones(@Param("milestonesIds") List<Long> milestonesIds);
}
