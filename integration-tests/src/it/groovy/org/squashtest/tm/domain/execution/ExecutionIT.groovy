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

import org.junit.runner.RunWith
import org.spockframework.runtime.Sputnik
import org.springframework.transaction.annotation.Transactional
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.squashtest.tm.domain.campaign.IterationTestPlanItem
import org.squashtest.tm.domain.execution.Execution
import org.squashtest.tm.domain.execution.ExecutionStatus
import org.unitils.dbunit.annotation.DataSet
import spock.lang.Unroll
import spock.unitils.UnitilsSupport

import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

@UnitilsSupport
@Transactional
@RunWith(Sputnik)
class ExecutionIT extends DbunitServiceSpecification {

	@PersistenceContext
	EntityManager em

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

	@DataSet
	def "Should find all different execution types"() {
		when:
		def res = em
			.createQuery("from Execution")
			.getResultList()
		then:
		res.size() == 2
	}

	@DataSet
	def "Should find a single classic execution"() {
		when:
		def res = em.find(Execution.class, -10L)
		then:
		res != null
		res.id == -10
		res.name == "execution-10"
	}

	@DataSet
	def "Should not find a single classic execution as a scripted execution"() {
		when:
		def res = em.find(ScriptedExecution.class, -10L)
		then:
		res == null
	}

	@DataSet
	def "Should find a single scripted execution as an execution"() {
		when:
		def res = em.find(Execution.class, -20L)
		then:
		res != null
		res.id == -20
		res.name == "execution-20"
	}

	@DataSet
	def "Should find a single scripted execution as a scripted execution"() {
		when:
		def res = em.find(ScriptedExecution.class, -20L)
		then:
		res != null
		res.id == -20
		res.name == "execution-20"
	}

	@DataSet
	def "Should correctly use an execution visitor"() {
		given:
			def res = ["hello", "how are you?", "goodbye"]
			def visitor = new ExecutionVisitor() {
				@Override
				void visit(Execution execution) {
					res[0] = "execution"
				}

				@Override
				void visit(ScriptedExecution scriptedExecution) {
					res[1] = "scriptedExecution"
				}
			}
		and:
			def execution = em.find(Execution.class, -10L)
			def scriptedExecution = em.find(Execution.class, -20L)
		when:
			visitor.visit(execution)
			visitor.visit(scriptedExecution)
		then:
			res[0] == "execution"
			res[1] == "scriptedExecution"
	}
}
