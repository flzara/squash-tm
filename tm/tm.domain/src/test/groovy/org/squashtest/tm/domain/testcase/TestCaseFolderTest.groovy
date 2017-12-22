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
package org.squashtest.tm.domain.testcase;

import org.squashtest.tm.tools.unittest.reflection.ReflectionCategory
import org.squashtest.tm.domain.library.GenericLibraryNode
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.domain.testutils.MockFactory;
import org.squashtest.tm.exception.DuplicateNameException;

import spock.lang.Specification


class TestCaseFolderTest extends Specification {

	MockFactory mockFactory = new MockFactory()

	TestCaseFolder folder = new TestCaseFolder()

	def setup(){
		folder.notifyAssociatedWithProject(mockFactory.mockProject())
	}

	def "should add test case to folder"() {
		given:
		TestCase testCase = new TestCase(name: "foo")

		when:
		folder.addContent(testCase)

		then:
		folder.content.contains testCase
	}

	def "should not add test with dup name"() {
		given:
		TestCase testCase = new TestCase(name: "foo")
		folder.addContent(testCase)

		when:
		folder.addContent(new TestCase(name: "foo"))

		then:
		thrown DuplicateNameException
	}

	def "should set this folder's project as the project of new content"() {
		given:
		Project project = new Project()
		use(ReflectionCategory) {
			GenericLibraryNode.set field: "project", of: folder, to: project
		}

		and:
		TestCaseFolder newContent = new TestCaseFolder()

		when:
		folder.addContent newContent

		then:
		newContent.project == project
	}

	def "should propagate this folder's project to its content"() {

		given:
		TestCaseFolder content = new TestCaseFolder()
		folder.addContent content

		and:
		Project project = new Project()

		when:
		folder.notifyAssociatedWithProject project

		then:
		content.project == project
	}


}
