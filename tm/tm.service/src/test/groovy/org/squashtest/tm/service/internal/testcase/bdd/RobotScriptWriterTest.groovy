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
package org.squashtest.tm.service.internal.testcase.bdd

import org.springframework.context.MessageSource
import org.squashtest.tm.domain.bdd.ActionWord
import org.squashtest.tm.domain.bdd.ActionWordParameter
import org.squashtest.tm.domain.bdd.ActionWordParameterValue
import org.squashtest.tm.domain.bdd.ActionWordText
import org.squashtest.tm.domain.bdd.Keyword
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.domain.testcase.Dataset
import org.squashtest.tm.domain.testcase.DatasetParamValue
import org.squashtest.tm.domain.testcase.KeywordTestCase
import org.squashtest.tm.domain.testcase.KeywordTestStep
import org.squashtest.tm.domain.testcase.Parameter
import org.squashtest.tm.domain.testcase.TestCaseImportance
import org.squashtest.tm.domain.tf.automationrequest.AutomationRequest
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Unroll

import static org.squashtest.tm.domain.bdd.BddImplementationTechnology.ROBOT
import static org.squashtest.tm.domain.testcase.TestCaseImportance.LOW

class RobotScriptWriterTest extends Specification {

	def robotScriptWriter = new RobotScriptWriter()

	def messageSource = Mock(MessageSource)

	Project project = new Project()

	AutomationRequest automationRequest = new AutomationRequest()

	def setup() {
		project.setBddImplementationTechnology(ROBOT)
		automationRequest.setAutomationPriority(4)
	}

	/* ----- Test Case Script ----- */
	def "Should generate a Robot script without test steps from a KeywordTestCase"() {
		given:
			KeywordTestCase keywordTestCase = new KeywordTestCase()
			keywordTestCase.setName("Disconnection test")
			keywordTestCase.setImportance(LOW)
			keywordTestCase.setAutomationRequest(automationRequest)
			keywordTestCase.notifyAssociatedWithProject(project)
		when:
			String result = robotScriptWriter.writeBddScript(keywordTestCase, messageSource, true)
		then:
			result ==
"""*** Settings ***
Resource	squash_resources.resource

*** Test Cases ***
Disconnection test"""
	}

	def "Should generate a Robot script with test steps containing only text from a KeywordTestCase"() {
		given:
			KeywordTestCase keywordTestCase = new KeywordTestCase()
			keywordTestCase.setName("Disconnection test")
			keywordTestCase.notifyAssociatedWithProject(project)

			KeywordTestStep step1 = new KeywordTestStep(Keyword.GIVEN, createBasicActionWord("I am connécted"))
			KeywordTestStep step2 = new KeywordTestStep(Keyword.WHEN, createBasicActionWord("I sign oùt"))
			KeywordTestStep step3 = new KeywordTestStep(Keyword.THEN, createBasicActionWord("I am dîsconnect&d"))

			keywordTestCase.addStep(step1)
			keywordTestCase.addStep(step2)
			keywordTestCase.addStep(step3)
		when:
			String result = robotScriptWriter.writeBddScript(keywordTestCase, messageSource, true)
		then:
			result ==
"""*** Settings ***
Resource	squash_resources.resource

*** Test Cases ***
Disconnection test
	Given I am connécted
	When I sign oùt
	Then I am dîsconnect&d"""
	}

