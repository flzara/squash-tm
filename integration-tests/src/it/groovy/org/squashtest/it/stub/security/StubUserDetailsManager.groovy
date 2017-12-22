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
package org.squashtest.it.stub.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component

import java.util.List;

import javax.validation.constraints.NotNull;

import org.springframework.dao.DataAccessException
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.squashtest.tm.service.internal.security.SquashUserDetailsManager;

/**
 * Not @Component because it has to be marked as @Primary
 */
class StubUserDetailsManager implements SquashUserDetailsManager {

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
		return {} as UserDetails;
	}

	@Override
	public void createUser(UserDetails user) {

	}

	@Override
	public void updateUser(UserDetails user) {

	}

	@Override
	public void deleteUser(String username) {

	}

	@Override
	public void changePassword(String oldPassword, String newPassword) {

	}

	@Override
	public boolean userExists(String username) {
		return true;
	}
	
	@Override
	public void changeUserLogin(String newLogin, String oldLogin) {
		
	}

	@Override
	public List<GrantedAuthority> loadAuthoritiesByUsername(
			@NotNull String username) {
		return Collections.emptyList
	}

}
