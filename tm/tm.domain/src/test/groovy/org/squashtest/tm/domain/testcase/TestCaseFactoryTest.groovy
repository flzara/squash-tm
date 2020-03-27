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
package org.squashtest.tm.domain.testcase

import spock.lang.Specification

class TestCaseFactoryTest extends Specification {

	def "#getTestCase(String, String) - Should create a new TestCase"() {
		given:
			def inputName = "  ~ Stàndârd Test Case 14 ~ "
			def trimmedName = "~ Stàndârd Test Case 14 ~"
		when:
			def testCase = TestCaseFactory.getTestCase("STANDARD", inputName)
		then:
			testCase != null
			TestCase.class.isAssignableFrom(testCase.class)
			! KeywordTestCase.class.isAssignableFrom(testCase.class)
			! ScriptedTestCase.class.isAssignableFrom(testCase.class)
			testCase.name == trimmedName
	}

	def "#getTestCase(String, String) - Should create a new KeywordTestCase"() {
		given:
			def inputName = "  ~ Keyword Test Case 17 ~ "
			def trimmedName = "~ Keyword Test Case 17 ~"
		when:
			def testCase = TestCaseFactory.getTestCase("KEYWORD", inputName)
		then:
			testCase != null
			KeywordTestCase.class.isAssignableFrom(testCase.class)
			testCase.name == trimmedName
	}

	def "#getTestCase(String, String) - Should create a new ScriptedTestCase"() {
		given:
			def inputName = "  ~ Gherkin Test Case 21 ~ "
			def trimmedName = "~ Gherkin Test Case 21 ~"
		when:
			def testCase = TestCaseFactory.getTestCase("GHERKIN", inputName)
		then:
			testCase != null
			ScriptedTestCase.class.isAssignableFrom(testCase.class)
			testCase.name == trimmedName
			! ((ScriptedTestCase) testCase).script.isEmpty()
	}
}
