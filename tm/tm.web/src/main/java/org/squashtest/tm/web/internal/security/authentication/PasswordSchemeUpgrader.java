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
package org.squashtest.tm.web.internal.security.authentication;


import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.squashtest.tm.api.security.authentication.AuthenticationProviderFeatures;
import org.squashtest.tm.service.SecurityConfig;
import org.squashtest.tm.service.internal.security.AuthenticationProviderContext;
import org.squashtest.tm.service.internal.security.InternalAuthenticationProviderFeatures;
import org.squashtest.tm.service.security.AdministratorAuthenticationService;
import org.squashtest.tm.web.internal.annotation.ApplicationComponent;

import javax.inject.Inject;

/**
 *<p>
 * On user interactive authentication success, that event handler will upgrade a user password to the newest password scheme, if it uses an obsolete scheme.
 * </p>
 *
 * <p>
 *	The upgrade will happen if all the following conditions are true :
 *	<ol>
 *	   <li>the principal is a User</li>
 *	   <li>the user authenticated via the internal provider (ie the dao provider)</li>
 *	   <li>the current password hash doesn't use the correct format</li>
 *	   <li>the password is available in the clear</li>
 *	</ol>
 *
 * 	In case condition 3 is met but not condition 4, the error will be logged and no upgrade will be applied.
 * </p>
 *
 * @bsiri
 */
@ApplicationComponent
public class PasswordSchemeUpgrader implements ApplicationListener<AuthenticationSuccessEvent> {

	private static final Logger LOGGER = LoggerFactory.getLogger(PasswordSchemeUpgrader.class);

	// That string is built to match the format defined in org.springframework.security.crypto.password.DelegatingPasswordEncoder
	// The prefix and suffix '{' and '}' are private static string so I can't link them explicitly, let's hope this convention
	// won't change in the future.
	private static final String PASSWORD_HASH_PREFIX = "{"+ SecurityConfig.CURRENT_USER_PASSWORD_HASH_SCHEME+"}";

	@Inject
	private AuthenticationProviderContext authProviderContext;

	@Inject
	private AdministratorAuthenticationService authService;


	@Override
	public void onApplicationEvent(AuthenticationSuccessEvent event) {
		try{
			upgradePasswordIfRequired(event);
		} 
		catch(Exception exception){ //NOSONAR : failure of this operation should not prevent the users to access the application
			LOGGER.debug("unexpected error while checking for password scheme upgrade", exception);
		}
	}
	
	
	private void upgradePasswordIfRequired(AuthenticationSuccessEvent event) {

		Authentication authentication = event.getAuthentication();

		LOGGER.debug("upgrading password for user '{}' if needed", authentication.getName());

		User user = extractUserIfExists(authentication);

		// guard against violations of condition 1
		if (user == null){
			LOGGER.trace("the principal is not a user (weird), skipping");
			return;
		}

		AuthenticationProviderFeatures features = authProviderContext.getProviderFeatures(authentication);

		// guard against violations of condition 2
		// note : this test is stronger than checking for features.isManagedPassword()
		// we really need to ensure that the provider is the internal provider
		if (features != InternalAuthenticationProviderFeatures.INSTANCE){
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("user logged in using provider '{}', skipping upgrade", features.getProviderName());
			}
			return;
		}


		// test condition 3
		boolean needsUpgrade = doesRequireUpgrade(user);

		// test condition 4
		String clearPassword = extractClearPasswordIfExists(authentication);

		if (needsUpgrade){
			if (clearPassword != null){
				LOGGER.trace("password needs scheme upgrade -> upgrading");

				authService.resetUserPassword(user.getUsername(), clearPassword);

			}
			else{
				LOGGER.trace("password needs scheme upgrade but password is unavailable. It must be changed manually.");
			}
		}

	}

	// ********************** helpers ***************************************

	/*
		Returns the principal as a User if it is actually a User, or null if the principal
		is something else.
	 */
	private User extractUserIfExists(Authentication authentication){
		Object principal = authentication.getPrincipal();
		if (principal != null && User.class.isAssignableFrom(principal.getClass())){
			return (User)principal;
		}
		else{
			return null;
		}
	}

	/*
		Returns the credentials as String if applicable, or null otherwise
	 */
	private String extractClearPasswordIfExists(Authentication authentication){
		Object creds = authentication.getCredentials();
		if (creds != null && String.class.isAssignableFrom(creds.getClass())){
			LOGGER.trace("password found");
			return (String)creds;
		}
		else return null;
	}

	/*
		Returns true only if the password is non blank and is of the wrong format.
	 */
	private boolean doesRequireUpgrade(User user){
		String pwd = user.getPassword();
		if (!StringUtils.isBlank(pwd)){
			return ! pwd.startsWith(PASSWORD_HASH_PREFIX);
		}
		else{
			return false;
		}
	}

}
