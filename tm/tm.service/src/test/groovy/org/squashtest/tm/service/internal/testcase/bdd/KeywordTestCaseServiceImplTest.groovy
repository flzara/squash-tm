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
import org.squashtest.tm.domain.bdd.ActionWordText
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

	//TODO-QUAN
	def "Should generate a Gherkin script from a KeywordTestCase"() {
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
}
