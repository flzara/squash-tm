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
package org.squashtest.tm.service.servers;


public class OAuth1aTemporaryTokens {
	// those are set during the first part of the oauth dance
	final private String tempToken;
	final private String tempTokenSecret;
	final private String redirectUrl;

	// this one is set once the callback is called
	private String verifier;

	public OAuth1aTemporaryTokens(String tempToken, String tempTokenSecret, String redirectUrl) {
		this.tempToken = tempToken;
		this.tempTokenSecret = tempTokenSecret;
		this.redirectUrl = redirectUrl;
	}

	public String getTempToken() {
		return tempToken;
	}

	public String getTempTokenSecret() {
		return tempTokenSecret;
	}

	public String getRedirectUrl() {
		return redirectUrl;
	}

	public String getVerifier() {
		return verifier;
	}

	public void setVerifier(String verifier) {
		this.verifier = verifier;
	}
}
