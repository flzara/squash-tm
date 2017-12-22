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
package org.squashtest.tm.web.internal.model.builder;

import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.squashtest.tm.tools.unittest.reflection.ReflectionCategory
import org.squashtest.tm.domain.infolist.ListItemReference;
import org.squashtest.tm.domain.requirement.Requirement
import org.squashtest.tm.domain.requirement.RequirementFolder
import org.squashtest.tm.domain.requirement.RequirementLibraryNode
import org.squashtest.tm.domain.requirement.RequirementVersion
import org.squashtest.tm.web.internal.controller.generic.NodeBuildingSpecification;
import org.squashtest.tm.web.testutils.MockFactory;
import org.squashtest.tm.service.internal.dto.json.JsTreeNode.State

class RequirementLibraryTreeNodeBuilderTest extends NodeBuildingSpecification {
	RequirementLibraryTreeNodeBuilder builder = new RequirementLibraryTreeNodeBuilder(permissionEvaluator())

	def "should build a RequirementFolder node"() {
		given:
		RequirementFolder node  = new RequirementFolder(name: "f")

		use (ReflectionCategory) {
			RequirementLibraryNode.set field: "id", of: node, to: 10L
		}

		when:
		def res = builder.setNode(node).build()

		then:
		res.title == node.name
		res.attr['resId'] == "${node.id}"
		res.attr['rel'] == "folder"
		res.attr['resType'] == "requirement-folders"
		res.state == State.leaf.name()
	}
	def "should build a Requirement node"() {
		given:
		RequirementVersion version = new RequirementVersion(name: "r", reference: "ref", category : new ListItemReference("RANDOM_CATEGORY"))
		Requirement node  = new Requirement(version)

		use (ReflectionCategory) {
			RequirementLibraryNode.set field: "id", of: node, to: 10L
		}

		when:
		def res = builder.setNode(node).build()

		then:
		res.title == node.reference + " - " + node.name
		res.attr['resId'] == "${node.id}"
		res.attr['resType'] == "requirements"
		res.attr['rel'] == "requirement"
		res.state == State.leaf.name()
	}

	def "should build a folder with leaf state"(){
		given :
		RequirementFolder node = new RequirementFolder(name:"folder")

		when :
		def res = builder.setNode(node).build()

		then :
		res.state == State.leaf.name()

	}

	def "should build a folder with closed state"(){
		given :
		RequirementFolder node = new RequirementFolder(name:"folder")
		node.addContent(new RequirementFolder());

		when :
		def res = builder.setNode(node).build()

		then :
		res.state == State.closed.name()

	}

	def "should expand a requirement node"(){
		given :
		Requirement node = new Requirement(resource: new RequirementVersion(), name:"folder", category : new ListItemReference("CAT_UNDEFINED"))
		node.notifyAssociatedWithProject(new MockFactory().mockProject())
		Requirement child = new Requirement(resource: new RequirementVersion(), name:"folder child", category : new ListItemReference("CAT_UNDEFINED"))
		node.addContent(child);

		use(ReflectionCategory) {
			RequirementLibraryNode.set field: "id", of: node, to: 10L
			RequirementLibraryNode.set field: "id", of: child, to: 100L
		}

		and:
		MultiMap expanded = new MultiValueMap()
		expanded.put("Requirement", 10L)

		when :
		def res = builder.expand(expanded).setNode(node).build()

		then :
		res.state == State.open.name()
		res.children.size() == 1

	}


}
