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
package org.squashtest.tm.service.thirdpartyserver;

import org.squashtest.tm.domain.servers.AuthenticationPolicy;
import org.squashtest.tm.domain.servers.AuthenticationProtocol;
import org.squashtest.tm.service.servers.ManageableCredentials;
import org.squashtest.tm.service.servers.ServerAuthConfiguration;
import org.squashtest.tm.service.servers.StoredCredentialsManager;

public interface ThirdPartyServerCredentialsService {

	/**
	 * Says whether the StoredCredentials service is properly configured
	 *
	 *  @see StoredCredentialsManager#isSecretConfigured()
	 *
	 * @return
	 */
	boolean isCredentialsServiceAvailable();

	/**
	 *
	 * @see StoredCredentialsManager#storeAppLevelCredentials(long, ManageableCredentials)
	 * @param serverId
	 * @param credentials
	 */
	void storeCredentials(long serverId, ManageableCredentials credentials);


	/**
	 *
	 * @see StoredCredentialsManager#storeServerAuthConfiguration(long, ServerAuthConfiguration)
	 * @param serverId
	 * @param conf
	 */
	void storeAuthConfiguration(long serverId, ServerAuthConfiguration conf);


	/**
	 *
	 * @see StoredCredentialsManager#findAppLevelCredentials(long)
	 * @param serverId
	 * @return
	 */
	ManageableCredentials findCredentials(long serverId);

	/**
	 *
	 * @See {@link StoredCredentialsManager#findServerAuthConfiguration(long)
	 * @param serverId
	 * @return
	 */
	ServerAuthConfiguration findAuthConfiguration(long serverId);

	/**
	 *
	 * @see StoredCredentialsManager#deleteAppLevelCredentials(long)
	 * @param serverId
	 */
	void deleteCredentials(long serverId);


	/**
	 *
	 * @see StoredCredentialsManager#deleteServerAuthConfiguration(long)
	 * @param serverId
	 */
	void deleteAuthConfiguration(long serverId);

	/**
	 * Changes the authentication policy for this server. If the chosen policy is
	 * APP_LEVEL be sure to {@link #storeCredentials(long, ManageableCredentials)} too.
	 *
	 * @param serverId
	 * @param policy
	 */
	void changeAuthenticationPolicy(long serverId, AuthenticationPolicy policy);


	/**
	 * Changes the authentication protocol. Be warned that doing this will automatically
	 * remove the authentication configuration and app-level credentials (since they target
	 * the former protocol)
	 *
	 * @param serverId
	 * @param protocol
	 */
	void changeAuthenticationProtocol(long serverId, AuthenticationProtocol protocol);
}
