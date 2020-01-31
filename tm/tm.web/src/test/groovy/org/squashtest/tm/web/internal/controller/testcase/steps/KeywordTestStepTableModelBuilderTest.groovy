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
package org.squashtest.tm.web.internal.controller.testcase.steps


import org.squashtest.tm.domain.bdd.ActionWord
import org.squashtest.tm.domain.bdd.Keyword
import org.squashtest.tm.domain.testcase.KeywordTestStep
import org.squashtest.tm.web.internal.model.datatable.DataTableModelConstants;
import spock.lang.Specification

class KeywordTestStepTableModelBuilderTest extends Specification {

	BehaviorTestStepTableModelBuilder builder = new BehaviorTestStepTableModelBuilder()

	def "should build an item data from a BehaviorTestStep"() {
		given:
			def testStep = Mock(KeywordTestStep)
			testStep.getKeyword() >> Keyword.GIVEN
			testStep.getActionWord() >> new ActionWord("goodbye")
		when:
			Map<String, String> resultItem1 = builder.buildItemData(testStep)
		then:
			resultItem1.size() == 4
			resultItem1.get("step-index") == "0"
			resultItem1.get("step-keyword") == "GIVEN"
			resultItem1.get("step-phrase") == "goodbye"
			resultItem1.get(DataTableModelConstants.DEFAULT_EMPTY_DELETE_HOLDER_KEY) == null
	}

	def "should build a raw model from 2 BehaviorTestSteps"() {
		given:
			def testStep = Mock(KeywordTestStep)
			testStep.getKeyword() >> Keyword.GIVEN
			testStep.getActionWord() >> new ActionWord("hello")
		and:
			def testStep2 = Mock(KeywordTestStep)
			testStep2.getKeyword() >> Keyword.THEN
			testStep2.getActionWord() >> new ActionWord("goodbye")
		when:
			List<Object> resultCollection = builder.buildRawModel([testStep,testStep2],1)
		then:
			resultCollection.size() == 2
			def item1 = resultCollection[0]
			def item2 = resultCollection[1]

			item1.size() == 4
			item1.get("step-index") == "1"
			item1.get("step-keyword") == "GIVEN"
			item1.get("step-phrase") == "hello"
			item1.get(DataTableModelConstants.DEFAULT_EMPTY_DELETE_HOLDER_KEY) == null

			item2.size() == 4
			item2.get("step-index") == "2"
			item2.get("step-keyword") == "THEN"
			item2.get("step-phrase") == "goodbye"
			item2.get(DataTableModelConstants.DEFAULT_EMPTY_DELETE_HOLDER_KEY) == null
	}
}
