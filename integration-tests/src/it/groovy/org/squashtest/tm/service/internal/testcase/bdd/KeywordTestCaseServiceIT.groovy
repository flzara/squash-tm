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

	def messageSource = Mock(MessageSource)

	def setup(){
		keywordTestCaseService.messageSource = messageSource
	}

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

	def "Should generate a Gherkin script from a KeywordTestCase"() {
		given:
			KeywordTestCase keywordTestCase = keywordTestCaseFinder.findById(-4L)
		when:
			messageSource.getMessage("testcase.bdd.keyword.name.given",null, _ as Locale) >> "Given"
			messageSource.getMessage("testcase.bdd.keyword.name.when",null, _ as Locale) >> "When"
			messageSource.getMessage("testcase.bdd.keyword.name.then",null, _ as Locale) >> "Then"
			def res = keywordTestCaseService.writeScriptFromTestCase(keywordTestCase)
		then:
			res ==
"""# language: en
Feature: Disconnection test

	Scenario: Disconnection test
		Given I am connected
		When I sign oùt
		Then Je suis déconnecté"""
	}

	@DataSet("KeywordTestCaseServiceIT.test-case-with-step-containing-param-value-as-free-text.xml")
	def "Should generate a Gherkin script with test steps containing parameter value as free text from a KeywordTestCase"() {
		given:
		KeywordTestCase keywordTestCase = keywordTestCaseFinder.findById(-14L)
		when:
		messageSource.getMessage("testcase.bdd.keyword.name.given",null, _ as Locale) >> "Given"
		messageSource.getMessage("testcase.bdd.keyword.name.when",null, _ as Locale) >> "When"
		messageSource.getMessage("testcase.bdd.keyword.name.then",null, _ as Locale) >> "Then"
		def res = keywordTestCaseService.writeScriptFromTestCase(keywordTestCase)
		then:
		res ==
			"""# language: en
Feature: Daily test

	Scenario: Daily test
		Given Today is Monday
		When It is "8 AM"
		Then I am working"""
	}

	@DataSet("KeywordTestCaseServiceIT.test-case-with-step-containing-TC-param-value-no-dataset.xml")
	def "Should generate a Gherkin script with test steps containing parameter associated with a TC param as value from a KeywordTestCase but no dataset"() {
		given:
		KeywordTestCase keywordTestCase = keywordTestCaseFinder.findById(-14L)
		when:
		messageSource.getMessage("testcase.bdd.keyword.name.given",null, _ as Locale) >> "Given"
		messageSource.getMessage("testcase.bdd.keyword.name.when",null, _ as Locale) >> "When"
		messageSource.getMessage("testcase.bdd.keyword.name.then",null, _ as Locale) >> "Then"
		def res = keywordTestCaseService.writeScriptFromTestCase(keywordTestCase)
		then:
		res ==
			"""# language: en
Feature: Daily test

	Scenario: Daily test
		Given Today is Monday
		When It is &lt;time&gt;
		Then I am working"""
	}

	@DataSet("KeywordTestCaseServiceIT.test-case-with-step-containing-no-TC-param-value-but-dataset.xml")
	def "Should generate a Gherkin script with test steps from a KeywordTestCase with dataset but no TC param"() {
		given:
		KeywordTestCase keywordTestCase = keywordTestCaseFinder.findById(-14L)
		when:
		messageSource.getMessage("testcase.bdd.keyword.name.given",null, _ as Locale) >> "Given"
		messageSource.getMessage("testcase.bdd.keyword.name.when",null, _ as Locale) >> "When"
		messageSource.getMessage("testcase.bdd.keyword.name.then",null, _ as Locale) >> "Then"
		def res = keywordTestCaseService.writeScriptFromTestCase(keywordTestCase)
		then:
		res ==
			"""# language: en
Feature: Daily test

	Scenario: Daily test
		Given Today is Monday
		When It is "time"
		Then I am working"""
	}

	@DataSet("KeywordTestCaseServiceIT.test-case-with-step-containing-1-TC-param-value-1-dataset.xml")
	def "Should generate a Gherkin script with test steps containing parameter associated with 1 TC param value and 1 dataset"() {
		given:
		KeywordTestCase keywordTestCase = keywordTestCaseFinder.findById(-14L)
		when:
		messageSource.getMessage("testcase.bdd.keyword.name.given",null, _ as Locale) >> "Given"
		messageSource.getMessage("testcase.bdd.keyword.name.when",null, _ as Locale) >> "When"
		messageSource.getMessage("testcase.bdd.keyword.name.then",null, _ as Locale) >> "Then"
		def res = keywordTestCaseService.writeScriptFromTestCase(keywordTestCase)
		then:
		res ==
			"""# language: en
Feature: Daily test

	Scenario Outline: Daily test
		Given Today is Monday
		When It is &lt;time&gt;
		Then I am working

		@dataset1
		Examples:
		| time |
		| "12 AM" |"""
	}

	@DataSet("KeywordTestCaseServiceIT.test-case-with-step-containing-2-TC-param-value-1-dataset.xml")
	def "Should generate a Gherkin script with test steps containing parameter associated with 2 TC param value and 1 dataset"() {
		given:
		KeywordTestCase keywordTestCase = keywordTestCaseFinder.findById(-14L)
		when:
		messageSource.getMessage("testcase.bdd.keyword.name.given",null, _ as Locale) >> "Given"
		messageSource.getMessage("testcase.bdd.keyword.name.when",null, _ as Locale) >> "When"
		messageSource.getMessage("testcase.bdd.keyword.name.then",null, _ as Locale) >> "Then"
		def res = keywordTestCaseService.writeScriptFromTestCase(keywordTestCase)
		then:
		res ==
			"""# language: en
Feature: Daily test

	Scenario Outline: Daily test
		Given Today is Monday
		When It is &lt;time&gt; in &lt;place&gt;
		Then I am working

		@dataset1
		Examples:
		| place | time |
		| "Nice" | "12 AM" |"""
	}

	///////////////////////////////////////////////////////////////////////////
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
