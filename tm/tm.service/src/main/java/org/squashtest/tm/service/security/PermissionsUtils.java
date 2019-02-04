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
package org.squashtest.tm.service.security;

import org.springframework.security.access.AccessDeniedException;

import java.util.Collection;
import java.util.List;

public final class PermissionsUtils {

	private static final String ROLE_ADMIN = "ROLE_ADMIN";

	private static final String ACCESS_IS_DENIED = "Access is denied";

	private PermissionsUtils() {
		super();
	}

	/**
	 * Will check if the current user has sufficient rights on the given checkable objects. If not will throw an
	 * {@link AccessDeniedException}.
	 *
	 * @throws AccessDeniedException
	 * @param permissionService
	 *            : the {@link PermissionEvaluationService} to use to do the check
	 * @param checkableObjects
	 *            : the {@link SecurityCheckableObject}s to check
	 */
	public static void checkPermission(PermissionEvaluationService permissionService,
			SecurityCheckableObject... checkableObjects) {
		for (SecurityCheckableObject object : checkableObjects) {
			if (!permissionService
					.hasRoleOrPermissionOnObject(ROLE_ADMIN, object.getPermission(), object.getObject())) {
				throw new AccessDeniedException(ACCESS_IS_DENIED);

			}
		}
	}

	/**
	 * Wil check if the current user has sufficient rights on the entities of the given ids and classname. If not, will
	 * throw an {@link AccessDeniedException}
	 *
	 * @throws AccessDeniedException
	 * @param permissionService
	 *            : the {@link PermissionEvaluationService} to use to do the check
	 * @param ids
	 *            : the ids of the entities to check the permissions on
	 * @param permission
	 *            : the permission name to check
	 * @param entityClassName
	 *            : the classname of the entities to check
	 */
	public static void checkPermission(PermissionEvaluationService permissionService, List<Long> ids,
			String permission, String entityClassName) {
		if (permissionService.hasRole(ROLE_ADMIN)) {
			return;
		}
		for (Long entityId : ids) {
			if (!permissionService.hasPermissionOnObject(permission, entityId, entityClassName)) {
				throw new AccessDeniedException(ACCESS_IS_DENIED);

			}
		}
	}

	public static void checkPermission(PermissionEvaluationService permissionService, Collection<?> toCheck, String permission){

		for (Object o : toCheck){
			if (!permissionService.hasRoleOrPermissionOnObject(ROLE_ADMIN, permission, o)){
				throw new AccessDeniedException(ACCESS_IS_DENIED);
			}
		}
	}
}
