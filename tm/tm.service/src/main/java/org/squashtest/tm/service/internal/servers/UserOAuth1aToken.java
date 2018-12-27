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
import org.squashtest.tm.domain.servers.AuthenticationProtocol;
import org.squashtest.tm.domain.servers.OAuth1aCredentials;
import org.squashtest.tm.domain.servers.ThirdPartyServer;
import org.squashtest.tm.service.servers.ManageableCredentials;
import org.squashtest.tm.service.servers.ServerAuthConfiguration;
import org.squashtest.tm.service.servers.StoredCredentialsManager;

/**
 * Represents OAuth 1a user tokens. These are incomplete credentials, please also read {@link ServerOAuth1aConsumerConf}
 * for details.
 */
public class UserOAuth1aToken implements ManageableCredentials {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserOAuth1aToken.class);

	/**
	 * The long-lasting (authorized) user token
	 */
	private String token = "";

	/**
	 * The long-lasting (authorized) user token secret.
	 */
	private String tokenSecret = "";

	public UserOAuth1aToken() {
	}

	public UserOAuth1aToken(String token, String tokenSecret) {
		this.token = token;
		this.tokenSecret = tokenSecret;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getTokenSecret() {
		return tokenSecret;
	}

	public void setTokenSecret(String tokenSecret) {
		this.tokenSecret = tokenSecret;
	}

	boolean isValid(){
		return ! StringUtils.isAnyBlank(token, tokenSecret);
	}

	//*********************** ManageableCredentials **************

	@Override
	public AuthenticationProtocol getImplementedProtocol() {
		return AuthenticationProtocol.OAUTH_1A;
	}

	@Override
	public boolean allowsUserLevelStorage() {
		return true;
	}

	@Override
	public boolean allowsAppLevelStorage(){
		return true;
	}

	@Override
	public OAuth1aCredentials build(StoredCredentialsManager storeManager, ThirdPartyServer server, String username) {

		OAuth1aCredentials result = null;

		LOGGER.debug("Building OAuth1aCredentials");

		ServerAuthConfiguration conf = storeManager.unsecuredFindServerAuthConfiguration(server.getId());

		if (! isValid()){
			LOGGER.debug("Attempted to build OAuth1a credentials for user '{}' but user tokens were invalidated and need to be recreated", username);
		}

		else if (! canBuildWith(conf)){
			LOGGER.error("Attempted to build OAuth1a credentials for user '{}' but could only find the user tokens. The rest of the configuration, " +
					   "usually held as app-level credentials, is absent or invalid.", username);
		}

		else{
			ServerOAuth1aConsumerConf serverConf = (ServerOAuth1aConsumerConf) conf;
			result = new OAuth1aCredentials(serverConf.getConsumerKey(), serverConf.getClientSecret(), token, tokenSecret, serverConf.getSignatureMethod());
		}

		return result;
	}

	private boolean canBuildWith(ServerAuthConfiguration serverConf){

		return (
			serverConf != null &&
				ServerOAuth1aConsumerConf.class.isAssignableFrom(serverConf.getClass())
		);

	}
}
