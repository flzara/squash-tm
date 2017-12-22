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
package org.squashtest.tm.service.requirement;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.CallTestStep;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestStep;

/**
 * Service for finding Requirements verified by a {@link TestCase}
 * 
 * @author mpagnon
 * 
 */
public interface VerifiedRequirementsFinderService {
	
	/**
	 * Returns the filtered list of {@link VerifiedRequirement}s directly verified by a test case. The non directly
	 * verified requirements (by called test-cases) are NOT included in the result.
	 * 
	 * @param testCaseId
	 *            : the id of the concerned {@link TestCase}.
	 * @param pas
	 *            : the {@link PagingAndSorting} to organize the result with
	 * @return a {@link PagedCollectionHolder} of {@link VerifiedRequirement} containing directly verified requirements
	 *         for the test case of the given id.
	 */
	PagedCollectionHolder<List<VerifiedRequirement>> findAllDirectlyVerifiedRequirementsByTestCaseId(long testCaseId,
			PagingAndSorting pas);

	/**
	 * Returns all {@link VerifiedRequirement} for the TestCase matching the given id. VerifiedRequirements verified by
	 * the {@link CallTestStep}s of the TestCase will be included.
	 * 
	 * @param testCaseId
	 *            : the id of the concerned {@link TestCase}
	 * @param pas
	 *            : the {@link PagingAndSorting} to organize the result with
	 * @return a {@link PagedCollectionHolder} of {@link VerifiedRequirement} containing directly and non directly (call
	 *         steps) verified requirements for the test case of the given id.
	 */
	PagedCollectionHolder<List<VerifiedRequirement>> findAllVerifiedRequirementsByTestCaseId(long testCaseId,
			PagingAndSorting pas);

	/**
	 * Returns all {@link VerifiedRequirement} for the TestCase matching the given id. VerifiedRequirements verified by
	 * the {@link CallTestStep}s of the TestCase will be included.
	 * 
	 * @param testCaseId
	 *            : the id of the concerned {@link TestCase}
	 * @return a List of {@link VerifiedRequirement} containing directly and non directly (call steps) verified
	 *         requirements for the test case of the given id.
	 */
	List<VerifiedRequirement> findAllVerifiedRequirementsByTestCaseId(long testCaseId);

	/**
	 * Will go through the given calling test cases. and will return their
	 * new 'isRequirementCovered' property.
	 * 
	 * @param updatedTestCaseId
	 *            : the id of the test case that has just been updated and might impact the 'calling' test cases
	 *            properties
	 * @param callingIds
	 *            : the ids of the calling test cases (direclty or indirectly) we want the 'isRequirementCovered'
	 *            property of.
	 * 
	 * @return a map with :
	 *         <ul>
	 *         <li>key : the id of the test case</li>
	 *         <li>value : true if the test-case has direct or indirect verified requirement.</li>
	 *         </ul>
	 */
	Map<Long, Boolean> findisReqCoveredOfCallingTCWhenisReqCoveredChanged(long updatedTestCaseId,
			Collection<Long> callingIds);

	/**
	 * Will find all {@link RequirementVersion} verified by the test case containing the step of the given id. The
	 * result will be paged according to the given {@link PagingAndSorting} param.
	 * 
	 * @param testStepId
	 *            : the id of the concerned {@link TestStep}
	 * @param paging
	 *            : the {@link PagingAndSorting} to organize the result with
	 * @return the list of verified requirements, paged and sorted.
	 */
	PagedCollectionHolder<List<VerifiedRequirement>> findAllDirectlyVerifiedRequirementsByTestStepId(long testStepId,
			PagingAndSorting paging);

	/**
	 * Will check if the test case matching the given id is linked directly to at least one requirement.
	 * 
	 * @param testCaseId
	 *            : the id of the test case to check the direct requirement coverage of
	 * @return true if the test case matching the given id is linked to at least one requirement
	 */
	boolean testCaseHasUndirectRequirementCoverage(long testCaseId);

	/**
	 * Will check if one called test case is covered by a requirement
	 * 
	 * @param testCaseId
	 *            : the id of the test case to check the called test case of
	 * @return true if one called test case is linked to a requirement
	 */
	boolean testCaseHasDirectCoverage(long testCaseId);

}
