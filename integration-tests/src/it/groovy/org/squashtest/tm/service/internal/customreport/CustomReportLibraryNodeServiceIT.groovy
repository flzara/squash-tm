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

import javax.inject.Inject

import org.hibernate.SessionFactory
import org.springframework.transaction.annotation.Transactional
import org.squashtest.tm.domain.chart.ChartDefinition;
import org.squashtest.tm.domain.tree.TreeEntity
import org.squashtest.tm.domain.customreport.CustomReportFolder;
import org.squashtest.tm.domain.customreport.CustomReportLibrary;
import org.squashtest.tm.domain.customreport.CustomReportLibraryNode;
import org.squashtest.tm.domain.customreport.CustomReportTreeDefinition;
import org.squashtest.tm.service.customreport.CustomReportLibraryNodeService;
import org.squashtest.tm.service.internal.repository.CustomReportLibraryDao;
import org.squashtest.tm.service.internal.repository.CustomReportLibraryNodeDao;
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.unitils.dbunit.annotation.DataSet

import spock.unitils.UnitilsSupport

@UnitilsSupport
@Transactional
@DataSet("CustomReportLibraryNodeServiceIT.sandbox.xml")
class CustomReportLibraryNodeServiceIT extends DbunitServiceSpecification {

	@Inject
	CustomReportLibraryNodeService service;

	@Inject
	CustomReportLibraryNodeDao crlnDao;

	@Inject
	CustomReportLibraryDao crlDao;

	def "should add new folder to library"() {
		given :
		def parent = crlnDao.findOne(-1L)
		def library = crlDao.findOne(-1L)

		CustomReportFolder folder = new CustomReportFolder()
		folder.setName("newFolder")

		when:
		def res = service.createNewNode(-1L,folder)
		def resId = res.getId()
		getSession().flush()
		getSession().clear()
		def newChildAfterPersist = crlnDao.findOne(resId)
		def parentNode = newChildAfterPersist.getParent()
		def entityLinkedToNode = newChildAfterPersist.getEntity()
		def projectLinked = entityLinkedToNode.getProject();

		then:
		res.id != null
		library != null
		parentNode.id == parent.id
		entityLinkedToNode != null
		projectLinked.getId() == -1L
	}

	def "should find descendants for nodes"() {

		when:
		def res = service.findDescendantIds(parentIds)

		then:
		res as Set == childrenIds as Set

		where:
		parentIds 			|| 	childrenIds
		[-20L]				||	[-20L,-40L]
		[-6L]				||	[-6L,-11L,-12L,-13L,-14L,-15L]
		[-7L]				||	[-7L]
		[-2L]				||	[-2L,-3L,-4L,-5L,-16L]
		[-10L]				||	[-10L,-20L,-30L,-40L,-2L,-3L,-4L,-5L,-7L,-16L]
		[-10L,-20L,-30L]	||	[-10L,-20L,-30L,-40L,-2L,-3L,-4L,-5L,-7L,-16L]
		[-10L,-7L]			||	[-10L,-20L,-30L,-40L,-2L,-3L,-4L,-5L,-7L,-16L]
		[-2L,-6L]			||	[-6L,-11L,-12L,-13L,-14L,-15L,-2L,-3L,-4L,-5L,-16L]
	}

	def "should delete various nodes"() {

		when:
		service.delete(nodesIds)

		then:

		for (id in deletedNodesIds) {
			def node = crlDao.findOne(id);
			node == null;
		}

		for (id in siblingIds) {
			def node = crlDao.findOne(id);
			node != null;
		}

		where:
		nodesIds 		|| 	 		siblingIds													|	deletedNodesIds
		[-40L]			||	[-10L,-20L,-30L,-2L,-3L,-4L,-5L,-7L,-6L,-11L,-12L,-13L,-14L,-16L]	|	[-40L]
		[-20L]			||	[-10L,-30L,-2L,-3L,-4L,-5L,-7L,-6L,-11L,-12L,-13L,-14L,-16L]		|	[-20L,-40L]
		[-20L,-40L]		||	[-10L,-30L,-2L,-3L,-4L,-5L,-7L,-6L,-11L,-12L,-13L,-14L,-16L]		|	[-20L,-40L]
		[-20L,-40L,-12L]||	[-10L,-30L,-2L,-3L,-4L,-5L,-7L,-6L,-11L,-16L]						|	[-20L,-40L,-12L,-13L,-14L]
		[-20L,-30L]		||	[-10L,-2L,-3L,-4L,-5L,-7L,-6L,-11L,-12L,-13L,-14L,-16L]				|	[-20L,-30L,-40L]
		[-11L,-15L]		||	[-10L,-20L,-30L,-40L,-2L,-3L,-4L,-5L,-7L,-6L,-12L,-13L,-14L,-16L]	|	[-11L,-15L]
	}

	def "should rename node and entity"() {

		when:
		service.renameNode(nodeId, newName)


		then:
		CustomReportLibraryNode node = crlnDao.findOne(nodeId)
		node.getName().equals(newName)
		node.getEntity().getName().equals(newName)

		where:
		nodeId 	|| newName
		-20L	|| "newFolderName"
		-2L		|| "newDashName"

	}

	def "should find node from library"() {
		given :
		def library = findEntity(CustomReportLibrary, -1L);

		when:
		CustomReportLibraryNode node = crlnDao.findNodeFromEntity(library);

		then:
		node.id == -1L
		node.name == "project-1"

	}

}
