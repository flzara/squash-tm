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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.JdbcUserDetailsManager;

/**
 * @author Gregory Fouquet
 *
 */
public class SquashUserDetailsManagerImpl extends JdbcUserDetailsManager implements SquashUserDetailsManager {

	private static final String CHANGE_USER_LOGIN = "update AUTH_USER set LOGIN = ? where LOGIN = ?";

	/**
	 *
	 */
	public SquashUserDetailsManagerImpl() {
		super();
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return super.loadUserByUsername(username);
	}

	/* (non-Javadoc)
		 * @see org.squashtest.tm.service.internal.security.SquashUserDetailsManager#changeUserLogin(java.lang.String, java.lang.String)
		 */
	@Override
	public void changeUserLogin(String newLogin, String oldLogin) {
		getJdbcTemplate().update(CHANGE_USER_LOGIN, newLogin, oldLogin);
	}

	/**
	 *
	 * @see org.squashtest.tm.service.security.UserDetailsService#loadAuthoritiesByUsername(java.lang.String)
	 */
	@Override
	public List<GrantedAuthority> loadAuthoritiesByUsername(@NotNull String username) {
		Set<GrantedAuthority> dbAuthsSet = new HashSet<>();

		if (getEnableAuthorities()) {
			dbAuthsSet.addAll(loadUserAuthorities(username));
		}

		if (getEnableGroups()) {
			dbAuthsSet.addAll(loadGroupAuthorities(username));
		}

		List<GrantedAuthority> dbAuths = new ArrayList<>(dbAuthsSet);

		addCustomAuthorities(username, dbAuths);

		return dbAuths;
	}


}
