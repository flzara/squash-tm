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
package org.squashtest.tm.domain.testcase

import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.domain.scm.ScmRepository
import org.squashtest.tm.domain.testautomation.AutomatedTest
import org.squashtest.tm.domain.testautomation.TestAutomationProject
import org.squashtest.tm.domain.testautomation.TestAutomationServer
import org.squashtest.tm.domain.testutils.MockFactory
import org.squashtest.tm.domain.tf.automationrequest.AutomationRequest
import org.squashtest.tm.domain.tf.automationrequest.AutomationRequestStatus
import org.squashtest.tm.tools.unittest.assertions.CollectionAssertions
import spock.lang.Specification
import spock.lang.Unroll

import static org.squashtest.tm.domain.testcase.TestCaseAutomatable.M
import static org.squashtest.tm.domain.testcase.TestCaseAutomatable.N
import static org.squashtest.tm.domain.testcase.TestCaseAutomatable.Y
import static org.squashtest.tm.domain.tf.automationrequest.AutomationRequestStatus.AUTOMATED
import static org.squashtest.tm.domain.tf.automationrequest.AutomationRequestStatus.AUTOMATION_IN_PROGRESS
import static org.squashtest.tm.domain.tf.automationrequest.AutomationRequestStatus.READY_TO_TRANSMIT
import static org.squashtest.tm.domain.tf.automationrequest.AutomationRequestStatus.REJECTED
import static org.squashtest.tm.domain.tf.automationrequest.AutomationRequestStatus.SUSPENDED
import static org.squashtest.tm.domain.tf.automationrequest.AutomationRequestStatus.TRANSMITTED
import static org.squashtest.tm.domain.tf.automationrequest.AutomationRequestStatus.WORK_IN_PROGRESS

class ScriptedTestCaseTest extends Specification {

	MockFactory mockFactory = new MockFactory()

	def setup() {
		CollectionAssertions.declareContainsExactly()
	}
	def "copy of a scripted test case should have the same script"() {
		given:
		ScriptedTestCase source = new ScriptedTestCase()
		source.setName("source")
		source.notifyAssociatedWithProject(mockFactory.mockProject())
		String script = "I am the script"
		source.setScript(script)

		when:
		def copy = source.createCopy()

		then:
		copy.script == script
	}

	@Unroll
	def "#isAutomatedInWorkflow() - Should tell whether the ScriptedTestCase is automated within an Automation Workflow"() {
		given: "ScriptedTestCase"
		ScriptedTestCase scriptedTestCase = new ScriptedTestCase()
		scriptedTestCase.automatable = automatable

		and: "Project"
		Project project = new Project()
		scriptedTestCase.notifyAssociatedWithProject(project)

		and: "AutomationRequest"
		AutomationRequest automationRequest = new AutomationRequest()
		automationRequest.requestStatus = automationRequestStatus
		scriptedTestCase.automationRequest = automationRequest

		and: "AutomatedTest"
		if(hasAutomatedTest) {
			TestAutomationProject testAutomationProject = new TestAutomationProject()
			project.testAutomationProjects.add(testAutomationProject)
			AutomatedTest automatedTest = new AutomatedTest()
			automatedTest.project = testAutomationProject
			scriptedTestCase.automatedTest = automatedTest
		}

		and: "TestAutomationServer"
		if(projectHasAutomationServer) {
			TestAutomationServer testAutomationServer = new TestAutomationServer()
			project.setTestAutomationServer(testAutomationServer)
		}

		and: "ScmRepository"
		if(projectHasScmRepository) {
			ScmRepository scmRepository = new ScmRepository()
			project.setScmRepository(scmRepository)
		}

		when:
		def isAutomatedInWorkflow = scriptedTestCase.isAutomatedInWorkflow()

		then:
		isAutomatedInWorkflow == resultIsAutomated

		where:
		automatable | automationRequestStatus | hasAutomatedTest | projectHasAutomationServer | projectHasScmRepository 	| resultIsAutomated

		// TestAutomatable is not 'Y'
		M           | AUTOMATED               | true             | true                       | true 						| false
		N           | AUTOMATED               | true             | true                       | true 						| false
		// AutomationRequestStatus is not 'AUTOMATED'
		Y           | TRANSMITTED             | true             | true                       | true 						| false
		Y           | AUTOMATION_IN_PROGRESS  | true             | true                       | true 						| false
		Y           | SUSPENDED               | true             | true                       | true 						| false
		Y           | REJECTED                | true             | true                       | true 						| false
		Y           | READY_TO_TRANSMIT       | true             | true                       | true 						| false
		Y           | WORK_IN_PROGRESS        | true             | true                       | true 						| false
		Y           | null                    | true             | true                       | true 						| false
		// TestCase has no AutomatedTest
		Y           | AUTOMATED               | false            | true                       | true 						| false
		// Project has no AutomationServer
		Y           | AUTOMATED               | true             | false                      | true 						| false
		// Project has no ScmRepository
		Y           | AUTOMATED               | true             | false                      | false						| false
		// Only case where result is true
		Y           | AUTOMATED               | true             | true                       | true 						| true
	}

