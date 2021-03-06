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
package org.squashtest.tm.service.testautomation;

import org.squashtest.tm.core.foundation.collection.ColumnFiltering;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndMultiSorting;
import org.squashtest.tm.core.foundation.lang.Couple;
import org.squashtest.tm.domain.EntityReference;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.testautomation.AutomatedExecutionExtender;
import org.squashtest.tm.domain.testautomation.AutomatedSuite;
import org.squashtest.tm.service.testautomation.model.AutomatedSuiteCreationSpecification;
import org.squashtest.tm.service.testautomation.model.AutomatedSuitePreview;
import org.squashtest.tm.service.testautomation.model.SuiteExecutionConfiguration;
import org.squashtest.tm.service.testautomation.model.TestAutomationProjectContent;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface AutomatedSuiteManagerService {

	/**
	 * Finds a suite given its id.
	 *
	 * @param id
	 * @return
	 */
	AutomatedSuite findById(String id);

	/**
	 * Finds all automated test plan list item ids from an iteration or a test suite
	 * @param entityReference an iteration or a test suite
	 * @return an arrayList of automated test plan list item ids
	 */
	List<Long> findTpiIdsWithAutomaticExecutionMode(EntityReference entityReference);

	/**
	 * Given a specification about a desired automated suite, returns a summary of what it would
	 * look like.
	 *
	 * @param specification
	 * @return
	 */
	AutomatedSuitePreview preview(AutomatedSuiteCreationSpecification specification);


	/**
	 * Returns the list of the test paths that would be run by a given automated suite specification, and for a
	 * given test automation project.
	 *
	 * @param specification
	 * @return
	 */
	List<String> findTestListPreview(AutomatedSuiteCreationSpecification specification, long automatedProjectId);

	/**
	 * Creates an AutomatedTestSuite according to the content of the specification.
	 *
	 * @param specification
	 * @return the created automated suite.
	 */
	AutomatedSuite createFromSpecification(AutomatedSuiteCreationSpecification specification);


	/**
	 * Creates then run the suite. The specification contains both creation instruction
	 * and the execution instruction (the execution configuration).
	 *
	 * @param specification
	 * @return the created automated suite
	 */
	AutomatedSuite createAndExecute(AutomatedSuiteCreationSpecification specification);

	/**
	 * Creates a new AutomatedSuite based on a given {@link IterationTestPlanItem} list belonging to a specific {@link Iteration}.
	 * Only automated tests planned in the test plan will be included. The automated executions are ordered according to the test plan.
	 *
	 * @param iterationId
	 * @param items
	 * @throws IllegalArgumentException All test plan items must belong to the selected iteration
	 * @return
	 */
	AutomatedSuite createFromIterationTestPlanItems(long iterationId, List<IterationTestPlanItem> items);

	/**
	 * Creates a new AutomatedSuite based on the whole test plan of an {@link Iteration}, given its ID. Only automated tests planned in the
	 * test plan will be included. The automated executions are ordered according to the test plan.
	 *
	 * @param iterationId
	 * @return
	 */
	AutomatedSuite createFromIterationTestPlan(long iterationId);

	/**
	 * Creates a new AutomatedSuite based on a given {@link IterationTestPlanItem} list belonging to a specific {@link TestSuite}.
	 * Only automated tests planned in the test plan will be included. The automated executions are ordered according to the test plan.
	 *
	 * @param testSuiteId
	 * @param items
	 * @throws IllegalArgumentException All test plan items must belong to the selected test suite
	 * @return
	 */
	AutomatedSuite createFromTestSuiteTestPlanItems(long testSuiteId, List<IterationTestPlanItem> items);

	/**
	 * Creates a new AutomatedSuite based on the whole test plan of a {@link TestSuite}, given its ID. Only automated tests planned in the
	 * test plan will be included. The automated executions are ordered according to the test plan.
	 *
	 * @param testSuiteId
	 * @return
	 */
	AutomatedSuite createFromTestSuiteTestPlan(long testSuiteId);

	void delete(AutomatedSuite suite);


	void delete(String automatedSuiteId);

	/**
	 * Clean all AutomatedSuites which are older than the lifetime configured in their project.
	 */
	void cleanOldSuites();

	/**
	 * Count the number of AutomatedSuites and AutomatedExecutions which are older than the lifetime configured in their
	 * project.
	 * @return An AutomationDeletionCount containing old suites count and old executions count.
	 */
	AutomationDeletionCount countOldAutomatedSuitesAndExecutions();

	/**
	 * Given the id of an {@link AutomatedSuite}, returns its content as tests grouped by projects.
	 *
	 * @param autoSuiteId
	 * @return
	 */
	Collection<TestAutomationProjectContent> sortByProject(String autoSuiteId);

	/**
	 * Given an {@link AutomatedSuite}, returns its content as tests grouped by projects.
	 *
	 * @param suite
	 * @return
	 */
	Collection<TestAutomationProjectContent> sortByProject(AutomatedSuite suite);

	/**
	 * Runs the given AutomatedSuite, equivalent to {@link #start(AutomatedSuite, Collection)} with
	 * an empty configuration.
	 *
	 * @param suite
	 */
	void start(AutomatedSuite suite);

	/**
	 * Runs an AutomatedSuite given its ID, equivalent to {@link #start(AutomatedSuite, Collection)} with
	 * an empty configuration.
	 * @param autoSuiteId
	 */
	void start(String autoSuiteId);


	/**
	 * Runs an automatedSuite with the given configuration.
	 *
	 * @param suite
	 * @param configuration
	 */
	void start(AutomatedSuite suite, Collection<SuiteExecutionConfiguration> configuration);


	/**
	 * Runs an automatedSuite given its ID with the given configuration.
	 *
	 * @param suite
	 * @param configuration
	 */
	void start(String suiteId, Collection<SuiteExecutionConfiguration> configuration);

	/**
	 * Creates a test list with parameters (dataset and cufs) from an automated suite.
	 *
	 * @param suite
	 * @param configuration
	 * @param withAllCustomFields
	 */

	Collection<Couple<AutomatedExecutionExtender, Map<String, Object>>> prepareExecutionOrder(AutomatedSuite suite, boolean withAllCustomFields);

	/**
	 * Given the id of an automated test suite, returns the list of executions associated to this automated test suite.
	 *
	 * @param automatedTestSuiteId
	 * @return
	 */

	List<Execution> findExecutionsByAutomatedTestSuiteId(String automatedTestSuiteId);


	/**
	 * Creates a new AutomatedSuite based on a collection of {@link IterationTestPlanItem}, given their ID. Only automated tests will
	 * be included. The automated executions are ordered according to the iteration's test plan.
	 *
	 * @param testPlanIds
	 * @param iterationId
	 * @return
	 */
	AutomatedSuite createFromItemsAndIteration(List<Long> testPlanIds, long iterationId);


	/**
	 * Creates a new AutomatedSuite based on a collection of {@link IterationTestPlanItem}, given their ID. Only automated tests will
	 * be included. The automated executions are ordered according to the test suite's test plan.
	 *
	 * @param testPlanIds
	 * @param testSuiteId
	 * @return
	 */
	AutomatedSuite createFromItemsAndTestSuite(List<Long> testPlanIds, long testSuiteId);

	/**
	 * Gets a list of AutomatedSuite given an iteration ID.
	 *
	 * @param iterationId
	 * @param paging
	 * @param filter
	 * @return
	 */
	PagedCollectionHolder<List<AutomatedSuite>> getAutomatedSuitesByIterationID(Long iterationId, PagingAndMultiSorting paging, ColumnFiltering filter);

	/**
	 * Gets a list of AutomatedSuite given a {@link TestSuite} ID.
	 *
	 * @param suiteId
	 * @param paging
	 * @param filter
	 * @return
	 */
	PagedCollectionHolder<List<AutomatedSuite>> getAutomatedSuitesByTestSuiteID(Long suiteId, PagingAndMultiSorting paging, ColumnFiltering filter);

}
