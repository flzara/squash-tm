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
import spock.lang.Specification
import spock.lang.Unroll

class RobotScriptWriterTest extends Specification {

	def robotScriptWriter = new RobotScriptWriter()

	Project project = new Project()

	/* ----- Test Case Script ----- */
	def "without test steps"() {
		given:
			KeywordTestCase keywordTestCase = new KeywordTestCase()
			keywordTestCase.setName("Disconnection test")
			keywordTestCase.notifyAssociatedWithProject(project)
		when:
			String result = robotScriptWriter.writeBddScript(keywordTestCase, null, true)
		then:
			result ==
"""*** Settings ***
Resource	squash_resources.resource

*** Test Cases ***
Disconnection test"""
	}

	def "with only text from a KeywordTestCase"() {
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
			String result = robotScriptWriter.writeBddScript(keywordTestCase, null, true)
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

	def "with parameter value as free text from a KeywordTestCase"() {
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
			String result = robotScriptWriter.writeBddScript(keywordTestCase, null, true)
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
	def "with parameter value as a number"() {
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
			String result = robotScriptWriter.writeBddScript(keywordTestCase, null, true)
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

	def "with TC param but no dataset"() {
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
			String result = robotScriptWriter.writeBddScript(keywordTestCase, null, true)
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

	def "with dataset but no param between <>"() {
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
			String result = robotScriptWriter.writeBddScript(keywordTestCase, null, true)
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

	def "with 1 dataset and 1 param between <>"() {
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
			String result = robotScriptWriter.writeBddScript(keywordTestCase, null, true)
			then:
			result ==
"""*** Settings ***
Resource	squash_resources.resource
Library		squash_tf.TFParamService

*** Test Cases ***
Daily test
	\${time} =	Get Test Param	DS_time

	Given Today is Monday
	When It is \${time}
	Then I am working"""
	}

	def "with 1 dataset and 1 param between <> without escaping the arrow symbols"() {
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
			String result = robotScriptWriter.writeBddScript(keywordTestCase, null, false)
		then:
			result ==
"""*** Settings ***
Resource	squash_resources.resource
Library		squash_tf.TFParamService

*** Test Cases ***
Daily test
	\${time} =	Get Test Param	DS_time

	Given Today is Monday
	When It is \${time}
	Then I am working"""
	}

	def "with 1 dataset and 2 param between <>"() {
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
			String result = robotScriptWriter.writeBddScript(keywordTestCase, null, true)
		then:
			result ==
"""*** Settings ***
Resource	squash_resources.resource
Library		squash_tf.TFParamService

*** Test Cases ***
Daily test
	\${time} =	Get Test Param	DS_time
	\${place} =	Get Test Param	DS_place

	Given Today is Monday
	When It is \${time} in \${place}
	Then I am working"""
	}

	def "with 1 dataset and 2 param between <> with values as number"() {
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
			String result = robotScriptWriter.writeBddScript(keywordTestCase, null, true)
		then:
			result ==
"""*** Settings ***
Resource	squash_resources.resource
Library		squash_tf.TFParamService

*** Test Cases ***
Count test
	\${total} =	Get Test Param	DS_total
	\${less} =	Get Test Param	DS_less
	\${left} =	Get Test Param	DS_left

	Given I buy \${total} tickets
	When I give \${less} to my friend
	Then I still have \${left} tickets"""
	}

	def "with 1 dataset and 2 param between <> used twice"() {
		given:
			KeywordTestCase keywordTestCase = new KeywordTestCase()
			keywordTestCase.setName("Working test")
			keywordTestCase.notifyAssociatedWithProject(project)

			def fragment1 = new ActionWordText("it is Monday")

			ActionWord actionWord1 = new ActionWord([fragment1] as List)
			KeywordTestStep step1 = new KeywordTestStep(Keyword.GIVEN, actionWord1)

			def fragment4 = new ActionWordText("it is ")
			def fragment5 = new ActionWordParameterMock(-2L, "param1", "10")
			def value2 = new ActionWordParameterValue("<time>")
			value2.setActionWordParam(fragment5)
			def fragment6 = new ActionWordText(" in ")
			def fragment2 = new ActionWordParameterMock(-1L, "param2", "Paris")
			def value1 = new ActionWordParameterValue("<place>")
			value1.setActionWordParam(fragment2)

			ActionWord actionWord2 = new ActionWord([fragment4, fragment5, fragment6, fragment2] as List)
			KeywordTestStep step2 = new KeywordTestStep(Keyword.WHEN, actionWord2)
			List<ActionWordParameterValue> paramValues2 = [value2, value1]
			step2.setParamValues(paramValues2)

			def fragment7 = new ActionWordText("I work at ")
			def fragment8 = new ActionWordParameterMock(-3L, "param1", "10")
			def value3 = new ActionWordParameterValue("<time>")
			value3.setActionWordParam(fragment8)
			def fragment9 = new ActionWordText(" in ")
			def fragment10 = new ActionWordParameterMock(-4L, "param2", "Paris")
			def value4 = new ActionWordParameterValue("<place>")
			value4.setActionWordParam(fragment10)

			ActionWord actionWord3 = new ActionWord([fragment7, fragment8, fragment9, fragment10] as List)
			KeywordTestStep step3 = new KeywordTestStep(Keyword.THEN, actionWord3)
			List<ActionWordParameterValue> paramValues3 = [value3, value4]
			step3.setParamValues(paramValues3)

			keywordTestCase.addStep(step1)
			keywordTestCase.addStep(step2)
			keywordTestCase.addStep(step3)

			def tcParam1 =  new Parameter("time", keywordTestCase)
			def tcParam2 =  new Parameter("place", keywordTestCase)
			def dataset =  new Dataset()
			dataset.setName("  dataset   1    ")

			keywordTestCase.addDataset(dataset)

			def paramValue1 =  new DatasetParamValue(tcParam1, dataset,"6AM")
			def paramValue2 =  new DatasetParamValue(tcParam2, dataset,"London")
			dataset.parameterValues = [paramValue1, paramValue2]
		when:
			String result = robotScriptWriter.writeBddScript(keywordTestCase, null, true)
		then:
		result ==
"""*** Settings ***
Resource	squash_resources.resource
Library		squash_tf.TFParamService

*** Test Cases ***
Working test
	\${time} =	Get Test Param	DS_time
	\${place} =	Get Test Param	DS_place

	Given it is Monday
	When it is \${time} in \${place}
	Then I work at \${time} in \${place}"""
	}

	def "with 1 datatable"() {
		given:
			KeywordTestCase keywordTestCase = new KeywordTestCase()
			keywordTestCase.setName("User table test")
			keywordTestCase.notifyAssociatedWithProject(project)

			KeywordTestStep step1 = new KeywordTestStep(Keyword.WHEN, createBasicActionWord("I am on user page"))
			KeywordTestStep step2 = new KeywordTestStep(Keyword.THEN, createBasicActionWord("I can see the users"))
			step2.setDatatable("| Henry | Dupond | henry.dupond@mail.com |\n" +
				"| Louis | Dupont | louis.dupont@mail.com |\n" +
				"| Charles | Martin | charles.martin@mail.com |")

			keywordTestCase.addStep(step1)
			keywordTestCase.addStep(step2)
		when:
			String result = robotScriptWriter.writeBddScript(keywordTestCase, null, true)
		then:
		result ==
"""*** Settings ***
Resource	squash_resources.resource

*** Test Cases ***
User table test
	\${row_1_1} =	Create List	Henry	Dupond	henry.dupond@mail.com
	\${row_1_2} =	Create List	Louis	Dupont	louis.dupont@mail.com
	\${row_1_3} =	Create List	Charles	Martin	charles.martin@mail.com
	\${datatable_1} =	Create List	\${row_1_1}	\${row_1_2}	\${row_1_3}

	When I am on user page
	Then I can see the users "\${datatable_1}\""""
	}

	def "with 2 datatables"() {
		given:
		KeywordTestCase keywordTestCase = new KeywordTestCase()
		keywordTestCase.setName("User table test")
		keywordTestCase.notifyAssociatedWithProject(project)

		KeywordTestStep step1 = new KeywordTestStep(Keyword.WHEN, createBasicActionWord("I am on user page"))
		KeywordTestStep step2 = new KeywordTestStep(Keyword.THEN, createBasicActionWord("I can see the users"))
		step2.setDatatable("| Henry | Dupond | henry.dupond@mail.com |\n" +
			"| Louis | Dupont | louis.dupont@mail.com |\n" +
			"| Charles | Martin | charles.martin@mail.com |")
		KeywordTestStep step3 = new KeywordTestStep(Keyword.AND, createBasicActionWord("I see the administrator"))
		step3.setDatatable("| Bruce | Wayne | batman@mail.com |\n" +
			"| Peter | Parker | spiderman@mail.com |\n" +
			"| Clark | Kent | superman@mail.com |")

		keywordTestCase.addStep(step1)
		keywordTestCase.addStep(step2)
		keywordTestCase.addStep(step3)
		when:
			String result = robotScriptWriter.writeBddScript(keywordTestCase, null, true)
		then:
		result ==
"""*** Settings ***
Resource	squash_resources.resource

*** Test Cases ***
User table test
	\${row_1_1} =	Create List	Henry	Dupond	henry.dupond@mail.com
	\${row_1_2} =	Create List	Louis	Dupont	louis.dupont@mail.com
	\${row_1_3} =	Create List	Charles	Martin	charles.martin@mail.com
	\${datatable_1} =	Create List	\${row_1_1}	\${row_1_2}	\${row_1_3}
	\${row_2_1} =	Create List	Bruce	Wayne	batman@mail.com
	\${row_2_2} =	Create List	Peter	Parker	spiderman@mail.com
	\${row_2_3} =	Create List	Clark	Kent	superman@mail.com
	\${datatable_2} =	Create List	\${row_2_1}	\${row_2_2}	\${row_2_3}

	When I am on user page
	Then I can see the users "\${datatable_1}"
	And I see the administrator "\${datatable_2}\""""
	}

	def "with 1 docstring"() {
		given:
			KeywordTestCase keywordTestCase = new KeywordTestCase()
			keywordTestCase.setName("Letter test")
			keywordTestCase.notifyAssociatedWithProject(project)

			KeywordTestStep step1 = new KeywordTestStep(Keyword.GIVEN, createBasicActionWord("following letter is displayed"))
			step1.setDocstring("\tDear Jack,\n" +
				"I have arrived in London this morning. Everything went well!\n" +
				"Looking forward to seeing you on Friday.\n" +
				"\n\tYour friend, John.")

			keywordTestCase.addStep(step1)
		when:
			String result = robotScriptWriter.writeBddScript(keywordTestCase, null, true)
		then:
			result ==
"""*** Settings ***
Resource	squash_resources.resource

*** Test Cases ***
Letter test
	\${docstring_1} =	Set Variable	\\tDear Jack,\\nI have arrived in London this morning. Everything went well!\\nLooking forward to seeing you on Friday.\\n\\n\\tYour friend, John.

	Given following letter is displayed "\${docstring_1}\""""
	}

	def "with 2 docstrings"() {
		given:
			KeywordTestCase keywordTestCase = new KeywordTestCase()
			keywordTestCase.setName("Letter test")
			keywordTestCase.notifyAssociatedWithProject(project)

			KeywordTestStep step1 = new KeywordTestStep(Keyword.GIVEN, createBasicActionWord("following letter is displayed"))
			step1.setDocstring("\tDear Jack,\n" +
				"I have arrived in London this morning. Everything went well!\n" +
				"Looking forward to seeing you on Friday.\n" +
				"\n\tYour friend, John.")
		KeywordTestStep step2 = new KeywordTestStep(Keyword.AND, createBasicActionWord("following letter is displayed"))
		step2.setDocstring("\tDear Jack,\n" +
			"I have arrived in London this morning. Everything went well!\n" +
			"Looking forward to seeing you on Tuesday.\n" +
			"\n\tYour friend, John.")

			keywordTestCase.addStep(step1)
			keywordTestCase.addStep(step2)
		when:
			String result = robotScriptWriter.writeBddScript(keywordTestCase, null, true)
		then:
			result ==
"""*** Settings ***
Resource	squash_resources.resource

*** Test Cases ***
Letter test
	\${docstring_1} =	Set Variable	\\tDear Jack,\\nI have arrived in London this morning. Everything went well!\\nLooking forward to seeing you on Friday.\\n\\n\\tYour friend, John.
	\${docstring_2} =	Set Variable	\\tDear Jack,\\nI have arrived in London this morning. Everything went well!\\nLooking forward to seeing you on Tuesday.\\n\\n\\tYour friend, John.

	Given following letter is displayed "\${docstring_1}\"
	And following letter is displayed "\${docstring_2}\""""
	}

	def "with both datatable and docstring on 1 step"() {
		given:
			KeywordTestCase keywordTestCase = new KeywordTestCase()
			keywordTestCase.setName("User table test")
			keywordTestCase.notifyAssociatedWithProject(project)

			KeywordTestStep step1 = new KeywordTestStep(Keyword.WHEN, createBasicActionWord("I am on user page"))
			KeywordTestStep step2 = new KeywordTestStep(Keyword.THEN, createBasicActionWord("I can see the users"))
			step2.setDatatable("| Henry | Dupond | henry.dupond@mail.com |\n" +
				"| Louis | Dupont | louis.dupont@mail.com |\n" +
				"| Charles | Martin | charles.martin@mail.com |")
			step2.setDocstring("\tDear Mommy,\nI am fine, thank You!\n\tYour Son.")
			keywordTestCase.addStep(step1)
			keywordTestCase.addStep(step2)
		when:
			String result = robotScriptWriter.writeBddScript(keywordTestCase, null, true)
		then:
			result ==
"""*** Settings ***
Resource	squash_resources.resource

*** Test Cases ***
User table test
	\${row_1_1} =	Create List	Henry	Dupond	henry.dupond@mail.com
	\${row_1_2} =	Create List	Louis	Dupont	louis.dupont@mail.com
	\${row_1_3} =	Create List	Charles	Martin	charles.martin@mail.com
	\${datatable_1} =	Create List	\${row_1_1}	\${row_1_2}	\${row_1_3}

	When I am on user page
	Then I can see the users "\${datatable_1}\""""
	}

	def "with 1 comment"() {
		given:
			KeywordTestCase keywordTestCase = new KeywordTestCase()
			keywordTestCase.setName("Comment test")
			keywordTestCase.notifyAssociatedWithProject(project)

			KeywordTestStep step1 = new KeywordTestStep(Keyword.GIVEN, createBasicActionWord("I do something"))
			step1.setComment("the action can be anything here");

			keywordTestCase.addStep(step1)
		when:
			String result = robotScriptWriter.writeBddScript(keywordTestCase, null, true)
		then:
			result ==
"""*** Settings ***
Resource	squash_resources.resource

*** Test Cases ***
Comment test
	Given I do something
	# the action can be anything here"""
	}


	/* ----- Test Step Script Generation ----- */

	def "step script with no parameters"() {
		given:
			KeywordTestStep step = new KeywordTestStep(
				Keyword.GIVEN,
				createBasicActionWord("Today is Monday"))
		when:
			String result = robotScriptWriter.writeBddStepScript(step, 0, 0)
		then:
			result == "Given Today is Monday"

	}

	def "step script with a parameter value as free text"() {
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
			String result = robotScriptWriter.writeBddStepScript(step, 0, 0)
		then:
			result == "When It is \"10 o'clock\""
	}

	def "step script with two side by side parameters valued as free text"() {
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
			String result = robotScriptWriter.writeBddStepScript(step, 0, 0)
		then:
			result == "Given I am in \"Los Angeles\"\"United States\""
	}

	@Unroll
	def "step script with a parameter value as a number"() {
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
			String result = robotScriptWriter.writeBddStepScript(step, 0, 0)
		then:
			result == "When It is \"${number}\""
		where:
			number << ["10", "10.5", "10,5", "-10.5", "-10,5"]
	}

	def "step script with several parameters"() {
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
			String result = robotScriptWriter.writeBddStepScript(step, 0, 0)
		then:
			result == "Then it is \"9\" o'clock in \"London\" with a \${weather} weather."
	}

	def "step script using a datatable"() {
		given:
			def fragment1 = new ActionWordText("the following users are listed")
			ActionWord actionWord = new ActionWord([fragment1] as List)
			KeywordTestStep step = new KeywordTestStep(Keyword.THEN, actionWord)
			step.setDatatable("| user1 | user1@mail.com |\n| user2 | user2@mail.com |")
		when:
			String result = robotScriptWriter.writeBddStepScript(step, 2, 0)
		then:
			result == "Then the following users are listed \"\${datatable_2}\""
	}

	def "step script using a docstring"() {
		given:
			def fragment1 = new ActionWordText("the following letter is displayed")
			ActionWord actionWord = new ActionWord([fragment1] as List)
			KeywordTestStep step = new KeywordTestStep(Keyword.THEN, actionWord)
			step.setDocstring("\tDear Santa,\n I want a lot of present this year.")
		when:
			String result = robotScriptWriter.writeBddStepScript(step, 0, 2)
		then:
			result == "Then the following letter is displayed \"\${docstring_2}\""
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