	@Unroll("should turn '#id:#name' into '#result'")
	def "should create a gherkin filename for a test case"(){
		expect :
		def testcase = new MockTC(id, name)
		testcase.createFilename() == result

		where :
		id 		| name										|	result
		815		| "fetch my data"							|	"815_fetch_my_data.feature"
		815		| "r\u00E9cup\u00E8re mes donn\u00E9es"		|	"815_recupere_mes_donnees.feature"
		815		| "r\u00FCckgewinnung der Daten"			|	"815_ruckgewinnung_der_Daten.feature"
	}

	@Unroll("#backupFilenameFor - should turn '#id:#name' into '#result'")
	def "should create a gherkin backup filename for a test case"(){
		expect :
		new MockTC(47L).createBackupFileName() == "47.feature"
	}

	def "should build filename match pattern"() {
		expect:
		new MockTC(44L, "holaTc").buildFilenameMatchPattern() == "44(_.*)?\\.feature"
	}

	def "#computeScriptWithAppendedMetadata() - Should write the metadata with no script content"() {
		given: "SriptedTestCase"
		ScriptedTestCase scriptedTestCase = new ScriptedTestCase()
		scriptedTestCase.setImportance(TestCaseImportance.HIGH)
		scriptedTestCase.setScript("")
		and: "AutomationRequest"
		AutomationRequest automationRequest = new AutomationRequest()
		automationRequest.setRequestStatus(AutomationRequestStatus.AUTOMATION_IN_PROGRESS)
		automationRequest.setAutomationPriority(4)
		scriptedTestCase.setAutomationRequest(automationRequest)
		when:
		def res = scriptedTestCase.computeScriptWithAppendedMetadata()
		then:
		res == """# Automation priority: 4
# Automation status: AUTOMATION_IN_PROGRESS
# Test case importance: HIGH
"""
	}

	def "#computeScriptWithAppendedMetadata() - Should write the script with metadata"() {
		given: "SriptedTestCase"
		ScriptedTestCase scriptedTestCase = new ScriptedTestCase()
		scriptedTestCase.setImportance(TestCaseImportance.HIGH)
		scriptedTestCase.setScript("this is a script")
		and: "AutomationRequest"
		AutomationRequest automationRequest = new AutomationRequest()
		automationRequest.setRequestStatus(AutomationRequestStatus.AUTOMATION_IN_PROGRESS)
		automationRequest.setAutomationPriority(4)
		scriptedTestCase.setAutomationRequest(automationRequest)
		when:
		def res = scriptedTestCase.computeScriptWithAppendedMetadata()
		then:
		res == """# Automation priority: 4
# Automation status: AUTOMATION_IN_PROGRESS
# Test case importance: HIGH
this is a script"""
	}

	class MockTC extends ScriptedTestCase{
		Long overId

		MockTC(Long id) {
			overId = id
		}

		MockTC(Long id, String name) {
			overId = id
			this.name=name
		}

		public Long getId() {
			return overId
		}

		public void setId(Long newId) {
			overId=newId
		}

	}
}
