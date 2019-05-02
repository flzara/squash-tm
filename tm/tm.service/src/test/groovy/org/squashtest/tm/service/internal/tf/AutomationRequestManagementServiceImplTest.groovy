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

import org.squashtest.tm.domain.testautomation.AutomatedTest
import org.squashtest.tm.domain.testautomation.TestAutomationProject
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.domain.tf.automationrequest.AutomationRequest
import org.squashtest.tm.service.internal.repository.AutomationRequestDao
import org.squashtest.tm.service.internal.repository.TestCaseDao
import org.squashtest.tm.service.testautomation.model.TestAutomationProjectContent
import org.squashtest.tm.service.testcase.TestCaseModificationService
import spock.lang.Specification

class AutomationRequestManagementServiceImplTest extends Specification {
	AutomationRequestManagementServiceImpl service = new AutomationRequestManagementServiceImpl()

	AutomationRequestDao automationRequestDao = Mock()

	TestCaseDao testCaseDao = Mock()
	TestCaseModificationService testCaseModificationService = Mock()

	def setup(){
		service.testCaseDao = testCaseDao
		service.requestDao = automationRequestDao
		service.testCaseModificationService = testCaseModificationService
	}

	def "Should find no TA script to associate with test case and remove previous auto associated script"(){
		given:
		TestCase targetTC = Mock()
		targetTC.id >> -1L
		targetTC.uuid >> "uuidNotFound"
		targetTC.automatedTest >> Mock(AutomatedTest)

		AutomationRequest automationRequest = Mock()
		automationRequest.manual >> false
		targetTC.automationRequest >> automationRequest

		testCaseDao.findById(-1L) >> targetTC
		testCaseModificationService.findAssignableAutomationTests(-1L) >> createAssignableTestList()

		when:
		service.updateScriptTa(-1L)

		then:
		1 * testCaseModificationService.removeAutomation(-1L)
		1 * automationRequestDao.updateIsManual(-1L, true)
	}

	def "Should find no TA script to associate with test case and not remove previous auto associated script"(){
		given:
		TestCase targetTC = Mock()
		targetTC.id >> -1L
		targetTC.uuid >> "uuidNotFound"
		targetTC.automatedTest >> Mock(AutomatedTest)

		AutomationRequest automationRequest = Mock()
		automationRequest.manual >> true
		targetTC.automationRequest >> automationRequest

		testCaseDao.findById(-1L) >> targetTC
		testCaseModificationService.findAssignableAutomationTests(-1L) >> createAssignableTestList()

		when:
		service.updateScriptTa(-1L)

		then:
		0 * testCaseModificationService.removeAutomation(-1L)
		1 * automationRequestDao.updateIsManual(-1L, true)
	}

	def "Should find one TA script to associate with test case"(){
		given:
		TestCase targetTC = Mock()
		targetTC.id >> -1L
		targetTC.uuid >> "uuid1"
		targetTC.automatedTest >> Mock(AutomatedTest)

		AutomationRequest automationRequest = Mock()
		automationRequest.manual >> true
		targetTC.automationRequest >> automationRequest

		testCaseDao.findById(-1L) >> targetTC
		testCaseModificationService.findAssignableAutomationTests(-1L) >> createAssignableTestList()

		when:
		service.updateScriptTa(-1L)

		then:
		1 * testCaseModificationService.bindAutomatedTest(-1L, -1L, "test1")
		1 * automationRequestDao.updateIsManual(-1L, false)
	}

	def "Should find more than one TA script to associate with test case"(){
		given:
		TestCase targetTC = Mock()
		targetTC.id >> -1L
		targetTC.uuid >> "uuid2"
		targetTC.automatedTest >> Mock(AutomatedTest)

		AutomationRequest automationRequest = Mock()
		automationRequest.manual >> true
		targetTC.automationRequest >> automationRequest

		testCaseDao.findById(-1L) >> targetTC
		testCaseModificationService.findAssignableAutomationTests(-1L) >> createAssignableTestList()

		when:
		service.updateScriptTa(-1L)

		then:
		1 * testCaseModificationService.removeAutomation(-1L)
		1 * automationRequestDao.updateIsManual(-1L, false)
		1 * automationRequestDao.updateConflictAssociation(-1L, "jobTA/test1,jobTA/test2")
	}

	def createAssignableTestList() {
		TestAutomationProject automationProject = Mock()
		automationProject.jobName >> "jobTA"
		automationProject.id >> -1L
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

		[automatedTest1, automatedTest2, automatedTest3].each {it.project >> automationProject}

		TestAutomationProjectContent projectContent = Mock()
		projectContent.project >> automationProject
		projectContent.tests >> [automatedTest1, automatedTest2, automatedTest3]

		return [projectContent]

	}
}
