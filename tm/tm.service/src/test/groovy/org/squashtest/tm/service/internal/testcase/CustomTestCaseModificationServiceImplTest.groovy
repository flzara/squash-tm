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

import org.springframework.context.ApplicationEventPublisher
import org.squashtest.tm.core.foundation.collection.Paging
import org.squashtest.tm.domain.campaign.IterationTestPlanItem
import org.squashtest.tm.domain.customfield.CustomField
import org.squashtest.tm.domain.customfield.CustomFieldBinding
import org.squashtest.tm.domain.customfield.CustomFieldValue
import org.squashtest.tm.domain.customfield.RawValue
import org.squashtest.tm.domain.infolist.InfoListItem
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.domain.testautomation.TestAutomationProject
import org.squashtest.tm.domain.testcase.ActionTestStep
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.domain.testcase.TestCaseImportance
import org.squashtest.tm.domain.testcase.TestStep
import org.squashtest.tm.exception.InconsistentInfoListItemException
import org.squashtest.tm.service.advancedsearch.IndexationService
import org.squashtest.tm.service.attachment.AttachmentManagerService
import org.squashtest.tm.service.campaign.IterationTestPlanFinder
import org.squashtest.tm.service.infolist.InfoListItemFinderService
import org.squashtest.tm.service.internal.customfield.PrivateCustomFieldValueService
import org.squashtest.tm.service.internal.library.GenericNodeManagementService
import org.squashtest.tm.service.internal.repository.ActionTestStepDao
import org.squashtest.tm.service.internal.repository.ParameterDao
import org.squashtest.tm.service.internal.repository.TestCaseDao
import org.squashtest.tm.service.internal.repository.TestStepDao
import org.squashtest.tm.service.internal.testautomation.UnsecuredAutomatedTestManagerService
import org.squashtest.tm.service.internal.testcase.event.TestCaseNameChangeEvent
import org.squashtest.tm.service.internal.testcase.event.TestCaseReferenceChangeEvent
import org.squashtest.tm.service.testcase.ParameterModificationService
import org.squashtest.tm.service.testutils.MockFactory
import org.squashtest.tm.tools.unittest.assertions.CollectionAssertions
import spock.lang.Specification
import spock.lang.Unroll

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
	AttachmentManagerService attachmentManagerService = Mock()
	IterationTestPlanFinder iterationTestPlanFinder = Mock()
	IndexationService indexationService = Mock()
	ActionTestStepDao actionStepDao = Mock()
	InfoListItemFinderService infoListItemService = Mock()
	ApplicationEventPublisher eventPublisher = Mock()

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
		service.attachmentManagerService = attachmentManagerService
		service.iterationTestPlanFinder = iterationTestPlanFinder
		service.indexationService = indexationService
		service.actionStepDao = actionStepDao
		service.infoListItemService = infoListItemService
		service.eventPublisher = eventPublisher
	}

	def "should find test case and add a step at last position"() {
		given:
		def testCase = new MockTC(3L, "tc1")
		testCaseDao.findById(10) >> testCase

		and:
		def firstStep = new MockActionStep(1L)
		testCase.addStep(firstStep)

		and:
		def newStep = new MockActionStep(4L)

		when:
		service.addActionTestStep(10, newStep)

		then:
		testCase.steps == [firstStep, newStep]
		1 * testStepDao.persist(newStep)
	}


	def "should find test case and add a step at first position"(){
		given:
		def testCase = new MockTC(3L, "tc1")
		testCaseDao.findById(10) >> testCase

		and:
		def firstStep = new MockActionStep(1L)
		testCase.addStep(firstStep)

		and:
		def newStep = new MockActionStep(4L)

		when:
		service.addActionTestStep(10, newStep, 0)

		then:
		testCase.steps == [newStep, firstStep]
		1 * testStepDao.persist(newStep)
	}




	@Unroll("should add an action step with initial custom field values at #msgposition")
	def "should add an action step with initial custom field values at given position"(){

		given: "a test case with steps"

		def testCase = new MockTC(3L, "tc1")
		testCaseDao.findById(10) >> testCase
		testCase.addStep(initialStep)

		and: "the custom field values for the new step"
		def cufValues = [
			(100L) : new RawValue("value 100"),
			(200L) : new RawValue("value 200")
		]

		and: "other services"
		def cufValue1 = mockCuf(100L, "empty 100", newStep)
		def cufValue2 = mockCuf(200L, "empty 200", newStep)
		cufValuesService.findAllCustomFieldValues(newStep) >> [ cufValue1, cufValue2 ]


		when :
		action(service, 10L, newStep, cufValues)

		then:
		testCase.steps == resultsteps

		cufValue1.value == "value 100"
		cufValue2.value == "value 200"

		where:
		initialStep << [new MockActionStep(1L),new MockActionStep(1L)]
		newStep << [new MockActionStep(4L), new MockActionStep(4L)]
		msgposition << ["last position", "first position"]
		action << [
			{tcservice, id, step, cufs -> tcservice.addActionTestStep(id, step, cufs)},	// defaults to last position
			{tcservice, id, step, cufs -> tcservice.addActionTestStep(id, step, cufs, 0)}, 	// first position
		]
		resultsteps << [
			[initialStep, newStep],
			[newStep, initialStep]
		]

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


	def "should decompose an automated test path into a project id and a test path"(){

		given:
		def autoprojBad = Mock(TestAutomationProject){getLabel() >> "not me"; getId() >> -50L}
		def autoprojGood = Mock(TestAutomationProject){getLabel() >> "me ! me !"; getId() >> 50L}

		def tc = Mock(TestCase) {
			getProject() >> Mock(Project) {
				getTestAutomationProjects() >> [autoprojBad, autoprojGood]
			}
		}

		and:
		testCaseDao.findById(_) >> tc

		when:
		def res = service.extractAutomatedProjectAndTestName(10L, "/me ! me !/test/path.ta")

		then:
		res.a1 == autoprojGood.getId()
		res.a2 == 'test/path.ta'


	}

	def "should remove test case link to automated script"(){
		given: "an automated test case"
		TestCase testCase = Mock()
		testCaseDao.findById(11L)>> testCase

		when:
		service.removeAutomation(11L)

		then : 1 * testCase.removeAutomatedScript()
	}


	def "should rename a test case"(){

		given :
		def tc = Mock(TestCase){
			getId() >> 10L
		}

		when :
		service.rename(10L, "Bob")

		then :
		// interactions
		1 * testCaseManagementService.renameNode(10L, "Bob")

		1 * testCaseDao.findById(10L) >> tc
		1 * iterationTestPlanFinder.findByReferencedTestCase(tc) >> [
			Mock(IterationTestPlanItem){
				getId() >> 1L
			},
			Mock(IterationTestPlanItem){
				getId() >> 2L
			}
		]
		1 *indexationService.batchReindexItpi([1L, 2L])
		1 * indexationService.batchReindexTc([10L])
		1* eventPublisher.publishEvent({
			it instanceof TestCaseNameChangeEvent && it.testCaseId == 10L && it.newName == "Bob"
		})
	}


	def "should change the reference of a test case"(){

		given :
		def tc = Mock(TestCase){
			getId() >> 10L
		}

		when :
		service.changeReference(10L, "reref")

		then :
		1 * tc.setReference("reref")

		1 * testCaseDao.findById(10L) >> tc
		1 * iterationTestPlanFinder.findByReferencedTestCase(tc) >> [
			Mock(IterationTestPlanItem){
				getId() >> 1L
			},
			Mock(IterationTestPlanItem){
				getId() >> 2L
			}
		]
		1 *indexationService.batchReindexItpi([1L, 2L])
		1 * indexationService.batchReindexTc([10L])
		1 * eventPublisher.publishEvent({
			it instanceof TestCaseReferenceChangeEvent && it.testCaseId == 10L && it.newReference == "reref"
		})
	}

	def "should change the importance of a test case"(){

		given :
		def tc = Mock(TestCase){
			getId() >> 10L
		}

		when :
		service.changeImportance(10L, TestCaseImportance.HIGH)

		then :
		1 * tc.setImportance(TestCaseImportance.HIGH)

		1 * testCaseDao.findById(10L) >> tc
		1 * iterationTestPlanFinder.findByReferencedTestCase(tc) >> [
			Mock(IterationTestPlanItem){
				getId() >> 1L
			},
			Mock(IterationTestPlanItem){
				getId() >> 2L
			}
		]
		1 *indexationService.batchReindexItpi([1L, 2L])
		1 * indexationService.batchReindexTc([10L])

	}


	def "should change the nature of a test case"(){

		given:
		def tc = Mock(TestCase){
			getProject() >> {
				Mock(Project){
					getId() >> 2L
				}
			}
		}
		def nature = Mock(InfoListItem)

		and:
		testCaseDao.findById(_) >> tc
		infoListItemService.findByCode(_) >> nature
		infoListItemService.isNatureConsistent(_,_) >> true

		when:
		service.changeNature(10L, "NAT_BOB")

		then:
		1 * tc.setNature(nature)

	}

	def "should not change the nature of a test case"(){

		given:
		def tc = Mock(TestCase){
			getProject() >> {
				Mock(Project){
					getId() >> 2L
				}
			}
		}
		def nature = Mock(InfoListItem)

		and:
		testCaseDao.findById(_) >> tc
		infoListItemService.findByCode(_) >> nature
		infoListItemService.isNatureConsistent(_,_) >> false

		when:
		service.changeNature(10L, "NAT_BOB")

		then:
		thrown InconsistentInfoListItemException
	}

	def "should change the type of a test case"(){

		given:
		def tc = Mock(TestCase){
			getProject() >> {
				Mock(Project){
					getId() >> 2L
				}
			}
		}
		def type = Mock(InfoListItem)

		and:
		testCaseDao.findById(_) >> tc
		infoListItemService.findByCode(_) >> type
		infoListItemService.isTypeConsistent(_,_) >> true

		when:
		service.changeType(10L, "TYP_MIKE")

		then:
		1 * tc.setType(type)

	}

	def "should not change the type of a test case"(){

		given:
		def tc = Mock(TestCase){
			getProject() >> {
				Mock(Project){
					getId() >> 2L
				}
			}
		}
		def type = Mock(InfoListItem)

		and:
		testCaseDao.findById(_) >> tc
		infoListItemService.findByCode(_) >> type
		infoListItemService.isTypeConsistent(_,_) >> false

		when:
		service.changeType(10L, "TYP_MIKE")

		then:
		thrown InconsistentInfoListItemException
	}


	def "should retrieve the steps of a test case"(){
		given:
		def steps = [Mock(ActionTestStep), Mock(ActionTestStep)]

		when:
		def res = service.findStepsByTestCaseId(10L)

		then:
		1 * testCaseDao.findTestSteps(10L) >> steps
		res == steps
	}


	def "should update the action and the expected result of a test step"(){
		given:
		def step = Mock(ActionTestStep)

		when:
		service.updateTestStepAction(10L, "Bob")
		service.updateTestStepExpectedResult(10L, "Mike")

		then:
		2 * actionStepDao.findById(10L) >> step
		1 * step.setAction("Bob")
		1 * step.setExpectedResult("Mike")
		2 * parameterModificationService.createParamsForStep(10L)

	}

	def "should move a step up in the step list"(){
		given:
		def tc = Mock(TestCase)
		def step1 = Mock(TestStep)
		def step2 = Mock(TestStep)

		and:
		testCaseDao.findById(10L) >> tc
		testStepDao.findListById([1L, 2L]) >> [step1, step2]

		when:
		service.changeTestStepsPosition(10L, 5, [1L, 2L])

		then:
		1 * tc.moveSteps(5, [step1, step2])

	}

	def "should remove a step from a test case, by index"(){
		given:
		def removedStep = Mock(TestStep)
		def tc = Mock(TestCase){
			getSteps() >> [Mock(TestStep), Mock(TestStep), removedStep, Mock(TestStep)]
		}
		and:
		testCaseDao.findById(10L) >> tc

		when:
		service.removeStepFromTestCaseByIndex(10L, 2)

		then:
		deletionHandler.deleteStep(tc, removedStep)

	}

	// more cheap code coverage upgrade !
	def "should find a Hibernate Initialized test case"(){
		when:
		service.findTestCaseWithSteps(10L)

		then:
		1 * testCaseDao.findAndInit(10L)
	}

	def "should remove a bunch of steps"(){

		given:
		def tc = Mock(TestCase)
		def step1 = Mock(TestStep)
		def step2 = Mock(TestStep)

		and:
		testCaseDao.findById(10L) >> tc
		testStepDao.findById(_) >>> [step1, step2]

		when:
		service.removeListOfSteps(10L, [1L, 2L])

		then:
		1 * deletionHandler.deleteStep(tc, step1)
		1 * deletionHandler.deleteStep(tc, step2)
	}


	def "should return a page list of steps"(){
		given:
		def paging = Mock(Paging)
		def steps = [Mock(TestStep), Mock(TestStep), Mock(TestStep), Mock(TestStep)]

		and:
		testCaseDao.findAllStepsByIdFiltered(10L, paging) >> steps.subList(0,2)
		testCaseDao.findTestSteps(10L) >> steps

		when:
		def res = service.findStepsByTestCaseIdFiltered(10L, paging)

		then:
		res.paging == paging
		res.totalNumberOfItems == 4
		res.items == steps.subList(0,2)

	}


	// ****************** test utilities *****************

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

	def mockCuf(cufId, initialValue, owner){
		return new CustomFieldValue(owner.getId(), owner.getBoundEntityType(),
			new CustomFieldBinding(customField :
				Mock(CustomField){
					getId() >> cufId
				},
				boundEntity : owner.getBoundEntityType()
			)
			,
			initialValue)
	}
}
