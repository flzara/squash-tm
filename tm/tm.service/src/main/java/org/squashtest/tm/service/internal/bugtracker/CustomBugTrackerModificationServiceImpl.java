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
package org.squashtest.tm.service.internal.bugtracker;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.core.bugtracker.core.BugTrackerNoCredentialsException;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.tm.domain.servers.AuthenticationPolicy;
import org.squashtest.tm.domain.servers.AuthenticationProtocol;
import org.squashtest.tm.domain.servers.Credentials;
import org.squashtest.tm.exception.NameAlreadyInUseException;
import org.squashtest.tm.service.bugtracker.BugTrackersService;
import org.squashtest.tm.service.bugtracker.CustomBugTrackerModificationService;
import org.squashtest.tm.service.internal.bugtracker.adapter.InternalBugtrackerConnector;
import org.squashtest.tm.service.internal.repository.BugTrackerDao;
import org.squashtest.tm.service.servers.ManageableCredentials;
import org.squashtest.tm.service.servers.ServerAuthConfiguration;
import org.squashtest.tm.service.servers.StoredCredentialsManager;

/**
 *
 * @author mpagnon
 *
 */
@Service("CustomBugTrackerModificationService")
@Transactional
public class CustomBugTrackerModificationServiceImpl implements CustomBugTrackerModificationService {

	@Inject
	private BugTrackerDao bugTrackerDao;

	@Inject
	private StoredCredentialsManager credentialsManager;

	@Inject
	private BugTrackerConnectorFactory connectorFactory;

	@Inject
	private BugTrackersService btService;


	@Override
	public void changeName(long bugtrackerId, String newName) {
		String trimedNewName = newName.trim();
		BugTracker bugTracker = bugTrackerDao.findOne(bugtrackerId);
		if(!bugTracker.getName().equals(trimedNewName)){
			BugTracker existing = bugTrackerDao.findByName(trimedNewName);
			if (existing == null){
				bugTracker.setName(trimedNewName);
			}
			else{
				throw new NameAlreadyInUseException(NameAlreadyInUseException.EntityType.BUG_TRACKER, trimedNewName);
			}
		}

	}


	@Override
	public boolean isCredentialsServiceAvailable() {
		return credentialsManager.isSecretConfigured();
	}


	@Override
	public void storeCredentials(long serverId, ManageableCredentials credentials) {
		credentialsManager.storeAppLevelCredentials(serverId, credentials);
	}


	@Override
	public ManageableCredentials findCredentials(long serverId) {
		return credentialsManager.findAppLevelCredentials(serverId);
	}


	@Override
	public void deleteCredentials(long serverId) {
		credentialsManager.deleteAppLevelCredentials(serverId);
	}


	@Override
	public AuthenticationProtocol[] getSupportedProtocols(BugTracker bugtracker) {
		InternalBugtrackerConnector connector = connectorFactory.createConnector(bugtracker);
		return connector.getSupportedAuthProtocols();
	}


	@Override
	public void changeAuthenticationPolicy(long bugtrackerId, AuthenticationPolicy policy) {
		BugTracker tracker = bugTrackerDao.findOne(bugtrackerId);
		tracker.setAuthenticationPolicy(policy);
	}


	@Override
	public void changeAuthenticationProtocol(long bugtrackerId, AuthenticationProtocol protocol) {
		BugTracker tracker = bugTrackerDao.findOne(bugtrackerId);
		tracker.setAuthenticationProtocol(protocol);

		credentialsManager.deleteAppLevelCredentials(bugtrackerId);
		credentialsManager.deleteServerAuthConfiguration(bugtrackerId);
	}


	@Override
	public void testCredentials(long bugtrackerId, ManageableCredentials credentials) {

		BugTracker bt = bugTrackerDao.findOne(bugtrackerId);
		Credentials usableCredentials = credentials.build(credentialsManager, bt, null);

		if (usableCredentials == null){
			throw new BugTrackerNoCredentialsException("credentials could not be built, either because the credentials themselves "
					+ "are not suitable, or because the protocol configuration is incomplete/invalid", null);
		}

		btService.testCredentials(bt, usableCredentials);
	}


	@Override
	public void storeAuthConfiguration(long serverId, ServerAuthConfiguration conf) {
		credentialsManager.storeServerAuthConfiguration(serverId, conf);
	}


	@Override
	public ServerAuthConfiguration findAuthConfiguration(long serverId) {
		return credentialsManager.findServerAuthConfiguration(serverId);
	}


	@Override
	public void deleteAuthConfiguration(long serverId) {
		credentialsManager.deleteServerAuthConfiguration(serverId);
	}


}
