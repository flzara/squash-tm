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

import java.util.Arrays;
import java.util.Locale;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.servers.AuthenticationProtocol;
import org.squashtest.tm.domain.servers.ThirdPartyServer;
import org.squashtest.tm.service.servers.EncryptionKeyChangedException;
import org.squashtest.tm.service.servers.ManageableCredentials;
import org.squashtest.tm.service.servers.MissingEncryptionKeyException;
import org.squashtest.tm.service.servers.ServerAuthConfiguration;
import org.squashtest.tm.service.servers.StoredCredentialsManager;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;

/**
 * That helper is useful to initialize instances of {@link ThirdPartyServerCredentialsManagementBean}. 
 * 
 * @author bsiri
 *
 */
@Component
public class ThirdPartyServerCredentialsManagementHelper {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ThirdPartyServerCredentialsManagementHelper.class);
	
	@Inject
	private StoredCredentialsManager credentialsManager;
	
	@Inject
	private InternationalizationHelper i18nHelper;
	
	
	public ThirdPartyServerCredentialsManagementBean initializeFor(ThirdPartyServer server, Locale locale){
		
		ThirdPartyServerCredentialsManagementBean bean = new ThirdPartyServerCredentialsManagementBean();

		// defaults
		bean.setRemoteUrl(server.getUrl());
		bean.setAuthPolicy(server.getAuthenticationPolicy());
		bean.setSelectedProto(server.getAuthenticationProtocol());


		// now check against the credentials
		try{
			ManageableCredentials credentials = credentialsManager.findAppLevelCredentials(server.getId());
			ServerAuthConfiguration configuration = credentialsManager.findServerAuthConfiguration(server.getId());

			bean.setCredentials(credentials);
			bean.setAuthConf(configuration);

		}

		// no encryption key : blocking error, internationalizable
		catch(MissingEncryptionKeyException ex){
			String msg = i18nHelper.internationalize(ex, locale);
			bean.setFailureMessage(msg);
		}
		// key changed : recoverable error, internationalizable
		catch(EncryptionKeyChangedException ex){
			String msg = i18nHelper.internationalize(ex, locale);
			bean.setWarningMessage(msg);
		}
		// other exceptions are treated as non blocking, non internationalizable errors
		catch(Exception ex){
			LOGGER.error(ex.getMessage(), ex);
			bean.setWarningMessage(ex.getMessage());
		}

		return bean;
		
	}
	
}
