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
package org.squashtest.tm.api.widget.access;

import org.squashtest.tm.api.security.acls.AccessRule;
import org.squashtest.tm.api.security.acls.Permission;
import org.squashtest.tm.api.widget.NodeSelectionBuilder;
import org.squashtest.tm.api.widget.TreeNodeType;

/**
 * @author Gregory
 * 
 */
public class DefaultNodeSelectionBuilder implements NodeSelectionBuilder, OperationBuilder {
	private final NodeSelection selection;

	/**
	 * 
	 */
	public DefaultNodeSelectionBuilder(SelectionMode selectionMode) {
		super();
		selection = new NodeSelection(selectionMode);
	}

	/**
	 * @see org.squashtest.tm.api.widget.access.OperationBuilder#or()
	 */
	@Override
	public NodeSelectionBuilder or() {
		return this;
	}

	/**
	 * @see org.squashtest.tm.api.widget.NodeSelectionBuilder#nodePermission(org.squashtest.tm.api.widget.TreeNodeType,
	 *      org.squashtest.tm.api.security.acls.Permission)
	 */
	@Override
	public OperationBuilder nodePermission(TreeNodeType nodeType, Permission permission) {
		selection.addRule(new SelectedNodePermission(nodeType, permission));
		return this;
	}

	/**
	 * @see org.squashtest.tm.api.widget.access.OperationBuilder#build()
	 */
	@Override
	public AccessRule build() {
		return selection;
	}

	/**
	 * @see org.squashtest.tm.api.widget.NodeSelectionBuilder#anyNode()
	 */
	@Override
	public OperationBuilder anyNode() {
		selection.addRule(new AnyNode(Permission.ANY));
		return this;
	}

	/**
	 * @see org.squashtest.tm.api.widget.NodeSelectionBuilder#anyNode(org.squashtest.tm.api.security.acls.Permission)
	 */
	@Override
	public OperationBuilder anyNode(Permission permission) {
		selection.addRule(new AnyNode(permission));
		return this;
	}

}