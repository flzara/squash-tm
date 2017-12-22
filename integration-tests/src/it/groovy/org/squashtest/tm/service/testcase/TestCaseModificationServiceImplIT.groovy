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
import org.squashtest.tm.domain.project.GenericProject
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.domain.testcase.ActionTestStep
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.domain.testcase.TestCaseFolder
import org.squashtest.tm.exception.DuplicateNameException
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.squashtest.tm.service.project.GenericProjectManagerService
import org.unitils.dbunit.annotation.DataSet
import spock.unitils.UnitilsSupport

import javax.inject.Inject

@UnitilsSupport
@RunWith(Sputnik)
@Transactional
class TestCaseModificationServiceImplIT extends DbunitServiceSpecification {

	@Inject
	private TestCaseModificationService service

	@Inject
	private TestCaseLibraryNavigationService navService

	@Inject GenericProjectManagerService projectService

	private int testCaseId=-1;
	private int folderId = -1;

	def setup(){
		projectService.persist(createProject())

		def libList= session.createQuery("from TestCaseLibrary").list()


		def lib = libList.get(libList.size()-1);

		def folder =  new TestCaseFolder(name:"folder")
		def testCase = new TestCase(name: "test case 1", description: "the first test case")

		navService.addFolderToLibrary(lib.id,folder)
		navService.addTestCaseToFolder (folder.id, testCase, null )

		folderId = folder.id;
		testCaseId= testCase.id;
	}


	def "should add a test step to test case"(){
		given :
		ActionTestStep step = new ActionTestStep(action: "action", expectedResult: "result")

		when :
		def teststep = service.addActionTestStep(testCaseId, step);

		then :
		teststep != null
		teststep.id != null
		teststep.action == step.action
		teststep.expectedResult == step.expectedResult
	}

	def "should add an empty test step to test case"(){
		given :
		ActionTestStep step = new ActionTestStep(action: "", expectedResult: "")

		when :
		def teststep = service.addActionTestStep(testCaseId, step);

		then :
		teststep != null
		teststep.id != null
		teststep.action == step.action
		teststep.expectedResult == step.expectedResult
	}

	def "should get a test step list from a test case"(){
		given :

		ActionTestStep step1 = new ActionTestStep(action: "first step", expectedResult: "should work")
		and :
		ActionTestStep step2 = new ActionTestStep(action: "second step", expectedResult: "should work too")

		when :

		service.addActionTestStep(testCaseId, step1);
		service.addActionTestStep(testCaseId, step2);

		def list = service.findStepsByTestCaseId(testCaseId);



		then :

		list != null
		list.size() == 2
		list[0].action == step1.action
		list[1].action == step2.action
	}


	def "should rename a lone test case"(){
		given :
		def newName = "new name"
		when :
		service.rename(testCaseId, newName);
		def testcase = service.findById(testCaseId)
		then :
		testcase!=null
		testcase.id == testCaseId
		testcase.name == "new name"
		testcase.description == "the first test case"
	}

	def "should rename a test case if another library node have a different name"(){
		given :
		def tc2name = "test case 2"
		def tc2desc = "should rename"
		def newName = "new name"

		def newtc= new TestCase(name: tc2name, description: tc2desc)

		when :
		navService.addTestCaseToFolder(folderId,newtc, null)
		service.rename(newtc.id, newName)
		def renewtc = service.findById(newtc.id)
		then :
		renewtc!=null
		renewtc.id == newtc.id
		renewtc.name == "new name"
		renewtc.description == "should rename"
	}

	def "should not rename a test case if another library node have the same name"(){
		given :
		def tc2name = "test case 2"
		def tc2desc = "should fail"
		def newName = "test case 1"

		def newtc = new TestCase(name: tc2name, description: tc2desc)

		when :
		navService.addTestCaseToFolder(folderId, newtc, null  )
		service.rename(newtc.id, newName)
		then :
		thrown(DuplicateNameException)
	}



	def "should change a test case description"(){
		given :
		def tcNewDesc = "the new desc"
		when :
		service.changeDescription(testCaseId, tcNewDesc)
		def tc = service.findById(testCaseId)

		then :
		tc.description == tcNewDesc
	}

	def "should change a test case reference"(){
		given :
		def tcNewRef = "the new ref"
		when :
		service.changeReference(testCaseId, tcNewRef)
		def tc = service.findById(testCaseId)

		then :
		tc.reference == tcNewRef
	}

	@DataSet("TestCaseModificationServiceImplIT.should update a test step action.xml")
	def "should update a test step action "(){
		given :
		def stepId = -2L
		and:
		def newAction = "manmana"
		when :
		service.updateTestStepAction(stepId, newAction)

		then :
		ActionTestStep step = findEntity(ActionTestStep.class, -2L);
		step.action == newAction;
	}


	def "should update a test step expected result"(){
		given :
		ActionTestStep step = new ActionTestStep(action: "first step", expectedResult: "should work")

		def newres = "confirm"

		when :
		def tstep = service.addActionTestStep(testCaseId, step)
		service.updateTestStepExpectedResult(tstep.id, newres)


		def listSteps = service.findStepsByTestCaseId (testCaseId)
		tstep = listSteps.get(0);

		then :
		tstep.expectedResult == newres
	}

