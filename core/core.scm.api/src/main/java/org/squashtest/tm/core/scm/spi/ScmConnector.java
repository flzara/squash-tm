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
package org.squashtest.tm.core.scm.spi;

import org.squashtest.tm.domain.servers.AuthenticationProtocol;
import org.squashtest.tm.domain.servers.Credentials;

import java.io.IOException;

public interface ScmConnector {
	/**
	 * Tells whether this connector supports the given {@link AuthenticationProtocol}.
	 * @param protocol The authentication protocol.
	 * @return True if the given protocol is supported. False otherwise.
	 */
	boolean supports(AuthenticationProtocol protocol);

	/**
	 * Get an Array of the AuthenticationProtocols supported by this ScmConnector.
	 * @return An Array containing all the supported AuthenticationProtocols of this ScmServer
	 */
	AuthenticationProtocol[] getSupportedProtocols();

	/**
	 * Initializes the local Source Code Management repository.
	 * It at least implies the clone of the remote repository on the local files server if it does not exist yet.
	 * @param credentials The {@link Credentials} to authenticate to the remote repository
	 * @throws IOException If an error occurs during the process. The causes can be diverse, including:
	 * <ul>
	 *     <li> The connector can not reach the given remote server</li>
	 *     <li> Squash does not have the rights to write in the local repository path</li>
	 *     <li> Squash can not write in the repository path due to a concurrent process</li>
	 *     <li> The local repository path given to the connector exists but is not valid</li>
	 * </ul>
	 */
	void initRepository(Credentials credentials) throws IOException;

	/**
	 * Prepares the local Source Code Management repository.
	 * It sets the repository in a state in which it is ready to accept files creation and modifications,
	 * then commit and push without side effects.
	 * It can imply cleaning untracked files, reverting some remaining modifications, switching to the right branch,
	 * pulling the remote repository.
	 * @param credentials The {@link Credentials} to authenticate to the remote repository
	 * @throws IOException If an error occurs during the process. The causes can be diverse, including:
	 * <ul>
	 *     <li> The local repository path given to the connector does not exist or is not valid</li>
	 *     <li> Squash does not have the rights to write in the local repository path</li>
	 *     <li> Squash can not write in the repository path due to a concurrent process</li>
	 *     <li> The connector can not reach the given remote server</li>
	 * </ul>
	 */
	void prepareRepository(Credentials credentials) throws IOException;

	/**
	 * Synchronizes the local Source Code Management repository with the remote repository.
	 * Commits all the current files modifications contained in the local repository's working directory
	 * and pushes them to the remote repository.
	 * @param credentials The {@link Credentials} which will be used to authenticate to the remote repository.
	 * @throws IOException If an error occurs during the process. The causes can be divers, including:
	 * <ul>
	 *     <li> The local repository path given to the connector does not exist or is not valid</li>
	 *     <li> Squash does not have the rights to write in the local repository path</li>
	 *     <li> Squash can not write in the repository path due to a concurrent process</li>
	 *     <li> The connector can not reach the given remote server</li>
	 * </code>
	 */
	void synchronize(Credentials credentials) throws IOException;

}
