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

import org.squashtest.tm.domain.keyword.Keyword
import spock.lang.Specification

class KeywordTestStepTest extends Specification {

	def "should associate a valid TestCase"() {
		given:
		TestCase testCase = new TestCase(TestCase.KEYWORD_ENABLED)
		KeywordTestStep keywordTestStep = new KeywordTestStep(new Keyword("hello"))
		when:
		keywordTestStep.setTestCase(testCase)
		then:
		keywordTestStep.testCase == testCase

	}
	def "should reject an invalid TestCase"() {
		given:
		TestCase testcase = new TestCase()
		KeywordTestStep keywordTestStep = new KeywordTestStep(new Keyword("hello"))
		when:
		keywordTestStep.setTestCase(testcase)
		then:
		thrown IllegalArgumentException
	}
}
