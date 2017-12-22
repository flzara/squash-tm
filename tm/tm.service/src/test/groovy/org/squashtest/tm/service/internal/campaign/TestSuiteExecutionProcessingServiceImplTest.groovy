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

import static org.junit.Assert.*

import javax.inject.Inject;

import org.squashtest.tm.domain.campaign.IterationTestPlanItem
import org.squashtest.tm.domain.campaign.TestSuite
import org.squashtest.tm.domain.execution.Execution
import org.squashtest.tm.domain.execution.ExecutionStep
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.service.internal.campaign.IterationTestPlanManager
import org.squashtest.tm.service.internal.campaign.TestSuiteExecutionProcessingServiceImpl;
import org.squashtest.tm.service.internal.repository.TestSuiteDao
import org.squashtest.tm.service.project.ProjectsPermissionFinder;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.user.UserAccountService;

import spock.lang.Specification
import spock.lang.Unroll

class TestSuiteExecutionProcessingServiceImplTest  extends Specification {
	TestSuiteExecutionProcessingServiceImpl manager = new TestSuiteExecutionProcessingServiceImpl()
	TestSuiteDao testSuiteDao = Mock()
	IterationTestPlanManager testPlanManager = Mock()
	UserAccountService userService = Mock()
	PermissionEvaluationService permissionEvaluationService = Mock()

	def setup() {
		manager.suiteDao = testSuiteDao
		manager.testPlanManager = testPlanManager
		manager.userService = userService
		manager.permissionEvaluationService = permissionEvaluationService
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
		testSuiteDao.findOne(10) >> suite

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

	@Unroll("should there have more test cases in test plan ? #moreExecutable !")
	def "should have more test cases in test plan"() {
		given:
		TestSuite testSuite = Mock()
		Project project = Mock()
		testSuite.getProject() >> project
		project.getId()>>1L
		testSuiteDao.findOne(10) >> testSuite

		and:
		testSuite.isLastExecutableTestPlanItem(20, _) >> lastExecutable


		when:
		def more = manager.hasMoreExecutableItems(10, 20)

		then:
		more == moreExecutable

		where:
		lastExecutable | moreExecutable
		false          | true
		true           | false
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
		testSuiteDao.findOne(10) >> suite

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