	def "should move step 2 to position #3 in a list of 3 test steps"(){
		given :
		def step1 = new ActionTestStep("first step", "first result")
		def step2 = new ActionTestStep("second step", "second result")
		def step3 = new ActionTestStep("third step", "third result")

		when :
		service.addActionTestStep(testCaseId, step1)
		def tstep2 =service.addActionTestStep(testCaseId, step2)
		def tstep3 = service.addActionTestStep(testCaseId, step3)

		service.changeTestStepPosition(testCaseId, tstep2.id, 2)

		def list = service.findStepsByTestCaseId(testCaseId)


		then :
		list[1].id == tstep3.id
		list[2].id == tstep2.id
	}

	def "should move step 3 to position #2 in a list of 3 test steps"(){
		given :
		def step1 = new ActionTestStep("first step", "first result")
		def step2 = new ActionTestStep("second step", "second result")
		def step3 = new ActionTestStep("third step", "third result")

		when :
		service.addActionTestStep(testCaseId, step1)
		def tstep2 =service.addActionTestStep(testCaseId, step2)
		def tstep3 = service.addActionTestStep(testCaseId, step3)

		service.changeTestStepPosition(testCaseId, tstep3.id, 1)

		def list = service.findStepsByTestCaseId(testCaseId)


		then :
		list[1].id == tstep3.id
		list[2].id == tstep2.id
	}

	def "should move a couple of steps to position #2"(){

		given :
		def step1 = new ActionTestStep("first step", "first result")
		def step2 = new ActionTestStep("second step", "second result")
		def step3 = new ActionTestStep("third step", "third result")
		def step4 = new ActionTestStep("fourth step", "fourth result")
		def step5 = new ActionTestStep("fifth step", "fifth result")
		def step6 = new ActionTestStep("sixth step", "sixth result")

		and :
		service.addActionTestStep(testCaseId, step1)
		service.addActionTestStep(testCaseId, step2)
		service.addActionTestStep(testCaseId, step3)
		service.addActionTestStep(testCaseId, step4)
		service.addActionTestStep(testCaseId, step5)
		service.addActionTestStep(testCaseId, step6)
		when :


		service.changeTestStepsPosition(testCaseId, 1, [step4, step5].collect{it.id})
		def reTc = service.findTestCaseWithSteps (testCaseId)

		then :
		reTc.getSteps().collect{it.id} == [step1, step4, step5, step2, step3, step6].collect{it.id}


	}

	def "should move the three first steps at last position"(){
		given :
		def step1 = new ActionTestStep("first step", "first result")
		def step2 = new ActionTestStep("second step", "second result")
		def step3 = new ActionTestStep("third step", "third result")
		def step4 = new ActionTestStep("fourth step", "fourth result")
		def step5 = new ActionTestStep("fifth step", "fifth result")
		def step6 = new ActionTestStep("sixth step", "sixth result")

		and :
		service.addActionTestStep(testCaseId, step1)
		service.addActionTestStep(testCaseId, step2)
		service.addActionTestStep(testCaseId, step3)
		service.addActionTestStep(testCaseId, step4)
		service.addActionTestStep(testCaseId, step5)
		service.addActionTestStep(testCaseId, step6)

		when :
		service.changeTestStepsPosition(testCaseId, 3, [step1, step2, step3].collect{it.id})
		def reTc = service.findTestCaseWithSteps (testCaseId)

		then :
		reTc.getSteps().collect{it.id} == [step4, step5, step6, step1, step2, step3].collect{it.id}
	}



	def "should remove step 2 in a list of three steps"(){
		given :
		def step1 = new ActionTestStep("first step", "first result")
		def step2 = new ActionTestStep("second step", "second result")
		def step3 = new ActionTestStep("third step", "third result")

		when :
		def tstep1 = service.addActionTestStep(testCaseId, step1)
		def tstep2 =service.addActionTestStep(testCaseId, step2)
		def tstep3 = service.addActionTestStep(testCaseId, step3)

		service.removeStepFromTestCase(testCaseId, tstep2.id)

		def list = service.findStepsByTestCaseId(testCaseId)

		then :
		list.size() == 2
		list[0].id == tstep1.id
		list[1].id == tstep3.id
	}


	def "should initialize a test case with his test steps"(){

		given :
		def tc = new TestCase(name:"rich-tc")
		def ts1 = new ActionTestStep(action:"action1", expectedResult:"ex1")
		def ts2 = new ActionTestStep(action:"action2", expectedResult:"ex2")



		navService.addTestCaseToFolder(folderId, tc, null)
		service.addActionTestStep tc.id, ts1
		service.addActionTestStep tc.id, ts2


		when :

		def obj = service.findTestCaseWithSteps (tc.id)


		then :
		obj.steps.size()==2
		obj.steps*.action.containsAll(["action1", "action2"])
	}


