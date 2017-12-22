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


import javax.inject.Provider

import org.squashtest.tm.core.foundation.lang.Couple
import org.squashtest.tm.domain.campaign.Campaign
import org.squashtest.tm.domain.campaign.Iteration
import org.squashtest.tm.domain.customfield.CustomField
import org.squashtest.tm.domain.customfield.CustomFieldBinding
import org.squashtest.tm.domain.customfield.CustomFieldValue
import org.squashtest.tm.domain.execution.Execution
import org.squashtest.tm.domain.execution.ExecutionStatus
import org.squashtest.tm.domain.testautomation.AutomatedExecutionExtender
import org.squashtest.tm.domain.testautomation.AutomatedSuite
import org.squashtest.tm.domain.testautomation.AutomatedTest
import org.squashtest.tm.domain.testautomation.TestAutomationProject
import org.squashtest.tm.domain.testautomation.TestAutomationServer
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.service.customfield.CustomFieldValueFinderService
import org.squashtest.tm.service.internal.testautomation.AutomatedSuiteManagerServiceImpl
import org.squashtest.tm.service.internal.testautomation.AutomatedTestManagerServiceImpl
import org.squashtest.tm.service.internal.testautomation.FetchTestListFuture
import org.squashtest.tm.service.internal.testautomation.FetchTestListTask
import org.squashtest.tm.service.internal.testautomation.TaParametersBuilder
import org.squashtest.tm.service.internal.testautomation.TestAutomationConnectorRegistry
import org.squashtest.tm.service.internal.testautomation.TestAutomationTaskExecutor
import org.squashtest.tm.service.internal.testautomation.AutomatedSuiteManagerServiceImpl.ExtenderSorter
import org.squashtest.tm.service.security.PermissionEvaluationService
import org.squashtest.tm.service.testautomation.model.TestAutomationProjectContent
import org.squashtest.tm.service.testautomation.spi.TestAutomationConnector
import org.squashtest.tm.service.testautomation.spi.UnknownConnectorKind

import spock.lang.Specification

class AutomatedSuiteManagerServiceTest extends Specification {


	TestAutomationConnectorRegistry connectorRegistry
	AutomatedSuiteManagerServiceImpl service
	PermissionEvaluationService permService


	CustomFieldValueFinderService finder = Mock()
	Provider builderProvider = Mock()

	def setup(){


		connectorRegistry = Mock()
		permService = Mock()
		permService.hasRoleOrPermissionOnObject(_, _, _) >> true
		permService.hasRoleOrPermissionOnObject(_, _, _, _) >> true

		service = new AutomatedSuiteManagerServiceImpl()
		service.connectorRegistry = connectorRegistry
		service.permissionService = permService

		service.customFieldValueFinder = finder
		builderProvider.get() >> new TaParametersBuilder()
		service.paramBuilder = builderProvider
	}




	def "should collect tests from extender list"(){
		given :
		def exts = []
		3.times { exts << mockExtender() }

		def tests = exts*.automatedTest

		and:
		finder.findAllCustomFieldValues(_) >> []

		when :
		def res = service.collectAutomatedExecs(exts)

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

		when:
		Couple couple = service.createAutomatedExecAndParams(extender)

		then:
		couple.a1 == extender
		couple.a2["TC_CUF_FIELD"] == "VALUE"
		couple.a2["IT_CUF_FIELD"] == "VALUE"
		couple.a2["CPG_CUF_FIELD"] == "VALUE"
	}



	private AutomatedExecutionExtender mockExtender(realExec) {
		AutomatedExecutionExtender extender = Mock()

		AutomatedTest automatedTest = Mock()
		extender.getAutomatedTest() >> automatedTest

		Execution exec = Mock()
		exec.iteration >> Mock(Iteration)
		exec.campaign >> Mock(Campaign)

		extender.getExecution() >> exec

		TestCase tc = Mock()

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
