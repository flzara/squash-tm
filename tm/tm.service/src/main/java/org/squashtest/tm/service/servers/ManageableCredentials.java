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

import org.squashtest.tm.domain.servers.AuthenticationProtocol;
import org.squashtest.tm.domain.servers.Credentials;
import org.squashtest.tm.domain.servers.ThirdPartyServer;

/**
 * <p>
 * 		Objects of this interfaces are not the credentials themselves, but allow for managing them.
 * </p>
 * <p>
 * 		Let alone the security concerns, the business of managing credentials goes beyond merely storing and retrieving
 * 		them. Some credentials are straightforward and some needs more complex operations to build. Some are cleared for
 * 		storage as application credentials, some are not. In short, there is a difference between the credentials consumed
 * 		by the other services and the information we need to manage.
 * </p>
 *
 * <p>
 *		In some cases the credentials alone would not suffice, and the server authentication configuration is required.
 *		To that end, the method that build the effective credentials {@link #build(StoredCredentialsManager, ThirdPartyServer, String)}
 *		receives extra parameters that allows for extra operations.
 * </p>
 */
public interface ManageableCredentials {

	/**
	 * See {@link Credentials#getImplementedProtocol()}
	 * @return
	 */
	AuthenticationProtocol getImplementedProtocol();

	/**
	 * States whether our business rules allow users to store this kind of credentials.
	 * Default is false.
	 *
	 * @return
	 */
	default boolean allowsUserLevelStorage(){
		return false;
	}

	/**
	 * States whether our business rules allow these credentials to be stored as application-level credentials.
	 * Default is false.
	 *
	 */
	default boolean allowsAppLevelStorage(){
		return false;
	}

	/**
	 * The factory method that produces the actual credentials, if the managed representation is not the definitive
	 * credentials itself. If credentials cannot be built for some reasons, will return null : the caller will decide
	 * if an exception must be thrown (eg BugTrackerNoCredentialsException) or else.
	 *
	 * @return
	 */
	Credentials build(StoredCredentialsManager storeManager, ThirdPartyServer server, String username);



}
