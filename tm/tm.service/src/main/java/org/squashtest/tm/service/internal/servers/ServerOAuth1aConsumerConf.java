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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.tm.domain.servers.AuthenticationProtocol;
import org.squashtest.tm.domain.servers.Credentials;
import org.squashtest.tm.domain.servers.OAuth1aCredentials;
import org.squashtest.tm.domain.servers.OAuth1aCredentials.SignatureMethod;
import org.squashtest.tm.service.servers.ManageableCredentials;
import org.squashtest.tm.service.servers.StoredCredentialsManager;

/**
 * <p>
 * 		This class contains all the information related to a OAuth1a remote endpoint. It also acts as a factory of
 * 		0Auth1aCredentials.
 *</p>
 *
 * <p>
 *     	The OAuth1aCredentials are created by merging a user's {@link UserOAuth1aToken} with
 *     	{@link ServerOAuth1aConsumerConf}. However, it can also embbed its own tokens, that will then be used to identity
 *     	Squash-TM itself.
 * </p>
 *
 * <p>
 *     Note that it implements {@link org.squashtest.tm.domain.servers.Credentials} for technical reasons (because of
 *     how the StoredCredentialsManager is built), but until that ill conception is fixed be careful not to expose
 *     them as consumable credentials.
 * </p>
 */
public class ServerOAuth1aConsumerConf implements ManageableCredentials {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServerOAuth1aConsumerConf.class);

	/**
	 * Identifier of the OAuth endpoint Squash-TM is supposed to dock to
	 */
	private String consumerKey = "";


	/**
	 * The client secret. Depending on the type of signature needed by the endpoint,
	 * it can be either a string (shared with the endpoint), either a pkcs8 private key
	 * (and the endpoint knows the public key)
	 *
	 */
	private String clientSecret = "";

	/**
	 * Squash-TM own tokens, if any.
	 */
	private UserOAuth1aToken squashtmTokens = new UserOAuth1aToken();

	/**
	 * The signature algorithm.
	 */
	private SignatureMethod signatureMethod = SignatureMethod.HMAC_SHA1;

	// *************** token dance part ******************

	private HttpMethod requestTokenHttpMethod = HttpMethod.GET;

	private String requestTokenUrl = "";

	private String userAuthorizationURL = "";

	private HttpMethod accessTokenHttpMethod = HttpMethod.GET;

	private String accessTokenUrl = "";


	// ****************** constructors *******************

	public ServerOAuth1aConsumerConf() {

	}

	public ServerOAuth1aConsumerConf(String consumerKey, OAuth1aCredentials.SignatureMethod signatureMethod, String clientSecret, UserOAuth1aToken squashtmTokens, HttpMethod requestTokenHttpMethod, String requestTokenUrl, String userAuthorizationURL, HttpMethod accessTokenHttpMethod, String accessTokenUrl) {
		this.consumerKey = consumerKey;
		this.signatureMethod = signatureMethod;
		this.clientSecret = clientSecret;
		this.squashtmTokens = squashtmTokens;
		this.requestTokenHttpMethod = requestTokenHttpMethod;
		this.requestTokenUrl = requestTokenUrl;
		this.userAuthorizationURL = userAuthorizationURL;
		this.accessTokenHttpMethod = accessTokenHttpMethod;
		this.accessTokenUrl = accessTokenUrl;
	}

	// *************** ManageableCredentials ***********************

	@Override
	public boolean allowsAppLevelStorage() {
		return true;
	}

	@Override
	public Credentials build(StoredCredentialsManager storeManager, BugTracker server, String username) {
		if (isValid()){
			return new OAuth1aCredentials(consumerKey, clientSecret, squashtmTokens.getToken(), squashtmTokens.getTokenSecret(), signatureMethod);
		}
		else{
			LOGGER.debug("cannot create oauth1a server credentials : no app-level tokens defined");
			return null;
		}
	}

	// ****************** accessors **********************

	private boolean isValid(){
		return (squashtmTokens != null && squashtmTokens.isValid());
	}

	@Override
	public void invalidate() {
		if (squashtmTokens != null){
			squashtmTokens.invalidate();
		}
	}

	@Override
	public AuthenticationProtocol getImplementedProtocol() {
		return AuthenticationProtocol.OAUTH_1A;
	}

	public String getConsumerKey() {
		return consumerKey;
	}

	public void setConsumerKey(String consumerKey) {
		this.consumerKey = consumerKey;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	public UserOAuth1aToken getSquashtmTokens() {
		return squashtmTokens;
	}

	public void setSquashtmTokens(UserOAuth1aToken squashtmTokens) {
		this.squashtmTokens = squashtmTokens;
	}

	public SignatureMethod getSignatureMethod() {
		return signatureMethod;
	}

	public void setSignatureMethod(SignatureMethod signatureMethod) {
		this.signatureMethod = signatureMethod;
	}

	public HttpMethod getRequestTokenHttpMethod() {
		return requestTokenHttpMethod;
	}

	public void setRequestTokenHttpMethod(HttpMethod requestTokenHttpMethod) {
		this.requestTokenHttpMethod = requestTokenHttpMethod;
	}

	public String getRequestTokenUrl() {
		return requestTokenUrl;
	}

	public void setRequestTokenUrl(String requestTokenUrl) {
		this.requestTokenUrl = requestTokenUrl;
	}

	public String getUserAuthorizationURL() {
		return userAuthorizationURL;
	}

	public void setUserAuthorizationURL(String userAuthorizationURL) {
		this.userAuthorizationURL = userAuthorizationURL;
	}

	public HttpMethod getAccessTokenHttpMethod() {
		return accessTokenHttpMethod;
	}

	public void setAccessTokenHttpMethod(HttpMethod accessTokenHttpMethod) {
		this.accessTokenHttpMethod = accessTokenHttpMethod;
	}

	public String getAccessTokenUrl() {
		return accessTokenUrl;
	}

	public void setAccessTokenUrl(String accessTokenUrl) {
		this.accessTokenUrl = accessTokenUrl;
	}


	// *********** support classes and alike *********

	public static enum HttpMethod{
		GET,
		POST
	}



}
