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
package org.squashtest.tm.service.internal.testcase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.requirement.RequirementCriticality;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.service.internal.repository.RequirementDao;
import org.squashtest.tm.service.internal.repository.RequirementVersionDao;
import org.squashtest.tm.service.internal.repository.TestCaseDao;
import org.squashtest.tm.service.testcase.TestCaseImportanceManagerService;

@Service
@Transactional
public class TestCaseImportanceManagerServiceImpl implements TestCaseImportanceManagerService {

	@Inject
	private RequirementDao requirementDao;

	@Inject
	private RequirementVersionDao requirementVersionDao;

	@Inject
	private TestCaseDao testCaseDao;

	@Inject private TestCaseCallTreeFinder callTreeFinder;

	/**
	 *
	 * @param testCaseId
	 * @return distinct criticalities found for all verified requirementVersions (including through call steps)
	 */
	private List<RequirementCriticality> findAllDistinctRequirementsCriticalityByTestCaseId(long testCaseId) {
		Set<Long> calleesIds = callTreeFinder.getTestCaseCallTree(testCaseId);
		calleesIds.add(testCaseId);
		return requirementDao.findDistinctRequirementsCriticalitiesVerifiedByTestCases(calleesIds);
	}

	/**
	 * <p>
	 * will deduce the importance of the given test case with the list of it's associated requirementVersions taking
	 * into account the requirementVersions associated through call steps.
	 * </p>
	 * <p>
	 * <i>NB: this can't be done in the setter of "importanceAuto" because of the call-step associated
	 * requirementVersions that is an info handled by the "service" package . </i>
	 * </p>
	 *
	 * @param testCaseId
	 * @return the test case autoCalculated importance
	 */
	private TestCaseImportance deduceImportanceAuto(long testCaseId) {
		List<RequirementCriticality> rCriticalities = findAllDistinctRequirementsCriticalityByTestCaseId(testCaseId);
		return  TestCaseImportance.deduceTestCaseImportance(rCriticalities);
	}

	/**
	 * @see org.squashtest.tm.service.testcase.TestCaseImportanceManagerService#changeImportanceIfIsAuto(long)
	 */
	@Override
	public void changeImportanceIfIsAuto(long testCaseId) {
		TestCase testCase = testCaseDao.findById(testCaseId);
		if (testCase.isImportanceAuto()) {
			TestCaseImportance importance = deduceImportanceAuto(testCaseId);
			testCase.setImportance(importance);
		}
	}

	/**
	 * @see org.squashtest.tm.service.testcase.TestCaseImportanceManagerService#changeImportanceIfRelationsAddedToReq(List,
	 *      RequirementVersion)
	 */
	@Override
	public void changeImportanceIfRelationsAddedToReq(List<TestCase> testCases, RequirementVersion requirementVersion) {
		RequirementCriticality requirementCriticality = requirementVersion.getCriticality();
		for (TestCase testCase : testCases) {
			changeImportanceIfRelationAdded(testCase, requirementCriticality);
		}
	}

	/**
	 * @see org.squashtest.tm.service.testcase.TestCaseImportanceManagerService#changeImportanceIfRelationsAddedToTestCases(List,
	 *      TestCase)
	 */
	@Override
	public void changeImportanceIfRelationsAddedToTestCase(List<RequirementVersion> requirementVersions,
			TestCase testCase) {
		if (!requirementVersions.isEmpty()) {
			List<RequirementCriticality> requirementCriticalities = extractCriticalities(requirementVersions);
			RequirementCriticality strongestRequirementCriticality = RequirementCriticality
					.findStrongestCriticality(requirementCriticalities);
			changeImportanceIfRelationAdded(testCase, strongestRequirementCriticality);
		}
	}

	private List<RequirementCriticality> extractCriticalities(List<RequirementVersion> requirementVersions) {
		List<RequirementCriticality> requirementCriticalities = new ArrayList<>(
				requirementVersions.size());
		for (RequirementVersion requirementVersion : requirementVersions) {
			requirementCriticalities.add(requirementVersion.getCriticality());
		}
		return requirementCriticalities;
	}

