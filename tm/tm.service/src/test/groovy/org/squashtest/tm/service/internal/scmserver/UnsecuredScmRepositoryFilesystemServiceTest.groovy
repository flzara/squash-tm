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
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.MessageSource
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.domain.scm.ScmRepository
import org.squashtest.tm.domain.scm.ScmServer
import org.squashtest.tm.domain.testcase.KeywordTestCase
import org.squashtest.tm.domain.testcase.ScriptedTestCase
import org.squashtest.tm.domain.testcase.TestCaseImportance
import org.squashtest.tm.domain.testcase.TestCaseVisitor
import org.squashtest.tm.domain.tf.automationrequest.AutomationRequest
import org.squashtest.tm.domain.tf.automationrequest.AutomationRequestStatus
import org.squashtest.tm.service.internal.library.PathService
import org.squashtest.tm.service.scmserver.ScmRepositoryManifest
import org.squashtest.tm.service.testcase.bdd.KeywordTestCaseService
import org.squashtest.tm.service.testutils.MockFactory
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Files

import static org.squashtest.tm.domain.bdd.BddScriptLanguage.ENGLISH

class UnsecuredScmRepositoryFilesystemServiceTest extends Specification{

	private UnsecuredScmRepositoryFilesystemService service = new UnsecuredScmRepositoryFilesystemService()

	private KeywordTestCaseService keywordTestCaseService = Mock(KeywordTestCaseService)

	private PathService pathService = Mock(PathService);

	private ApplicationEventPublisher eventPublisher = Mock(ApplicationEventPublisher);

	private MessageSource messageSource = Mock(MessageSource);

	@Shared
	private ScmRepository scm = new MockFactory().mockScmRepository(10L, "scmtest_", "squash"){
		dir("squash") {
			file "456_lame_pun.feature"
		}
	}

	def setup() {
		service.pathService = pathService
		service.eventPublisher = eventPublisher
		service.keywordTestCaseService = keywordTestCaseService
		service.messageSource = messageSource

		def server = Mock(ScmServer)
		server.getUrl() >> "http://github.com"
		scm.setScmServer(server)
	}

	def cleanupSpec(){
		FileUtils.forceDelete(scm.baseRepositoryFolder)
	}

	def listContentRelativePathFromWorkingDirectory() {
		return FileUtils
			.listFiles(scm.getWorkingFolder(), null, true)
			.collect { relativePathFromWorkingDirectory(it) }
	}