	def "should remove a list of steps"(){
		given :
		def tc = new TestCase(name:"stepdeletion-tc")
		def ts1 = new ActionTestStep(action:"action1", expectedResult:"ex1")
		def ts2 = new ActionTestStep(action:"action2", expectedResult:"ex2")
		def ts3 = new ActionTestStep(action:"action3", expectedResult:"ex3")
		def ts4 = new ActionTestStep(action:"action4", expectedResult:"ex4")
		def ts5 = new ActionTestStep(action:"action5", expectedResult:"ex5")


		navService.addTestCaseToFolder(folderId, tc, null)
		service.addActionTestStep tc.id, ts1
		service.addActionTestStep tc.id, ts2
		service.addActionTestStep tc.id, ts3
		service.addActionTestStep tc.id, ts4
		service.addActionTestStep tc.id, ts5


		when :

		def toRemove = [ts1.id, ts3.id, ts5.id]

		service.removeListOfSteps(tc.id, toRemove);

		def obj = service.findStepsByTestCaseId(tc.id)


		then :
		obj.size()==2
		obj*.action.containsAll(["action2", "action4"])
	}

	@DataSet("TestCaseModificationServiceImplIT.should remove automated script link.xml")
	def "should remove automated script link"(){
		given :
		def testCaseId = -11L

		when :
		service.removeAutomation(testCaseId)

		then:
		TestCase testCase = findEntity(TestCase.class, testCaseId)
		testCase.getAutomatedTest() == null
	}

	def GenericProject createProject(){
		Project p = new Project();
		p.name = Double.valueOf(Math.random()).toString();
		p.description = "eaerazer"
		return p
	}

	@DataSet("TestCaseModificationServiceImplIT.should find test cases.xml")
	def "should find all test cases by ids"(){
		when :
		def res = service.findAllByIds([-10L, -30L])

		then:
		res*.getId().containsAll([-10L, -30L])
		res.size() == 2
	}

	@DataSet("TestCaseModificationServiceImplIT.should find test cases.xml")
	def "should find all test cases by ancestor ids"(){
		when :
		def res = service.findAllByAncestorIds(ancestors)

		then:
		res*.getId().containsAll(expected)
		res.size() == expected.size()

		where:
		ancestors 		| expected
		[-10L]	     	| [-10L]
		[-50L]  	   	| [-30L]
		[-40L]     		| [-30L,-20L]
		[-20L, -40L]    | [-20L, -30L]
	}

	def "should paste a bunch of steps to end of list"(){

		given :
		def step1 = new ActionTestStep("first step", "first result")
		def step2 = new ActionTestStep("second step", "second result")
		def step3 = new ActionTestStep("third step", "third result")
		def step4 = new ActionTestStep("fourth step", "fourth result")
		def step5 = new ActionTestStep("fifth step", "fifth result")
		def step6 = new ActionTestStep("sixth step", "sixth result")
		def steps = [step1, step3, step2, step4, step6, step5]

		and :
		service.addActionTestStep(testCaseId, step1)
		service.addActionTestStep(testCaseId, step3)
		service.addActionTestStep(testCaseId, step2)
		service.addActionTestStep(testCaseId, step4)
		service.addActionTestStep(testCaseId, step6)
		service.addActionTestStep(testCaseId, step5)
		session.flush();
		session.clear();

		when :


		service.pasteCopiedTestStepToLastIndex(testCaseId, steps.collect{it.id})
		session.flush()
		session.clear()
		TestCase result = findEntity(TestCase.class,testCaseId)

		then :
		result.getSteps().size() == 12;
		def expectedActionList = steps.collect({it.getAction()})
		expectedActionList.addAll(expectedActionList)
		def stepsResults = result.getSteps().collect({it.getAction()})
		stepsResults.asList() == expectedActionList.asList()

	}

	def "should paste a bunch of steps to position 3"(){

		given :
		def step1 = new ActionTestStep("first step", "first result")
		def step2 = new ActionTestStep("second step", "second result")
		def step3 = new ActionTestStep("third step", "third result")
		def step4 = new ActionTestStep("fourth step", "fourth result")
		def step5 = new ActionTestStep("fifth step", "fifth result")
		def step6 = new ActionTestStep("sixth step", "sixth result")


		and :
		service.addActionTestStep(testCaseId, step1)
		service.addActionTestStep(testCaseId, step3)
		service.addActionTestStep(testCaseId, step2)
		service.addActionTestStep(testCaseId, step4)
		service.addActionTestStep(testCaseId, step6)
		service.addActionTestStep(testCaseId, step5)


		def steps = [step1, step3, step2, step4, step6, step5]

		when :
		service.pasteCopiedTestSteps(testCaseId, step2.getId(),steps.collect{it.getId()})
		TestCase result = findEntity(TestCase.class,testCaseId)

		then :
		result.getSteps().size() == 12;
		def expectedActionList = [step1, step3, step2, step1, step3, step2, step4, step6, step5, step4, step6, step5].collect {it.getAction()}
		def stepsResults = result.getSteps().collect({it.getAction()})
		stepsResults == expectedActionList

	}
}