	private void changeImportanceIfRelationAdded(TestCase testCase, RequirementCriticality requirementCriticality) {

		if (testCase.isImportanceAuto()) {
			TestCaseImportance importance = testCase.getImportance();
			TestCaseImportance newImportance = importance.deduceNewImporanceWhenAddCriticality(requirementCriticality);
			if (newImportance != importance) {
				testCase.setImportance(newImportance);
				List<TestCase> callingTestCases = testCaseDao.findAllCallingTestCases(testCase.getId(), null);
				for (TestCase callingTestCase : callingTestCases) {
					changeImportanceIfRelationAdded(callingTestCase, requirementCriticality);
				}
			}
		} else {
			List<TestCase> callingTestCases = testCaseDao.findAllCallingTestCases(testCase.getId(), null);
			for (TestCase callingTestCase : callingTestCases) {
				changeImportanceIfRelationAdded(callingTestCase, requirementCriticality);
			}
		}

	}

	/**
	 * @see org.squashtest.tm.service.testcase.TestCaseImportanceManagerService#changeImportanceIfRelationsRemovedFromReq(List,
	 *      long)
	 */
	@Override
	public void changeImportanceIfRelationsRemovedFromReq(List<Long> testCasesIds, long requirementVersionId) {
		RequirementVersion requirementVersion = requirementVersionDao.findOne(requirementVersionId);
		RequirementCriticality requirementCriticality = requirementVersion.getCriticality();
		TestCaseImportance reqCritImportance = TestCaseImportance.deduceTestCaseImportance(Arrays
				.asList(requirementCriticality));
		List<TestCase> testCases = extractTestCases(testCasesIds);
		for (TestCase testCase : testCases) {
			changeImportanceIfRelationRemoved(reqCritImportance, testCase);
		}
	}

	/**
	 * @see org.squashtest.tm.service.testcase.TestCaseImportanceManagerService#changeImportanceIfRelationRemoved(TestCaseImportance,
	 *      TestCase)
	 */
	@Override
	public void changeImportanceIfRelationRemoved(TestCaseImportance maxReqCritImportance, TestCase testCase) {
		if (testCase.isImportanceAuto()) {
			TestCaseImportance actualImportance = testCase.getImportance();
			if (maxReqCritImportance.getLevel() <= actualImportance.getLevel()) {
				TestCaseImportance newImportance = deduceImportanceAuto(testCase.getId());
				if (newImportance != actualImportance) {
					testCase.setImportance(newImportance);
					List<TestCase> callingTestCases = testCaseDao.findAllCallingTestCases(testCase.getId(), null);
					for (TestCase callingTestCase : callingTestCases) {
						changeImportanceIfRelationRemoved(maxReqCritImportance, callingTestCase);
					}
				}
			}
		} else {
			List<TestCase> callingTestCases = testCaseDao.findAllCallingTestCases(testCase.getId(), null);
			for (TestCase callingTestCase : callingTestCases) {
				changeImportanceIfRelationRemoved(maxReqCritImportance, callingTestCase);
			}
		}
	}

	private List<TestCase> extractTestCases(List<Long> testCasesIds) {
		List<TestCase> testCases = new ArrayList<>(testCasesIds.size());
		for (long testCaseId : testCasesIds) {
			testCases.add(testCaseDao.findById(testCaseId));

		}
		return testCases;
	}

	/**
	 * @see org.squashtest.tm.service.testcase.TestCaseImportanceManagerService#changeImportanceIfRelationsRemovedFromTestCase(List,
	 *      long)
	 */
	@Override
	public void changeImportanceIfRelationsRemovedFromTestCase(List<Long> requirementsVersionIds, long testCaseId) {
		if (!requirementsVersionIds.isEmpty()) {
			TestCase testCase = testCaseDao.findById(testCaseId);
			List<RequirementCriticality> reqCriticalities = requirementDao
					.findDistinctRequirementsCriticalities(requirementsVersionIds);
			TestCaseImportance maxReqCritImportance = TestCaseImportance.deduceTestCaseImportance(reqCriticalities);
			changeImportanceIfRelationRemoved(maxReqCritImportance, testCase);
		}
	}

