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

import org.springframework.context.MessageSource
import org.squashtest.tm.domain.bdd.ActionWord
import org.squashtest.tm.domain.bdd.ActionWordParameter
import org.squashtest.tm.domain.bdd.ActionWordParameterValue
import org.squashtest.tm.domain.bdd.ActionWordText
import org.squashtest.tm.domain.bdd.BddScriptLanguage
import org.squashtest.tm.domain.bdd.Keyword
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.domain.testutils.MockFactory
import org.squashtest.tm.tools.unittest.assertions.CollectionAssertions
import spock.lang.Specification
import spock.lang.Unroll

class KeywordTestCaseTest extends Specification {

	MockFactory mockFactory = new MockFactory()

	Project project = new Project()

	def messageSource = Mock(MessageSource)

	def setup() {
		CollectionAssertions.declareContainsExactly()
		project.setBddScriptLanguage(BddScriptLanguage.ENGLISH)
	}

	def "copy of a test case should have the same keyword steps"() {
		given:
			KeywordTestCase source = new KeywordTestCase()
			source.setName("source")
			source.notifyAssociatedWithProject(mockFactory.mockProject())
			def fragmentText = new ActionWordText("Harry Potter")
			ActionWord actionWord = new ActionWord([fragmentText] as List)
			KeywordTestStep sourceStep = new KeywordTestStep(Keyword.AND, actionWord)
			source.steps << sourceStep
		when:
			def copy = source.createCopy()
		then:
			copy.steps.size() == 1
			KeywordTestStep copiedStep = copy.steps[0] as KeywordTestStep
			copiedStep.keyword == sourceStep.keyword
			copiedStep.actionWord == sourceStep.actionWord
			!copiedStep.is(sourceStep)
	}

	@Unroll
	def "should compute whether this test case contains steps using ActionWordParamValues linked to test case parameters"() {
		given: "A KeywordTestCase with some steps with params"
			KeywordTestCase testCase = new KeywordTestCase()
			testCase.setName("I love fruit")
			testCase.notifyAssociatedWithProject(project)

			def fragment1 = new ActionWordText("I have several ")
			def fragment2 = new ActionWordParameter("fruit", "apples")
			def fragment3 = new ActionWordText(" in my basket")
			def actionWord1 = new ActionWord([fragment1, fragment2, fragment3])
			def value2 = new ActionWordParameterValue("pears")
			value2.setActionWordParam(fragment2)
			KeywordTestStep step1 = new KeywordTestStep(Keyword.GIVEN, actionWord1)
			step1.setParamValues([value2])

			def fragment4 = new ActionWordText("I eat ")
			def fragment5 = new ActionWordParameter("amount", "1")
			def fragment6 = new ActionWordText(" kilograms")
			def actionWord2 = new ActionWord([fragment4, fragment5, fragment6])
			def value5 = new ActionWordParameterValue("0.7")
			value5.setActionWordParam(fragment5)
			KeywordTestStep step2 = new KeywordTestStep(Keyword.WHEN, actionWord2)
			step2.setParamValues([value5])

			def fragment7 = new ActionWordText("I am ")
			def fragment8 = new ActionWordParameter("mood", "fine")
			def fragment9 = new ActionWordText(" in my life")
			def actionWord3 = new ActionWord([fragment7, fragment8, fragment9])
			def value8 = new ActionWordParameterValue(valueParam)
			value8.setActionWordParam(fragment8)
			KeywordTestStep step3 = new KeywordTestStep(Keyword.THEN, actionWord3)
			step3.setParamValues([value8])

			testCase.addStep(step1)
			testCase.addStep(step2)
			testCase.addStep(step3)
		when:
			def res = testCase.containsStepsUsingTcParam()
		then:
			res == expectedResult
		where:
			valueParam | expectedResult
			"<happy>"  | true
			"happy"    | false
	}

}
