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
package org.squashtest.tm.web.internal.controller.testcase

import org.squashtest.tm.domain.testcase.KeywordTestCase
import org.squashtest.tm.domain.testcase.ScriptedTestCase
import org.squashtest.tm.domain.testcase.TestCase
import spock.lang.Specification
import spock.lang.Unroll

import static org.squashtest.tm.domain.testcase.TestCaseKind.GHERKIN
import static org.squashtest.tm.domain.testcase.TestCaseKind.KEYWORD
import static org.squashtest.tm.domain.testcase.TestCaseKind.STANDARD

class TestCaseFormModelTest extends Specification {

	def "#getTestCase() - Should create standard test case"() {
		given:
			TestCaseFormModel testCaseFormModel = new TestCaseFormModel()
			testCaseFormModel.setScriptLanguage("STANDARD")
			testCaseFormModel.setName("Standàrd 11")
			testCaseFormModel.setReference("STD|1")
			testCaseFormModel.setDescription("~ A stândàrd tést c@se ~")
		when:
			def testCase = testCaseFormModel.getTestCase()
		then:
			TestCase.class.isAssignableFrom(testCase.class)
			testCase.name == "Standàrd 11"
			testCase.reference == "STD|1"
			testCase.description == "~ A stândàrd tést c@se ~"
	}

	def "#getTestCase() - Should create scripted test case"() {
		given:
			TestCaseFormModel testCaseFormModel = new TestCaseFormModel()
			testCaseFormModel.setScriptLanguage("GHERKIN")
			testCaseFormModel.setName("ScriptedTestCase_6")
			testCaseFormModel.setReference("SC6")
			testCaseFormModel.setDescription("A scripted test case")
		when:
			def testCase = testCaseFormModel.getTestCase()
		then:
			ScriptedTestCase.class.isAssignableFrom(testCase.class)
			testCase.name == "ScriptedTestCase_6"
			testCase.reference == "SC6"
			testCase.description == "A scripted test case"
			! ((ScriptedTestCase) testCase).getScript().isEmpty()
	}

	def "#getTestCase() - Should create keyword test case"() {
		given:
			TestCaseFormModel testCaseFormModel = new TestCaseFormModel()
			testCaseFormModel.setScriptLanguage("KEYWORD")
			testCaseFormModel.setName("KeywordTestCase_3")
			testCaseFormModel.setReference("KW3")
			testCaseFormModel.setDescription("A keyword test case")
		when:
			def testCase = testCaseFormModel.getTestCase()
		then:
			KeywordTestCase.class.isAssignableFrom(testCase.class)
			testCase.name == "KeywordTestCase_3"
			testCase.reference == "KW3"
			testCase.description == "A keyword test case"
	}

	@Unroll
	def "getTestCaseKind() - Should get test case kind #expectedOutput from scriptedLanguage"() {
		given:
			TestCaseFormModel testCaseFormModel = new TestCaseFormModel()
			testCaseFormModel.setScriptLanguage(input)
		when:
			def kind = testCaseFormModel.getTestCaseKind()
		then:
			kind == expectedOutput
		where:
			input 		| expectedOutput
			"ghErkIn" 	| GHERKIN
			"kEYwOrd" 	| KEYWORD
			"stAndArd" 	| STANDARD
	}

}
