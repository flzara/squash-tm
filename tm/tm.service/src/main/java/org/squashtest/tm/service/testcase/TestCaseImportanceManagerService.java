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

import java.util.List;

import org.squashtest.tm.domain.requirement.RequirementCriticality;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseImportance;

public interface TestCaseImportanceManagerService {

	/**
	 * <p>
	 * will compute and update the importance of the testCase if it's importanceAuto == true.
	 * </p>
	 * 
	 * @param testCaseId
	 */
	void changeImportanceIfIsAuto(long testCaseId);

	/**
	 * <p>
	 * will compute and update the importance of each test-case of the list<br>
	 * and, for each test-case "TC" of the list, if necessary, will update the importance of any test-case calling the
	 * "TC".
	 * </p>
	 * 
	 * @param testCases
	 *            list of test-cases added to the requirement
	 * @param requirementVersion
	 */
	void changeImportanceIfRelationsAddedToReq(List<TestCase> testCases, RequirementVersion requirementVersion);

	/**
	 * <p>
	 * will compute and update the importance of the test-case if it's importance is auto <br>
	 * and, if necessary, will update the importance of any test-case calling the parameter test-case.
	 * </p>
	 * 
	 * @param requirements
	 *            list of requirements added to the test-case
	 * @param testCase
	 */
	void changeImportanceIfRelationsAddedToTestCase(List<RequirementVersion> requirementVersions, TestCase testCase);

	/**
	 * <p>
	 * will compute and update the importance of the test-cases if their importance is auto<br>
	 * and, for each test-case "TC" of the list, if necessary, will update the importance of any test-case calling the
	 * "TC".
	 * </p>
	 * 
	 * @param testCasesIds
	 * @param requirementId
	 */
	void changeImportanceIfRelationsRemovedFromReq(List<Long> testCasesIds, long requirementVersionId);

	/**
	 * <p>
	 * will compute and update the importance of the test-case if it's importance is auto <br>
	 * and, if necessary, will update the importance of any test-case calling the parameter test-case.
	 * </p>
	 * 
	 * @param requirementVersionsIds
	 * @param testCaseId
	 */
	void changeImportanceIfRelationsRemovedFromTestCase(List<Long> requirementVersionsIds, long testCaseId);

	/**
	 * <p>
	 * will update the importance of any directly associated test-case if it's importanceAuto = true. <br>
	 * takes also care of test-cases calling the directly associated ones.<br>
	 * <i>this method must be called before the modification of criticality</i>
	 * </p>
	 * 
	 * @param requirementId
	 * @param oldRequirementCriticality
	 */
	void changeImportanceIfRequirementCriticalityChanged(long requirementVersionId,
			RequirementCriticality oldRequirementCriticality);

	/**
	 * <p>
	 * will compute and update the importance of the parent testCase if it's importance is auto <br>
	 * and, if necessary, will update the importance of any test-case calling the parent test-case.
	 * </p>
	 * 
	 * @param calledTestCase
	 * @param parentTestCase
	 */
	void changeImportanceIfCallStepAddedToTestCases(TestCase calledTestCase, TestCase parentTestCase);

	/**
	 * <p>
	 * will compute and update the importance of the parent test case if it's importance is auto <br>
	 * and, if necessary, will update the importance of any test-case calling the parent test-case.
	 * </p>
	 * 
	 * @param calledTestCase
	 * @param parentTestCase
	 */
	void changeImportanceIfCallStepRemoved(TestCase calledTestCase, TestCase parentTestCase);

	/**
	 * <p>
	 * will compute and update the importance of the test case and it's parents given the removal of requirements
	 * </p>
	 * 
	 * @param reqCritImportance
	 *            is the testCaseImportance that is computed out of the list of removed requirements
	 * @param testCase
	 *            the test case where the requirements are removed from
	 */
	void changeImportanceIfRelationRemoved(TestCaseImportance reqCritImportance, TestCase testCase);


}
