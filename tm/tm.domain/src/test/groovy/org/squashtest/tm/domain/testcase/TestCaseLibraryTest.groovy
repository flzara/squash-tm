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

import org.squashtest.tm.domain.infolist.InfoList
import org.squashtest.tm.domain.infolist.InfoListItem
import org.squashtest.tm.domain.infolist.SystemListItem
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.domain.project.ProjectTemplate
import org.squashtest.tm.domain.testutils.MockFactory;
import org.squashtest.tm.exception.DuplicateNameException

import spock.lang.Specification

class TestCaseLibraryTest   extends Specification {

	MockFactory mockFactory = new MockFactory()
	TestCaseLibrary library = new TestCaseLibrary()

	def setup() {
		library.notifyAssociatedWithProject(mockFactory.mockProject());
	}

	def "should add test case to library"() {
		given:
		TestCase testCase = new TestCase(name: "foo")

		when:
		library.addContent(testCase)

		then:
		library.rootContent.contains testCase
	}

	def "should not add test case to library template"() {
		given:
		TestCase testCase = new TestCase(name: "foo")
		library.notifyAssociatedWithProject(new ProjectTemplate());

		when:
		library.addContent(testCase)

		then:
		thrown UnsupportedOperationException
	}

	def "should not add test with dup name"() {
		given:
		TestCase testCase = new TestCase(name: "foo")
		library.addContent(testCase)

		when:
		library.addContent(new TestCase(name: "foo"))

		then:
		thrown DuplicateNameException
	}

	def "should set project of library to newly added node"() {
		given:
		Project project = new Project()
		library.project = project

		and:
		TestCaseFolder folder = new TestCaseFolder(name: "foo")

		when:
		library.addContent folder

		then:
		folder.project == project
	}


}
