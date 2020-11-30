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
package org.squashtest.tm.service.internal.actionword

import org.springframework.transaction.annotation.Transactional
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.squashtest.tm.domain.actionword.ActionWordLibraryNode
import org.squashtest.tm.domain.actionword.ActionWordTreeDefinition
import org.squashtest.tm.domain.bdd.ActionWord
import org.squashtest.tm.domain.bdd.ActionWordFragment
import org.squashtest.tm.domain.bdd.ActionWordText
import org.squashtest.tm.exception.DuplicateNameException
import org.squashtest.tm.service.actionword.ActionWordLibraryNodeService
import org.squashtest.tm.service.internal.repository.ActionWordDao
import org.squashtest.tm.service.internal.repository.ProjectDao
import org.unitils.dbunit.annotation.DataSet
import spock.lang.Unroll
import spock.unitils.UnitilsSupport

import javax.inject.Inject

import static org.squashtest.tm.domain.actionword.ActionWordTreeDefinition.ACTION_WORD

@DataSet
@Transactional
@UnitilsSupport
class ActionWordLibraryNodeServiceIT extends DbunitServiceSpecification {

	@Inject
	ActionWordDao actionWordDao

	@Inject
	ProjectDao projectDao

	@Inject
	ActionWordLibraryNodeService actionWordLibraryNodeService

	def createBasicActionWord(String singleFragment) {
		def fragment = new ActionWordText(singleFragment)
		return new ActionWord([fragment] as List)
	}

	def  "should create a new action word node"() {
		given:
			ActionWord newActionWord = createBasicActionWord("a new action word")
		when:
			def newNode = actionWordLibraryNodeService.createNewNode(-1L, newActionWord)
		then:
			newNode != null
			newNode.id != null
			newNode.name == "a new action word"

			def library = newNode.library
			library != null
			library.id == -1L

			def project = library.project
			project != null
			project.id == -1L
			project.name == "action word project"
	}

	def "should not create a new action word with a name already in use"() {
		given:
			ActionWord newActionWord = createBasicActionWord("press the red button")
		when:
			actionWordLibraryNodeService.createNewNode(-1L, newActionWord)
		then:
			thrown DuplicateNameException
	}

	def "should find a node of type action word from its entity"() {
		given:
			def actionWord = actionWordDao.getOne(-1L)
			actionWord.id == -1L
			actionWord.name == "press the red button"
			actionWord.project.id == -1L
		when:
			def node = actionWordLibraryNodeService.findNodeFromEntity(actionWord)
		then:
			node != null
			node.id == -2L
			node.name == "press the red button"
			node.entityType == ACTION_WORD

			node.children != null
			node.children.size() == 0

			node.library != null
			node.library.id == -1L
			node.library.name == "action word project"

			node.library.project != null
			node.library.project.id == -1L
			node.library.project.name == "action word project"
	}

	def "should find a node of type library from its entity"() {
		given:
			def project = projectDao.getOne(-1L)
			project.id == -1L
			project.name == "action word project"
			def actionWordLibrary = project.getActionWordLibrary()
			actionWordLibrary.id == -1L
			actionWordLibrary.name == "action word project"
		when:
			def node = actionWordLibraryNodeService.findNodeFromEntity(actionWordLibrary)
		then:
			node != null
			node.id == -1L
			node.name == "action word project"
			node.entityType == ActionWordTreeDefinition.LIBRARY

			node.library.project != null
			node.library.project.id == -1L
			node.library.project.name == "action word project"
	}

	def "should delete various nodes"() {
		when:
		actionWordLibraryNodeService.delete(nodesIds)

		then:

		deletedNodesIds.every {
			! found(ActionWordLibraryNode, it)
		}

		siblingIds.every {
			found (ActionWordLibraryNode, it)
		}

		where:
		nodesIds 		|| 	 		siblingIds													|	deletedNodesIds
		[-2L]			||	[-3L,-4L,-7L,-6L]													|	[-2L]
		[-2L,-4L]		||	[-3L,-7L,-6L]														|	[-2L,-4L]
		[-3L,-7L]		||	[-2L,-4L,-6L]														|	[-3L,-7L]
	}

	@Unroll
	def "should return Action word node path relative to its project"(){
		expect:
		actionWordLibraryNodeService.findActionWordLibraryNodePathById(nodeId) == nodePath

		where:
		nodeId			| nodePath
		-1L				| "action word project"
		-2L				| "action word project/press the red button"
		-3L				| "action word project/today is \"param1\""
		-4L				| "action word project/I have \"number\" of \"fruit\""
		-5L				| "another project"
		-6L				| "another project/another day"
		-7L				| "another project/\"param1\" + \"param2\" = \"param3\""
	}

	@DataSet("ActionWordLibraryNodeServiceCopyNodeIT.xml")
	def "simulateCopyNodes(List<Long>, long) - Should find no action words with the same token and return true"() {
		expect:
			actionWordLibraryNodeService.simulateCopyNodes([-2L, -3L, -4L], -7L)
	}

	@DataSet("ActionWordLibraryNodeServiceCopyNodeIT.xml")
	def "copyNodes(List<Long>, long) - Should copy three action word nodes into an empty library"() {
		when:
			def nodeList = actionWordLibraryNodeService.copyNodes([-2L, -3L, -4L], -7L)
			em.flush()
			em.clear()
		then:
			nodeList.size() == 3
			ActionWordLibraryNode targetLibraryNode = findEntity(ActionWordLibraryNode.class, -7L)
			List<ActionWordLibraryNode> children = targetLibraryNode.getChildren()
			children.size() == 3
			children.every {
				it.getId() != null
				! it.getName().isEmpty()
				it.getLibrary().getId() == -3L
				it.getParent() == targetLibraryNode
				it.getChildren().isEmpty()
				it.getEntityType() == ACTION_WORD

				ActionWord aw = it.getEntity()
				aw.getId() != null
				! aw.getName().isEmpty()
				! aw.getDescription().isEmpty()
				aw.getProject().getId() == -3L
				aw.getKeywordTestSteps().size() == 0
				! aw.generateToken().isEmpty()
				List<ActionWordFragment> fragments = aw.getFragments()
				! fragments.isEmpty()
				fragments.every {
					it.getId() != null
					it.getActionWord() == aw
				}
			}
	}

	@DataSet("ActionWordLibraryNodeServiceCopyNodeIT.xml")
	def "simulateCopyNodes(List<Long>, long) - Should find an action word with the same token and return false"() {
		expect:
			! actionWordLibraryNodeService.simulateCopyNodes([-2L], -5L)
	}

	@DataSet("ActionWordLibraryNodeServiceCopyNodeIT.xml")
	def "copyNodes(List<Long>, long) - Should try to copy an action word node with an existing name into a library"() {
		when:
			def nodeList = actionWordLibraryNodeService.copyNodes([-2L], -5L)
			em.flush()
			em.clear()
		then:
			nodeList.isEmpty()
			ActionWordLibraryNode targetLibraryNode = findEntity(ActionWordLibraryNode.class, -5L)
			List<ActionWordLibraryNode> children = targetLibraryNode.getChildren()
			children.size() == 1
	}

}
