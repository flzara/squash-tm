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

import org.squashtest.tm.tools.unittest.reflection.ReflectionCategory
import org.squashtest.tm.domain.attachment.Attachment
import org.squashtest.tm.domain.execution.Execution
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.domain.testcase.TestStep
import org.squashtest.tm.exception.DuplicateNameException;

import spock.lang.Specification

class TestSuiteTest extends Specification {

	def "should rename normally"(){
		given :
		def iteration = Mock(Iteration)
		iteration.checkSuiteNameAvailable(_) >> true

		and :
		def suite = new TestSuite(name:"bob")
		suite.iteration = iteration

		when :
		suite.rename("robert")

		then :
		suite.name == "robert"
	}

	def "should rant because cannot rename"(){

		given :
		def iteration = Mock(Iteration)
		iteration.checkSuiteNameAvailable(_) >> false

		and :
		def suite = new TestSuite(name : "bob")
		suite.iteration = iteration

		when :
		suite.rename("robert")

		then :
		thrown DuplicateNameException
		suite.name == "bob"
	}


	def "should associate with a bunch of items test plan"(){
		given :
		def items = []
		3.times{items <<  mockITP(it)}
		and :
		def suite = new TestSuite()
		when :
		suite.bindTestPlanItems(items)

		then :
		1 * items[0].addTestSuite(suite)
		1 * items[1].addTestSuite(suite)
		1 * items[2].addTestSuite(suite)
	}

	def "should associate with a bunch of item test plan"(){

		given :
		def items = []
		3.times{items <<  mockITP(it)}

		and :
		def iteration = Mock(Iteration)
		iteration.getTestPlans() >> items

		and :
		def suite = new TestSuite()
		suite.iteration=iteration

		when :
		suite.bindTestPlanItemsById([0l, 1l, 2l])

		then :
		1 * items[0].addTestSuite(suite)
		1 * items[1].addTestSuite(suite)
		1 * items[2].addTestSuite(suite)
	}

	def "should reorder item test plans (1)"(){

		given :

		def iteration = new Iteration()
		def suite = new TestSuite()
		iteration.addTestSuite(suite)
		suite.iteration=iteration

		def items = []
		10.times{
			def item = new IterationTestPlanItem(referencedTestCase:Mock(TestCase))
			iteration.addTestPlan(item)
			items << item
		}

		and :
		suite.bindTestPlanItems(items[2, 4, 6, 7, 8, 9])
		
		and :

		def toMove = items[4, 6, 7]

		when :

		suite.reorderTestPlan(0, toMove)

		then :
		suite.getTestPlan() == items[4, 6, 7, 2, 8, 9]
		iteration.getTestPlans() == items[0, 1, 2, 3, 4, 5, 6, 7, 8, 9]
	}

	def "should reorder item test plans (2)"(){

		given :

		def iteration = new Iteration()
		def suite = new TestSuite()
		iteration.addTestSuite(suite)
		suite.iteration=iteration

		def items = []
		10.times{
			def item = new IterationTestPlanItem(referencedTestCase:Mock(TestCase))
			iteration.addTestPlan(item)
			items << item
		}

		and :
		suite.bindTestPlanItems(items[2, 4, 6, 7, 8, 9])

		and :

		def toMove = items[4, 6, 7]

		when :

		suite.reorderTestPlan(2, toMove)

		then :
		suite.getTestPlan() == items[2, 8, 4, 6, 7, 9]
		iteration.getTestPlans() == items[0, 1, 2, 3, 4, 5, 6, 7, 8, 9]
	}

