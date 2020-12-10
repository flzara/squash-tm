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
package org.squashtest.tm.service.internal.thirdpartyserver;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.servers.AuthenticationPolicy;
import org.squashtest.tm.domain.servers.AuthenticationProtocol;
import org.squashtest.tm.domain.servers.ThirdPartyServer;
import org.squashtest.tm.service.internal.repository.ThirdPartyServerDao;
import org.squashtest.tm.service.servers.ManageableCredentials;
import org.squashtest.tm.service.servers.ServerAuthConfiguration;
import org.squashtest.tm.service.servers.StoredCredentialsManager;
import org.squashtest.tm.service.thirdpartyserver.ThirdPartyServerCredentialsService;

import javax.inject.Inject;

import static org.squashtest.tm.service.security.Authorizations.HAS_ROLE_ADMIN;

@Service("ThirdPartyServerCredentialsService")
@Transactional
public class ThirdPartyServerCredentialsServiceImpl implements ThirdPartyServerCredentialsService {

	@Inject
	private StoredCredentialsManager credentialsManager;

	@Inject
	private ThirdPartyServerDao thirdPartyServerDao;

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

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public void changeAuthenticationPolicy(long serverId, AuthenticationPolicy policy) {
		ThirdPartyServer tracker = thirdPartyServerDao.getOne(serverId);
		tracker.setAuthenticationPolicy(policy);
	}


	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public void changeAuthenticationProtocol(long serverId, AuthenticationProtocol protocol) {
		ThirdPartyServer tracker = thirdPartyServerDao.getOne(serverId);
		tracker.setAuthenticationProtocol(protocol);

		credentialsManager.deleteAppLevelCredentials(serverId);
		credentialsManager.deleteServerAuthConfiguration(serverId);
	}
}
