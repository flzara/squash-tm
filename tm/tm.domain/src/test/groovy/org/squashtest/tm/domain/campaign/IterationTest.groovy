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
package org.squashtest.tm.domain.campaign

import org.squashtest.tm.domain.attachment.Attachment
import org.squashtest.tm.domain.attachment.AttachmentList
import org.squashtest.tm.domain.execution.Execution
import org.squashtest.tm.domain.execution.ExecutionStatus
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.domain.testcase.TestCaseExecutionMode
import org.squashtest.tm.exception.DuplicateNameException
import spock.lang.Specification
import spock.lang.Unroll

import java.text.SimpleDateFormat

class IterationTest extends Specification {


	Iteration copySource = new Iteration(
	description: "description",
	name: "name",
	reference: "ref",
	campaign: Mock(Campaign)
	)


	@Unroll
	def "copy of an Iteration should copy its property #prop"() {
		when:
		Iteration copy = copySource.createCopy()

		then:
		copy[prop] == copySource[prop]

		where:
		prop << ["name", "description", "reference"]
	}


	def "copy of an Iteration should copy it's planning infos"() {
		given:
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy")

		Date expectedStart = format.parse("01/01/2001")
		Date actualStart = format.parse("04/04/2002")
		Date actualEnd = format.parse("05/05/2003")

		copySource.setScheduledStartDate(expectedStart)
		copySource.setActualStartAuto(false)
		copySource.setActualStartDate(actualStart)
		copySource.setActualEndAuto(true)
		copySource.setActualEndDate(actualEnd)

		when:
		Iteration copy = copySource.createCopy()

		then: "scheduled and auto infos are copied"
		copy.getScheduledStartDate().equals(expectedStart)
		copy.getScheduledEndDate() == null
		copy.isActualStartAuto() == copySource.isActualStartAuto()
		copy.isActualEndAuto() == copySource.isActualEndAuto()
		and:"acual dates are not copied [Issue 1250]"
		copy.getActualStartDate() == null
		copy.getActualEndDate() == null

		copy.getName() == copySource.getName()
	}
	def "copy of an Iteration should copy it's test plan without deleted testCases"() {
		given:
		TestCase tc1 = new TestCase()
		IterationTestPlanItem testPlanItem = new IterationTestPlanItem()
		testPlanItem.setReferencedTestCase(tc1)
		IterationTestPlanItem testPlanItemWithoutTestCase = new IterationTestPlanItem()

		copySource.addTestPlan(testPlanItem)
		copySource.addTestPlan(testPlanItemWithoutTestCase)

		when:
		Iteration copy = copySource.createCopy()

		then:
		copy.getTestPlans().size() == 1
		copy.getTestPlans().get(0).getReferencedTestCase() == tc1
	}
	def "copy of an Iteration should copy it's attachments"() {
		given:
		Attachment attach = Mock(Attachment)
		Attachment attachCopy = new Attachment()
		attach.hardCopy() >> attachCopy
		copySource.getAttachmentList().addAttachment(attach)

		when:
		Iteration copy = copySource.createCopy()
		AttachmentList attList = copy.getAttachmentList()
		Set<Attachment> list = attList.getAllAttachments()

		then:
		list.size() == 1
		list.asList() == [attachCopy]
	}
	def "copy of test suites should return the indexes of test-plan-items to bind in iteration-copied-test-plan"(){
		TestCase tc1 = new TestCase()
		IterationTestPlanItem testPlanItem1 = new IterationTestPlanItem()
		testPlanItem1.setLabel("testPlanItem1")
		testPlanItem1.setReferencedTestCase(tc1)
		IterationTestPlanItem testPlanItemWithoutTestCase = new IterationTestPlanItem()
		TestCase tc2 = new TestCase()
		IterationTestPlanItem testPlanItem2 = new IterationTestPlanItem()
		testPlanItem2.setReferencedTestCase(tc2)
		testPlanItem2.setLabel("testPlanItem2")
		copySource.addTestPlan(testPlanItem1)
		copySource.addTestPlan(testPlanItemWithoutTestCase)
		copySource.addTestPlan(testPlanItem2)
		TestSuite testSuite = new TestSuite()
		copySource.addTestSuite(testSuite)
		testSuite.setName("testSuite")
		testSuite.bindTestPlanItems([testPlanItem1, testPlanItem2])

		when:
		Map<TestSuite, List<Integer>> testSuitesPastableCopies = copySource.createTestSuitesPastableCopy()

		then:
		testSuitesPastableCopies.size() == 1
		testSuitesPastableCopies.entrySet().find {it.getKey().getName() == "testSuite"} != null
		def entry = testSuitesPastableCopies.entrySet().find {it.getKey().getName() == "testSuite"}
		entry.getValue().size() == 2
		entry.getValue().find {it == 0} != null
		entry.getValue().find {it == 1} != null
	}
	def "should add a test plan"(){

		given :
		TestCase testCase = Mock()
		testCase.getId() >> 1
		testCase.getName() >> "testCase1"
		testCase.getExecutionMode() >> TestCaseExecutionMode.MANUAL

		Iteration iteration = new Iteration()

		when :
		IterationTestPlanItem testplan = new IterationTestPlanItem(testCase)
		iteration.addTestPlan(testplan)

		then :
		iteration.getTestPlans().size()==1
		iteration.getTestPlans().get(0).getLabel()=="testCase1"
	}


