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

import org.squashtest.tm.core.foundation.collection.ColumnFiltering;
import org.squashtest.tm.core.foundation.collection.MultiSorting;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndMultiSorting;
import org.squashtest.tm.domain.campaign.CampaignLibraryNode;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.service.annotation.BatchPreventConcurrent;
import org.squashtest.tm.service.annotation.Id;
import org.squashtest.tm.service.annotation.PreventConcurrent;
import org.squashtest.tm.service.annotation.PreventConcurrents;

/**
 * Service that aims at managing the test cases of a campaign (i.e. its test plan)
 *
 * @author Agnes Durand
 */
public interface IterationTestPlanManagerService extends IterationTestPlanFinder {

	/**
	 * Returns an iteration filtered for a specific user. It returns an iteration with a test plan containing only the
	 * items that are assigned to that user or have been executed by that user.
	 *
	 * @param iterationId
	 * @return the test plan of given iteration filtered by the current user
	 */
	PagedCollectionHolder<List<IndexedIterationTestPlanItem>> findAssignedTestPlan(long iterationId,
			PagingAndMultiSorting sorting, ColumnFiltering filtering);

	/**
	 * Adds a list of test cases to an iteration. If a test case have one or several datasets, that test case will be planned
	 * as many time with a different dataset.
	 * {@link Id} annotation is used by {@link PreventConcurrent}, {@link BatchPreventConcurrent} and {@link PreventConcurrents} in sub classes
	 */
	@PreventConcurrent(entityType=Iteration.class,paramName="iterationId")
	void addTestCasesToIteration(List<Long> testCaseIds,@Id long iterationId);

	/**
	 * Will add the given test case, with the given test plan, to the test plan of the given iteration.
	 * {@link Id} annotation is used by {@link PreventConcurrent}, {@link BatchPreventConcurrent} and {@link PreventConcurrents} in sub classes
	 * @param testCaseId
	 * @param datasetId, may be null
	 * @param iterationId
	 */

	@PreventConcurrent(entityType=Iteration.class,paramName="iterationId")
	void addTestCaseToIteration(Long testCaseId, Long datasetId, @Id long iterationId);

	/**
	 * Adds a list of test cases to an iteration. If a test case have one or several datasets, that test case will be planned
	 * as many time with a different dataset.
	 * @param testCaseIds
	 * @param iteration
	 */
	List<IterationTestPlanItem> addTestPlanItemsToIteration(List<Long> testCaseIds, Iteration iteration);
	
	/**
	 * Will copy each items into the test plan of the given iteration. In business terms it means that each designated pair of testcase + dataset  
	 * will be replanned in the target iteration.
	 * 
	 * @param iterationTestPlanIds
	 * @param iterationId
	 */
	@PreventConcurrent(entityType=Iteration.class,paramName="iterationId")
	void copyTestPlanItems(List<Long> iterationTestPlanIds, @Id long iterationId);

	void changeTestPlanPosition(long iterationId, int newPosition, List<Long> itemIds);


	void reorderTestPlan( long iterationId, MultiSorting newSorting);

	/**
	 * Removes a list of test cases from a campaign excepted the test plans which were executed
	 *
	 * @param testPlanIds
	 *            the ids of the test plan managing that test case for that iteration
	 * @param iterationId
	 *            the id of the iteration
	 * @return true if at least one test plan item was not deleted (because of insufficient rights on executed item)
	 */
	@PreventConcurrent(entityType=Iteration.class,paramName="iterationId")
	boolean removeTestPlansFromIteration(List<Long> testPlanIds,@Id long iterationId);

	/**
	 * Removes a list of test cases from an iteration excepted the test plans which were executed
	 *
	 * @param testPlanIds
	 *            the ids of the test plan managing that test case for that iteration
	 * @param iteration
	 *            the iteration
	 * @return true if at least one test plan was already executed and therefore not deleted
	 */
	boolean removeTestPlansFromIterationObj(List<Long> testPlanIds, Iteration iteration);

	/**
	 * Removes a test case from an iteration except if the test plans was executed
	 *
	 * @param testPlanId
	 *            the id of the test plan managing that test case for that iteration
	 * @param iterationId
	 * @return true if the test plan was already executed and therefore not deleted
	 */
	boolean removeTestPlanFromIteration(long testPlanItemId);

	/**
	 * Will update the item test plan execution metadata using the last execution data.
	 *
	 * @param execution
	 */
	void updateMetadata(IterationTestPlanItem item);

	/**
	 * Assign User with Execute Access to a TestPlan item.
	 *
	 * @param testCaseId
	 * @param campaignId
	 */
	void assignUserToTestPlanItem(long testPlanItemId, long userId);

	/**
	 * Assign User with Execute Access to a multiple TestPlan items.
	 *
	 * @param testPlanIds
	 * @param campaignId
	 */
	void assignUserToTestPlanItems(List<Long> testPlanIds, long userId);

	/**
	 * <p>
	 * persist each iteration_test_plan_item and add it to iteration
	 * </p>
	 *
	 * @param testPlan
	 * @param iterationId
	 */
	@PreventConcurrent(entityType=Iteration.class,paramName="iterationId")
	void addTestPlanToIteration(List<IterationTestPlanItem> testPlan,@Id long iterationId);

	/**
	 *
	 * @return the list of defined execution statuses
	 */
	List<ExecutionStatus> getExecutionStatusList();


	/**
	 * Assigns an execution status to each test plan item matching the given ids. Override the current itp execution
	 * status and other itp execution metadatas.
	 *
	 * @param testPlanIds
	 * @param statusName
	 */
	List<IterationTestPlanItem> forceExecutionStatus(List<Long> testPlanIds, String statusName);

	/**
	 * Creates a fragment of test plan, containing either :
	 * <ul>
	 * <li>a unique item when the test case is not parameterized</li>
	 * <li>one item per dataset when the test case is parameterized</li>
	 * </ul>
	 *
	 * <strong>Note :</strong> The returned test plan fragment is in a transient state.
	 *
	 * Also assigns each item to the given user.
	 *
	 * @param referenced
	 * @param assignee
	 * @return
	 */
	Collection<IterationTestPlanItem> createTestPlanFragment(TestCase testCase, User assignee);

	/**
	 * Attach a dataset to an item. If the ID of the dataset is null the item will reference
	 * no dataset instead.
	 *
	 *
	 * @param itemId
	 * @param datasetId (may be null)
	 */
	void changeDataset(long itemId, Long datasetId);



}