	def "Should generate a Robot script with test steps containing parameter value as free text from a KeywordTestCase"() {
		given:
			KeywordTestCase keywordTestCase = new KeywordTestCase()
			keywordTestCase.setName("Daily test")
			keywordTestCase.notifyAssociatedWithProject(project)

			KeywordTestStep step1 = new KeywordTestStep(Keyword.GIVEN, createBasicActionWord("Today is Monday"))

			def fragment1 = new ActionWordText("It is ")
			def fragment2 = new ActionWordParameterMock(-1L, "param1", "12 o'clcock")
			def value1 = new ActionWordParameterValue("10 o'clock")
			value1.setActionWordParam(fragment2)
			ActionWord actionWord2 = new ActionWord([fragment1, fragment2] as List)
			KeywordTestStep step2 = new KeywordTestStep(Keyword.WHEN, actionWord2)
			List<ActionWordParameterValue> paramValues = [value1]
			step2.setParamValues(paramValues)

			KeywordTestStep step3 = new KeywordTestStep(Keyword.THEN, createBasicActionWord("I am working"))
			keywordTestCase.addStep(step1)
			keywordTestCase.addStep(step2)
			keywordTestCase.addStep(step3)
		when:
			String result = robotScriptWriter.writeBddScript(keywordTestCase, messageSource, true)
		then:
			result ==
"""*** Settings ***
Resource	squash_resources.resource

*** Test Cases ***
Daily test
	Given Today is Monday
	When It is "10 o'clock"
	Then I am working"""
	}

	@Unroll
	def "Should generate a Robot script with test steps containing parameter value as a number from a KeywordTestCase"() {
		given:
			KeywordTestCase keywordTestCase = new KeywordTestCase()
			keywordTestCase.setName("Daily test")
			keywordTestCase.notifyAssociatedWithProject(project)

			KeywordTestStep step1 = new KeywordTestStep(Keyword.GIVEN, createBasicActionWord("Today is Monday"))

			def fragment1 = new ActionWordText("It is ")
			def fragment2 = new ActionWordParameterMock(-1L, "param1", "12 o'clcock")
			def value1 = new ActionWordParameterValue(word)
			value1.setActionWordParam(fragment2)
			ActionWord actionWord2 = new ActionWord([fragment1, fragment2] as List)
			KeywordTestStep step2 = new KeywordTestStep(Keyword.WHEN, actionWord2)
			List<ActionWordParameterValue> paramValues = [value1]
			step2.setParamValues(paramValues)

			keywordTestCase.addStep(step1)
			keywordTestCase.addStep(step2)
		when:
			String result = robotScriptWriter.writeBddScript(keywordTestCase, messageSource, true)
		then:
			result ==
"""*** Settings ***
Resource	squash_resources.resource

*** Test Cases ***
Daily test
	Given Today is Monday
	When It is \"${word}\""""
		where:
			word << ["10", "10.5", "10,5", "-10.5", "-10,5"]
	}

	def "Should generate a Robot script with test steps containing parameter associated with a TC param as value from a KeywordTestCase but no dataset"() {
		given:
			KeywordTestCase keywordTestCase = new KeywordTestCase()
			keywordTestCase.setName("Daily test")
			keywordTestCase.notifyAssociatedWithProject(project)

			KeywordTestStep step1 = new KeywordTestStep(Keyword.GIVEN, createBasicActionWord("Today is Monday"))

			def fragment1 = new ActionWordText("It is ")
			def fragment2 = new ActionWordParameterMock(-1L, "param1", "12 o'clcock")
			def value1 = new ActionWordParameterValue("<time>")
			value1.setActionWordParam(fragment2)
			ActionWord actionWord2 = new ActionWord([fragment1, fragment2] as List)
			KeywordTestStep step2 = new KeywordTestStep(Keyword.WHEN, actionWord2)
			List<ActionWordParameterValue> paramValues = [value1]
			step2.setParamValues(paramValues)

			KeywordTestStep step3 = new KeywordTestStep(Keyword.THEN, createBasicActionWord("I am working"))

			keywordTestCase.addStep(step1)
			keywordTestCase.addStep(step2)
			keywordTestCase.addStep(step3)
		when:
			String result = robotScriptWriter.writeBddScript(keywordTestCase, messageSource, true)
		then:
			result ==
"""*** Settings ***
Resource	squash_resources.resource

*** Test Cases ***
Daily test
	Given Today is Monday
	When It is \${time}
	Then I am working"""
	}

