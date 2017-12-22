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

import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.requirement.RequirementLibrary;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseLibrary;

/**
 * Service for management of Requirements verified by a {@link TestCase}
 * 
 * @author Gregory Fouquet
 * 
 */
public interface VerifyingTestCaseManagerService {
	final String REJECTION_KEY = "REJECTION";
	final String IDS_KEY = "IDS";

	/**
	 * Returns the collection of {@link RequirementLibrary}s which Requirements can be linked by a {@link TestCase}
	 * 
	 * @return
	 */
	List<TestCaseLibrary> findLinkableTestCaseLibraries();

	/**
	 * Adds a list of test cases to the ones verified by a requirement. If a test-case is already verified, nothing
	 * special happens.
	 * 
	 * @param requirementsIds
	 * @param testCaseId
	 * @return a map with
	 *         <ul>
	 *         <li>key : {@link #REJECTION_KEY} , value : Collection of VerifiedRequirementException</li>
	 *         <li>key : {@link #IDS_KEY}, value : Collection of associated TestCases Ids (including test cases
	 *         contained by selected folders</li>
	 *         </ul>
	 */
	Map<String, Collection<?>> addVerifyingTestCasesToRequirementVersion(List<Long> testCaseIds, long requirementVersionId);

	/**
	 * Removes a list of test-cases from the ones verified by a requirment. If a test-case is not verified by the test
	 * case, nothing special happens.
	 * 
	 * @param testCaseId
	 * @param requirementsIds
	 */
	void removeVerifyingTestCasesFromRequirementVersion(List<Long> testCaseIds, long requirementVersionId);

	/**
	 * Removes a test-case from the ones verified by a requirement. If the test-case was not previously verified by the
	 * requirment, nothing special happens.
	 * 
	 * @param testCaseId
	 * @param requirementsIds
	 */
	void removeVerifyingTestCaseFromRequirementVersion(long testCaseId, long requirementVersionId);

	/**
	 * @param requirementId
	 * @param pagingAndSorting
	 * @return
	 */
	@Transactional(readOnly = true)
	PagedCollectionHolder<List<TestCase>> findAllByRequirementVersion(long requirementId,
			PagingAndSorting pagingAndSorting);

	/**
	 * 
	 * @param requirementVersionId
	 * @return
	 */
	@Transactional(readOnly = true)
	List<TestCase> findAllByRequirementVersion(long requirementVersionId);

}
