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

import org.squashtest.tm.core.foundation.collection.Paging;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.NamedReferencePair;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.testcase.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Gregory Fouquet
 */
public interface CustomTestCaseDao extends EntityDao<TestCase> {


	/**
	 * That method exists because hibernate doesn't quite respect the cascade persist for a set of test case and steps
	 */
	void persistTestCaseAndSteps(TestCase testCase);

	/**
	 * if the transient test case has steps, will invoke {@link #persistTestCaseAndSteps(TestCase)} else, will just save
	 * it.
	 */
	void safePersist(TestCase testCase);

	TestCase findAndInit(Long testCaseId);


	/**
	 * Given a test case id, return its steps ordered by their index
	 */
	List<TestStep> findTestSteps(long testCaseId);


	List<Long> findAllTestCaseIdsByNodeIds(Collection<Long> nodeIds);

	/***
	 * This method returns the test step's associated TestCase
	 *
	 * @param testStepId the test step id
	 * @return the associated test Case
	 */
	TestCase findTestCaseByTestStepId(long testStepId);

	/**
	 * Given a list of test case ids, returns a sublist of the test case ids. An id will be included in the output list
	 * if at least one test case calls the given test case.
	 *
	 * @param testCasesIds the list of test case ids under inquiry.
	 * @return a sublist of the input list, with test cases never called filtered out.
	 */
	List<Long> findTestCasesHavingCaller(Collection<Long> testCasesIds);

	/**
	 * Given a list of test case ids, returns data about those test cases and which test cases called them.
	 * The result is a list of NamedReferencePair.
	 * If a test case calls the same other test case multiple times the resultset will contain as many pairs.
	 * <p>
	 * Note that only first-level callers will be included if found, additional invokations will be needed to fetch all
	 * the hierarchy.
	 *
	 * @param testCaseIds the list of test case ids under inquiry.
	 * @return a structure described just like above.
	 */
	List<NamedReferencePair> findTestCaseCallsUpstream(Collection<Long> testCaseIds);

	/**
	 * Given a list of test case ids, returns data about those test cases and which test cases they do call.
	 * The result is a list of NamedReferencePair.
	 * If a test case calls the same other test case multiple times the resultset will contain as many pairs.
	 * <p>
	 * Note that only first-level callers will be included if found, additional invokations will be needed to fetch all
	 * the hierarchy.
	 *
	 * @param testCaseIds the list of test case ids under inquiry.
	 * @return a structure described just like above.
	 */
	List<NamedReferencePair> findTestCaseCallsDownstream(Collection<Long> testCaseIds);


	/**
	 * Finds all the ids of the test cases called by a given list of test cases.
	 */
	List<Long> findAllTestCasesIdsCalledByTestCases(Collection<Long> testCasesIds);

	/**
	 * Finds all the ids of the test cases that are calling the ones matching the testCasesIds parameter
	 *
	 * @param testCasesIds : the ids of the called test cases
	 * @return the ids of the calling test cases.
	 */
	List<Long> findAllTestCasesIdsCallingTestCases(List<Long> testCasesIds);

	/**
	 * returns the test cases having at least one call test step referencing the given test case.
	 *
	 * @param testCaseId the id of the test case.
	 * @param sorting    the sorting attributes and the like.
	 * @return the list of test cases having at least one call step calling the input test case.
	 */
	List<TestCase> findAllCallingTestCases(long testCaseId, PagingAndSorting sorting);

	/**
	 * return all test cases having at least one call test step referencing the given test case.
	 *
	 * @param calleeId the id of the called test case
	 * @return the test cases calling the test case matching the given id param.
	 */
	List<TestCase> findAllCallingTestCases(long calleeId);

	/**
	 * returns all the call test step that reference the test case given its id.
	 * Note that like in {@link #findAllCallingTestCases(long, PagingAndSorting)},
	 * the paging and sorting can sort on attributes that belong to the caller
	 * TestCases
	 */
	List<CallTestStep> findAllCallingTestSteps(long testCaseId, PagingAndSorting sorting);


	/**
	 * invokes #findAllCallingTestSteps, sorting by project name, test case reference, test case name and step no
	 */
	List<CallTestStep> findAllCallingTestSteps(long testCaseId);

	/**
	 * Returns the test cases ids first called by the call steps found in the list of given test steps ids. Note: only
	 * first level called test case are returned.
	 *
	 * @return the list of test case ids called by test steps
	 */
	List<Long> findCalledTestCaseOfCallSteps(List<Long> testStepsIds);

	/**
	 * Returns paged and sorted collection of test cases verifying the requirement version of given id.
	 */
	List<TestCase> findAllByVerifiedRequirementVersion(long verifiedId, PagingAndSorting sorting);

	long countByVerifiedRequirementVersion(long verifiedId);

	/**
	 * Returns unsorted collection of test cases verifying the requirement version of given id.
	 */
	List<TestCase> findUnsortedAllByVerifiedRequirementVersion(long requirementId);


	/**
	 * Returns all the execution associated to this test-case
	 */
	List<Execution> findAllExecutionByTestCase(Long tcId);


	List<ExportTestCaseData> findTestCaseToExportFromNodes(List<Long> nodesIds);

	/**
	 * Return all test-cases that are linked to an iteration-test-plan-item
	 */
	List<TestCase> findAllLinkedToIteration(List<Long> nodeIds);

	List<TestStep> findAllStepsByIdFiltered(long testCaseId, Paging filter);

	/**
	 * Will return all ids and importanceof test cases among the given ones that have the property importanceAuto to true.
	 *
	 * @param testCaseIds : the ids of the testCases to filter
	 * @return the filter result with ids of test case having their importanceAuto to true.
	 */
	Map<Long, TestCaseImportance> findAllTestCaseImportanceWithImportanceAuto(Collection<Long> testCaseIds);


}
