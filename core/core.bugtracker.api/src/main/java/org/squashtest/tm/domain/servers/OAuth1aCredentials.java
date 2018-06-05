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
package org.squashtest.tm.domain.servers;


import org.apache.commons.lang3.NotImplementedException;
import org.squashtest.csp.core.bugtracker.net.OAuthUtils;

/**
 * <p>
	Credentials of type OAuth 1a.
 	</p>
 */

public class OAuth1aCredentials implements Credentials {

	/**
	 * Identifier of the OAuth endpoint Squash-TM is supposed to dock to
	 */
	private final String consumerKey;


	/**
	 * The client secret. Depending on the type of signature needed by the endpoint,
	 * it can be either a string (shared with the endpoint), either a pkcs8 private key
	 * (and the endpoint knows the public key)
	 *
	 */
	private final String clientSecret;

	/**
	 * The long-lasting (authorized) user token
	 */
	private final String token;

	/**
	 * The long-lasting (authorized) user token secret.
	 */
	private final String tokenSecret;

	/**
	 * The signature algorithm.
	 */
	private final SignatureMethod signatureMethod;


	public static enum SignatureMethod{
		HMAC_SHA1,
		RSA_SHA1;
	}

	// *********** getters, setters, methods **************

	public OAuth1aCredentials(String consumerKey, String clientSecret, String token, String tokenSecret, SignatureMethod signatureMethod) {
		this.consumerKey = consumerKey;
		this.clientSecret = clientSecret;
		this.token = token;
		this.tokenSecret = tokenSecret;
		this.signatureMethod = signatureMethod;
	}


	/*
		Returns the value of the http header 'authorization' for the given request.
	 */
	public String createAuthorizationHeader(String url, String httpMethod){
		return OAuthUtils.createAuthorizationHeader(this, url, httpMethod);
	}


	@Override
	public AuthenticationProtocol getImplementedProtocol() {
		return AuthenticationProtocol.OAUTH_1A;
	}

	public String getConsumerKey() {
		return consumerKey;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public String getToken() {
		return token;
	}

	public String getTokenSecret() {
		return tokenSecret;
	}

	public SignatureMethod getSignatureMethod() {
		return signatureMethod;
	}



}
