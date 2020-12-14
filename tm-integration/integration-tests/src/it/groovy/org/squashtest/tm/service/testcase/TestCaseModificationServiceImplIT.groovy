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
package org.squashtest.tm.service.testcase

import org.junit.runner.RunWith
import org.spockframework.runtime.Sputnik
import org.springframework.transaction.annotation.Transactional
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.squashtest.it.stub.security.UserContextHelper
import org.squashtest.tm.domain.bdd.ActionWord
import org.squashtest.tm.domain.bdd.ActionWordParameter
import org.squashtest.tm.domain.bdd.ActionWordParameterValue
import org.squashtest.tm.domain.bdd.ActionWordText
import org.squashtest.tm.domain.bdd.Keyword
import org.squashtest.tm.domain.project.GenericProject
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.domain.testcase.ActionTestStep
import org.squashtest.tm.domain.testcase.KeywordTestStep
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.domain.testcase.TestCaseFolder
import org.squashtest.tm.exception.DuplicateNameException
import org.squashtest.tm.service.project.GenericProjectManagerService
import org.unitils.dbunit.annotation.DataSet
import spock.unitils.UnitilsSupport

import javax.inject.Inject

import static org.squashtest.tm.domain.bdd.Keyword.THEN

@UnitilsSupport
@RunWith(Sputnik)
@Transactional
class TestCaseModificationServiceImplIT extends DbunitServiceSpecification {

	@Inject
	private TestCaseModificationService service

	@Inject
	private TestCaseLibraryNavigationService navService

	@Inject
	GenericProjectManagerService projectService

	@Inject
	ParameterModificationService parameterModificationService

	@Inject
	DatasetModificationService datasetModificationService

	private int testCaseId = -1
	private int folderId = -1

	def setup() {
		UserContextHelper.setUsername("creator")
		projectService.persist(createProject())

		def libList = session.createQuery("from TestCaseLibrary").list()

		def lib = libList.get(libList.size() - 1)

		def folder = new TestCaseFolder(name: "folder")
		def testCase = new TestCase(name: "test case 1", description: "the first test case")

		navService.addFolderToLibrary(lib.id, folder)
		navService.addTestCaseToFolder(folder.id, testCase, null)

		folderId = folder.id
		testCaseId = testCase.id
	}

	static boolean isH2() {
		return System.properties['jooq.sql.dialect'] == null || System.properties['jooq.sql.dialect'] == 'H2'
	}


	def "should add a test step to test case"() {
		given:
		ActionTestStep step = new ActionTestStep(action: "action", expectedResult: "result")

		when:
		def teststep = service.addActionTestStep(testCaseId, step);

		then:
		teststep != null
		teststep.id != null
		teststep.action == step.action
		teststep.expectedResult == step.expectedResult
	}

	def "should add an empty test step to test case"() {
		given:
		ActionTestStep step = new ActionTestStep(action: "", expectedResult: "")

		when:
		def teststep = service.addActionTestStep(testCaseId, step);

		then:
		teststep != null
		teststep.id != null
		teststep.action == step.action
		teststep.expectedResult == step.expectedResult
	}

	def "should get a test step list from a test case"() {
		given:

		ActionTestStep step1 = new ActionTestStep(action: "first step", expectedResult: "should work")
		and:
		ActionTestStep step2 = new ActionTestStep(action: "second step", expectedResult: "should work too")

		when:

		service.addActionTestStep(testCaseId, step1);
		service.addActionTestStep(testCaseId, step2);

		def list = service.findStepsByTestCaseId(testCaseId);


		then:

		list != null
		list.size() == 2
		list[0].action == step1.action
		list[1].action == step2.action
	}


	def "should rename a lone test case"() {
		given:
		def newName = "new name"

		when:
		service.rename(testCaseId, newName);
		def testcase = service.findById(testCaseId)

		then:
		testcase != null
		testcase.id == testCaseId
		testcase.name == "new name"
		testcase.description == "the first test case"
	}

	def "should rename a test case if another library node have a different name"() {
		given:
		def tc2name = "test case 2"
		def tc2desc = "should rename"
		def newName = "new name"

		def newtc = new TestCase(name: tc2name, description: tc2desc)

		when:
		navService.addTestCaseToFolder(folderId, newtc, null)
		service.rename(newtc.id, newName)
		def renewtc = service.findById(newtc.id)

		then:
		renewtc != null
		renewtc.id == newtc.id
		renewtc.name == "new name"
		renewtc.description == "should rename"
	}

	def "should not rename a test case if another library node have the same name"() {
		given:
		def tc2name = "test case 2"
		def tc2desc = "should fail"
		def newName = "test case 1"

		def newtc = new TestCase(name: tc2name, description: tc2desc)

		when:
		navService.addTestCaseToFolder(folderId, newtc, null)
		service.rename(newtc.id, newName)

		then:
		thrown(DuplicateNameException)
	}


	def "should change a test case description"() {
		given:
		def tcNewDesc = "the new desc"

		when:
		service.changeDescription(testCaseId, tcNewDesc)
		def tc = service.findById(testCaseId)

		then:
		tc.description == tcNewDesc
	}

	def "should change a test case reference"() {
		given:
		def tcNewRef = "the new ref"

		when:
		service.changeReference(testCaseId, tcNewRef)
		def tc = service.findById(testCaseId)

		then:
		tc.reference == tcNewRef
	}