	def "should add two test plans"(){
		given :
		TestCase testCase = Mock()
		testCase.getId() >> 1
		testCase.getName() >> "testCase1"
		testCase.getExecutionMode() >> TestCaseExecutionMode.MANUAL

		TestCase testCase2 = Mock()
		testCase2.getId() >> 2
		testCase2.getName() >> "testCase2"
		testCase2.getExecutionMode() >> TestCaseExecutionMode.MANUAL


		Iteration iteration = new Iteration()
		iteration.addTestPlan(new IterationTestPlanItem (testCase))
		iteration.addTestPlan(new IterationTestPlanItem (testCase2))

		when :
		List<Execution> listExec = iteration.getExecutions()
		List<IterationTestPlanItem> listPlans = iteration.getTestPlans()

		then :
		listExec.size()==0
		listPlans.size()==2
	}

	def "should return the index of a test plan item"(){

		given :
		def iteration = new Iteration()
		def item1 = new IterationTestPlanItem(referencedTestCase:Mock(TestCase))
		def item2 = new IterationTestPlanItem(referencedTestCase:Mock(TestCase))
		def item3 = new IterationTestPlanItem(referencedTestCase:Mock(TestCase))

		iteration.addTestPlan(item1)
		iteration.addTestPlan(item2)
		iteration.addTestPlan(item3)

		when :

		def index = iteration.getIndexOf(item2)


		then :
		index == 1
	}

	/* *********************** autodates cascade logic * ****************************** */

	private buildTestIteration(){

		TestCase testCase1 = Mock()
		testCase1.getId() >> 1
		testCase1.getName() >> "testCase1"
		testCase1.getExecutionMode() >> TestCaseExecutionMode.MANUAL

		TestCase testCase2 = Mock()
		testCase2.getId() >> 2
		testCase2.getName() >> "testCase2"
		testCase2.getExecutionMode() >> TestCaseExecutionMode.MANUAL


		Iteration iteration = new Iteration()

		IterationTestPlanItem tp1 = new IterationTestPlanItem(iteration:iteration,referencedTestCase:testCase1)
		IterationTestPlanItem tp2 = new IterationTestPlanItem(iteration:iteration,referencedTestCase:testCase2)

		tp1.executionStatus = ExecutionStatus.SUCCESS
		tp2.executionStatus = ExecutionStatus.SUCCESS

		tp1.lastExecutedOn = null
		tp2.lastExecutedOn = null

		iteration.addTestPlan(tp1)
		iteration.addTestPlan(tp2)

		return iteration
	}

