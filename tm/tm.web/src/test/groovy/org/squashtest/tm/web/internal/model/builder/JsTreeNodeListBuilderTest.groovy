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
package org.squashtest.tm.web.internal.model.builder

import org.squashtest.tm.domain.Identified
import org.squashtest.tm.service.security.PermissionEvaluationService
import org.squashtest.tm.service.internal.dto.json.JsTreeNode
import org.squashtest.tm.web.internal.controller.generic.NodeBuildingSpecification

class JsTreeNodeListBuilderTest extends NodeBuildingSpecification {
	def "should build list of tree nodes"() {
		given:
		PermissionEvaluationService permEvaluator = Mock()
		Map<String, Boolean> perms = Mock()
		perms.get(_) >> false
		permEvaluator._ >> perms

		and:
		DummyBuilder nodeBuilder = new DummyBuilder(permEvaluator)
		JsTreeNodeListBuilder listBuilder = new JsTreeNodeListBuilder(nodeBuilder)


		when:
		def nodes = listBuilder.setModel([dummy("foo"), dummy("bar")]).build()


		then:
		nodes*.title == ["foo", "bar"]
	}

	private Dummy dummy(title) {
		new Dummy(title: title)
	}
}

class Dummy implements Identified {
	public String title
	@Override
	Long getId() {
		return 1
	}
}

class DummyBuilder extends GenericJsTreeNodeBuilder<Dummy, DummyBuilder> {
	String title

	def DummyBuilder(pes) {
		super(pes)
	}

	@Override
	protected JsTreeNode doBuild(JsTreeNode node, Dummy model) {
		node.title = model.title
		return node;
	}

	/**
	 * @see org.squashtest.tm.web.internal.model.builder.JsTreeNodeBuilder#doAddChildren(org.squashtest.tm.service.internal.dto.json.JsTreeNode, java.lang.Object)
	 */
	@Override
	protected void doAddChildren(JsTreeNode node, Dummy model) {
		// NOOP

	}

}
