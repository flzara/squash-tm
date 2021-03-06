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
package org.squashtest.tm.service.scmserver;


import org.squashtest.csp.core.bugtracker.core.BugTrackerRemoteException;
import org.squashtest.tm.domain.scm.ScmServer;
import org.squashtest.tm.domain.servers.AuthenticationPolicy;
import org.squashtest.tm.domain.servers.AuthenticationProtocol;
import org.squashtest.tm.service.servers.ManageableCredentials;
import org.squashtest.tm.service.servers.ServerAuthConfiguration;
import org.squashtest.tm.service.servers.StoredCredentialsManager;

/**
 * <p>
 *     This service is temporary and will manage the credentials of a {@link org.squashtest.tm.domain.scm.ScmServer}
 * until the unified ThirdPartyServer infrastructure is complete. It is essentially a copy/pasta of
 * a massive part of CustomBugTrackerModificationService, which does the same thing for the bugtrackers.
 * </p>
 *
 * <p>
 *     Deadlines imposes us to loan that technical debt (based on good code hopefully).
 *     Remember that this is temporary and you should not copy/paste it again.
 * </p>
 *
 * <p>
 *     Edit AGU 26/11/2020: following introduction of TestAutomationServer as ThirdPartyServer,
 *     all methods which only deals with ThirdPartyServer (and not a specification like ScmServer) have been moved to
 *     {@link org.squashtest.tm.service.thirdpartyserver.ThirdPartyServerCredentialsService}
 * </p>
 */
public interface ScmServerCredentialsService {


	/**
	 * Returns the authentication protocols supported by the underlying connector
	 *
	 * @param server
	 * @return
	 */
	AuthenticationProtocol[] getSupportedProtocols(ScmServer server);


	/**
	 * Tests whether the given credentials are valid for the given server.
	 * The method exits normally if the credentials are valid.
	 *
	 * @param serverId
	 * @param credentials
	 * @throws BugTrackerRemoteException in case of a problem.
	 * @throws org.squashtest.csp.core.bugtracker.core.BugTrackerNoCredentialsException if the credentials where rejected, or could not be built due to incomplete data
	 * @return
	 */
	// Note : the exceptions thrown here belong to the BugTracker domain, they should be replaced with
	// exceptions from the ThirdPartyServer domain once the exception family exists
	void testCredentials(long serverId, ManageableCredentials credentials);
}
