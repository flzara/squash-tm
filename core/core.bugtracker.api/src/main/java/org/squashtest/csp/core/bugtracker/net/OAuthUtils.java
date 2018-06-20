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
package org.squashtest.csp.core.bugtracker.net;

import com.google.api.client.auth.oauth.*;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import org.apache.commons.codec.binary.Base64;
import org.springframework.http.HttpMethod;
import org.squashtest.tm.domain.servers.OAuth1aCredentials;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

public final class OAuthUtils {

	private OAuthUtils(){}

	public static OAuthSigner createHmacSigner(String clientSecret, String tokenSecret){
		OAuthHmacSigner signer = new OAuthHmacSigner();
		signer.clientSharedSecret = clientSecret;
		signer.tokenSharedSecret = tokenSecret;
		return signer;
	}

	public static OAuthSigner createRsaSigner(String clientSecret){

		OAuthRsaSigner signer = new OAuthRsaSigner();

		//prepare the private key
		byte[] pkBytes = Base64.decodeBase64(clientSecret);
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pkBytes);

		try {
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			signer.privateKey = keyFactory.generatePrivate(keySpec);
		}
		catch(NoSuchAlgorithmException nsae){
			throw new RuntimeException("Your JRE (JCE provider) does not support the RSA algorithm, where did you get that !?", nsae);
		}
		catch(InvalidKeySpecException ikse){
			throw new RuntimeException("Cannot create private key for oauth rsa signing", ikse);
		}

		return signer;

	}


	/**
	 * Creates the signature that corresponds to the given target url and given http method.
	 * Throws IllegalArgumentException if the url or the method are malformed, or if the credentials
	 * cannot properly sign the request.
	 *
	 * @param creds
	 * @param targetUrl
	 * @param httpMethod
	 * @return
	 */
	public static final String createAuthorizationHeader(OAuth1aCredentials creds, String targetUrl, String httpMethod){

		validateUrlAndMethod(targetUrl, httpMethod);

		// create the oauth params
		OAuthParameters params = createOAuthParams(creds);

		// compute the signature
		GenericUrl genericTargetUrl = new GenericUrl(targetUrl);
		String normMethod = httpMethod.toUpperCase();

		computeSignature(params, genericTargetUrl, normMethod);

		// done
		return params.getAuthorizationHeader();

	}


	// **************** private boilerplate ************************

	static OAuthSigner createSigner(OAuth1aCredentials creds){
		if (creds.getSignatureMethod() == OAuth1aCredentials.SignatureMethod.HMAC_SHA1){
			return createHmacSigner(creds.getClientSecret(), creds.getTokenSecret());
		}
		else{
			return createRsaSigner(creds.getClientSecret());
		}
	}

	private void validateParameters(String url, String method){
		try{
			new URL(url);
		}
		catch(MalformedURLException ex){
			throw new IllegalArgumentException("malformed url : "+url);
		}

	}

	private static OAuthParameters createOAuthParams(OAuth1aCredentials creds){
		OAuthParameters params = new OAuthParameters();
		params.consumerKey = creds.getConsumerKey();
		params.signer = createSigner(creds);
		params.token = creds.getToken();
		params.verifier = creds.getTokenSecret();
		return params;
	}

	private static void validateUrlAndMethod(String url, String method){
		// 1 : validate URL
		try{
			new URL(url);
		}
		catch(MalformedURLException ex){
			throw new IllegalArgumentException("malformed url :", ex);
		}

		// 2 : valudate method. This will naturally throw IllegalArgumentException in
		// case it is invalid
		HttpMethod.valueOf(method.toUpperCase());

	}

	// this is ripped of OAuthParameters#intercept(HttpRequest), sorry
	private static void computeSignature(OAuthParameters params, GenericUrl url, String method){
		params.computeNonce();
		params.computeTimestamp();
		try {
			params.computeSignature(method, url);
		}
		catch(GeneralSecurityException e){
			throw new IllegalArgumentException("it seems that something is wrong with your credentials that prevented the computation of the signature", e);
		}
	}

}
