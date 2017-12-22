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

import javax.validation.constraints.NotNull;

import org.squashtest.tm.api.security.acls.AccessRule;
import org.squashtest.tm.api.security.acls.Permission;
import org.squashtest.tm.api.widget.TreeNodeType;
import org.squashtest.tm.core.foundation.lang.Assert;

/**
 * Access rule meaning : some kind of node has to be selected, and user need a permission on this node.
 *
 * @author Gregory Fouquet
 *
 */
public class SelectedNodePermission implements AccessRule {
	private final TreeNodeType nodeType;
	private final Permission permission;

	public SelectedNodePermission(@NotNull TreeNodeType nodeType, @NotNull Permission permission) {
		super();
		this.nodeType = nodeType;
		this.permission = permission;
		Assert.parameterNotNull(nodeType, "nodeType");
		Assert.parameterNotNull(permission, "permission");
	}

	/**
	 * @return the nodeType
	 */
	public TreeNodeType getNodeType() {
		return nodeType;
	}

	/**
	 * @return the permission
	 */
	public Permission getPermission() {
		return permission;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() { // GENERATED:START
		final int prime = 31;
		int result = 7;
		result = prime * result + (getNodeType() == null ? 0 : getNodeType().hashCode());
		result = prime * result + (getPermission() == null ? 0 : getPermission().hashCode());
		return result;
	} // GENERATED:END

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) { // GENERATED:START
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		SelectedNodePermission other = (SelectedNodePermission) obj;
		if (getNodeType() != other.getNodeType()) {
			return false;
		}
		if (getPermission() != other.getPermission()) {
			return false;
		}
		return true;
	} // GENERATED:END

}
