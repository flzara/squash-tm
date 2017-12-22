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
package org.squashtest.tm.service.internal.requirement

import org.springframework.transaction.annotation.Transactional
import org.squashtest.tm.domain.requirement.RequirementLibraryNode
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.unitils.dbunit.annotation.DataSet
import spock.unitils.UnitilsSupport

@UnitilsSupport
@Transactional
class RequirementNodeWalkerIT extends DbunitServiceSpecification {

	@DataSet("RequirementnodeWalkerIT.xml")
	def "Should retrieve only the selected requirement "(){

		given:
		def RequirementNodeWalker walker = new RequirementNodeWalker()

		and:
		def selectedNodeId = -10L
		RequirementLibraryNode node = getSession().get(RequirementLibraryNode.class, selectedNodeId)

		when :
		List retrieveNodes = walker.walk([node])

		then :
		retrieveNodes.size() == 1
		retrieveNodes[0].getId() == selectedNodeId
	}


	@DataSet("RequirementnodeWalkerIT.xml")
	def "Should retrieve the selected requirement folder and all its children"(){

		given:
		def RequirementNodeWalker walker = new RequirementNodeWalker()

		and:
		def selectedNodeId = -1L
		RequirementLibraryNode node = getSession().get(RequirementLibraryNode.class, selectedNodeId)

		when :
		List retrievedNodes = walker.walk([node])

		then :
		retrievedNodes.size() == 4
		retrievedNodes*.getId().sort() == [-10L, -11L, -12L, -13L].reverse()
	}
}
