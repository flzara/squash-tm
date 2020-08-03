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
}
