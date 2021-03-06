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

import org.squashtest.tm.domain.Identified;
import org.squashtest.tm.service.security.PermissionEvaluationService;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Stub PermissionEvaluationService which has no role and has permission on entities with an odd identifier
 * @author jsimon
 * @since 1.12.0
 */

public class StubPermissionOnOddEntitiesEvaluationService implements PermissionEvaluationService {

	Set<String> permissionsToRefuse = new HashSet<String>();

	public StubPermissionOnOddEntitiesEvaluationService() {
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
		return hasPermissionOnObject(permission, identified.getId(),  object.getClass().getName() );
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
		return false;
	}

	@Override
	public boolean hasPermissionOnObject(String permission, Long entityId, String entityClassName) {
		String permissionString = permission + entityClassName + entityId;
		if (permissionsToRefuse.contains(permissionString)) {
			return false;
		}

		//Simulate project manager for odd project only.
		if (entityId % 2 == 0){
			return false;
		}
		return true;
	}

	@Override
	public Map<String, Boolean> hasRoleOrPermissionsOnObject(String role, String[] permissions, Object entity) {
		return null;
	}

	@Override
	public Collection<String> permissionsOn(@NotNull String className, long id) {
		return null;
	}

}