	def "copy of a TestSuite's test plan should avoid deleted testCases"() {
		given:
		TestCase tc = new TestCase()
		IterationTestPlanItem testPlanItem = new IterationTestPlanItem(Mock(TestCase))
		testPlanItem.setReferencedTestCase(tc)
		testPlanItem.setLabel("name")

		and:
		IterationTestPlanItem testPlanItemWithoutTestCase = new IterationTestPlanItem(Mock(TestCase))
		// we need to remove the test case leter, otherwise the item wont be added to the test plan.

		and:
		Iteration iteration = new Iteration()
		iteration.addTestPlan(testPlanItem)
		iteration.addTestPlan(testPlanItemWithoutTestCase)
		testPlanItemWithoutTestCase.referencedTestCase = null


		and:
		TestSuite testSuite = new TestSuite()
		iteration.addTestSuite(testSuite)
		testSuite.bindTestPlanItems([
			testPlanItem,
			testPlanItemWithoutTestCase
		])

		when:
		List<IterationTestPlanItem> copiedTestPlan = testSuite.createPastableCopyOfTestPlan()

		then:
		copiedTestPlan.size() == 1
		copiedTestPlan.get(0).getLabel() == "name"
	}
	def "copy of a TestSuite's test plan should not copy executions"() {
		given:
		TestCase tc1 = new TestCase()
		IterationTestPlanItem testPlanItem = new IterationTestPlanItem()
		testPlanItem.setReferencedTestCase(tc1)
		testPlanItem.setLabel("name")
		Execution execution = new Execution()
		testPlanItem.addExecution(execution)
		Iteration iteration = new Iteration()
		iteration.addTestPlan(testPlanItem)
		TestSuite testSuite = new TestSuite()
		testSuite.setIteration iteration
		testSuite.bindTestPlanItems([testPlanItem])

		when:
		List<IterationTestPlanItem> copiedTestPlan = testSuite.createPastableCopyOfTestPlan()

		then:
		copiedTestPlan.size() == 1
		copiedTestPlan.get(0).getLabel() == "name"
		copiedTestPlan.get(0).getExecutions().isEmpty()
	}
	def "copy of a TestSuite should copy it's name , description and attachments only"() {
		given:
		TestCase tc1 = new TestCase()
		IterationTestPlanItem testPlanItem = new IterationTestPlanItem()
		testPlanItem.setReferencedTestCase(tc1)
		Iteration iteration = new Iteration()
		iteration.addTestPlan(testPlanItem)
		TestSuite testSuite = new TestSuite()
		testSuite.setIteration iteration
		testSuite.bindTestPlanItems([testPlanItem])
		testSuite.setName("name")
		testSuite.setDescription("description")
		Attachment attach1 = new Attachment()
		attach1.setName("nameAttach1")
		Attachment attach2 = new Attachment()
		attach2.setName("nameAttach2")
		testSuite.attachmentList.addAttachment(attach1)
		testSuite.attachmentList.addAttachment(attach2)

		when:
		TestSuite copiedTestSuite = testSuite.createCopy()

		then:
		copiedTestSuite.getIteration() == null
		copiedTestSuite.getName() == "name"
		copiedTestSuite.getDescription() == "description"
		copiedTestSuite.attachmentList.allAttachments.size() == 2
		copiedTestSuite.attachmentList.allAttachments.contains(attach1) == false
		copiedTestSuite.attachmentList.allAttachments.contains(attach2) == false
		List<Attachment> copiedAttachments = copiedTestSuite.attachmentList.allAttachments.asList()
		copiedAttachments.get(0).getName() == "nameAttach1" || copiedAttachments.get(0).getName() == "nameAttach2"
		copiedAttachments.get(1).getName() == "nameAttach1" || copiedAttachments.get(1).getName() == "nameAttach2"
	}
	def mockITP = {
		def m = Mock(IterationTestPlanItem)
		m.getId() >> it
		m.getTestSuites() >> new ArrayList<TestSuite>();
		return m
	}

	def "should return the first item of test plan"() {
		given:
		TestSuite testSuite = new TestSuite()
		Iteration iteration = new Iteration()
		testSuite.setIteration(iteration)

		and:
		IterationTestPlanItem item = new IterationTestPlanItem(Mock(TestCase))
		iteration.addTestPlan(item)
		testSuite.bindTestPlanItem(item)

		and:
		IterationTestPlanItem otherItem = new IterationTestPlanItem(Mock(TestCase))
		iteration.addTestPlan(otherItem)
		testSuite.bindTestPlanItem(otherItem)

		when:
		def res = testSuite.getFirstTestPlanItem()

		then:
		res == item
	}

