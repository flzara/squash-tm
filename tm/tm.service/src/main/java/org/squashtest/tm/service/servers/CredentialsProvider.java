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
 * Retrieves the credentials of a given user for a server, and manage the {@link UserLiveCredentials}. It is NOT intended
 * to manage the persistent credentials (that is the job of {@link StoredCredentialsManager}. The services
 * here are <b></b>scoped to the user context</b> of the current execution flow : all operations implicitly target the current user.
 * Since the user context is thread-based it means that the live credentials must be set and cleaned up at careful times.
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
	 * Checks whether the user has credentials for the given server.
	 *
	 * @param server
	 * @return
	 */
	boolean hasCredentials(BugTracker server);

	/**
	 * Retrieves the credentials of the current user for the given server, if any.
	 *
	 * @param server
	 * @return
	 */
	Optional<Credentials> getCredentials(BugTracker server);


	/**
	 * Sets the current credentials for the current user and for the given server.
	 * The credentials must not be null.
	 *
	 * @param server
	 * @param credentials
	 */
	void addToLiveCredentials(BugTracker server, Credentials credentials);


	/**
	 * Discard the credentials set for that server.
	 * 
	 * @param server
	 */
	void removeFromLiveCredentials(BugTracker server);


	// ******* LiveCredentials plumbing (infrastructure) **************

	/**
	 * Restore the current uses's LiveCredentials. Internal usage only.
	 *
	 *
	 * @param credentials
	 */
	void restoreLiveCredentials(UserLiveCredentials credentials);


	/**
	 * Returns the live credentials of the current user.
	 *
	 * @return
	 */
	UserLiveCredentials getLiveCredentials();


	/**
	 *  Clears the context for the current user. Internal usage only.
	 *
	 */
	void clearLiveCredentials();


}
