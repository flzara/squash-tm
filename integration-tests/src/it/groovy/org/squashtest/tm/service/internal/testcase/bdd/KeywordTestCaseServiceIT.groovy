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
import org.squashtest.tm.domain.bdd.BddImplementationTechnology
import org.squashtest.tm.domain.testcase.KeywordTestCase
import org.squashtest.tm.domain.testcase.KeywordTestStep
import org.squashtest.tm.service.testcase.bdd.KeywordTestCaseFinder
import org.squashtest.tm.service.testcase.bdd.KeywordTestCaseService
import org.unitils.dbunit.annotation.DataSet
import spock.lang.Unroll
import spock.unitils.UnitilsSupport

import javax.inject.Inject

import static org.squashtest.tm.domain.bdd.BddImplementationTechnology.CUCUMBER
import static org.squashtest.tm.domain.bdd.BddImplementationTechnology.ROBOT

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

	def setupRobotProject(KeywordTestCase tc) {
		tc.getProject().bddImplementationTechnology = BddImplementationTechnology.ROBOT
	}

	/* ----- Cucumber Scripts ----- */
	def "Should generate a Gherkin script without test steps from a KeywordTestCase"() {
		given:
			KeywordTestCase keywordTestCase = keywordTestCaseFinder.findById(-6L)
		when:
			messageSource.getMessage("testcase.bdd.script.label.feature",null, _ as Locale) >> "Feature: "
			def res = keywordTestCaseService.writeScriptFromTestCase(keywordTestCase, true)
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
		    messageSource.getMessage("testcase.bdd.script.label.feature",null, _ as Locale) >> "Feature: "
			messageSource.getMessage("testcase.bdd.script.label.scenario",null, _ as Locale) >> "Scenario: "
			def res = keywordTestCaseService.writeScriptFromTestCase(keywordTestCase, true)
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
		messageSource.getMessage("testcase.bdd.script.label.feature",null, _ as Locale) >> "Feature: "
		messageSource.getMessage("testcase.bdd.script.label.scenario",null, _ as Locale) >> "Scenario: "
		def res = keywordTestCaseService.writeScriptFromTestCase(keywordTestCase, true)
		then:
		res ==
			"""# language: en
Feature: Daily test

	Scenario: Daily test
		Given Today is Monday
		When It is "8 AM"
		Then I am working"""
	}

	@DataSet("KeywordTestCaseServiceIT.test-case-with-step-containing-param-value-as-number.xml")
	def "Should generate a Gherkin script with test steps containing parameter value as number from a KeywordTestCase"() {
		given:
		KeywordTestCase keywordTestCase = keywordTestCaseFinder.findById(-14L)
		when:
		messageSource.getMessage("testcase.bdd.keyword.name.given",null, _ as Locale) >> "Given"
		messageSource.getMessage("testcase.bdd.keyword.name.when",null, _ as Locale) >> "When"
		messageSource.getMessage("testcase.bdd.keyword.name.then",null, _ as Locale) >> "Then"
		messageSource.getMessage("testcase.bdd.script.label.feature",null, _ as Locale) >> "Feature: "
		messageSource.getMessage("testcase.bdd.script.label.scenario",null, _ as Locale) >> "Scenario: "
		def res = keywordTestCaseService.writeScriptFromTestCase(keywordTestCase, true)
		then:
		res ==
			"""# language: en
Feature: Daily test

	Scenario: Daily test
		Given Today is Monday
		When It is 8.5
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
		messageSource.getMessage("testcase.bdd.script.label.feature",null, _ as Locale) >> "Feature: "
		messageSource.getMessage("testcase.bdd.script.label.scenario",null, _ as Locale) >> "Scenario: "
		def res = keywordTestCaseService.writeScriptFromTestCase(keywordTestCase, true)
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
		messageSource.getMessage("testcase.bdd.script.label.feature",null, _ as Locale) >> "Feature: "
		messageSource.getMessage("testcase.bdd.script.label.scenario",null, _ as Locale) >> "Scenario: "
		def res = keywordTestCaseService.writeScriptFromTestCase(keywordTestCase, true)
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
		messageSource.getMessage("testcase.bdd.script.label.feature",null, _ as Locale) >> "Feature: "
		messageSource.getMessage("testcase.bdd.script.label.scenario-outline",null, _ as Locale) >> "Scenario Outline: "
		messageSource.getMessage("testcase.bdd.script.label.examples",null, _ as Locale) >> "Examples:"
		def res = keywordTestCaseService.writeScriptFromTestCase(keywordTestCase, true)
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

	@DataSet("KeywordTestCaseServiceIT.test-case-with-step-containing-1-TC-param-value-1-dataset.xml")
	def "Should generate a Gherkin script with test steps containing parameter associated with 1 TC param value and 1 dataset without escaping arrow symbols"() {
		given:
		KeywordTestCase keywordTestCase = keywordTestCaseFinder.findById(-14L)
		when:
		messageSource.getMessage("testcase.bdd.keyword.name.given",null, _ as Locale) >> "Given"
		messageSource.getMessage("testcase.bdd.keyword.name.when",null, _ as Locale) >> "When"
		messageSource.getMessage("testcase.bdd.keyword.name.then",null, _ as Locale) >> "Then"
		messageSource.getMessage("testcase.bdd.script.label.feature",null, _ as Locale) >> "Feature: "
		messageSource.getMessage("testcase.bdd.script.label.scenario-outline",null, _ as Locale) >> "Scenario Outline: "
		messageSource.getMessage("testcase.bdd.script.label.examples",null, _ as Locale) >> "Examples:"
		def res = keywordTestCaseService.writeScriptFromTestCase(keywordTestCase, false)
		then:
		res ==
			"""# language: en
Feature: Daily test

	Scenario Outline: Daily test
		Given Today is Monday
		When It is <time>
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
		messageSource.getMessage("testcase.bdd.script.label.feature",null, _ as Locale) >> "Feature: "
		messageSource.getMessage("testcase.bdd.script.label.scenario-outline",null, _ as Locale) >> "Scenario Outline: "
		messageSource.getMessage("testcase.bdd.script.label.examples",null, _ as Locale) >> "Examples:"
		def res = keywordTestCaseService.writeScriptFromTestCase(keywordTestCase, true)
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
		| "Nice" | 12 |"""
	}

	@DataSet("KeywordTestCaseServiceIT.test-case-with-step-containing-2-TC-param-value-1-dataset-name-with-spaces.xml")
	def "Should generate a Gherkin script with test steps containing parameter associated with 2 TC param value and 1 dataset whose name contains spaces"() {
		given:
		KeywordTestCase keywordTestCase = keywordTestCaseFinder.findById(-14L)
		when:
		messageSource.getMessage("testcase.bdd.keyword.name.given",null, _ as Locale) >> "Given"
		messageSource.getMessage("testcase.bdd.keyword.name.when",null, _ as Locale) >> "When"
		messageSource.getMessage("testcase.bdd.keyword.name.then",null, _ as Locale) >> "Then"
		messageSource.getMessage("testcase.bdd.script.label.feature",null, _ as Locale) >> "Feature: "
		messageSource.getMessage("testcase.bdd.script.label.scenario-outline",null, _ as Locale) >> "Scenario Outline: "
		messageSource.getMessage("testcase.bdd.script.label.examples",null, _ as Locale) >> "Examples:"
		def res = keywordTestCaseService.writeScriptFromTestCase(keywordTestCase, true)
		then:
		res ==
			"""# language: en
Feature: Daily test

	Scenario Outline: Daily test
		Given Today is Monday
		When It is &lt;time&gt; in &lt;place&gt;
		Then I am working

		@dataset_1
		Examples:
		| place | time |
		| "Nice" | 12 |"""
	}

	/* ----- Robot Framework ----- */
	def "Should generate a Robot script without test steps from a KeywordTestCase"() {
		given:
			KeywordTestCase keywordTestCase = keywordTestCaseFinder.findById(-6L)
			setupRobotProject(keywordTestCase)
		when:
			def res = keywordTestCaseService.writeScriptFromTestCase(keywordTestCase, true)
		then:
			res ==
"""*** Settings ***
Resource	squash_resources.resource

*** Test Cases ***
empty test"""
	}

	def "Should generate a Robot script from a KeywordTestCase"() {
		given:
			KeywordTestCase keywordTestCase = keywordTestCaseFinder.findById(-4L)
			setupRobotProject(keywordTestCase)
		when:
			def res = keywordTestCaseService.writeScriptFromTestCase(keywordTestCase, true)
		then:
			res ==
"""*** Settings ***
Resource	squash_resources.resource

*** Test Cases ***
Disconnection test
	Given I am connected
	When I sign oùt
	Then Je suis déconnecté"""
	}

	@DataSet("KeywordTestCaseServiceIT.test-case-with-step-containing-param-value-as-free-text.xml")
	def "Should generate a Robot script with test steps containing parameter value as free text from a KeywordTestCase"() {
		given:
			KeywordTestCase keywordTestCase = keywordTestCaseFinder.findById(-14L)
			setupRobotProject(keywordTestCase)
		when:
			def res = keywordTestCaseService.writeScriptFromTestCase(keywordTestCase, true)
		then:
			res ==
"""*** Settings ***
Resource	squash_resources.resource

*** Test Cases ***
Daily test
	Given Today is Monday
	When It is "8 AM"
	Then I am working"""
	}

	@DataSet("KeywordTestCaseServiceIT.test-case-with-step-containing-param-value-as-number.xml")
	def "Should generate a Robot script with test steps containing parameter value as number from a KeywordTestCase"() {
		given:
			KeywordTestCase keywordTestCase = keywordTestCaseFinder.findById(-14L)
			setupRobotProject(keywordTestCase)
		when:
			def res = keywordTestCaseService.writeScriptFromTestCase(keywordTestCase, true)
		then:
			res ==
"""*** Settings ***
Resource	squash_resources.resource

*** Test Cases ***
Daily test
	Given Today is Monday
	When It is "8.5"
	Then I am working"""
	}

	@DataSet("KeywordTestCaseServiceIT.test-case-with-step-containing-TC-param-value-no-dataset.xml")
	def "Should generate a Robot script with test steps containing parameter associated with a TC param as value from a KeywordTestCase but no dataset"() {
		given:
			KeywordTestCase keywordTestCase = keywordTestCaseFinder.findById(-14L)
			setupRobotProject(keywordTestCase)
		when:
			def res = keywordTestCaseService.writeScriptFromTestCase(keywordTestCase, true)
		then:
			res ==
"""*** Settings ***
Resource	squash_resources.resource

*** Test Cases ***
Daily test
	Given Today is Monday
	When It is \${time}
	Then I am working"""
	}

	@DataSet("KeywordTestCaseServiceIT.test-case-with-step-containing-no-TC-param-value-but-dataset.xml")
	def "Should generate a Robot script with test steps from a KeywordTestCase with dataset but no TC param"() {
		given:
			KeywordTestCase keywordTestCase = keywordTestCaseFinder.findById(-14L)
			setupRobotProject(keywordTestCase)
		when:
			def res = keywordTestCaseService.writeScriptFromTestCase(keywordTestCase, true)
		then:
			res ==
"""*** Settings ***
Resource	squash_resources.resource

*** Test Cases ***
Daily test
	Given Today is Monday
	When It is "time"
	Then I am working"""
	}

	@DataSet("KeywordTestCaseServiceIT.test-case-with-step-containing-1-TC-param-value-1-dataset.xml")
	def "Should generate a Robot script with test steps containing parameter associated with 1 TC param value and 1 dataset"() {
		given:
			KeywordTestCase keywordTestCase = keywordTestCaseFinder.findById(-14L)
			setupRobotProject(keywordTestCase)
		when:
			def res = keywordTestCaseService.writeScriptFromTestCase(keywordTestCase, true)
		then:
			res ==
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

	@DataSet("KeywordTestCaseServiceIT.test-case-with-step-containing-1-TC-param-value-1-dataset.xml")
	def "Should generate a Robot script with test steps containing parameter associated with 1 TC param value and 1 dataset without escaping arrow symbols"() {
		given:
			KeywordTestCase keywordTestCase = keywordTestCaseFinder.findById(-14L)
			setupRobotProject(keywordTestCase)
		when:
		def res = keywordTestCaseService.writeScriptFromTestCase(keywordTestCase, false)
		then:
			res	==
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

	@DataSet("KeywordTestCaseServiceIT.test-case-with-step-containing-2-TC-param-value-1-dataset.xml")
	def "Should generate a Robot script with test steps containing parameter associated with 2 TC param value and 1 dataset"() {
		given:
			KeywordTestCase keywordTestCase = keywordTestCaseFinder.findById(-14L)
			setupRobotProject(keywordTestCase)
		when:
			def res = keywordTestCaseService.writeScriptFromTestCase(keywordTestCase, true)
		then:
			res ==
"""*** Settings ***
Resource	squash_resources.resource
Library		squash_tf.TFParamService

*** Test Cases ***
Daily test
	\${place} =	Get Test Param	DS_place
	\${time} =	Get Test Param	DS_time

	Given Today is Monday
	When It is \${time} in \${place}
	Then I am working"""
	}

	@DataSet("KeywordTestCaseServiceIT.test-case-with-step-containing-2-TC-param-value-used-twice.xml")
	def "Should generate a Robot script with test steps containing parameter associated with 1 TC param value used twice and 1 dataset"() {
		given:
			KeywordTestCase keywordTestCase = keywordTestCaseFinder.findById(-14L)
			setupRobotProject(keywordTestCase)
		when:
			def res = keywordTestCaseService.writeScriptFromTestCase(keywordTestCase, true)
		then:
			res ==
"""*** Settings ***
Resource	squash_resources.resource
Library		squash_tf.TFParamService

*** Test Cases ***
Daily test
	\${place} =	Get Test Param	DS_place
	\${time} =	Get Test Param	DS_time

	Given Today is Monday
	When It is \${time} in \${place}
	Then I am working at \${time} in \${place}"""
	}

	@DataSet("KeywordTestCaseServiceIT.test-case-with-step-containing-datatable.xml")
	def "Should generate a Robot script with a test step containing a datatable"() {
		given:
			KeywordTestCase keywordTestCase = keywordTestCaseFinder.findById(-1L)
			setupRobotProject(keywordTestCase)
			String datatable = "| Henry | Dupont | henry.dupont@mail.com |\n" +
				"| Louis | Dupond | louis.dupond@mail.com |\n" +
				"| Charles | Martin | charles.martin@mail.com |"
			((KeywordTestStep) keywordTestCase.getSteps().get(0)).setDatatable(datatable)
		when:
			def res = keywordTestCaseService.writeScriptFromTestCase(keywordTestCase, true)
		then:
		res ==
"""*** Settings ***
Resource	squash_resources.resource

*** Test Cases ***
User table test
	\${row_1_1}=	Create List	Henry	Dupont	henry.dupont@mail.com
	\${row_1_2}=	Create List	Louis	Dupond	louis.dupond@mail.com
	\${row_1_3}=	Create List	Charles	Martin	charles.martin@mail.com
	\${datatable_1}=	Create List	\${row_1_1}	\${row_1_2}	\${row_1_3}

	Given following users are listed "\${datatable_1}\""""
	}

	/* ----- File System Methods ----- */

	@Unroll("Should create a file name for #bddTechnology")
	def "Should create a File name for a Keyword Test case"() {
		given: "a keyword test case"
			KeywordTestCase keywordTestCase = keywordTestCaseFinder.findById(-4L)
			keywordTestCase.project.bddImplementationTechnology = bddTechnology
		when: "I create the file name"
			def result = keywordTestCaseService.createFileName(keywordTestCase)
		then: "the result is as expected"
			result == expectedResult
		where:
			bddTechnology	| expectedResult
			CUCUMBER 		| "-4_Disconnection_test.feature"
			ROBOT 			| "-4_Disconnection_test.robot"
	}

	@Unroll("Should create a backup file name for #bddTechnology")
	def "Should create a backup File name for a Keyword Test case"(){
		given: "a keyword test case"
			KeywordTestCase keywordTestCase = keywordTestCaseFinder.findById(-4L)
			keywordTestCase.project.bddImplementationTechnology = bddTechnology
		when: "I create the backup file name"
			def result = keywordTestCaseService.createBackupFileName(keywordTestCase)
		then: "the result is as expected"
			result == expectedResult
		where:
			bddTechnology	| expectedResult
			CUCUMBER		| "-4.feature"
			ROBOT			| "-4.robot"
	}

	@Unroll("Should build name pattern for #bddTechnology")
	def "Should build Pattern for a Keyword Test case"(){
		given: "a keyword test case"
			KeywordTestCase keywordTestCase = keywordTestCaseFinder.findById(-4L)
			keywordTestCase.project.bddImplementationTechnology = bddTechnology
		when: "I build the file name pattern"
			def result = keywordTestCaseService.buildFilenameMatchPattern(keywordTestCase)
		then: "the result is as expected"
			result == expectedResult
		where:
			bddTechnology	| expectedResult
			CUCUMBER		| "-4(_.*)?\\.feature"
			ROBOT			| "-4(_.*)?\\.robot"
	}
}
