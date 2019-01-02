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
package org.squashtest.tm.web.internal.controller.thirdpartyserver;

import java.util.List;

import org.squashtest.tm.domain.servers.AuthenticationPolicy;
import org.squashtest.tm.domain.servers.AuthenticationProtocol;
import org.squashtest.tm.service.servers.ManageableCredentials;
import org.squashtest.tm.service.servers.ServerAuthConfiguration;


/**
 * <p>This bean holds the necessary elements for the the generation of the credentials management UI subcomponent, used 
 * in the server administration pages e.g. for BugTracker or ScmServer etc.</p>
 * 
 * @author bsiri
 *
 */
public final class ThirdPartyServerCredentialsManagementBean{

	// the error messages. If they remains to null it is a good thing
	private String failureMessage = null;
	private String warningMessage = null;

	// the rest is used if the above is null
	private AuthenticationPolicy authPolicy;
	private List<AuthenticationProtocol> availableProtos;
	private AuthenticationProtocol selectedProto;


	// the URL of the third party server entity being configured
	private String entityUrl = null;
	// the url of the remote server
	private String remoteUrl = null;
	
	// conf
	private ServerAuthConfiguration authConf;
	// app-level credentials
	private ManageableCredentials credentials;
	
	
	// UI features
	private boolean featureTestCredentialsButton = true;
	private boolean featureAuthPolicySelection = true;
	

	public String getFailureMessage() {
		return failureMessage;
	}

	public void setFailureMessage(String failureMessage) {
		this.failureMessage = failureMessage;
	}

	public String getWarningMessage() {
		return warningMessage;
	}

	public void setWarningMessage(String warningMessage) {
		this.warningMessage = warningMessage;
	}
	


	public AuthenticationPolicy getAuthPolicy() {
		return authPolicy;
	}

	public void setAuthPolicy(AuthenticationPolicy authPolicy) {
		this.authPolicy = authPolicy;
	}


	public List<AuthenticationProtocol> getAvailableProtos() {
		return availableProtos;
	}

	public void setAvailableProtos(List<AuthenticationProtocol> availableProtos) {
		this.availableProtos = availableProtos;
	}

	public AuthenticationProtocol getSelectedProto() {
		return selectedProto;
	}

	public void setSelectedProto(AuthenticationProtocol selectedProto) {
		this.selectedProto = selectedProto;
	}

	public ServerAuthConfiguration getAuthConf() {
		return authConf;
	}

	public void setAuthConf(ServerAuthConfiguration authConf) {
		this.authConf = authConf;
	}

	public ManageableCredentials getCredentials() {
		return credentials;
	}

	public void setCredentials(ManageableCredentials credentials) {
		this.credentials = credentials;
	}

	public String getEntityUrl() {
		return entityUrl;
	}

	public void setEntityUrl(String entityUrl) {
		this.entityUrl = entityUrl;
	}

	public String getRemoteUrl() {
		return remoteUrl;
	}

	public void setRemoteUrl(String remoteUrl) {
		this.remoteUrl = remoteUrl;
	}

	public boolean isFeatureTestCredentialsButton() {
		return featureTestCredentialsButton;
	}

	public void setFeatureTestCredentialsButton(boolean featureTestCredentialsButton) {
		this.featureTestCredentialsButton = featureTestCredentialsButton;
	}

	public boolean isFeatureAuthPolicySelection() {
		return featureAuthPolicySelection;
	}

	public void setFeatureAuthPolicySelection(boolean featureAuthPolicySelection) {
		this.featureAuthPolicySelection = featureAuthPolicySelection;
	}


	
}