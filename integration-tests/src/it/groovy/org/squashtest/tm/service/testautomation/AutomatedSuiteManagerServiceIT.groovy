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
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.squashtest.tm.core.foundation.collection.ColumnFiltering
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder
import org.squashtest.tm.core.foundation.collection.PagingAndMultiSorting
import org.squashtest.tm.core.foundation.collection.Sorting
import org.squashtest.tm.core.foundation.lang.Couple
import org.squashtest.tm.domain.campaign.IterationTestPlanItem
import org.squashtest.tm.domain.campaign.TestSuite
import org.squashtest.tm.domain.execution.Execution
import org.squashtest.tm.domain.execution.ExecutionStatus
import org.squashtest.tm.domain.project.GenericProject
import org.squashtest.tm.domain.testautomation.AutomatedExecutionExtender
import org.squashtest.tm.domain.testautomation.AutomatedSuite
import org.squashtest.tm.domain.testautomation.TestAutomationServer
import org.squashtest.tm.service.internal.repository.AutomatedExecutionExtenderDao
import org.squashtest.tm.service.internal.repository.AutomatedSuiteDao
import org.squashtest.tm.service.internal.repository.AutomatedTestDao
import org.squashtest.tm.service.internal.repository.CustomFieldDao
import org.squashtest.tm.service.internal.repository.CustomFieldValueDao
import org.squashtest.tm.service.internal.repository.DenormalizedFieldValueDao
import org.squashtest.tm.service.internal.repository.ExecutionDao
import org.squashtest.tm.service.internal.repository.ExecutionStepDao
import org.squashtest.tm.service.internal.repository.IterationTestPlanDao
import org.squashtest.tm.service.internal.repository.TestSuiteDao
import org.unitils.dbunit.annotation.DataSet
import spock.unitils.UnitilsSupport

import javax.inject.Inject
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@NotThreadSafe
@UnitilsSupport
@Transactional
class AutomatedSuiteManagerServiceIT extends DbunitServiceSpecification {

	@Inject
	AutomatedSuiteManagerService service

	@Inject
	AutomatedSuiteDao automatedSuiteDao

	@Inject
	AutomatedExecutionExtenderDao automatedExecutionExtenderDao

	@Inject
	IterationTestPlanDao itpiDao

	@Inject
	ExecutionDao executionDao

	@Inject
	ExecutionStepDao executionStepDao

	@Inject
	CustomFieldValueDao customFieldValueDao

	@Inject
	DenormalizedFieldValueDao denormalizedFieldValueDao

	@Inject
	TestSuiteDao suiteDao

	@Inject
	AutomatedTestDao automatedTestDao;

	@DataSet("TestAutomationService.sandbox.xml")
	def "should return executions associated to an automated test suite given its id"(){
		when:
		def res = service.findExecutionsByAutomatedTestSuiteId("suite1")
		then:
		res[0].id == -41L
		res[1].id == -40L
	}

	def getServer(id){
		return getSession().load(TestAutomationServer.class, id)
	}

	def getProject(id){
		return getSession().load(GenericProject.class, id)
	}

	@DataSet("TestAutomationService.TFtrigger.xml")
    def "should return automated test suite associated to an iteration given a test plan items list"() {
    	given:
        def testItemsList = itpiDao.findAllByIdsOrderedByIterationTestPlan([-201L, -202L, -203L])

        when:
		AutomatedSuite suite = service.createFromIterationTestPlanItems(testItemsList.get(0).getIteration().getId(), testItemsList)

        then:
		suite.executionExtenders.size() == 3
		suite.executionExtenders[0].id == 1L
		suite.executionExtenders[0].automatedTest.id == -71L
	}