	def relativePathFromWorkingDirectory(File file) {
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

	def "should create a file with the nominal name for given test case"() {

		given:
		def project = Mock(Project) {
			isUseTreeStructureInScmRepo() >> false
		}
		def tc = new ScriptedTcMock(123L, "yes test case")
		tc.notifyAssociatedWithProject(project)

		when :
		def file = service.createTestNominal(scm, tc)

		then :
		file.name == "123_yes_test_case.feature"

		cleanup:
		clean file
	}

	def "should create a file with the backup name for a given test case"() {

		given:
		def project = Mock(Project) {
			isUseTreeStructureInScmRepo() >> false
		}
		def tc = new ScriptedTcMock(123L, "yes test case")
		tc.notifyAssociatedWithProject(project)

		when :
		def file = service.createTestBackup(scm, tc)

		then :
		file.name == "123.feature"

		cleanup:
		clean file
	}

	@Unroll
	def "locateOrMoveOrCreateTestFile - Should create/locate/move the file for a given test case when Using tree structure"() {
		given:
			def scmServer = Mock(ScmServer) {
				getUrl() >> "http://the_url.org"
			}
			def scmRepo = Mock(ScmRepository) {
				getScmServer() >> scmServer
			}
			def project = Mock(Project) {
				isUseTreeStructureInScmRepo() >> true
				getScmRepository() >> scmRepo
			}
			def tc = new ScriptedTcMock(pTestCaseId, pTestCaseName)
			tc.notifyAssociatedWithProject(project)
			def manifest = new ScmRepositoryManifest(scm)
		and:
			1 * pathService.buildTestCaseFoldersPath(pTestCaseId) >> pReturnedFoldersPath
		when:
			def file = service.locateOrMoveOrCreateTestFile(manifest, tc)
		then:
			relativePathFromWorkingDirectory(file) == pExpectedRelativePath
			file.exists()
		where:
			// Rewrite to create our own file where we want as the test below
			pTestCaseId	| pTestCaseName			| pReturnedFoldersPath 		| pExpectedRelativePath
			456			| "lame pun"        	| null                 		| "456_lame_pun.feature"
			456			| "renàmed lame pùn"	| null                 		| "456_renamed_lame_pun.feature"
			456			| "lame pun"        	| "màin fôlder/sùb foldèr"	| "main_folder/sub_folder/456_lame_pun.feature"
			456			| "renàmed lame pùn"	| "màin fôlder/sùb foldèr"	| "main_folder/sub_folder/456_renamed_lame_pun.feature"
	}

	@Unroll
	def "locateOrMoveOrCreateTestFile - Should create/locate/move the file for a given test case when Not using tree structure"() {
		given:
			def project = Mock(Project) {
				isUseTreeStructureInScmRepo() >> false
			}
			def tc = new ScriptedTcMock(pTestCaseId, pTestCaseName)
			tc.notifyAssociatedWithProject(project)
			def manifest = new ScmRepositoryManifest(scm)
		when:
			def file = service.locateOrMoveOrCreateTestFile(manifest, tc)
		then:
			relativePathFromWorkingDirectory(file) == pExpectedRelativePath
			file.exists()
		where:
			// Rewrite to create our own file where we want as the test below
			pTestCaseId	| pTestCaseName		 	| pExpectedRelativePath

			123			| "new test case"	 	| "123_new_test_case.feature"
			456			| "lame pun"		 	| "456_lame_pun.feature"
			456			| "renàmed lame pùn" 	| "456_renamed_lame_pun.feature"
			5			| "another test case"	| "5_another_test_case.feature"
	}

	/**
	 * This test aims to check the following issue:
	 * 	- a file exists in the repository with a name containing a number other than its id (e.g. 46_test_4.feature)
	 * 	- we try to locate the file of a test case which id is equal to this number (expecting 4_test_case.feature)
	 * 	-> the first test is wrongfully detected and replaced by the second test case's test file
	 */
	@Unroll
	def "locateOrMoveOrCreateTestFile - Should not locate the wrong file when Not using tree structure"() {
		given: "create a file containing ambiguous pattern"
			def workingFolder = scm.getWorkingFolder()
			def file = new File(workingFolder, "55_5_5.feature")
			createFileAndSubFolders(file)
		and:
			def project = Mock(Project) {
				isUseTreeStructureInScmRepo() >> false
			}
		and: "create a test case which id is the problematic number"
			def tc = new ScriptedTcMock(5L, "new-file")
			tc.notifyAssociatedWithProject(project)
			def manifest = new ScmRepositoryManifest(scm)
		when:
			def resultFile = service.locateOrMoveOrCreateTestFile(manifest, tc)
		then:
			resultFile != null
			resultFile.exists()
			relativePathFromWorkingDirectory(resultFile) == "5_new-file.feature"
			listContentRelativePathFromWorkingDirectory().containsAll(["55_5_5.feature", "5_new-file.feature"])
		cleanup:
			clean(file)
			clean(resultFile)
	}

	/**
	 * This test aims to check the following issue:
	 * 	- a file exists in the repository with a folder containing a number other than its id (e.g. the_4.feature_test_folder/78_test.feature)
	 * 	- we try to locate the file of a test case which id is equal to this number (expecting 4_test_case.feature)
	 * 	-> the first test is wrongfully detected and replaced by the second test case's test file
	 */
	@Unroll
	def "locateOrMoveOrCreateTestFile - Should not locate the wrong file when Using tree structure"() {
		given: "create a file ending with a number"
			def workingFolder = scm.getWorkingFolder()
			def file = new File(workingFolder, "5_main_5_folder_5/sub_5_folder.feature_folder/55_5_5.feature")
			createFileAndSubFolders(file)
		and:
			def project = Mock(Project) {
				isUseTreeStructureInScmRepo() >> true
			}
		and: "create a test case which id is the number above"
			def tc = new ScriptedTcMock(5L, "new-file")
			tc.notifyAssociatedWithProject(project)
			def manifest = new ScmRepositoryManifest(scm)
		and:
			1 * pathService.buildTestCaseFoldersPath(_) >> "folder/subfolder"
		when:
			def resultFile = service.locateOrMoveOrCreateTestFile(manifest, tc)
		then:
			resultFile != null
			resultFile.exists()
			relativePathFromWorkingDirectory(resultFile) == "folder/subfolder/5_new-file.feature"
			def repositoryContent = listContentRelativePathFromWorkingDirectory()
			repositoryContent.containsAll([
				"5_main_5_folder_5/sub_5_folder.feature_folder/55_5_5.feature",
				"folder/subfolder/5_new-file.feature"])
		cleanup:
			clean(file)
			clean(resultFile)
	}

	/**
	 * Same as the test above, but with both files already created.
	 */
	@Unroll
	def "locateOrMoveOrCreateTestFile - Should not locate the wrong file when Using tree structure and both filed are already written"() {
		given: "create a file ending with a number"
			def workingFolder = scm.getWorkingFolder()
			def file = new File(workingFolder, "5_main_5_folder_5/sub_5_folder.feature_folder/55_5_5.feature")
			createFileAndSubFolders(file)
		and: "create the other file"
			def  secondFile = new File(workingFolder, "folder/subfolder/5_new-file.feature")
			createFileAndSubFolders(secondFile)
		and:
			def project = Mock(Project) {
				isUseTreeStructureInScmRepo() >> true
			}
		and: "create the test case of the 5_new-file.feature"
			def tc = new ScriptedTcMock(5L, "new-file")
			tc.notifyAssociatedWithProject(project)
		and:
			1 * pathService.buildTestCaseFoldersPath(_) >> "folder/subfolder"
		when:
			def manifest = new ScmRepositoryManifest(scm)
			def resultFile = service.locateOrMoveOrCreateTestFile(manifest, tc)
		then:
			resultFile != null
			resultFile.exists()
			relativePathFromWorkingDirectory(resultFile) == "folder/subfolder/5_new-file.feature"
			def repositoryContent = listContentRelativePathFromWorkingDirectory()
			repositoryContent.containsAll([
				"5_main_5_folder_5/sub_5_folder.feature_folder/55_5_5.feature",
				"folder/subfolder/5_new-file.feature"])
		cleanup:
			clean(file)
			clean(resultFile)
	}

	@Unroll
	def "moveAndRenameFileIfNeeded - Should rename/move the file or not when Using tree structure"() {
		given:
			def project = Mock(Project) {
				isUseTreeStructureInScmRepo() >> true
			}
		and: "test case"
			def testCase = new ScriptedTcMock(499L, pTestCaseName)
			testCase.notifyAssociatedWithProject(project)
		and: "create a file"
			def workingFolder = scm.getWorkingFolder()
			def file = new File(workingFolder, pInitialFilePath)
			createFileAndSubFolders(file)
		and:
			1 * pathService.buildTestCaseFoldersPath(499L) >> pReturnedFoldersPath
		when:
			def resultFile = service.moveAndRenameFileIfNeeded(testCase, file, scm)
		then:
			relativePathFromWorkingDirectory(resultFile) == pExpectedRelativePath
			resultFile.exists()
		cleanup:
			clean(file)
			clean(resultFile)
		where:
			pInitialFilePath									| pTestCaseName		| pReturnedFoldersPath		| pExpectedRelativePath

			"main_folder/sub_folder/499_connection.feature"	| "cônnèctîon"		| "main folder/sub folder" 	| "main_folder/sub_folder/499_connection.feature"
			"main_folder/sub_folder/499_connection.feature"	| "décônnèctîon"	| "main folder/sub folder" 	| "main_folder/sub_folder/499_deconnection.feature"
			"499_connection.feature"							| "cônnèctîon"		| "main folder/sub folder" 	| "main_folder/sub_folder/499_connection.feature"
			"499_connection.feature"							| "décônnèctîon"	| "main folder/sub folder" 	| "main_folder/sub_folder/499_deconnection.feature"

			"main_folder/sub_folder/499_connection.feature"	| "décônnèctîon"	| null						| "499_deconnection.feature"
	}

	@Unroll
	def "moveAndRenameFileIfNeeded - Should rename/move the file or not when Using tree structure when Not using tree structure"() {
		given:
			def project = Mock(Project) {
			isUseTreeStructureInScmRepo() >> false
		}
		and: "test case"
			def testCase = new ScriptedTcMock(499L, pTestCaseName)
			testCase.notifyAssociatedWithProject(project)
		and: "create a file"
			def workingFolder = scm.getWorkingFolder()
			def file = new File(workingFolder, pInitialFilePath)
			createFileAndSubFolders(file)
		when:
			def resultFile = service.moveAndRenameFileIfNeeded(testCase, file, scm)
		then:
			relativePathFromWorkingDirectory(resultFile) == pExpectedRelativePath
			resultFile.exists()
			cleanup:
			clean(file)
			clean(resultFile)
		where:
			pInitialFilePath									| pTestCaseName		| pReturnedFoldersPath		| pExpectedRelativePath

			"499_connection.feature"							| "cônnèctîon"		| "-any/folders/path-"		| "499_connection.feature"
			"499_connection.feature"							| "décônnèctîon	"	| "-any/folders/path-"		| "499_deconnection.feature"
			"main_folder/sub_folder/499_connection.feature"	| "cônnèctîon"		| "-any/folders/path-"		| "499_connection.feature"
			"main_folder/sub_folder/499_connection.feature"	| "décônnèctîon"	| "-any/folders/path-"		| "499_deconnection.feature"
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
# Test case importance: MEDIUM
"""

		and: "the test case"

		def tc = new ScriptedTestCase(
			importance: TestCaseImportance.MEDIUM,
			automationRequest: new AutomationRequest(automationPriority: 2, requestStatus: AutomationRequestStatus.SUSPENDED),
			script:script
		)


		when:
		service.printToFile(testfile, tc)

		then:
		testfile.text == metadata + script

		cleanup:
		clean dir

	}

	def "should export two Gherkin test cases : a new one, and one that existed already; and a Keyword test case"(){

		given: "the scripts"
		def script1 = "drunken Cicero says"
		def script2 = "Lorem *hips*um"
		def script3 = "I am a keyword test case script"

		def metadata1 =
			"""# Automation priority: 1
# Test case importance: HIGH
"""

		def metadata2 =
			"""# Automation priority: 3
# Test case importance: LOW
"""

		def metadata3 =
			"""# Automation priority: 1
# Test case importance: Low
"""

		and: "the Gherkin test cases"
		def scmServer = Mock(ScmServer) {
			getUrl() >> "http://theUrl"
		}
		def scmRepo = Mock(ScmRepository) {
			getScmServer() >> scmServer
		}
		def project = Mock(Project) {
			isUseTreeStructureInScmRepo() >> false
			getScmRepository() >> scmRepo
			getBddScriptLanguage() >> ENGLISH
		}

		ScriptedTestCase newTc = Mock(ScriptedTestCase){
			getId() >> 123L
			getName() >> "yes test case"
			getImportance() >> TestCaseImportance.HIGH
			getAutomationRequest() >>
				new AutomationRequest(automationPriority: 1, requestStatus: AutomationRequestStatus.AUTOMATED)
			getProject() >> project
			getScript() >> script1
			accept(_) >> { TestCaseVisitor visitor -> visitor.visit(it) }
			computeScriptWithAppendedMetadata() >> metadata1 + script1
			buildFilenameMatchPattern() >> String.format("%d(_.*)?\\.%s", it.getId(), ScriptedTestCase.FEATURE_EXTENSION)
			createFilename() >> "123_yes_test_case.feature"
		}

		ScriptedTestCase updateTc = Mock(ScriptedTestCase){
			getId() >> 456L
			getName() >> "lame pun"
			getImportance() >> TestCaseImportance.LOW
			getAutomationRequest() >>
				new AutomationRequest(automationPriority: 3, requestStatus: AutomationRequestStatus.AUTOMATION_IN_PROGRESS)
			getProject() >> project
			getScript() >> script2
			accept(_) >> { TestCaseVisitor visitor -> visitor.visit(it) }
			computeScriptWithAppendedMetadata() >> metadata2 + script2
			buildFilenameMatchPattern() >> String.format("%d(_.*)?\\.%s", it.getId(), ScriptedTestCase.FEATURE_EXTENSION)
			createFilename() >> "456_lame_pun.feature"
		}

		and: "the Keyword test case"
		KeywordTestCase keywordTc = Mock(KeywordTestCase){
			getId() >> 777L
			getName() >> "keyword test case"
			getImportance() >> TestCaseImportance.LOW
			getAutomationRequest() >>
				new AutomationRequest(automationPriority: 1, requestStatus: AutomationRequestStatus.AUTOMATED)
			getProject() >> project
			accept(_) >> { TestCaseVisitor visitor -> visitor.visit(it) }
		}
		def locale = project.getBddScriptLanguage().getLocale()
		messageSource.getMessage("testcase.bdd.script.label.test-case-importance", null, locale) >> "# Test case importance: "
		messageSource.getMessage("testcase.bdd.script.label.automation-priority", null, locale) >> "# Automation priority: "
		messageSource.getMessage("test-case.importance.LOW", null, locale) >> "Low"
		messageSource.getMessage("automation-request.request_status.AUTOMATED", null, locale) >> "Automated"
		keywordTestCaseService.buildFilenameMatchPattern(keywordTc) >> "777(_.*)?\\.feature"
		keywordTestCaseService.createFileName(keywordTc) >> "777_keyword_test_case.feature"
		keywordTestCaseService.writeScriptFromTestCase(keywordTc, false) >> script3

		when:
		service.createOrUpdateScriptFile(scm, [updateTc, newTc, keywordTc])

		then:
		File newScript = new File(scm.workingFolder, "123_yes_test_case.feature")
		File updateScript = new File(scm.workingFolder, "456_lame_pun.feature")
		File keywordTcScript = new File(scm.workingFolder, "777_keyword_test_case.feature")

		newScript.exists()
		updateScript.exists()
		keywordTcScript.exists()

		newScript.text == metadata1 + script1
		updateScript.text == metadata2 + script2
		keywordTcScript.text == metadata3 + script3

		cleanup:
		clean newScript
		clean keywordTcScript

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

	class ScriptedTcMock extends ScriptedTestCase {

		private Long overId

		ScriptedTcMock(Long overId, String name) {
			this.overId = overId
			this.name = name
		}

		Long getId() {
			return overId
		}

		void setId(Long overId) {
			this.overId = overId
		}
	}

	// ********** scaffolding **********

	def clean(File file){
		if (file?.exists()){
			FileUtils.forceDelete(file)
		}
	}

}
