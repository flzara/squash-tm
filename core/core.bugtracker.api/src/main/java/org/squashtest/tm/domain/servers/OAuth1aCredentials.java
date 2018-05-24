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


/**
 * <p>
	Credentials of type OAuth 1a. It is an all-in-one bundle, because it contains the actual credentials (ie the required
info in order to sign requests) and the mean of obtain them (ie the urls and all for the oauth dance).
 	</p>
 */

/*
	Note :
	In the proper bugtracker use-cases (the user authorized Squash already), the message signature require the
	user tokens for one part (each user has its own), and some other info such as consumerKey which are unique for a given
	server. Because of that, the credentials must be reconstructed

	because OAuth1 signature require the user tokens

 */
public class OAuth1aCredentials implements Credentials {

	// ****** signature elements ******************

	private String consumerKey;

	private SignatureMethod signatureMethod;

	/**
	 * The client secret. Depending on the type of signature needed by the endpoint,
	 * it can be either a string (shared with the endpoint), either a pkcs8 private key
	 * (and the endpoint knows the public key)
	 *
	 */
	private String clientSecret;

	/**
	 * The persistent (authorized) token
	 */
	private String token;

	/**
	 * The persistent (authorized) token secret.
	 */
	private String tokenSecret;


	// *************** token dance part ******************

	private HttpMethod requestTokenHttpMethod = HttpMethod.GET;

	private String requestTokenUrl;

	private String userAuthorizationURL;

	private HttpMethod accessTokenHttpMethod = HttpMethod.GET;

	private String accessTokenUrl;


	// ****************** constructors *******************

	public OAuth1aCredentials() {
	}

	public OAuth1aCredentials(String consumerKey, SignatureMethod signatureMethod, String clientSecret, String token, String tokenSecret, HttpMethod requestTokenHttpMethod, String requestTokenUrl, String userAuthorizationURL, HttpMethod accessTokenHttpMethod, String accessTokenUrl) {
		this.consumerKey = consumerKey;
		this.signatureMethod = signatureMethod;
		this.clientSecret = clientSecret;
		this.token = token;
		this.tokenSecret = tokenSecret;
		this.requestTokenHttpMethod = requestTokenHttpMethod;
		this.requestTokenUrl = requestTokenUrl;
		this.userAuthorizationURL = userAuthorizationURL;
		this.accessTokenHttpMethod = accessTokenHttpMethod;
		this.accessTokenUrl = accessTokenUrl;
	}

	// ****************** accessors **********************


	@Override
	public AuthenticationProtocol getImplementedProtocol() {
		return AuthenticationProtocol.OAUTH_1A;
	}

	public String getConsumerKey() {
		return consumerKey;
	}

	public SignatureMethod getSignatureMethod() {
		return signatureMethod;
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

	public HttpMethod getRequestTokenHttpMethod() {
		return requestTokenHttpMethod;
	}

	public String getRequestTokenUrl() {
		return requestTokenUrl;
	}

	public String getUserAuthorizationURL() {
		return userAuthorizationURL;
	}

	public HttpMethod getAccessTokenHttpMethod() {
		return accessTokenHttpMethod;
	}

	public String getAccessTokenUrl() {
		return accessTokenUrl;
	}


	// *************** factories ***********************

	// create a new instance without the token dance metadata
	// the access tokens will be those of a user, supplied as a parameter.
	public OAuth1aCredentials withUserTokens(OAuth1aCredentials userTokens){
		return new OAuth1aCredentials(consumerKey, signatureMethod, clientSecret,userTokens.token, userTokens.tokenSecret, requestTokenHttpMethod, requestTokenUrl,
			userAuthorizationURL, accessTokenHttpMethod, accessTokenUrl);
	}

	// *********** support classes and alike *********

	public static enum SignatureMethod{
		HMAC_SHA1,
		RSA_SHA1;
	}

	public static enum HttpMethod{
		GET,
		POST
	}

}