	def "should change a test case source code repository URL"() {
		given:
		def tcNewSourceCodeRepositoryUrl ="http://test"

		when:
		service.changeSourceCodeRepositoryUrl(testCaseId, tcNewSourceCodeRepositoryUrl)
		def tc = service.findById(testCaseId)

		then:
		tc.sourceCodeRepositoryUrl == tcNewSourceCodeRepositoryUrl
	}

	def "should change a test case automated test reference"() {
		given:
		def tcNewRef = "the new ref"

		when:
		service.changeAutomatedTestReference(testCaseId, tcNewRef)
		def tc = service.findById(testCaseId)

		then:
		tc.automatedTestReference == tcNewRef
	}

	@DataSet("TestCaseModificationServiceImplIT.should change a test case automated test technology.xml")
	def "should change a test case automated test technology"() {
		given:
		def newTechId = -2L
		when:
		service.changeAutomatedTestTechnology(testCaseId, newTechId)

		def tc = service.findById(testCaseId)

		then:
		tc.automatedTestTechnology.id == newTechId
		tc.automatedTestTechnology.name == "Cypress"
	}

	@DataSet("TestCaseModificationServiceImplIT.should update a test step action.xml")
	def "should update a test step action "() {
		given:
		def stepId = -2L

		and:
		def newAction = "manmana"

		when:
		service.updateTestStepAction(stepId, newAction)

		then:
		ActionTestStep step = findEntity(ActionTestStep.class, -2L);
		step.action == newAction;
	}


	def "should update a test step expected result"() {
		given:
		ActionTestStep step = new ActionTestStep(action: "first step", expectedResult: "should work")

		def newres = "confirm"

		when:
		def tstep = service.addActionTestStep(testCaseId, step)
		service.updateTestStepExpectedResult(tstep.id, newres)


		def listSteps = service.findStepsByTestCaseId(testCaseId)
		tstep = listSteps.get(0);

		then:
		tstep.expectedResult == newres
	}

	def "should move step 2 to position #3 in a list of 3 test steps"() {
		given:
		def step1 = new ActionTestStep("first step", "first result")
		def step2 = new ActionTestStep("second step", "second result")
		def step3 = new ActionTestStep("third step", "third result")

		when:
		service.addActionTestStep(testCaseId, step1)
		def tstep2 = service.addActionTestStep(testCaseId, step2)
		def tstep3 = service.addActionTestStep(testCaseId, step3)

		service.changeTestStepPosition(testCaseId, tstep2.id, 2)

		def list = service.findStepsByTestCaseId(testCaseId)


		then:
		list[1].id == tstep3.id
		list[2].id == tstep2.id
	}

	def "should move step 3 to position #2 in a list of 3 test steps"() {
		given:
		def step1 = new ActionTestStep("first step", "first result")
		def step2 = new ActionTestStep("second step", "second result")
		def step3 = new ActionTestStep("third step", "third result")

		when:
		service.addActionTestStep(testCaseId, step1)
		def tstep2 = service.addActionTestStep(testCaseId, step2)
		def tstep3 = service.addActionTestStep(testCaseId, step3)

		service.changeTestStepPosition(testCaseId, tstep3.id, 1)

		def list = service.findStepsByTestCaseId(testCaseId)


		then:
		list[1].id == tstep3.id
		list[2].id == tstep2.id
	}

	def "should move a couple of steps to position #2"() {
		given:
		def step1 = new ActionTestStep("first step", "first result")
		def step2 = new ActionTestStep("second step", "second result")
		def step3 = new ActionTestStep("third step", "third result")
		def step4 = new ActionTestStep("fourth step", "fourth result")
		def step5 = new ActionTestStep("fifth step", "fifth result")
		def step6 = new ActionTestStep("sixth step", "sixth result")

		and:
		service.addActionTestStep(testCaseId, step1)
		service.addActionTestStep(testCaseId, step2)
		service.addActionTestStep(testCaseId, step3)
		service.addActionTestStep(testCaseId, step4)
		service.addActionTestStep(testCaseId, step5)
		service.addActionTestStep(testCaseId, step6)
		when:


		service.changeTestStepsPosition(testCaseId, 1, [step4, step5].collect { it.id })
		def reTc = service.findTestCaseWithSteps(testCaseId)

		then:
		reTc.getSteps().collect { it.id } == [step1, step4, step5, step2, step3, step6].collect { it.id }


	}

	def "should move the three first steps at last position"() {
		given:
		def step1 = new ActionTestStep("first step", "first result")
		def step2 = new ActionTestStep("second step", "second result")
		def step3 = new ActionTestStep("third step", "third result")
		def step4 = new ActionTestStep("fourth step", "fourth result")
		def step5 = new ActionTestStep("fifth step", "fifth result")
		def step6 = new ActionTestStep("sixth step", "sixth result")

		and:
		service.addActionTestStep(testCaseId, step1)
		service.addActionTestStep(testCaseId, step2)
		service.addActionTestStep(testCaseId, step3)
		service.addActionTestStep(testCaseId, step4)
		service.addActionTestStep(testCaseId, step5)
		service.addActionTestStep(testCaseId, step6)

		when:
		service.changeTestStepsPosition(testCaseId, 3, [step1, step2, step3].collect { it.id })
		def reTc = service.findTestCaseWithSteps(testCaseId)

		then:
		reTc.getSteps().collect { it.id } == [step4, step5, step6, step1, step2, step3].collect { it.id }
	}


