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
import org.squashtest.tm.domain.bdd.BddImplementationTechnology
import org.squashtest.tm.domain.bdd.BddScriptLanguage
import org.squashtest.tm.domain.bdd.Keyword
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.domain.testcase.KeywordTestCase
import org.squashtest.tm.domain.testcase.KeywordTestStep
import org.squashtest.tm.service.testcase.bdd.KeywordTestCaseService
import spock.lang.Specification

class KeywordTestCaseServiceImplTest extends Specification {

	KeywordTestCaseService keywordTestCaseService = new KeywordTestCaseServiceImpl()
	def messageSource = Mock(MessageSource)

	def setup(){
		keywordTestCaseService.messageSource = messageSource
	}

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

	def "Should write a Cucumber script from a KeywordTestCase"() {
		given: "A Project configured with Cucumber technology and German language"
			Project project = new Project()
			project.setBddImplementationTechnology(BddImplementationTechnology.CUCUMBER)
			project.setBddScriptLanguage(BddScriptLanguage.ENGLISH)
		and: "A KeywordTestCase with some steps in this Project"
			KeywordTestCase testCase = new KeywordTestCase()
			testCase.setName("I love fruit")
			testCase.notifyAssociatedWithProject(project)

			def fragment1 = new ActionWordText("I have several ")
			def fragment2 = new ActionWordParameterMock(-1L, "fruit", "apples")
			def fragment3 = new ActionWordText(" in my basket")
			def actionWord1 = new ActionWord([fragment1, fragment2, fragment3])
			def value2 = new ActionWordParameterValue("<fruit>")
			value2.setActionWordParam(fragment2)
			KeywordTestStep step1 = new KeywordTestStep(Keyword.GIVEN, actionWord1)
			step1.setParamValues([value2])

			def fragment4 = new ActionWordText("I eat ")
			def fragment5 = new ActionWordParameterMock(-2L, "amount", "1")
			def fragment6 = new ActionWordText(" kilograms")
			def actionWord2 = new ActionWord([fragment4, fragment5, fragment6])
			def value5 = new ActionWordParameterValue("0.7")
			value5.setActionWordParam(fragment5)
			KeywordTestStep step2 = new KeywordTestStep(Keyword.WHEN, actionWord2)
			step2.setParamValues([value5])

			def fragment7 = new ActionWordText("I am ")
			def fragment8 = new ActionWordParameterMock(-3L, "mood", "fine")
			def fragment9 = new ActionWordText(" in my life")
			def actionWord3 = new ActionWord([fragment7, fragment8, fragment9])
			def value8 = new ActionWordParameterValue("happy")
			value8.setActionWordParam(fragment8)
			KeywordTestStep step3 = new KeywordTestStep(Keyword.THEN, actionWord3)
			step3.setParamValues([value8])

			testCase.addStep(step1)
			testCase.addStep(step2)
			testCase.addStep(step3)
		and:
			"I mock the Message Source"
			5 * messageSource.getMessage(*_) >>> ["Given", "When", "Then", "Scenario: ", "Feature: "]
		when: "I write the Script with the Service and without escaping the arrow symbols"
			def result = keywordTestCaseService.writeScriptFromTestCase(testCase, false)
		then: "I should obtain the following script"
			result ==
"""# language: en
Feature: I love fruit

	Scenario: I love fruit
		Given I have several <fruit> in my basket
		When I eat 0.7 kilograms
		Then I am "happy" in my life"""
	}

	def "Should write a Robot script from a KeywordTestCase"() {
		given: "A Project configured with Cucumber technology and German language"
			Project project = new Project()
			project.setBddImplementationTechnology(BddImplementationTechnology.ROBOT)
			project.setBddScriptLanguage(BddScriptLanguage.ENGLISH)
			and: "A KeywordTestCase with some steps in this Project"
			KeywordTestCase testCase = new KeywordTestCase()
			testCase.setName("I love fruit")
			testCase.notifyAssociatedWithProject(project)

			def fragment1 = new ActionWordText("I have several ")
			def fragment2 = new ActionWordParameterMock(-1L, "fruit", "apples")
			def fragment3 = new ActionWordText(" in my basket")
			def actionWord1 = new ActionWord([fragment1, fragment2, fragment3])
			def value2 = new ActionWordParameterValue("<fruit>")
			value2.setActionWordParam(fragment2)
			KeywordTestStep step1 = new KeywordTestStep(Keyword.GIVEN, actionWord1)
			step1.setParamValues([value2])

			def fragment4 = new ActionWordText("I eat ")
			def fragment5 = new ActionWordParameterMock(-2L, "amount", "1")
			def fragment6 = new ActionWordText(" kilograms")
			def actionWord2 = new ActionWord([fragment4, fragment5, fragment6])
			def value5 = new ActionWordParameterValue("0.7")
			value5.setActionWordParam(fragment5)
			KeywordTestStep step2 = new KeywordTestStep(Keyword.WHEN, actionWord2)
			step2.setParamValues([value5])

			def fragment7 = new ActionWordText("I am ")
			def fragment8 = new ActionWordParameterMock(-3L, "mood", "fine")
			def fragment9 = new ActionWordText(" in my life")
			def actionWord3 = new ActionWord([fragment7, fragment8, fragment9])
			def value8 = new ActionWordParameterValue("happy")
			value8.setActionWordParam(fragment8)
			KeywordTestStep step3 = new KeywordTestStep(Keyword.THEN, actionWord3)
			step3.setParamValues([value8])

			testCase.addStep(step1)
			testCase.addStep(step2)
			testCase.addStep(step3)
		when: "I write the Script with the Service and without escaping the arrow symbols"
				def result = keywordTestCaseService.writeScriptFromTestCase(testCase, false)
				then: "I should obtain the following script"
				result ==
"""*** Settings ***
Resource	squash_resources.resource

*** Test Cases ***
I love fruit
	Given I have several \${fruit} in my basket
	When I eat "0.7" kilograms
	Then I am "happy" in my life"""

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
