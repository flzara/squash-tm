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
package org.squashtest.csp.core.bugtracker.core;

import org.squashtest.csp.core.bugtracker.net.AuthenticationCredentials;
import org.squashtest.tm.domain.servers.AuthenticationProtocol;
import org.squashtest.tm.domain.servers.BasicAuthenticationCredentials;
import org.squashtest.tm.domain.servers.Credentials;

public final class ConnectorUtils {
	private ConnectorUtils(){

	}

	public static boolean supports(AuthenticationProtocol[] supported, AuthenticationProtocol mode){
		for (AuthenticationProtocol mm : supported){
			if (mode == mm){
				return true;
			}
		}
		return false;
	}

	/**
	 * Convert a {@link BasicAuthenticationCredentials} to a {@link AuthenticationCredentials} for
	 * retrocompatibility purposes
	 */
	public static AuthenticationCredentials backportCredentials(Credentials credentials, AuthenticationProtocol[] supported){
		if (!supports(supported, AuthenticationProtocol.BASIC_AUTH)){
			throw new UnsupportedAuthenticationModeException(AuthenticationProtocol.BASIC_AUTH.toString());
		}

		if (!BasicAuthenticationCredentials.class.isAssignableFrom(credentials.getClass())){
			throw new UnsupportedAuthenticationModeException(credentials.getClass().getSimpleName());
		}

		BasicAuthenticationCredentials creds = (BasicAuthenticationCredentials) credentials;
		return new AuthenticationCredentials(creds.getUsername(), new String(creds.getPassword()));
	}

}
