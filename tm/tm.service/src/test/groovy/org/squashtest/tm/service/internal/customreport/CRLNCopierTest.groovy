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
package org.squashtest.tm.service.internal.customreport

import org.squashtest.tm.domain.customreport.CustomReportFolder
import org.squashtest.tm.domain.customreport.CustomReportLibrary
import org.squashtest.tm.domain.customreport.CustomReportLibraryNode
import org.squashtest.tm.domain.customreport.CustomReportTreeDefinition
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.service.internal.repository.CustomReportLibraryNodeDao
import spock.lang.Specification

/**
 * Created by jthebault on 29/02/2016.
 */
class CRLNCopierTest extends Specification {

	CustomReportLibraryNodeDao customReportLibraryNodeDao = Mock()

	def setup(){
		Project project = Mock()
		CustomReportLibrary library = Mock()
		CustomReportLibraryNode nodeFolder1 = new CustomReportLibraryNode()
		CustomReportFolder folder1 = new CustomReportFolder()
		folder1.setName("Folder1")
		folder1.setDescription("un superbe repertoire")
		folder1.setProject(project)
		nodeFolder1.setName("Folder1")
		nodeFolder1.setEntity(folder1)
		nodeFolder1.setLibrary(library)
		customReportLibraryNodeDao.findOne(1L) >> nodeFolder1

		CustomReportLibraryNode targetNode = new CustomReportLibraryNode()
		CustomReportFolder targetFolder = new CustomReportFolder()
		targetFolder.setName("FolderTarget")
		targetFolder.setDescription("un autre repertoire")
		targetNode.setName("FolderTarget")
		targetNode.setEntity(targetFolder)
		targetNode.setLibrary(library)
		targetNode.entityType = CustomReportTreeDefinition.FOLDER
		customReportLibraryNodeDao.findOne(2L) >> targetNode

	}

	def "shouldResolveNameConflict"(){
		given:
		CustomReportLibraryNode target = Mock()
		target.childNameAlreadyUsed('name1') >> true
		target.childNameAlreadyUsed('name2') >> true
		target.childNameAlreadyUsed('name2-Copie1') >> true

		CustomReportLibraryNode origin = new CustomReportLibraryNode()
		origin.setEntity(new CustomReportFolder())
		origin.setName('name1')

		CustomReportLibraryNode origin2 = new CustomReportLibraryNode()
		origin2.setEntity(new CustomReportFolder())
		origin2.setName('name2')

		and:
		NameResolver resolver = new NameResolver()

		when:
		resolver.resolveNewName(origin,target)
		resolver.resolveNewName(origin2,target)

		then:
		origin.getName().equals("name1-Copie1")
		origin2.getName().equals("name2-Copie2")
	}

	def "should copy a single node"(){
		given:
		CustomReportLibraryNode source = customReportLibraryNodeDao.findOne(1L)
		CustomReportLibraryNode target = customReportLibraryNodeDao.findOne(2L)

		and:
		CRLNCopier treeLibraryNodeCopier = new CRLNCopier()
		treeLibraryNodeCopier.nameResolver = Mock(NameResolver)

		when:
		treeLibraryNodeCopier.copyNodes([source],target)

		then:
		def children = target.getChildren()
		children.size() == 1
		def copy = children.get(0)
		copy.name == source.getName()
		copy.id != 1L
		CustomReportFolder entity = copy.getEntity()
		entity.getDescription().equals(source.getEntity().getDescription())

	}

}
