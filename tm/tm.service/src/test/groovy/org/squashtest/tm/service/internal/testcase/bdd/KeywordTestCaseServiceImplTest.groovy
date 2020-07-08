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
import org.squashtest.tm.domain.bdd.BddScriptLanguage
import org.squashtest.tm.domain.bdd.Keyword
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.domain.testcase.Dataset
import org.squashtest.tm.domain.testcase.DatasetParamValue
import org.squashtest.tm.domain.testcase.KeywordTestCase
import org.squashtest.tm.domain.testcase.KeywordTestStep
import org.squashtest.tm.domain.testcase.Parameter
import org.squashtest.tm.service.testcase.bdd.KeywordTestCaseService
import spock.lang.Specification
import spock.lang.Unroll

class KeywordTestCaseServiceImplTest extends Specification {

	KeywordTestCaseService keywordTestCaseService = new KeywordTestCaseServiceImpl()
	def messageSource = Mock(MessageSource)

	def setup(){
		keywordTestCaseService.messageSource = messageSource
	}

	def createBasicActionWord(String singleFragment) {
		def fragment = new ActionWordText(singleFragment)
		return new ActionWord([fragment] as List)
	}

	def createMockKeywordTestCase(String name) {
		KeywordTestCase keywordTestCase = Mock()
		Project project = Mock()
		project.getBddScriptLanguage() >> BddScriptLanguage.ENGLISH
		keywordTestCase.getProject() >> project
		keywordTestCase.getName() >> name
		return keywordTestCase
	}

	def "Should generate a Gherkin script without test steps from a KeywordTestCase"() {
		given:
		KeywordTestCase keywordTestCase = createMockKeywordTestCase("Disconnection test")
		keywordTestCase.getSteps() >> []

		when:
		2 * messageSource.getMessage(*_) >>> ["# language: ", "Feature: "]
		String result = keywordTestCaseService.writeScriptFromTestCase(keywordTestCase)

		then:
		result == "# language: en\nFeature: Disconnection test"
	}

	def "Should generate a Gherkin script with test steps containing only text from a KeywordTestCase"() {
		given:
		KeywordTestCase keywordTestCase = createMockKeywordTestCase("Disconnection test")
		keywordTestCase.getDatasets() >> []

		KeywordTestStep step1 = new KeywordTestStep(Keyword.GIVEN, createBasicActionWord("I am connécted"))
		KeywordTestStep step2 = new KeywordTestStep(Keyword.WHEN, createBasicActionWord("I sign oùt"))
		KeywordTestStep step3 = new KeywordTestStep(Keyword.THEN, createBasicActionWord("I am dîsconnect&d"))

		keywordTestCase.getSteps() >> [step1, step2, step3]

		when:
		6 * messageSource.getMessage(*_) >>> ["Given", "When", "Then", "Scenario: ", "# language: ", "Feature: "]
		String result = keywordTestCaseService.writeScriptFromTestCase(keywordTestCase)

		then:
		result ==
"""# language: en
Feature: Disconnection test

	Scenario: Disconnection test
		Given I am connécted
		When I sign oùt
		Then I am dîsconnect&d"""
	}

	def "Should generate a Gherkin script with test steps containing parameter value as free text from a KeywordTestCase"() {
		given:
		KeywordTestCase keywordTestCase = createMockKeywordTestCase("Daily test")
		keywordTestCase.getDatasets() >> []

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
		keywordTestCase.getSteps() >> [step1, step2, step3]

		when:
		6 * messageSource.getMessage(*_) >>> ["Given", "When", "Then", "Scenario: ", "# language: ", "Feature: "]
		String result = keywordTestCaseService.writeScriptFromTestCase(keywordTestCase)

		then:
		result ==
			"""# language: en
Feature: Daily test

	Scenario: Daily test
		Given Today is Monday
		When It is "10 o'clock"
		Then I am working"""
	}

	@Unroll
	def "Should generate a Gherkin script with test steps containing parameter value as a number from a KeywordTestCase"() {
		given:
		KeywordTestCase keywordTestCase = createMockKeywordTestCase("Daily test")
		keywordTestCase.getDatasets() >> []

		KeywordTestStep step1 = new KeywordTestStep(Keyword.GIVEN, createBasicActionWord("Today is Monday"))

		def fragment1 = new ActionWordText("It is ")
		def fragment2 = new ActionWordParameterMock(-1L, "param1", "12 o'clcock")
		def value1 = new ActionWordParameterValue(word)
		value1.setActionWordParam(fragment2)
		ActionWord actionWord2 = new ActionWord([fragment1, fragment2] as List)
		KeywordTestStep step2 = new KeywordTestStep(Keyword.WHEN, actionWord2)
		List<ActionWordParameterValue> paramValues = [value1]
		step2.setParamValues(paramValues)

		keywordTestCase.getSteps() >> [step1, step2]

		when:
		5 * messageSource.getMessage(*_) >>> ["Given", "When", "Scenario: ", "# language: ", "Feature: "]
		String result = keywordTestCaseService.writeScriptFromTestCase(keywordTestCase)

		then:
		result ==
"""# language: en
Feature: Daily test

	Scenario: Daily test
		Given Today is Monday
		When It is """+word

		where:
		word << ["10", "10.5", "10,5", "-10.5", "-10,5"]
	}

