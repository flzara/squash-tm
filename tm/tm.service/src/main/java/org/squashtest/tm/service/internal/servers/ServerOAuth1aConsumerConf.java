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


import org.squashtest.tm.domain.servers.AuthenticationProtocol;
import org.squashtest.tm.domain.servers.OAuth1aCredentials;
import org.squashtest.tm.domain.servers.OAuth1aCredentials.SignatureMethod;
import org.squashtest.tm.service.servers.ServerAuthConfiguration;

import javax.validation.constraints.NotBlank;

/**
 * <p>
 * 		This class contains all the information related to a OAuth1a remote endpoint. It also acts as a factory of
 * 		0Auth1aCredentials. The OAuth1aCredentials are created by merging a user's {@link UserOAuth1aToken} with
 *     	{@link ServerOAuth1aConsumerConf}.
 * </p>
 */
public class ServerOAuth1aConsumerConf implements ServerAuthConfiguration {


	/**
	 * Identifier of the OAuth endpoint Squash-TM is supposed to dock to
	 */

	@NotBlank
	private String consumerKey = "";


	/**
	 * The client secret. Depending on the type of signature needed by the endpoint,
	 * it can be either a string (shared with the endpoint), either a pkcs8 private key
	 * (and the endpoint knows the public key)
	 *
	 */
	@NotBlank
	private String clientSecret = "";


	/**
	 * The signature algorithm.
	 */
	private SignatureMethod signatureMethod = SignatureMethod.HMAC_SHA1;

	// *************** token dance part ******************

	private HttpMethod requestTokenHttpMethod = HttpMethod.GET;

	@NotBlank
	private String requestTokenUrl = "";

	@NotBlank
	private String userAuthorizationUrl = "";

	private HttpMethod accessTokenHttpMethod = HttpMethod.GET;

	@NotBlank
	private String accessTokenUrl = "";


	// ****************** constructors *******************

	public ServerOAuth1aConsumerConf() {

	}

	public ServerOAuth1aConsumerConf(String consumerKey, OAuth1aCredentials.SignatureMethod signatureMethod, String clientSecret, HttpMethod requestTokenHttpMethod, String requestTokenUrl, String userAuthorizationUrl, HttpMethod accessTokenHttpMethod, String accessTokenUrl) {
		this.consumerKey = consumerKey;
		this.signatureMethod = signatureMethod;
		this.clientSecret = clientSecret;
		this.requestTokenHttpMethod = requestTokenHttpMethod;
		this.requestTokenUrl = requestTokenUrl;
		this.userAuthorizationUrl = userAuthorizationUrl;
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

	public void setConsumerKey(String consumerKey) {
		this.consumerKey = consumerKey;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
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

	public String getUserAuthorizationUrl() {
		return userAuthorizationUrl;
	}

	public void setUserAuthorizationUrl(String userAuthorizationURL) {
		this.userAuthorizationUrl = userAuthorizationURL;
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
