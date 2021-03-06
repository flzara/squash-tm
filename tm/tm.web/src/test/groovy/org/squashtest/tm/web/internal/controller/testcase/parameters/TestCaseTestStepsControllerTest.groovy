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
package org.squashtest.tm.web.internal.controller.testcase.parameters

import org.springframework.validation.BindException
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder
import org.squashtest.tm.core.foundation.collection.SinglePageCollectionHolder
import org.squashtest.tm.domain.attachment.AttachmentList
import org.squashtest.tm.domain.bdd.ActionWord
import org.squashtest.tm.domain.bdd.ActionWordFragment
import org.squashtest.tm.domain.bdd.ActionWordParameter
import org.squashtest.tm.domain.bdd.ActionWordParameterValue
import org.squashtest.tm.domain.bdd.ActionWordText
import org.squashtest.tm.domain.bdd.Keyword
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.domain.testcase.ActionTestStep
import org.squashtest.tm.domain.testcase.KeywordTestStep
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.domain.testcase.TestStep
import org.squashtest.tm.service.customfield.CustomFieldHelper
import org.squashtest.tm.service.customfield.CustomFieldHelperService
import org.squashtest.tm.service.security.PermissionEvaluationService
import org.squashtest.tm.service.testcase.TestCaseModificationService
import org.squashtest.tm.tools.unittest.reflection.ReflectionCategory
import org.squashtest.tm.web.internal.controller.testcase.steps.KeywordTestStepModel
import org.squashtest.tm.web.internal.controller.testcase.steps.TestCaseTestStepsController
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters
import spock.lang.Specification

import javax.servlet.http.HttpServletRequest

class TestCaseTestStepsControllerTest extends Specification {
	TestCaseTestStepsController controller = new TestCaseTestStepsController()

	TestCaseModificationService testCaseModificationService = Mock()
	HttpServletRequest request = Mock()
	InternationalizationHelper messageSource = Mock()
	CustomFieldHelperService cufHelperService = Mock()
	PermissionEvaluationService permissionService = Mock()

	def setup() {
		controller.testCaseModificationService = testCaseModificationService
		request.getCharacterEncoding() >> "ISO-8859-1"
		controller.internationalizationHelper = messageSource
		controller.cufHelperService = cufHelperService
		controller.permissionService = permissionService
	}


	def "should build table model for test case steps"() {
		given:
		AttachmentList al = Mock()
		al.size() >> 1
		al.getId() >> 5

		and:
		ActionTestStep step1 = new ActionTestStep(action: "a1", expectedResult: "r1")
		use(ReflectionCategory) {
			TestStep.set field: "id", of: step1, to: 1L
			ActionTestStep.set field: "attachmentList", of: step1, to: al
		}

		and:
		ActionTestStep step2 = new ActionTestStep(action: "a2", expectedResult: "r2")
		use(ReflectionCategory) {
			TestStep.set field: "id", of: step2, to: 2L
			ActionTestStep.set field: "attachmentList", of: step2, to: al
		}


		and:
		PagedCollectionHolder<List<ActionTestStep>> holder = new SinglePageCollectionHolder<List<ActionTestStep>>([step1, step2])
		testCaseModificationService.findStepsByTestCaseIdFiltered(10, _) >> holder

		and:
		TestCase tc = Mock()
		tc.project >> Mock(Project)
		testCaseModificationService.findById(_) >> tc

		and:
		DataTableDrawParameters params = new DataTableDrawParameters();
		params.setiDisplayLength(10);
		params.setiDisplayStart(0)
		params.setsEcho("echo");

		and:
		CustomFieldHelper cufhelper = Mock()
		cufhelper.getCustomFieldValues() >> []
		cufhelper.restrictToCommonFields() >> cufhelper
		cufhelper.setRenderingLocations(_) >> cufhelper

		cufHelperService.newStepsHelper(_, _) >> cufhelper

		when:
		def res = controller.getStepsTableModel(10, params)

		then:
		res.sEcho == "echo"
		res.aaData == [
			[
				"step-id"                  : 1L,
				"empty-browse-holder"      : null,
				"customFields"             : [:],
				"nb-attachments"           : 1,
				"empty-requirements-holder": null,
				"nb-requirements"          : 0,
				"step-index"               : 1,
				"step-type"                : "action",
				"attach-list-id"           : 5L,
				"step-result"              : "r1",
				"has-requirements"         : false,
				"empty-delete-holder"      : null,
				"step-action"              : "a1",
				"call-step-info"           : null
			],
			[
				"step-id"                  : 2L,
				"empty-browse-holder"      : null,
				"customFields"             : [:],
				"nb-attachments"           : 1,
				"empty-requirements-holder": null,
				"nb-requirements"          : 0,
				"step-index"               : 2,
				"step-type"                : "action",
				"attach-list-id"           : 5L,
				"step-result"              : "r2",
				"has-requirements"         : false,
				"call-step-info"           : null,
				"empty-delete-holder"      : null,
				"step-action"              : "a2"
			]]


	}

