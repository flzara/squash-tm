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

import org.squashtest.tm.domain.library.Folder
import org.squashtest.tm.domain.library.Library
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.exception.DuplicateNameException
import org.squashtest.tm.service.internal.repository.FolderDao
import org.squashtest.tm.service.internal.repository.LibraryDao
import org.squashtest.tm.service.internal.repository.TestCaseDao
import org.squashtest.tm.service.security.PermissionEvaluationService
import spock.lang.Specification

class GenericNodeManagementServiceTest extends Specification {
	TestCaseDao nodeDao = Mock()
	FolderDao folderDao = Mock()
	LibraryDao libraryDao = Mock()
	PermissionEvaluationService permissionService = Mock()
	NodeManagementService service = new GenericNodeManagementService(permissionService, nodeDao, folderDao, libraryDao)

	def setup() {
		permissionService.hasRoleOrPermissionOnObject(_, _, _) >> true
	}

	def "should find node"() {
		given:
		TestCase tc  = Mock()
		nodeDao.findById(10) >> tc

		when:
		def found = service.findNode(10)

		then:
		found == tc
	}

	def "should remove a node"(){
		given :
		TestCase tc  = Mock()

		when :
		service.removeNode(10)

		then :
		1 * nodeDao.findById(10) >> tc
		1 * nodeDao.remove(tc)
	}

	def "should rename a node in library root"(){
		given:
		TestCase tc  = Mock()
		tc.name >> "Mike"
		nodeDao.findById(10) >> tc

		and:
		Library lib = Mock()
		libraryDao.findByRootContent(tc) >> lib
		lib.isContentNameAvailable("Bob") >> true

		when :
		service.renameNode(10,"Bob")

		then :
		1 * tc.setName("Bob")
	}

	def "should not rename a node in library root"(){
		given:
		TestCase tc  = new TestCase(name: "Mike")
		nodeDao.findById(10) >> tc

		and:
		Library lib = Mock()
		libraryDao.findByRootContent(tc) >> lib
		lib.isContentNameAvailable("Bob") >> false

		when :
		service.renameNode(10,"Bob")

		then :
		thrown(DuplicateNameException)
	}

	def "should rename a node in folder"(){
		given:
		TestCase tc  = new TestCase(name: "Mike")
		nodeDao.findById(10) >> tc

		and:
		libraryDao.findByRootContent(tc) >> null

		and:
		Folder container = Mock()
		folderDao.findByContent(tc) >> container
		container.isContentNameAvailable("Bob") >> true

		when :
		service.renameNode(10,"Bob")

		then :
		tc.name == "Bob"
	}

	def "should not rename a node in folder"(){
		given:
		TestCase tc  = new TestCase(name: "Mike")
		nodeDao.findById(10) >> tc

		and:
		libraryDao.findByRootContent(tc) >> null

		and:
		Folder container = Mock()
		folderDao.findByContent(tc) >> container
		container.isContentNameAvailable("Bob") >> false

		when :
		service.renameNode(10,"Bob")

		then :
		thrown(DuplicateNameException)
	}

	def "should rename a library root node with its name"(){
		given:
		TestCase tc  = Mock()
		tc.name >> "Mike"
		nodeDao.findById(10) >> tc

		and: "if asked, name is already attributed"
		Library lib = Mock()
		libraryDao.findByRootContent(tc) >> lib
		lib.isContentNameAvailable("Mike") >> false

		when :
		service.renameNode(10,"Mike")

		then :
		notThrown DuplicateNameException
	}

	def "should rename a folder content node with its name"(){
		given:
		TestCase tc  = Mock()
		tc.name >> "Mike"
		nodeDao.findById(10) >> tc

		and: "if asked, name is already attributed"
		Folder container = Mock()
		folderDao.findByContent(tc) >> container
		container.isContentNameAvailable("Mike") >> false

		when :
		service.renameNode(10,"Mike")

		then :
		notThrown DuplicateNameException
	}
}
