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

import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.domain.testcase.RequirementVersionCoverage;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.annotation.EmptyCollectionGuard;

/**
 * Data access methods for {@link RequirementVersionCoverage}s. Methods are all dynamically generated: see {@link DynamicDaoFactoryBean}.
 * 
 * @author mpagnon
 * 
 */
public interface RequirementVersionCoverageDao extends Repository<RequirementVersionCoverage, Long>, CustomRequirementVersionCoverageDao{



	/**
	 * Will return the {@link RequirementVersionCoverage} entity matching the given verifying and verified params.
	 * @param verifiedRequirementVersionId : the id of the verified {@link RequirementVersion}
	 * @param verifyingTestCaseId : the id of the verifying {@link TestCase}
	 * @return the corresponding {@link RequirementVersionCoverage}
	 */
	// note : uses a named query in package-info or elsewhere	
	RequirementVersionCoverage byRequirementVersionAndTestCase(@Param("rvId") long verifiedRequirementVersionId, @Param("tcId") long verifyingTestCaseId);

	/**
	 * Will return the {@link RequirementVersionCoverage} entities matching the verified requirementVersion and one of the verifying test case params.
	 * @param verifyingTestCasesIds : the ids of the concerned {@link TestCase}s
	 * @param verifiedRequirementVersionId : the id of the concerned {@link RequirementVersion}
	 * @return a list of matching {@link RequirementVersionCoverage}
	 */
	// note : uses a named query in package-info or elsewhere	
	@EmptyCollectionGuard
	List<RequirementVersionCoverage> byRequirementVersionAndTestCases(@Param("tcIds") List<Long> verifyingTestCasesIds,
			@Param("rvId")	long verifiedRequirementVersionId);

	/**
	 * Will return the {@link RequirementVersionCoverage} entities matching the verifying test-case and one of the verified requirement versions
	 * @param verifiedRequirementVersionsIds : the ids of the concerned {@link RequirementVersion}s
	 * @param verifyingTestCaseId : the id of the concerned {@link TestCase}
	 * @return a list of matching {@link RequirementVersionCoverage}
	 */
	// note : uses a named query in package-info or elsewhere	
	@EmptyCollectionGuard
	List<RequirementVersionCoverage> byTestCaseAndRequirementVersions(@Param("rvIds") List<Long> verifiedRequirementVersionsIds,
			@Param("tcId")	long verifyingTestCaseId);

	/**
	 * will return the {@link RequirementVersionCoverage} entities matching one of the verified requirement version and linked to the test step.
	 * @param verifiedRequirementVersionsIds : the ids of the concerned {@link RequirementVersion}s
	 * @param testStepId : the id of the concerned {@link ActionTestStep}
	 * @return the list of matching {@link RequirementVersionCoverage}
	 */
	// note : uses a named query in package-info or elsewhere	
	@EmptyCollectionGuard
	List<RequirementVersionCoverage> byRequirementVersionsAndTestStep(@Param("rvIds") List<Long> verifiedRequirementVersionsIds, @Param("stepId")long testStepId);

	/**
	 * Returns the total amount of {@link RequirementVersionCoverage} witch verifying {@link TestCase}'s id matches the given param.
	 * 
	 * @param testCaseId : the id of the verifying {@link TestCase}
	 * @return the amount of {@link RequirementVersionCoverage} for this test case
	 */
	// note : uses a named query in package-info or elsewhere	
	long numberByTestCase(@Param("tcId") long testCaseId);

	/**
	 * Returns the total amount of {@link RequirementVersion} witch verifying {@link TestCase}'s id matches on of the given id params.
	 * 
	 * @param testCaseIds : the ids of verifying {@link TestCase}s
	 * @return the amount of distinct {@link RequirementVersion} for these test cases
	 */
	// note : uses a named query in package-info or elsewhere	
	@EmptyCollectionGuard
	long numberDistinctVerifiedByTestCases(@Param("tcIds") Collection<Long> testCaseIds);

	/**
	 * Returns the total amount of distinct {@link RequirementVersionCover} witch verifying {@link TestCase}'s id matches on of the given id params.
	 * 
	 * @param testCaseIds : the ids of verifying {@link TestCase}s
	 * @return the amount of {@link RequirementVersionCoverage} for these test cases
	 */
	// note : uses a named query in package-info or elsewhere	
	@EmptyCollectionGuard
	long numberByTestCases(@Param("tcIds") Collection<Long> testCaseIds);




}
