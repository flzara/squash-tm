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
package org.squashtest.tm.domain.testcase

import java.lang.reflect.Modifier

import org.squashtest.tm.core.foundation.exception.NullArgumentException
import org.squashtest.tm.tools.unittest.assertions.CollectionAssertions
import org.squashtest.tm.tools.unittest.reflection.ReflectionCategory
import org.squashtest.tm.domain.testautomation.AutomatedTest
import org.squashtest.tm.domain.testutils.MockFactory;
import org.squashtest.tm.exception.UnknownEntityException

import spock.lang.Specification
import spock.lang.Unroll

class TestCaseTest extends Specification {

	MockFactory mockFactory = new MockFactory()
	TestCase testCase = new TestCase()

	def setup() {
		CollectionAssertions.declareContainsExactly()
	}

	def "should add a step at the end of the list"() {
		given:
		testCase.steps << new ActionTestStep(action: "1")
		testCase.steps << new ActionTestStep(action: "2")

		and:
		def newStep = new ActionTestStep(action: "3")

		when:
		testCase.addStep(newStep)

		then:
		testCase.steps[2] == newStep
	}

	def "should not add a null step"() {
		when:
		testCase.addStep(null)

		then:
		thrown(NullArgumentException)
	}

	def "should not add a null dataset"() {
		when:
		testCase.addDataset(null)

		then:
		thrown(NullArgumentException)
	}

	def "should not add a null parameter"() {
		when:
		testCase.addParameter(null)

		then:
		thrown(NullArgumentException)
	}

	def "should move step from given index to a greater index"() {
		given:
		def step0 = new ActionTestStep(action:"0")
		def step1 = new ActionTestStep(action:"1")
		def step2 = new ActionTestStep(action:"2")
		def step3 = new ActionTestStep( action:"3")

		testCase.steps << step0
		testCase.steps << step1
		testCase.steps << step2
		testCase.steps << step3

		when:
		testCase.moveStep(1, 3)

		then:
		testCase.steps == [step0, step2, step3, step1]
	}

	def "should move step from given index to a lesser index"() {
		given:
		def step0 = new ActionTestStep(action:"0")
		def step1 = new ActionTestStep(action:"1")
		def step2 = new ActionTestStep(action:"2")
		def step3 = new ActionTestStep(action:"3")

		testCase.steps << step0
		testCase.steps << step1
		testCase.steps << step2
		testCase.steps << step3

		when:
		testCase.moveStep(2, 0)

		then:
		testCase.steps == [step2, step0, step1, step3]
	}

	def "should move a list of steps to a lesser index"(){

		given :
		def step0 = new ActionTestStep(action:"0")
		def step1 = new ActionTestStep(action:"1")
		def step2 = new ActionTestStep(action:"2")
		def step3 = new ActionTestStep(action:"3")


		testCase.steps << step0
		testCase.steps << step1
		testCase.steps << step2
		testCase.steps << step3


		def tomove = [step2, step3]
		def position = 1
		def result = [step0, step2, step3, step1]


		when :

		testCase.moveSteps(position, tomove)

		then :
		testCase.steps.collect{ it.action } == result.collect{ it.action }
	}



	def "should move a list of steps to a last position"(){

		given :
		def step0 = new ActionTestStep( action:"0")
		def step1 = new ActionTestStep( action:"1")
		def step2 = new ActionTestStep( action:"2")
		def step3 = new ActionTestStep( action:"3")


		testCase.steps << step0
		testCase.steps << step1
		testCase.steps << step2
		testCase.steps << step3


		def tomove = [step0, step1]
		def position = 2
		def result = [step2, step3, step0, step1]

		when :
		testCase.moveSteps(position, tomove)

		then :
		testCase.steps.collect{ it.action } == result.collect{ it.action }
	}



	def "should return position of step"() {
		given:
		TestStep step10 = Mock()
		step10.id >> 10
		testCase.steps << step10

		TestStep step20 = Mock()
		step20.id >> 20
		testCase.steps << step20


		when:
		def pos = testCase.getPositionOfStep(20)

		then:
		pos == 1
	}

	def "should throw exception when position of unknown step is asked"() {
		given:
		TestStep step10 = Mock()
		step10.id >> 10
		testCase.steps << step10

		when:
		def pos = testCase.getPositionOfStep(20)

		then:
		thrown(UnknownEntityException)
	}

	@Unroll("copy of test case should have the same '#propName' property")
	def "copy of a test case should have the same simple properties"() {
		given:
		TestCase source = new TestCase()
		source.setName("foo");
		source.notifyAssociatedWithProject(mockFactory.mockProject())

		source[propName] = propValue

		when:
		def copy = source.createCopy()

		then:
		copy[propName] == source[propName]

		where:
		propName        | propValue
		"prerequisite"  | "foobarfoo"
		"name"          | "foo"
		"description"   | "bar"
		"executionMode" | TestCaseExecutionMode.AUTOMATED
		"importance"    | TestCaseImportance.HIGH
		"status"		| TestCaseStatus.APPROVED
		"reference"     | "barfoo"
	}

	def "copy of a test case should have the same steps"() {
		given:
		TestCase source = new TestCase()
		source.setName("source");
		source.notifyAssociatedWithProject(mockFactory.mockProject())
		ActionTestStep sourceStep = new ActionTestStep(action: "fingerpoke opponent", expectedResult: "win the belt")
		source.steps << sourceStep

		when:
		def copy = source.createCopy()

		then:
		copy.steps.size() == 1
		copy.steps[0].action == sourceStep.action
		copy.steps[0].expectedResult == sourceStep.expectedResult
		!copy.steps[0].is(sourceStep)
	}


	def "should remove automated script link"(){
		given :
		TestCase automatedTestCase = new TestCase();
		AutomatedTest automatedTest = new AutomatedTest();
		use(ReflectionCategory){
			TestCase.set field:"automatedTest", of:automatedTestCase, to: automatedTest
		}
		when :
		automatedTestCase.removeAutomatedScript();

		then:
		automatedTestCase.automatedTest == null;

	}

	def "should create blank test case"() {
		given:
		def findFields
		findFields = { it ->
			List fields = it.declaredFields
			def sc = it.superclass
			if (sc != null) {
				fields.addAll(findFields(sc))
			}

			return fields
		}

		and:
		List blankableFields = findFields(TestCase)
				.findAll({ !(Collection.isAssignableFrom(it.type) || Map.isAssignableFrom(it.type)) })
				.findAll({ !(Modifier.isStatic(it.modifiers) || Modifier.isFinal(it.modifiers))})
				.findAll({ ! [Long.TYPE, Integer.TYPE, Boolean.TYPE, Float.TYPE, Double.TYPE].contains(it.type) })
				.findAll({ ! it.name.startsWith('audit') && ! it.name.equals("executionMode")})
				.each { it.setAccessible(true) }

		when:
		TestCase res = TestCase.createBlankTestCase()

		then:
		blankableFields.findAll({ it.get(res) != null })*.name == []

	}

}