	def "Should generate a Robot script with test steps from a KeywordTestCase with dataset but no param between <>"() {
		given:
			KeywordTestCase keywordTestCase = new KeywordTestCase()
			keywordTestCase.setName("Daily test")
			keywordTestCase.notifyAssociatedWithProject(project)

			KeywordTestStep step1 = new KeywordTestStep(Keyword.GIVEN, createBasicActionWord("Today is Monday"))

			def fragment1 = new ActionWordText("It is ")
			def fragment2 = new ActionWordParameterMock(-1L, "param1", "12 o'clcock")
			def value1 = new ActionWordParameterValue("time")
			value1.setActionWordParam(fragment2)
			ActionWord actionWord2 = new ActionWord([fragment1, fragment2] as List)
			KeywordTestStep step2 = new KeywordTestStep(Keyword.WHEN, actionWord2)
			List<ActionWordParameterValue> paramValues = [value1]
			step2.setParamValues(paramValues)

			KeywordTestStep step3 = new KeywordTestStep(Keyword.THEN, createBasicActionWord("I am working"))

			keywordTestCase.addStep(step1)
			keywordTestCase.addStep(step2)
			keywordTestCase.addStep(step3)

			def tcParam =  new Parameter("tcParam")
			keywordTestCase.getParameters() >> [tcParam]
			def dataset =  new Dataset()
			dataset.setName("dataset1")

			keywordTestCase.addDataset(dataset)

			def value =  new DatasetParamValue(tcParam, dataset,"9 AM")
			dataset.addParameterValue(value)
		when:
			String result = robotScriptWriter.writeBddScript(keywordTestCase, messageSource, true)
		then:
			result ==
"""*** Settings ***
Resource	squash_resources.resource

*** Test Cases ***
Daily test
	Given Today is Monday
	When It is "time"
	Then I am working"""
	}

	def "Should generate a Robot script with test steps from a KeywordTestCase with 1 dataset and 1 param between <>"() {
		given:
			KeywordTestCase keywordTestCase = new KeywordTestCase()
			keywordTestCase.setName("Daily test")
			keywordTestCase.notifyAssociatedWithProject(project)

			KeywordTestStep step1 = new KeywordTestStep(Keyword.GIVEN, createBasicActionWord("Today is Monday"))

			def fragment1 = new ActionWordText("It is ")
			def fragment2 = new ActionWordParameterMock(-1L, "param1", "12 o'clcock")
			def value1 = new ActionWordParameterValue("<time>")
			value1.setActionWordParam(fragment2)
			ActionWord actionWord2 = new ActionWord([fragment1, fragment2] as List)
			KeywordTestStep step2 = new KeywordTestStep(Keyword.WHEN, actionWord2)
			List<ActionWordParameterValue> paramValues = [value1]
			step2.setParamValues(paramValues)

			KeywordTestStep step3 = new KeywordTestStep(Keyword.THEN, createBasicActionWord("I am working"))

			keywordTestCase.addStep(step1)
			keywordTestCase.addStep(step2)
			keywordTestCase.addStep(step3)

			def tcParam =  new Parameter("time", keywordTestCase)
			def dataset =  new Dataset()
			dataset.setName("dataset1")

			keywordTestCase.addDataset(dataset)

			def value =  new DatasetParamValue(tcParam, dataset,"9 AM")
			dataset.addParameterValue(value)
		when:
			String result = robotScriptWriter.writeBddScript(keywordTestCase, messageSource, true)
			then:
			result ==
"""*** Settings ***
Resource	squash_resources.resource
Library		squash_tf.TFParamService

*** Test Cases ***
Daily test
	\${time} =	Get Param	time

	Given Today is Monday
	When It is \${time}
	Then I am working"""
	}