	def "should remove step 2 in a list of three steps"() {
		given:
		def step1 = new ActionTestStep("first step", "first result")
		def step2 = new ActionTestStep("second step", "second result")
		def step3 = new ActionTestStep("third step", "third result")

		when:
		def tstep1 = service.addActionTestStep(testCaseId, step1)
		def tstep2 = service.addActionTestStep(testCaseId, step2)
		def tstep3 = service.addActionTestStep(testCaseId, step3)

		service.removeStepFromTestCase(testCaseId, tstep2.id)

		def list = service.findStepsByTestCaseId(testCaseId)

		then:
		list.size() == 2
		list[0].id == tstep1.id
		list[1].id == tstep3.id
	}


	def "should initialize a test case with his test steps"() {
		given:
		def tc = new TestCase(name: "rich-tc")
		def ts1 = new ActionTestStep(action: "action1", expectedResult: "ex1")
		def ts2 = new ActionTestStep(action: "action2", expectedResult: "ex2")


		navService.addTestCaseToFolder(folderId, tc, null)
		service.addActionTestStep tc.id, ts1
		service.addActionTestStep tc.id, ts2


		when:

		def obj = service.findTestCaseWithSteps(tc.id)


		then:
		obj.steps.size() == 2
		obj.steps*.action.containsAll(["action1", "action2"])
	}


	def "should remove a list of steps"() {
		given:
		def tc = new TestCase(name: "stepdeletion-tc")
		def ts1 = new ActionTestStep(action: "action1", expectedResult: "ex1")
		def ts2 = new ActionTestStep(action: "action2", expectedResult: "ex2")
		def ts3 = new ActionTestStep(action: "action3", expectedResult: "ex3")
		def ts4 = new ActionTestStep(action: "action4", expectedResult: "ex4")
		def ts5 = new ActionTestStep(action: "action5", expectedResult: "ex5")


		navService.addTestCaseToFolder(folderId, tc, null)
		service.addActionTestStep tc.id, ts1
		service.addActionTestStep tc.id, ts2
		service.addActionTestStep tc.id, ts3
		service.addActionTestStep tc.id, ts4
		service.addActionTestStep tc.id, ts5


		when:

		def toRemove = [ts1.id, ts3.id, ts5.id]

		service.removeListOfSteps(tc.id, toRemove);

		def obj = service.findStepsByTestCaseId(tc.id)


		then:
		obj.size() == 2
		obj*.action.containsAll(["action2", "action4"])
	}

	@DataSet("TestCaseModificationServiceImplIT.should remove some keyword steps.xml")
	def "should remove a list of keyword steps"() {
		given:
		def toRemove = [-1L, -2L]

		when:
		service.removeListOfSteps(-1L, toRemove);
		def obj = service.findStepsByTestCaseId(-1L)

		then:
		obj.size() == 1
		KeywordTestStep keywordTestStep = obj[0];
		keywordTestStep.getId() == -3L
		keywordTestStep.getKeyword() == THEN
		keywordTestStep.getActionWord().createWord() == "GoodBye!"
	}

	@DataSet("TestCaseModificationServiceImplIT.should remove automated script link.xml")
	def "should remove automated script link"() {
		given:
		def testCaseId = -11L

		when:
		service.removeAutomation(testCaseId)

		then:
		TestCase testCase = findEntity(TestCase.class, testCaseId)
		testCase.getAutomatedTest() == null
	}

	def GenericProject createProject() {
		Project p = new Project();
		p.name = Double.valueOf(Math.random()).toString();
		p.description = "eaerazer"
		return p
	}

	@DataSet("TestCaseModificationServiceImplIT.should find test cases.xml")
	def "should find all test cases by ids"() {
		when:
		def res = service.findAllByIds([-10L, -30L])

		then:
		res*.getId().containsAll([-10L, -30L])
		res.size() == 2
	}

	@DataSet("TestCaseModificationServiceImplIT.should find test cases.xml")
	def "should find all test cases by ancestor ids"() {
		when:
		def res = service.findAllByAncestorIds(ancestors)

		then:
		res*.getId().containsAll(expected)
		res.size() == expected.size()

		where:
		ancestors    | expected
		[-10L]       | [-10L]
		[-50L]       | [-30L]
		[-40L]       | [-30L, -20L]
		[-20L, -40L] | [-20L, -30L]
	}

	def "should paste a bunch of steps to end of list"() {
		given:
		def step1 = new ActionTestStep("first step", "first result")
		def step2 = new ActionTestStep("second step", "second result")
		def step3 = new ActionTestStep("third step", "third result")
		def step4 = new ActionTestStep("fourth step", "fourth result")
		def step5 = new ActionTestStep("fifth step", "fifth result")
		def step6 = new ActionTestStep("sixth step", "sixth result")
		def steps = [step1, step3, step2, step4, step6, step5]

		and:
		service.addActionTestStep(testCaseId, step1)
		service.addActionTestStep(testCaseId, step3)
		service.addActionTestStep(testCaseId, step2)
		service.addActionTestStep(testCaseId, step4)
		service.addActionTestStep(testCaseId, step6)
		service.addActionTestStep(testCaseId, step5)
		session.flush();
		session.clear();

		when:


		service.pasteCopiedTestStepToLastIndex(testCaseId, steps.collect { it.id })
		session.flush()
		session.clear()
		TestCase result = findEntity(TestCase.class, testCaseId)

		then:
		result.getSteps().size() == 12;
		def expectedActionList = steps.collect({ it.getAction() })
		expectedActionList.addAll(expectedActionList)
		def stepsResults = result.getSteps().collect({ it.getAction() })
		stepsResults.asList() == expectedActionList.asList()

	}

