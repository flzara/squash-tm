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
package org.squashtest.it.stub.security;

import java.util.*;

import org.hibernate.Hibernate;
import org.squashtest.tm.domain.Identified;
import org.squashtest.tm.service.security.PermissionEvaluationService;

import javax.validation.constraints.NotNull;


public class StubPermissionEvaluationService implements PermissionEvaluationService {

	Set<String> permissionsToRefuse = new HashSet<String>();

	public StubPermissionEvaluationService() {
		super();
	}

	public void emptyPermissionsToRefuse() {
		permissionsToRefuse.clear();
	}

	public void addPermissionToRefuse(String permission, String className, long id) {
		String permissionString = permission + className + id;
		permissionsToRefuse.add(permissionString);
	}

	@Override
	public boolean hasRoleOrPermissionOnObject(String role, String permission, Object object) {
		Identified identified = (Identified) object;
		return hasPermissionOnObject(permission, identified.getId(), Hibernate.getClass(object).getName());
	}

	@Override
	public boolean hasPermissionOnObject(String permission, Object entity) {
		return false;
	}

	@Override
	public boolean canRead(Object object) {
		return true;
	}

	@Override
	public boolean hasMoreThanRead(Object object) {
		return true;
	}

	@Override
	public boolean hasRoleOrPermissionOnObject(String role, String permission, Long entityId, String entityClassName) {
		return hasPermissionOnObject(permission, entityId, entityClassName);
	}

	@Override
	public boolean hasRole(String role) {
		return true;
	}

	@Override
	public boolean hasPermissionOnObject(String permission, Long entityId, String entityClassName) {
		String permissionString = permission + entityClassName + entityId;
		if (permissionsToRefuse.contains(permissionString)) {
			return false;
		}
		return true;
	}

	@Override
	public Map<String, Boolean> hasRoleOrPermissionsOnObject(String role, String[] permissions, Object entity) {
		Map<String, Boolean> res = new HashMap<String, Boolean>();
		for (String perm : permissions) {
			res.put(perm, hasRoleOrPermissionOnObject(role, perm, entity));
		}
		return res;
	}

	@Override
	public Collection<String> permissionsOn(@NotNull String className, long id) {
		return null;
	}
}
