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

import org.spockframework.util.NotThreadSafe
import org.springframework.transaction.annotation.Transactional
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.squashtest.it.stub.security.StubPermissionEvaluationService
import org.squashtest.it.stub.security.UserContextHelper
import org.squashtest.tm.domain.campaign.Iteration
import org.squashtest.tm.domain.execution.Execution
import org.squashtest.tm.exception.execution.EmptyIterationTestPlanException
import org.squashtest.tm.exception.execution.TestPlanItemNotExecutableException
import org.unitils.dbunit.annotation.DataSet
import spock.unitils.UnitilsSupport

import javax.inject.Inject

@NotThreadSafe
@UnitilsSupport
@Transactional
class IterationExecutionProcessingServiceIT extends DbunitServiceSpecification {

	@Inject
	private TestPlanExecutionProcessingService<Iteration> service

	@Inject
	private StubPermissionEvaluationService stubPermissionEvaluationService

	def setup(){
		UserContextHelper.setUsername("Joe")
	}

	@DataSet("IterationExecutionProcessingServiceIT.should not find exec step cause no item.xml")
	def "should try to start and not find execution because test plan empty"(){
		given :
		long iterationId = -1L
		when :
		service.startResume(iterationId)

		then :
		thrown EmptyIterationTestPlanException
	}

	@DataSet("IterationExecutionProcessingServiceIT.should not find exec step cause no item for tester.xml")
	def "should try to start and not find execution because test plan empty for tester"(){
		given :
		def iterationId = -1L
		stubPermissionEvaluationService.addPermissionToRefuse("READ_UNASSIGNED", Iteration.class.getName(), -1L)

		when:
		service.startResume(iterationId)

		then:
		thrown EmptyIterationTestPlanException
		stubPermissionEvaluationService.emptyPermissionsToRefuse()
	}

	@DataSet("IterationExecutionProcessingServiceIT.should not find exec step cause all term.xml")
	def "should try to resume and not find execution because all terminated"(){
		given :
		long iterationId = -1L

		when :
		service.startResume(iterationId)

		then :
		thrown TestPlanItemNotExecutableException
	}

	@DataSet("IterationExecutionProcessingServiceIT.should not find exec step cause all term for tester.xml")
	def "should try to resume and not find execution because all terminated for tester"(){
		given :
		long iterationId = -1L
		stubPermissionEvaluationService.addPermissionToRefuse("READ_UNASSIGNED", Iteration.class.getName(), -1L)

		when :
		service.startResume(iterationId)

		then :
		thrown TestPlanItemNotExecutableException
		stubPermissionEvaluationService.emptyPermissionsToRefuse()
	}

	@DataSet("IterationExecutionProcessingServiceIT.should not find exec step cause no step.xml")
	def "should try to resume and not find execution because all have no step"(){
		given :
		long iterationId = -1L

		when :
		service.startResume(iterationId)

		then :
		thrown TestPlanItemNotExecutableException
	}

	@DataSet("IterationExecutionProcessingServiceIT.should not find exec step cause no step for tester.xml")
	def "should try to resume and not find execution because all have no step for tester"(){
		given :
		long iterationId = -1L
		stubPermissionEvaluationService.addPermissionToRefuse("READ_UNASSIGNED", Iteration.class.getName(), -1L)

		when :
		service.startResume(iterationId)

		then :
		thrown TestPlanItemNotExecutableException
		stubPermissionEvaluationService.emptyPermissionsToRefuse()
	}

	@DataSet("IterationExecutionProcessingServiceIT.should find exec step through new exec.xml")
	def "should try to resume and create new execution"(){
		given :
		long iterationId = -1L

		when :
		Execution execution = service.startResume(iterationId)

		then :
		execution != null
		execution.findFirstUnexecutedStep().action == "lipsum4"
	}

	@DataSet("IterationExecutionProcessingServiceIT.should find exec step through old exec.xml")
	def "should try to resume and find old execution"(){
		given :
		long iterationId = -1L

		when :
		Execution execution = service.startResume(iterationId)

		then :
		execution != null
		execution.findFirstUnexecutedStep().getId() == -5
	}



}
