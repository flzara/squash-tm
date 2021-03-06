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

import org.hibernate.Query
import org.junit.runner.RunWith
import org.spockframework.runtime.Sputnik
import org.springframework.context.MessageSource
import org.springframework.transaction.annotation.Transactional
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.squashtest.tm.domain.campaign.Iteration
import org.squashtest.tm.domain.campaign.IterationTestPlanItem
import org.squashtest.tm.domain.campaign.TestPlanStatus
import org.squashtest.tm.domain.campaign.TestSuite
import org.squashtest.tm.domain.customfield.CustomFieldValue
import org.squashtest.tm.domain.customfield.RenderingLocation
import org.squashtest.tm.domain.denormalizedfield.DenormalizedFieldHolderType
import org.squashtest.tm.domain.execution.Execution
import org.squashtest.tm.domain.execution.ExecutionStatus
import org.squashtest.tm.exception.DuplicateNameException
import org.squashtest.tm.service.customfield.CustomFieldValueFinderService
import org.unitils.dbunit.annotation.DataSet
import spock.lang.Unroll
import spock.unitils.UnitilsSupport

import javax.inject.Inject
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

@UnitilsSupport
@Transactional
@RunWith(Sputnik)
class IterationModificationServiceIT extends DbunitServiceSpecification {

	@Inject
	IterationModificationService iterService

	@Inject
	CustomFieldValueFinderService customFieldValueFinderService

	@PersistenceContext
	EntityManager em

	@DataSet("IterationModificationServiceIT.should copy-paste a TestSuite.xml")
	def "should copy-paste a TestSuite"() {
		given:
		def testSuiteId = -1L
		def iterationId = -10L

		when:
		TestSuite copyOfSuite = iterService.copyPasteTestSuiteToIteration(testSuiteId, iterationId)

		then:
		copyOfSuite.getIteration().getId() == iterationId
		copyOfSuite.getTestPlan().size() == 2
		copyOfSuite.getName() == "suite de test 1"
		copyOfSuite.getId() != -1L
		copyOfSuite.getId() != null
		copyOfSuite.getTestPlan().each { it.getExecutions().size() == 0 }
		copyOfSuite.getTestPlan().each { it.getExecutionStatus() == ExecutionStatus.READY }
		copyOfSuite.getTestPlan().each { it.getIteration().getId() == iterationId }
	}

	@DataSet("IterationModificationServiceIT.should copy-paste a TestSuite and rename it.xml")
	def "should copy-paste a TestSuite and rename it depending on TestSuites at destination"() {
		given:
		def testSuiteId = -1L
		def iterationId = -1L

		when:
		TestSuite copyOfSuite = iterService.copyPasteTestSuiteToIteration(testSuiteId, iterationId)

		then:
		copyOfSuite.getName() == "suite de test 1-Copie1"
	}

	@DataSet("IterationModificationServiceIT.should copy-paste 2 TestSuites.xml")
	def "should copy-paste 2 TestSuites"() {
		given:
		def testSuite1Id = -1L
		def testSuite2Id = -2L
		def iterationId = -10L
		def Long[] testSuiteIds = new Long[2]
		testSuiteIds[0] = testSuite1Id
		testSuiteIds[1] = testSuite2Id

		when:
		List<TestSuite> copyOfSuites = iterService.copyPasteTestSuitesToIteration(testSuiteIds, iterationId)

		then:
		copyOfSuites.size() == 2
		copyOfSuites.get(0).getIteration().getId() == iterationId
		copyOfSuites.get(0).getTestPlan().size() == 2
		copyOfSuites.get(0).getName() == "suite de test 1"
		copyOfSuites.get(0).getId() != -1L
		copyOfSuites.get(0).getId() != null
		copyOfSuites.get(0).getTestPlan().each { it.getExecutions().size() == 0 }
		copyOfSuites.get(0).getTestPlan().each { it.getExecutionStatus() == ExecutionStatus.READY }
		copyOfSuites.get(0).getTestPlan().each { it.getIteration().getId() == iterationId }
		copyOfSuites.get(1).getIteration().getId() == iterationId
		copyOfSuites.get(1).getName() == "suite de test 2"
		copyOfSuites.get(1).getId() != -2L
		copyOfSuites.get(1).getId() != null
	}


