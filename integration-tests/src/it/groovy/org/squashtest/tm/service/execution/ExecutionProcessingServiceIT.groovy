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
package org.squashtest.tm.service.execution

import static org.squashtest.tm.domain.execution.ExecutionStatus.*

import javax.inject.Inject

import org.spockframework.util.NotThreadSafe
import org.springframework.transaction.annotation.Transactional
import org.squashtest.tm.domain.campaign.IterationTestPlanItem
import org.squashtest.tm.domain.execution.Execution
import org.squashtest.tm.domain.execution.ExecutionStatus
import org.squashtest.tm.domain.execution.ExecutionStatusReport
import org.squashtest.tm.domain.execution.ExecutionStep
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.unitils.dbunit.annotation.DataSet

import spock.lang.Unroll
import spock.unitils.UnitilsSupport


@NotThreadSafe
@UnitilsSupport
@Transactional
class ExecutionProcessingServiceIT extends DbunitServiceSpecification {

	@Inject ExecutionProcessingService procservice


	// NOT WORKY SINCE SERVICE FUSION, DON'T KNOW WHY
	@DataSet("ExecutionModificationServiceIT.execution.xml")
	@Unroll("should iterate over step #order of the referenced test case")
	def "should iterate over the 5 steps of the referenced test case"(){

		given :
		def executionId = -1L

		when :
		ExecutionStep step = procservice.findStepAt(executionId, order)

		then :
		step.action == action
		step.executionStepOrder == order

		where:
		order | action
		0     | "action 1"
		1     | "action 2"
		2     | "action 3"
		3     | "action 4"
		4     | "action 5"
	}

	@DataSet("ExecutionModificationServiceIT.execution.xml")
	def "should raise an out of bound exception"(){
		given :
		def executionId = -1L

		when :
		def fails = procservice.findStepAt(executionId, 10)

		then :
		thrown(IndexOutOfBoundsException)
	}


	@DataSet("ExecutionModificationServiceIT.execution.xml")
	def "should update execution step comment"(){

		given :
		def executionStepId = -1L
		def newComment = "Wooooohooo I did that here too !"

		when :
		procservice.setExecutionStepComment(executionStepId, newComment)


		then :
		def	executionStep = findEntity(ExecutionStep.class, executionStepId)
		executionStep.getComment() == newComment
	}

	@DataSet("ExecutionModificationServiceIT.execution.xml")
	def "should get me the first and third step"(){
		given :
		def executionId = -1L

		when :

		def exec1 = procservice.findStepAt(executionId, 0)
		def exec3 = procservice.findStepAt(executionId, 2)

		then :
		exec1.getAction()=="action 1"
		exec3.getAction()=="action 3"
	}

	@DataSet("ExecutionProcessingServiceIT.execution.runnable.xml")
	def "should return the current step of the execution"(){

		given :
		def executionId = -1L

		when :

		def lastOne = procservice.findRunnableExecutionStep(executionId)


		then :
		lastOne.action=="action 2"
	}

	@DataSet("ExecutionModificationServiceIT.execution.xml")
	def "should bring an execution report"(){
		given :
		def executionId = -1L
		when :
		def report = procservice.getExecutionStatusReport(executionId)

		then :
		report.get(READY)==5
	}
	//TODO unroll block 1
	@DataSet("ExecutionModificationServiceIT.execution.xml")
	def "should set an execution status for an execution to UNTESTABLE"(){
		given :
		def executionId = -1L
		ExecutionStatusReport report = new ExecutionStatusReport()
		report.set(UNTESTABLE, 1)

		when :
		procservice.setExecutionStatus(executionId, report)

		then :
		def reExec = findEntity(Execution.class, executionId)
		reExec.executionStatus == ExecutionStatus.UNTESTABLE
	}

	@DataSet("ExecutionModificationServiceIT.execution.xml")
	def "should set an execution status for an execution to BLOCKED"(){
		given :
		def executionId = -1L

		and : ExecutionStatusReport report = new ExecutionStatusReport()
		report.set(UNTESTABLE, 1)
		report.set(BLOCKED, 1)
		report.set(SUCCESS, 4)

		when :
		procservice.setExecutionStatus(executionId, report)

		then :
		def reExec = findEntity(Execution.class, executionId)
		reExec.executionStatus == ExecutionStatus.BLOCKED
	}

