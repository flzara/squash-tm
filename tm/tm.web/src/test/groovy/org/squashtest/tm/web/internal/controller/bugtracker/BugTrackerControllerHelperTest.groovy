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
package org.squashtest.tm.web.internal.controller.bugtracker

import javax.servlet.http.HttpServletRequest

import org.springframework.context.MessageSource
import org.squashtest.tm.domain.execution.Execution
import org.squashtest.tm.domain.execution.ExecutionStep
import org.squashtest.tm.domain.testcase.TestCase


class BugTrackerControllerHelperTest extends spock.lang.Specification {

	def "should build execution url"(){
		given:
		HttpServletRequest request = Mock()
		request.getServerPort()>> 8080
		request.getContextPath()>>"/contextPath"
		request.getServerName()>>"serverName"
		request.getScheme() >> "http"
		Execution execution = Mock()
		execution.getId()>> 65

		when:
		def result = BugTrackerControllerHelper.buildExecutionUrl(request, execution)

		then :
		result == "http://serverName:8080/contextPath/executions/65"
	}

	def "should get default description for execution"(){
		given:
		Execution execution = Mock()
		TestCase testCase = Mock()
		execution.getReferencedTestCase()>> testCase
		testCase.getName() >> "test case name"
		testCase.getReference() >> "Reference"
		Locale locale = new Locale("en");
		MessageSource messageSource = Mock()
		messageSource.getMessage("issue.default.description.testCase", null, locale) >> "Test Case"
		messageSource.getMessage("issue.default.description.execution", null, locale) >> "Execution"
		messageSource.getMessage("issue.default.description.description", null, locale) >> "Issue description"
		def executionUrl = "url"

		when:
		def result = BugTrackerControllerHelper.getDefaultDescription(execution, locale, messageSource, executionUrl)

		then:
		result == "# Test Case: [Reference] test case name\n# Execution: url\n\n# Issue description :\n"
	}

	def "should get default description for execution step"(){
		given:
		ExecutionStep step = Mock()
		Execution execution = Mock()
		List<ExecutionStep> steps = Mock()
		execution.getSteps() >> steps
		step.getExecution() >> execution
		steps.size() >>5
		step.getExecutionStepOrder()>>0
		TestCase testCase = Mock()
		execution.getReferencedTestCase()>> testCase
		testCase.getName() >> "test case name"
		testCase.getReference() >> "Reference"
		Locale locale = new Locale("en");
		MessageSource messageSource = Mock()
		messageSource.getMessage("issue.default.description.testCase", null, locale) >> "Test Case"
		messageSource.getMessage("issue.default.description.concernedStep", null, locale) >> "Concerned Step"
		messageSource.getMessage("issue.default.description.execution", null, locale) >> "Execution"
		messageSource.getMessage("issue.default.description.description", null, locale) >> "Issue description"
		def executionUrl = "url"

		when:
		def result = BugTrackerControllerHelper.getDefaultDescription (step, locale, messageSource, executionUrl)

		then:
		result == "# Test Case: [Reference] test case name\n# Execution: url\n# Concerned Step: 1/5\n\n# Issue description :\n"
	}

	def "should get default comment for execution step"(){
		given:
		ExecutionStep buggedStep = Mock()
		buggedStep.getAction()>> "action description"
		buggedStep.getExpectedResult()>>"expected result description"
		Execution execution = Mock()
		buggedStep.getId() >>1
		buggedStep.getExecutionStepOrder()>>0
		List<ExecutionStep> steps = [buggedStep]
		buggedStep.getExecution()>> execution
		execution.getSteps() >> steps
		Locale locale = new Locale("en");
		MessageSource messageSource = Mock()
		messageSource.getMessage("issue.default.additionalInformation.action", null, locale) >> "-------------------Action---------------------\n"
		messageSource.getMessage("issue.default.additionalInformation.expectedResult", null, locale) >> "\n\n----------------Expected Result---------------\n"
		messageSource.getMessage("issue.default.additionalInformation.step", null, locale) >> "Step"

		when:
		def result = BugTrackerControllerHelper.getDefaultAdditionalInformations (buggedStep, locale, messageSource)

		then:
		result == "=============================================\n"+
				"|    Step 1/1\n"+
				"=============================================\n"+
				"-------------------Action---------------------\n"+
				"action description"+
				"\n\n----------------Expected Result---------------\n"+
				"expected result description\n\n\n"
	}
}