	def "Should generate a Gherkin script with test steps containing parameter associated with a TC param as value from a KeywordTestCase but no dataset"() {
		given:
		KeywordTestCase keywordTestCase = createMockKeywordTestCase("Daily test")
		keywordTestCase.getDatasets() >> []

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

		keywordTestCase.getSteps() >> [step1, step2, step3]

		when:
		6 * messageSource.getMessage(*_) >>> ["Given", "When", "Then", "Scenario: ", "# language: ", "Feature: "]
		String result = keywordTestCaseService.writeScriptFromTestCase(keywordTestCase)

		then:
		result ==
			"""# language: en
Feature: Daily test

	Scenario: Daily test
		Given Today is Monday
		When It is &lt;time&gt;
		Then I am working"""
	}

	def "Should generate a Gherkin script with test steps from a KeywordTestCase with dataset but no param between <>"() {
		given:
		KeywordTestCase keywordTestCase = createMockKeywordTestCase("Daily test")

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

		keywordTestCase.getSteps() >> [step1, step2, step3]

		def tcParam =  new Parameter("tcParam")
		keywordTestCase.getParameters() >> [tcParam]
		def dataset =  new Dataset()
		dataset.setName("dataset1")
		keywordTestCase.getDatasets() >> [dataset]
		def value =  new DatasetParamValue(tcParam, dataset,"9 AM")
		dataset.addParameterValue(value)

		when:
		6 * messageSource.getMessage(*_) >>> ["Given", "When", "Then", "Scenario: ", "# language: ", "Feature: "]
		String result = keywordTestCaseService.writeScriptFromTestCase(keywordTestCase)

		then:
		result ==
			"""# language: en
Feature: Daily test

	Scenario: Daily test
		Given Today is Monday
		When It is "time"
		Then I am working"""
	}

	def "Should generate a Gherkin script with test steps from a KeywordTestCase with 1 dataset and 1 param between <>"() {
		given:
		KeywordTestCase keywordTestCase = createMockKeywordTestCase("Daily test")

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

		keywordTestCase.getSteps() >> [step1, step2, step3]

		def tcParam =  new Parameter("time", keywordTestCase)
		def dataset =  new Dataset()
		dataset.setName("dataset1")
		keywordTestCase.getDatasets() >> [dataset]
		def value =  new DatasetParamValue(tcParam, dataset,"9 AM")
		dataset.addParameterValue(value)

		when:
		7 * messageSource.getMessage(*_) >>> ["Given", "When", "Then", "Scenario Outline: ", "Examples:", "# language: ", "Feature: "]
		String result = keywordTestCaseService.writeScriptFromTestCase(keywordTestCase)

		then:
		result ==
"""# language: en
Feature: Daily test

	Scenario Outline: Daily test
		Given Today is Monday
		When It is &lt;time&gt;
		Then I am working

		@dataset1
		Examples:
		| time |
		| "9 AM" |"""
	}

	def "Should generate a Gherkin script with test steps from a KeywordTestCase with 1 dataset and 2 param between <>"() {
		given:
		KeywordTestCase keywordTestCase = createMockKeywordTestCase("Daily test")

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

		keywordTestCase.getSteps() >> [step1, step2, step3]

		def tcParam1 =  new Parameter("time", keywordTestCase)
		def tcParam2 =  new Parameter("place", keywordTestCase)
		def dataset =  new Dataset()
		dataset.setName("dataset1")
		keywordTestCase.getDatasets() >> [dataset]
		def paramValue1 =  new DatasetParamValue(tcParam1, dataset,"9 AM")
		def paramValue2 =  new DatasetParamValue(tcParam2, dataset,"London")
		dataset.parameterValues = [paramValue1, paramValue2]

		when:
		7 * messageSource.getMessage(*_) >>> ["Given", "When", "Then", "Scenario Outline: ", "Examples:", "# language: ", "Feature: "]
		String result = keywordTestCaseService.writeScriptFromTestCase(keywordTestCase)

		then:
		result ==
			"""# language: en
Feature: Daily test

	Scenario Outline: Daily test
		Given Today is Monday
		When It is &lt;time&gt; in &lt;place&gt;
		Then I am working

		@dataset1
		Examples:
		| place | time |
		| "London" | "9 AM" |"""
	}

