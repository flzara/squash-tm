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
import java.util.Collection;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Builds {@link User} objects without having to use the ugly, 10-parameters constructor
 *
 * @author Gregory Fouquet
 *
 */
public final class UserBuilder {
	private String login;
	private String password;
	private boolean enabled;
	private Boolean accountNonExpired;
	private Boolean credentialsNonExpired;
	private Boolean accountNonLocked;
	private Collection<? extends GrantedAuthority> authorities = new ArrayList<>(0);

	/**
	 *
	 */
	private UserBuilder() {
		super();
	}

	public static UserBuilder forUser(@NotBlank String login) {
		UserBuilder builder = new UserBuilder();
		builder.login = login;
		return builder;
	}

	public static UserBuilder duplicate(@NotNull UserDetails user) {
		UserBuilder builder = new UserBuilder();
		builder.login = user.getUsername();
		builder.password = user.getPassword();
		builder.authorities = user.getAuthorities();
		builder.enabled = user.isEnabled();
		builder.accountNonExpired = user.isAccountNonExpired();
		builder.accountNonLocked = user.isAccountNonLocked();
		builder.credentialsNonExpired = user.isCredentialsNonExpired();

		return builder;
	}

	public UserBuilder password(@NotBlank String password) {
		this.password = password;
		return this;
	}

	public UserDetails build() {
		return new User(login, password, enabled, valueOrDefault(accountNonExpired),
				valueOrDefault(credentialsNonExpired), valueOrDefault(accountNonLocked), authorities);
	}

	/**
	 * @param potentialValue
	 * @return
	 */
	private boolean valueOrDefault(Boolean potentialValue) {
		return potentialValue == null ? enabled : potentialValue;
	}

	/**
	 * @param active
	 * @return
	 */
	public UserBuilder active(@NotNull boolean active) {
		enabled = active;
		return this;
	}
}
