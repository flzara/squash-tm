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
import org.squashtest.tm.domain.servers.AuthenticationPolicy;
import org.squashtest.tm.domain.servers.Credentials;
import org.squashtest.tm.security.UserContextHolder;
import org.squashtest.tm.service.servers.CredentialsProvider;
import org.squashtest.tm.service.servers.StoredCredentialsManager;
import org.squashtest.tm.service.servers.UserCredentialsCache;

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

	private final ThreadLocal<UserCredentialsCache> threadedCache = new ThreadLocal<>();


	@Override
	public String currentUser() {
		UserCredentialsCache userCache = getCache();
		return userCache.getUser();
	}

	@Override
	public boolean hasCredentials(BugTracker server) {
		return getCredentials(server).isPresent();
	}

	@Override
	public boolean hasAppLevelCredentials(BugTracker server) {
		return getAppLevelCredentials(server).isPresent();
	}

	@Override
	public Optional<Credentials> getCredentials(BugTracker server) {
		LOGGER.debug("CredentialsProviderImpl : looking for credentials for server '{}' for current user", server.getName());

		Credentials credentials = getCredentialsFromCache(server);

		if (credentials == null){
			credentials = getUserCredentialsFromStore(server);
		}

		Optional<Credentials> option;
		if (credentials != null){
			LOGGER.debug("CredentialsProviderImpl : credentials found");
			option = Optional.of(credentials);
		}
		else{
			LOGGER.debug("CredentialsProviderImpl : credentials not found");
			option = Optional.empty();
		}

		return option;

	}

	@Override
	public Optional<Credentials> getAppLevelCredentials(BugTracker server) {

		LOGGER.debug("CredentialsProviderImpl : looking for app-level credentials for server '{}'", server.getName());

		Credentials credentials = getAppLevelCredentialsFromStore(server);

		Optional<Credentials> option;
		if (credentials != null){
			LOGGER.debug("CredentialsProviderImpl : credentials found");
			option = Optional.of(credentials);
		}
		else{
			LOGGER.debug("CredentialsProviderImpl : credentials not found");
			option = Optional.empty();
		}

		return option;
	}



	private Credentials getCredentialsFromCache(BugTracker server){
		UserCredentialsCache userCache = getCache();
		Credentials credentials = null;
		if (userCache.hasCredentials(server)){
			credentials = userCache.getCredentials(server);
			LOGGER.trace("CredentialsProviderImpl : found in cache");
		}
		return credentials;
	}

	private Credentials getUserCredentialsFromStore(BugTracker server){
		Credentials credentials = storedCredentialsManager.findUserCredentials(server.getId(), currentUser());
		if (credentials != null){
			LOGGER.trace("CredentialsProviderImpl : found in database");
		}
		return credentials;
	}

	private Credentials getAppLevelCredentialsFromStore(BugTracker server){
		Credentials credentials = storedCredentialsManager.unsecuredFindAppLevelCredentials(server.getId());
		if (credentials != null){
			LOGGER.trace("CredentialsProviderImpl : found in database");
		}
		return credentials;
	}



	// ************** cache management ******************


	@Override
	public void cacheCredentials(BugTracker server, Credentials credentials) {
		if (server.getAuthenticationPolicy() != AuthenticationPolicy.APP_LEVEL){
			LOGGER.debug("CredentialsProviderImpl : caching credentials for server '{}'", server.getName());
			UserCredentialsCache userCache = getCache();
			userCache.cacheIfAllowed(server, credentials);
		}
		else{
			LOGGER.debug("CredentialsProviderImpl : refused to cache application-level credentials");
		}
	}

	@Override
	public void uncacheCredentials(BugTracker server) {
		UserCredentialsCache userCache = getCache();
		userCache.uncache(server);
	}

	@Override
	public void restoreCache(UserCredentialsCache credentials) {
		if (credentials == null) {
			throw new NullArgumentException("Cannot store null credentials");
		}
		LOGGER.debug("CredentialsProviderImpl : restoring credentials cache for user '{}'", credentials.getUser());
		this.threadedCache.set(credentials);
	}

	@Override
	public void unloadCache() {
		UserCredentialsCache cache = threadedCache.get();
		if (cache != null) {
			LOGGER.debug("CredentialsProviderImpl : unloading credentials cache for user '{}'", cache.getUser() );
		}
		threadedCache.remove();
	}

	@Override
	public UserCredentialsCache getCache(){
		UserCredentialsCache userCache = threadedCache.get();

		if (userCache == null){
			LOGGER.trace("CredentialsProviderImpl : current user has no credentials cache (yet).");
			userCache = createDefaultOrDie();
			threadedCache.set(userCache);
		}

		return userCache;
	}

	/*
		Creates an empty instance of UserCredentialsCache, but a user context (ie a security context) is required for that.
		If none is available an exception will be thrown because this provider only deals with user credentials
		(not Squash-TM own credentials), indicating a programmatic error.
	 */
	private UserCredentialsCache createDefaultOrDie(){
		LOGGER.debug("CredentialsProviderImpl : attempting to create a default cache");
		String username = UserContextHolder.getUsername();
		if (StringUtils.isBlank(username)){
			throw new IllegalStateException(
				"CredentialsProviderImpl : attempted to get the credentials cache for current user but " +
					"none were found. This is a programming error, which means that either there is no user context, or " +
					"that the thread was initiated in an illegal way (the credentials cache were not loaded from the session)");
		}
		return new UserCredentialsCache(username);
	}
}
