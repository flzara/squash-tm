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
package org.squashtest.tm.service.internal.testcase

import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
import org.springframework.context.MessageSource
import org.squashtest.tm.domain.bdd.ActionWord
import org.squashtest.tm.domain.bdd.ActionWordParameter
import org.squashtest.tm.domain.bdd.ActionWordParameterValue
import org.squashtest.tm.domain.bdd.ActionWordText
import org.squashtest.tm.domain.customfield.BindableEntity
import org.squashtest.tm.domain.customfield.CustomField
import org.squashtest.tm.domain.customfield.CustomFieldBinding
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.domain.testcase.KeywordTestCase
import org.squashtest.tm.domain.testcase.KeywordTestStep
import org.squashtest.tm.domain.testcase.ScriptedTestCase
import org.squashtest.tm.domain.testcase.TestCaseFolder
import org.squashtest.tm.domain.testcase.TestCaseLibrary
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode
import org.squashtest.tm.service.customfield.CustomFieldBindingFinderService
import org.squashtest.tm.service.internal.customfield.PrivateCustomFieldValueService
import org.squashtest.tm.service.internal.library.AbstractLibraryNavigationService
import org.squashtest.tm.service.internal.repository.KeywordTestCaseDao
import org.squashtest.tm.service.internal.repository.ProjectDao
import org.squashtest.tm.service.internal.repository.ScriptedTestCaseDao
import org.squashtest.tm.service.internal.repository.TestCaseDao
import org.squashtest.tm.service.internal.repository.TestCaseFolderDao
import org.squashtest.tm.service.internal.repository.TestCaseLibraryDao
import org.squashtest.tm.service.internal.repository.TestCaseLibraryNodeDao
import org.squashtest.tm.service.security.PermissionEvaluationService
import org.squashtest.tm.tools.unittest.reflection.ReflectionCategory
import spock.lang.Specification

import static org.squashtest.tm.domain.bdd.BddImplementationTechnology.ROBOT
import static org.squashtest.tm.domain.bdd.BddImplementationTechnology.CUCUMBER
import static org.squashtest.tm.domain.bdd.BddScriptLanguage.ENGLISH
import static org.squashtest.tm.domain.bdd.Keyword.GIVEN
import static org.squashtest.tm.domain.bdd.Keyword.AND

class TestCaseLibraryNavigationServiceImplTest extends Specification {

	AbstractLibraryNavigationService<TestCaseLibrary, TestCaseFolder, TestCaseLibraryNode> service = new TestCaseLibraryNavigationServiceImpl()
	TestCaseLibraryDao testCaseLibraryDao = Mock()
	TestCaseFolderDao testCaseFolderDao = Mock()
	TestCaseDao testCaseDao = Mock()
	ProjectDao projectDao = Mock()
	TestCaseLibraryNodeDao nodeDao = Mock()
	PermissionEvaluationService permissionService = Mock()
	ScriptedTestCaseDao scriptedTestCaseDao = Mock()
	KeywordTestCaseDao keywordTestCaseDao = Mock()
	PrivateCustomFieldValueService customValueService = Mock()
	CustomFieldBindingFinderService customFieldBindingFinderService  = Mock()

	def setup() {
		service.testCaseLibraryDao = testCaseLibraryDao
		service.testCaseFolderDao = testCaseFolderDao
		service.testCaseDao = testCaseDao
		service.projectDao = projectDao
		service.testCaseLibraryNodeDao = nodeDao
		service.scriptedTestCaseDao = scriptedTestCaseDao
		service.keywordTestCaseDao = keywordTestCaseDao
		service.customFieldBindingFinderService = customFieldBindingFinderService
		service.customValueService = customValueService

		use (ReflectionCategory) {
			AbstractLibraryNavigationService.set(field: "permissionService", of: service, to: permissionService)
			AbstractLibraryNavigationService.set(field: "customFieldValuesService", of: service, to: customValueService)

		}
		permissionService.hasRoleOrPermissionOnObject(_, _, _) >> true
	}

	def "should find root content of library"() {
		given:
		def rootContent = [
			Mock(TestCaseLibraryNode),
			Mock(TestCaseLibraryNode)
		]
		testCaseLibraryDao.findAllRootContentById(10) >> rootContent


		when:
		def found = service.findLibraryRootContent(10)

		then:
		found == rootContent
	}

	def "should find content of folder"() {
		given:
		def content = [
			Mock(TestCaseLibraryNode),
			Mock(TestCaseLibraryNode)
		]
		testCaseFolderDao.findAllContentById(10) >> content


		when:
		def found = service.findFolderContent(10)

		then:
		found == content
	}

	def "should find library"() {
		given:
		TestCaseLibrary l = Mock()
		testCaseLibraryDao.findById(10) >> l


		when:
		def found = service.findLibrary(10)

		then:
		found == l
	}

	def "should find folder"() {
		given:
		TestCaseFolder f = Mock()
		testCaseFolderDao.findById(10) >> f

		when:
		def found = service.findFolder(10)

		then:
		found == f
	}

