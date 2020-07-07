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
import org.squashtest.tm.domain.actionword.ActionWordTreeDefinition
import org.squashtest.tm.domain.bdd.ActionWord
import org.squashtest.tm.domain.bdd.ActionWordText
import org.squashtest.tm.exception.DuplicateNameException
import org.squashtest.tm.service.actionword.ActionWordLibraryNodeService
import org.squashtest.tm.service.internal.repository.ActionWordDao
import org.squashtest.tm.service.internal.repository.ProjectDao
import org.unitils.dbunit.annotation.DataSet
import spock.unitils.UnitilsSupport

import javax.inject.Inject

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
			node.entityType == ActionWordTreeDefinition.ACTION_WORD

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
}
