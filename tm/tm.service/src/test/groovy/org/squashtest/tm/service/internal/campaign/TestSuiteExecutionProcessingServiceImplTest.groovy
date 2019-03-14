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
package org.squashtest.tm.service.internal.campaign

import gherkin.ast.Background
import gherkin.ast.Feature
import gherkin.ast.GherkinDocument
import gherkin.ast.Scenario
import gherkin.ast.ScenarioDefinition
import org.squashtest.tm.domain.campaign.Iteration
import org.squashtest.tm.domain.campaign.IterationTestPlanItem
import org.squashtest.tm.domain.campaign.TestSuite
import org.squashtest.tm.domain.execution.Execution
import org.squashtest.tm.domain.execution.ExecutionStep
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.domain.testcase.ScriptedTestCaseExtender
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.domain.testcase.TestCaseKind
import org.squashtest.tm.domain.testcase.TestStep;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.service.internal.repository.TestSuiteDao
import org.squashtest.tm.service.internal.testcase.scripted.gherkin.GherkinTestCaseParser
import org.squashtest.tm.service.security.PermissionEvaluationService
import org.squashtest.tm.service.testcase.scripted.ScriptedTestCaseParser;
import org.squashtest.tm.service.user.UserAccountService
import org.squashtest.tm.tools.unittest.reflection.ReflectionCategory;
import spock.lang.Specification
import spock.lang.Unroll

import java.util.function.Function

class TestSuiteExecutionProcessingServiceImplTest  extends Specification {
	TestSuiteExecutionProcessingServiceImpl manager
	TestSuiteDao testSuiteDao = Mock()
	IterationTestPlanManager testPlanManager = Mock()
	UserAccountService userService = Mock()
	PermissionEvaluationService permissionEvaluationService = Mock()
	CampaignNodeDeletionHandler campaignNodeDeletionHandler = Mock()
	Function<ScriptedTestCaseExtender, ScriptedTestCaseParser> parserFactory = Mock()

	def setup() {
		manager = new TestSuiteExecutionProcessingServiceImpl(campaignNodeDeletionHandler, testPlanManager, userService, permissionEvaluationService, parserFactory)
		manager.suiteDao = testSuiteDao
		User user = Mock()
		user.getLogin() >> "admin"
		userService.findCurrentUser() >> user
	}

	def "should start new execution of test suite"() {
		given:
		TestSuite suite = Mock()
		Project project = Mock()
		suite.getProject() >> project
		project.getId() >> 1L
		IterationTestPlanItem item = Mock()
		item.isTestCaseDeleted()>>false
		item.isExecutableThroughTestSuite()>>true
		suite.findFirstExecutableTestPlanItem(_)>>item
		item.getExecutions()>> []
		and:
		testSuiteDao.getOne(10) >> suite

		and:
		Execution exec = Mock()
		ExecutionStep executionStep = Mock()
		exec.getSteps()>> [executionStep]
		testPlanManager.addExecution(_) >> exec

		when:
		def res = manager.startResume(10)

		then:
		res == exec
	}

	@Unroll("should there have more items in test plan ? #moreExecutable !")
	def "should have more items in test plan"() {
		given:
		testSuiteDao.getOne(10) >> aSuiteWithExecutableItems(10L, 20L)

		when:
		def more = manager.hasMoreExecutableItems(10L, 10L)

		then:
		more

	}

	@Unroll("should there have more items in test plan ? #more !")
	def "should not have more items in test plan"() {
		given:
		testSuiteDao.getOne(10) >> aSuiteWithExecutableItems(10L, 20L)

		when:
		def more = manager.hasMoreExecutableItems(10L, 20L)

		then:
		!more

	}

	def "item should be the last executable of test plan"() {
		given:
		TestSuite testSuite = aSuiteWithExecutableItems(10L, 20L)
		testSuiteDao.getOne(10) >> testSuite

		and:
		TestCase testCase = Mock()
		testCase.getKind() >> TestCaseKind.STANDARD
		IterationTestPlanItem item = new IterationTestPlanItem(testCase)
		User user = Mock()
		user.getLogin() >> "admin"
		use (ReflectionCategory) {
			IterationTestPlanItem.set field: "id", of: item, to: 30L
			IterationTestPlanItem.set field: "user", of: item, to: user
		}
		testSuite.iteration.addTestPlan(item)
		testSuite.bindTestPlanItem(item)

		and:
		IterationTestPlanItem otherItem = new IterationTestPlanItem(Mock(TestCase))
		testSuite.iteration.addTestPlan(otherItem)
		testSuite.bindTestPlanItem(otherItem)
		use (ReflectionCategory) {
			IterationTestPlanItem.set field: "id", of: otherItem, to: 40L
			IterationTestPlanItem.set field: "referencedTestCase", of: otherItem, to: null
			IterationTestPlanItem.set field: "user", of: item, to: user
		}

		when:
		def res = manager.hasMoreExecutableItems(10L, 20L)

		then:
		!res
	}

