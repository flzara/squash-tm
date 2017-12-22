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
package org.squashtest.tm.service.internal.testcase

import static org.squashtest.tm.domain.testcase.TestCaseImportance.*
import static RequirementCriticality.*

import org.squashtest.tm.tools.unittest.reflection.ReflectionCategory
import org.squashtest.tm.domain.requirement.RequirementCriticality
import org.squashtest.tm.domain.requirement.RequirementVersion
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode
import org.squashtest.tm.service.internal.repository.RequirementDao
import org.squashtest.tm.service.internal.repository.RequirementVersionDao
import org.squashtest.tm.service.internal.repository.TestCaseDao
import spock.lang.Specification
class TestCaseImportanceManagerServiceImplTest extends Specification {
	TestCaseImportanceManagerServiceImpl service = new TestCaseImportanceManagerServiceImpl()
	TestCaseCallTreeFinder callTreeFinder = Mock()
	RequirementDao requirementDao = Mock()
	RequirementVersionDao requirementVersionDao = Mock()
	TestCaseDao testCaseDao = Mock()

	def setup() {
		service.testCaseDao = testCaseDao
		service.requirementDao = requirementDao
		service.callTreeFinder = callTreeFinder
		service.requirementVersionDao = requirementVersionDao
	}
	def"should change importance if is Auto"(){
		given:

		TestCase testCase = new TestCase()
		testCase.setImportanceAuto true
		testCase.setImportance LOW
		testCaseDao.findById(10) >> testCase

		Set<Long> calleesIds = new HashSet<Long>()
		callTreeFinder.getTestCaseCallTree(10)>> calleesIds

		List<RequirementCriticality> crits = Arrays.asList(UNDEFINED, CRITICAL)
		requirementDao.findDistinctRequirementsCriticalitiesVerifiedByTestCases(calleesIds)>> crits


		when:
		service.changeImportanceIfIsAuto(10)
		then:
		testCase.getImportance() == HIGH
	}
	def"should NOT change importance if is NOT Auto"(){
		given:

		TestCase testCase = new TestCase()
		testCase.setImportanceAuto false
		testCase.setImportance LOW
		testCaseDao.findById(10) >> testCase

		when:
		service.changeImportanceIfIsAuto(10)
		then:
		testCase.getImportance() == LOW
	}
	def "should change importance if relation added to Req"(){
		given:
		//only one test case is added with importance = low
		TestCase testCase = new TestCase()
		use(ReflectionCategory) {
			TestCaseLibraryNode.set field: "id", of: testCase, to: 1L
		}
		testCase.setImportanceAuto true
		testCase.setImportance LOW
		List<TestCase> testCases = Arrays.asList(testCase)

		//the requirement to which the test case is newly linked is critical
		RequirementVersion requirementVersion = Mock()
		requirementVersion.getCriticality()>> CRITICAL

		//called tree contains an unimportant criticality + the newly added
		Set<Long> calleesIds = []
		callTreeFinder.getTestCaseCallTree(1)>> calleesIds
		List<RequirementCriticality> crits = Arrays.asList(UNDEFINED, CRITICAL)
		requirementDao.findDistinctRequirementsCriticalitiesVerifiedByTestCases(calleesIds)>> crits



		//one test case is calling the test case and has an importance low to be updated
		TestCase calling = new TestCase()
		use(ReflectionCategory) {
			TestCaseLibraryNode.set field: "id", of: calling, to: 2L
		}
		calling.setImportanceAuto true
		calling.setImportance LOW
		testCaseDao.findAllCallingTestCases(1, null)>> Arrays.asList(calling)

		//calling call tree contains the test-case's requirements
		Set<Long> calleesIds2 = [1]
		callTreeFinder.getTestCaseCallTree(2)>> calleesIds2
		requirementDao.findDistinctRequirementsCriticalitiesVerifiedByTestCases(calleesIds2)>> crits

		//no test-case is calling the calling one
		testCaseDao.findAllCallingTestCases(2, null)>> new ArrayList(0)
		when:
		service.changeImportanceIfRelationsAddedToReq testCases, requirementVersion

		then:
		testCase.getImportance() == HIGH
		calling.getImportance()	== HIGH
	}
	def"should change importance if relation is added to TestCase"(){
		//the test case
		TestCase testCase = new TestCase()
		use(ReflectionCategory) {
			TestCaseLibraryNode.set field: "id", of: testCase, to: 1L
		}
		testCase.setImportanceAuto true
		testCase.setImportance LOW

		//the requirement Versions
		RequirementVersion rv1 = new RequirementVersion()
		rv1.setCriticality CRITICAL
		RequirementVersion rv2 =  new RequirementVersion()
		rv2.setCriticality MAJOR
		List<RequirementVersion> requirementVersions = Arrays.asList(rv1, rv2)
		// no test case is calling this test case
		testCaseDao.findAllCallingTestCases(1, null)>> new ArrayList(0);

		when:
		service.changeImportanceIfRelationsAddedToTestCase requirementVersions, testCase

		then:
		testCase.getImportance() == HIGH

	}

