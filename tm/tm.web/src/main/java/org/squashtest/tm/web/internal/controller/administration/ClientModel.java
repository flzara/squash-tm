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
package org.squashtest.tm.web.internal.controller.administration;

import org.hibernate.validator.constraints.NotBlank;

public class ClientModel {

	@NotBlank
	private String clientId;
	@NotBlank
	private String clientSecret;
	@NotBlank
	private String registeredRedirectUri;

	public ClientModel(){

	}

	public ClientModel(String clientId, String clientSecret, String registeredRedirectUri){
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		this.registeredRedirectUri = registeredRedirectUri;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId){
		this.clientId = clientId.trim();
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret.trim();
	}

	public String getRegisteredRedirectUri() {
		return registeredRedirectUri;
	}

	public void setRegisteredRedirectUri(String registeredRedirectUri) {
		this.registeredRedirectUri = registeredRedirectUri.trim();
	}


}
