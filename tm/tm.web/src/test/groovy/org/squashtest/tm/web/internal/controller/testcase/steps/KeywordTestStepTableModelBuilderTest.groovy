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
import org.squashtest.tm.domain.bdd.ActionWordFragment
import org.squashtest.tm.domain.bdd.ActionWordParameter
import org.squashtest.tm.domain.bdd.ActionWordParameterValue
import org.squashtest.tm.domain.bdd.ActionWordText
import org.squashtest.tm.domain.bdd.Keyword
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.domain.testcase.KeywordTestStep
import org.squashtest.tm.service.security.PermissionEvaluationService
import org.squashtest.tm.web.internal.model.datatable.DataTableModelConstants
import spock.lang.Specification

class KeywordTestStepTableModelBuilderTest extends Specification {

	PermissionEvaluationService permissionService = Mock();

	KeywordTestStepTableModelBuilder builder = new KeywordTestStepTableModelBuilder(permissionService)

	def "should build an item data from a KeywordTestStep"() {
		given:
		def testStep = initKeywordTestStep(-68L, Keyword.GIVEN)
		def project = Mock(Project)
		project.getId() >> 4L

		def actionWord1 = Mock(ActionWord)
		ActionWordText text1 = new ActionWordText("goodbye")
		List<ActionWordFragment> fragments1 = new ArrayList<>()
		fragments1.add(text1)
		actionWord1.getFragments() >> fragments1
		actionWord1.getProject() >> project
		testStep.getActionWord() >> actionWord1

		when:
		Map<String, String> resultItem1 = builder.buildItemData(testStep)

		then:
		resultItem1.get("step-index") == "0"
		resultItem1.get("step-keyword") == "GIVEN"
		resultItem1.get("step-action-word") == "goodbye"
		resultItem1.get(DataTableModelConstants.DEFAULT_EMPTY_DELETE_HOLDER_KEY) == null
	}

	def "should build a raw model from 2 KeywordTestStep"() {
		given:
		def testStep = initKeywordTestStep(-99L, Keyword.GIVEN)
		def project = Mock(Project)
		project.getId() >> 2L

		def actionWord = Mock(ActionWord)
		ActionWordText text = new ActionWordText("hello ")
		ActionWordParameter parameter = new MockActionWordParameter(-50L)
		List<ActionWordFragment> fragments = new ArrayList<>()
		fragments.add(text)
		fragments.add(parameter)
		actionWord.getFragments() >> fragments
		actionWord.getProject() >> project
		testStep.getActionWord() >> actionWord

		List<ActionWordParameterValue> values = new ArrayList<>();
		ActionWordParameterValue value = new ActionWordParameterValue("Sunday")
		value.setActionWordParam(parameter)
		values.add(value)
		testStep.getParamValues() >> values

		and:
		def testStep2 = initKeywordTestStep(-77L, Keyword.THEN)

		def actionWord2 = Mock(ActionWord)
		ActionWordText text2 = new ActionWordText("goodbye")
		List<ActionWordFragment> fragments2 = new ArrayList<>()
		fragments2.add(text2)
		actionWord2.getFragments() >> fragments2
		actionWord2.getProject() >> project
		testStep2.getActionWord() >> actionWord2

		and:
		permissionService.hasRoleOrPermissionOnObject(_,_,_,_) >> false

		when:
		List<Object> resultCollection = builder.buildRawModel([testStep, testStep2], 1)

		then:
		resultCollection.size() == 2
		def item1 = resultCollection[0]
		def item2 = resultCollection[1]

		item1.get("step-index") == "1"
		item1.get("step-keyword") == "GIVEN"
		item1.get("step-action-word") == "hello <span style=\"color: blue;\">Sunday</span>"
		item1.get(DataTableModelConstants.DEFAULT_EMPTY_DELETE_HOLDER_KEY) == null

		item2.get("step-index") == "2"
		item2.get("step-keyword") == "THEN"
		item2.get("step-action-word") == "goodbye"
		item2.get(DataTableModelConstants.DEFAULT_EMPTY_DELETE_HOLDER_KEY) == null
	}

	def "should build a raw model from 2 KeywordTestStep in which there is an Actionword with a Parameter associated with Test Case parameter"() {
		given:
		def testStep = initKeywordTestStep(-99L, Keyword.GIVEN)
		def project = Mock(Project)
		project.getId() >> 4L

		def actionWord = Mock(ActionWord)
		ActionWordText text = new ActionWordText("hello ")
		ActionWordParameter parameter = new MockActionWordParameter(-50L)
		List<ActionWordFragment> fragments = new ArrayList<>()
		fragments.add(text)
		fragments.add(parameter)
		actionWord.getFragments() >> fragments
		actionWord.getProject() >> project
		testStep.getActionWord() >> actionWord

		List<ActionWordParameterValue> values = new ArrayList<>();
		ActionWordParameterValue value = new ActionWordParameterValue("<dateOfWeek>")
		value.setActionWordParam(parameter)
		values.add(value)
		testStep.getParamValues() >> values

		and:
		def testStep2 = initKeywordTestStep(-77L, Keyword.THEN)

		def actionWord2 = Mock(ActionWord)
		ActionWordText text2 = new ActionWordText("goodbye")
		List<ActionWordFragment> fragments2 = new ArrayList<>()
		fragments2.add(text2)
		actionWord2.getFragments() >> fragments2
		actionWord2.getProject() >> project
		testStep2.getActionWord() >> actionWord2

		and:
		permissionService.hasRoleOrPermissionOnObject(_,_,_,_) >> false

		when:
		List<Object> resultCollection = builder.buildRawModel([testStep, testStep2], 1)

		then:
		resultCollection.size() == 2
		def item1 = resultCollection[0]
		def item2 = resultCollection[1]

		item1.get("step-index") == "1"
		item1.get("step-keyword") == "GIVEN"
		item1.get("step-action-word") == "hello <span style=\"color: blue;\">&lt;dateOfWeek&gt;</span>"
		item1.get(DataTableModelConstants.DEFAULT_EMPTY_DELETE_HOLDER_KEY) == null

		item2.get("step-index") == "2"
		item2.get("step-keyword") == "THEN"
		item2.get("step-action-word") == "goodbye"
		item2.get(DataTableModelConstants.DEFAULT_EMPTY_DELETE_HOLDER_KEY) == null
	}

	def initKeywordTestStep(id, keyword) {
		def testStep = Mock(KeywordTestStep) {
			getId() >> id
			getKeyword() >> keyword
			getDatatable() >> ""
			getDocstring() >> ""
			getComment() >> ""
		}

		return testStep;
	}

	class MockActionWordParameter extends  ActionWordParameter {
		Long setId

		MockActionWordParameter(Long setId) {
			this.setId = setId
		}

		Long getId() {
			return setId
		}

		void setId(Long newId) {
			setId = newId
		}
	}
}