	def "item should not be the last executable of test plan"() {
		given:
		TestSuite testSuite = aSuiteWithExecutableItems(10L, 20L)

		when:
		def res = testSuite.isLastExecutableTestPlanItem(10L)

		then:
		res == false
	}

	def "item should be the last of test plan"() {
		given:
		TestSuite testSuite = aSuiteWithExecutableItems(10L, 20L)

		when:
		def res = testSuite.isLastExecutableTestPlanItem(20L)

		then:
		res == true
	}

	def "item should be the last executable of test plan"() {
		given: 
		TestSuite testSuite = aSuiteWithExecutableItems(10L, 20L)

		and:
		IterationTestPlanItem item = new IterationTestPlanItem(Mock(TestCase))
		use (ReflectionCategory) {
			IterationTestPlanItem.set field: "id", of: item, to: 30L
		}
		testSuite.iteration.addTestPlan(item)
		item.addTestSuite(testSuite)

		and:
		IterationTestPlanItem otherItem = new IterationTestPlanItem(Mock(TestCase))
		testSuite.iteration.addTestPlan(otherItem)
		otherItem.addTestSuite(testSuite)
		use (ReflectionCategory) {
			IterationTestPlanItem.set field: "id", of: otherItem, to: 40L
			IterationTestPlanItem.set field: "referencedTestCase", of: otherItem, to: null
		}

		when:
		def res = testSuite.isLastExecutableTestPlanItem(20L)

		then:
		res == true
	}

	def "wrong item should not be the last of test plan"() {
		given:
		TestSuite testSuite = new TestSuite()
		Iteration iteration = new Iteration()
		testSuite.setIteration(iteration)

		and:
		IterationTestPlanItem item = new IterationTestPlanItem(Mock(TestCase))
		use (ReflectionCategory) {
			IterationTestPlanItem.set field: "id", of: item, to: 10L
		}
		iteration.addTestPlan(item)
		item.addTestSuite(testSuite)

		when:
		def res = testSuite.isLastExecutableTestPlanItem(30L)

		then:
		res == false
	}


	def "should return next executable item of test plan"() {
		given:
		TestSuite testSuite = aSuiteWithExecutableItems(10L, 20L)
		when:
		def res = testSuite.findNextExecutableTestPlanItem(10L)

		then:
		res.id == 20L
	}

	def "should return first executable item of test plan"() {
		given:
		TestSuite testSuite = aSuiteWithExecutableItems(10L, 20L)
		when:
		def res = testSuite.findFirstExecutableTestPlanItem()

		then:
		res.id == 10L
	}


