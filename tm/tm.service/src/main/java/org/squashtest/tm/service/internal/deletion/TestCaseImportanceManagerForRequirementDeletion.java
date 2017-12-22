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
package org.squashtest.tm.service.internal.deletion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.requirement.RequirementCriticality;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.service.internal.repository.RequirementDao;
import org.squashtest.tm.service.testcase.TestCaseImportanceManagerService;

@Component
@Scope("prototype")
public class TestCaseImportanceManagerForRequirementDeletion {
	private Map<TestCase, List<RequirementCriticality>> requirementDeletionConcernedTestCases;

	@Inject
	private RequirementDao requirementDao;

	@Inject
	private TestCaseImportanceManagerService testCaseImportanceManagerService;

	public TestCaseImportanceManagerForRequirementDeletion() {

	}

	/**
	 * <p>
	 * will find TestCases concerned by deleted requirements versions and store them in property along with the
	 * concerned criticalities
	 * </p>
	 *
	 * @param requirementIds
	 */
	public void prepareRequirementDeletion(List<Long> requirementLibrarieNodesIds) {
		this.requirementDeletionConcernedTestCases = new HashMap<>();
		List<RequirementVersion> requirementVersions = requirementDao.findVersionsForAll(requirementLibrarieNodesIds);
		storeReqVersionConcernedTestCases(requirementVersions);
	}

	private void storeReqVersionConcernedTestCases(List<RequirementVersion> requirementVersions) {
		for (RequirementVersion reqVersion : requirementVersions) {
			Set<TestCase> concernedTestCases = reqVersion.getVerifyingTestCases();
			RequirementCriticality nextCriticality = reqVersion.getCriticality();
			storeConcernedTestCases(concernedTestCases, nextCriticality);
		}
	}

	private void storeConcernedTestCases(Set<TestCase> concernedTestCases, RequirementCriticality nextCriticality) {
		for (TestCase testCase : concernedTestCases) {
			storeConcernedTestCase(nextCriticality, testCase);
		}
	}

	private void storeConcernedTestCase(RequirementCriticality nextCriticality, TestCase testCase) {
		if (this.requirementDeletionConcernedTestCases.containsKey(testCase)) {
			List<RequirementCriticality> storedCriticalities = this.requirementDeletionConcernedTestCases.get(testCase);
			storedCriticalities.add(nextCriticality);

		} else {
			List<RequirementCriticality> reqCriticalities = new ArrayList<>();
			reqCriticalities.add(nextCriticality);
			this.requirementDeletionConcernedTestCases.put(testCase, reqCriticalities);

		}
	}

	/**
	 * <p>
	 * <b style="color:red">Warning !! </b>this method is to be used from a
	 * {@linkplain TestCaseImportanceManagerForRequirementDeletion} that has been instantiated just before the
	 * requirements deletion, and after the
	 * {@linkplain TestCaseImportanceManagerForRequirementDeletion#prepareRequirementDeletion(List)} method<br>
	 * <br>
	 * will adapt the importance of the TestCases concerned by deleted requirements versions if their importanceAuto
	 * property is 'true'.
	 * </p>
	 *
	 */
	public void changeImportanceAfterRequirementDeletion() {
		if (this.requirementDeletionConcernedTestCases != null && !this.requirementDeletionConcernedTestCases.isEmpty()) {
			for (Entry<TestCase, List<RequirementCriticality>> testCaseAndCriticalities : this.requirementDeletionConcernedTestCases
					.entrySet()) {
				TestCase testCase = testCaseAndCriticalities.getKey();
				List<RequirementCriticality> requirementCriticalities = testCaseAndCriticalities.getValue();
				TestCaseImportance maxReqCritImportance = TestCaseImportance
						.deduceTestCaseImportance(requirementCriticalities);
				testCaseImportanceManagerService.changeImportanceIfRelationRemoved(maxReqCritImportance, testCase);
			}
		}
		this.requirementDeletionConcernedTestCases = null;
	}
}