	def "should paste a bunch of steps to position 3"() {
		given:
		def step1 = new ActionTestStep("first step", "first result")
		def step2 = new ActionTestStep("second step", "second result")
		def step3 = new ActionTestStep("third step", "third result")
		def step4 = new ActionTestStep("fourth step", "fourth result")
		def step5 = new ActionTestStep("fifth step", "fifth result")
		def step6 = new ActionTestStep("sixth step", "sixth result")


		and:
		service.addActionTestStep(testCaseId, step1)
		service.addActionTestStep(testCaseId, step3)
		service.addActionTestStep(testCaseId, step2)
		service.addActionTestStep(testCaseId, step4)
		service.addActionTestStep(testCaseId, step6)
		service.addActionTestStep(testCaseId, step5)


		def steps = [step1, step3, step2, step4, step6, step5]

		when:
		service.pasteCopiedTestSteps(testCaseId, step2.getId(), steps.collect { it.getId() })
		TestCase result = findEntity(TestCase.class, testCaseId)

		then:
		result.getSteps().size() == 12;
		def expectedActionList = [step1, step3, step2, step1, step3, step2, step4, step6, step5, step4, step6, step5].collect {
			it.getAction()
		}
		def stepsResults = result.getSteps().collect({ it.getAction() })
		stepsResults == expectedActionList

	}

	@DataSet("TestCaseModificationServiceImplIT.keyword test cases.xml")
	def "should add a keyword test step with a new action word to test case"() {
		when:
		KeywordTestStep createdKeywordTestStep = service.addKeywordTestStep(-4L, "AND", "  hello    ")

		then:
		createdKeywordTestStep != null
		createdKeywordTestStep.id != null

		Keyword.AND == createdKeywordTestStep.keyword

		ActionWord actionWord = createdKeywordTestStep.actionWord
		actionWord.id != null
		actionWord.createWord() == "hello"
		actionWord.token == "T-hello-"
		!actionWord.getKeywordTestSteps().isEmpty()

		def fragments = actionWord.getFragments()
		fragments.size() == 1
		def f1 = fragments.get(0)
		f1.class.is(ActionWordText)
		def text1 = (ActionWordText) f1
		text1.getText() == "hello"
		text1.id != null
		text1.actionWord == actionWord
	}

	@DataSet("TestCaseModificationServiceImplIT.keyword test cases.xml")
	def "should add a keyword test step with a new action word to test case at a specific index"() {
		when:
		KeywordTestStep createdKeywordTestStep = service.addKeywordTestStep(-4L, "AND", "  hello    ",1)

		then:
		createdKeywordTestStep != null
		createdKeywordTestStep.id != null

		Keyword.AND == createdKeywordTestStep.keyword

		ActionWord actionWord = createdKeywordTestStep.actionWord
		actionWord.id != null
		actionWord.createWord() == "hello"
		actionWord.token == "T-hello-"
		!actionWord.getKeywordTestSteps().isEmpty()

		def fragments = actionWord.getFragments()
		fragments.size() == 1
		def f1 = fragments.get(0)
		f1.class.is(ActionWordText)
		def text1 = (ActionWordText) f1
		text1.getText() == "hello"
		text1.id != null
		text1.actionWord == actionWord

		and:
		def testCase = createdKeywordTestStep.getTestCase()
		def testCaseSteps = testCase.getSteps()
		testCaseSteps.size() == 3
		testCaseSteps[1].keyword == Keyword.AND
		testCaseSteps[1].actionWord == actionWord

	}

	@DataSet("TestCaseModificationServiceImplIT.keyword test cases.xml")
	def "should add a keyword test step with an action word id to test case at a specific index"() {
		when:
		KeywordTestStep createdKeywordTestStep = service.addKeywordTestStep(-4L, "AND", "  hello    ", -33L, 1)

		then:
		createdKeywordTestStep != null
		createdKeywordTestStep.id != null

		Keyword.AND == createdKeywordTestStep.keyword

		ActionWord actionWord = createdKeywordTestStep.actionWord
		actionWord.id == -33
		actionWord.createWord() == "hello"
		actionWord.token == "T-hello-"
		!actionWord.getKeywordTestSteps().isEmpty()

		def fragments = actionWord.getFragments()
		fragments.size() == 1
		def f1 = fragments.get(0)
		f1.class.is(ActionWordText)
		def text1 = (ActionWordText) f1
		text1.getText() == "hello"
		text1.id != null
		text1.actionWord == actionWord

		and:
		def testCase = createdKeywordTestStep.getTestCase()
		def testCaseSteps = testCase.getSteps()
		testCaseSteps.size() == 3
		testCaseSteps[1].keyword == Keyword.AND
		testCaseSteps[1].actionWord == actionWord

	}

