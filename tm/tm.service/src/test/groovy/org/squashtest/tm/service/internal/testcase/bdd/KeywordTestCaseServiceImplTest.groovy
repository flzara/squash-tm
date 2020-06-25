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
import org.squashtest.tm.domain.testcase.Dataset
import org.squashtest.tm.domain.testcase.DatasetParamValue
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.domain.testcase.KeywordTestCase
import org.squashtest.tm.domain.testcase.KeywordTestStep
import org.squashtest.tm.domain.testcase.Parameter
import org.squashtest.tm.service.testcase.bdd.KeywordTestCaseService
import spock.lang.Specification

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

	def createMockKeywordTestCase() {
		KeywordTestCase keywordTestCase = Mock()
		Project project = Mock()
		project.getBddScriptLanguage() >> BddScriptLanguage.ENGLISH
		keywordTestCase.getProject() >> project
		keywordTestCase.getName() >> "Disconnection test"
		return keywordTestCase
	}

	def "Should generate a Gherkin script without test steps from a KeywordTestCase"() {
		given:
		KeywordTestCase keywordTestCase = createMockKeywordTestCase()
		keywordTestCase.getSteps() >> []

		when:
		String result = keywordTestCaseService.writeScriptFromTestCase(keywordTestCase)

		then:
		result == "# language: en\nFeature: Disconnection test"
	}

	def "Should generate a Gherkin script with test steps containing only text from a KeywordTestCase"() {
		given:
		KeywordTestCase keywordTestCase = createMockKeywordTestCase()

		KeywordTestStep step1 = new KeywordTestStep(Keyword.GIVEN, createBasicActionWord("I am connécted"))
		KeywordTestStep step2 = new KeywordTestStep(Keyword.WHEN, createBasicActionWord("I sign oùt"))
		KeywordTestStep step3 = new KeywordTestStep(Keyword.THEN, createBasicActionWord("I am dîsconnect&d"))

		keywordTestCase.getSteps() >> [step1, step2, step3]

		when:
		3 * messageSource.getMessage(*_) >>> ["Given", "When", "Then"]
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
		KeywordTestCase keywordTestCase = new KeywordTestCase()
		keywordTestCase.setName("Daily test")

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
		3 * messageSource.getMessage(*_) >>> ["Given", "When", "Then"]
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

	def "Should generate a Gherkin script with test steps containing parameter associated with a TC param as value from a KeywordTestCase but no dataset"() {
		given:
		KeywordTestCase keywordTestCase = new KeywordTestCase()
		keywordTestCase.setName("Daily test")

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
		3 * messageSource.getMessage(*_) >>> ["Given", "When", "Then"]
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
		KeywordTestCase keywordTestCase = new KeywordTestCase()
		keywordTestCase.setName("Daily test")

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

		def tcParam =  new Parameter("tcParam", keywordTestCase)
		def dataset =  new Dataset("dataset1", keywordTestCase)
		def value =  new DatasetParamValue(tcParam, dataset,"9 AM")
		dataset.addParameterValue(value)

		when:
		3 * messageSource.getMessage(*_) >>> ["Given", "When", "Then"]
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
		KeywordTestCase keywordTestCase = new KeywordTestCase()
		keywordTestCase.setName("Daily test")

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
		def dataset =  new Dataset("dataset1", keywordTestCase)
		def value =  new DatasetParamValue(tcParam, dataset,"9 AM")
		dataset.addParameterValue(value)

		when:
		3 * messageSource.getMessage(*_) >>> ["Given", "When", "Then"]
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
		KeywordTestCase keywordTestCase = new KeywordTestCase()
		keywordTestCase.setName("Daily test")

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
		def dataset =  new Dataset("dataset1", keywordTestCase)
		def paramValue1 =  new DatasetParamValue(tcParam1, dataset,"9 AM")
		def paramValue2 =  new DatasetParamValue(tcParam2, dataset,"London")
		dataset.parameterValues = [paramValue1, paramValue2]

		when:
		3 * messageSource.getMessage(*_) >>> ["Given", "When", "Then"]
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