	def"should change importance if relation removed from requirement"(){
		//the only test case
		TestCase testCase = new TestCase()
		use(ReflectionCategory) {
			TestCaseLibraryNode.set field: "id", of: testCase, to: 1L
		}
		testCase.setImportanceAuto true
		testCase.setImportance HIGH
		List<TestCase> testCasesIds = Arrays.asList (1L)
		testCaseDao.findById(1L)>> testCase

		//the requirement
		RequirementVersion rv = new RequirementVersion()
		rv.setCriticality CRITICAL
		requirementVersionDao.findOne( 3L )>> rv

		//no called test case
		Set<Long> calleesIds2 = [1L]
		callTreeFinder.getTestCaseCallTree(1L)>> new HashSet(0)
		//no calling test case
		testCaseDao.findAllCallingTestCases (1L, null)>> new ArrayList(0)

		//the new associated criticalities list of the test case after removal
		requirementDao.findDistinctRequirementsCriticalitiesVerifiedByTestCases(calleesIds2) >> Arrays.asList(MAJOR, MINOR)

		when:
		service.changeImportanceIfRelationsRemovedFromReq testCasesIds, 3L

		then:
		testCase.getImportance() == MEDIUM

	}
	def"should change importance if relation is removed from Test-Case"(){
		//the test case
		TestCase testCase = new TestCase()
		use(ReflectionCategory) {
			TestCaseLibraryNode.set field: "id", of: testCase, to: 1L
		}
		testCase.setImportanceAuto true
		testCase.setImportance HIGH
		testCaseDao.findById(1L)>> testCase

		//the requirement versions
		List<Long> requirementVersionsIds = [3L, 4L]
		List<RequirementCriticality> crits = Arrays.asList(CRITICAL, MAJOR)
		requirementDao.findDistinctRequirementsCriticalities(requirementVersionsIds)>>crits

		//no called test case
		Set<Long> calleesIds2 = [1L]
		callTreeFinder.getTestCaseCallTree(1L)>> new HashSet(0)
		//no calling test case
		testCaseDao.findAllCallingTestCases (1L, null)>> new ArrayList(0)

		//the new associated criticalities list of the test case after removal
		requirementDao.findDistinctRequirementsCriticalitiesVerifiedByTestCases(calleesIds2) >> Arrays.asList(MAJOR, MINOR)

		when:
		service.changeImportanceIfRelationsRemovedFromTestCase requirementVersionsIds, 1L

		then:
		testCase.getImportance() == MEDIUM
	}
	def"should change importance if criticality updated"(){
		RequirementCriticality oldRequirementCriticality = MINOR
		RequirementVersion rv = new RequirementVersion()
		rv.setCriticality CRITICAL
		requirementVersionDao.findOne (3L )>> rv

		TestCase testCase = new TestCase()
		testCase.setImportance LOW
		testCase.setImportanceAuto true
		use(ReflectionCategory) {
			TestCaseLibraryNode.set field: "id", of: testCase, to: 1L
		}
		testCaseDao.findUnsortedAllByVerifiedRequirementVersion (3L) >> Arrays.asList (testCase)

		//no called test case
		Set<Long> calleesIds2 = [1L]
		callTreeFinder.getTestCaseCallTree(1L)>> new HashSet(0)
		//no calling test case
		testCaseDao.findAllCallingTestCases (1L, null)>> new ArrayList(0)

		//the new associated criticalities list of the test case after change
		requirementDao.findDistinctRequirementsCriticalitiesVerifiedByTestCases(calleesIds2) >> Arrays.asList(CRITICAL)

		when:
		service.changeImportanceIfRequirementCriticalityChanged 3L, oldRequirementCriticality

		then:
		testCase.getImportance() == HIGH
	}
}