	@DataSet("TestCaseModificationServiceImplIT.keyword test cases.xml")
	def "should add a keyword test step with a new action word containing parameters to test case"() {
		when:
		KeywordTestStep createdKeywordTestStep = service.addKeywordTestStep(-4L, "AND", "  today is  \"Friday\"\"\".   ")

		then:
		createdKeywordTestStep != null
		createdKeywordTestStep.id != null

		Keyword.AND == createdKeywordTestStep.keyword

		ActionWord actionWord = createdKeywordTestStep.actionWord
		actionWord.id != null
		actionWord.createWord() == "today is \"param1\"\"param2\"."
		actionWord.token == "TPPT-today is -.-"
		def fragments = actionWord.getFragments()
		fragments.size() == 4

		def f1 = fragments.get(0)
		f1.class.is(ActionWordText)
		def text1 = (ActionWordText) f1
		text1.getText() == "today is "
		text1.id != null
		text1.actionWord == actionWord

		def f2 = fragments.get(1)
		f2.class.is(ActionWordParameter)
		def param1 = (ActionWordParameter) f2
		param1.name == "param1"
		param1.id != null
		param1.defaultValue == "Friday"
		param1.actionWord == actionWord

		def f3 = fragments.get(2)
		f3.class.is(ActionWordParameter)
		def param2 = (ActionWordParameter) f3
		param2.name == "param2"
		param2.id != null
		param2.defaultValue == ""
		param2.actionWord == actionWord

		def f4 = fragments.get(3)
		f4.class.is(ActionWordText)
		def text2 = (ActionWordText) f4
		text2.getText() == "."
		text2.id != null
		text2.actionWord == actionWord

		def paramValues = createdKeywordTestStep.paramValues
		paramValues.size() == 2
		ActionWordParameterValue value1 = paramValues.get(0)
		value1.id != null
		value1.value == "Friday"
		value1.actionWordParam == param1
		value1.keywordTestStep == createdKeywordTestStep

		ActionWordParameterValue value2 = paramValues.get(1)
		value2.id != null
		value2.value == "\"\""
		value2.actionWordParam == param2
		value2.keywordTestStep == createdKeywordTestStep
	}

	@DataSet("TestCaseModificationServiceImplIT.keyword test cases.xml")
	def "should add a keyword test step with a new action word containing parameters to test case in which some are in <>"() {
		when:
		KeywordTestStep createdKeywordTestStep = service.addKeywordTestStep(-4L, "AND", "  today is  <date> of < Year >  .   ")

		then:
		createdKeywordTestStep != null
		createdKeywordTestStep.id != null

		Keyword.AND == createdKeywordTestStep.keyword

		ActionWord actionWord = createdKeywordTestStep.actionWord
		actionWord.id != null
		actionWord.createWord() == "today is \"param1\" of \"param2\" ."
		actionWord.token == "TPTPT-today is - of - .-"
		def fragments = actionWord.getFragments()
		fragments.size() == 5

		def f1 = fragments.get(0)
		f1.class.is(ActionWordText)
		def text1 = (ActionWordText) f1
		text1.getText() == "today is "
		text1.id != null
		text1.actionWord == actionWord

		def f2 = fragments.get(1)
		f2.class.is(ActionWordParameter)
		def param1 = (ActionWordParameter) f2
		param1.name == "param1"
		param1.id != null
		param1.defaultValue == ""
		param1.actionWord == actionWord

		def f3 = fragments.get(2)
		f3.class.is(ActionWordText)
		def text2 = (ActionWordText) f3
		text2.getText() == " of "
		text2.id != null
		text2.actionWord == actionWord

		def f4 = fragments.get(3)
		f4.class.is(ActionWordParameter)
		def param2 = (ActionWordParameter) f4
		param2.name == "param2"
		param2.id != null
		param2.defaultValue == ""
		param2.actionWord == actionWord

		def f5 = fragments.get(4)
		f5.class.is(ActionWordText)
		def text3 = (ActionWordText) f5
		text3.getText() == " ."
		text3.id != null
		text3.actionWord == actionWord

		def paramValues = createdKeywordTestStep.paramValues
		paramValues.size() == 2
		ActionWordParameterValue value1 = paramValues.get(0)
		value1.id != null
		value1.value == "<date>"
		value1.actionWordParam == param1
		value1.keywordTestStep == createdKeywordTestStep

		ActionWordParameterValue value2 = paramValues.get(1)
		value2.id != null
		value2.value == "<Year>"
		value2.actionWordParam == param2
		value2.keywordTestStep == createdKeywordTestStep

		def tcParams = createdKeywordTestStep.getTestCase().getParameters()
		tcParams.size() == 2
		tcParams.collect { it.name }.sort() == ["Year", "date"]
	}

	@DataSet("TestCaseModificationServiceImplIT.keyword test cases.xml")
	def "should add a keyword test step with an existing action word to test case"() {
		when:
		KeywordTestStep createdKeywordTestStep = service.addKeywordTestStep(-4L, "THEN", "    the Action wôrd exists.    ")

		then:
		createdKeywordTestStep != null
		createdKeywordTestStep.id != null

		THEN == createdKeywordTestStep.keyword

		ActionWord actionWord = createdKeywordTestStep.actionWord
		actionWord.id == -78L
		actionWord.createWord() == "the Action wôrd exists."
		actionWord.token == "T-the Action wôrd exists.-"

		def fragments = actionWord.getFragments()
		fragments.size() == 1

		def f1 = fragments.get(0)
		f1.class.is(ActionWordText)
		def text1 = (ActionWordText) f1
		text1.getText() == "the Action wôrd exists."
		text1.id == -7
		text1.actionWord == actionWord
	}

