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
package org.squashtest.tm.service.internal.security;

import org.squashtest.tm.api.security.authentication.AuthenticationProviderFeatures;

/**
 * Features of Squash default authentication provider.
 *
 * @author zyang
 *
 */

public class DefaultAuthenticationProviderFeatures implements AuthenticationProviderFeatures {

	public static final DefaultAuthenticationProviderFeatures INSTANCE = new DefaultAuthenticationProviderFeatures();

	private DefaultAuthenticationProviderFeatures(){}


	/**
	 * @return true
	 * @see AuthenticationProviderFeatures#isManagedPassword()
	 */
	@Override
	public boolean isManagedPassword() {
		return true;
	}

	/**
	 * @return "internal"
	 * @see AuthenticationProviderFeatures#getProviderName()
	 */
	@Override
	public String getProviderName() {
		return "default";
	}

	/**
	 * @return false
	 * @see AuthenticationProviderFeatures#shouldCreateMissingUser()
	 */
	@Override
	public boolean shouldCreateMissingUser() {
		return false;
	}

}