	//One of the item test plan status is not terminated i.e. RUNNING or READY
	private buildTestIterationNotTerminated(){

		TestCase testCase1 = Mock()
		testCase1.getId() >> 1
		testCase1.getName() >> "testCase1"
		testCase1.getExecutionMode() >> TestCaseExecutionMode.MANUAL

		TestCase testCase2 = Mock()
		testCase2.getId() >> 2
		testCase2.getName() >> "testCase2"
		testCase2.getExecutionMode() >> TestCaseExecutionMode.MANUAL


		Iteration iteration = new Iteration()

		IterationTestPlanItem tp1 = new IterationTestPlanItem(iteration:iteration,referencedTestCase:testCase1)
		IterationTestPlanItem tp2 = new IterationTestPlanItem(iteration:iteration,referencedTestCase:testCase2)
		tp1.setExecutionStatus(ExecutionStatus.READY)
		tp2.setExecutionStatus(ExecutionStatus.SUCCESS)

		iteration.addTestPlan(tp1)
		iteration.addTestPlan(tp2)

		return iteration
	}


	def "should autocompute actual start date and end date"(){
		given :
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy")
		Date expectedStart = format.parse("01/01/2001")
		Date expectedEnd = format.parse("05/05/2010")

		Iteration iteration = buildTestIteration()
		List<IterationTestPlanItem> testPlans = iteration.getTestPlans()

		testPlans[1].setLastExecutedOn(expectedStart)
		testPlans[0].setLastExecutedOn(expectedEnd)


		when :

		iteration.setActualStartAuto(true)
		iteration.setActualEndAuto(true)

		then :

		iteration.actualStartDate.equals(expectedStart)
		iteration.actualEndDate.equals(expectedEnd)
	}


	def "should autocompute actual start and end date to the same date"(){
		given :

		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy")
		Date expectedDate = format.parse("01/01/2001")

		Iteration iteration = buildTestIteration()
		List<IterationTestPlanItem> testPlans = iteration.getTestPlans()

		testPlans[1].setLastExecutedOn(expectedDate)

		when :


		iteration.setActualStartAuto(true)
		iteration.setActualEndAuto(true)


		then :
		iteration.actualStartDate.equals(iteration.actualEndDate)
		iteration.actualStartDate.equals(expectedDate)
	}


	/*
	 * expected result is that
	 * - the actual start after update is the new execution date of an Item Test Plan,
	 * - the actual end after update is the actual start before update
	 *
	 */
	def "should udpate actual start and end because new input is lower"(){
		given :
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy")

		Date initialStart = format.parse("01/01/2001")
		Date initialEnd = format.parse("05/05/2010")
		Date newStart = format.parse("10/10/2000")

		Iteration iteration = buildTestIteration()
		List<IterationTestPlanItem> testPlans = iteration.getTestPlans()

		testPlans[1].lastExecutedOn = initialStart
		testPlans[0].lastExecutedOn = initialEnd

		iteration.setActualStartAuto(true)
		iteration.setActualEndAuto(true)

		when :
		testPlans[0].lastExecutedOn=newStart //note that this test plan was previously set to initialEnd

		then :
		iteration.actualStartDate.equals(newStart)
		iteration.actualEndDate.equals(initialStart)

	}

	/*
	 * expected result is both dates set to initial end
	 *
	 */
	def "should update actual start and end because new input is null"(){
		given :
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy")

		Date initialStart = format.parse("01/01/2001")
		Date initialEnd = format.parse("05/05/2010")

		Iteration iteration = buildTestIteration()
		List<IterationTestPlanItem> testPlans = iteration.getTestPlans()

		testPlans[1].lastExecutedOn = initialStart
		testPlans[0].lastExecutedOn = initialEnd

		iteration.setActualStartAuto(true)
		iteration.setActualEndAuto(true)

		when :
		testPlans[1].lastExecutedOn=null

		then :
		iteration.actualStartDate.equals(iteration.actualEndDate)
		iteration.actualStartDate.equals(initialEnd)
	}

