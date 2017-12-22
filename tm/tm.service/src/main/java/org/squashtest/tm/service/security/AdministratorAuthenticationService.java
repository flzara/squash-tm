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

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 
 * 
 * Configuration should point to an LDAP service or an Squash ad-hoc service if no LDAP is available.
 * 
 * 
 * Security should ensure that only the current user or an ADMIN authority can actually perform the requested operation,
 * safe for the modification of a password (only the user may access it).
 * 
 * @author bsiri
 * 
 */
public interface AdministratorAuthenticationService extends UserAuthenticationService {

	void createNewUserPassword(String login, String plainTextPassword, boolean enabled, boolean accountNonExpired,
			boolean credentialsNonExpired, boolean accountNonLocked, Collection<GrantedAuthority> autorities);
	
	void createUser(UserDetails userDetails);

	void resetUserPassword(String login, String plainTextPassword);

	void deactivateAccount(String login);
	
	void activateAccount(String login);
	
	void deleteAccount(String login);

	/**
	 * 
	 * @param login
	 * @return true if there is authentication data for the given login.
	 */
	boolean userExists(String login);
}