	def "Should generate a Robot script with test steps from a KeywordTestCase with 1 dataset and 1 param between <> without escaping the arrow symbols"() {
		given:
			KeywordTestCase keywordTestCase = new KeywordTestCase()
			keywordTestCase.setName("Daily test")
			keywordTestCase.notifyAssociatedWithProject(project)

			KeywordTestStep step1 = new KeywordTestStep(Keyword.GIVEN, createBasicActionWord("Today is Monday"))

			def fragment1 = new ActionWordText("It is ")
			def fragment2 = new ActionWordParameterMock(-1L, "param1", "12 o'clcock")
			def value1 = new ActionWordParameterValue("<time>")
			value1.setActionWordParam(fragment2)
			ActionWord actionWord2 = new ActionWord([fragment1, fragment2] as List)
			KeywordTestStep step2 = new KeywordTestStep(Keyword.WHEN, actionWord2)
			List<ActionWordParameterValue> paramValues = [value1]
			step2.setParamValues(paramValues)

			KeywordTestStep step3 = new KeywordTestStep(Keyword.THEN, createBasicActionWord("I am working"))

			keywordTestCase.addStep(step1)
			keywordTestCase.addStep(step2)
			keywordTestCase.addStep(step3)

			def tcParam =  new Parameter("time", keywordTestCase)
			def dataset =  new Dataset()
			dataset.setName("dataset1")

			keywordTestCase.addDataset(dataset)

			def value =  new DatasetParamValue(tcParam, dataset,"9 AM")
			dataset.addParameterValue(value)
		when:
			String result = robotScriptWriter.writeBddScript(keywordTestCase, messageSource, false)
		then:
			result ==
"""*** Settings ***
Resource	squash_resources.resource
Library		squash_tf.TFParamService

*** Test Cases ***
Daily test
	\${time} =	Get Param	time

	Given Today is Monday
	When It is \${time}
	Then I am working"""
	}

	def "Should generate a Robot script with test steps from a KeywordTestCase with 1 dataset and 2 param between <>"() {
		given:
			KeywordTestCase keywordTestCase = new KeywordTestCase()
			keywordTestCase.setName("Daily test")
			keywordTestCase.notifyAssociatedWithProject(project)

			KeywordTestStep step1 = new KeywordTestStep(Keyword.GIVEN, createBasicActionWord("Today is Monday"))

			def fragment1 = new ActionWordText("It is ")
			def fragment2 = new ActionWordParameterMock(-1L, "param1", "12 o'clcock")
			def value1 = new ActionWordParameterValue("<time>")
			value1.setActionWordParam(fragment2)
			def fragment3 = new ActionWordText(" in ")
			def fragment4 = new ActionWordParameterMock(-2L, "param2", "Paris")
			def value2 = new ActionWordParameterValue("<place>")
			value2.setActionWordParam(fragment4)
			ActionWord actionWord2 = new ActionWord([fragment1, fragment2, fragment3, fragment4] as List)
			KeywordTestStep step2 = new KeywordTestStep(Keyword.WHEN, actionWord2)
			List<ActionWordParameterValue> paramValues = [value1, value2]
			step2.setParamValues(paramValues)

			KeywordTestStep step3 = new KeywordTestStep(Keyword.THEN, createBasicActionWord("I am working"))

			keywordTestCase.addStep(step1)
			keywordTestCase.addStep(step2)
			keywordTestCase.addStep(step3)

			def tcParam1 =  new Parameter("time", keywordTestCase)
			def tcParam2 =  new Parameter("place", keywordTestCase)
			def dataset =  new Dataset()
			dataset.setName("dataset1")

			keywordTestCase.addDataset(dataset)

			def paramValue1 =  new DatasetParamValue(tcParam1, dataset,"9 AM")
			def paramValue2 =  new DatasetParamValue(tcParam2, dataset,"London")
			dataset.parameterValues = [paramValue1, paramValue2]
		when:
			String result = robotScriptWriter.writeBddScript(keywordTestCase, messageSource, true)
		then:
			result ==
"""*** Settings ***
Resource	squash_resources.resource
Library		squash_tf.TFParamService

*** Test Cases ***
Daily test
	\${time} =	Get Param	time
	\${place} =	Get Param	place

	Given Today is Monday
	When It is \${time} in \${place}
	Then I am working"""
	}