	@DataSet("TestCaseModificationServiceImplIT.keyword test cases.xml")
	def "should add a keyword test step with an existing action word that contains parameters to test case"() {
		when:
		KeywordTestStep createdKeywordTestStep = service.addKeywordTestStep(-4L, "AND", "    today is \"Tuesday\" of \"May\" \"2020\"   ")

		then:
		createdKeywordTestStep != null
		createdKeywordTestStep.id != null

		Keyword.AND == createdKeywordTestStep.keyword

		ActionWord actionWord = createdKeywordTestStep.actionWord
		actionWord.id == -66L
		actionWord.createWord() == "today is \"date\" of \"month\" \"year\""
		actionWord.token == "TPTPTP-today is - of - -"

		def fragments = actionWord.getFragments()
		fragments.size() == 6

		def f1 = fragments.get(0)
		f1.class.is(ActionWordText)
		def text1 = (ActionWordText) f1
		text1.text == "today is "
		text1.id == -6
		text1.actionWord == actionWord

		def f2 = fragments.get(1)
		f2.class.is(ActionWordParameter)
		def parameter = (ActionWordParameter) f2
		parameter.id == -5
		parameter.defaultValue == "Monday"
		parameter.name == "date"
		text1.actionWord == actionWord

		def f3 = fragments.get(2)
		f3.class.is(ActionWordText)
		def text2 = (ActionWordText) f3
		text2.text == " of "
		text2.id == -4
		text2.actionWord == actionWord

		def f4 = fragments.get(3)
		f4.class.is(ActionWordParameter)
		def parameter2 = (ActionWordParameter) f4
		parameter2.id == -3
		parameter2.defaultValue == ""
		parameter2.name == "month"
		parameter2.actionWord == actionWord

		def f5 = fragments.get(4)
		f5.class.is(ActionWordText)
		def text3 = (ActionWordText) f5
		text3.text == " "
		text3.id == -2
		text3.actionWord == actionWord

		def f6 = fragments.get(5)
		f6.class.is(ActionWordParameter)
		def parameter3 = (ActionWordParameter) f6
		parameter3.id == -1
		parameter3.defaultValue == "2000"
		parameter3.name == "year"
		parameter3.actionWord == actionWord

		def paramValues = createdKeywordTestStep.paramValues
		paramValues.size() == 3
		ActionWordParameterValue value1 = paramValues.get(0)
		value1.id != null
		value1.value == "Tuesday"
		value1.actionWordParam == parameter
		value1.keywordTestStep == createdKeywordTestStep

		ActionWordParameterValue value2 = paramValues.get(1)
		value2.id != null
		value2.value == "May"
		value2.actionWordParam == parameter2
		value2.keywordTestStep == createdKeywordTestStep

		ActionWordParameterValue value3 = paramValues.get(2)
		value3.id != null
		value3.value == "2020"
		value3.actionWordParam == parameter3
		value3.keywordTestStep == createdKeywordTestStep

	}

	@DataSet("TestCaseModificationServiceImplIT.keyword test cases.xml")
	def "should add a keyword test step with an existing action word that contains parameters in which some are between <> to test case"() {
		when:
		KeywordTestStep createdKeywordTestStep = service.addKeywordTestStep(-4L, "AND", "    today is < date > of <mon1h> \"2020\"   ")

		then:
		createdKeywordTestStep != null
		createdKeywordTestStep.id != null

		Keyword.AND == createdKeywordTestStep.keyword

		ActionWord actionWord = createdKeywordTestStep.actionWord
		actionWord.id == -66L
		actionWord.createWord() == "today is \"date\" of \"month\" \"year\""
		actionWord.token == "TPTPTP-today is - of - -"

		def fragments = actionWord.getFragments()
		fragments.size() == 6

		def f1 = fragments.get(0)
		f1.class.is(ActionWordText)
		def text1 = (ActionWordText) f1
		text1.text == "today is "
		text1.id == -6
		text1.actionWord == actionWord

		def f2 = fragments.get(1)
		f2.class.is(ActionWordParameter)
		def parameter = (ActionWordParameter) f2
		parameter.id == -5
		parameter.defaultValue == "Monday"
		parameter.name == "date"
		text1.actionWord == actionWord

		def f3 = fragments.get(2)
		f3.class.is(ActionWordText)
		def text2 = (ActionWordText) f3
		text2.text == " of "
		text2.id == -4
		text2.actionWord == actionWord

		def f4 = fragments.get(3)
		f4.class.is(ActionWordParameter)
		def parameter2 = (ActionWordParameter) f4
		parameter2.id == -3
		parameter2.defaultValue == ""
		parameter2.name == "month"
		parameter2.actionWord == actionWord

		def f5 = fragments.get(4)
		f5.class.is(ActionWordText)
		def text3 = (ActionWordText) f5
		text3.text == " "
		text3.id == -2
		text3.actionWord == actionWord

		def f6 = fragments.get(5)
		f6.class.is(ActionWordParameter)
		def parameter3 = (ActionWordParameter) f6
		parameter3.id == -1
		parameter3.defaultValue == "2000"
		parameter3.name == "year"
		parameter3.actionWord == actionWord

		def paramValues = createdKeywordTestStep.paramValues
		paramValues.size() == 3
		ActionWordParameterValue value1 = paramValues.get(0)
		value1.id != null
		value1.value == "<date>"
		value1.actionWordParam == parameter
		value1.keywordTestStep == createdKeywordTestStep

		ActionWordParameterValue value2 = paramValues.get(1)
		value2.id != null
		value2.value == "<mon1h>"
		value2.actionWordParam == parameter2
		value2.keywordTestStep == createdKeywordTestStep

		ActionWordParameterValue value3 = paramValues.get(2)
		value3.id != null
		value3.value == "2020"
		value3.actionWordParam == parameter3
		value3.keywordTestStep == createdKeywordTestStep

		def tcParams = createdKeywordTestStep.getTestCase().getParameters()
		tcParams.size() == 2
		tcParams.collect { it.name }.sort() == ["date", "mon1h"]
	}

