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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.attachment.Attachment;
import org.squashtest.tm.domain.attachment.AttachmentContent;
import org.squashtest.tm.service.attachment.RawAttachment;
import org.squashtest.tm.service.internal.repository.AttachmentDao;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Component("fileSystemAttachmentRepository")
@ConditionalOnProperty(name = "squashtm.feature.file.repository", havingValue = "true")
@Transactional
public class FileSystemAttachmentRepository implements AttachmentRepository {

	private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemAttachmentRepository.class);

	@Inject
	private Environment env;

	@PersistenceContext
	private EntityManager entityManager;

	@Inject
	private AttachmentDao attachmentDao;

	@Inject
	private AttachmentStorageModeConfigurer storageConfigurer;

	@Override
	public AttachmentContent createContent(RawAttachment rawAttachment, long attachmentListId) throws IOException {
		AttachmentContent content = new AttachmentContent();
		entityManager.persist(content);
		entityManager.flush();//flush to force database to give us the id we need to compute path
		String path = findFolderPath(attachmentListId);
		path = path + content.getId();
		InputStream stream = rawAttachment.getStream();
		FileUtils.copyInputStreamToFile(stream, new File(path));//love this method. Kudos to apache
		return content;
	}

	@Override
	public InputStream getContentStream(Long attachmentId) throws FileNotFoundException {
		String path = getAttachmentPath(attachmentId);
		return new FileInputStream(path);
	}

	@Override
	public void removeContent(long attachmentId) throws IOException {
		String path = getAttachmentPath(attachmentId);
		Files.delete(Paths.get(path));
	}

	@Override
	public void copyContent(Attachment copy) {
		Long sourceId = copy.getAttachmentToCopyId();
		String sourcePath = getAttachmentPath(sourceId);
		String copyPath = getAttachmentPath(copy.getId());
		try {
			FileUtils.copyFile(new File(sourcePath), new File(copyPath));
		} catch (IOException e) {
			LOGGER.error("Unable to copy " + sourcePath + " to " + copyPath);
			throw new RuntimeException(e);
		}
	}

	@Override
	public void deleteContent(List<Long> attachmentListIds) {
		for (Long attachmentListId : attachmentListIds) {
			String folderPath = findFolderPath(attachmentListId);
			File folder = new File(folderPath);
			try {
				FileUtils.cleanDirectory(folder);
				Files.deleteIfExists(Paths.get(folderPath));
			} catch (IOException e) {
				LOGGER.error("Unable to delete " + folderPath);
				throw new RuntimeException(e);
			}
		}
	}


	/**
	 * Calculate the file path for an attachment.
	 * The path is defined as : the attachment list id / attachment CONTENT id
	 * The attachment list id is padded to 12 chars to have a for level hierarchy
	 * ex : For an attachment list id of 9856 and a CONTENT ID of 56897 path will be
	 * /000/000/009/856/56897
	 * @param attachmentId the ATTACHMENT ID, the method will take care to find the content id for generating path
	 * @return the path as specified above
	 */
	private String getAttachmentPath(Long attachmentId) {
		Attachment attachment = attachmentDao.getOne(attachmentId);
		Long attachmentListId = attachment.getAttachmentList().getId();
		String folderPath = findFolderPath(attachmentListId);
		Long contentId = attachment.getContent().getId();
		if (contentId == null) {
			throw new IllegalArgumentException("Content id is null. Not able to generate path for content of attachment : " + attachment.toString());
		}
		return folderPath + contentId;
	}

	/**
	 * Calculate the folder path for an attachment list.
	 * The path is defined as : the attachment list id.
	 * The attachment list id is padded to 12 chars to have a for level hierarchy
	 * ex : For an attachment list id of 9856 folder path will be
	 * /000/000/009/856
	 * @param attachmentListId the attachment list ID
	 * @return the path as specified above
	 */
	private String findFolderPath(long attachmentListId) {
		String id = String.valueOf(attachmentListId);
		String paddedId = StringUtils.leftPad(id, 12, "0");
		List<String> parts = new ArrayList<>();
		parts.add(paddedId.substring(0, 3));
		parts.add(paddedId.substring(3, 6));
		parts.add(paddedId.substring(6, 9));
		parts.add(paddedId.substring(9, 12));
		String path = StringUtils.join(parts, "/");
		path = storageConfigurer.getRepoPath() + path;
		return StringUtils.appendIfMissing(path, "/");
	}
}
