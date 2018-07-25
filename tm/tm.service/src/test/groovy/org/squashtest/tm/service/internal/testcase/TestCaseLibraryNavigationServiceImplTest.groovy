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
import org.squashtest.tm.domain.customfield.BindableEntity
import org.squashtest.tm.domain.customfield.CustomField
import org.squashtest.tm.domain.customfield.CustomFieldBinding
import org.squashtest.tm.domain.testcase.ScriptedTestCaseExtender
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.service.customfield.CustomFieldBindingFinderService
import org.squashtest.tm.service.internal.customfield.PrivateCustomFieldValueService
import org.squashtest.tm.service.internal.repository.ScriptedTestCaseExtenderDao;
import org.squashtest.tm.tools.unittest.reflection.ReflectionCategory;
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.domain.testcase.TestCaseFolder
import org.squashtest.tm.domain.testcase.TestCaseLibrary
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode
import org.squashtest.tm.service.internal.library.AbstractLibraryNavigationService;
import org.squashtest.tm.service.internal.repository.ProjectDao;
import org.squashtest.tm.service.internal.repository.TestCaseDao
import org.squashtest.tm.service.internal.repository.TestCaseFolderDao
import org.squashtest.tm.service.internal.repository.TestCaseLibraryDao
import org.squashtest.tm.service.internal.repository.TestCaseLibraryNodeDao;
import org.squashtest.tm.service.security.PermissionEvaluationService
import spock.lang.Specification

import static org.squashtest.tm.domain.testcase.ScriptedTestCaseLanguage.GHERKIN


class TestCaseLibraryNavigationServiceImplTest extends Specification {

	AbstractLibraryNavigationService<TestCaseLibrary, TestCaseFolder, TestCaseLibraryNode> service = new TestCaseLibraryNavigationServiceImpl()
	TestCaseLibraryDao testCaseLibraryDao = Mock()
	TestCaseFolderDao testCaseFolderDao = Mock()
	TestCaseDao testCaseDao = Mock()
	ProjectDao projectDao = Mock()
	TestCaseLibraryNodeDao nodeDao = Mock()
	PermissionEvaluationService permissionService = Mock()
	ScriptedTestCaseExtenderDao scriptedTestCaseExtenderDao = Mock()
	PrivateCustomFieldValueService customValueService = Mock()
	CustomFieldBindingFinderService customFieldBindingFinderService  = Mock()

	def setup() {
		service.testCaseLibraryDao = testCaseLibraryDao
		service.testCaseFolderDao = testCaseFolderDao
		service.testCaseDao = testCaseDao
		service.projectDao = projectDao
		service.testCaseLibraryNodeDao = nodeDao
		service.scriptedTestCaseExtenderDao = scriptedTestCaseExtenderDao
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
//
//	def "should persist a hierarchy of folders at the root of a library"() {
//
//		given:
//		def path = "/project/folder1/folder2/folder \\/ 3/folder4"
//
//		and:
//		Project p = Mock()
//		TestCaseLibrary tcl = Mock()
//		p.getTestCaseLibrary() >> tcl
//		tcl.getId() >> 5l
//
//		and:
//		Project project = Mock()
//
//
//		CustomField cuf = Mock()
//		cuf.getId() >> 5L
//
//		BindableEntity entity1 = Mock()
//		BindableEntity entity2 = Mock()
//
//		CustomFieldBinding binding1 = Mock()
//		CustomFieldBinding binding2 = Mock()
//
//		binding1.getBoundEntity() >> entity1
//		binding1.getCustomField() >> cuf
//
//		binding2.getBoundEntity() >> entity2
//		binding2.getCustomField() >> cuf
//
//		List<CustomFieldBinding> bindings = [binding1, binding2]
//
//		TestCaseFolder tcln1 = new TestCaseFolder()
//		TestCaseFolder tcln2 = new TestCaseFolder()
//		TestCaseFolder tcln3 = new TestCaseFolder()
//		TestCaseFolder newFolder = new TestCaseFolder()
//		newFolder.getName() >> "project"
//		tcln1.getName() >> "folder2"
//		tcln2.getName() >> "folder / 3"
//		tcln3.getName() >> "folder4"
//		tcln1.getContent() >> [tcln2]
//		tcln2.getContent() >> [tcln3]
//		String[] split = ["project", "folder1", "folder2", "folder / 3", "folder4"]
//
//		and:
//		nodeDao.findNodeIdsByPath(_) >> [null, null, null, null]
//		service.getFolderDao().persist(newFolder) >> { newFolder.getId() } >> 5L
//		and:
//		projectDao.findByName("project") >> p
//		testCaseLibraryDao.findById(5l) >> tcl
//
//
//		customFieldBindingFinderService.findCustomFieldsForProjectAndEntity(5L, BindableEntity.TESTCASE_FOLDER ) >> bindings
//
//		when :
//		service.mkdirs(path)
//
//		then :
//
//		1 * testCaseFolderDao.persist ( {
//			it.id == 5L &&
//			it.name == "folder1" &&
//				it.content[0].name == "folder2" &&
//				it.content[0].content[0].name == "folder / 3" &&
//				it.content[0].content[0].content[0].name == "folder4"
//		})
//
//		1 * tcl.addContent( {
//			it.id == 5L &&
//			it.name == "folder1" &&
//				it.content[0].name == "folder2" &&
//				it.content[0].content[0].name == "folder / 3" &&
//				it.content[0].content[0].content[0].name == "folder4"
//		} )
//	}
//

	def "should export some gherkin test cases"(){
		given:
		def testCase1 = Mock(TestCase)
		testCase1.getId() >> 1L
		def extender1 = new ScriptedTestCaseExtender(testCase1, GHERKIN)
		extender1.script = "Feature: one"
		extender1.id = 1L
		extender1.testCaseId =1L

		def testCase2 = Mock(TestCase)
		testCase2.getId() >> 2L
		def extender2 = new ScriptedTestCaseExtender(testCase2, GHERKIN)
		extender2.script = "Feature: two\nScenario: one"
		extender2.id = 2L
		extender2.testCaseId = 2L

		and:
		scriptedTestCaseExtenderDao.findByLanguageAndTestCase_IdIn(GHERKIN,_) >> [extender1,extender2]

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

}