	@DataSet("IterationModificationServiceIT.denormalizedField.xml")
	def "should create an execution and copy the custom fields"() {

		when:
		Execution exec = iterService.addExecution(-1L)

		then: "5 denormalized fields are created"
		Query query1 = getSession().createQuery("from DenormalizedFieldValue dfv")
		query1.list().size() == 5

		and: "3 denormalized fields are linked to execution"
		Query query = getSession().createQuery("from DenormalizedFieldValue dfv where dfv.denormalizedFieldHolderId = :id and dfv.denormalizedFieldHolderType = :type order by dfv.position")
		query.setParameter("id", exec.id)
		query.setParameter("type", DenormalizedFieldHolderType.EXECUTION)
		def result = query.list()
		result.size() == 3

		and: "denormalized fields are in right order"
		result.get(0).value == "T"
		result.get(1).value == "U"
		result.get(2).value == "V"

		and: "2 denormalized fields are linked to execution"
		query.setParameter("id", exec.steps.get(0).id)
		query.setParameter("type", DenormalizedFieldHolderType.EXECUTION_STEP)
		def result2 = query.list()
		result2.size() == 2

		and: "denormalized fields are in right order"
		result.get(0).value == "T"
		result.get(1).value == "U"
	}

	//TODO
	@DataSet("IterationModificationServiceIT.addManualKeywordExecutionWithDataset.xml")
	def "should create a keyword execution with dataset"() {
		given:
		def msgSource = Mock(MessageSource)

		when:
		msgSource.getMessage(_, null, _) >> "??tant donn?? que"
		Execution exec = iterService.addExecution(-1L, msgSource)

		then:
		def steps = exec.steps
		steps.size() == 1
		def step1 = steps[0]
		def step1Action = step1.getAction()
		step1Action == "??tant donn?? que Today is Friday"
	}

	@DataSet("IterationModificationServiceIT.denormalizedField.xml")
	def "should denormalize execution step fields"() {
		when:
		Execution exec = iterService.addExecution(-2L)

		Query query = getSession().createQuery("from DenormalizedFieldValue dfv where dfv.denormalizedFieldHolderId = :id and dfv.denormalizedFieldHolderType = :type order by dfv.position")
		query.setParameter("id", exec.id)
		query.setParameter("type", DenormalizedFieldHolderType.EXECUTION)

		then:
		query.list().size() == 3

		and:
		query.setParameter("id", exec.steps.get(0).id)
		query.setParameter("type", DenormalizedFieldHolderType.EXECUTION_STEP)
		def step1denofields = query.list()

		then: "fields from test step should be denormalized"
		step1denofields.size() == 2
		step1denofields*.code == ["cufUprim", "cufT"]
		step1denofields*.value == ["Uprim", "T"]
		step1denofields*.renderingLocations == [[RenderingLocation.STEP_TABLE] as Set, [] as Set]

		and: "fields from test step and called test step should be denormalized"
		query.setParameter("id", exec.steps.get(1).id)
		query.setParameter("type", DenormalizedFieldHolderType.EXECUTION_STEP)
		def step2denofields = query.list()

		then:
		step2denofields.size() == 2
		step2denofields.collect { it.code } == ["cufT", "cufU"]
		step2denofields.collect { it.value } == ["T", "U"]
		step2denofields.collect {
			it.renderingLocations
		} == [[RenderingLocation.STEP_TABLE] as Set, [RenderingLocation.STEP_TABLE] as Set]
	}


	@DataSet("IterationModificationServiceIT.should create a suite with custom fields.xml")
	def "should create a suite with custom fields"() {
		given:
		TestSuite suite = new TestSuite(name: "fishnet")

		when:
		iterService.addTestSuite(-1L, suite)
		em.flush()

		then:
		suite.getUuid() != null
		List<CustomFieldValue> customFieldValues =
			customFieldValueFinderService.findAllCustomFieldValues(suite.getBoundEntityId(), suite.getBoundEntityType())
		customFieldValues.size() == 1
		customFieldValues.get(0).value == "winklepicker"
	}

	@DataSet("IterationModificationServiceIT.add exec to itp.xml")
	def "should create a new execution for the test case in the iteration"() {
		given:
		def iterationId = -1L
		def itemTestPlanId = -1L

		when:
		iterService.addExecution(itemTestPlanId)

		then:
		IterationTestPlanItem item = findEntity(IterationTestPlanItem.class, itemTestPlanId)
		item.getExecutions().size() == 2
	}