	@DataSet("TestAutomationService.TFtrigger.xml")
	def "should return collection of tests with params (associated with an iteration)"() {
		given:
		def testItemsList = itpiDao.findAllByIdsOrderedByIterationTestPlan([-201L, -202L, -203L])

		AutomatedSuite suite = service.createFromIterationTestPlanItems(testItemsList.get(0).getIteration().getId(), testItemsList)

		when:
		Collection<Couple<AutomatedExecutionExtender, Map<String, Object>>> executionOrder = service.prepareExecutionOrder(suite, true)

		then:
		executionOrder.size() == 3
		executionOrder[0].a1.execution.referencedTestCase.uuid == "5bb09a58-72fd-4630-95fa-1b4651052c6a"
		executionOrder[0].a1.automatedTest.name == "test 1"

		executionOrder[0].a2.containsKey("TC_UUID")
		executionOrder[0].a2.get("TC_UUID") == "5bb09a58-72fd-4630-95fa-1b4651052c6a"

		executionOrder[0].a2.containsKey("TC_REFERENCE")
		executionOrder[0].a2.get("TC_REFERENCE") == "ref"
	}

	@DataSet("TestAutomationService.TFtrigger.xml")
	def "should return automated test suite associated to a test suite given a test plan items list"() {
		given:
		def testItemsList = itpiDao.findAllByIdsOrderedBySuiteTestPlan([-201L, -202L, -203L], -21L)
		when:
		AutomatedSuite suite = service.createFromTestSuiteTestPlanItems(testItemsList.get(0).getTestSuites().get(0).getId(), testItemsList)

		then:
		suite.executionExtenders.size() == 3
		suite.executionExtenders[0].id == 7L
		suite.executionExtenders[0].automatedTest.id == -71L
	}

	@DataSet("TestAutomationService.TFtrigger.xml")
	def "should return collection of tests with params (associated with a test suite)"() {
		given:
		def testItemsList = itpiDao.findAllByIdsOrderedBySuiteTestPlan([-201L, -202L, -203L], -21L)

		AutomatedSuite suite = service.createFromTestSuiteTestPlanItems(testItemsList.get(0).getTestSuites().get(0).getId(), testItemsList)

		when:
		Collection<Couple<AutomatedExecutionExtender, Map<String, Object>>> executionOrder = service.prepareExecutionOrder(suite, true)

		then:
		executionOrder.size() == 3
		executionOrder[0].a1.execution.referencedTestCase.uuid == "5bb09a58-72fd-4630-95fa-1b4651052c6a"
		executionOrder[0].a1.automatedTest.name == "test 1"

		executionOrder[0].a2.containsKey("TC_UUID")
		executionOrder[0].a2.get("TC_UUID") == "5bb09a58-72fd-4630-95fa-1b4651052c6a"

		executionOrder[0].a2.containsKey("TC_REFERENCE")
		executionOrder[0].a2.get("TC_REFERENCE") == "ref"

		executionOrder[0].a2.containsKey("TC_CUF_accreditation")
		executionOrder[0].a2.get("TC_CUF_accreditation") == "toto"

		executionOrder[0].a2.containsKey("IT_CUF_public")
		executionOrder[0].a2.get("IT_CUF_public") == "true"

		executionOrder[0].a2.containsKey("CPG_CUF_taglist")
		executionOrder[0].a2.get("CPG_CUF_taglist") == "SEC-2"

		executionOrder[0].a2.containsKey("TS_CUF_checkbox")
		executionOrder[0].a2.get("TS_CUF_checkbox") == "false"

		executionOrder[0].a2.containsKey("DSNAME")
		executionOrder[0].a2.get("DSNAME") == "dataset1"

		executionOrder[0].a2.containsKey("DS_param101")
		executionOrder[0].a2.get("DS_param101") == "titi"
	}