	def "should return next executable item of TestSuite's test plan"() {
		given: "a test suite and an iteration"
		TestSuite testSuite = new TestSuite()
		Iteration iteration = new Iteration()
		testSuite.setIteration(iteration)
		TestCase testCase = Mock()
		TestStep testStep = Mock()
		testCase.getSteps() >> [testStep]

		and:"item linked to test suite and iteration"
		IterationTestPlanItem item = new IterationTestPlanItem(testCase)
		use (ReflectionCategory) {
			IterationTestPlanItem.set field: "id", of: item, to: 10L
		}
		iteration.addTestPlan(item)
		testSuite.bindTestPlanItem(item)

		and:"item2 linked iteration ONLY"
		IterationTestPlanItem item2 = new IterationTestPlanItem(testCase)
		use (ReflectionCategory) {
			IterationTestPlanItem.set field: "id", of: item2, to: 20L
		}
		iteration.addTestPlan(item2)

		and:"item3 linked to test suite and iteration"
		IterationTestPlanItem item3 = new IterationTestPlanItem(testCase)
		use (ReflectionCategory) {
			IterationTestPlanItem.set field: "id", of: item3, to: 30L
		}
		iteration.addTestPlan(item3)
		testSuite.bindTestPlanItem(item3)


		when:
		def res = testSuite.findNextExecutableTestPlanItem(10L)

		then:
		res.id == 30L
	}
	def "should return first executable item of TestSuite's test plan"() {
		given: "a test suite and an iteration"
		TestSuite testSuite = new TestSuite()
		Iteration iteration = new Iteration()
		iteration.addTestSuite(testSuite)

		TestCase testCase = Mock()
		TestStep testStep = Mock()
		testCase.getSteps() >> [testStep]
		TestCase testCase2 = Mock()
		testCase2.getSteps() >> []

		and:"item linked to test suite and iteration whith last execution = terminated"
		IterationTestPlanItem item = new IterationTestPlanItem(testCase)
		use (ReflectionCategory) {
			IterationTestPlanItem.set field: "id", of: item, to: 10L
		}
		iteration.addTestPlan(item)
		Execution execution1 = Mock()
		execution1.findFirstUnexecutedStep()>> null
		item.addExecution(execution1)

		and:"item2 linked iteration ONLY"
		IterationTestPlanItem item2 = new IterationTestPlanItem(testCase)
		use (ReflectionCategory) {
			IterationTestPlanItem.set field: "id", of: item2, to: 20L
		}
		iteration.addTestPlan(item2)

		and:"item3 linked to test suite and iteration"
		IterationTestPlanItem item3 = new IterationTestPlanItem(testCase)
		use (ReflectionCategory) {
			IterationTestPlanItem.set field: "id", of: item3, to: 30L
		}
		iteration.addTestPlan(item3)

		testSuite.bindTestPlanItems([item, item3])

		when:
		def res = testSuite.findFirstExecutableTestPlanItem()

		then:
		res.id == 30L
	}
	def aSuiteWithExecutableItems(Long... ids) {
		TestSuite testSuite = new TestSuite()
		Iteration iteration = new Iteration()
		testSuite.setIteration(iteration)

		ids.each { id ->
			TestCase testCase = Mock()
			TestStep testStep = Mock()
			testCase.getSteps() >> [testStep]
			IterationTestPlanItem item = new IterationTestPlanItem(testCase)
			use (ReflectionCategory) {
				IterationTestPlanItem.set field: "id", of: item, to: id
			}
			iteration.addTestPlan(item)
			testSuite.bindTestPlanItem(item);
		}

		return testSuite
	}
	def "item should not be the first executable of test plan"() {
		given:
		TestSuite testSuite = aSuiteWithExecutableItems(10L, 20L)

		when:
		def res = testSuite.isFirstExecutableTestPlanItem(20L)

		then:
		res == false
	}

	def "item should be the first of test plan"() {
		given:
		TestSuite testSuite = aSuiteWithExecutableItems(10L, 20L)

		when:
		def res = testSuite.isFirstExecutableTestPlanItem(10L)

		then:
		res == true
	}

	def "item should be the first executable of test plan"() {
		given:
		TestSuite testSuite = new TestSuite()
		Iteration iteration = new Iteration()
		testSuite.setIteration(iteration)

		and:
		IterationTestPlanItem otherItem = new IterationTestPlanItem(Mock(TestCase))
		iteration.addTestPlan(otherItem)
		testSuite.bindTestPlanItem(otherItem)
		use (ReflectionCategory) {
			IterationTestPlanItem.set field: "id", of: otherItem, to: 20L
			IterationTestPlanItem.set field: "referencedTestCase", of: otherItem, to: null
		}

		and:
			IterationTestPlanItem item = new IterationTestPlanItem(Mock(TestCase))
		use (ReflectionCategory) {
			IterationTestPlanItem.set field: "id", of: item, to: 10L
		}
		iteration.addTestPlan(item)
		testSuite.bindTestPlanItem(item)
		
		when:
		def res = testSuite.isFirstExecutableTestPlanItem(10L)

		then:
		res == true
	}

	def "wrong item should not be the first of test plan"() {
		given:
		TestSuite testSuite = new TestSuite()
		Iteration iteration = new Iteration()
		testSuite.setIteration(iteration)

		and:
		IterationTestPlanItem item = new IterationTestPlanItem(Mock(TestCase))
		use (ReflectionCategory) {
			IterationTestPlanItem.set field: "id", of: item, to: 10L
		}
		iteration.addTestPlan(item)
		item.addTestSuite(testSuite)

		when:
		def res = testSuite.isFirstExecutableTestPlanItem(30L)

		then:
		res == false
	}

}