	def "wrong item should not be the last of test plan"() {
		given:
		TestSuite testSuite = new TestSuite()
		Iteration iteration = new Iteration()
		testSuite.setIteration(iteration)
		testSuiteDao.getOne(10) >> testSuite

		and:
		TestCase testCase = Mock()
		testCase.getKind() >> TestCaseKind.STANDARD
		IterationTestPlanItem item = new IterationTestPlanItem(testCase)
		User user = Mock()
		user.getLogin() >> "admin"
		use (ReflectionCategory) {
			IterationTestPlanItem.set field: "id", of: item, to: 10L
			IterationTestPlanItem.set field: "user", of: item, to: user
		}
		iteration.addTestPlan(item)
		testSuite.bindTestPlanItem(item)

		when:
		def res = manager.hasMoreExecutableItems(10L, 30L)

		then:
		res
	}

	def "item linked to Gherkin test should be the last executable of test plan if script have scenarios"() {
		given:
		TestSuite testSuite = aSuiteWithExecutableItems(10L, 20L)
		testSuiteDao.getOne(10) >> testSuite

		and:
		TestCase testCase = Mock()
		testCase.getKind() >> TestCaseKind.GHERKIN
		ScriptedTestCaseExtender extender = Mock()
		extender.getScript() >> "script"
		testCase.getScriptedTestCaseExtender() >> extender

		and:
		GherkinTestCaseParser parser = Mock()
		GherkinDocument document = Mock()
		Feature feature = Mock()
		Scenario scenario = Mock()
		List<ScenarioDefinition> scenarios = [scenario]
		feature.getChildren() >> scenarios
		document.getFeature() >> feature
		parser.parseToGherkinDocument(extender) >> document

		parserFactory.apply(extender) >> parser


		and:
		IterationTestPlanItem item = new IterationTestPlanItem(testCase)
		User user = Mock()
		user.getLogin() >> "admin"
		use (ReflectionCategory) {
			IterationTestPlanItem.set field: "id", of: item, to: 30L
			IterationTestPlanItem.set field: "user", of: item, to: user
		}
		testSuite.iteration.addTestPlan(item)
		testSuite.bindTestPlanItem(item)

		when:
		def res = manager.hasMoreExecutableItems(10L, 20L)

		then:
		res
	}

	def "item linked to Gherkin test should not be the last executable of test plan if script doesn't have scenarios"() {
		given:
		TestSuite testSuite = aSuiteWithExecutableItems(10L, 20L)
		testSuiteDao.getOne(10) >> testSuite

		and:
		TestCase testCase = Mock()
		testCase.getKind() >> TestCaseKind.GHERKIN
		ScriptedTestCaseExtender extender = Mock()
		extender.getScript() >> "script"
		testCase.getScriptedTestCaseExtender() >> extender

		and:
		GherkinTestCaseParser parser = Mock()
		GherkinDocument document = Mock()
		Feature feature = Mock()
		Background background = Mock()
		List<ScenarioDefinition> scenarios = [background]
		feature.getChildren() >> scenarios
		document.getFeature() >> feature
		parser.parseToGherkinDocument(extender) >> document

		parserFactory.apply(extender) >> parser


		and:
		IterationTestPlanItem item = new IterationTestPlanItem(testCase)
		User user = Mock()
		user.getLogin() >> "admin"
		use (ReflectionCategory) {
			IterationTestPlanItem.set field: "id", of: item, to: 30L
			IterationTestPlanItem.set field: "user", of: item, to: user
		}
		testSuite.iteration.addTestPlan(item)
		testSuite.bindTestPlanItem(item)

		when:
		def res = manager.hasMoreExecutableItems(10L, 20L)

		then:
		!res
	}

	def aSuiteWithExecutableItems(Long... ids) {
		TestSuite testSuite = new TestSuite()
		Iteration iteration = new Iteration()
		testSuite.setIteration(iteration)

		ids.each { id ->
			TestCase testCase = Mock()
			testCase.getKind() >> TestCaseKind.STANDARD
			TestStep testStep = Mock()
			testCase.getSteps() >> [testStep]
			IterationTestPlanItem item = new IterationTestPlanItem(testCase)
			User user = Mock()
			user.getLogin() >> "admin"
			use (ReflectionCategory) {
				IterationTestPlanItem.set field: "id", of: item, to: id
				IterationTestPlanItem.set field: "user", of: item, to: user
			}
			iteration.addTestPlan(item)
			testSuite.bindTestPlanItem(item)
		}

		return testSuite
	}

	def "should start next execution of test suite"() {
		given:
		TestSuite suite = Mock()
		Project project = Mock()
		suite.getProject() >> project
		project.getId() >> 1L
		IterationTestPlanItem nextItem = Mock()
		suite.findNextExecutableTestPlanItem(100, _)>>nextItem
		nextItem.isExecutableThroughTestSuite()>>true

		and:
		testSuiteDao.getOne(10) >> suite

		and:
		Execution exec = Mock()
		ExecutionStep executionStep = Mock()
		exec.getSteps()>>[executionStep]
		testPlanManager.addExecution(_) >> exec

		when:
		def res = manager.startResumeNextExecution(10, 100)

		then:
		res == exec
	}
}
