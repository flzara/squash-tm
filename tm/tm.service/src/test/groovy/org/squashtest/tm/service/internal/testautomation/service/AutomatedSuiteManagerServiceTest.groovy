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
package org.squashtest.tm.service.internal.testautomation.service

import org.squashtest.tm.core.foundation.collection.ColumnFiltering
import org.squashtest.tm.core.foundation.collection.DefaultSorting
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder
import org.squashtest.tm.core.foundation.collection.PagingAndMultiSorting
import org.squashtest.tm.core.foundation.collection.SortOrder
import org.squashtest.tm.core.foundation.collection.Sorting
import org.squashtest.tm.core.foundation.lang.Couple
import org.squashtest.tm.domain.campaign.Campaign
import org.squashtest.tm.domain.campaign.Iteration
import org.squashtest.tm.domain.campaign.IterationTestPlanItem
import org.squashtest.tm.domain.campaign.TestSuite
import org.squashtest.tm.domain.customfield.CustomField
import org.squashtest.tm.domain.customfield.CustomFieldBinding
import org.squashtest.tm.domain.customfield.CustomFieldValue
import org.squashtest.tm.domain.execution.Execution
import org.squashtest.tm.domain.execution.ExecutionStatus
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.domain.testautomation.*
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.service.customfield.CustomFieldValueFinderService
import org.squashtest.tm.service.internal.customfield.PrivateCustomFieldValueService
import org.squashtest.tm.service.internal.denormalizedField.PrivateDenormalizedFieldValueService
import org.squashtest.tm.service.internal.repository.IterationTestPlanDao
import org.squashtest.tm.service.internal.repository.ProjectDao
import org.squashtest.tm.service.internal.testautomation.AutomatedSuiteManagerServiceImpl
import org.squashtest.tm.service.internal.testautomation.AutomatedSuiteManagerServiceImpl.ExtenderSorter
import org.squashtest.tm.service.internal.testautomation.TaParametersBuilder
import org.squashtest.tm.service.internal.testautomation.TestAutomationConnectorRegistry
import org.squashtest.tm.service.security.PermissionEvaluationService
import org.squashtest.tm.service.testautomation.spi.TestAutomationConnector
import org.squashtest.tm.service.testautomation.spi.UnknownConnectorKind
import org.squashtest.tm.service.internal.repository.AutomatedSuiteDao
import org.squashtest.tm.service.internal.repository.ExecutionDao
import spock.lang.Specification

import javax.inject.Provider
import javax.persistence.EntityManager

class AutomatedSuiteManagerServiceTest extends Specification {


	TestAutomationConnectorRegistry connectorRegistry
	AutomatedSuiteManagerServiceImpl service
	PermissionEvaluationService permService
	AutomatedSuiteDao autoSuiteDao


	CustomFieldValueFinderService finder = Mock()
	PrivateCustomFieldValueService customFieldValuesService = Mock()
	PrivateDenormalizedFieldValueService denormalizedFieldValueService = Mock()
	Provider builderProvider = Mock()
	ExecutionDao executionDaoMock = Mock()
	EntityManager entityManager = Mock()
	ProjectDao projectDao = Mock()
	IterationTestPlanDao testPlanDao = Mock()

	def setup(){


		connectorRegistry = Mock()
		permService = Mock()
		autoSuiteDao = Mock()
		permService.hasRoleOrPermissionOnObject(_, _, _) >> true
		permService.hasRoleOrPermissionOnObject(_, _, _, _) >> true
		permService.hasRole(_) >> true

		service = new AutomatedSuiteManagerServiceImpl(autoSuiteDao: autoSuiteDao,
                                                                executionDao: executionDaoMock,
                                                                customFieldValuesService: customFieldValuesService,
                                                                denormalizedFieldValueService: denormalizedFieldValueService)
		service.connectorRegistry = connectorRegistry
		service.permissionService = permService

		service.customFieldValueFinder = finder
		builderProvider.get() >> { return new TaParametersBuilder()}
		service.paramBuilder = builderProvider

		service.entityManager = entityManager
		service.projectDao = projectDao
		service.testPlanDao = testPlanDao

	}




	def "should collect tests from extender list"(){
		given :
		def exts = []
		3.times { exts << mockExtender() }

		def tests = exts*.automatedTest

		and:
		finder.findAllCustomFieldValues(_) >> []

		when :
		def res = service.collectAutomatedExecs(exts, true)

		then :
		res*.a1 == exts
		res*.a2.each { it == []}

	}



