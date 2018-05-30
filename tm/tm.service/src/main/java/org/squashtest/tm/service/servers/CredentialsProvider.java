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
package org.squashtest.tm.service.servers;

import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.tm.domain.servers.Credentials;

import java.util.Optional;

/**
 * <p>
 * Retrieves the credentials of a given user for a server, and manage the {@link UserCredentialsCache}. It is NOT intended
 * to manage the persistent credentials (that is the job of {@link StoredCredentialsManager}. The services
 * here are <b>scoped to the user context</b> of the current execution flow : all operations implicitly target the current user.
 * The only exception is #getAppLevelCredentials(), which means (as the name implies) that Squash-TM own credentials themselves should be
 * retrieved. The caller of the method must ensure that it will not call cache operations when no user context is defined or
 * disappointing events will follow.
 * Since the user context is thread-based it means that the credentials cache must be set and cleaned up at careful times.
 * It also means that if there is no user context available this service will (hopefully) fail early.
 * </p>
 *
 * <p>
 *     Note : if no user context is available (eg when Squash-TM do things on its own) the credentials should be loaded using
 *     the lower-level service {@link StoredCredentialsManager}.
 * </p>
 *
 * <p>
 *     Historically this class was known as BugTrackerContextHolder but was repurposed as the credentials management
 *     became more advanced (live vs cold credentials etc).
 * </p>
 *
 * @author bsiri
 *
 */
public interface CredentialsProvider {

	/**
	 * @return the username of the current user. Should be invoked only if you know that
	 * there is indeed a user context.
	 */
	String currentUser();

	/**
	 * Checks whether the user has credentials for the given server.
	 *
	 * @param server
	 * @return
	 */
	boolean hasCredentials(BugTracker server);

	/**
	 * Checks whether Squash-TM owns credentials for the given server.
	 *
	 * @param server
	 * @return
	 */
	boolean hasAppLevelCredentials(BugTracker server);

	/**
	 * Retrieves the credentials of the current user for the given server, if any.
	 *
	 * @param server
	 * @return
	 */
	Optional<Credentials> getCredentials(BugTracker server);

	/**
	 * Retrieves Squash-TM own credentials for the given server, if any.
	 *
	 * @param server
	 * @return
	 */
	Optional<Credentials> getAppLevelCredentials(BugTracker server);


	/**
	 * Store the given credentials into the cache for the given bugtracker
	 * (and for the current user). The credentials will be cached only if
	 * the cache deems them cachable indeed (see documentation in {@link UserCredentialsCache}).
	 * That method should not be invoked to cache Squash-TM app level credentials. The credentials must not be null.
	 *
	 * @param server
	 * @param credentials
	 */
	void cacheCredentials(BugTracker server, Credentials credentials);


	/**
	 * Discard the credentials that were set for the given server and current user
	 * from the cache.
	 *
	 * @param server
	 */
	void uncacheCredentials(BugTracker server);


	// ******* UserCredentialsCache plumbing (infrastructure) **************

	/*
		hint: think of those operations as the management of a ThreadLocal<UserCredentialsCache>
	 */

	/**
	 * Reinstall the cache given in parameter as the current user's credentials cache
	 * ie the current thread context. Internal usage only.
	 *
	 * @param credentials
	 */
	void restoreCache(UserCredentialsCache credentials);


	/**
	 * Returns the credentials cache of the current user.
	 *
	 * @return
	 */
	UserCredentialsCache getCache();


	/**
	 *  Uninstall the cache and clears the thread context.
	 *
	 */
	void unloadCache();


}
