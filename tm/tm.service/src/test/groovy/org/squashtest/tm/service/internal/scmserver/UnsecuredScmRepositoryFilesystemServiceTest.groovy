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
import org.squashtest.tm.domain.scm.ScmRepository
import org.squashtest.tm.domain.testcase.ScriptedTestCaseExtender
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.domain.testcase.TestCaseKind
import org.squashtest.tm.service.scmserver.ScmRepositoryManifest
import org.squashtest.tm.service.testutils.MockFactory
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Files

class UnsecuredScmRepositoryFilesystemServiceTest extends Specification{

	@Shared
	private ScmRepository scm = new MockFactory().mockScmRepository(10L, "scmtest_", "squash"){
		dir("squash"){
			file "456_lame_pun.feature"
		}
	}

	def cleanupSpec(){
		FileUtils.forceDelete(scm.baseRepositoryFolder)
	}

	private UnsecuredScmRepositoryFilesystemService service = new UnsecuredScmRepositoryFilesystemService()



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
		def tc = Mock(TestCase){
			getId() >> 123L
			getName() >> "yes test case"
			getKind() >> TestCaseKind.GHERKIN
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
		def tc = Mock(TestCase){
			getId() >> 123L
			getName() >> "yes test case"
			getKind() >> TestCaseKind.GHERKIN
		}

		when :
		def file = service.createTestBackup(scm, tc)

		then :
		file.name == "123.feature"

		cleanup:
		clean file

	}

	def "should locate the file for a given test case"(){

		given :
		def tc = Mock(TestCase){
			getId() >> 456L
			getKind() >> TestCaseKind.GHERKIN
			getName() >> "lame_pun"
		}

		and:
		def manifest = new ScmRepositoryManifest(scm)

		when :
		def file = service.locateOrRenameOrCreateTestFile(manifest, tc)

		then :
		file.name == "456_lame_pun.feature"

	}

	def "#locateOrRenameOrCreateTestFile - should locate the file and rename it"() {
		given:
			def tc = Mock(TestCase) {
				getId() >> 456L
				getKind() >> TestCaseKind.GHERKIN
				getName() >> "new_lame_pun"
			}
		and:
			def manifest = new ScmRepositoryManifest(scm)
		when:
			def file = service.locateOrRenameOrCreateTestFile(manifest, tc)
		then:
			file.name == "456_new_lame_pun.feature"
	}

	def "#renameFileIfNeeded - should not need to rename the file"() {
		given:
			def file = new File("499_press_button.feature")
			file.createNewFile()
			def testCase = Mock(TestCase) {
				getId() >> 499L
				getKind() >> TestCaseKind.GHERKIN
				getName() >> "press_button"
			}
		when:
			def resultFile = service.renameFileIfNeeded(testCase, file)
		then:
			resultFile == file
		cleanup:
			clean(file)
	}

	def "#renameFileIfNeeded - should rename the file"() {
		given:
			File file = new File(scm.getWorkingFolder(), "499_press_button.feature")
			file.createNewFile()
			def testCase = Mock(TestCase) {
				getId() >> 499L
				getKind() >> TestCaseKind.GHERKIN
				getName() >> "click_button"
			}
		when:
			def resultFile = service.renameFileIfNeeded(testCase, file)
		then:
			resultFile.getName().equals("499_click_button.feature")
		cleanup:
			clean(file)
			clean(resultFile)
	}


	def "should create the file with nominal name if not exists"(){
		given :
		def tc = Mock(TestCase){
			getId() >> 123L
			getName() >> "yes test case"
			getKind() >> TestCaseKind.GHERKIN
		}

		and:
		def manifest = new ScmRepositoryManifest(scm)

		when :
		def file = service.locateOrRenameOrCreateTestFile(manifest, tc)

		then :
		file.name == "123_yes_test_case.feature"

		cleanup:
		clean file

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

		and: "the test case"
		def tc = new TestCase(
			kind: TestCaseKind.GHERKIN,
			scriptedTestCaseExtender: new ScriptedTestCaseExtender(script:script)
		)

		when:
		service.printToFile(testfile, tc)

		then:
		testfile.text == script

		cleanup:
		clean dir

	}



	def "should export two test cases : a new one, and one that existed already"(){

		given: "the scripts"
		def script1 = "drunken Cicero says"
		def script2 = "Lorem *hips*um"


		and: "the test cases"
		def newTc = Mock(TestCase){
			getId() >> 123L
			getName() >> "yes test case"
			getKind() >> TestCaseKind.GHERKIN
			isScripted() >> true
			getScriptedTestCaseExtender() >> new ScriptedTestCaseExtender(script:script1)
		}

		def updateTc = Mock(TestCase){
			getId() >> 456L
			getName() >> "lame pun"
			getKind() >> TestCaseKind.GHERKIN
			isScripted() >> true
			getScriptedTestCaseExtender() >> new ScriptedTestCaseExtender(script:script2)
		}

		when:
		service.createOrUpdateScriptFile(scm, [updateTc, newTc])

		then:
		File newScript = new File(scm.workingFolder, "123_yes_test_case.feature")
		File updateScript = new File(scm.workingFolder, "456_lame_pun.feature")

		newScript.exists()
		updateScript.exists()

		newScript.text == script1
		updateScript.text == script2

		cleanup:
		clean newScript

	}


	// ********** scaffolding **********

	def clean(File file){
		if (file?.exists()){
			FileUtils.forceDelete(file)
		}
	}

}