	@DataSet("ExecutionModificationServiceIT.execution.xml")
	def "should set an execution status for an execution to FAILURE"(){
		given :
		def executionId = -1L

		and : ExecutionStatusReport report = new ExecutionStatusReport()
		report.set(UNTESTABLE, 1)
		report.set(FAILURE, 1)
		report.set(SUCCESS, 4)

		when :
		procservice.setExecutionStatus(executionId, report)

		then :
		def reExec = findEntity(Execution.class, executionId)
		reExec.executionStatus == ExecutionStatus.FAILURE
	}

	@DataSet("ExecutionModificationServiceIT.execution.xml")
	def "should set an execution status for an execution to SUCCESS"(){
		given :
		def executionId = -1L

		and : ExecutionStatusReport report = new ExecutionStatusReport()
		report.set(UNTESTABLE, 1)
		report.set(SUCCESS, 5)

		when :
		procservice.setExecutionStatus(executionId, report)

		then :
		def reExec = findEntity(Execution.class, executionId)
		reExec.executionStatus == ExecutionStatus.SUCCESS
	}

	@DataSet("ExecutionModificationServiceIT.execution.xml")
	def "should set an execution status for an execution to RUNNING"(){
		given :
		def executionId = -1L

		and :ExecutionStatusReport report = new ExecutionStatusReport()
		report.set(UNTESTABLE, 1)
		report.set(SUCCESS, 1)
		report.set(RUNNING, 3)
		report.set(READY, 1)

		when :
		procservice.setExecutionStatus(executionId, report)

		then :
		def reExec = findEntity(Execution.class, executionId)
		reExec.executionStatus == ExecutionStatus.RUNNING
	}
	@DataSet("ExecutionModificationServiceIT.execution.xml")
	def "should set an execution status for an execution to READY"(){
		given :
		def executionId = -1L

		and : ExecutionStatusReport report = new ExecutionStatusReport()
		report.set(UNTESTABLE, 1)
		report.set(READY, 5)

		when :
		procservice.setExecutionStatus(executionId, report)

		then :
		def reExec = findEntity(Execution.class, executionId)
		reExec.executionStatus == ExecutionStatus.READY
	}
	//END unroll block 1

	//TODO unroll block 2
	@DataSet("ExecutionModificationServiceIT.execution.xml")
	def "should update executionStep status and accordingly update the status of its parent execution to BLOCKED"(){

		given :
		def executionStepId = -2L
		def executionId = -1L


		when :
		procservice.changeExecutionStepStatus(executionStepId, ExecutionStatus.BLOCKED)


		then :
		Execution exec = findEntity(Execution.class, executionId)
		exec.getExecutionStatus() == ExecutionStatus.BLOCKED
	}
	@DataSet("ExecutionModificationServiceIT.execution.xml")
	def "should update executionStep status and accordingly update the status of its parent execution to FAILURE"(){

		given :
		def executionStepId = -2L
		def executionId = -1L

		when :
		procservice.changeExecutionStepStatus(executionStepId, ExecutionStatus.FAILURE)

		then :
		Execution exec = findEntity(Execution.class, executionId)
		exec.getExecutionStatus() == ExecutionStatus.FAILURE
	}

	@DataSet("ExecutionModificationServiceIT.execution.xml")
	def "should update executionStep status and accordingly update the status of its parent execution to SUCCESS"(){

		given :
		def executionStepsIds = [-1L, -2L, -3L, -4L, -5L]
		def executionId = -1L

		when :
		for (def stepId : executionStepsIds){
			procservice.changeExecutionStepStatus(stepId, ExecutionStatus.SUCCESS)
		}

		then :
		Execution exec = findEntity(Execution.class, executionId)
		exec.getExecutionStatus() == ExecutionStatus.SUCCESS
	}

	@DataSet("ExecutionModificationServiceIT.execution.xml")
	def "should update executionStep status and accordingly update the status of its parent execution to RUNNING"(){

		given :
		def executionId = -1L
		def executionStepId = -2L

		when :
		procservice.changeExecutionStepStatus(executionStepId, ExecutionStatus.SUCCESS)


		then :
		Execution exec = findEntity(Execution.class, executionId)
		exec.getExecutionStatus() == ExecutionStatus.RUNNING
	}

