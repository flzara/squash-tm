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

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.squashtest.tm.api.security.authentication.AuthenticationProviderFeatures;
import org.squashtest.tm.api.security.authentication.FeaturesAwareAuthentication;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.exception.user.LoginAlreadyExistsException;
import org.squashtest.tm.service.user.AdministrationService;
import org.squashtest.tm.web.internal.annotation.ApplicationComponent;

/**
 * This class checks if an {@link User} matches the authenticated user. If not, creates this user.
 *
 * @author Gregory Fouquet
 *
 */
@ApplicationComponent
public class AuthenticatedMissingUserCreator implements ApplicationListener<AuthenticationSuccessEvent>, Ordered {
	private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticatedMissingUserCreator.class);

	/*@Value("#{authenticationProviderContext.currentProviderFeatures}")
	private AuthenticationProviderFeatures authenticationProviderFeatures;*/
	@Inject
	private AuthenticationProviderContext authProviderContext;

	@Inject
	private AdministrationService userAccountManager;

	/**
	 *
	 */
	public AuthenticatedMissingUserCreator() {
		super();
		LOGGER.info("created");
	}
	
	
	// we must ensure that the user exist 
	// before any other event listener that rely on its existence 
	// kick in
	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE +1;
	}


	/**
	 * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
	 */
	@Override
	public void onApplicationEvent(AuthenticationSuccessEvent event) {

		AuthenticationProviderFeatures features = authProviderContext.getProviderFeatures(event.getAuthentication());
		if (features.shouldCreateMissingUser()) {
			createMissingUser(event.getAuthentication());
		}
	}
	

	private void createMissingUser(Authentication principal) {
		LOGGER.debug("Will try to create user from principal if it does not exist");

		try {
			userAccountManager.checkLoginAvailability(principal.getName());
		} catch (LoginAlreadyExistsException ex) { // NOSONAR : this exception is part of the nominal use cas
			// user already exists -> bail out
			return;
		}

		LOGGER.info("Authenticated principal does not match any User, a new User will be created");
		createUserFromPrincipal(principal);
	}

	private void createUserFromPrincipal(Authentication principal) {
		try {
			userAccountManager.createUserFromLogin(principal.getName());

		} catch (LoginAlreadyExistsException e) {
			LOGGER.warn("Something went wrong while trying to create missing authenticated user", e);
		}
	}
}
