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
package org.squashtest.tm.service.internal.scmserver

import org.apache.commons.io.FileUtils
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.domain.scm.ScmRepository
import org.squashtest.tm.domain.testcase.ScriptedTestCaseExtender
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.domain.testcase.TestCaseImportance
import org.squashtest.tm.domain.testcase.TestCaseKind
import org.squashtest.tm.domain.tf.automationrequest.AutomationRequest
import org.squashtest.tm.domain.tf.automationrequest.AutomationRequestStatus
import org.squashtest.tm.service.internal.library.PathService
import org.squashtest.tm.service.scmserver.ScmRepositoryManifest
import org.squashtest.tm.service.testutils.MockFactory
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Files

class UnsecuredScmRepositoryFilesystemServiceTest extends Specification{

	private UnsecuredScmRepositoryFilesystemService service = new UnsecuredScmRepositoryFilesystemService()

	private PathService pathService = Mock(PathService);

	@Shared
	private ScmRepository scm = new MockFactory().mockScmRepository(10L, "scmtest_", "squash"){
		dir("squash"){
			file "456_lame_pun.feature"
		}
	}

	def setup() {
		service.pathService = pathService
	}

	def cleanupSpec(){
		FileUtils.forceDelete(scm.baseRepositoryFolder)
	}

	def String relativePathFromWorkingDirectory(File file) {
		return scm.getWorkingFolder().toURI().relativize(file.toURI()).toString()
	}


	def "should create a file in the scm, if it doesn't exist"(){

		when :
		File file = service.doCreateTestFile(scm, "bob")

		then:
		file.exists()
		file.name == "bob"

		cleanup:
		clean file
	}

	def "should return a file found instead of creating it, if exists"(){

		given:
		def existing = new File(scm.workingFolder, "existing")
		existing.createNewFile()

		when :
		def file = service.doCreateTestFile(scm, "existing")

		then :
		file.equals existing

		cleanup:
		clean file

	}


	def "should create a file with the nominal name for given test case"(){

		given:
		def project = Mock(Project) {
			isUseTreeStructureInScmRepo() >> false
		}
		def tc = Mock(TestCase){
			getId() >> 123L
			getName() >> "yes test case"
			getKind() >> TestCaseKind.GHERKIN
			getProject() >> project
		}

		when :
		def file = service.createTestNominal(scm, tc)

		then :
		file.name == "123_yes_test_case.feature"

		cleanup:
		clean file

	}

	def "should create a file with the backup name for a given test case"(){

		given:
		def project = Mock(Project) {
			isUseTreeStructureInScmRepo() >> false
		}
		def tc = Mock(TestCase){
			getId() >> 123L
			getName() >> "yes test case"
			getKind() >> TestCaseKind.GHERKIN
			getProject() >> project
		}

		when :
		def file = service.createTestBackup(scm, tc)

		then :
		file.name == "123.feature"

		cleanup:
		clean file

	}

	def "#locateOrMoveOrCreateTestFile - Should create/locate/move the file for a given test case"() {
		given:
			def project = Mock(Project) {
				isUseTreeStructureInScmRepo() >> pUseTreeStructure
			}
			def tc = Mock(TestCase) {
				getId() >> pTestCaseId
				getKind() >> TestCaseKind.GHERKIN
				getName() >> pTestCaseName
				getProject() >> project
			}
			def manifest = new ScmRepositoryManifest(scm)
		and:
			nbrPathCall * pathService.buildTestCaseFoldersPath(pTestCaseId) >> pReturnedFoldersPath
		when:
			def file = service.locateOrMoveOrCreateTestFile(manifest, tc)
		then:
			relativePathFromWorkingDirectory(file) == pExpectedRelativePath
			file.exists()
		where:
			// Rewrite to create our own file where we want as the test below
			pTestCaseId | pUseTreeStructure	| pTestCaseName		| pReturnedFoldersPath 		| pExpectedRelativePath

			123         | false 			| "new test case"	| "any/folder/path"    		| "123_new_test_case.feature"
			123         | true 				| "new test case"	| "màin fôlder/sùb foldèr"  | "main_folder/sub_folder/123_new_test_case.feature"

			456         | false             | "lame pun"        | "any/folder/path"    		| "456_lame_pun.feature"
			456         | false             | "renàmed lame pùn"| "any/folder/path"    		| "456_renamed_lame_pun.feature"

			456         | true              | "lame pun"        | null                 		| "456_lame_pun.feature"
			456			| true            	| "renàmed lame pùn"| null                 		| "456_renamed_lame_pun.feature"
			456			| true             	| "lame pun"        | "màin fôlder/sùb foldèr"	| "main_folder/sub_folder/456_lame_pun.feature"
			456			| true             	| "renàmed lame pùn"| "màin fôlder/sùb foldèr"	| "main_folder/sub_folder/456_renamed_lame_pun.feature"

			nbrPathCall = pUseTreeStructure ? 1 : 0 // pathService is only called (twice) if the tree structure is used
	}