	@DataSet("ExecutionModificationServiceIT.execution.xml")
	def "should update executionStep status and accordingly update the status of its parent execution to READY"(){

		given :
		def executionStepId = -2L
		def executionId = -1L

		when :
		procservice.changeExecutionStepStatus(executionStepId, ExecutionStatus.READY)

		then :
		Execution exec = findEntity(Execution.class, executionId)
		exec.getExecutionStatus() == ExecutionStatus.READY
	}
	//End unroll block 2


	//TODO unroll block 3
	// NOT WORKY SINCE SERVICE FUSION, DON'T KNOW WHY
	@DataSet("ExecutionModificationServiceIT.execution.xml")
	def "after step update, execution status should swap from BLOCKED to RUNNING"(){

		given :
		def executionId = -1L
		def executionStepId = -1L
		def exec = findEntity(Execution.class, executionId)

		when :
		procservice.changeExecutionStepStatus(executionStepId, ExecutionStatus.BLOCKED)
		def firstStatus = exec.executionStatus

		procservice.changeExecutionStepStatus(executionStepId, ExecutionStatus.SUCCESS)
		def secondStatus = exec.executionStatus

		then :
		firstStatus == ExecutionStatus.BLOCKED
		secondStatus == ExecutionStatus.RUNNING
	}

	// NOT WORKY SINCE SERVICE FUSION, DON'T KNOW WHY
	@DataSet("ExecutionModificationServiceIT.execution.xml")
	def "after step update, execution status should swap from BLOCKED to READY"(){

		given :
		def executionId = -1L
		def executionStepId = -1L
		def exec = findEntity(Execution.class, executionId)

		when :
		procservice.changeExecutionStepStatus(executionStepId, ExecutionStatus.BLOCKED)
		def firstStatus = exec.executionStatus

		procservice.changeExecutionStepStatus(executionStepId, ExecutionStatus.READY)
		def secondStatus = exec.executionStatus

		then :
		firstStatus == ExecutionStatus.BLOCKED
		secondStatus == ExecutionStatus.READY
	}
	//END unroll block 3


	@DataSet("ExecutionModificationServiceIT.execution.xml")
	def "after step update, execution status should stay BLOCKED"(){

		given :
		def executionStep1Id = -1L
		def executionStep2Id = -2L
		def executionId = -1L
		def exec = findEntity(Execution.class, -1)

		when :
		procservice.changeExecutionStepStatus(executionStep1Id, ExecutionStatus.BLOCKED)

		procservice.changeExecutionStepStatus(executionStep2Id, ExecutionStatus.BLOCKED)

		def firstStatus  = exec.executionStatus

		procservice.changeExecutionStepStatus(executionStep1Id, ExecutionStatus.SUCCESS)

		def secondStatus = exec.executionStatus

		then :
		firstStatus== ExecutionStatus.BLOCKED
		secondStatus == ExecutionStatus.BLOCKED
	}

	@DataSet("ExecutionModificationServiceIT.execution.xml")
	def "after step update, execution status should swap from BLOCKED to FAILURE"(){

		given :
		def executionStep1Id = -1L
		def executionStep2Id = -2L
		def executionId = -1L
		def exec = findEntity(Execution.class, -1)

		when :
		procservice.changeExecutionStepStatus(executionStep1Id, ExecutionStatus.BLOCKED)

		procservice.changeExecutionStepStatus(executionStep2Id, ExecutionStatus.FAILURE)

		def firstStatus  = exec.executionStatus

		procservice.changeExecutionStepStatus(executionStep1Id, ExecutionStatus.SUCCESS)

		def secondStatus = exec.executionStatus

		then :
		firstStatus== ExecutionStatus.BLOCKED
		secondStatus == ExecutionStatus.FAILURE
	}
	@DataSet("ExecutionProcessingServiceIT.execution.allStepsSuccess.xml")
	def "after step update, execution status should swap from BLOCKED to SUCCESS"(){
		given :
		def executionStepId = -2L
		def executionId = -1L
		def exec = findEntity(Execution.class, executionId)

		when :
		procservice.changeExecutionStepStatus(executionStepId, ExecutionStatus.BLOCKED)
		def firstStatus = exec.executionStatus

		procservice.changeExecutionStepStatus(executionStepId, ExecutionStatus.SUCCESS)
		def secondStatus = exec.executionStatus


		then :
		firstStatus == ExecutionStatus.BLOCKED
		secondStatus == ExecutionStatus.SUCCESS
	}