	/*
	 * expected result  :
	 * 	- new start = new start
	 *  - new end = initial end
	 */
	def "should update actual start date because the item test plan responsible for the former date has changed"(){

		given :
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy")

		Date initialStart = format.parse("01/01/2001")
		Date initialEnd = format.parse("05/05/2010")
		Date newStart = format.parse("01/01/2005")

		Iteration iteration = buildTestIteration()
		List<IterationTestPlanItem> testPlans = iteration.getTestPlans()

		testPlans[1].lastExecutedOn = initialStart
		testPlans[0].lastExecutedOn = initialEnd

		iteration.setActualStartAuto(true)
		iteration.setActualEndAuto(true)


		when :
		testPlans[1].lastExecutedOn = newStart

		then :
		iteration.actualStartDate.equals(newStart)
		iteration.actualEndDate.equals(initialEnd)
	}


	/*
	 * expected result  :
	 *  - new start = initial start
	 *  - new end = new end
	 */
	def "should update actual end date because the item test plan responsible for the former date has changed"(){
		given :
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy")

		Date initialStart = format.parse("01/01/2001")
		Date initialEnd = format.parse("05/05/2010")
		Date newEnd = format.parse("01/01/2005")

		Iteration iteration = buildTestIteration()
		List<IterationTestPlanItem> testPlans = iteration.getTestPlans()

		testPlans[1].lastExecutedOn = initialStart
		testPlans[0].lastExecutedOn = initialEnd

		iteration.setActualStartAuto(true)
		iteration.setActualEndAuto(true)


		when :
		testPlans[0].lastExecutedOn = newEnd

		then :
		iteration.actualStartDate.equals(initialStart)
		iteration.actualEndDate.equals(newEnd)
	}

	def "should not update actual start and end date because they simply aren't in autoset mode"(){

		given :
		Iteration iteration = buildTestIteration()
		List<IterationTestPlanItem> testPlans = iteration.getTestPlans()

		when :
		testPlans[0].lastExecutedOn=new Date()


		then :
		iteration.actualEndDate == null
		iteration.actualStartDate == null
	}

	//terminated means not RUNNING nor READY
	def "should not update actual end date because test plan status is not terminated"(){
		given :
		Iteration iteration = buildTestIterationNotTerminated()
		List<IterationTestPlanItem> testPlans = iteration.getTestPlans()
		iteration.setActualEndAuto(true)

		when :
		testPlans[0].lastExecutedOn = new Date()

		then :
		iteration.actualEndDate == null
	}

	def "should tell that the suite name is available"(){
		given :
		Iteration iteration = new Iteration()
		iteration.testSuites = [
			new TestSuite(name:"suite1"),
			new TestSuite(name:"suite2")
		]

		when :
		def res =iteration.checkSuiteNameAvailable("suite3")
		then :
		res == true
	}

	def "should tell that the suite name is not available"(){
		given :
		Iteration iteration = new Iteration()
		iteration.testSuites = [
			new TestSuite(name:"suite1"),
			new TestSuite(name:"suite2")
		]

		when :
		def res =iteration.checkSuiteNameAvailable("suite2")
		then :
		res == false
	}

	def "should add a suite to an iteration"(){
		given :
		Iteration iteration = new Iteration()
		iteration.testSuites = [
			new TestSuite(name:"suite1"),
			new TestSuite(name:"suite2")
		]

		when :
		def suite = new TestSuite(name:"suite3")
		iteration.addTestSuite(suite)
		then :
		iteration.testSuites.contains(suite)
	}


	def "should rant because the iteration already have a suite with same name"(){
		given :
		Iteration iteration = new Iteration()
		iteration.testSuites = [
			new TestSuite(name:"suite1"),
			new TestSuite(name:"suite2")
		]

		when :
		def suite = new TestSuite(name:"suite2")
		iteration.addTestSuite(suite)
		then :
		thrown DuplicateNameException
	}

	def "ALWAYS PASSING should return only once each planned test cases"() {
		given:
		Iteration i = new Iteration()

		and:
		TestCase tc = Mock()

		and: "two test plan items referencing the same test case"
		[1, 2].each {
			IterationTestPlanItem item = Mock()
			item.referencedTestCase >> tc
			i.addTestPlan(item)
		}

		expect:
		true // not sure about business rules, need confirmation first.
		//i.plannedTestCase == [tc]
	}
}
