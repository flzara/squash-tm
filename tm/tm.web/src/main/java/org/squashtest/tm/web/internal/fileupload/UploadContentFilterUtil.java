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
package org.squashtest.tm.web.internal.fileupload;

import org.apache.commons.io.FilenameUtils;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.squashtest.tm.event.ConfigUpdateEvent;
import org.squashtest.tm.service.configuration.ConfigurationService;
import org.squashtest.tm.web.internal.controller.attachment.UploadedData;

@Component
public class UploadContentFilterUtil implements ApplicationListener<ApplicationEvent> {
    /**
     * This is fetched from app context when context started event is triggered.
     */
	private ConfigurationService config;

	private String[] allowed;

	private String whiteListKey = ConfigurationService.Properties.UPLOAD_EXTENSIONS_WHITELIST;

	private void updateConfig() {
		String whiteList = config.findConfiguration(whiteListKey);
		allowed = whiteList.split(",");
	}

	public boolean isTypeAllowed(UploadedData upload) {

		String fileType = FilenameUtils.getExtension(upload.getName());

		for (String type : allowed) {
			if (type.trim().equalsIgnoreCase(fileType)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		if (event instanceof ContextRefreshedEvent && config == null) {
			config = ((ContextRefreshedEvent) event).getApplicationContext().getBean(ConfigurationService.class);
        }

		if (event instanceof ConfigUpdateEvent || event instanceof ContextRefreshedEvent) {
			updateConfig();
		}
	}
}
