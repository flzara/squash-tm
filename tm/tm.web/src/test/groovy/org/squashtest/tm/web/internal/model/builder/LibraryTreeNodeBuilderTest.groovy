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
import org.squashtest.tm.domain.attachment.AttachmentList
import org.squashtest.tm.domain.library.Copiable
import org.squashtest.tm.domain.library.Library
import org.squashtest.tm.domain.library.LibraryNode
import org.squashtest.tm.domain.library.NodeVisitor
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.service.security.PermissionEvaluationService
import org.squashtest.tm.service.internal.dto.json.JsTreeNode

import spock.lang.Specification

class LibraryTreeNodeBuilderTest extends Specification{
	PermissionEvaluationService permissionEvaluationService = Mock()
	Map rights = Mock()
	boolean hasRights = true
	DummyLibraryTreeNodeBuilder builder = new DummyLibraryTreeNodeBuilder(permissionEvaluationService)

	def setup() {
		permissionEvaluationService.hasRoleOrPermissionsOnObject(_, _, _) >> rights
		rights.get(_) >> { hasRights }
	}

	def "should set shared attributes of node"() {
		given:
		DummyNode node = new DummyNode(name: "tc", id: 10)

		when:
		def res = builder.setNode(node).build()

		then:
		res.title == node.name
		res.attr['resId'] == "${node.id}"
	}

	def "node building should invoke addCustomAttributes template method"() {
		given:
		DummyNode node = new DummyNode(name: "tc", id: 10)

		when:
		def res = builder.setNode(node).build()

		then:
		builder.addCustomAttributesCalled == true
	}

	def "node should not be editable by default"() {
		given:
		DummyNode node = new DummyNode(name: "tc", id: 10)

		and:
		hasRights = false

		when:
		def res = builder.setNode(node).build()

		then:
		res.attr["editable"] == "false"
	}

	def "node should not be editable"() {
		given:
		DummyNode node = new DummyNode(name: "tc", id: 10)

		and:
		permissionEvaluationService.hasRoleOrPermissionOnObject(_, _, _) >> true

		when:
		def res = builder.setNode(node).build()

		then:
		res.attr["editable"] == "true"
	}

	def "node should be expanded"() {
		given:
		DummyNode node = new DummyNode(name: "tc", id: 10)
		node.children << new DummyNode(name: "c1", id: 100)
		node.children << new DummyNode(name: "c2", id: 200)

		and:
		MultiMap expand = new MultiValueMap()
		expand.put("TestCase", 10L)

		when:
		def res = builder.expand(expand).setNode(node).build()

		then:
		res.getChildren().size() == 2
	}

}

class DummyLibraryTreeNodeBuilder extends LibraryTreeNodeBuilder<DummyNode> {
	boolean addCustomAttributesCalled

	DummyLibraryTreeNodeBuilder(pes) {
		super(pes)
	}

	void addCustomAttributes(DummyNode libraryNode, JsTreeNode treeNode) {
		addCustomAttributesCalled = true
	}

	/**
	 * @see org.squashtest.tm.web.internal.model.builder.GenericJsTreeNodeBuilder#doAddChildren(JsTreeNode, org.squashtest.tm.domain.Identified)
	 */
	@Override
	protected void doAddChildren(JsTreeNode node, DummyNode model) {

		model.children.each {
			node.children << it.id
		}

	}

	@Override
	protected boolean passesMilestoneFilter() {
		return true;
	}

}

class DummyNode implements  LibraryNode {
	Long id
	String name
	String description
	List children = []
	void deleteMe(){}
	Project getProject() {}
	Library<LibraryNode> getLibrary() {}
	void notifyAssociatedWithProject(Project project){}
	Copiable createCopy() {return null}
	void accept(NodeVisitor visitor) {
		visitor.visit(new TestCase())
	}
	AttachmentList getAttachmentList() {}
}
