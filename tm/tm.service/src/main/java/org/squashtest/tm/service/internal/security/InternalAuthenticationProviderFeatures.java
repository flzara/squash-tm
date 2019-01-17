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

import org.springframework.stereotype.Component;
import org.squashtest.tm.api.security.authentication.AuthenticationProviderFeatures;

/**
 * Features of Squash internal (db-based) authentication provider.
 * 
 * @author Gregory Fouquet
 * 
 */

// note : Spring will instantiate one singleton brean, and there is another one declared as static INSTANCE. Something is not square here... But I've no time to fix it yet. 
@Component
public class InternalAuthenticationProviderFeatures implements AuthenticationProviderFeatures {
	
	public static final String NAME = "internal";  

	// make class Singleton
	public static final InternalAuthenticationProviderFeatures INSTANCE = new InternalAuthenticationProviderFeatures();

	// private constructor
	private InternalAuthenticationProviderFeatures(){}

	/**
	 * @return false
	 * @see org.squashtest.tm.api.security.authentication.AuthenticationProviderFeatures#isManagedPassword()
	 */
	@Override
	public boolean isManagedPassword() {
		return false;
	}

	/**
	 * @return "internal"
	 * @see org.squashtest.tm.api.security.authentication.AuthenticationProviderFeatures#getProviderName()
	 */
	@Override
	public String getProviderName() {
		return InternalAuthenticationProviderFeatures.NAME;
	}

	/**
	 * @return false
	 * @see org.squashtest.tm.api.security.authentication.AuthenticationProviderFeatures#shouldCreateMissingUser()
	 */
	@Override
	public boolean shouldCreateMissingUser() {
		return false;
	}

}
