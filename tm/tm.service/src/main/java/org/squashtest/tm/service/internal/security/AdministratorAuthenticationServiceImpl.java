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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.UnauthorizedPasswordChange;
import org.squashtest.tm.security.UserContextHolder;
import org.squashtest.tm.service.security.AdministratorAuthenticationService;
import static org.squashtest.tm.api.security.acls.Roles.*;

@Component("squashtest.core.security.AdministratorAuthenticationService")
public class AdministratorAuthenticationServiceImpl implements AdministratorAuthenticationService {
	private static final Logger LOGGER = LoggerFactory.getLogger(AdministratorAuthenticationServiceImpl.class);

	@Inject
	@Named("squashtest.core.security.JdbcUserDetailsManager")
	private SquashUserDetailsManager userManager;

	@Inject
	@Lazy
	private PasswordEncoder encoder;
	
	@Inject
	private AuthenticationProviderContext authenticationProviderContext;

	public void setUserDetailsManager(SquashUserDetailsManager userManager) {
		this.userManager = userManager;
	}

	public void setPasswordEncoder(PasswordEncoder encoder) {
		this.encoder = encoder;
	}


	@Override
	public boolean canModifyUser() {
		return authenticationProviderContext.isInternalProviderEnabled();
	}

	@Override
	public void changeAuthenticatedUserPassword(String plainOldPassword, String clearNewPassword) {
		String encNewPasswd = encode(clearNewPassword);
		userManager.changePassword(plainOldPassword, encNewPasswd);
	}
	

	@Override
	public boolean hasAuthenticatedUserLocalPassword() {
		UserDetails user = userManager.loadUserByUsername(UserContextHolder.getUsername());
		return ! StringUtils.isBlank(user.getPassword());
	}

	private String encode(String plainNewPassword) {
		return encoder.encode(plainNewPassword);
	}

	@Override
	public void createNewUserPassword(String login, String plainTextPassword, boolean enabled,
	                                  boolean accountNonExpired, boolean credentialsNonExpired, boolean accountNonLocked,
	                                  Collection<GrantedAuthority> autorities) {

		String encodedPassword = encode(plainTextPassword);

		UserDetails user = new User(login, encodedPassword, enabled, accountNonExpired, credentialsNonExpired,
			accountNonLocked, autorities);
		userManager.createUser(user);

	}

	

	@Override
	public void resetAuthenticatedUserPassword(String clearNewPassword) {
		String login = UserContextHolder.getUsername();
		resetUserPassword(login, clearNewPassword);
		
	}
	
	@Override
	public void resetUserPassword(String login, String clearNewPassword) {
		if (! canModifyUser()) {
			throw new UnauthorizedPasswordChange(
					"The authentication service do not allow users to change their passwords using Squash");
		}
		UserDetails user = userManager.loadUserByUsername(login);
		String encodedPassword = encode(clearNewPassword);
		UserDetails updateCommand = new User(login, encodedPassword, user.isEnabled(), true, true, true,
			user.getAuthorities());
		LOGGER.debug("reset password for user {}", login);
		userManager.updateUser(updateCommand);

	}
	

	@Override
	public void changeUserlogin(String newLogin, String oldLogin) {
		userManager.changeUserLogin(newLogin, oldLogin);
	}

	@Override
	public void deactivateAccount(String login) {
		if (userManager.userExists(login)) {
			UserDetails oldUser = userManager.loadUserByUsername(login);
			Set<GrantedAuthority> filteredAuthorities = filterAuthorities(oldUser.getAuthorities());
			UserDetails newUser = new User(login, oldUser.getPassword(), false, oldUser.isAccountNonExpired(),
				oldUser.isCredentialsNonExpired(), oldUser.isAccountNonLocked(), filteredAuthorities);
			LOGGER.debug("Deactivate account for user {}", login);
			userManager.updateUser(newUser);

		} else {
			LOGGER.trace("User {} has no authentidation data, it can't be deactivated", login);
		}
	}

	@Override
	public void activateAccount(String login) {
		if (userManager.userExists(login)) {
			UserDetails oldUser = userManager.loadUserByUsername(login);
			Set<GrantedAuthority> filteredAuthorities = filterAuthorities(oldUser.getAuthorities());
			UserDetails newUser = new User(login, oldUser.getPassword(), true, oldUser.isAccountNonExpired(),
				oldUser.isCredentialsNonExpired(), oldUser.isAccountNonLocked(), filteredAuthorities);
			LOGGER.debug("Activating account for user {}", login);
			userManager.updateUser(newUser);

		} else {
			LOGGER.trace("User {} has no authentidation data, it can't be activated", login);
		}
	}

	@Override
	public void deleteAccount(String login) {
		if (userManager.userExists(login)) {
			userManager.deleteUser(login);
		} else {
			LOGGER.trace("User {} has no authentidation data, it can't be deleted", login);
		}

	}

	private Set<GrantedAuthority> filterAuthorities(Collection<? extends GrantedAuthority> authorities){
		/*[Issue 6749] : We have to filter the user's authorities while activating/deactivating his account, otherwise
		 undesired authorities are added to the "core_party_authority" table*/
		Set<GrantedAuthority> filteredAuthorities = new HashSet<>();
		for (GrantedAuthority authority: authorities) {
			if(authority.toString().equals(ROLE_TM_PROJECT_MANAGER)){
				filteredAuthorities.add(new SimpleGrantedAuthority(ROLE_TM_PROJECT_MANAGER));
			}
		}
		return filteredAuthorities;
	}

	/**
	 * @see org.squashtest.tm.service.security.AdministratorAuthenticationService#userExists(java.lang.String)
	 */
	@Override
	public boolean userExists(String login) {
		return userManager.userExists(login);
	}

	/**
	 * @see org.squashtest.tm.service.security.AdministratorAuthenticationService#createUser(org.springframework.security.core.userdetails.UserDetails)
	 */
	@Override
	public void createUser(UserDetails plaintextPasswordUser) {
		String encodedPassword = encode(plaintextPasswordUser.getPassword());

		UserDetails user = UserBuilder.duplicate(plaintextPasswordUser).password(encodedPassword).build();
		userManager.createUser(user);
	}

}
