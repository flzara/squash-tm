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

/**
 * Defines constants for authorization rules.
 *
 * @author Gregory Fouquet
 *
 */
public final class Authorizations {
	public static final String READ = "READ";

	public static final String ROLE_ADMIN = "ROLE_ADMIN";

	public static final String HAS_ROLE_ADMIN = "hasRole('ROLE_ADMIN')";

	public static final String OR_HAS_ROLE_ADMIN = " or hasRole('ROLE_ADMIN')";

	public static final String MILESTONE_FEAT_ENABLED = "@featureManager.isEnabled('MILESTONE')";

	public static final String HAS_ROLE_ADMIN_OR_PROJECT_MANAGER = "hasRole('ROLE_ADMIN') or hasRole('ROLE_TM_PROJECT_MANAGER')";

	public static final String OR_HAS_ROLE_ADMIN_OR_PROJECT_MANAGER = " or (hasRole('ROLE_ADMIN') or hasRole('ROLE_TM_PROJECT_MANAGER'))";

	private Authorizations() {
		super();
	}


}
