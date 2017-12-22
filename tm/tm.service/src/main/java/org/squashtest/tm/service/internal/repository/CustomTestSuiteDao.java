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

import org.squashtest.tm.core.foundation.collection.ColumnFiltering;
import org.squashtest.tm.core.foundation.collection.Filtering;
import org.squashtest.tm.core.foundation.collection.PagingAndMultiSorting;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.campaign.TestPlanStatistics;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.execution.ExecutionStatusReport;
import org.squashtest.tm.service.campaign.IndexedIterationTestPlanItem;

import java.util.List;

public interface CustomTestSuiteDao {


	/**
	 * Will fill a {@link TestPlanStatistics} bean with infos taken from the test plan of the {@link TestSuite} matching
	 * the given id.
	 *
	 * @param testSuitId
	 *            : the id of the concerned {@link TestSuite}
	 * @return the filled {@link TestPlanStatistics} bean
	 */
	TestPlanStatistics getTestSuiteStatistics(long testSuitId);

	/**
	 * Will fill a {@link TestPlanStatistics} bean with the infos taken from the test plan of the {@link TestSuite}
	 * matching the given id. But the infos will be taken only from the {@link IterationTestPlanItem} that are assigned
	 * to the user matching the given login.
	 *
	 * @param suiteId
	 *            : the id of the concerned {@link TestSuite}
	 * @param userLogin
	 *            : the login of the user we want the {@link IterationTestPlanItem} to be assigned to
	 * @return the fielled {@link TestPlanStatistics} bean
	 */
	TestPlanStatistics getTestSuiteStatistics(long suiteId, String userLogin);


	List<IterationTestPlanItem> findTestPlan(long suiteId, PagingAndMultiSorting sorting, Filtering filter,
		ColumnFiltering columnFiltering);

	/**
	 * Returns the paged list of [index, IterationTestPlanItem] wrapped in an {@link IndexedIterationTestPlanItem}
	 *
	 */
	List<IndexedIterationTestPlanItem> findIndexedTestPlan(long suiteId, PagingAndMultiSorting sorting,
		Filtering filtering, ColumnFiltering columnFiltering);


	long countTestPlans(Long suiteId, Filtering filtering, ColumnFiltering columnFiltering);

	ExecutionStatusReport getStatusReport(Long id);

}
