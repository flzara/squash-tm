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
package org.squashtest.tm.service.internal.servers;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.tm.core.foundation.exception.NullArgumentException;
import org.squashtest.tm.domain.servers.Credentials;
import org.squashtest.tm.security.UserContextHolder;
import org.squashtest.tm.service.servers.CredentialsProvider;
import org.squashtest.tm.service.servers.StoredCredentialsManager;
import org.squashtest.tm.service.servers.UserLiveCredentials;

import javax.inject.Inject;
import java.util.Optional;


/**
 * @see CredentialsProvider
 */
@Service("squashtest.tm.service.CredentialsProvider")
public class CredentialsProviderImpl implements CredentialsProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(CredentialsProviderImpl.class);

	@Inject
	private StoredCredentialsManager storedCredentialsManager;

	private final ThreadLocal<UserLiveCredentials> userLiveCredentials = new ThreadLocal<>();


	@Override
	public boolean hasCredentials(BugTracker server) {
		UserLiveCredentials liveCredentials = getLiveCredentials();
		return liveCredentials.hasCredentials(server);
	}

	@Override
	public Optional<Credentials> getCredentials(BugTracker server) {
		Credentials credentials = null;

		UserLiveCredentials liveCreds = getLiveCredentials();

		if (liveCreds.hasCredentials(server)){
			credentials = liveCreds.getCredentials(server);
		}
		else{
			credentials = storedCredentialsManager.findUserCredentials(server.getId(), liveCreds.getUser());
		}

		return (credentials != null) ? Optional.of(credentials) : Optional.empty();
	}

	@Override
	public void addToLiveCredentials(BugTracker server, Credentials credentials) {
		UserLiveCredentials liveCredentials = getLiveCredentials();
		liveCredentials.setCredentials(server, credentials);
	}



	@Override
	public void removeFromLiveCredentials(BugTracker server) {
		UserLiveCredentials liveCredentials = getLiveCredentials();
		liveCredentials.removeCredentials(server);
	}

	@Override
	public void restoreLiveCredentials(UserLiveCredentials credentials) {
		if (credentials == null) {
			throw new NullArgumentException("Cannot store null credentials");
		}
		LOGGER.debug("CredentialsProviderImpl : restoring live credentials for user '{}'", credentials.getUser());
		this.userLiveCredentials.set(credentials);
	}

	@Override
	public void clearLiveCredentials() {
		userLiveCredentials.remove();
	}

	@Override
	public UserLiveCredentials getLiveCredentials(){
		UserLiveCredentials liveCredentials = userLiveCredentials.get();

		if (liveCredentials == null){
			liveCredentials = createDefaultOrDie();
			userLiveCredentials.set(liveCredentials);
		}

		return liveCredentials;
	}

	/*
		Creates an empty instance of UserLiveCredentials, but a user context (ie a security context) is required for that.
		If none is available an exception will be thrown because this provider only deals with user credentials
		(not Squash-TM own credentials), indicating a programmatic error.
	 */
	private UserLiveCredentials createDefaultOrDie(){
		String username = UserContextHolder.getUsername();
		if (StringUtils.isBlank(username)){
			throw new IllegalStateException(
				"CredentialsProviderImpl : attempted to get live credentials for current user but " +
					"none were found. This is a programming error, which means that either there is no user context, or " +
					"that the thread was initiated in an illegal way (the live credentials were not loaded from the session)");
		}
		return new UserLiveCredentials(username);
	}
}
