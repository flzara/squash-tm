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

import org.apache.commons.io.FileUtils
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.domain.scm.ScmRepository
import org.squashtest.tm.domain.testcase.TestCaseKind
import org.squashtest.tm.service.internal.repository.TestAutomationServerDao
import org.squashtest.tm.service.scmserver.ScmRepositoryManifest
import org.squashtest.tm.service.testutils.MockFactory
import spock.lang.Shared

import javax.inject.Provider


import org.squashtest.tm.domain.testautomation.TestAutomationProject
import org.squashtest.tm.domain.testautomation.TestAutomationServer
import org.squashtest.tm.service.internal.testautomation.AutomatedTestManagerServiceImpl
import org.squashtest.tm.service.internal.testautomation.FetchTestListFuture
import org.squashtest.tm.service.internal.testautomation.FetchTestListTask
import org.squashtest.tm.service.internal.testautomation.TestAutomationConnectorRegistry
import org.squashtest.tm.service.internal.testautomation.TestAutomationTaskExecutor
import org.squashtest.tm.service.testautomation.model.TestAutomationProjectContent

import spock.lang.Specification

import java.nio.file.Files
import java.util.stream.Collectors

class AutomatedTestManagerServiceTest extends Specification {


	TestAutomationConnectorRegistry connectorRegistry;
	AutomatedTestManagerServiceImpl service;
	TestAutomationTaskExecutor executor;

	@Shared
	ScmRepository repo1 = new MockFactory().mockScmRepository("ATMSTest_", "squash"){
		dir("squash"){
			file "123_feat_gherkin.feature"
			file "456_another_gherkin.feature"
			file "1642_unknown_tech.whatever"
		}
	}

	@Shared
	ScmRepository repo2 = new MockFactory().mockScmRepository("ATMSTest2_", "scripts"){
		dir("scripts"){
			file "789_ghertest1.feature"
			file "159_ghertest2.feature"
			file "1642_whatsis.dunno"
		}
	}



	def cleanupSpec(){
		FileUtils.forceDelete(repo1.baseRepositoryFolder)
		FileUtils.forceDelete(repo2.baseRepositoryFolder)
	}


	def setup(){
		connectorRegistry = Mock()
		executor = Mock()
		service = new AutomatedTestManagerServiceImpl()
		service.connectorRegistry = connectorRegistry
		service.executor = executor;

	}


	// ************** listing tests from server *********************

	def "should gather tests from remote server"(){


		given : "the automation projects"
		TestAutomationProject project1 = Mock()
		TestAutomationProject project2 = Mock()


		and : "the futures"
		FetchTestListFuture future1 = Mock ()
		FetchTestListFuture future2 = Mock ()


		and : "the results"
		TestAutomationProjectContent result1 = Mock()
		TestAutomationProjectContent result2 = Mock()


		when :
		def res = service.listTestsFromRemoteServers( [ project1, project2])

		then :

		res == [result1, result2]

		1 * executor.sumbitFetchTestListTask({ it -> it.project == project1}) >> future1
		1 * executor.sumbitFetchTestListTask({ it -> it.project == project2}) >> future2


		1 * future1.get(_,_) >> result1
		1 * future2.get(_,_) >> result2

	}

	def "should handle a fetch task failure"(){

		given :
		// has to mock a lot of things because of the logging
		TestAutomationProject project = new TestAutomationProject(
			"main job",
			new TestAutomationServer("main server", new URL("http://testauto.org"), "admin", "admin")
		)
		FetchTestListTask task = new FetchTestListTask(connectorRegistry, project)

		and :
		Exception ex = new Exception()
		FetchTestListFuture fail = Mock{
			get(_,_) >> { throw ex }
			getTask() >> task
		}

		when :
		def content = service.extractFromFuture(fail)

		then :
		content.project == project
		content.knownProblem == ex

	}


	// **************** listing tests from Scm **********************

	def "should group the automation projects by SCM"(){

		given : "the scms"
		def scm1 = Mock(ScmRepository)
		def scm2 = Mock(ScmRepository)

		and : "the projects"
		def projectWithScm1 = new Project(scmRepository: scm1)
		def projectWithScm2 = new Project(scmRepository: scm2)
		def projectWithoutScm = new Project(scmRepository: null)

		and : "the automation projects"
		def auto11 = new TestAutomationProject(tmProject: projectWithScm1)
		def auto12 = new TestAutomationProject(tmProject: projectWithScm1)

		def auto21 = new TestAutomationProject(tmProject: projectWithScm2)
		def auto22 = new TestAutomationProject(tmProject: projectWithScm2)

		def auto3 = new TestAutomationProject(tmProject: projectWithoutScm)


		when :
		def res = service.automationProjectsGroupByScm([auto21, auto12, auto11, auto3, auto22])

		then:
		res[scm1] as Set == [auto11, auto12] as Set
		res[scm2] as Set == [auto21, auto22] as Set
		res.values().flatten().contains(auto3) == false


	}


	def "group tests by technology"(){

		expect:
		service.groupTestsByTechnology(repo1) == [
			(TestCaseKind.STANDARD) : ["squash/1642_unknown_tech.whatever"],
			(TestCaseKind.GHERKIN)	: ["squash/123_feat_gherkin.feature", "squash/456_another_gherkin.feature"]
		]
	}

