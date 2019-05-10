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
package org.squashtest.tm.service.internal.tf

import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.domain.testautomation.AutomatedTest
import org.squashtest.tm.domain.testautomation.TestAutomationProject
import org.squashtest.tm.domain.testautomation.TestAutomationServer
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.domain.testcase.TestCaseAutomatable
import org.squashtest.tm.domain.tf.automationrequest.AutomationRequest
import org.squashtest.tm.service.internal.repository.AutomationRequestDao
import org.squashtest.tm.service.internal.repository.TestCaseDao
import org.squashtest.tm.service.internal.testautomation.UnsecuredAutomatedTestManagerService
import org.squashtest.tm.service.testautomation.model.TestAutomationProjectContent
import org.squashtest.tm.service.testcase.TestCaseModificationService
import spock.lang.Specification

class AutomationRequestManagementServiceImplTest extends Specification {
	AutomationRequestManagementServiceImpl service = new AutomationRequestManagementServiceImpl()

	AutomationRequestDao automationRequestDao = Mock()

	TestCaseDao testCaseDao = Mock()
	TestCaseModificationService testCaseModificationService = Mock()
	UnsecuredAutomatedTestManagerService taService = Mock()

	def setup(){
		service.testCaseDao = testCaseDao
		service.requestDao = automationRequestDao
		service.testCaseModificationService = testCaseModificationService
		service.taService = taService
	}

	def "Should find no TA script to associate with test case and remove previous auto associated script"(){
		given:
		// A test Case
		TestCase targetTC = Mock()
		targetTC.id >> -1L
		targetTC.uuid >> "uuidNotFound"
		targetTC.automatedTest >> Mock(AutomatedTest)
		targetTC.automatable >> TestCaseAutomatable.Y

		// An automation Request
		AutomationRequest automationRequest = Mock()
		automationRequest.manual >> false
		targetTC.automationRequest >> automationRequest

		// A project
		Project project = Mock()
		project.isAllowAutomationWorkflow() >> true
		project.testAutomationProjects >> []
		targetTC.project >> project

		// A test automation server
		TestAutomationServer server = Mock()
		server.id >> -1L
		project.testAutomationServer >> server

		testCaseDao.findAllByIdsWithProject(Collections.singletonList(-1L)) >> [targetTC]
		taService.listTestsFromRemoteServers(_) >> createAssignableTestList()

		when:
		service.updateTAScript(Collections.singletonList(-1L))

		then:
		1 * testCaseModificationService.removeAutomation(-1L)
		1 * automationRequestDao.updateIsManual(-1L, true)
	}

	def "Should find no TA script to associate with test case and not remove previous auto associated script"(){
		given:

		// A TestCase
		TestCase targetTC = Mock()
		targetTC.id >> -1L
		targetTC.uuid >> "uuidNotFound"
		targetTC.automatedTest >> Mock(AutomatedTest)
		targetTC.automatable >> TestCaseAutomatable.Y


		// An AutomationRequest
		AutomationRequest automationRequest = Mock()
		automationRequest.manual >> true
		targetTC.automationRequest >> automationRequest

		// A project
		Project project = Mock()
		project.isAllowAutomationWorkflow() >> true
		project.testAutomationProjects >> []
		targetTC.project >> project

		// A TestAutomationServer
		TestAutomationServer server = Mock()
		server.id >> -1L
		project.testAutomationServer >> server

		testCaseDao.findAllByIdsWithProject(Collections.singletonList(-1L)) >> [targetTC]
		taService.listTestsFromRemoteServers(_) >> createAssignableTestList()

		when:
		service.updateTAScript(Collections.singletonList(-1L))

		then:
		0 * testCaseModificationService.removeAutomation(-1L)
		1 * automationRequestDao.updateIsManual(-1L, true)
	}

	def "Should find one TA script to associate with test case"(){
		given:

		// A TestCase
		TestCase targetTC = Mock()
		targetTC.id >> -1L
		targetTC.uuid >> "uuid1"
		targetTC.automatedTest >> Mock(AutomatedTest)
		targetTC.automatable >> TestCaseAutomatable.Y

		// An AutomationRequest
		AutomationRequest automationRequest = Mock()
		automationRequest.manual >> true
		targetTC.automationRequest >> automationRequest

		// A project
		Project project = Mock()
		project.isAllowAutomationWorkflow() >> true
		targetTC.project >> project

		// An AutomationProject
		TestAutomationProject automationProject = Mock()
		automationProject.jobName >> "jobTA"
		automationProject.id >> -1L
		project.testAutomationProjects >> [automationProject]


		// An AutomationServer
		TestAutomationServer server = Mock()
		server.id >> -1L
		automationProject.server >> server
		project.testAutomationServer >> server

		testCaseDao.findAllByIdsWithProject(Collections.singletonList(-1L)) >> [targetTC]
		taService.listTestsFromRemoteServers(_) >> createAssignableTestList()

		when:
		service.updateTAScript(Collections.singletonList(-1L))

		then:
		1 * testCaseModificationService.bindAutomatedTestAutomatically(-1L, -1L, "test1")
		1 * automationRequestDao.updateIsManual(-1L, false)
	}