	@DataSet("TestAutomationService.findPagedList.xml")
	def "should return paged collection of automated suite given an iteration ID"() {
		given:
		PagingAndMultiSorting paging = new TestPagingMultiSorting()
		ColumnFiltering filtering = ColumnFiltering.UNFILTERED

		when:
		PagedCollectionHolder<List<AutomatedSuite>> pagedSuites = service.getAutomatedSuitesByIterationID(-11L, paging, filtering)

		then:

		pagedSuites.totalNumberOfItems == 2
		pagedSuites.firstItemIndex == 0
		pagedSuites.pagedItems.size() == 2

	}

	@DataSet("TestAutomationService.findPagedList.xml")
	def "should return paged collection of automated suite given a test suite ID"() {
		given:
		PagingAndMultiSorting paging = new TestPagingMultiSorting()
		ColumnFiltering filtering = ColumnFiltering.UNFILTERED

		when:
		PagedCollectionHolder<List<AutomatedSuite>> pagedSuites = service.getAutomatedSuitesByTestSuiteID(-21L, paging, filtering)

		then:

		pagedSuites.totalNumberOfItems == 1
		pagedSuites.firstItemIndex == 0
		pagedSuites.pagedItems.size() == 1
		pagedSuites.pagedItems[0].getId() == "123"
		pagedSuites.pagedItems[0].executionExtenders.size() == 3

	}

	@DataSet("TestAutomationService.deleteOldAutomatedSuites.xml")
	def "Should delete all old automated suites and keep the new ones"() {
		when: "create new automated suites with their new automated execution extenders"
			1.upto(10) {
				service.createFromIterationTestPlan(-1L)
			}
		then:
			List<IterationTestPlanItem> itemsBefore = itpiDao.findAll()
			itemsBefore.size() == 1
			itemsBefore.get(0).getExecutions().size() == 110
			itemsBefore.get(0).setExecutionStatus(ExecutionStatus.SUCCESS)
			automatedSuiteDao.findAll().size() == 60
			automatedExecutionExtenderDao.count() == 110
			List<Execution> executionsBefore = executionDao.findAll()
			executionsBefore.size() == 110
			executionStepDao.findAll().size() == 3
			customFieldValueDao.count() == 13
			denormalizedFieldValueDao.count() == 12
			List<TestSuite> suitesBefore = suiteDao.findAll()
			suitesBefore.size() == 1
			suitesBefore.get(0).executionStatus == ExecutionStatus.SUCCESS
			automatedTestDao.findAll().size() == 2
		when:
			def startTime = LocalDateTime.now()
			service.cleanOldSuites()
			def endTime = LocalDateTime.now()
		then:
			List<IterationTestPlanItem> itemsAfter = itpiDao.findAll()
			itemsAfter.size() == 1
			itemsAfter.get(0).getExecutions().size() == 10
			itemsAfter.get(0).executionStatus == ExecutionStatus.READY
			automatedSuiteDao.findAll().size() == 10
			automatedExecutionExtenderDao.findAll().size() == 10
			List<Execution> executionsAfter = executionDao.findAll()
			executionsAfter.size() == 10
			executionsAfter.every({ it.getSteps().size() == 0 })
			executionStepDao.findAll().size() == 0
			customFieldValueDao.count() == 11
			denormalizedFieldValueDao.count() == 10
			List<TestSuite> suitesAfter = suiteDao.findAll()
			suitesAfter.size() == 1
			suitesAfter.get(0).executionStatus == ExecutionStatus.READY
			automatedTestDao.findAll().size() == 1
			ChronoUnit.MILLIS.between(startTime, endTime) < 2000
	}

	def "Should not throw any Exception if no old automated suites are to delete"() {
		given: "an empty dataset"
		when:
			service.cleanOldSuites()
		then:
			noExceptionThrown()
	}
}

class TestPagingMultiSorting implements PagingAndMultiSorting{

	@Override
	int getFirstItemIndex() {
		return 0
	}

	@Override
	int getPageSize() {
		return 50
	}

	@Override
	boolean shouldDisplayAll() {
		return false
	}

	@Override
	List<Sorting> getSortings() {
		return Collections.emptyList()
	}

}