	def "should start some tests"(){
		given :
		AutomatedSuite suite = mockAutomatedSuite()

		and :
		def jenConnector = Mock(TestAutomationConnector)
		def qcConnector = Mock(TestAutomationConnector)

		and:
		autoSuiteDao.findAndFetchForAutomatedExecutionCreation(_) >> []
		finder.findAllCustomFieldValues(_) >> []

		when :
		service.start(suite)

		then :

		1 * connectorRegistry.getConnectorForKind("jenkins") >> jenConnector
		1 * connectorRegistry.getConnectorForKind("qc") >> qcConnector
		1 * jenConnector.executeParameterizedTests(_, "12345", _)
		1 * qcConnector.executeParameterizedTests(_, "12345", _)

	}



	def "should notify some executions that an error occured before they could start"(){
		given :
		AutomatedSuite suite = mockAutomatedSuite()

		suite.executionExtenders.each{
			def exec = new Execution()
			exec.automatedExecutionExtender = it
		}

		and :
		def jenConnector = Mock(TestAutomationConnector)
		def qcConnector = Mock(TestAutomationConnector)

		connectorRegistry.getConnectorForKind("jenkins") >> jenConnector
		connectorRegistry.getConnectorForKind("qc") >> { throw new UnknownConnectorKind("connector unknown") }

		and:
		autoSuiteDao.findAndFetchForAutomatedExecutionCreation(_) >> suite.executionExtenders
		finder.findAllCustomFieldValues(_) >> []

		and:
		def errors = 0
		suite.executionExtenders.each { it.setExecutionStatus(_) >> { st -> st == ExecutionStatus.ERROR ?: errors++ } }

		when :
		service.start(suite)

		then :
		1 * jenConnector.executeParameterizedTests(_, "12345", _)
		errors == 6
	}

        def "should return collection of tests with params"() {
                given :
		AutomatedSuite suite = mockAutomatedSuite()

		and:
		finder.findAllCustomFieldValues(_) >> []

		when :
		Collection<Couple<AutomatedExecutionExtender, Map<String, Object>>> executionOrder = service.prepareExecutionOrder(suite, true)

		then :
                executionOrder.size() == 18
                executionOrder[0].a1.execution.referencedTestCase.uuid == "44d63d7e-11dd-44b0-b584-565b6f791fa9"
                executionOrder[0].a1.automatedTest.name == "project-jenkins-1 - test 0"
        }


	def "should return a view on an AutomatedSuite as TestAutomationProjectContent[]"(){
		given :
		AutomatedSuite suite = mockAutomatedSuite()
		TestAutomationConnector jenConnector = Mock(TestAutomationConnector)
		connectorRegistry.getConnectorForKind(_) >> jenConnector
		jenConnector.testListIsOrderGuaranteed(_)>> true

		when :
		def res = service.sortByProject(suite)
		then :
		res.collect{
			[
				it.project.jobName,
				it.tests.collect { it.name }
			]
		} 	as Set == [
			[ "project-jenkins-1", [
					"project-jenkins-1 - test 0",
					"project-jenkins-1 - test 1",
					"project-jenkins-1 - test 2",
					"project-jenkins-1 - test 3",
					"project-jenkins-1 - test 4",
					"project-jenkins-1 - test 5",
				]
			],
			[ "project-qc-1", [
					"project-qc-1 - test 0",
					"project-qc-1 - test 1",
					"project-qc-1 - test 2",
					"project-qc-1 - test 3",
					"project-qc-1 - test 4",
					"project-qc-1 - test 5",
				]
			],
			[ "project-jenkins-2", [
					"project-jenkins-2 - test 0",
					"project-jenkins-2 - test 1",
					"project-jenkins-2 - test 2",
					"project-jenkins-2 - test 3",
					"project-jenkins-2 - test 4",
					"project-jenkins-2 - test 5",
				]
			],
		] as Set
	}

