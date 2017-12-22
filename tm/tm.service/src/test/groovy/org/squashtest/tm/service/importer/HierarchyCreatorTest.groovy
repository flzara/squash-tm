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
package org.squashtest.tm.service.importer


import java.util.zip.ZipInputStream

import org.squashtest.tm.domain.infolist.InfoList
import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.domain.infolist.SystemListItem;
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.domain.testcase.TestCaseFolder
import org.squashtest.tm.service.testutils.MockFactory;
import org.squashtest.tm.service.internal.archive.Entry
import org.squashtest.tm.service.internal.archive.ZipReader.ZipReaderEntry
import org.squashtest.tm.service.internal.importer.ExcelTestCaseParser
import org.squashtest.tm.service.internal.importer.HierarchyCreator

import spock.lang.Specification


class HierarchyCreatorTest extends Specification {

	MockFactory mockFactory = new MockFactory()

	def "should find an existing folder"(){

		given :
		def importer = new HierarchyCreator();

		and :
		def folder = Mock(TestCaseFolder)
		importer.pathMap.put("/toto/folder", folder);

		and :
		def entry = Mock(Entry)
		entry.getName() >> "/toto/folder"

		when :
		def result = importer.findOrCreateFolder(entry)


		then :
		result == folder

	}

	def "should recursively create missing parent folders"(){

		given :
		def importer = new HierarchyCreator();


		and :
		def entry = new ZipReaderEntry(null, "/melvin/van/peebles", true);

		when :
		importer.findOrCreateFolder(entry)

		then :
		def peebles = importer.pathMap.getMappedElement("/melvin/van/peebles")
		peebles instanceof TestCaseFolder
		peebles.getName() == "peebles"

		def van = importer.pathMap.getMappedElement("/melvin/van")
		van instanceof TestCaseFolder
		van.getName() == "van"
		van.getContent() == [peebles]

		def melvin = importer.pathMap.getMappedElement("/melvin")
		melvin instanceof TestCaseFolder
		melvin.getName() == "melvin"
		melvin.getContent() == [van]

		def root = importer.pathMap.getMappedElement("/")
		root.getContent() == [melvin]

	}

	def "should create a test case"(){

		given :
		def importer = new HierarchyCreator();
		def parser = Mock(ExcelTestCaseParser)
		parser.stripFileExtension("peebles.xlsx") >> "peebles"
		importer.setParser(parser)

		and :
		def project = mockFactory.mockProject()
		importer.setProject(project)

		and :
		def mTc = new TestCase()
		parser.parseFile(_,_) >> mTc


		and :
		def entry = new ZipReaderEntry(Mock(ZipInputStream), "/melvin/van/peebles.xlsx", false);

		and :
		def parent = new TestCaseFolder()
		parent.notifyAssociatedWithProject(project)
		importer.pathMap.put("/melvin/van", parent)

		when :
		importer.createTestCase(entry)

		then :
		def tc = importer.pathMap.getMappedElement("/melvin/van/peebles.xlsx")
		tc instanceof TestCase
		tc.getName() == "peebles"

		parent.getContent() == [tc]
	}


}