	def "should build table model for keyword test case steps"() {
		given:
		def project = Mock(Project)
		project.getId() >> -24L

		def actionWord1 = Mock(ActionWord)
		ActionWordText text1 = new ActionWordText("hello")
		List<ActionWordFragment> fragments1 = new ArrayList<>()
		fragments1.add(text1)
		actionWord1.getFragments() >> fragments1
		actionWord1.getProject() >> project

		KeywordTestStep step1 = new KeywordTestStep(Keyword.GIVEN, actionWord1)
		List<ActionWordParameterValue> paramValues1 = new ArrayList<>()
		use(ReflectionCategory) {
			TestStep.set field: "id", of: step1, to: 1L
			KeywordTestStep.set field: "paramValues", of: step1, to: paramValues1
		}

		and:
		def actionWord2 = Mock(ActionWord)
		ActionWordText text2 = new ActionWordText("how are ")
		ActionWordText text3 = new ActionWordText(" ?")
		ActionWordParameter parameter = new MockActionWordParameter(-50L)
		List<ActionWordFragment> fragments2 = new ArrayList<>()
		fragments2.add(text2)
		fragments2.add(parameter)
		fragments2.add(text3)
		actionWord2.getFragments() >> fragments2
		actionWord2.getProject() >> project

		KeywordTestStep step2 = new KeywordTestStep(Keyword.AND, actionWord2)
		List<ActionWordParameterValue> paramValues2 = new ArrayList<>()
		ActionWordParameterValue value = new ActionWordParameterValue("you")
		value.setActionWordParam(parameter)
		paramValues2.add(value)
		use(ReflectionCategory) {
			TestStep.set field: "id", of: step2, to: 2L
			KeywordTestStep.set field: "paramValues", of: step2, to: paramValues2
		}

		and:
		TestCase tc = Mock()
		tc.getSteps() >> [step1, step2]
		testCaseModificationService.findById(7L) >> tc

		and:
		DataTableDrawParameters params = new DataTableDrawParameters()
		params.setiDisplayLength(10)
		params.setiDisplayStart(0)
		params.setsEcho("echo")

		and:
		PagedCollectionHolder<List<ActionTestStep>> holder = new SinglePageCollectionHolder<List<ActionTestStep>>([step1, step2])
		testCaseModificationService.findStepsByTestCaseIdFiltered(7L, _) >> holder

		and:
		permissionService.hasRoleOrPermissionOnObject(_,_,_,_) >> false

		when:
		def res = controller.getKeywordTestStepTableModel(7L, params)

		then:
		res.sEcho == "echo"
		res.aaData == [
			[
				"entity-id"          	   : "1",
				"step-keyword"       	   : 'GIVEN',
				"step-index"        	   : '1',
				"empty-delete-holder"	   : null,
				"step-action-word"         : 'hello',
				"toggle-step-details"	   : null,
				"step-datatable"		   : '',
				"step-docstring"		   : '',
				"step-comment"			   : '',
				"step-action-word-url"	   : null,
				"action-word-id"		   : '',
				"step-action-word-unstyled": 'hello'
			],
			[
				"entity-id"          		: "2",
				"step-keyword"       		: 'AND',
				"step-index"         		: '2',
				"empty-delete-holder"		: null,
				"step-action-word"   		: 'how are <span style=\"color: blue;\">you</span> ?',
				"toggle-step-details"	   : null,
				"step-datatable"		   : '',
				"step-docstring"		   : '',
				"step-comment"			   : '',
				"step-action-word-url"	   : null,
				"action-word-id"		   : '',
				"step-action-word-unstyled" : 'how are "you" ?'
			]]


	}

	def "should change step index"() {
		when:
		controller.changeStepIndex(10, 1, 20)

		then:
		1 * testCaseModificationService.changeTestStepPosition(20, 10, 1)
	}


