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
package org.squashtest.tm.service.internal.configuration;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.squashtest.tm.event.ConfigUpdateEvent;
import org.squashtest.tm.service.configuration.ConfigurationService;
import org.squashtest.tm.service.testautomation.spi.BadConfiguration;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.net.MalformedURLException;
import java.net.URL;

import static org.squashtest.tm.service.configuration.ConfigurationService.Properties.SQUASH_CALLBACK_URL;


/**
 * Singleton component providing the value of Squash public url.
 * <br\>
 * This url can be written at two different locations :
 * <ul>
 *     <li>In the database in table CORE_CONFIG with the key {@value ConfigurationService.Properties#SQUASH_CALLBACK_URL}</li>
 *     <li>In the configuration file with key {@value PROPERTY_NAME}</li>
 * </ul>
 * Priority is given to the property stored in database.
 * If it is not set or empty, then the property in configuration file is read.
 * <br\>
 */
@Component
public class CallbackUrlProvider {

	private static final String PROPERTY_NAME = "tm.test.automation.server.callbackurl";

	@Inject
	private ConfigurationService configService;

	private String callbackUrlFromDatabase = null;
	private String callbackUrlFromConfFile = null;

	/**
	 * Update the attribute {@link #callbackUrlFromDatabase} by calling the ConfigurationService.
	 * This method is called once when the bean is created, and each time the database property is updated.
	 */
	@PostConstruct
	@EventListener(classes = {ConfigUpdateEvent.class}, condition = "#root.event.source=='" + SQUASH_CALLBACK_URL + "'")
	private void updateCallbackUrlFromDatabase() {
		callbackUrlFromDatabase = configService.findConfiguration(SQUASH_CALLBACK_URL);
	}

	/**
	 * Value of the Squash callback Url written in the Configuration Properties File. More precisely the value of
	 * the property {@value PROPERTY_NAME}.
	 * @param callbackUrlFromPropertiesFile
	 */
	@Value("${" + PROPERTY_NAME + "}")
	void setUrlFromPropertiesFile(String callbackUrlFromPropertiesFile) {
		this.callbackUrlFromConfFile = callbackUrlFromPropertiesFile;
	}

	/**
	 * Get the callback Url of Squash.
	 * <br\>
	 * First search if the property is set in database with key {@value ConfigurationService.Properties#SQUASH_CALLBACK_URL}.
	 * If it is a valid property, return the corresponding {@link URL}. If it is set but not valid, throw a {@link BadConfiguration}.
	 * If it is not set or empty, then search the property in configuration properties file with key {@value PROPERTY_NAME} and
	 * process like the first one.
	 * @return An instance of {@link URL} with the value of Squash callback Url.
	 * @throws BadConfiguration If the Url found is not a valid {@link URL} or if both of the properties are not set.
	 */
	public URL getCallbackUrl() {
		if(callbackUrlFromDatabase != null && !callbackUrlFromDatabase.isEmpty()) {
			try {
				return new URL(callbackUrlFromDatabase);
			} catch (MalformedURLException ex) {
				BadConfiguration bc = new BadConfiguration(
					"The url '" + callbackUrlFromDatabase + "' specified at property '" + SQUASH_CALLBACK_URL + "' in database is malformed. " +
						"Please contact the administration team.", ex);
				bc.setPropertyName(SQUASH_CALLBACK_URL);
				throw bc;
			}
		} else if(callbackUrlFromConfFile != null && !callbackUrlFromConfFile.isEmpty()){
			try {
				return new URL(callbackUrlFromConfFile);
			} catch(MalformedURLException ex) {
				BadConfiguration bc = new BadConfiguration(
					"The url '" + callbackUrlFromConfFile + "' specified at property '" + PROPERTY_NAME + "' in configuration file is malformed. " +
						"Please contact the administration team." +
						"Note that the property '" + PROPERTY_NAME + "' in configuration file is now DEPRECATED and a configuration in administration settings page is preferred. "
						, ex);
				bc.setPropertyName(PROPERTY_NAME);
				throw bc;
			}
		} else {
			throw new BadConfiguration("The public Url of Squash is not set. " +
				"Please contact the administration team. " +
				"It is recommended to set SquashTM callback url property in administration settings page.");
		}
	}
}
