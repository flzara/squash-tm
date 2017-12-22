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
package org.squashtest.tm.service.internal.library

import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.domain.testcase.TestCaseFolder
import org.squashtest.tm.service.internal.customfield.PrivateCustomFieldValueService
import org.squashtest.tm.service.internal.repository.RequirementVersionCoverageDao
import org.squashtest.tm.service.internal.repository.TestCaseDao
import org.squashtest.tm.service.internal.repository.TestCaseFolderDao

import org.squashtest.tm.service.security.PermissionEvaluationService
import org.squashtest.tm.service.testutils.MockFactory
import spock.lang.Specification

public class TreeNodeCopierTest extends Specification{


	private TreeNodeCopier copier = new TreeNodeCopier()
	private TestCaseDao testCaseDao = Mock()
	private TestCaseFolderDao testCaseFolderDao = Mock()
	private PrivateCustomFieldValueService customFieldValueManagerService = Mock()
	private PermissionEvaluationService permissionService = Mock()
	private RequirementVersionCoverageDao requirementVersionCoverageDao = Mock()


	MockFactory mockFactory = new MockFactory()


	def setup(){
		copier.testCaseDao = testCaseDao;
		copier.testCaseFolderDao = testCaseFolderDao
		copier.customFieldValueManagerService = customFieldValueManagerService
		permissionService.hasRoleOrPermissionOnObject(_, _, _) >> true
		copier.permissionService = permissionService
		copier.requirementVersionCoverageDao = requirementVersionCoverageDao
	}

	def "should copy a node without renaming it because the name was available"(){
		given :
		TestCase tcOrig =  new TestCase(name:"test case okay");
		tcOrig.notifyAssociatedWithProject(mockFactory.mockProject())

		and : "the folder"
		TestCaseFolder folder = Mock()
		folder.isContentNameAvailable(_) >> true


		when :
		def result = copier.performOperation(tcOrig, folder, null)


		then :
		result.collect{it.name}==["test case okay"];
	}



	def "should copy a node and rename it as the 4th copy"(){

		given : "the test case and it's copy"

		TestCase tcOrig = new TestCase(name: "NX_OHNOZ")
		tcOrig.notifyAssociatedWithProject(mockFactory.mockProject())
		and : "the folder"
		TestCaseFolder folder = Mock()

		folder.isContentNameAvailable(_) >> false

		folder.getContentNames()>>  ["NX_OHNOZ", "NX_OHNOZ-Copie1", "NX_OHNOZ-Copie3"]


		when :
		def result = copier.performOperation(tcOrig, folder, null)


		then :
		result.collect{it.name}==["NX_OHNOZ-Copie4"];


	}


	def "should copy a node and rename it as the 1000th copy"(){
		given : "the test case and it's copy"
		TestCase tcOrig = new TestCase(name: "NX_OHNOZ")
		tcOrig.notifyAssociatedWithProject(mockFactory.mockProject())

		and : "the folder"
		TestCaseFolder folder = Mock();
		folder.isContentNameAvailable(_) >> false;
		folder.getContentNames() >> ["NX_OHNOZ-Copie999"]


		when :
		def result = copier.performOperation(tcOrig, folder, null)


		then :
		result.collect{it.name}==["NX_OHNOZ-Copie1000"]

	}


	def "should copy a node and rename it as the 2th copy"(){
		given : "the test case and it's copy"
		TestCase tcOrig = new TestCase(name: "NX_OHNOZ")
		tcOrig.notifyAssociatedWithProject(mockFactory.mockProject())
		and : "the folder"
		TestCaseFolder folder = Mock()
		folder.isContentNameAvailable(_) >> false
		folder.getContentNames() >>  ["NX_OHNOZ", "NX_OHNOZ-Copie1", "NX_OHNOZ-Copie1-Copie7"]


		when :
		def result = copier.performOperation(tcOrig, folder, null)


		then :
		result.collect{it.name}==["NX_OHNOZ-Copie2"]

	}

	def "should copy a node and not rename despite copies are present since the original name is available anyway"(){
		given : "a project"
		def project = mockFactory.mockProject()

		and : "the test case and it's copy"
		TestCase tcOrig = new TestCase(name: "NX_OHNOZ")
		tcOrig.notifyAssociatedWithProject(project)

		and : "the folder"
		TestCaseFolder folder = new TestCaseFolder()
		folder.notifyAssociatedWithProject(project)
		folder.addContent(new TestCase(name:"NX_OHNOZ-Copie1"))
		folder.addContent(new TestCase(name:"NX_OHNOZ-Copie1-Copie7"))

		when :
		def result = copier.performOperation(tcOrig, folder, null)


		then :
		result.collect{it.name}==["NX_OHNOZ"]

	}
}
