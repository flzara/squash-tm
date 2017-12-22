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

import org.squashtest.tm.tools.unittest.assertions.CollectionAssertions
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.domain.testcase.ActionTestStep
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.service.internal.customfield.PrivateCustomFieldValueService
import org.squashtest.tm.service.internal.library.GenericNodeManagementService
import org.squashtest.tm.service.internal.repository.ParameterDao
import org.squashtest.tm.service.internal.repository.TestCaseDao
import org.squashtest.tm.service.internal.repository.TestStepDao
import org.squashtest.tm.service.internal.testautomation.UnsecuredAutomatedTestManagerService
import org.squashtest.tm.service.testcase.ParameterModificationService
import org.squashtest.tm.service.testutils.MockFactory
import spock.lang.Specification

class CustomTestCaseModificationServiceImplTest extends Specification {
	CustomTestCaseModificationServiceImpl service = new CustomTestCaseModificationServiceImpl()
	TestCaseDao testCaseDao = Mock()
	TestStepDao testStepDao = Mock()
	ParameterDao parameterDao = Mock()
	GenericNodeManagementService testCaseManagementService = Mock()
	TestCaseNodeDeletionHandler deletionHandler = Mock()
	PrivateCustomFieldValueService cufValuesService = Mock()
	ParameterModificationService parameterModificationService = Mock()
	UnsecuredAutomatedTestManagerService taService = Mock()

	MockFactory mockFactory = new MockFactory()

	def setup() {
		CollectionAssertions.declareContainsExactlyIds()
		CollectionAssertions.declareContainsExactly()

		service.testCaseDao = testCaseDao
		service.testStepDao = testStepDao
		service.testCaseManagementService = testCaseManagementService
		service.deletionHandler = deletionHandler
		service.customFieldValuesService = cufValuesService
		service.parameterModificationService = parameterModificationService
		service.taService = taService
	}

	def "should find test case and add a step"() {
		given:
		def testCase = new MockTC(3L, "tc1")
		testCaseDao.findById(10) >> testCase

		and:
		def step = new MockActionStep(4L)

		when:
		service.addActionTestStep(10, step)

		then:
		testCase.steps == [step]
		1 * testStepDao.persist(step)
	}

	def "should find test case and change its step index"() {
		given:
		TestCase testCase = new TestCase()
		testCaseDao.findById(10) >> testCase

		and:
		ActionTestStep step1 = Mock()
		step1.getId() >> 30
		testCase.steps[0] = step1

		ActionTestStep step2 = Mock()
		step2.getId() >> 5
		testCase.steps[1] = step2


		when:
		service.changeTestStepPosition(10, 5, 0)

		then:
		testCase.steps[0] == step2
	}

	def "should find test case and remove one of its steps"() {
		given:
		TestCase testCase = Mock()
		testCaseDao.findById(10) >> testCase

		and:
		ActionTestStep tstep = Mock()
		testStepDao.findById(20) >> tstep

		when:
		service.removeStepFromTestCase(10, 20)

		then:
		1 * deletionHandler.deleteStep(testCase, tstep)
	}


	def "should copy and insert a Test Step a a specific position"(){
		given:
		TestCase testCase = new TestCase()
		ActionTestStep step1 = new ActionTestStep("a","a")
		ActionTestStep step2 = new ActionTestStep("b", "b")

		and :
		Project p = Mock()
		testCase.notifyAssociatedWithProject (mockFactory.mockProject())

		and:
		testCase.addStep step1
		testCase.addStep step2
		testCaseDao.findById(0) >> testCase
		testStepDao.findAllByIds([1]) >> [step2]
		testStepDao.findPositionOfStep(0) >> 1
		testStepDao.findByIdOrderedByIndex(_) >> [step1, step2]


		when:
		service.pasteCopiedTestStep(0, 0, 1)

		then:
		testCase.steps.get(1).action == (step2.action)
		testCase.steps.get(1).expectedResult == step2.expectedResult
	}




	def "should remove test case link to automated script"(){
		given: "an automated test case"
		TestCase testCase = Mock()
		testCaseDao.findById(11L)>> testCase

		when:
		service.removeAutomation(11L)

		then : 1 * testCase.removeAutomatedScript()
	}

	class MockTC extends TestCase{
		Long overId;
		MockTC(Long id){
			overId = id;
			name="don't care"
		}
		MockTC(Long id, String name){
			this(id);
			this.name=name;
		}
		public Long getId(){
			return overId;
		}
		public void setId(Long newId){
			overId=newId;
		}
		public Project getProject(){
			Project project = new Project();
			return project;
		}
	}

	class MockActionStep extends ActionTestStep{
		Long overId;
		MockActionStep(Long id){
			overId = id;
		}
		public Long getId(){
			return overId;
		}
		public void setId(Long newId){
			overId=newId;
		}
	}
}
