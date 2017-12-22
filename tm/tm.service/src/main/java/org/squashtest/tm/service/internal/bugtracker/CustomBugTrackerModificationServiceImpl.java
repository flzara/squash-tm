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
import org.squashtest.csp.core.bugtracker.core.BugTrackerConnectorFactory;
import org.squashtest.csp.core.bugtracker.core.BugTrackerNoCredentialsException;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.csp.core.bugtracker.service.InternalBugtrackerConnector;
import org.squashtest.tm.domain.servers.AuthenticationProtocol;
import org.squashtest.tm.domain.servers.AuthenticationStatus;
import org.squashtest.tm.domain.servers.Credentials;
import org.squashtest.tm.exception.NameAlreadyInUseException;
import org.squashtest.tm.service.bugtracker.CustomBugTrackerModificationService;
import org.squashtest.tm.service.internal.repository.BugTrackerDao;
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
	public void storeCredentials(long serverId, Credentials credentials) {
		credentialsManager.storeCredentials(serverId, credentials);
	}


	@Override
	public Credentials findCredentials(long serverId) {
		return credentialsManager.findCredentials(serverId);
	}


	@Override
	public void deleteCredentials(long serverId) {
		credentialsManager.deleteCredentials(serverId);
	}


	@Override
	public AuthenticationProtocol[] getSupportedProtocols(BugTracker bugtracker) {
		InternalBugtrackerConnector connector = connectorFactory.createConnector(bugtracker);
		return connector.getSupportedAuthProtocols();
	}


	@Override
	public void testCredentials(long bugtrackerId, Credentials credentials) {
		BugTracker bt = bugTrackerDao.findOne(bugtrackerId);
		InternalBugtrackerConnector connector = connectorFactory.createConnector(bt);
		
		connector.checkCredentials(credentials);
		
	}
	
	
	
	

}
