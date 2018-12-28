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
package org.squashtest.tm.service.internal.scmserver;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.core.bugtracker.core.BugTrackerNoCredentialsException;
import org.squashtest.tm.domain.scm.ScmServer;
import org.squashtest.tm.domain.servers.AuthenticationPolicy;
import org.squashtest.tm.domain.servers.AuthenticationProtocol;
import org.squashtest.tm.domain.servers.Credentials;
import org.squashtest.tm.domain.servers.ThirdPartyServer;
import org.squashtest.tm.service.internal.repository.ScmServerDao;
import org.squashtest.tm.service.scmserver.ScmServerCredentialsService;
import org.squashtest.tm.service.servers.ManageableCredentials;
import org.squashtest.tm.service.servers.ServerAuthConfiguration;
import org.squashtest.tm.service.servers.StoredCredentialsManager;

import javax.inject.Inject;

import static org.squashtest.tm.service.security.Authorizations.HAS_ROLE_ADMIN;

@Service("ScmServerCredentialsService")
@Transactional
public class ScmServerCredentialsServiceImpl implements ScmServerCredentialsService {

	@Inject
	private ScmServerDao serverDao;

	@Inject
	private StoredCredentialsManager credentialsManager;

	@Inject
	private ScmConnectorRegistry scmConnectorRegistry;


	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public boolean isCredentialsServiceAvailable() {
		return credentialsManager.isSecretConfigured();
	}


	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public void storeCredentials(long serverId, ManageableCredentials credentials) {
		credentialsManager.storeAppLevelCredentials(serverId, credentials);
	}


	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public ManageableCredentials findCredentials(long serverId) {
		return credentialsManager.findAppLevelCredentials(serverId);
	}


	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public void deleteCredentials(long serverId) {
		credentialsManager.deleteAppLevelCredentials(serverId);
	}


	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public AuthenticationProtocol[] getSupportedProtocols(ThirdPartyServer server) {
		return scmConnectorRegistry.createConnector((ScmServer) server).getSupportedProtocols();
	}


	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public void changeAuthenticationPolicy(long serverId, AuthenticationPolicy policy) {
		ThirdPartyServer tracker = serverDao.getOne(serverId);
		tracker.setAuthenticationPolicy(policy);
	}


	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public void changeAuthenticationProtocol(long serverId, AuthenticationProtocol protocol) {
		ThirdPartyServer tracker = serverDao.getOne(serverId);
		tracker.setAuthenticationProtocol(protocol);

		credentialsManager.deleteAppLevelCredentials(serverId);
		credentialsManager.deleteServerAuthConfiguration(serverId);
	}


	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public void testCredentials(long serverId, ManageableCredentials credentials) {

		ThirdPartyServer server = serverDao.getOne(serverId);
		Credentials usableCredentials = credentials.build(credentialsManager, server, null);

		if (usableCredentials == null){
			throw new BugTrackerNoCredentialsException("credentials could not be built, either because the credentials themselves "
														   + "are not suitable, or because the protocol configuration is incomplete/invalid", null);
		}

		// TODO : tester avec le vrai connecteur
		// no exception thrown here
	}


	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public void storeAuthConfiguration(long serverId, ServerAuthConfiguration conf) {
		credentialsManager.storeServerAuthConfiguration(serverId, conf);
	}


	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public ServerAuthConfiguration findAuthConfiguration(long serverId) {
		return credentialsManager.findServerAuthConfiguration(serverId);
	}


	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public void deleteAuthConfiguration(long serverId) {
		credentialsManager.deleteServerAuthConfiguration(serverId);
	}

}
