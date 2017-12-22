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

import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.tm.domain.users.User;

//@Transactional(readOnly = true)
@Transactional
public interface IterationTestPlanFinder {


	PagedCollectionHolder<List<IndexedIterationTestPlanItem>> findTestPlan(long iterationId, PagingAndSorting filter);

	IterationTestPlanItem findTestPlanItem(long itemTestPlanId);

	List<IterationTestPlanItem> findTestPlanItems(List<Long> ids);

	/**
	 * Returns a collection of {@link TestCaseLibrary}, the test cases of which may be added to the campaign
	 */
	List<TestCaseLibrary> findLinkableTestCaseLibraries();

	List<TestCase> findPlannedTestCases(Long iterationId);

	/**
	 * Get Users with Execute Access for an Iteration and its TestPlan.
	 *
	 * @param testCaseId
	 * @param campaignId
	 */
	List<User> findAssignableUserForTestPlan(long iterationId);

	List<IterationTestPlanItem> findByReferencedTestCase (TestCase referencedTestCase);
}