	def "group automation projects by technology and make them AutomationProjectContent"(){

		given :
		def reg1 = new TestAutomationProject(label: "regular 1", canRunGherkin: false)
		def gher1 = new TestAutomationProject(label: "gherkin 1", canRunGherkin: true)
		def reg2 = new TestAutomationProject(label: "regular 2", canRunGherkin: false)
		def gher2 = new TestAutomationProject(label: "gherkin 2", canRunGherkin: true)

		when :
		def res = service.groupProjectsByTechnology([gher2, reg1, reg2, gher1])

		then :
		res.values().flatten().every {it.class == TestAutomationProjectContent }
		res[TestCaseKind.STANDARD].collect {it.project} == [reg1, reg2]
		res[TestCaseKind.GHERKIN].collect {it.project} == [gher1, gher2]

	}


	def "should create the automation project contents for 2 scms"(){

		given : "the projects"
		def projectWithScm1 = new Project(scmRepository: repo1)
		def projectWithScm2 = new Project(scmRepository: repo2)
		def projectWithoutScm = new Project(scmRepository: null)

		and : "the automation projects"
		def gher1 = new TestAutomationProject(label: "gherkin 1", tmProject: projectWithScm1, canRunGherkin: true)
		def reg1 = new TestAutomationProject(label: "regular 1",tmProject: projectWithScm1, canRunGherkin: false)

		def reg2 = new TestAutomationProject(label: "regular 2", tmProject: projectWithScm2, canRunGherkin: false)
		def gher2 = new TestAutomationProject(label: "gherkin 2", tmProject: projectWithScm2, canRunGherkin: true)

		def noscm = new TestAutomationProject(tmProject: projectWithoutScm)


		when :
		def contents = service.listTestsFromScm([gher2, reg1, noscm, reg2, gher1])

		then :

		// check gher1 first, must contain gherkin tests from the scm1
		def contentGher1 = contents.find {it.project == gher1 }
		contentGher1.tests.collect {it.fullLabel }.sort() == [
			"/gherkin 1/squash/123_feat_gherkin.feature",
			"/gherkin 1/squash/456_another_gherkin.feature"
		]

		// same check for project regular 1, with regular tests from scm1
		def contentReg1 = contents.find { it.project == reg1 }
		contentReg1.tests.collect {it.fullLabel }.sort() == [
			"/regular 1/squash/1642_unknown_tech.whatever"
		]

		// reg2 contains regular tests from scm2
		def contentReg2 = contents.find { it.project == reg2}
		contentReg2.tests.collect {it.fullLabel }.sort() == [
			"/regular 2/scripts/1642_whatsis.dunno"
		]

		// gher2 contains gherkin tests from scm2
		def contentGher2 = contents.find {it.project == gher2 }
		contentGher2.tests.collect {it.fullLabel }.sort() == [
			"/gherkin 2/scripts/159_ghertest2.feature",
			"/gherkin 2/scripts/789_ghertest1.feature"
		]

		// and no result for the "no scm" project
		contents.find {it.project == noscm } == null


	}

/*
	def "should return distinct scms referenced by the various test automation projects"(){

		given :

		def projects = [
			Mock(TestAutomationProject){
				getTmProject() >> Mock(Project){
					getScmRepository()>> scm
				}
				getLabel() >> "I reference the SCM"
			},
			Mock(TestAutomationProject){
				getTmProject() >> Mock(Project){
					getScmRepository()>> null
				}
				getLabel() >> "I reference no scm"
			},
			Mock(TestAutomationProject){
				getTmProject() >> Mock(Project){
					getScmRepository()>> Mock(ScmRepository){ getName() >> "different repo"}
				}
				getLabel() >> "I reference a different SCM"
			},
			Mock(TestAutomationProject){
				getTmProject() >> Mock(Project){
					getScmRepository()>> scm
				}
				getLabel() >> "I reference the same SCM than the first item"
			}
		]

		when :
		def res = service.gatherRepositories(projects)

		then :
		res.size() == 2
		res.collect {it.name} as Set == ["my repository", "different repo"] as Set

	}




	def "should not list from repository if there are no repository to list from"(){

		given :
		TestAutomationProject p1 = Mock(TestAutomationProject){
			isCanRunGherkin() >> true
			getTmProject() >> Mock(Project){
				getScmRepository() >> null
			}
		}

		when :
		def res = service.listTestsFromScm([p1])

		then :
		! res.isPresent()

	}


	def "should not list from repository if there are no Gherkin-able projects"(){

		given :
		TestAutomationProject p1 = Mock(TestAutomationProject){
			isCanRunGherkin() >> false
			getTmProject() >> Mock(Project){
				getScmRepository()>> scm
			}
		}

		when :
		def res = service.listTestsFromScm([p1])

		then:
		! res.isPresent()

	}


	def "should return the content of the scm as automated tests that belong to a Gherkin-able project"(){

		given: "a server"
		TestAutomationServer server = new TestAutomationServer("leroy_jenkins", new URL("http://myci:8080/jenkins"), "admin", "admin")

		and : "the projects"

		def projects = [
			Mock(TestAutomationProject){
				getServer() >> server
				isCanRunGherkin() >> false
				getTmProject() >> Mock(Project){
					getScmRepository()>> scm
				}
				getLabel() >> "Something else"
			},
			Mock(TestAutomationProject){
				getServer() >> server
				isCanRunGherkin() >> true
				getTmProject() >> Mock(Project){
					getScmRepository()>> scm
				}
				getLabel() >> "Gherkin Project"
			}
		]

		when :
		def res = service.listTestsFromScm(projects)

		then :
		res.isPresent()

		def projectContent = res.get()
		projectContent.project.getLabel() == "Gherkin Project"
		projectContent.tests.collect { it.fullLabel }.sort() == [
		    "/Gherkin Project/squash/220_test2.ta",
			"/Gherkin Project/squash/815_test1.ta",
			"/Gherkin Project/squash/subfolder/999_test3.ta"].sort();
		projectContent.tests.collect {it.project }.unique().label == ["Gherkin Project"]


	}
*/

}
