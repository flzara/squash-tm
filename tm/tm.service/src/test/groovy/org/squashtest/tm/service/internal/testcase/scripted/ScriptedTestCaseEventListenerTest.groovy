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
package org.squashtest.tm.service.internal.testcase.scripted

import org.apache.commons.io.FileUtils
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.domain.scm.ScmRepository
import org.squashtest.tm.domain.testautomation.TestAutomationProject
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.domain.testcase.TestCaseKind
import org.squashtest.tm.service.testcase.TestCaseModificationService
import org.squashtest.tm.service.testutils.MockFactory
import spock.lang.Shared
import spock.lang.Specification


class ScriptedTestCaseEventListenerTest extends Specification {

	private ScriptedTestCaseEventListener listener = new ScriptedTestCaseEventListener()
	private TestCaseModificationService tcService = Mock()

	@Shared
	private ScmRepository scm = new MockFactory().mockScmRepository(10L, "repo", "squash"){
		dir("squash"){
			file "123_ghertest.feature"
		}
	}

	def setup(){
		listener.tcService = tcService
	}

	def cleanupSpec(){
		FileUtils.forceDelete(scm.baseRepositoryFolder)
	}

	// ************** autobind event *********************

	def "should locate the first script-able server (by lexicographical order) for a bunch of test cases"(){

		given: "the automation project"

		def canScriptZ = new TestAutomationProject(label: "can script ZZZ", canRunScript: true)
		def cannotScript = new TestAutomationProject(label: "cannot script", canRunScript: false)
		def canScriptA = new TestAutomationProject(label: "can script AAA", canRunScript: true)

		and: "the TM project"

		def project = new Project(testAutomationProjects: [canScriptZ, cannotScript, canScriptA])

		and : "the test cases"

		def testcases = (1..5).collect { Mock(TestCase){ getProject() >> project }}


		when:

		def maybeScript = listener.findFirstScriptProject testcases

		then:
		maybeScript.get() == canScriptA

	}

	def "should find no Script-able automation project for a bunch of test cases"(){
		given: "the automation project"

		def cannot1 = new TestAutomationProject(label: "cannot script", canRunScript: false)
		def cannot2 = new TestAutomationProject(label: "cannot script either", canRunScript: false)
		def cannot3 = new TestAutomationProject(label: "ditto", canRunScript: false)

		and: "the TM project"

		def project = new Project(testAutomationProjects: [cannot1, cannot2, cannot3])

		and : "the test cases"

		def testcases = (1..5).collect { Mock(TestCase){ getProject() >> project }}


		when:

		def maybeScript = listener.findFirstScriptProject testcases

		then:
		! maybeScript.isPresent()
	}


	def "should autobind a test case because a Script-able automation project is available and the script was found in the scm"(){

		given :
		def auto = Mock(TestAutomationProject){
			getId() >> 10L
			getLabel() >> "script project"
			isCanRunScript() >> true
		}

		def project = new Project(label: "tm project", testAutomationProjects: [auto])

		def testCase = Mock(TestCase){
			getId() >> 123L
			getProject() >> project
			getKind() >> TestCaseKind.GHERKIN
		}

		when :
		listener.autoBindWithScm(scm, [testCase])

		then :
		1 * tcService.bindAutomatedTest(123L, 10L, "123_ghertest.feature")

	}

}
