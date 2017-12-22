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

import org.squashtest.tm.core.foundation.exception.NullArgumentException;
import org.squashtest.tm.domain.library.LibraryNode;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.internal.dto.json.JsTreeNode;
import org.squashtest.tm.service.internal.dto.json.JsTreeNode.State;

/**
 * Superclass which builds a {@link JsTreeNode} from a LibraryNode.
 *
 * @author Gregory Fouquet
 *
 */
public abstract class LibraryTreeNodeBuilder<LN extends LibraryNode> extends GenericJsTreeNodeBuilder<LN, LibraryTreeNodeBuilder<LN>> {
	protected LN node;
	protected JsTreeNode builtNode;

	public LibraryTreeNodeBuilder(PermissionEvaluationService permissionEvaluationService) {
		super(permissionEvaluationService);
	}

	/**
	 * Hook for template method {@link #build()}. Implementors should add to the {@link JsTreeNode} attributes specific
	 * to the {@link LibraryNode}.
	 *
	 * @param libraryNode
	 * @param treeNode
	 * @see #build()
	 */
	protected abstract void addCustomAttributes(LN libraryNode, JsTreeNode treeNode);

	/**
	 * Adds to the node being build attributes for a leaf node
	 *
	 * @param resType
	 *            the nodeType attribute of the node
	 */
	protected final void addLeafAttributes(String rel, String resType) {
		builtNode.addAttr("rel", rel);
		builtNode.addAttr("resType", resType);
		builtNode.setState(State.leaf);
	}

	/**
	 * Adds to the node being build attributes for a folder node
	 *
	 * @param resType
	 */
	protected final void addFolderAttributes(String resType) {
		builtNode.addAttr("rel", "folder");
		builtNode.addAttr("resType", resType);
		builtNode.setState(State.closed);

		// milestone attributes : folders are yes-men
		builtNode.addAttr("milestone-creatable-deletable", "true");
		builtNode.addAttr("milestone-editable", "true");
	}

	/**
	 * Builds a {@link JsTreeNode} from the {@link LibraryNode} previously set with {@link #setNode(LibraryNode)}
	 * Might return null if no jstree node can be built because of some constraints (milestones notably)
	 *
	 * @return
	 */
	@Override
	public final JsTreeNode doBuild(JsTreeNode builtNode, LN model) {
		this.builtNode = builtNode;
		this.node = model;

		if (passesMilestoneFilter()){
			addCommonAttributes();
			addCustomAttributes(node, builtNode);
		}
		else{
			this.builtNode = null;
		}

		return this.builtNode;

	}

	protected void addCommonAttributes() {
		String name = node.getName();
		builtNode.setTitle(name);
		builtNode.addAttr("name", name);
		builtNode.addAttr("resId", String.valueOf(node.getId()));
		// FIXME may break when node is a proxy
		builtNode.addAttr("id", node.getClass().getSimpleName() + '-' + node.getId());
	}

	/**
	 * if the milestone filter is set,
	 * should say if the current node should be built.
	 */
	protected abstract boolean passesMilestoneFilter();

	/**
	 * s sets the {@link LibraryNode} which will be used to build a {@link JsTreeNode}
	 *
	 * @param node
	 * @return
	 */
	public final LibraryTreeNodeBuilder<LN> setNode(LN node) {
		if (node == null) {
			throw new NullArgumentException("node");
		}
		this.setModel(node);

		return this;
	}

}
