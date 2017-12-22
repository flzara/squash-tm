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
package org.squashtest.tm.domain.servers;


import java.util.Arrays;

/*
 * <p>
 * Tech note : the use of a char[] instead of a String for passwords seems a common practice, provided that we actually
 * wipe the char[] after use (see for instance http://www.oracle.com/technetwork/java/seccodeguide-139067.html#2-3
 * and specifically  https://docs.oracle.com/javase/6/docs/technotes/guides/security/crypto/CryptoSpec.html#PBEEx)
 * </p>
 *
 * <p>
 * 	Currently this practice is not widely enforced throughout the application, so here this is really for show.
 * 	However if one day the in-memory password usage is actually implemented this bit is already done.
 * </p>
 */
public class BasicAuthenticationCredentials implements Credentials {
	private String username = "";
	private char[] password = new char[0];



	@Override
	public AuthenticationProtocol getImplementedProtocol() {
		return AuthenticationProtocol.BASIC_AUTH;
	}

	public BasicAuthenticationCredentials() {

	}

	public BasicAuthenticationCredentials(String login, char[] password) {
		super();
		this.username = login;
		this.password = password;
	}

	public String getUsername() {
		return username;
	}

	public char[] getPassword() {
		return password;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(char[] password) {
		this.password = password;
	}

	/**
	 * Should be invoked once the caller has no longer use for this credentials
	 */
	public void wipePassword(){
		//NOOP it make the bugtracker auto-connect loosing it's password...
		//We must find another way to protect the password
//		Arrays.fill(password, '\0');
	}
}
