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
import org.squashtest.tm.domain.bdd.BddScriptLanguage
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.domain.testcase.KeywordTestCase
import org.squashtest.tm.service.testcase.bdd.KeywordTestCaseService
import spock.lang.Specification

class KeywordTestCaseServiceImplTest extends Specification {

	KeywordTestCaseService keywordTestCaseService = new KeywordTestCaseServiceImpl()
	def messageSource = Mock(MessageSource)

	def setup(){
		keywordTestCaseService.messageSource = messageSource
	}

	def createMockKeywordTestCase(String name) {
		KeywordTestCase keywordTestCase = Mock()
		Project project = Mock()
		project.getBddScriptLanguage() >> BddScriptLanguage.ENGLISH
		keywordTestCase.getProject() >> project
		keywordTestCase.getName() >> name
		return keywordTestCase
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
