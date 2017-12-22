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

import java.util.Collection;
import java.util.Map;

import javax.validation.constraints.NotNull;


/**
 * This service evaluates permissions of the current user.
 *
 *
 *
 * @author Gregory Fouquet
 *
 */
public interface PermissionEvaluationService {
	/**
	 * @param role
	 * @param permission
	 *            String representation of the permission.
	 * @param object
	 * @return true if the current user either has the given role or has the required permission on the given object.
	 */
	boolean hasRoleOrPermissionOnObject(String role, String permission, Object object);

	/**
	 *
	 * @param permission String representation of the permission.
	 * @param entity
	 * @return true if the current user has the required permission on the given object.
	 */
	boolean hasPermissionOnObject(String permission, Object entity);

	/**
	 * Same as {@link #hasRoleOrPermissionOnObject(String, String, Object)}, except that Object is explicitly identified
	 * by its ID and classname
	 *
	 * @param role
	 * @param permission
	 * @param entityId
	 * @param entityClassName
	 * @return
	 */
	boolean hasRoleOrPermissionOnObject(String role, String permission, Long entityId, String entityClassName);


	/**
	 * short hand for hasRoleOrPermissionOnObject('ROLE_ADMIN', 'READ', object);
	 *
	 * @param object
	 * @return
	 */
	boolean canRead(Object object);


	/**
	 * return true if the user has more than readonly on the object
	 * @param object
	 * @return
	 */
	boolean hasMoreThanRead(Object object);

	/**
	 * return true if the user has the given role.
	 * @param role
	 * @return
	 */
	boolean hasRole(String role);

	/**
	 * returns <code>true</code> if the current user has the given permission on the object of the given id and classname.
	 *
	 * @param permission
	 * @param entityId
	 * @param entityClassName
	 * @return <code>true</code> if the current user has the given permission on the object of the given id and classname.
	 */
	boolean hasPermissionOnObject(String permission, Long entityId, String entityClassName);

	/**
	 *
	 * @param permissions String representation of the permission.
	 * @param entity
	 * @return true if the current user has the required permission on the given object.
	 */
	Map<String, Boolean> hasRoleOrPermissionsOnObject(String role, String[] permissions, Object entity);

	/**
	 * returns the list of all permissions the curent user has on en entity refernced by its classname and identifier.
	 * admin role is checked and grants all permisisons.
	 *
	 * @param className  class name of the entity
	 * @param id identifier of the entity
	 * @return the set of permissions for the given entity.
	 */
	Collection<String> permissionsOn(@NotNull String className, long id);

}
