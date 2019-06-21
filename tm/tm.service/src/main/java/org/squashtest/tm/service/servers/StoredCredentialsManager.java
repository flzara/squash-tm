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
import org.squashtest.csp.core.bugtracker.domain.User;
import org.squashtest.tm.domain.servers.Credentials;
import org.squashtest.tm.domain.servers.StoredCredentials;

/**
 * <p>
 * 		That manager deals with the stored credentials for third party servers, most of the time for the usage of Squash itself.
 * 		The credentials are encrypted with AES 128, so that neither the JCE extension nor setting crypto.policy is required.
 * </p>
 *
 * <p>
 *     Note that this service is INTENDED FOR MANAGEMENT ONLY. If you need to read credentials for consumption, use {@link CredentialsProvider}
 *     instead. This service deals with the manageable form of credentials, aka a {@link ManageableCredentials}, which can be different
 *     from the credentials actually used (eg OAuth1aCredentials must be recomposed from Server-owned data + user-specific tokens).
 *     As a management service, this class only deals with manageable representations. {@link CredentialsProvider} provides
 *     with usable representations.
 * </p>
 */
public interface StoredCredentialsManager {

	/**
	 * Tells whether the secret key used for encryption has been configured. Returns false if not, in which case the administrator
	 * should know about that and the problem fixed before any credential is stored. The user context for that operation needs not
	 * to be that of an administrator.
	 *
	 * @return true if a secret key is available.
	 */
	boolean isSecretConfigured();

	// ****************** User credentials management *************************

	/**
	 * Stores the given credentials for the given user. If the server already had a stored credential,
	 * the previous credentials will be replaced by the new ones (so this also serves as an update operation).
	 * Credentials that cannot be saved as user credentials will throw an exception : the invoker should check
	 * if the operation is legal first (see {@link ManageableCredentials#allowsUserLevelStorage()})
	 * @param serverId
	 * @param username
	 * @param credentials
	 * @throws IllegalArgumentException if the credentials cannot be stored as user-level credentials
	 * @throws MissingEncryptionKeyException if no secret key was configured
	 */
	void storeUserCredentials(long serverId, String username, ManageableCredentials credentials);

	/**
	 * Returns the stored credentials associated to a server for the given user, or null if none is defined.
	 *
	 * @param serverId
	 * @param username
	 * @return the credentials or null if none are defined
	 * @throws EncryptionKeyChangedException if credentials exist but cannot be loaded because they were encrypted with
	 * 			a different key
	 * @throws MissingEncryptionKeyException if if no secret key was configured
	 */
	ManageableCredentials findUserCredentials(long serverId, String username);

	/**
	 * Returns the stored credentials associated to a server for the current user, or null if none is defined.
	 *
	 * @param serverId
	 * @return the credentials or null if none are defined
	 * @throws EncryptionKeyChangedException if credentials exist but cannot be loaded because they were encrypted with
	 * 			a different key
	 * @throws MissingEncryptionKeyException if if no secret key was configured
	 */
	ManageableCredentials findCurrentUserCredentials(long serverId);


	/**
	 * Like {@link #findUserCredentials(long, String)}, but left unsecured : no check will be performed on the authorizations of the
	 * user context. Useful when no user context is available in the thread (eg Squash-TM initiated the request on its own).
	 *
	 * @param serverId
	 * @param username
	 * @return
	 */
	ManageableCredentials unsecuredFindUserCredentials(long serverId, String username);


	/**
	 * Will remove the stored credentials of a server and given user if there were one.
	 *
	 * @param serverId
	 * @param username
	 */
	void deleteUserCredentials(long serverId, String username);


	// ****************** Application-level credentials management *******************

	/**
	 * Stores the given credentials for the given server as application-level credentials (ie Squash-TM own credentials).
	 * If the server already had a stored credential, the previous credentials will be replaced by the new ones
	 * (so this also serves as an update operation).
	 * Credentials that cannot be saved as server credentials will throw an exception: the invoker should check
	 * if the operation is legal first (see {@link ManageableCredentials#allowsAppLevelStorage()}).
	 *
	 * @param serverId
	 * @param credentials
	 * @throws IllegalArgumentException if the credentials cannot be stored as user-level credentials
	 * @throws MissingEncryptionKeyException if no secret key was configured
	 */
	void storeAppLevelCredentials(long serverId, ManageableCredentials credentials);


	/**
	 * Returns Squash-TM own stored credentials associated to a server, or null if none is defined.
	 *
	 * @param serverId
	 * @return the credentials or null if none are defined
	 * @throws EncryptionKeyChangedException if credentials exist but cannot be loaded because they were encrypted with
	 * 			a different key
	 * @throws MissingEncryptionKeyException if if no secret key was configured
	 */
	ManageableCredentials findAppLevelCredentials(long serverId);


	/**
	 * Like {@link #findAppLevelCredentials(long)} but left unsecured : no check will be performed on the authorizations of the
	 * user context. For Squash internal use only.
	 *
	 * @param serverId
	 * @return
	 */
	ManageableCredentials unsecuredFindAppLevelCredentials(long serverId);



	/**
	 * Will remove Squash-TM own stored credentials of a server if there were one.
	 *
	 * @param serverId
	 */
	void deleteAppLevelCredentials(long serverId);


	// ********************** server auth configuration **************

	/**
	 * Stores the given configuration for the given server. By nature only Squash-TM hold that information
	 * (unlike credentials, which can be owned by users too), and can be defined by administrators only.
	 * If there was already a configuration for that server, the new configuration will replace
	 * the former configuration (so this also serves as an update operation).
	 *
	 * @param serverId
	 * @param conf
	 * @throws MissingEncryptionKeyException if no secret key was configured
	 */
	void storeServerAuthConfiguration(long serverId, ServerAuthConfiguration conf);

	/**
	 * Retrieves the authentication configuration for a given server.
	 *
	 * @param serverId
	 * @return the configuration, or null if none is defined.
	 * @throws EncryptionKeyChangedException if credentials exist but cannot be loaded because they were encrypted with
	 * 			a different key
	 * @throws MissingEncryptionKeyException if if no secret key was configured
	 */
	ServerAuthConfiguration findServerAuthConfiguration(long serverId);


	/**
	 * Similar to {@link #findServerAuthConfiguration(long)}, without security checks. For Squash-TM internal
	 * usage only.
	 *
	 * @param serverId
	 * @return
	 */
	ServerAuthConfiguration unsecuredFindServerAuthConfiguration(long serverId);


	/**
	 * Will remove the given configuration from the database.
	 */
	void deleteServerAuthConfiguration(long serverId);

	// ********************** deprecated *****************************

	/**
	 * Returns the credentials in usable form directly.
	 *
	 * @param serverId
	 * @deprecated Should call {@link CredentialsProvider#getAppLevelCredentials(BugTracker)} instead.
	 * @return
	 */
	@Deprecated
	Credentials unsecuredFindCredentials(long serverId);


}