	def "Should generate a Robot script with test steps from a KeywordTestCase with 1 dataset and 2 param between <> with values as number"() {
		given:
			KeywordTestCase keywordTestCase = new KeywordTestCase()
			keywordTestCase.setName("Count test")
			keywordTestCase.notifyAssociatedWithProject(project)

			def fragment1 = new ActionWordText("I buy ")
			def fragment2 = new ActionWordParameterMock(-1L, "param1", "10")
			def value1 = new ActionWordParameterValue("<total>")
			value1.setActionWordParam(fragment2)
			def fragment3 = new ActionWordText(" tickets")

			ActionWord actionWord1 = new ActionWord([fragment1, fragment2, fragment3] as List)
			KeywordTestStep step1 = new KeywordTestStep(Keyword.GIVEN, actionWord1)
			List<ActionWordParameterValue> paramValues1 = [value1]
			step1.setParamValues(paramValues1)

			def fragment4 = new ActionWordText("I give ")
			def fragment5 = new ActionWordParameterMock(-2L, "param1", "10")
			def value2 = new ActionWordParameterValue("<less>")
			value2.setActionWordParam(fragment5)
			def fragment6 = new ActionWordText(" to my friend")

			ActionWord actionWord2 = new ActionWord([fragment4, fragment5, fragment6] as List)
			KeywordTestStep step2 = new KeywordTestStep(Keyword.WHEN, actionWord2)
			List<ActionWordParameterValue> paramValues2 = [value2]
			step2.setParamValues(paramValues2)

			def fragment7 = new ActionWordText("I still have ")
			def fragment8 = new ActionWordParameterMock(-3L, "param1", "10")
			def value3 = new ActionWordParameterValue("<left>")
			value3.setActionWordParam(fragment8)
			def fragment9 = new ActionWordText(" tickets")

			ActionWord actionWord3 = new ActionWord([fragment7, fragment8, fragment9] as List)
			KeywordTestStep step3 = new KeywordTestStep(Keyword.THEN, actionWord3)
			List<ActionWordParameterValue> paramValues3 = [value3]
			step3.setParamValues(paramValues3)

			keywordTestCase.addStep(step1)
			keywordTestCase.addStep(step2)
			keywordTestCase.addStep(step3)

			def tcParam1 =  new Parameter("total", keywordTestCase)
			def tcParam2 =  new Parameter("less", keywordTestCase)
			def tcParam3 =  new Parameter("left", keywordTestCase)
			def dataset =  new Dataset()
			dataset.setName("dataset1")

			keywordTestCase.addDataset(dataset)

			def paramValue1 =  new DatasetParamValue(tcParam1, dataset,"5")
			def paramValue2 =  new DatasetParamValue(tcParam2, dataset,"3")
			def paramValue3 =  new DatasetParamValue(tcParam3, dataset,"two")
			dataset.parameterValues = [paramValue1, paramValue2, paramValue3]
		when:
			String result = robotScriptWriter.writeBddScript(keywordTestCase, messageSource, true)
		then:
			result ==
"""*** Settings ***
Resource	squash_resources.resource
Library		squash_tf.TFParamService

*** Test Cases ***
Count test
	\${total} =	Get Param	total
	\${less} =	Get Param	less
	\${left} =	Get Param	left

	Given I buy \${total} tickets
	When I give \${less} to my friend
	Then I still have \${left} tickets"""
	}

