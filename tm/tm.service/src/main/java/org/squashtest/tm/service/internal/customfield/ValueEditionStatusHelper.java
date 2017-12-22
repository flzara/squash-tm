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
package org.squashtest.tm.service.internal.customfield;

import javax.inject.Inject;

import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.service.security.PermissionEvaluationService;

/**
 * 
 * @author Gregory Fouquet
 * 
 */
abstract class ValueEditionStatusHelper implements ValueEditionStatusStrategy {

	@Inject
	private PermissionEvaluationService permissionEvaluator;

	/**
	 * 
	 */
	public ValueEditionStatusHelper() {
		super();
	}

	protected final boolean userHasPermission(long boundEntityId, BindableEntity bindableEntity) {
		boolean hasPermission = permissionEvaluator.hasRoleOrPermissionOnObject("ROLE_ADMIN", "WRITE", boundEntityId,
				bindableEntity.getReferencedClass().getName());
		return hasPermission;
	}

}