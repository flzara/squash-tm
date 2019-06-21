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
package org.squashtest.tm.service.internal.servers;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.tm.domain.servers.BasicAuthenticationCredentials;
import org.squashtest.tm.domain.servers.Credentials;
import org.squashtest.tm.domain.servers.ThirdPartyServer;
import org.squashtest.tm.service.servers.ManageableCredentials;
import org.squashtest.tm.service.servers.StoredCredentialsManager;

/**
 * Straightforward implementation of ManageableCredentials for BasicAuthenticationCredentials
 *
 */
public class ManageableBasicAuthCredentials extends BasicAuthenticationCredentials implements ManageableCredentials {

	private static final Logger LOGGER = LoggerFactory.getLogger(ManageableBasicAuthCredentials.class);

	public ManageableBasicAuthCredentials() {
	}

	public ManageableBasicAuthCredentials(String login, char[] password) {
		super(login, password);
	}

	public ManageableBasicAuthCredentials(String login, String password) {
		super(login, password);
	}

	@Override
	public boolean allowsAppLevelStorage() {
		return true;
	}

	@Override
	public boolean allowsUserLevelStorage() {
		return true;
	}

	private boolean isValid(){
		return ! StringUtils.isBlank(getUsername());
		// note : empty password is fine
	}

	@Override
	public Credentials build(StoredCredentialsManager storeManager, ThirdPartyServer server, String username) {
		if (isValid()){
			return this;
		}
		else{
			LOGGER.debug("Cannot create the credentials because username and/or password is empty. Perhaps were they invalidated ?");
			return null;
		}
	}
}