	def "#moveAndRenameFileIfNeeded - Should rename/move the file or not"() {
		given:
			def project = Mock(Project) {
				isUseTreeStructureInScmRepo() >> pUseTreeStructure
			}
			def workingFolder = scm.getWorkingFolder()
			def file = new File(workingFolder, pInitialFilePath)
			createFileAndSubFolders(file)
			def testCase = Mock(TestCase) {
				getId() >> 499L
				getKind() >> TestCaseKind.GHERKIN
				getName() >> pTestCaseName
				getProject() >> project
			}
		and:
			pNbrePathCall * pathService.buildTestCaseFoldersPath(499L) >> pReturnedFoldersPath
		when:
			def resultFile = service.moveAndRenameFileIfNeeded(testCase, file, workingFolder)
		then:
			relativePathFromWorkingDirectory(resultFile) == pExpectedRelativePath
			resultFile.exists()
		cleanup:
			clean(file)
			clean(resultFile)
		where:
			pUseTreeStructure	| pInitialFilePath									| pTestCaseName	| pReturnedFoldersPath		| pExpectedRelativePath

			false 				| "499_connection.feature"							| "cônnèctîon"	| "any/folders/path"		| "499_connection.feature"
			false 				| "499_connection.feature"							| "décônnèctîon"| "any/folders/path"		| "499_deconnection.feature"
			false 				| "main_folder/sub_folder/499_connection.feature"	| "cônnèctîon"	| "any/folders/path"		| "499_connection.feature"
			false 				| "main_folder/sub_folder/499_connection.feature"	| "décônnèctîon"| "any/folders/path"		| "499_deconnection.feature"

			true 				| "main_folder/sub_folder/499_connection.feature"	| "cônnèctîon"	| "main folder/sub folder" 	| "main_folder/sub_folder/499_connection.feature"
			true 				| "main_folder/sub_folder/499_connection.feature"	| "décônnèctîon"| "main folder/sub folder" 	| "main_folder/sub_folder/499_deconnection.feature"
			true 				| "499_connection.feature"							| "cônnèctîon"	| "main folder/sub folder" 	| "main_folder/sub_folder/499_connection.feature"
			true 				| "499_connection.feature"							| "décônnèctîon"| "main folder/sub folder" 	| "main_folder/sub_folder/499_deconnection.feature"

			true 				| "main_folder/sub_folder/499_connection.feature"	| "décônnèctîon"| null	| "499_deconnection.feature"

			pNbrePathCall = pUseTreeStructure ? 1 : 0
	}


	@Ignore
	def "should create the file with backup name if not exists and IOException occured"(){

		// couldn't find how to simulate an IOException

	}


	def "def should print a TestCase to an existing file"(){

		given: "the directory"
		def dir = Files.createTempDirectory("USRFSTest_").toFile()
		def testfile = new File(dir, "test")
		testfile.createNewFile()

		and: "the script content"
		def script =
			"""pull
merge
commit
push
go home quickly before someone notices that the ITs are broken"""

		def metadata =
			"""# Automation priority: 2
# Automation status: SUSPENDED
# Test case importance: MEDIUM
"""

		and: "the test case"
		def tcExtender = new ScriptedTestCaseExtender(script:script)

		def tc = new TestCase(
			kind: TestCaseKind.GHERKIN,
			importance: TestCaseImportance.MEDIUM,
			automationRequest: new AutomationRequest(automationPriority: 2, requestStatus: AutomationRequestStatus.SUSPENDED),
			scriptedTestCaseExtender: tcExtender
		)

		tcExtender.setTestCase(tc)

		when:
		service.printToFile(testfile, tc)

		then:
		testfile.text == metadata + script

		cleanup:
		clean dir

	}



	def "should export two test cases : a new one, and one that existed already"(){

		given: "the scripts"
		def script1 = "drunken Cicero says"
		def script2 = "Lorem *hips*um"

		def metadata1 =
			"""# Automation priority: 1
# Automation status: AUTOMATED
# Test case importance: HIGH
"""

		def metadata2 =
			"""# Automation priority: 3
# Automation status: AUTOMATION_IN_PROGRESS
# Test case importance: LOW
"""

		and: "the test cases"
		def project = Mock(Project) {
			isUseTreeStructureInScmRepo() >> false
		}
		def newTcExtender = new ScriptedTestCaseExtender(script:script1)
		def newTc = Mock(TestCase){
			getId() >> 123L
			getName() >> "yes test case"
			getKind() >> TestCaseKind.GHERKIN
			getImportance() >> TestCaseImportance.HIGH
			isScripted() >> true
			getScriptedTestCaseExtender() >> newTcExtender
			getAutomationRequest() >>
				new AutomationRequest(automationPriority: 1, requestStatus: AutomationRequestStatus.AUTOMATED)
			getProject() >> project
		}
		newTcExtender.setTestCase(newTc)

		def updateTcExtender = new ScriptedTestCaseExtender(script:script2)
		def updateTc = Mock(TestCase){
			getId() >> 456L
			getName() >> "lame pun"
			getImportance() >> TestCaseImportance.LOW
			getKind() >> TestCaseKind.GHERKIN
			isScripted() >> true
			getScriptedTestCaseExtender() >> updateTcExtender
			getAutomationRequest() >>
				new AutomationRequest(automationPriority: 3, requestStatus: AutomationRequestStatus.AUTOMATION_IN_PROGRESS)
			getProject() >> project
		}
		updateTcExtender.setTestCase(updateTc)

		when:
		service.createOrUpdateScriptFile(scm, [updateTc, newTc])

		then:
		File newScript = new File(scm.workingFolder, "123_yes_test_case.feature")
		File updateScript = new File(scm.workingFolder, "456_lame_pun.feature")

		newScript.exists()
		updateScript.exists()

		newScript.text == metadata1 + script1
		updateScript.text == metadata2 + script2

		cleanup:
		clean newScript

	}

	// utils

	/**
	 * Try to create the given file and its absent parent folders.
	 * @param file The abstract file to create
	 */
	private void createFileAndSubFolders(File file) throws IOException {
		File parentFile = file.getParentFile();
		if(!parentFile.mkdirs() && !parentFile.isDirectory()) {
				throw new RuntimeException("directory could not be created at path " + file.toString());
		}
		file.createNewFile();
	}

	// ********** scaffolding **********

	def clean(File file){
		if (file?.exists()){
			FileUtils.forceDelete(file)
		}
	}

}
