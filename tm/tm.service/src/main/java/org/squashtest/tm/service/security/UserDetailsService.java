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

import java.util.List;

import javax.validation.constraints.NotNull;

import org.springframework.security.core.GrantedAuthority;

/**
 * @author Gregory Fouquet
 *
 */
public interface UserDetailsService extends org.springframework.security.core.userdetails.UserDetailsService { // NOSONAR cant find a better name
	/**
	 * Loads authorities as the {@link #loadUserByUsername(String)} method would, but it does not check the
	 * authentication table beforehand.
	 * 
	 * @param username
	 * @return
	 */
	List<GrantedAuthority> loadAuthoritiesByUsername(@NotNull String username);
	
	
	/**
	 * Changes the user login (for authentication) from oldLogin to newLogin
	 * 
	 * @param newLogin
	 * @param oldLogin
	 */
	void changeUserLogin(String newLogin, String oldLogin);

}
