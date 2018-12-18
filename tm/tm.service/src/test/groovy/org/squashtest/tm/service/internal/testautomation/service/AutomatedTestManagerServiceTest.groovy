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
import org.squashtest.tm.service.internal.repository.TestAutomationServerDao
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
	ScmRepository scm = initScmRepository()

	def cleanupSpec(){
		FileUtils.forceDelete(scm.baseRepositoryFolder)
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
		def res = service.listTestsFromServer( [ project1, project2])

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


	// **************** listing tests from Git **********************


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



	// note : this test and the method it tests should rather be relocated in the ScmRepository entity itself
	def "should return the list of test in the squash folder as relative to the base repository path"(){

		when :
		def res = service.collectTestRelativePath(scm).collect(Collectors.toList()).sort()

		then:
		res == ["squash/220_test2.ta", "squash/815_test1.ta", "squash/subfolder/999_test3.ta"]

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

	// ******************** scaffolding *****************************


	/*
	 * Defines a repository as follow
	 *
	 * /(TMP_DIR)/ATMSTest_{random int}
	 * 	+ squash
	 * 	|	+ 815_test1.ta
	 * 	|	+ 220_test2.ta
	 * 	|	- subfolder
	 * 	|		- 999_test3.ta
	 * 	+ unrelated_file.txt
	 * 	+ unrelated_folder
	 * 		+another_file.txt
	 */
	def initScmRepository(){

		// create the file structure
		File base = Files.createTempDirectory("ATMSTest_").toFile()
		base.deleteOnExit()

		FileTreeBuilder builder = new FileTreeBuilder(base)
		builder{
			dir("squash") {
				file "815_test1.ta"
				file "220_test2.ta"
				dir ("subfolder"){
					file "999_test3.ta"
				}
			}
			file "unrelated_file.txt"
			dir("unrelated_folder"){
				file "another_file.txt"
			}

		}

		// now we can create the ScmRepository

		return new ScmRepository( name: "my repository", repositoryPath: base.absolutePath, workingFolderPath: "squash")


	}


}
