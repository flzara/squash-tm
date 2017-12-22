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
package org.squashtest.tm.domain.library;

import org.squashtest.tm.domain.infolist.InfoList;
import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.domain.infolist.SystemListItem;
import org.squashtest.tm.domain.library.Folder
import org.squashtest.tm.domain.library.FolderSupport
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.domain.testutils.MockFactory;
import org.squashtest.tm.exception.DuplicateNameException;

import spock.lang.Specification

class FolderSupportTest extends Specification{
	Folder folder = Mock()

	MockFactory mockFactory = new MockFactory()

	/**
	 * The tested object
	 */
	FolderSupport folderSupport = new FolderSupport(folder)

	def "should not add test with dup name"() {
		given:
		TestCase testCase = new TestCase(name: "foo")
		folder.getContent() >> [testCase]

		when:
		folderSupport.addContent(new TestCase(name: "foo"))

		then:
		thrown DuplicateNameException
	}

	def "should set this folder's project as the project of new content"() {
		given:
		Project project = mockFactory.mockProject()


		folder.getProject() >> project
		folder.getContent() >> []


		and:
		TestCase newContent = new TestCase(name: 'foo')

		when:
		folderSupport.addContent newContent

		then:
		newContent.project == project
	}

	def "should set folder contents project when notified of new project"() {
		given:
		Project project = mockFactory.mockProject()

		and:
		TestCase content = new TestCase(name: 'foo')
		folder.getContent() >> [content]

		when:
		folderSupport.notifyAssociatedProjectWasSet null, project

		then:
		content.project == project
	}

	def "should set folder contents project when notified of project change"() {
		given:
		Project formerProject = mockFactory.mockProject()
		Project newProject = mockFactory.mockProject()

		and:
		TestCase content = new TestCase(name: 'foo')
		folder.getContent() >> [content]

		when:
		folderSupport.notifyAssociatedProjectWasSet formerProject, newProject

		then:
		content.project == newProject
	}

	def "should set not folder contents project when notified of same project"() {
		given:
		Project project = new Project()

		and:
		TestCase content = new TestCase(name: 'foo')
		folder.getContent() >> [content]

		when:
		folderSupport.notifyAssociatedProjectWasSet project, project

		then:
		content.project == null
	}



}