	def "Should generate a Gherkin script with test steps from a KeywordTestCase with 1 dataset and 2 param between <> with values as number"() {
		given:
		KeywordTestCase keywordTestCase = createMockKeywordTestCase("Count test")

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

		keywordTestCase.getSteps() >> [step1, step2, step3]

		def tcParam1 =  new Parameter("total", keywordTestCase)
		def tcParam2 =  new Parameter("less", keywordTestCase)
		def tcParam3 =  new Parameter("left", keywordTestCase)
		def dataset =  new Dataset()
		dataset.setName("dataset1")
		keywordTestCase.getDatasets() >> [dataset]
		def paramValue1 =  new DatasetParamValue(tcParam1, dataset,"5")
		def paramValue2 =  new DatasetParamValue(tcParam2, dataset,"3")
		def paramValue3 =  new DatasetParamValue(tcParam3, dataset,"two")
		dataset.parameterValues = [paramValue1, paramValue2, paramValue3]

		when:
		7 * messageSource.getMessage(*_) >>> ["Given", "When", "Then", "Scenario Outline: ", "Examples:", "# language: ", "Feature: "]
		String result = keywordTestCaseService.writeScriptFromTestCase(keywordTestCase)

		then:
		result ==
			"""# language: en
Feature: Count test

	Scenario Outline: Count test
		Given I buy &lt;total&gt; tickets
		When I give &lt;less&gt; to my friend
		Then I still have &lt;left&gt; tickets

		@dataset1
		Examples:
		| left | less | total |
		| "two" | 3 | 5 |"""
	}

	def "Should generate a Gherkin script with test steps from a KeywordTestCase with 1 dataset whose name contains spaces"() {
		given:
		KeywordTestCase keywordTestCase = createMockKeywordTestCase("Count test")

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

		keywordTestCase.getSteps() >> [step1, step2, step3]

		def tcParam1 =  new Parameter("total", keywordTestCase)
		def tcParam2 =  new Parameter("less", keywordTestCase)
		def tcParam3 =  new Parameter("left", keywordTestCase)
		def dataset =  new Dataset()
		dataset.setName("  dataset   1    ")
		keywordTestCase.getDatasets() >> [dataset]
		def paramValue1 =  new DatasetParamValue(tcParam1, dataset,"5")
		def paramValue2 =  new DatasetParamValue(tcParam2, dataset,"3")
		def paramValue3 =  new DatasetParamValue(tcParam3, dataset,"two")
		dataset.parameterValues = [paramValue1, paramValue2, paramValue3]

		when:
		7 * messageSource.getMessage(*_) >>> ["Given", "When", "Then", "Scenario Outline: ", "Examples:", "# language: ", "Feature: "]
		String result = keywordTestCaseService.writeScriptFromTestCase(keywordTestCase)

		then:
		result ==
			"""# language: en
Feature: Count test

	Scenario Outline: Count test
		Given I buy &lt;total&gt; tickets
		When I give &lt;less&gt; to my friend
		Then I still have &lt;left&gt; tickets

		@dataset_1
		Examples:
		| left | less | total |
		| "two" | 3 | 5 |"""
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	def "Should create a File name for a Keyword Test case"(){
		given:
		def keywordTestCase = new KeywordTCMock(777L, "Test de Déconnexion")

		when:
		def result = keywordTestCaseService.createFileName(keywordTestCase)

		then:
		result == "777_Test_de_Deconnexion.feature"
	}

	def "Should create a backup File name for a Keyword Test case"(){
		given:
		def keywordTestCase = new KeywordTCMock(777L)

		when:
		def result = keywordTestCaseService.createBackupFileName(keywordTestCase)

		then:
		result == "777.feature"
	}

	def "Should build Pattern for a Keyword Test case"(){
		given:
		def keywordTestCase = new KeywordTCMock(777L, "Test de Déconnexion")

		when:
		def result = keywordTestCaseService.buildFilenameMatchPattern(keywordTestCase)

		then:
		result == "777(_.*)?\\.feature"
	}

	class KeywordTCMock extends KeywordTestCase {
		private Long id

		KeywordTCMock(Long id) {
			this.id = id
		}

		KeywordTCMock(Long id, String name) {
			this.setName(name)
			this.id = id
		}

		Long getId() {
			return id
		}

		void setId(Long id) {
			this.id = id
		}
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
}
