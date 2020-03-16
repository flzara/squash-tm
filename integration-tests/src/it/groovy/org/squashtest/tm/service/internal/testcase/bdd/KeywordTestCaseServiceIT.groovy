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

import org.springframework.transaction.annotation.Transactional
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.squashtest.tm.domain.testcase.KeywordTestCase
import org.squashtest.tm.service.testcase.bdd.KeywordTestCaseFinder
import org.squashtest.tm.service.testcase.bdd.KeywordTestCaseService
import org.unitils.dbunit.annotation.DataSet
import spock.unitils.UnitilsSupport

import javax.inject.Inject

@UnitilsSupport
@Transactional
@DataSet
class KeywordTestCaseServiceIT extends DbunitServiceSpecification {

	@Inject
	KeywordTestCaseFinder keywordTestCaseFinder

	@Inject
	KeywordTestCaseService keywordTestCaseService

	def "Should generate a Gherkin script without test steps from a KeywordTestCase"() {
		given:
			KeywordTestCase keywordTestCase = keywordTestCaseFinder.findById(-6L)
		when:
			def res = keywordTestCaseService.writeScriptFromTestCase(keywordTestCase)
		then:
			res ==
			"# language: en\n" +
			"Feature: empty test"
	}

	//FIXME: keyword in Keyword test step is forcefully removed as no messageSource bean is found
	def "Should generate a Gherkin script from a KeywordTestCase"() {
		given:
			KeywordTestCase keywordTestCase = keywordTestCaseFinder.findById(-4L)
		when:
			def res = keywordTestCaseService.writeScriptFromTestCase(keywordTestCase)
		then:
			res ==
"""# language: en
Feature: Disconnection test

	Scenario: Disconnection test
		 I am connected
		 I sign oùt
		 Je suis déconnecté"""
	}

	def "Should create a File name for a Keyword Test case"() {
		given:
		KeywordTestCase keywordTestCase = keywordTestCaseFinder.findById(-4L)

		when:
		def result = keywordTestCaseService.createFileName(keywordTestCase)

		then:
		result == "-4_Disconnection_test.feature"
	}

	def "Should create a backup File name for a Keyword Test case"(){
		given:
		KeywordTestCase keywordTestCase = keywordTestCaseFinder.findById(-4L)

		when:
		def result = keywordTestCaseService.createBackupFileName(keywordTestCase)

		then:
		result == "-4.feature"
	}

	def "Should build Pattern for a Keyword Test case"(){
		given:
		KeywordTestCase keywordTestCase = keywordTestCaseFinder.findById(-4L)
		when:
		def result = keywordTestCaseService.buildFilenameMatchPattern(keywordTestCase)

		then:
		result == "-4(_.*)?\\.feature"
	}
}
