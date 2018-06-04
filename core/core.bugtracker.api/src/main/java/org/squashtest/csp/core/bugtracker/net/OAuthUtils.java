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

import com.google.api.client.auth.oauth.OAuthHmacSigner;
import com.google.api.client.auth.oauth.OAuthRsaSigner;
import com.google.api.client.auth.oauth.OAuthSigner;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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


}
