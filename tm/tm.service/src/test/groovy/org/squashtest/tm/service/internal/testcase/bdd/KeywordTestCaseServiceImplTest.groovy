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
import org.squashtest.tm.domain.bdd.Keyword
import org.squashtest.tm.domain.testcase.KeywordTestCase
import org.squashtest.tm.domain.testcase.KeywordTestStep
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.service.internal.repository.TestCaseDao
import org.squashtest.tm.service.testcase.bdd.KeywordTestCaseService
import spock.lang.Specification

class KeywordTestCaseServiceImplTest extends Specification {

	KeywordTestCaseService keywordTestCaseService = new KeywordTestCaseServiceImpl()

	def "Should generate a Gherkin script without test steps from a KeywordTestCase"() {
		given:
		KeywordTestCase keywordTestCase = new KeywordTestCase()
		keywordTestCase.setName("Disconnection test")

		when:
		String result = keywordTestCaseService.writeScriptFromTestCase(keywordTestCase)

		then:
		result == "# language: en\nFeature: Disconnection test"
	}

	def "Should generate a Gherkin script from a KeywordTestCase"() {
		given:
		KeywordTestCase keywordTestCase = new KeywordTestCase()
		keywordTestCase.setName("Disconnection test")

		KeywordTestStep step1 = new KeywordTestStep(Keyword.GIVEN, new ActionWord("I am connécted"))
		KeywordTestStep step2 = new KeywordTestStep(Keyword.WHEN, new ActionWord("I sign oùt"))
		KeywordTestStep step3 = new KeywordTestStep(Keyword.THEN, new ActionWord("I am dîsconnect&d"))

		keywordTestCase.addStep(step1)
		keywordTestCase.addStep(step2)
		keywordTestCase.addStep(step3)

		when:
		String result = keywordTestCaseService.writeScriptFromTestCase(keywordTestCase)

		then:
		result ==
"""# language: en
Feature: Disconnection test

	Scenario: Disconnection test
		GIVEN I am connécted
		WHEN I sign oùt
		THEN I am dîsconnect&d"""
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
