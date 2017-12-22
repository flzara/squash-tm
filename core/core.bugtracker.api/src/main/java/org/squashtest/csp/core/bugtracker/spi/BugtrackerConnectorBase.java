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
package org.squashtest.csp.core.bugtracker.spi;

import org.squashtest.csp.core.bugtracker.core.BugTrackerNoCredentialsException;
import org.squashtest.csp.core.bugtracker.core.BugTrackerRemoteException;
import org.squashtest.csp.core.bugtracker.core.ConnectorUtils;
import org.squashtest.csp.core.bugtracker.core.UnsupportedAuthenticationModeException;
import org.squashtest.csp.core.bugtracker.net.AuthenticationCredentials;
import org.squashtest.tm.domain.servers.AuthenticationProtocol;
import org.squashtest.tm.domain.servers.Credentials;


/**
 * Base interface for all three kinds of connector (namely {@link BugTrackerConnector}, {@link AdvancedBugTrackerConnector}
 * and {@link OslcBugTrackerConnector}).
 *
 * The purpose of this interface is mainly to factor in one place the common features of these connectors rather than
 * for class design concerns. This is sort of a bad design but the alternative would be code copy pasta in several places.
 */
public interface BugtrackerConnectorBase{

	/**
	 * Declares which authentication protocols are supported by this BugTrackerConnector.
	 * Default implementation returns [{@link AuthenticationProtocol#BASIC_AUTH}]
	 *
	 * @return
	 */
	default AuthenticationProtocol[] getSupportedAuthProtocols(){
		return new AuthenticationProtocol[]{AuthenticationProtocol.BASIC_AUTH};
	}

	/**
	 * Declares whether the given connector supports a given connection protocol.
	 *
	 * @param mode
	 */
	default boolean supports(AuthenticationProtocol mode){
		return ConnectorUtils.supports(this.getSupportedAuthProtocols(), mode);
	}


	/**
	 * Authenticates to the bug tracker with the given credentials. If authentication does not fail, it should not be
	 * required again at least for the current thread.
	 *
	 * Default implementation delegates to the deprecated {@link #authenticate(AuthenticationCredentials)}
	 * if the connector supports the BASIC_AUTH mode
	 *
	 * @param credentials the credentials
	 * @throw UnsupportedAuthenticationModeException if the connector cannot use the given credentials
	 */
	default void authenticate(Credentials credentials) throws UnsupportedAuthenticationModeException {
		AuthenticationCredentials creds = ConnectorUtils.backportCredentials(credentials, getSupportedAuthProtocols());
		authenticate(creds);
	}


	/**
	 * will check if the current credentials are actually acknowledged by the bugtracker
	 *
	 * Default implementation delegates to the deprecated {@link #checkCredentials(AuthenticationCredentials)}
	 * if the connector supports the BASIC_AUTH mode
	 *
	 * @param credentials
	 * @return nothing
	 * @throw UnsupportedAuthenticationModeException if the connector cannot use the given credentials
	 * @throw {@link BugTrackerNoCredentialsException} if the credentials are invalid
	 * @throw {@link BugTrackerRemoteException} for other network exceptions.
	 */
	default void checkCredentials(Credentials credentials) throws BugTrackerNoCredentialsException,
																	  BugTrackerRemoteException{
		AuthenticationCredentials creds = ConnectorUtils.backportCredentials(credentials, getSupportedAuthProtocols());
		checkCredentials(creds);
	}

	/**
	 * Returns an {@link BugTrackerInterfaceDescriptor}
	 *
	 * @return
	 */
	BugTrackerInterfaceDescriptor getInterfaceDescriptor();


	// ************ legacy methods, deprecated from now on ***************************


	/**
	 * Must set the credentials in the connector context for remote authentication challenges
	 *
	 * @Deprecated use {@link #authenticate(Credentials)} instead
	 * @param credentials
	 */
	@Deprecated
	void authenticate(AuthenticationCredentials credentials);

	/**
	 * Must set the credentials as in {@link #authenticate(AuthenticationCredentials)} and immediately test them against
	 * the endpoint to check their validity
	 *
	 * @Deprecated use {@link #checkCredentials(Credentials)} instead
	 * @param credentials
	 * @throws BugTrackerNoCredentialsException
	 *             for null arguments
	 * @throws BugTrackerRemoteException
	 *             for else.
	 */
	@Deprecated
	void checkCredentials(AuthenticationCredentials credentials)
		throws BugTrackerNoCredentialsException, BugTrackerRemoteException;


}