	/**
	 * @see org.squashtest.tm.service.testcase.TestCaseImportanceManagerService#changeImportanceIfRequirementCriticalityChanged(long,
	 *      RequirementCriticality)
	 */
	@Override
	public void changeImportanceIfRequirementCriticalityChanged(long requirementVersionId,
			RequirementCriticality oldRequirementCriticality) {
		RequirementVersion requirementVersion = requirementVersionDao.findOne(requirementVersionId);
		List<TestCase> testCases = testCaseDao.findUnsortedAllByVerifiedRequirementVersion(requirementVersionId);
		for (TestCase testCase : testCases) {
			changeImportanceIfRequirementCriticalityChanged(oldRequirementCriticality, requirementVersion, testCase);
		}
	}

	private void changeImportanceIfRequirementCriticalityChanged(RequirementCriticality oldRequirementCriticality,
			RequirementVersion requirementVersion, TestCase testCase) {
		// if test-case is auto
		if (testCase.isImportanceAuto()) {
			TestCaseImportance importanceAuto = testCase.getImportance();
			// if change of criticality can change importanceAuto
			boolean importanceAutoCanChange = importanceAuto.changeOfCriticalityCanChangeImportanceAuto(
					oldRequirementCriticality, requirementVersion.getCriticality());
			if (importanceAutoCanChange) {
				// -if it changes
				TestCaseImportance newImportanceAuto = deduceImportanceAuto(testCase.getId());

				if (importanceAuto != newImportanceAuto) {
					// -- => change importance
					testCase.setImportance(newImportanceAuto);
					// -- look for any calling test case and call the method on
					// them

					List<TestCase> callingTestCases = testCaseDao.findAllCallingTestCases(testCase.getId(), null);
					for (TestCase callingTestCase : callingTestCases) {
						changeImportanceIfRequirementCriticalityChanged(oldRequirementCriticality, requirementVersion,
								callingTestCase);
					}
				}

			}
		} else {
			// call the method in callers
			List<TestCase> callingTestCases = testCaseDao.findAllCallingTestCases(testCase.getId(), null);
			for (TestCase callingTestCase : callingTestCases) {
				changeImportanceIfRequirementCriticalityChanged(oldRequirementCriticality, requirementVersion,
						callingTestCase);
			}
		}
	}

	/**
	 * @see org.squashtest.tm.service.testcase.TestCaseImportanceManagerService#changeImportanceIfCallStepAddedToTestCases(TestCase,
	 *      TestCase)
	 */
	@Override
	public void changeImportanceIfCallStepAddedToTestCases(TestCase calledTestCase, TestCase parentTestCase) {
		List<RequirementCriticality> rCriticalities = findAllDistinctRequirementsCriticalityByTestCaseId(calledTestCase
				.getId());
		if (!rCriticalities.isEmpty()) {
			RequirementCriticality strongestRequirementCriticality = RequirementCriticality
					.findStrongestCriticality(rCriticalities);
			changeImportanceIfRelationAdded(parentTestCase, strongestRequirementCriticality);
		}
	}

	/**
	 * @see org.squashtest.tm.service.testcase.TestCaseImportanceManagerService#changeImportanceIfCallStepRemoved(TestCase,
	 *      TestCase)
	 */
	@Override
	public void changeImportanceIfCallStepRemoved(TestCase calledTestCase, TestCase parentTestCase) {
		List<RequirementCriticality> rCriticalities = findAllDistinctRequirementsCriticalityByTestCaseId(calledTestCase
				.getId());
		if (!rCriticalities.isEmpty()) {
			TestCaseImportance maxReqCritImportance = TestCaseImportance.deduceTestCaseImportance(rCriticalities);
			changeImportanceIfRelationRemoved(maxReqCritImportance, parentTestCase);
		}

	}

}
