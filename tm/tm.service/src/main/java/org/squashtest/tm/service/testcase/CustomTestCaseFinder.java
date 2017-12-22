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
package org.squashtest.tm.service.testcase;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.Paging;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.testcase.CallTestStep;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.domain.testcase.TestStep;

/**
 * @author Gregory
 * 
 */
public interface CustomTestCaseFinder {

	TestCase findTestCaseWithSteps(long testCaseId);

	List<TestStep> findStepsByTestCaseId(long testCaseId);

	PagedCollectionHolder<List<TestStep>> findStepsByTestCaseIdFiltered(long testCaseId, Paging filter);

	/**
	 * That method returns the list of test cases having at least one CallTestStep directly calling the test case
	 * identified by testCaseId. The list is wrapped in a PagedCollectionHolder, that contains meta informations
	 * regarding the filtering, as usual.
	 * 
	 * @param testCaseId
	 *            the Id of the called test case.
	 * @param sorting
	 *            the sorting parameters.
	 * @return a non null but possibly empty PagedCollectionHolder wrapping the list of first-level calling test cases.
	 * 
	 * @deprecated use {@link #findCallingTestSteps(long, PagingAndSorting)} instead
	 */
	@Deprecated
	PagedCollectionHolder<List<TestCase>> findCallingTestCases(long testCaseId, PagingAndSorting sorting);



	PagedCollectionHolder<List<CallTestStep>> findCallingTestSteps(long testCaseId, PagingAndSorting sorting);

	List<CallTestStep> findAllCallingTestSteps(long testCaseId);

	/**
	 * Fetches all the test cases which have at least one ancestor from the given list. If ancestorID is a folder id,
	 * fetches all its descendant test cases. If it is a test cases id, fetches the given test case.
	 * 
	 * @param ancestorIds
	 * @return
	 */
	List<TestCase> findAllByAncestorIds(@NotNull Collection<Long> ancestorIds);

	/**
	 * Fetches all the test cases calling the one matching the given id param.
	 * 
	 * @param calleeId
	 * @return all calling test cases
	 */
	List<TestCase> findAllCallingTestCases(long calleeId);

	TestCase findTestCaseFromStep(long testStepId);

	/**
	 * Will find the ids and importance of the given test cases with importanceAuto = true
	 * 
	 * @param testCaseIds
	 *            : the id of the testCases
	 * @return a map with :
	 *         <ul>
	 *         <li>key : the id of the test case</li>
	 *         <li>value : it's {@link TestCaseImportance}</li>
	 *         </ul>
	 */
	Map<Long, TestCaseImportance> findImpTCWithImpAuto(Collection<Long> testCaseIds);


	/**
	 * willgo through the list of test-case to update and retain the ones that are calling the updated test case.
	 * 
	 * @param updatedId
	 *            : the id of the test case we want the calling test cases of
	 * @param callingCandidates
	 *            : the ids of potential calling test cases we are interested in
	 * @return the set of test case ids that are calling(direclty or indireclty) the updated one and whose ids are in
	 *         the callingCadidates list.
	 */
	Set<Long> findCallingTCids(long updatedId, Collection<Long> callingCandidates);


	/**
	 * returns direct milestone membership, plus indirect milestones due to verified requirements
	 * 
	 * @param testCaseId
	 * @return
	 */
	Collection<Milestone> findAllMilestones(long testCaseId);






}