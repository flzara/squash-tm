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
package org.squashtest.tm.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @author Gregory Fouquet
 * 
 */
public final class UserContextHolder {
	private static final Logger LOGGER = LoggerFactory.getLogger(UserContextHolder.class);

	private UserContextHolder() {
		super();
	}

	private static SecurityContext getContext() {
		return SecurityContextHolder.getContext();
	}

	/**
	 * Returns the principal registered by the security manager for the current thread.
	 * 
	 * @return
	 */
	public static Authentication getPrincipal() {
		SecurityContext context = getContext();
		LOGGER.debug("Got authentication {}", context.getAuthentication());
		return context.getAuthentication();
	}

	/**
	 * Returns the username registered by the security manager for the current thread.
	 * Note : the username is extracted ffrom current Principal, which was built from the UserDetailManager.usersByUsernameQuery query.
	 * It should be the exact username, not the one input when logging in (which means it has the right case)
	 * 
	 * @return
	 */
	public static String getUsername() {
		Authentication principal = getPrincipal();
		return principal == null ? "" : principal.getName();
	}
}
