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
package org.squashtest.tm.domain.requirement

import org.squashtest.tm.tools.unittest.reflection.ReflectionCategory
import org.squashtest.tm.domain.attachment.Attachment
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.exception.DuplicateNameException
import spock.lang.Ignore;
import spock.lang.Specification

class RequirementFolderTest extends Specification {
	RequirementFolder folder = new RequirementFolder()

	def "folder should hold a name and description"() {
		when:
		folder.name = "my name"
		folder.description = "my desc"

		then:
		folder.name == "my name"
		folder.description == "my desc"
	}

	def "should add a folder in another folder"(){

		given:
		folder.setName("bar")
		RequirementFolder fooFolder = new RequirementFolder()
		fooFolder.setName("foo")

		when:
		folder.addContent(fooFolder)

		then:
		folder.getContent().contains(fooFolder)
	}

	def "should set this folder's project as the project of new content"() {
		given:
		Project project = new Project()

		use(ReflectionCategory) {
			RequirementLibraryNode.set field: "project", of: folder, to: project
		}

		and:
		RequirementFolder newContent = new RequirementFolder()

		when:
		folder.addContent newContent

		then:
		newContent.project == project
	}

	def "should propagate this folder's project to its content"() {

		given:
		RequirementFolder content = new RequirementFolder()
		folder.addContent content

		and:
		Project project = new Project()

		when:
		folder.notifyAssociatedWithProject project

		then:
		content.project == project
	}

	def "should create a 'pastable' copy"() {
		given:
		RequirementFolder folder = new RequirementFolder(name: "foo", description: "bar")

		and:
		Attachment attachment = new Attachment()
		attachment.setType("txt")
		folder.attachmentList.addAttachment attachment

		when:
		def res = folder.createCopy()

		then:
		res.name == folder.name
		res.description == folder.description
		res.resource != null
		res.resource.name == folder.name

		res.attachmentList.allAttachments.size() == 1
		!res.attachmentList.allAttachments.contains(attachment)
	}

}
