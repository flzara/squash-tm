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
package org.squashtest.tm.service.internal.attachment;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import static org.squashtest.tm.service.internal.attachment.AttachmentStorageMode.DATABASE;
import static org.squashtest.tm.service.internal.attachment.AttachmentStorageMode.FILE_REPOSITORY;

@Component
public class AttachmentStorageModeConfigurer {

	private static final Logger LOGGER = LoggerFactory.getLogger(AttachmentStorageModeConfigurer.class);

	@Inject
	private AbstractEnvironment environment;

	private AttachmentStorageMode attachmentStorageMode = DATABASE;

	private String repoPath;

	@PostConstruct
	public void init(){
		boolean isFileRepo = environment.getProperty("squashtm.feature.file.repository",Boolean.class, Boolean.FALSE);
		if(isFileRepo){
			attachmentStorageMode = FILE_REPOSITORY;
			LOGGER.info("The property 'squashtm.feature.file.repository' is set to true. Attachments will be stored in file system.");
			repoPath = environment.getRequiredProperty("squash.path.file.repository");
			repoPath = StringUtils.appendIfMissing(repoPath, "/");
			LOGGER.info("File repository path is configure as : {}", repoPath);
		} else {
			LOGGER.info("squashtm.feature.file.repository is set to false or not indicated. Attachments will be stored in database.");
		}
	}

	public AttachmentStorageMode getAttachmentStorageMode() {
		return attachmentStorageMode;
	}

	public String getRepoPath() {
		return repoPath;
	}

}