	@Unroll
	@DataSet("IterationModificationServiceIT.create executions of different types.xml")
	def "Should create executions of different types"() {
		given:
			def execQuery = em.createQuery("select count(*) from Execution")
			def scriptedExecQuery = em.createQuery("select count(*)  from ScriptedExecution")
			def keywordExecQuery = em.createQuery("select count(*)  from KeywordExecution")
		and:
			execQuery.getSingleResult() == 0
			scriptedExecQuery.getSingleResult() == 0
			keywordExecQuery.getSingleResult() == 0
		when:
			iterService.addExecution(itpiId)
		then:
			execQuery.getSingleResult() == expectNbreExec
			scriptedExecQuery.getSingleResult() == expectNbreScriptedExec
			keywordExecQuery.getSingleResult() == expectNbreKeywordExecution
		where:
		// -1L is Standard Exec ; -2L is a Scripted Exec ; -3L is a Keyword Exec
		itpiId 	| expectNbreExec 	| expectNbreScriptedExec 	| expectNbreKeywordExecution
		-1L		| 1					|0							| 0
		-2L		| 1					|1							| 0
		-3L		| 1					|0							| 1
	}

	@DataSet("IterationModificationServiceIT update Item Plan with last execution data.xml")
	def "Should update Item Plan with last execution data 4"() {
		given:
		def exec3 = findEntity(Execution.class, -3L)
		def testPlanId = -1L

		when:
		//you add an execution, the values are still null
		iterService.addExecution(testPlanId)
		IterationTestPlanItem tp = exec3.getTestPlan()
		def lastExecutedBy4 = tp.lastExecutedBy
		def lastExecutedOn4 = tp.lastExecutedOn

		then:
		//the execution data are null if a new execution was set
		lastExecutedBy4 == null
		lastExecutedOn4 == null

	}

	@DataSet("IterationModificationServiceIT.addSuite.xml")
	def "should add a TestSuite to an iteration"() {

		given:
		def suite = new TestSuite()
		suite.name = "suite"
		def iterationId = -1L

		when:
		iterService.addTestSuite(iterationId, suite)

		then:
		def iteration = findEntity(Iteration.class, iterationId)
		def resuite = iteration.getTestSuites()
		resuite.size() == 1
		resuite[0].iteration.id == iterationId
	}

	@DataSet("IterationModificationServiceIT.1suite.xml")
	def "should rant because there is a conflict in suite names"() {

		given:
		def iterationId = -1L

		and:
		def resuite = new TestSuite()
		resuite.name = "suite"

		when:
		iterService.addTestSuite(iterationId, resuite)

		then:
		thrown DuplicateNameException
	}

	@DataSet("IterationModificationServiceIT.should move TestSuites.xml")
	def "should move one test suite in the iteration"() {

		given:
		def iterationId = -1L
		List<Long> itemIds = Arrays.asList(-3L)

		when:
		iterService.changeTestSuitePosition(iterationId, 0, itemIds)

		then:
		def iteration = findEntity(Iteration.class, iterationId)
		def testSuites = iteration.getTestSuites()
		testSuites.size() == 3
		testSuites[0].id == -3L
		testSuites[1].id == -1L
		testSuites[2].id == -2L
	}

	@DataSet("IterationModificationServiceIT.should move TestSuites.xml")
	def "should move two test suites in the iteration"() {

		given:
		def iterationId = -1L
		List<Long> itemIds = Arrays.asList(-3L,-2L)

		when:
		iterService.changeTestSuitePosition(iterationId, 0, itemIds)

		then:
		def iteration = findEntity(Iteration.class, iterationId)
		def testSuites = iteration.getTestSuites()
		testSuites.size() == 3
		testSuites[2].id == -1L
	}

	@DataSet("IterationModificationServiceIT.should find Iteration statistics.xml")
	def "should find Iteration statistics"() {

		given:
		def iterationId = -1L

		when:
		def result = iterService.findIterationStatistics(iterationId)

		then:
		result.nbTestCases == 7
		result.nbRunning == 1
		result.nbSuccess == 1
		result.nbBlocked == 1
		result.nbFailure == 1
		result.nbSettled == 1
		result.nbUntestable == 1
		result.nbReady == 1
		result.nbDone == 5
		result.status == TestPlanStatus.RUNNING
		result.progression == 71
	}
}