	def "Should generate a Robot script with test steps from a KeywordTestCase with 1 dataset whose name contains spaces"() {
		given:
			KeywordTestCase keywordTestCase = new KeywordTestCase()
			keywordTestCase.setName("Count test")
			keywordTestCase.notifyAssociatedWithProject(project)

			def fragment1 = new ActionWordText("I buy ")
			def fragment2 = new ActionWordParameterMock(-1L, "param1", "10")
			def value1 = new ActionWordParameterValue("<total>")
			value1.setActionWordParam(fragment2)
			def fragment3 = new ActionWordText(" tickets")

			ActionWord actionWord1 = new ActionWord([fragment1, fragment2, fragment3] as List)
			KeywordTestStep step1 = new KeywordTestStep(Keyword.GIVEN, actionWord1)
			List<ActionWordParameterValue> paramValues1 = [value1]
			step1.setParamValues(paramValues1)


			def fragment4 = new ActionWordText("I give ")
			def fragment5 = new ActionWordParameterMock(-2L, "param1", "10")
			def value2 = new ActionWordParameterValue("<less>")
			value2.setActionWordParam(fragment5)
			def fragment6 = new ActionWordText(" to my friend")

			ActionWord actionWord2 = new ActionWord([fragment4, fragment5, fragment6] as List)
			KeywordTestStep step2 = new KeywordTestStep(Keyword.WHEN, actionWord2)
			List<ActionWordParameterValue> paramValues2 = [value2]
			step2.setParamValues(paramValues2)

			def fragment7 = new ActionWordText("I still have ")
			def fragment8 = new ActionWordParameterMock(-3L, "param1", "10")
			def value3 = new ActionWordParameterValue("<left>")
			value3.setActionWordParam(fragment8)
			def fragment9 = new ActionWordText(" tickets")

			ActionWord actionWord3 = new ActionWord([fragment7, fragment8, fragment9] as List)
			KeywordTestStep step3 = new KeywordTestStep(Keyword.THEN, actionWord3)
			List<ActionWordParameterValue> paramValues3 = [value3]
			step3.setParamValues(paramValues3)

			keywordTestCase.addStep(step1)
			keywordTestCase.addStep(step2)
			keywordTestCase.addStep(step3)

			def tcParam1 =  new Parameter("total", keywordTestCase)
			def tcParam2 =  new Parameter("less", keywordTestCase)
			def tcParam3 =  new Parameter("left", keywordTestCase)
			def dataset =  new Dataset()
			dataset.setName("  dataset   1    ")

			keywordTestCase.addDataset(dataset)

			def paramValue1 =  new DatasetParamValue(tcParam1, dataset,"5")
			def paramValue2 =  new DatasetParamValue(tcParam2, dataset,"3")
			def paramValue3 =  new DatasetParamValue(tcParam3, dataset,"two")
			dataset.parameterValues = [paramValue1, paramValue2, paramValue3]
		when:
			String result = robotScriptWriter.writeBddScript(keywordTestCase, messageSource, true)
		then:
		result ==
"""*** Settings ***
Resource	squash_resources.resource
Library		squash_tf.TFParamService

*** Test Cases ***
Count test
	\${total} =	Get Param	total
	\${less} =	Get Param	less
	\${left} =	Get Param	left

	Given I buy \${total} tickets
	When I give \${less} to my friend
	Then I still have \${left} tickets"""
	}

	/* ----- Test Step Script Generation ----- */

	def "Should generate a step script with no parameters"() {
		given:
			KeywordTestStep step = new KeywordTestStep(
				Keyword.GIVEN,
				createBasicActionWord("Today is Monday"))
		when:
			String result = robotScriptWriter.writeBddStepScript(step, null, null, true)
		then:
			result == "Given Today is Monday"

	}

	def "Should generate a step script with a parameter value as free text"() {
		given:
			def fragment1 = new ActionWordText("It is ")
			def fragment2 = new ActionWordParameterMock(-1L, "param1", "12 o'clcock")
			def value = new ActionWordParameterValue("10 o'clock")
			value.setActionWordParam(fragment2)
			ActionWord actionWord = new ActionWord([fragment1, fragment2] as List)

			KeywordTestStep step = new KeywordTestStep(Keyword.WHEN, actionWord)
			List<ActionWordParameterValue> paramValues = [value]
			step.setParamValues(paramValues)
		when:
			String result = robotScriptWriter.writeBddStepScript(step, null, null, true)
		then:
			result == "When It is \"10 o'clock\""
	}

