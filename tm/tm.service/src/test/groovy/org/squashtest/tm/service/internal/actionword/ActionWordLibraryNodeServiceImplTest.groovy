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

import org.squashtest.tm.domain.actionword.ActionWordLibrary
import org.squashtest.tm.domain.actionword.ActionWordLibraryNode
import org.squashtest.tm.domain.actionword.ActionWordTreeDefinition
import org.squashtest.tm.domain.bdd.ActionWord
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.service.internal.repository.ActionWordLibraryNodeDao
import spock.lang.Specification

class ActionWordLibraryNodeServiceImplTest extends Specification {

	ActionWordLibraryNodeServiceImpl service = new ActionWordLibraryNodeServiceImpl()

	ActionWordLibraryNodeDao actionWordLibraryNodeDao = Mock()

	def setup() {
		service.actionWordLibraryNodeDao = actionWordLibraryNodeDao
	}

	def "should create a new action word node"() {
		given:
			def parentId = -1L
			def awTreeEntity = new ActionWord("hello")
		and:
			def awLibrary = Mock(ActionWordLibrary)
			def project = Mock(Project)
			awLibrary.getProject() >> project
		and:
			def parentNode = new ActionWordLibraryNode()
			parentNode.setEntityType(ActionWordTreeDefinition.LIBRARY)
			parentNode.setLibrary(awLibrary)
			1 * actionWordLibraryNodeDao.getOne(parentId) >> parentNode
		when:
			service.createNewNode(parentId, awTreeEntity)
		then:
			1 * actionWordLibraryNodeDao.save({
				it != null
				it.name == "hello"
				it.entity != null
				it.entity.word == "hello"
				it.getLibrary() == awLibrary
			})
	}
}