	//TODO unroll block 4
	@DataSet("ExecutionProcessingServiceIT.execution.allStepsSuccess.xml")
	def "after step update, execution status should swap from SUCCESS to RUNNING"(){
		given :
		def executionStepId = -2L
		def executionId = -1L
		def exec = findEntity(Execution.class, executionId)

		when :
		procservice.changeExecutionStepStatus(executionStepId, ExecutionStatus.READY)

		then:
		exec.executionStatus == ExecutionStatus.RUNNING
	}

	@DataSet("ExecutionProcessingServiceIT.execution.allStepsSuccess.xml")
	def "after step update, execution status should swap from SUCCESS to READY"(){
		given :
		def executionStepsIds = [-1L, -2L, -3L, -4L, -5L]
		def executionId = -1L
		def exec = findEntity(Execution.class, executionId)

		when :
		for (def stepId : executionStepsIds){
			procservice.changeExecutionStepStatus(stepId,ExecutionStatus.READY)
		}

		then :
		exec.executionStatus == ExecutionStatus.READY
	}
	//END block 4
	@DataSet("ExecutionProcessingServiceIT.execution.allStepsFailure.xml")
	def "after step update, execution status should swap from FAILURE to SUCCESS"(){
		given :
		def executionStepsIds = [-1L, -2L, -3L, -4L, -5L]
		def executionId = -1L
		def exec = findEntity(Execution.class, executionId)

		when :

		def execStatusList = []

		for (def stepID : executionStepsIds){
			procservice.changeExecutionStepStatus(stepID,ExecutionStatus.SUCCESS)
			execStatusList << exec.executionStatus
		}

		then :
		execStatusList == [
			ExecutionStatus.FAILURE,
			ExecutionStatus.FAILURE,
			ExecutionStatus.FAILURE,
			ExecutionStatus.FAILURE,
			ExecutionStatus.SUCCESS
		]
	}
	@DataSet("ExecutionModificationServiceIT update Item Plan with last execution data.xml")
	def "Should update Item Plan with last execution data 1"(){
		given:
		def exec1 = findEntity(Execution.class, -1L)
		def exec1Step1Id = -1L


		when:
		//you change the status of a step in the first execution, the item test plan is not updated
		//the getLastUpdatedBy and on are null
		procservice.changeExecutionStepStatus(exec1Step1Id, ExecutionStatus.SUCCESS)
		IterationTestPlanItem tp = exec1.getTestPlan()
		def lastExecutedBy1 = tp.lastExecutedBy
		def lastExecutedOn1 = tp.lastExecutedOn



		then:
		//The item plan should not be updated if the first execution is modified
		lastExecutedBy1 == null
		lastExecutedOn1 == null


	}

	@DataSet("ExecutionModificationServiceIT update Item Plan with last execution data.xml")
	def "Should update Item Plan with last execution data 2"(){
		given:

		def exec3 = findEntity(Execution.class, -3L)
		def exec3Step1Id = -7L

		when:
		//you change the status of a step in the last execution, the item test plan is updated
		//the getLastUpdatedBy and on are not null
		procservice.changeExecutionStepStatus(exec3Step1Id, ExecutionStatus.SUCCESS)
		IterationTestPlanItem tp = exec3.getTestPlan()
		def lastExecutedBy2 = tp.lastExecutedBy
		def lastExecutedOn2 = tp.lastExecutedOn

		then:
		//The item plan should be updated only if the last execution is modified
		lastExecutedBy2 != null
		lastExecutedOn2 != null


	}

	@DataSet("ExecutionModificationServiceIT update Item Plan with last execution data.xml")
	def "Should update Item Plan with last execution data 3"(){
		given:
		def exec3 = findEntity(Execution.class, -3L)
		def exec3Step1Id = -7L

		when:

		//you set the status of the step to READY for the last execution
		//=> the getLastUpdatedBy and on are null
		procservice.changeExecutionStepStatus(exec3Step1Id, ExecutionStatus.READY)
		IterationTestPlanItem tp = exec3.getTestPlan()
		def lastExecutedBy3 = tp.lastExecutedBy
		def lastExecutedOn3 = tp.lastExecutedOn


		then:
		//The item plan should be reset if
		//=>the last execution is modified
		//=>the execution/step status is READY
		lastExecutedBy3 == null
		lastExecutedOn3 == null

	}

}
