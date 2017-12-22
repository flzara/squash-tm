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
package org.squashtest.tm.service.testautomation

import org.spockframework.util.NotThreadSafe
import org.springframework.transaction.annotation.Transactional
import org.squashtest.tm.domain.execution.Execution
import org.squashtest.tm.domain.project.GenericProject
import org.squashtest.tm.domain.testautomation.AutomatedExecutionExtender
import org.squashtest.tm.domain.testautomation.AutomatedSuite
import org.squashtest.tm.domain.testautomation.TestAutomationProject
import org.squashtest.tm.domain.testautomation.TestAutomationServer
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.exception.NameAlreadyInUseException
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.unitils.dbunit.annotation.DataSet
import spock.unitils.UnitilsSupport

import javax.inject.Inject

@NotThreadSafe
@UnitilsSupport
@Transactional
public class TestAutomationServerManagerServiceIT extends DbunitServiceSpecification {

	@Inject
	private TestAutomationServerManagerService service

	@DataSet("TestAutomationServerManagerServiceIT.not bound.xml")
	def "should find has not bound project" (){
		given :
		def serverId = -1L
		when :
		boolean result = service.hasBoundProjects(serverId)
		then:
		!result
	}

	@DataSet("TestAutomationServerManagerServiceIT.not bound.xml")
	def "should find has no execution1" (){
		given :
		def serverId = -1L
		when :
		boolean result = service.hasExecutedTests(serverId)
		then:
		!result
	}

	@DataSet("TestAutomationServerManagerServiceIT.bound.xml")
	def "should find has no execution2" (){
		given :
		def serverId = -1L
		when :
		boolean result = service.hasExecutedTests(serverId)
		then:
		!result
	}


	@DataSet("TestAutomationServerManagerServiceIT.bound.xml")
	def "should find has bound1" (){
		given :
		def serverId = -1L
		when :
		boolean result = service.hasBoundProjects(serverId)
		then:
		result
	}

	@DataSet("TestAutomationServerManagerServiceIT.executed.xml")
	def "should find has bound2" (){
		given :
		def serverId = -11L
		when :
		boolean result = service.hasBoundProjects(serverId)
		then:
		result
	}

	@DataSet("TestAutomationServerManagerServiceIT.executed.xml")
	def "should find has execution" (){
		given :
		def serverId = -11L
		when :
		boolean result = service.hasExecutedTests(serverId)
		then:
		result
	}
	@DataSet("TestAutomationServerManagerServiceIT.not bound.xml")
	def "should change description" (){
		given :
		def serverId = -1L
		def newDesc = "new description"
		when :
		service.changeDescription(serverId, newDesc)
		then:
		TestAutomationServer tas = findEntity(TestAutomationServer.class, serverId)
		tas.description == newDesc
	}


	@DataSet("TestAutomationServerManagerServiceIT.not bound.xml")
	def "should change login" (){
		given :
		def serverId = -1L
		def newLogin = "newLogin"
		when :
		service.changeLogin(serverId, newLogin)
		then:
		TestAutomationServer tas = findEntity(TestAutomationServer.class, serverId)
		tas.login == newLogin
	}

	@DataSet("TestAutomationServerManagerServiceIT.not bound.xml")
	def "should change password" (){
		given :
		def serverId = -1L
		def newPassword = "password"
		when :
		service.changePassword(serverId, newPassword)
		then:
		TestAutomationServer tas = findEntity(TestAutomationServer.class, serverId)
		tas.password == newPassword
	}

	@DataSet("TestAutomationServerManagerServiceIT.not bound.xml")
	def "should change name" (){
		given :
		def serverId = -1L
		def newName = "new name"
		when :
		service.changeName(serverId, newName)
		then:
		TestAutomationServer tas = findEntity(TestAutomationServer.class, serverId)
		tas.name == newName
	}

	@DataSet("TestAutomationServerManagerServiceIT.2 not bound.xml")
	def "should throw name already in use exception" (){
		given :
		def serverId = -1L
		def newName = "new name"
		when :
		service.changeName(serverId, newName)
		then:
		thrown(NameAlreadyInUseException.class)
	}

	@DataSet("TestAutomationServerManagerServiceIT.not bound.xml")
	def "should not throw name already in use exception" (){
		given :
		def serverId = -1L
		def sameName = "Roberto-1"
		when :
		service.changeName(serverId, sameName)
		then:
		notThrown(NameAlreadyInUseException.class)
	}

	@DataSet("TestAutomationServerManagerServiceIT.not bound.xml")
	def "should delete a test automation server" (){
		given :
		def serverId = -1L
		when :
		service.deleteServer(serverId)
		then:
		!found(TestAutomationServer.class, serverId)
	}

	@DataSet("TestAutomationServerManagerServiceIT.bound.xml")
	def "should delete a tas bound to a project" (){
		given :
		def serverId = -1L
		def taProjectId = -1L
		def tmProjectId = -1L
		when :
		service.deleteServer(serverId)
		then:
		!found(TestAutomationServer.class, serverId)
		!found(TestAutomationProject.class, taProjectId)
		GenericProject tmProject = findEntity(GenericProject, tmProjectId)
		tmProject.getTestAutomationServer() == null
		tmProject.getTestAutomationProjects().size() == 0
	}

	@DataSet("TestAutomationServerManagerServiceIT.executed.xml")
	def "should delete a tas with executions" (){
		given :
		def serverId = -11L
		def taProjectId = -10L
		def tmProjectId = -1L
		def tmTestId = -13L
		def taTestId = -12L
		def executionId = -15L
		def automatedSuiteId = "-16"
		def automatedExecutionExtenderId = -17L
		when :
		service.deleteServer(serverId)
		then:
		!found(TestAutomationServer.class, serverId)
		!found(TestAutomationProject.class, taProjectId)
		GenericProject tmProject = findEntity(GenericProject, tmProjectId)
		tmProject.getTestAutomationServer() == null
		tmProject.getTestAutomationProjects().size() == 0
		found(Execution.class, executionId)
		TestCase test = findEntity(TestCase.class, tmTestId)
		test !=null
		test.getAutomatedTest() == null
		found(AutomatedSuite.class, automatedSuiteId)
		AutomatedExecutionExtender aee = findEntity(AutomatedExecutionExtender.class, automatedExecutionExtenderId)
		aee != null
		aee.getResultURL() == null
	}
}