	@DataSet("TestCaseModificationServiceImplIT.keyword test cases.xml")
	def "should add a keyword test step with a new action word containing parameters as numbers to test case"() {
		when:
		KeywordTestStep createdKeywordTestStep = service.addKeywordTestStep(-4L, "AND", "  1   year with   0.5 month   and -10,5\"month\"   2 month  5 week 0,77   <tcParam>4 day   -13.5  semester   and  55    ")

		then:
		createdKeywordTestStep != null
		createdKeywordTestStep.id != null

		Keyword.AND == createdKeywordTestStep.keyword

		ActionWord actionWord = createdKeywordTestStep.actionWord
		actionWord.id != null
		actionWord.createWord() == "\"param1\" year with \"param2\" month and \"param3\"\"param4\" \"param5\" month \"param6\" week \"param7\" \"param8\"\"param9\" day \"param10\" semester and \"param11\""
		actionWord.token == "PTPTPPTPTPTPTPPTPTP- year with - month and - - month - week - - day - semester and -"
		def fragments = actionWord.getFragments()
		fragments.size() == 19

		def f0 = fragments.get(0)
		f0.class.is(ActionWordParameter)
		def param1 = (ActionWordParameter) f0
		param1.name == "param1"
		param1.id != null
		param1.defaultValue == "1"
		param1.actionWord == actionWord

		def f1 = fragments.get(1)
		f1.class.is(ActionWordText)
		def text1 = (ActionWordText) f1
		text1.getText() == " year with "
		text1.id != null
		text1.actionWord == actionWord

		def f2 = fragments.get(2)
		f2.class.is(ActionWordParameter)
		def param2 = (ActionWordParameter) f2
		param2.name == "param2"
		param2.defaultValue == "0.5"

		def f3 = fragments.get(3)
		f3.class.is(ActionWordText)
		def text2 = (ActionWordText) f3
		text2.getText() == " month and "
		text2.id != null

		def f4 = fragments.get(4)
		f4.class.is(ActionWordParameter)
		def param3 = (ActionWordParameter) f4
		param3.name == "param3"
		param3.id != null

		def f5 = fragments.get(5)
		f5.class.is(ActionWordParameter)
		def param4 = (ActionWordParameter) f5
		param4.name == "param4"
		param4.id != null

		def f6 = fragments.get(6)
		f6.class.is(ActionWordText)
		def text3 = (ActionWordText) f6
		text3.getText() == " "
		text3.id != null

		def f7 = fragments.get(7)
		f7.class.is(ActionWordParameter)
		def param5 = (ActionWordParameter) f7
		param5.name == "param5"
		param5.id != null

		def f8 = fragments.get(8)
		f8.class.is(ActionWordText)
		def text4 = (ActionWordText) f8
		text4.getText() == " month "
		text4.id != null

		def f9 = fragments.get(9)
		f9.class.is(ActionWordParameter)
		def param6 = (ActionWordParameter) f9
		param6.name == "param6"
		param6.id != null

		def f10 = fragments.get(10)
		f10.class.is(ActionWordText)
		def text5 = (ActionWordText) f10
		text5.getText() == " week "
		text5.id != null

		def f11 = fragments.get(11)
		f11.class.is(ActionWordParameter)
		def param7 = (ActionWordParameter) f11
		param7.name == "param7"
		param7.id != null

		def f12 = fragments.get(12)
		f12.class.is(ActionWordText)
		def text6 = (ActionWordText) f12
		text6.getText() == " "
		text6.id != null

		def f13 = fragments.get(13)
		f13.class.is(ActionWordParameter)
		def param8 = (ActionWordParameter) f13
		param8.name == "param8"
		param8.id != null

		def f14 = fragments.get(14)
		f14.class.is(ActionWordParameter)
		def param9 = (ActionWordParameter) f14
		param9.name == "param9"
		param9.id != null

		def f15 = fragments.get(15)
		f15.class.is(ActionWordText)
		def text7 = (ActionWordText) f15
		text7.getText() == " day "
		text7.id != null

		def f16 = fragments.get(16)
		f16.class.is(ActionWordParameter)
		def param10 = (ActionWordParameter) f16
		param10.name == "param10"
		param10.id != null

		def f17 = fragments.get(17)
		f17.class.is(ActionWordText)
		def text8 = (ActionWordText) f17
		text8.getText() == " semester and "
		text8.id != null

		def f18 = fragments.get(18)
		f18.class.is(ActionWordParameter)
		def param11 = (ActionWordParameter) f18
		param11.name == "param11"
		param11.id != null
		///////////////////////////////////////////////////

		def paramValues = createdKeywordTestStep.paramValues
		paramValues.size() == 11
		ActionWordParameterValue value1 = paramValues.get(0)
		value1.id != null
		value1.value == "1"
		value1.actionWordParam == param1
		value1.keywordTestStep == createdKeywordTestStep

		ActionWordParameterValue value2 = paramValues.get(1)
		value2.id != null
		value2.value == "0.5"
		value2.actionWordParam == param2

		ActionWordParameterValue value3 = paramValues.get(2)
		value3.id != null
		value3.value == "-10,5"
		value3.actionWordParam == param3

		ActionWordParameterValue value4 = paramValues.get(3)
		value4.id != null
		value4.value == "month"
		value4.actionWordParam == param4

		ActionWordParameterValue value5 = paramValues.get(4)
		value5.id != null
		value5.value == "2"
		value5.actionWordParam == param5

		ActionWordParameterValue value6 = paramValues.get(5)
		value6.id != null
		value6.value == "5"
		value6.actionWordParam == param6

		ActionWordParameterValue value7 = paramValues.get(6)
		value7.id != null
		value7.value == "0,77"
		value7.actionWordParam == param7

		ActionWordParameterValue value8 = paramValues.get(7)
		value8.id != null
		value8.value == "<tcParam>"
		value8.actionWordParam == param8

		ActionWordParameterValue value9 = paramValues.get(8)
		value9.id != null
		value9.value == "4"
		value9.actionWordParam == param9

		ActionWordParameterValue value10 = paramValues.get(9)
		value10.id != null
		value10.value == "-13.5"
		value10.actionWordParam == param10

		ActionWordParameterValue value11 = paramValues.get(10)
		value11.id != null
		value11.value == "55"
		value11.actionWordParam == param11
	}