	def "Should find more than one TA script to associate with test case"(){
		given:
		// A TestCase
		TestCase targetTC = Mock()
		targetTC.id >> -1L
		targetTC.uuid >> "uuid2"
		targetTC.automatedTest >> Mock(AutomatedTest)
		targetTC.automatable >> TestCaseAutomatable.Y

		// An AutomationRequest
		AutomationRequest automationRequest = Mock()
		automationRequest.manual >> true
		targetTC.automationRequest >> automationRequest

		// A project
		Project project = Mock()
		project.isAllowAutomationWorkflow() >> true
		targetTC.project >> project

		// An AutomationProject
		TestAutomationProject automationProject = Mock()
		automationProject.jobName >> "jobTA"
		automationProject.id >> -1L
		project.testAutomationProjects >> [automationProject]

		// An AutomationServer
		TestAutomationServer server = Mock()
		server.id >> -1L
		automationProject.server >> server
		project.testAutomationServer >> server

		testCaseDao.findAllByIdsWithProject(Collections.singletonList(-1L)) >> [targetTC]
		taService.listTestsFromRemoteServers(_) >> createAssignableTestList()

		when:
		service.updateTAScript(Collections.singletonList(-1L))

		then:
		1 * testCaseModificationService.removeAutomation(-1L)
		1 * automationRequestDao.updateIsManual(-1L, false)
		1 * automationRequestDao.updateConflictAssociation(-1L, "jobTA/test1#jobTA/test2")
	}

	def "For multiple testcases update, should ask for automation server's test list the minimum time"(){
		given:
		// Two TestCases

		TestCase targetTC = Mock()
		targetTC.id >> -1L
		targetTC.uuid >> "uuid1"
		targetTC.automatedTest >> Mock(AutomatedTest)
		targetTC.automatable >> TestCaseAutomatable.Y

		TestCase targetTC2 = Mock()
		targetTC2.id >> -2L
		targetTC2.uuid >> "uuid2"
		targetTC2.automatedTest >> Mock(AutomatedTest)
		targetTC2.automatable >> TestCaseAutomatable.Y

		// Two AutomationRequest
		AutomationRequest automationRequest = Mock()
		automationRequest.manual >> true
		targetTC.automationRequest >> automationRequest

		AutomationRequest automationRequest2 = Mock()
		automationRequest2.manual >> true
		targetTC2.automationRequest >> automationRequest2

		// A project
		Project project = Mock()
		project.isAllowAutomationWorkflow() >> true
		targetTC.project >> project
		targetTC2.project >> project

		// An AutomationProject
		TestAutomationProject automationProject = Mock()
		automationProject.jobName >> "jobTA"
		automationProject.id >> -1L
		project.testAutomationProjects >> [automationProject]

		// An AutomationServer
		TestAutomationServer server = Mock()
		server.id >> -1L
		automationProject.server >> server
		project.testAutomationServer >> server

		testCaseDao.findAllByIdsWithProject([-1L, -2L]) >> [targetTC, targetTC2]

		when:
		service.updateTAScript([-1L, -2L])

		then:
		1 * taService.listTestsFromRemoteServers(_) >> { arguments ->
			final Collection<TestAutomationProject> projects = arguments[0]
			assert projects.size() == 1
			return createAssignableTestList()
		}
		1 * testCaseModificationService.bindAutomatedTestAutomatically(-1L, -1L, "test1")
		1 * automationRequestDao.updateIsManual(-1L, false)

		1 * automationRequestDao.updateConflictAssociation(-2L, "jobTA/test1#jobTA/test2")
		1 * testCaseModificationService.removeAutomation(-2L)
		1 * automationRequestDao.updateIsManual(-2L, false)
	}

	def createAssignableTestList() {

		// Create a mocked TestAutomationProject
		TestAutomationProject automationProject = Mock()
		automationProject.jobName >> "jobTA"
		automationProject.id >> -1L

		// With a mocked AutomationServer
		TestAutomationServer server = Mock()
		server.id >> -1L
		automationProject.server >> server

		// Create a list of mocked AutomatedTest
		AutomatedTest automatedTest1 = Mock()
		automatedTest1.name >> "test1"
		automatedTest1.getFullName() >> "jobTA/test1"
		automatedTest1.linkedTC >> ["uuid1", "uuid2"]
		AutomatedTest automatedTest2 = Mock()
		automatedTest2.name >> "test2"
		automatedTest2.getFullName() >> "jobTA/test2"
		automatedTest2.linkedTC >> ["uuid2"]
		AutomatedTest automatedTest3 = Mock()
		automatedTest3.name >> "test3"
		automatedTest3.getFullName() >> "jobTA/test3"
		automatedTest3.linkedTC >> []

		// Linked them to the previously created AutomationProject
		[automatedTest1, automatedTest2, automatedTest3].each {it.project >> automationProject}

		// Create the corresponding TestAutomationProjectContent
		TestAutomationProjectContent projectContent = Mock()
		projectContent.project >> automationProject
		projectContent.tests >> [automatedTest1, automatedTest2, automatedTest3]

		return [projectContent]

	}
}