	def mockAutomatedSuite(){

		AutomatedSuite suite = new AutomatedSuite()
		suite.id = "12345"

		TestAutomationServer serverJenkins = new TestAutomationServer("thejenkins", new URL("http://jenkins-ta"), "jen", "kins", "jenkins")
		TestAutomationServer serverQC = new TestAutomationServer("theQC", new URL("http://qc-ta"), "the", "QC", "qc")

		TestAutomationProject projectJ1 = new TestAutomationProject("project-jenkins-1", serverJenkins)
		TestAutomationProject projectQC1 = new TestAutomationProject("project-qc-1", serverQC)
		TestAutomationProject projectJ2 = new TestAutomationProject("project-jenkins-2", serverJenkins)

		def allTests = []

		def projects = [
			projectJ1,
			projectQC1,
			projectJ2
		]

		projects.each{ proj ->

			5.times{ num ->

				AutomatedTest test = new AutomatedTest("${proj.jobName} - test $num", proj)
				allTests << test
			}
		}

		def exts = []

		suite.addExtenders(
				projects.collect { proj ->
					// returns list of lists of exts
					return  (0..5).collect { // returns list of exts
						mockExtender()
					}
					.eachWithIndex { extender, num ->

						// performs stuff on exts and returns exts
						extender.getAutomatedProject() >> proj
						def autotest = extender.getAutomatedTest()
						autotest.getProject() >> proj
						autotest.getName() >> "${proj.jobName} - test $num"
					}
				}.flatten()
				)

		return suite
	}
	def "should create automated test and params couple"() {
		given:
		AutomatedExecutionExtender extender = mockExtender()

		and:
		CustomFieldValue value = Mock()
		value.value >> "VALUE"

		CustomFieldBinding binding = Mock()
		value.binding >> binding

		CustomField field = Mock()
		field.code >> "FIELD"
		binding.customField >> field

		finder.findAllCustomFieldValues(_) >> [value]
		AutomatedSuiteManagerServiceImpl.CustomFieldValuesForExec cufValues = Mock()
		cufValues.getValueForTestcase(_) >> [value]
		cufValues.getValueForIteration(_) >> [value]
		cufValues.getValueForCampaign(_) >> [value]
		cufValues.getValueForTestSuite(_) >> [value]

		when:
		Couple couple = service.createAutomatedExecAndParams(extender, cufValues)

		then:
		couple.a1 == extender
		couple.a2["TC_CUF_FIELD"] == "VALUE"
		couple.a2["IT_CUF_FIELD"] == "VALUE"
		couple.a2["CPG_CUF_FIELD"] == "VALUE"
	}

	def "should find automated suites for automated suites table from iteration id"() {
		given:

		PagingAndMultiSorting paging = new TestPagingMultiSorting()
		ColumnFiltering filtering = ColumnFiltering.UNFILTERED

		List<AutomatedSuite> expectedSuites = mockAutomatedSuiteList()

		autoSuiteDao.findAutomatedSuitesByIterationID(1L, paging, filtering) >> expectedSuites
		autoSuiteDao.countSuitesByIterationId(1L, filtering) >> expectedSuites.size()

		when:
		PagedCollectionHolder<List<AutomatedSuite>> pagedSuites = service.getAutomatedSuitesByIterationID(1L, paging, filtering)

		then:
		pagedSuites.firstItemIndex == 0
		pagedSuites.totalNumberOfItems == 2
		pagedSuites.pagedItems.size() == 2
		pagedSuites.pagedItems[0].getId() == "12345"
		pagedSuites.pagedItems[1].getId() == "56789"
	}

	def "should find automated suites for automated suites table from test suite id"() {
		given:

		PagingAndMultiSorting paging = new TestPagingMultiSorting()
		ColumnFiltering filtering = ColumnFiltering.UNFILTERED

		List<AutomatedSuite> expectedSuites = mockAutomatedSuiteList()

		autoSuiteDao.findAutomatedSuitesByTestSuiteID(1L, paging, filtering) >> expectedSuites
		autoSuiteDao.countSuitesByTestSuiteId(1L, filtering) >> expectedSuites.size()

		when:
		PagedCollectionHolder<List<AutomatedSuite>> pagedSuites = service.getAutomatedSuitesByTestSuiteID(1L, paging, filtering)

		then:
		pagedSuites.firstItemIndex == 0
		pagedSuites.totalNumberOfItems == 2
		pagedSuites.pagedItems.size() == 2
		pagedSuites.pagedItems[0].getId() == "12345"
		pagedSuites.pagedItems[1].getId() == "56789"
	}

	private def mockAutomatedSuiteList(){
		AutomatedSuite suite1 = Mock()
		suite1.getId() >> "12345"

		AutomatedSuite suite2 = Mock()
		suite2.getId() >> "56789"

		return [suite1, suite2]

	}

        def "should create automated suite from ITPI list and iteration uuid"() {
                given:
                List<IterationTestPlanItem> items = mockITPIList()
				testPlanDao.fetchForAutomatedExecutionCreation(_) >> items
				AutomatedSuite autoSuite = new AutomatedSuite()
				autoSuite.id = "3fb11dd8-6e5c-4020-ade9-9378ff206fbc"
				autoSuiteDao.createNewSuite() >> autoSuite
				entityManager.find(AutomatedSuite.class, "3fb11dd8-6e5c-4020-ade9-9378ff206fbc") >> autoSuite

                when:
                AutomatedSuite suite = service.createFromIterationTestPlanItems(1L, items)

                then:
                suite.executionExtenders.size() == 5
        }