	def "should add folder to folder"() {
		given:
		TestCaseFolder newFolder = Mock()
		newFolder.getContent() >> []
		and:
		TestCaseFolder container = Mock()
		testCaseFolderDao.findById(10) >> container
		and :
		Project project = Mock()
		newFolder.getProject() >> project
		project.getId() >>  10L
		CustomField cuf = Mock()
		cuf.getId() >> 4L

		BindableEntity entity1 = Mock()
		BindableEntity entity2 = Mock()

		CustomFieldBinding binding1 = Mock()
		CustomFieldBinding binding2 = Mock()

		binding1.getBoundEntity() >> entity1
		binding1.getCustomField() >> cuf

		binding2.getBoundEntity() >> entity2
		binding2.getCustomField() >> cuf

		List<CustomFieldBinding> bindings = [binding1, binding2]
		customFieldBindingFinderService.findCustomFieldsForProjectAndEntity(10L, BindableEntity.TESTCASE_FOLDER) >> bindings

		when:
		service.addFolderToFolder(10, newFolder)

		then:
		container.addContent newFolder
		1 * testCaseFolderDao.persist(newFolder)
	}


	def "should create a hierarchy of folders"(){

		given:
		def splitname = ["project", "folder1", "folder2", "folder \\/ 3", "folder4"] as String[]
		def idx = 2

		when :
		def res = service.mkTransFolders(idx, splitname)

		def names = []
		TestCaseFolder iter = res

		// groovy doesn't support do..while yet
		while (iter != null){
			names << iter.name
			iter = (iter.hasContent()) ? iter.content[0] : null
		}


		then :
		names == ["folder2", "folder / 3", "folder4"]


	}

	def "should export some gherkin test cases"(){
		given:
		def testCase1 = Mock(ScriptedTestCase)
		testCase1.getId() >> 1L
		testCase1.getScript() >> "Feature: one"

		def testCase2 = Mock(ScriptedTestCase)
		testCase2.getId() >> 2L
		testCase2.getScript() >> "Feature: two\nScenario: one"

		and:
		scriptedTestCaseDao.findAllById(_) >> [testCase1, testCase2]

		when:
		File export = service.doGherkinExport([]);
		String name = export.getName()

		then:
		export != null
		name.matches("export-feature-.*\\.zip")
		def zipArchiveInputStream = new ZipArchiveInputStream(new FileInputStream(export), "UTF-8", true)
		zipArchiveInputStream.nextEntry.name == "tc_1.feature"
		def lines = zipArchiveInputStream.readLines()
		lines.size() == 1
		lines.get(0).equals("Feature: one")

		def zipArchiveInputStream1 = new ZipArchiveInputStream(new FileInputStream(export), "UTF-8", true)
		zipArchiveInputStream1.nextEntry.name == "tc_1.feature"
		zipArchiveInputStream1.nextEntry.name == "tc_2.feature"
		def lines2 = zipArchiveInputStream1.readLines()
		lines2.size() == 2
		lines2 == ["Feature: two","Scenario: one"]
	}

	def "should export some keyword test cases"(){
		given:
		def project1 = Mock(Project)
		project1.bddImplementationTechnology >> ROBOT
		project1.bddScriptLanguage >> ENGLISH

		def testCase1 = Mock(KeywordTestCase)
		testCase1.id >> 1L
		testCase1.name >> "one"
		testCase1.project >> project1
		testCase1.datasets >> []

		def project2 = Mock(Project)
		project2.bddImplementationTechnology >> CUCUMBER
		project2.bddScriptLanguage >> ENGLISH

		def testCase2 = Mock(KeywordTestCase)
		testCase2.id >> 2L
		testCase2.name >> "two"
		testCase2.project >> project2
		testCase2.datasets >> []

		def fragment1 = new ActionWordText("I have ")
		def fragment2 = new ActionWordParameter()
		fragment2.setId(-2L)
		fragment2.setName("param1")
		def fragment3 = new ActionWordText(" apples")
		def actionWord = Mock(ActionWord) {
			getId() >> -77L
			getToken() >> "TPT-I have - apples-"
			getFragments() >> [fragment1, fragment2, fragment3]
		}
		def param1 = new ActionWordParameterValue("5")
		param1.setActionWordParam(fragment2)
		def step1 = Mock(KeywordTestStep) {
			getKeyword() >> GIVEN
			getActionWord() >> actionWord
			getParamValues() >> [param1]
			getTestCase() >> testCase1
		}
		testCase1.steps >> [step1]

		def param2 = new ActionWordParameterValue("3")
		param2.setActionWordParam(fragment2)
		def step2 = new KeywordTestStep()
		step2.setKeyword(AND)
		step2.setActionWord(actionWord)
		step2.setParamValues([param2])
		step2.setTestCase(testCase2)

		testCase2.steps >> [step2]


		and:
		keywordTestCaseDao.findAllById(_) >> [testCase1, testCase2]
		def messageSource = Mock(MessageSource)
		3 * messageSource.getMessage(*_) >>> ["And", "Scenario: ", "Feature: "]

		when:
		File export = service.doKeywordExport([], messageSource);
		String name = export.getName()

		then:
		export != null
		name.matches("export-keyword-.*\\.zip")
		def zipArchiveInputStream = new ZipArchiveInputStream(new FileInputStream(export), "UTF-8", true)
		zipArchiveInputStream.nextEntry.name == "tc_1.robot"
		def lines1 = zipArchiveInputStream.readLines()
		lines1.size() == 6
		lines1 == ["*** Settings ***", "Resource	squash_resources.resource", "", "*** Test Cases ***", "one", "\tGiven I have \"5\" apples"]

		def zipArchiveInputStream1 = new ZipArchiveInputStream(new FileInputStream(export), "UTF-8", true)
		zipArchiveInputStream1.nextEntry.name == "tc_1.robot"
		zipArchiveInputStream1.nextEntry.name == "tc_2.feature"
		def lines2 = zipArchiveInputStream1.readLines()
		lines2.size() == 5
		lines2 == ["# language: en", "Feature: two", "", "\tScenario: two", "\t\tAnd I have 3 apples"]
	}

}
