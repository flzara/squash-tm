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

import org.squashtest.tm.domain.servers.Credentials;

/**
 * <p>
 * That manager deals with the stored credentials for third party servers, most of the time for the usage of Squash itself.
 * The credentials are encrypted with AES 128, so that neither the JCE extension nor setting crypto.policy is required.
 * </p>
 * 
 * <p>
 * 	Unless stated otherwise, users of this service must have administrator privileges.
 * </p>
 *
 */
public interface StoredCredentialsManager {

	/**
	 * Tells whether the secret key used for encryption has been configured. Returns false if not, in which case the administrator
	 * should know about that and the problem fixed before any credential is stored. The user context for that operation needs not 
	 * to be that of an administrator.
	 *
	 * @return true if a secret key was provided
	 */
	boolean isSecretConfigured();

	/**
	 * Stores the given credentials for the given server. If the server already had a stored credential,
	 * the previous credentials will be replaced by the new ones (so this also serves as an update operation).
	 * @param serverId
	 * @param credentials
	 * @throws MissingEncryptionKeyException if no secret key was configured
	 */
	void storeCredentials(long serverId, Credentials credentials);


	/**
	 * Returns the stored credentials associated to a server.
	 *
	 *
	 * @param serverId
	 * @return the credentials or null if none are defined
	 * @throws EncryptionKeyChangedException if credentials exist but cannot be loaded because they were encrypted with
	 * 			a different key
	 * @throws MissingEncryptionKeyException if if no secret key was configured
	 */
	Credentials findCredentials(long serverId);
	
	
	/**
	 * Like {@link #findCredentials(long)} but left unsecured : no check will be performed on the authorizations of the 
	 * user context. For Squash internal use only.
	 * 
	 * @param serverId
	 * @return
	 */
	Credentials unsecuredFindCredentials(long serverId);


	/**
	 * Will remove the stored credentials of a server if there were one.
	 *
	 * @param serverId
	 */
	void deleteCredentials(long serverId);




}