	@DataSet("TestCaseModificationServiceImplIT.should update keyword test steps.xml")
	def "should update the keyword of a keyword test step"() {
		given:
		def stepId = -18L

		when:
		service.updateKeywordTestStep(stepId, THEN)

		then:
		KeywordTestStep step = findEntity(KeywordTestStep.class, stepId)
		step.keyword == THEN
	}

	@DataSet("TestCaseModificationServiceImplIT.should update keyword test steps.xml")
	def "should update the datatable of a keyword test step"() {
		given:
		def stepId = -18L

		and:
		def updatedDatatable = """| product | price |
| Expresso | 0.40 |"""

		when:
		service.updateKeywordTestStepDatatable(stepId, updatedDatatable)

		then:
		KeywordTestStep step = findEntity(KeywordTestStep.class, stepId)
		step.datatable == updatedDatatable
	}

	@DataSet("TestCaseModificationServiceImplIT.should update keyword test steps.xml")
	def "should update the docstring of a keyword test step"() {
		given:
		def stepId = -18L

		and:
		def updatedDocstring = """Product (string)
Price (in euro)"""

		when:
		service.updateKeywordTestStepDocstring(stepId, updatedDocstring)

		then:
		KeywordTestStep step = findEntity(KeywordTestStep.class, stepId)
		step.docstring == updatedDocstring
	}

	@DataSet("TestCaseModificationServiceImplIT.should update keyword test steps.xml")
	def "should update the comment of a keyword test step"() {
		given:
		def stepId = -18L

		and:
		def updatedComment = """Products are from France.
Prices are all with taxes."""

		when:
		service.updateKeywordTestStepComment(stepId, updatedComment)

		then:
		KeywordTestStep step = findEntity(KeywordTestStep.class, stepId)
		step.comment == updatedComment
	}

	@DataSet("TestCaseModificationServiceImplIT.should update keyword test steps.xml")
	def "should update the action word of a keyword test step with same token but new parameter values"() {
		given:
		def stepId = -19L

		when:
		service.updateKeywordTestStep(stepId, "I have <number> apples")

		then:
		KeywordTestStep step = findEntity(KeywordTestStep.class, stepId)
		step.paramValues.size() == 1
		step.paramValues[0].value == "<number>"
		step.actionWord.id == -119
		step.actionWord.token == "TPT-I have - apples-"
		step.testCase.parameters.size() == 1
		step.testCase.parameters[0].name == "number"
	}

	@DataSet("TestCaseModificationServiceImplIT.should update keyword test steps.xml")
	def "should update the action word of a keyword test step with an existing action by removing a parameter"() {
		given:
		def stepId = -19L

		when:
		service.updateKeywordTestStep(stepId, "I have apples")

		then:
		KeywordTestStep step = findEntity(KeywordTestStep.class, stepId)
		step.paramValues.size() == 0
		step.actionWord.id == -118
		step.actionWord.token == "T-I have apples-"
		step.actionWord.fragments.size() == 1
		step.actionWord.fragments[0].text == "I have apples"
		step.testCase.parameters.size() == 0
	}

	@DataSet("TestCaseModificationServiceImplIT.should update keyword test steps.xml")
	def "should update the action word of a keyword test step with modifying token and with a given action word id"() {
		given:
		def stepId = -19L
		def actionWordId = -126

		when:
		service.updateKeywordTestStep(stepId, "hello", actionWordId)

		then:
		KeywordTestStep step = findEntity(KeywordTestStep.class, stepId)
		step.paramValues.size() == 0
		step.actionWord.id == -126
		step.actionWord.token == "T-hello-"
		step.actionWord.fragments.size() == 1
		step.actionWord.fragments[0].text == "hello"
		step.testCase.parameters.size() == 0
	}

	@DataSet("TestCaseModificationServiceImplIT.should update keyword test steps.xml")
	def "should update the action word of a keyword test step with a new action by adding a parameter and text"() {
		given:
		def stepId = -18L

		when:
		service.updateKeywordTestStep(stepId, "I have <number> <fruit> and \"vegetables\"")

		then:
		KeywordTestStep step = findEntity(KeywordTestStep.class, stepId)
		step.paramValues.size() == 3
		step.paramValues[0].value == "<number>"
		step.paramValues[1].value == "<fruit>"
		step.paramValues[2].value == "vegetables"
		step.actionWord.id != -118
		step.actionWord.token == "TPTPTP-I have - - and -"
		step.actionWord.fragments.size() == 6
		step.actionWord.fragments[0].text == "I have "
		step.actionWord.fragments[2].text == " "
		step.actionWord.fragments[4].text == " and "
		step.testCase.parameters.size() == 2
		step.testCase.parameters[0].name == "number"
		step.testCase.parameters[1].name == "fruit"
	}
}
