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
import org.squashtest.tm.core.foundation.lang.Assert;

/**
 * @author Gregory Fouquet
 *
 */
public class AnyNode implements AccessRule {
	private final Permission permission;

	public AnyNode(@NotNull Permission permission) {
		super();
		this.permission = permission;
		Assert.parameterNotNull(permission, "permission");
	}

	/**
	 * @return the permission
	 */
	public Permission getPermission() {
		return permission;
	}
}
