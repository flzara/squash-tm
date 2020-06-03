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

import org.squashtest.tm.domain.bdd.ActionWord
import org.squashtest.tm.domain.bdd.ActionWordText
import org.squashtest.tm.domain.bdd.Keyword
import org.squashtest.tm.domain.execution.ExecutionStep
import spock.lang.Specification

class KeywordTestStepTest extends Specification {

	def "should throw IllegalArgumentException for null Keyword"() {
		given:
		def fragmentText = new ActionWordText("hello")
		when:
		new KeywordTestStep(null, new ActionWord([fragmentText] as List))
		then:
		thrown IllegalArgumentException
	}

	def "should throw IllegalArgumentException for null ActionWord"() {
		when:
		new KeywordTestStep(Keyword.GIVEN, null)
		then:
		thrown IllegalArgumentException
	}

	def "should associate a valid TestCase"() {
		given:
		KeywordTestCase testCase = new KeywordTestCase()
		def fragmentText = new ActionWordText("hello")
		ActionWord actionWord = new ActionWord([fragmentText] as List)
		KeywordTestStep keywordTestStep = new KeywordTestStep(Keyword.GIVEN, actionWord)
		when:
		keywordTestStep.setTestCase(testCase)
		then:
		keywordTestStep.testCase == testCase

	}

	def "should reject an invalid TestCase"() {
		given:
		TestCase testcase = new TestCase()
		def fragmentText = new ActionWordText("hello")
		ActionWord actionWord = new ActionWord([fragmentText] as List)
		KeywordTestStep keywordTestStep = new KeywordTestStep(Keyword.GIVEN, actionWord)
		when:
		keywordTestStep.setTestCase(testcase)
		then:
		thrown IllegalArgumentException
	}

	def "should create an execution step" () {
		given:
		def fragmentText = new ActionWordText("hello")
		ActionWord actionWord = new ActionWord([fragmentText] as List)
		KeywordTestStep keywordTestStep = new KeywordTestStep(Keyword.GIVEN, actionWord)
		when:
		def res = keywordTestStep.createExecutionSteps(null)
		then:
		res != null
		res.size() == 1
		ExecutionStep executionStep = res.get(0)
		executionStep.action == "GIVEN hello"
	}

	def "shoud copy a KeywordTestStep"() {
		given:
			ActionWord actionWord = Mock(ActionWord)
			actionWord.getId() >> 78L
			actionWord.createWord() >> "Halo!"
			KeywordTestStep keywordTestStep = new KeywordTestStep(
				Keyword.BUT,
				actionWord)
		when:
			KeywordTestStep copy = keywordTestStep.createCopy()
		then:
			copy != keywordTestStep
			copy.getKeyword() == keywordTestStep.getKeyword()
			def originalWord = keywordTestStep.getActionWord()
			def copyWord = copy.actionWord
			copyWord.getId() == originalWord.getId()
			copyWord.createWord() == originalWord.createWord()

	}
}
