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
package org.squashtest.tm.domain.users;

import org.squashtest.tm.security.acls.PermissionGroup;

/**
 * This class represents a user and an aggregation of permissions (read, write and so on) which can be given to this
 * user and which have a scope of object identities.
 * 
 * This class is used to populate a permission table for given project
 * 
 * @author mpagnon
 * 
 */
public class UserProjectPermissionsBean {

	private User user;
	private PermissionGroup permissionGroup;

	public UserProjectPermissionsBean(User user, PermissionGroup permissionGroup) {
		this.user = user;
		this.permissionGroup = permissionGroup;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public PermissionGroup getPermissionGroup() {
		return permissionGroup;
	}

	public void setPermissionGroup(PermissionGroup permissionGroup) {
		this.permissionGroup = permissionGroup;
	}
}