	def "Should generate a step script with two side by side parameters valued as free text"() {
		given:
			def fragment1 = new ActionWordText("I am in ")
			def fragment2 = new ActionWordParameterMock(-1L, "param1", "Paris")
			def value2 = new ActionWordParameterValue("Los Angeles")
			value2.setActionWordParam(fragment2)
			def fragment3 = new ActionWordParameterMock(-2L, "param2", "France")
			def value3 = new ActionWordParameterValue("United States")
			value3.setActionWordParam(fragment3)

			ActionWord actionWord = new ActionWord([fragment1, fragment2, fragment3] as List)

			KeywordTestStep step = new KeywordTestStep(Keyword.GIVEN, actionWord)
			List<ActionWordParameterValue> paramValues = [value2, value3]
			step.setParamValues(paramValues)
		when:
			String result = robotScriptWriter.writeBddStepScript(step, null, null, true)
		then:
			result == "Given I am in \"Los Angeles\"\"United States\""
	}

	@Unroll
	def "Should generate a step script with a parameter value as a number"() {
		given:
			def fragment1 = new ActionWordText("It is ")
			def fragment2 = new ActionWordParameterMock(-1L, "param1", "12 o'clcock")
			def value = new ActionWordParameterValue(number)
			value.setActionWordParam(fragment2)
			ActionWord actionWord = new ActionWord([fragment1, fragment2] as List)

			KeywordTestStep step = new KeywordTestStep(Keyword.WHEN, actionWord)
			List<ActionWordParameterValue> paramValues = [value]
			step.setParamValues(paramValues)
		when:
			String result = robotScriptWriter.writeBddStepScript(step, null, null, true)
		then:
			result == "When It is \"${number}\""
		where:
			number << ["10", "10.5", "10,5", "-10.5", "-10,5"]
	}

	def "Should generate a step script with several parameters"() {
		given:
			def fragment1 = new ActionWordText("it is ")
			def fragment2 = new ActionWordParameterMock(-1L, "param1", "12")
			def value2 = new ActionWordParameterValue("9")
			value2.setActionWordParam(fragment2)
			def fragment3 = new ActionWordText(" o'clock in ")
			def fragment4 = new ActionWordParameterMock(-2L, "param2", "Paris")
			def value4 = new ActionWordParameterValue("London")
			def fragment5 = new ActionWordText(" with a ")
			def fragment6 = new ActionWordParameterMock(-3L, "param3", "sunny")
			def value6 = new ActionWordParameterValue("<weather>")
			value6.setActionWordParam(fragment6)
			def fragment7 = new ActionWordText(" weather.")
			value4.setActionWordParam(fragment4)

			ActionWord actionWord = new ActionWord(
				[fragment1, fragment2, fragment3, fragment4, fragment5, fragment6, fragment7] as List)

			KeywordTestStep step = new KeywordTestStep(Keyword.THEN, actionWord)
			List<ActionWordParameterValue> paramValues = [value2, value4, value6]
			step.setParamValues(paramValues)
		when:
			String result = robotScriptWriter.writeBddStepScript(step, null, null, true)
		then:
			result == "Then it is \"9\" o'clock in \"London\" with a \${weather} weather."
	}


	class ActionWordParameterMock extends ActionWordParameter {
		private Long id

		ActionWordParameterMock(Long id, String name, String defaultValue) {
			this.setName(name)
			this.setDefaultValue(defaultValue)
			this.id = id
		}

		Long getId() {
			return id
		}

		void setId(Long id) {
			this.id = id
		}
	}

	def createBasicActionWord(String singleFragment) {
		def fragment = new ActionWordText(singleFragment)
		return new ActionWord([fragment] as List)
	}

}
