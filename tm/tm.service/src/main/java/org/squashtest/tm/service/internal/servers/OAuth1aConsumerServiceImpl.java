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

import com.google.api.client.auth.oauth.*;
import com.google.api.client.http.apache.ApacheHttpTransport;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.squashtest.csp.core.bugtracker.core.BugTrackerNoCredentialsException;
import org.squashtest.csp.core.bugtracker.core.BugTrackerRemoteException;
import org.squashtest.csp.core.bugtracker.net.OAuthUtils;
import org.squashtest.tm.domain.servers.AuthenticationProtocol;
import org.squashtest.tm.service.servers.*;

import javax.inject.Inject;
import java.io.IOException;


@Service("squashtest.tm.service.OAuth1aConsumerServiceImpl")
public class OAuth1aConsumerServiceImpl implements OAuth1aConsumerService {

	private static final Logger LOGGER = LoggerFactory.getLogger(OAuth1aConsumerServiceImpl.class);

	/*
		from https://developers.google.com/api-client-library/java/google-http-java-client/reference/1.20.0/com/google/api/client/http/HttpTransport :
		"Implementation is thread-safe, and sub-classes must be thread-safe. For maximum efficiency, applications should use a single globally-shared instance of the HTTP transport."
	*/
	static ApacheHttpTransport transport = new ApacheHttpTransport();

	@Inject
	private StoredCredentialsManager credManager;

	@Inject
	private CredentialsProvider credProvider;

	@Override
	public OAuth1aTemporaryTokens requestTemporaryToken(long serverId, String callbackUrl) {

		LOGGER.debug("requesting temporary tokens for server '{}'", serverId);

		ServerOAuth1aConsumerConf conf = loadConf(serverId);

		// get the tokens
		try {
			SquashGetTemporaryToken getTemporaryToken = new SquashGetTemporaryToken(conf, callbackUrl);

			// code spelunking note :
			// according to Google http client API I don't need to worry about disconnecting the connections
			// because #execute() invokes HttpResponse#parseAsString(), which is supposed to close the input stream for me.
			// if you do however notice a leak in that area, please blame Google instead of me :-)
			OAuthCredentialsResponse response = getTemporaryToken.execute();

			// prepare the authorization redirection
			OAuthAuthorizeTemporaryTokenUrl authorizationUrl = new OAuthAuthorizeTemporaryTokenUrl(conf.getUserAuthorizationURL());
			authorizationUrl.temporaryToken = response.token;

			// return
			return new OAuth1aTemporaryTokens(response.token, response.tokenSecret, authorizationUrl.toString());
		}
		catch(IOException ex){
			throw new BugTrackerRemoteException("failed to retrieve OAuth temporary tokens because an exception occurred at the endpoint", ex);
		}

	}

	@Override
	public void authorize(long serverId, OAuth1aTemporaryTokens tempTokens) {

		String user = credProvider.currentUser();

		LOGGER.debug("authorizing Squash-TM for user '{}' and server '{}'", user, serverId);

		ServerOAuth1aConsumerConf conf = loadConf(serverId);

		try {
			SquashGetAccessToken getAccess = new SquashGetAccessToken(conf, tempTokens);
			OAuthCredentialsResponse response = getAccess.execute();

			UserOAuth1aToken userTokens = new UserOAuth1aToken(response.token, response.tokenSecret);

			credManager.storeUserCredentials(serverId, user, userTokens);
		}
		catch(IOException ex){
			throw new BugTrackerRemoteException("failed to obtain authorization because an exception occurred at the endpoint", ex);
		}

	}



	// *********** private boilerplate ********************

	private ServerOAuth1aConsumerConf loadConf(long serverId){

		LOGGER.debug("requesting temporary tokens for server '{}'", serverId);

		ManageableCredentials credentials = credManager.findAppLevelCredentials(serverId);
		if (credentials == null || credentials.getImplementedProtocol() != AuthenticationProtocol.OAUTH_1A ){
			throw new BugTrackerNoCredentialsException("No OAuth 1a configuration available !", null);
		}

		ServerOAuth1aConsumerConf conf = (ServerOAuth1aConsumerConf) credentials;

		return conf;
	}


	private static final class SquashGetTemporaryToken extends OAuthGetTemporaryToken {

		public SquashGetTemporaryToken(ServerOAuth1aConsumerConf conf, String callbackUrl) {
			super(conf.getRequestTokenUrl());

			boolean usePost = conf.getRequestTokenHttpMethod() == ServerOAuth1aConsumerConf.HttpMethod.POST;

			this.callback = callbackUrl;
			this.transport = OAuth1aConsumerServiceImpl.transport;
			this.consumerKey = conf.getConsumerKey();
			this.usePost = usePost;
			this.signer = createSigner(conf, null);
		}

	}


	private static final class SquashGetAccessToken extends OAuthGetAccessToken{

		public SquashGetAccessToken(ServerOAuth1aConsumerConf conf, OAuth1aTemporaryTokens tempTokens){
			super(conf.getAccessTokenUrl());

			boolean usePost = conf.getRequestTokenHttpMethod() == ServerOAuth1aConsumerConf.HttpMethod.POST;

			this.consumerKey = conf.getConsumerKey();
			this.signer = createSigner(conf, tempTokens.getTempTokenSecret());
			this.temporaryToken = tempTokens.getTempToken();
			this.transport = OAuth1aConsumerServiceImpl.transport;
			this.verifier = tempTokens.getVerifier();
			this.usePost = usePost;

		}

	}



	static OAuthSigner createSigner(ServerOAuth1aConsumerConf conf, String tokenSecret){
		OAuthSigner signer = null;
		switch(conf.getSignatureMethod()){
			case HMAC_SHA1:
				signer = OAuthUtils.createHmacSigner(conf.getClientSecret(), tokenSecret);
				break;

			case RSA_SHA1:
				signer = OAuthUtils.createRsaSigner(conf.getClientSecret());
				break;

			default:
				throw new NotImplementedException("OAuth signature protocol '"+conf.getSignatureMethod()+"' not supported !");
		}
		return signer;
	}


}
