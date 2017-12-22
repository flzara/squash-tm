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
package org.squashtest.csp.core.bugtracker.service;

import org.squashtest.csp.core.bugtracker.core.BugTrackerNoCredentialsException;
import org.squashtest.csp.core.bugtracker.core.BugTrackerRemoteException;
import org.squashtest.csp.core.bugtracker.core.UnsupportedAuthenticationModeException;
import org.squashtest.csp.core.bugtracker.net.AuthenticationCredentials;
import org.squashtest.csp.core.bugtracker.spi.BugTrackerInterfaceDescriptor;
import org.squashtest.csp.core.bugtracker.spi.BugtrackerConnectorBase;
import org.squashtest.tm.domain.servers.AuthenticationProtocol;
import org.squashtest.tm.domain.servers.Credentials;

abstract class AbstractInternalConnectorAdapter implements InternalBugtrackerConnector{

	abstract BugtrackerConnectorBase getConnector();

	@Override
	public AuthenticationProtocol[] getSupportedAuthProtocols(){
		return getConnector().getSupportedAuthProtocols();
	}

	@Override
	public boolean supports(AuthenticationProtocol mode){
		return getConnector().supports(mode);
	}

	@Override
	public void authenticate(Credentials credentials) throws UnsupportedAuthenticationModeException{
		getConnector().authenticate(credentials);
	}

	@Override
	public void checkCredentials(Credentials credentials) throws BugTrackerNoCredentialsException,
															  BugTrackerRemoteException{
		getConnector().checkCredentials(credentials);
	}


	@Deprecated
	@Override
	public void authenticate(AuthenticationCredentials credentials){
		getConnector().authenticate(credentials);
	}


	@Deprecated
	@Override
	public void checkCredentials(AuthenticationCredentials credentials) throws BugTrackerNoCredentialsException,
																			BugTrackerRemoteException{
		getConnector().checkCredentials(credentials);
	}

	@Override
	public BugTrackerInterfaceDescriptor getInterfaceDescriptor() {
		return getConnector().getInterfaceDescriptor();
	}
}
