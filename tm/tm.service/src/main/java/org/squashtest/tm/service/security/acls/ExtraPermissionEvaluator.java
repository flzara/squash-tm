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
package org.squashtest.tm.service.security.acls;

import java.io.Serializable;

import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;

/**
 * For any other provider of permission evaluation. Some components freak out when more than one {@link PermissionEvaluator} exists in
 * the bean factory because we can't tell them to pick the one we want. This interface thus offers the same services while not being
 * a potential candidate for injection.
 * 
 * @author bsiri
 *
 */
public interface ExtraPermissionEvaluator {
	/**
	 *
	 * @param authentication represents the user in question. Should not be null.
	 * @param targetDomainObject the domain object for which permissions should be checked. May be null
	 *          in which case implementations should return false, as the null condition can be checked explicitly
	 *          in the expression.
	 * @param permission a representation of the permission object as supplied by the expression system. Not null.
	 * @return true if the permission is granted, false otherwise
	 */
	boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission);

	/**
	 * Alternative method for evaluating a permission where only the identifier of the target object
	 * is available, rather than the target instance itself.
	 *
	 * @param authentication represents the user in question. Should not be null.
	 * @param targetId the identifier for the object instance (usually a Long)
	 * @param targetType a String representing the target's type (usually a Java classname). Not null.
	 * @param permission a representation of the permission object as supplied by the expression system. Not null.
	 * @return true if the permission is granted, false otherwise
	 */
	boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission);
}
