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
package org.squashtest.tm.web.internal.security.authentication;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.squashtest.tm.api.security.authentication.AuthenticationProviderFeatures;
import org.squashtest.tm.core.foundation.lang.Assert;
import org.squashtest.tm.web.internal.annotation.ApplicationComponent;

/**
 * @author Gregory Fouquet
 * 
 */
@ApplicationComponent
public class AuthenticationProviderContext {
	/**
	 * logger.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationProviderContext.class);
	/**
	 * name of the current auth provider.
	 */
	@Value("${authentication.provider:internal}")
	private String currentProviderName;

	/**
	 * list of configured provider features.
	 */
	@Inject
	private List<AuthenticationProviderFeatures> providersFeatures = new ArrayList<>();

	/**
	 * 
	 */
	public AuthenticationProviderContext() {
		super();
	}

	public AuthenticationProviderFeatures getCurrentProviderFeatures() {
		for (AuthenticationProviderFeatures features : providersFeatures) {
			if (currentProviderName.equals(features.getProviderName())) {
				return features;
			}
		}

		LOGGER.error("Provider features named {} could not be found in list {}", currentProviderName, providersFeatures);

		throw new IllegalStateException("Features for authentication provider named '" + currentProviderName
				+ "' not available");
	}

	/**
	 * initialization and checks.
	 */
	@PostConstruct
	public void initializeContext() {
		checkConfiguration();
	}

	private void checkConfiguration() {
		Assert.propertyNotBlank(currentProviderName, "currentPropertyName should not be blank");
		getCurrentProviderFeatures();
	}

}
