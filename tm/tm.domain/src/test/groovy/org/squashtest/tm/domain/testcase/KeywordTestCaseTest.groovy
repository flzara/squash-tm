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

import org.squashtest.tm.core.foundation.exception.NullArgumentException
import org.squashtest.tm.domain.bdd.ActionWord
import org.squashtest.tm.domain.bdd.Keyword
import org.squashtest.tm.domain.testautomation.AutomatedTest
import org.squashtest.tm.domain.testutils.MockFactory
import org.squashtest.tm.exception.UnknownEntityException
import org.squashtest.tm.tools.unittest.assertions.CollectionAssertions
import org.squashtest.tm.tools.unittest.reflection.ReflectionCategory
import spock.lang.Specification
import spock.lang.Unroll

import java.lang.reflect.Modifier


class KeywordTestCaseTest extends Specification {

	MockFactory mockFactory = new MockFactory()

	def setup() {
		CollectionAssertions.declareContainsExactly()
	}

	def "copy of a test case should have the same keyword steps"() {
		given:
		KeywordTestCase source = new KeywordTestCase()
		source.setName("source")
		source.notifyAssociatedWithProject(mockFactory.mockProject())
		ActionWord actionWord = new ActionWord("Harry Potter")
		KeywordTestStep sourceStep = new KeywordTestStep(Keyword.AND, actionWord)
		source.steps << sourceStep

		when:
		def copy = source.createCopy()

		then:
		copy.steps.size() == 1
		KeywordTestStep copiedStep = copy.steps[0]
		copiedStep.keyword == sourceStep.keyword
		copiedStep.actionWord == sourceStep.actionWord
		!copiedStep.is(sourceStep)
	}


}