	def "should add a keyword test step with given keyword and actionWord"() {
		given:
		KeywordTestStepModel testStepModel = new KeywordTestStepModel()
		testStepModel.setKeyword("BUT")
		testStepModel.setActionWord("add a BDD test step")

		and:
		def testStep = Mock(KeywordTestStep)
		testStep.getId() >> 2020

		when:
		testCaseModificationService.addKeywordTestStep(1L, "BUT", "add a BDD test step") >> testStep

		then:
		controller.addKeywordTestStep(testStepModel, 1L) == 2020
	}

	def "should add a keyword test step with given keyword and actionWord at sepcific index"() {
		given:
		KeywordTestStepModel testStepModel = new KeywordTestStepModel()
		testStepModel.setKeyword("BUT")
		testStepModel.setActionWord("add a BDD test step")
		testStepModel.setIndex(2)

		and:
		def testStep = Mock(KeywordTestStep)
		testStep.getId() >> 2020

		when:
		testCaseModificationService.addKeywordTestStep(1L, "BUT", "add a BDD test step", 2) >> testStep

		then:
		controller.addKeywordTestStep(testStepModel, 1L) == 2020
	}

	def "should add a keyword test step with given keyword and parameterized actionWord"() {
		given:
		KeywordTestStepModel testStepModel = new KeywordTestStepModel()
		testStepModel.setKeyword("BUT")
		testStepModel.setActionWord("add a \"BDD\" test \"step\"")

		and:
		def testStep = Mock(KeywordTestStep)
		testStep.getId() >> 2020

		when:
		testCaseModificationService.addKeywordTestStep(1L, "BUT", "add a \"BDD\" test \"step\"") >> testStep

		then:
		controller.addKeywordTestStep(testStepModel, 1L) == 2020
	}

	def "should add a keyword test step with given keyword and parameterized actionWord in which value is between <>"() {
		given:
		KeywordTestStepModel testStepModel = new KeywordTestStepModel()
		testStepModel.setKeyword("BUT")
		testStepModel.setActionWord("add a \"BDD\" test <tcParam>")

		and:
		def testStep = Mock(KeywordTestStep)
		testStep.getId() >> 2020

		when:
		testCaseModificationService.addKeywordTestStep(1L, "BUT", "add a \"BDD\" test <tcParam>") >> testStep

		then:
		controller.addKeywordTestStep(testStepModel, 1L) == 2020
	}

	def "should add a keyword test step with given keyword and parameterized actionWord in which values are numbers"() {
		given:
		KeywordTestStepModel testStepModel = new KeywordTestStepModel()
		testStepModel.setKeyword("BUT")
		testStepModel.setActionWord("add \"1\" and 2")

		and:
		def testStep = Mock(KeywordTestStep)
		testStep.getId() >> 2020

		when:
		testCaseModificationService.addKeywordTestStep(1L, "BUT", "add \"1\" and 2") >> testStep

		then:
		controller.addKeywordTestStep(testStepModel, 1L) == 2020
	}

	def "should throw exception when adding a keyword test step with empty keyword"() {
		given:
		KeywordTestStepModel testStepModel = new KeywordTestStepModel()
		testStepModel.setKeyword("")
		testStepModel.setActionWord("add a BDD test step")

		and:
		def testStep = Mock(KeywordTestStep)
		testStep.getId() >> 2020
		testCaseModificationService.addKeywordTestStep(1L, "", "add a BDD test step") >> testStep

		when:
		controller.addKeywordTestStep(testStepModel, 1L)

		then:
		BindException ex = thrown()
		ex.message == "org.springframework.validation.BeanPropertyBindingResult: 1 errors\n" +
			"Field error in object 'add-keyword-test-step' on field 'keyword': rejected value []; codes [message.notBlank.add-keyword-test-step.keyword,message.notBlank.keyword,message.notBlank.java.lang.String,message.notBlank]; arguments []; default message [null]"
	}

	def "should throw exception when adding a keyword test step with empty Action word"() {
		given:
		KeywordTestStepModel testStepModel = new KeywordTestStepModel()
		testStepModel.setKeyword("AND")
		testStepModel.setActionWord("")

		and:
		def testStep = Mock(KeywordTestStep)
		testStep.getId() >> 2020
		testCaseModificationService.addKeywordTestStep(1L, "AND", "") >> testStep

		when:
		controller.addKeywordTestStep(testStepModel, 1L)

		then:
		BindException ex = thrown()
		ex.message == "org.springframework.validation.BeanPropertyBindingResult: 1 errors\n" +
			"Field error in object 'add-keyword-test-step' on field 'actionWord': rejected value []; codes [message.notBlank.add-keyword-test-step.actionWord,message.notBlank.actionWord,message.notBlank.java.lang.String,message.notBlank]; arguments []; default message [null]"
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
