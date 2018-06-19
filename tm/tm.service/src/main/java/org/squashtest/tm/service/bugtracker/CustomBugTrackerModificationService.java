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
package org.squashtest.tm.service.bugtracker;

import static org.squashtest.tm.service.security.Authorizations.HAS_ROLE_ADMIN;

import org.springframework.security.access.prepost.PreAuthorize;
import org.squashtest.csp.core.bugtracker.core.BugTrackerRemoteException;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.tm.domain.servers.AuthenticationPolicy;
import org.squashtest.tm.domain.servers.AuthenticationProtocol;
import org.squashtest.tm.service.servers.ManageableCredentials;
import org.squashtest.tm.service.servers.ServerAuthConfiguration;
import org.squashtest.tm.service.servers.StoredCredentialsManager;


public interface CustomBugTrackerModificationService {

	@PreAuthorize(HAS_ROLE_ADMIN)
	void changeName(long bugtrackerId, String newName);


	//**** credential services, some being forwarded to StoredCredentialsManager ****
	

	/**
	 * Returns the authentication protocols supported by the underlying connector
	 *
	 * @param bugtracker
	 * @return
	 */
	@PreAuthorize(HAS_ROLE_ADMIN)
	AuthenticationProtocol[] getSupportedProtocols(BugTracker bugtracker);


	/**
	 * Changes the authentication policy for this server. Be warned that doing this 
	 * will automatically remove the app-level credentials (since they target the 
	 * former protocol).
	 * 
	 * @param bugtrackerId
	 * @param policy
	 */
	@PreAuthorize(HAS_ROLE_ADMIN)
	void changeAuthenticationPolicy(long bugtrackerId, AuthenticationPolicy policy);

	/**
	 * Changes the authentication protocol. Be warned that doing this will automatically 
	 * remove the authentication configuration and app-level credentials (since they target 
	 * the former protocol) 
	 * 
	 * @param bugtrackerId
	 * @param protocol
	 */
	@PreAuthorize(HAS_ROLE_ADMIN)
	void changeAuthenticationProtocol(long bugtrackerId, AuthenticationProtocol protocol);
	
	
	
	/**
	 * Says whether the StoredCredentials service is properly configured
	 *
	 *  @see StoredCredentialsManager#isSecretConfigured()
	 *
	 * @return
	 */
	@PreAuthorize(HAS_ROLE_ADMIN)
	boolean isCredentialsServiceAvailable();

	/**
	 *
	 * @see StoredCredentialsManager#storeAppLevelCredentials(long, ManageableCredentials)
	 * @param serverId
	 * @param credentials
	 */
	@PreAuthorize(HAS_ROLE_ADMIN)
	void storeCredentials(long serverId, ManageableCredentials credentials);

	
	/**
	 * 
	 * @see StoredCredentialsManager#storeAuthenticationInformation(long, ServerAuthConfiguration)A
	 * @param serverId
	 * @param conf
	 */
	@PreAuthorize(HAS_ROLE_ADMIN)
	void storeAuthConfiguration(long serverId, ServerAuthConfiguration conf);
	
	
	/**
	 *
	 * @see StoredCredentialsManager#findAppLevelCredentials(long)
	 * @param serverId
	 * @return
	 */
	@PreAuthorize(HAS_ROLE_ADMIN)
	ManageableCredentials findCredentials(long serverId);
	
	/**
	 * 
	 * @See {@link StoredCredentialsManager#findServerAuthConfiguration(long)
	 * @param serverId
	 * @return
	 */
	@PreAuthorize(HAS_ROLE_ADMIN)
	ServerAuthConfiguration findAuthConfiguration(long serverId);


	/**
	 * Tests whether the given credentials are valid for the given bugtracker.
	 * The method exits normally if the credentials are valid.
	 *
	 * @param bugtrackerId
	 * @param credentials
	 * @throws BugTrackerRemoteException in case of a problem.
	 * @throws BugTrackerNoCredentialsException if the credentials where rejected, or could not be built due to incomplete data
	 * @return
	 */
	@PreAuthorize(HAS_ROLE_ADMIN)
	void testCredentials(long bugtrackerId, ManageableCredentials credentials);


	/**
	 *
	 * @see StoredCredentialsManager#deleteAppLevelCredentials(long)
	 * @param serverId
	 */
	@PreAuthorize(HAS_ROLE_ADMIN)
	void deleteCredentials(long serverId);
	
	
	/**
	 * 
	 * @see StoredCredentialsManager#deleteServerAuthConfiguration(long)
	 * @param serverId
	 */
	@PreAuthorize(HAS_ROLE_ADMIN)
	void deleteAuthConfiguration(long serverId);

}
