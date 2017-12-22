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

import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.RequirementVersionCoverage;
import org.squashtest.tm.domain.testcase.TestCase;

public interface CustomRequirementVersionCoverageDao extends EntityDao<RequirementVersionCoverage>{

	/**
	 * Returns a paged and ordered list taken from all {@link RequirementVersionCoverage} directly linked to the {@link TestCase} matching the given id param.
	 * @param testCaseId : the id of the verifying {@link TestCase}
	 * @param pas : the {@link PagingAndSorting} param to organize the result with
	 * @return : a list of {@link RequirementVersionCoverage} representing a sorted page of all direct coverages for the given test case
	 */
	List<RequirementVersionCoverage> findAllByTestCaseId(long testCaseId, PagingAndSorting pas);


	/**
	 * Returns a paged and ordered list taken from all DISTINCT {@link RequirementVersion} linked to at least one of the {@link TestCase}s matching the given ids param.
	 * @param testCaseIds : the ids of the verifying {@link TestCase}s
	 * @param pas : the {@link PagingAndSorting} param to organize the result with
	 * @return : a list of {@link RequirementVersion} representing a sorted page of distinct requirements for the given test cases
	 */
	List<RequirementVersion> findDistinctRequirementVersionsByTestCases(Collection<Long> testCaseIds, PagingAndSorting pas);

	/**
	 * Returns a list of all DISTINCT {@link RequirementVersion} linked to at least one of the {@link TestCase}s matching the given ids param.
	 * @param testCaseIds : the ids of the verifying {@link TestCase}s
	 * @return : a list of {@link RequirementVersion} representing all distinct requirements for the given test cases
	 */
	@Transactional(readOnly=true)
	List<RequirementVersion> findDistinctRequirementVersionsByTestCases(Collection<Long> testCaseIds);


	/**
	 * Simply delete the given {@link RequirementVersionCoverage}, ensuring that relationships with the TestCase and its steps
	 * are removed too.
	 * 
	 * @param requirementVersionCoverage
	 */
	void delete(RequirementVersionCoverage requirementVersionCoverage);

}
