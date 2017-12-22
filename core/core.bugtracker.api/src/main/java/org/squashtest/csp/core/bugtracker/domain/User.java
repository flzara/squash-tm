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
package org.squashtest.csp.core.bugtracker.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.squashtest.tm.bugtracker.definition.RemoteUser;

/**
 * @author bsiri
 *
 */

public class User implements Identifiable<User>, RemoteUser {

	public static final User NO_USER = new User(Identifiable.DUMMY_ID, Identifiable.DUMMY_NAME);

	private String id;
	private String name;

	private List<Permission> permissions = new ArrayList<>();

	public User() {
		super();
	}

	public User(String id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	public List<Permission> getPermissions() {
		return permissions;
	}

	public void addUser(Permission permission) {
		this.permissions.add(permission);
	}

	public void addPermission(Permission permission) {
		permissions.add(permission);
	}

	public void addAllPermissions(Collection<Permission> permissions) {
		this.permissions.addAll(permissions);
	}

	public Permission findPermissionByName(String permissionName) {
		for (Permission permission : permissions) {
			if (permission.getName().equals(permissionName)) {
				return permission;
			}
		}
		return null;
	}

	public Permission findPermissionById(String userId) {
		for (Permission permission : permissions) {
			if (permission.getId().equals(userId)) {
				return permission;
			}
		}
		return null;
	}

	@Override
	public boolean isDummy() {
		return this.id.equals(NO_USER.id);
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	/** exists for the purpose of being javabean compliant */
	public void setDummy(Boolean dummy) {

	}

}
