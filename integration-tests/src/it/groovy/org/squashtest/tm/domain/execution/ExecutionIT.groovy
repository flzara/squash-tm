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
package org.squashtest.tm.domain.execution

import javax.inject.Inject

import org.hibernate.SessionFactory
import org.hibernate.Query
import org.junit.runner.RunWith
import org.spockframework.runtime.Sputnik
import org.springframework.transaction.annotation.Transactional
import org.squashtest.tm.domain.campaign.IterationTestPlanItem
import org.squashtest.tm.domain.campaign.TestSuite
import org.squashtest.tm.domain.execution.Execution
import org.squashtest.tm.domain.execution.ExecutionStatus
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.squashtest.tm.service.campaign.IterationModificationService
import org.squashtest.tm.domain.denormalizedfield.DenormalizedFieldHolderType
import org.squashtest.tm.domain.denormalizedfield.DenormalizedFieldValue
import org.unitils.dbunit.annotation.DataSet
import org.unitils.dbunit.annotation.ExpectedDataSet

import spock.lang.Unroll
import spock.unitils.UnitilsSupport

@UnitilsSupport
@Transactional
@RunWith(Sputnik)
class ExecutionIT extends DbunitServiceSpecification {


	@DataSet("ExecutionIT.3executions.xml")
	def "should correctly update an item test plan status when the status of an execution is updated "(){
		given :
		def iterationId = -1L
		def testPlanItemId = -2L


		def exec1 = findEntity(Execution.class, -2L)
		def exec2 = findEntity(Execution.class, -3L)
		def exec3 = findEntity(Execution.class, -4L)

		def testPlanItem = findEntity(IterationTestPlanItem.class, -2L)

		when :
		def status0 = testPlanItem.getExecutionStatus()

		exec1.setExecutionStatus(ExecutionStatus.BLOCKED)
		def status1 = testPlanItem.getExecutionStatus()

		//the last execution will impose it's status to the itemTestPlan
		//we're performing it now, not in last position, to check
		//the next execution being updated wont affect it
		exec3.setExecutionStatus(ExecutionStatus.SUCCESS)
		def status2 = testPlanItem.getExecutionStatus()


		exec2.setExecutionStatus(ExecutionStatus.BLOCKED)
		def status3 = testPlanItem.getExecutionStatus()

		then :
		status0 == ExecutionStatus.READY
		status1 == ExecutionStatus.READY
		status2 == ExecutionStatus.SUCCESS
		status3 == ExecutionStatus.SUCCESS


	}

	@Unroll
	@DataSet("ExecutionIT.3executions.xml")
	def "should retrieve a test plan from his  executions"(){
		given :
		def execId = idExec

		when :
		def exec = findEntity(Execution.class, execId)
		IterationTestPlanItem itp = exec.getTestPlan()

		then :
		itp.id == testPlanId
		
		where :
		idExec |testPlanId
		-1L | -1L
		-2L | -2L
		-3L | -2L
	}
}
