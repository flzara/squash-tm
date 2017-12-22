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
package org.squashtest.tm.service.campaign

import javax.inject.Inject

import org.spockframework.util.NotThreadSafe
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.execution.Execution
import org.squashtest.tm.exception.execution.EmptyTestSuiteTestPlanException;
import org.squashtest.tm.exception.execution.TestPlanItemNotExecutableException
import org.squashtest.tm.security.UserContextHolder;
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.squashtest.it.stub.security.StubAuthentication;
import org.squashtest.it.stub.security.StubPermissionEvaluationService
import org.squashtest.it.stub.security.UserContextHelper;
import org.unitils.dbunit.annotation.DataSet

import spock.unitils.UnitilsSupport

@NotThreadSafe
@UnitilsSupport
@Transactional
class TestSuiteExecutionProcessingServiceIT extends DbunitServiceSpecification {

	@Inject
	private TestSuiteExecutionProcessingService service

	@Inject
	private StubPermissionEvaluationService stubPermissionEvaluationService
	
	def setup(){
		UserContextHelper.setUsername("Joe")
	}

	@DataSet("TestSuiteExecutionProcessingServiceIT.should not find exec step cause no item.xml")
	def "should try to start and not find execution because test plan empty"(){
		given :
		long testSuiteId = -1L
		when :
		Execution execution = service.startResume(testSuiteId)

		then :
		thrown EmptyTestSuiteTestPlanException
	}

	@DataSet("TestSuiteExecutionProcessingServiceIT.should not find exec step cause no item for tester.xml")
	def "should try to start and not find execution because test plan empty for tester"(){
		given :
		def testSuiteId = -1L
		stubPermissionEvaluationService.addPermissionToRefuse("READ_UNASSIGNED", TestSuite.class.getName(), -1L)

		when:
		Execution execution = service.startResume(testSuiteId)

		then:
		thrown EmptyTestSuiteTestPlanException
		stubPermissionEvaluationService.emptyPermissionsToRefuse()
	}

	@DataSet("TestSuiteExecutionProcessingServiceIT.should not find exec step cause all term.xml")
	def "should try to resume and not find execution because all terminated"(){
		given :
		long testSuiteId = -1L

		when :
		Execution execution = service.startResume(testSuiteId)

		then :
		thrown TestPlanItemNotExecutableException
	}

	@DataSet("TestSuiteExecutionProcessingServiceIT.should not find exec step cause all term for tester.xml")
	def "should try to resume and not find execution because all terminated for tester"(){
		given :
		long testSuiteId = -1L
		stubPermissionEvaluationService.addPermissionToRefuse("READ_UNASSIGNED", TestSuite.class.getName(), -1L)

		when :
		Execution execution = service.startResume(testSuiteId)

		then :
		thrown TestPlanItemNotExecutableException
		stubPermissionEvaluationService.emptyPermissionsToRefuse()
	}

	@DataSet("TestSuiteExecutionProcessingServiceIT.should not find exec step cause no step.xml")
	def "should try to resume and not find execution because all have no step"(){
		given :
		long testSuiteId = -1L

		when :
		Execution execution = service.startResume(testSuiteId)

		then :
		thrown TestPlanItemNotExecutableException
	}

	@DataSet("TestSuiteExecutionProcessingServiceIT.should not find exec step cause no step for tester.xml")
	def "should try to resume and not find execution because all have no step for tester"(){
		given :
		long testSuiteId = -1L
		stubPermissionEvaluationService.addPermissionToRefuse("READ_UNASSIGNED", TestSuite.class.getName(), -1L)

		when :
		Execution execution = service.startResume(testSuiteId)

		then :
		thrown TestPlanItemNotExecutableException
		stubPermissionEvaluationService.emptyPermissionsToRefuse()
	}

	@DataSet("TestSuiteExecutionProcessingServiceIT.should find exec step through new exec.xml")
	def "should try to resume and create new execution"(){
		given :
		long testSuiteId = -1L

		when :
		Execution execution = service.startResume(testSuiteId)

		then :
		execution != null
		execution.findFirstUnexecutedStep().action == "lipsum4"
	}

	@DataSet("TestSuiteExecutionProcessingServiceIT.should find exec step through old exec.xml")
	def "should try to resume and find old execution"(){
		given :
		long testSuiteId = -1L

		when :
		Execution execution = service.startResume(testSuiteId)

		then :
		execution != null
		execution.findFirstUnexecutedStep().getId() == -5
	}



}
