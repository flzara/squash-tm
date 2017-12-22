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

import org.squashtest.tm.domain.requirement.RequirementCoverageStat;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.exception.requirement.RequirementVersionNotLinkableException;
import org.squashtest.tm.exception.requirement.VerifiedRequirementException;

/**
 * Service for management of Requirements verified by a {@link TestCase}
 *
 * @author Gregory Fouquet
 *
 */
public interface VerifiedRequirementsManagerService extends VerifiedRequirementsFinderService{
	/**
	 * Adds a list of requirements to the ones verified by a test case. If a requirement is already verified, nothing
	 * special happens.
	 *
	 * @param requirementsIds
	 * @param testCaseId
	 * @return
	 */
	Collection<VerifiedRequirementException> addVerifiedRequirementsToTestCase(List<Long> requirementsIds,
			long testCaseId);

	/**
	 * Adds a list of requirement's current requirement-versions to the ones verified by the step's test case and bind them to the step. If the version  already verified by the test case, it is only bound to the step.
	 * If a sister version is already bound to the test case the version is not added.
	 *
	 * @param requirementsIds
	 * @param testStepId
	 *            : the id of the concerned {@link ActionTestStep}
	 * @return
	 */
	Collection<VerifiedRequirementException> addVerifiedRequirementsToTestStep(List<Long> requirementsIds,
			long testStepId);
	/**
	 * Adds the requirement-version to the ones verified by the step's test case and bind it to the step. If the version is already verified by the test case, it is only bound to the step.
	 * If a sister version is already bound to the test case the version is not added.
	 * @param requirementVersionId
	 * @param testStepId
	 * @return
	 */
	Collection<VerifiedRequirementException> addVerifiedRequirementVersionToTestStep(long requirementVersionId,
			long testStepId);
	/**
	 * Adds a list of requirement-versions to the ones verified by a test case. If the version or a sister is already
	 * verified, the requirement is not added and nothing special happens.
	 *
	 * @param requirementVersionsByTestCase
	 *            : list of requirementVersions mapped by test-case
	 * @return
	 */
	Collection<VerifiedRequirementException> addVerifyingRequirementVersionsToTestCase(
			Map<TestCase, List<RequirementVersion>> requirementVersionsByTestCase);

	/**
	 * Removes a list of requirements from the ones verified by a test case. If a requirement is not verified by the
	 * test case, nothing special happens.
	 *
	 * @param testCaseId
	 * @param requirementVersionsIds
	 */
	void removeVerifiedRequirementVersionsFromTestCase(List<Long> requirementVersionsIds, long testCaseId);

	/**
	 * Removes a requirement from the ones verified by a test case. If the requirement was not previously verified by
	 * the test case, nothing special happens.
	 *
	 * @param testCaseId
	 * @param requirementsIds
	 */
	void removeVerifiedRequirementVersionFromTestCase(long requirementVersionId, long testCaseId);

	/**
	 * Removes a requirement version from the step but not from the test case. If the requirement version was not previously verified by
	 * the test step, nothing special happens.
	 *
	 * @param testStepId
	 * @param requirementsIds
	 */
	void removeVerifiedRequirementVersionsFromTestStep(List<Long> requirementVersionsIds, long testStepId);

	/**
	 * Removes a requirement version from the given test case and replaces it with the wanted version if the same
	 * version is selected as was previously attached, nothing happens
	 *
	 * @param testCaseId
	 * @param oldVerifiedRequirementVersionId
	 * @param newVerifiedRequirementVersionId
	 * @throws RequirementVersionNotLinkableException
	 */
	int changeVerifiedRequirementVersionOnTestCase(long oldVerifiedRequirementVersionId,
			long newVerifiedRequirementVersionId, long testCaseId);

	/**
	 * Return a list of coverage rate for a designed requirement version
	 * @param requirementVersionId
	 * @param iterations
	 * @return
	 */
	void findCoverageStat(Long requirementVersionId, List<Long> iterationsIds, RequirementCoverageStat stats);




}