		def "should create automated suite from ITPI list and test suite uuid"() {
				given:
				List<IterationTestPlanItem> items = mockITPIList()
				testPlanDao.fetchForAutomatedExecutionCreation(_) >> items
				AutomatedSuite autoSuite = new AutomatedSuite()
				autoSuite.id = "3fb11dd8-6e5c-4020-ade9-9378ff206fbc"
				autoSuiteDao.createNewSuite() >> autoSuite
				entityManager.find(AutomatedSuite.class, "3fb11dd8-6e5c-4020-ade9-9378ff206fbc") >> autoSuite

				when:
				AutomatedSuite suite = service.createFromTestSuiteTestPlanItems(1L, items)

				then:
				suite.executionExtenders.size() == 5
		}

        private List<IterationTestPlanItem> mockITPIList() {
            List<IterationTestPlanItem> itpiList = new ArrayList<>()
            5.times { num -> itpiList.add(mockITPI()) }
            return itpiList
        }

        private IterationTestPlanItem mockITPI() {
            IterationTestPlanItem itpi = Mock()
			Project mockProject = Mock()
			mockProject.getId() >> 1L
			itpi.getProject() >> mockProject
            itpi.isAutomated() >> true
            itpi.getIteration() >> mockIteration()
			itpi.getTestSuites() >> mockTestSuiteList()
            itpi.createAutomatedExecution() >> Mock(Execution) {
                getAutomatedExecutionExtender() >> Mock(AutomatedExecutionExtender)
            }
            return itpi
        }

        private Iteration mockIteration() {
            Iteration iter = Mock()
            iter.getId() >> 1L
            return iter
        }

		private TestSuite mockTestSuite() {
			TestSuite testSuite = Mock()
			testSuite.getId() >> 1L
			return testSuite
		}

		private List<TestSuite> mockTestSuiteList() {
			List<TestSuite> testSuiteList = new ArrayList<>()
			testSuiteList.add(mockTestSuite())
			return testSuiteList
		}


	private AutomatedExecutionExtender mockExtender(realExec) {
		AutomatedExecutionExtender extender = Mock()

		AutomatedTest automatedTest = Mock()
		extender.getAutomatedTest() >> automatedTest
		Execution exec = Mock()
		Iteration iteration = Mock()

		exec.iteration >>iteration
		def iterationTestPlanItem = Mock(IterationTestPlanItem)
		exec.testPlan >> iterationTestPlanItem
		exec.campaign >> Mock(Campaign)
		iterationTestPlanItem.getIteration() >> iteration
		iterationTestPlanItem.getTestSuites() >> new ArrayList<TestSuite>()
		iteration.getTestSuites() >> new ArrayList<TestSuite>()

		extender.getExecution() >> exec

		TestCase tc = Mock()
		tc.uuid >> "44d63d7e-11dd-44b0-b584-565b6f791fa9"

		exec.referencedTestCase >> tc

		return extender
	}

}

class ExtenderSorterTest extends Specification {

	def "extender sorter should sort extenders "(){
		given :
		AutomatedSuite suite = makeSomeSuite()

		when :
		def sorter = new ExtenderSorter(suite, [])

		then :
		def col1 = sorter.getNextEntry()
		def col2 = sorter.getNextEntry()

		col1.key == "jenkins"
		col2.key == "qc"

		col1.value.size() == 10

		col1.value.collect{ it.automatedTest.project }.unique()*.name as Set == [
			"project-jenkins-1",
			"project-jenkins-2"] as Set

		col2.value.size() == 5

		col2.value.collect{ it.automatedTest.project}.unique()*.name as Set == ["project-qc-1"] as Set
	}

	def makeSomeSuite() {
		AutomatedSuite suite = new AutomatedSuite()
		suite.id = "12345"

		TestAutomationServer serverJenkins = new TestAutomationServer("thejenkins", new URL("http://jenkins-ta"), "jen", "kins", "jenkins")
		TestAutomationServer serverQC = new TestAutomationServer("theQC", new URL("http://qc-ta"), "the", "QC", "qc")

		TestAutomationProject projectJ1 = new TestAutomationProject("project-jenkins-1", serverJenkins)
		TestAutomationProject projectQC1 = new TestAutomationProject("project-qc-1", serverQC)
		TestAutomationProject projectJ2 = new TestAutomationProject("project-jenkins-2", serverJenkins)

		def allTests = []

		def projects = [
			projectJ1,
			projectQC1,
			projectJ2
		]

		projects.each{ proj ->
			5.times{ num ->
				AutomatedTest test = new AutomatedTest("${proj.name} - test $num", proj)
				allTests << test
			}
		}

		def allExts = []

		allTests.each{
			def ex = new AutomatedExecutionExtender()
			ex.automatedTest = it

			allExts << ex
		}

		suite.addExtenders(allExts)

		return suite
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
