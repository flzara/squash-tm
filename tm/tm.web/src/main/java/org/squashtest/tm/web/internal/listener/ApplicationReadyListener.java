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
package org.squashtest.tm.web.internal.listener;

import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.squashtest.tm.service.configuration.ConfigurationService;
import org.squashtest.tm.web.internal.annotation.ApplicationComponent;

import javax.inject.Inject;

@ApplicationComponent
public class ApplicationReadyListener implements ApplicationListener<ApplicationReadyEvent> {

	@Value("${tm.test.automation.server.callbackurl}")
	private String callbackUrlFromConfFile;

	@Inject
	private ConfigurationService configurationService;

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		copySquashPublicUrlFromConfFileIntoDatabase();
	}

	/**
	 * If the value of SquashTM public Url is not set in database
	 * (property {@value ConfigurationService.Properties#SQUASH_CALLBACK_URL}), and if it is set in configuration file
	 * (property 'tm.test.automation.server.callbackurl'), then copy this value into the database property.
	 * <br\>
	 * Here the validity of the property in configuration file is not verified before being copied.
	 * It will accustom users to use the property in database in the future.
	 */
	private void copySquashPublicUrlFromConfFileIntoDatabase() {
		String callbackUrlFromDatabase =
			configurationService.findConfiguration(ConfigurationService.Properties.SQUASH_CALLBACK_URL);
		if(Strings.isBlank(callbackUrlFromDatabase) && !Strings.isBlank(callbackUrlFromConfFile)) {
			configurationService.set(ConfigurationService.Properties.SQUASH_CALLBACK_URL, callbackUrlFromConfFile);
		}
	}
}
