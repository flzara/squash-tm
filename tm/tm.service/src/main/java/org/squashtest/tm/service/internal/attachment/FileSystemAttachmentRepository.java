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
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
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

@Component
public class FileSystemAttachmentRepository implements AttachmentRepository {

	private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemAttachmentRepository.class);

	@Inject
	private Environment env;

	@PersistenceContext
	private EntityManager entityManager;

	@Inject
	private AttachmentDao attachmentDao;

	private String repoPath;

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

	private String getAttachmentPath(Long attachmentId) {
		Attachment attachment = attachmentDao.findOne(attachmentId);
		Long attachmentListId = attachment.getAttachmentList().getId();
		String folderPath = findFolderPath(attachmentListId);
		Long contentId = attachment.getContent().getId();
		return folderPath + contentId;
	}

	@Override
	public void removeContent(long attachmentId) throws IOException {
		String path = getAttachmentPath(attachmentId);
		Files.delete(Paths.get(path));
	}

	private String findFolderPath(long attachmentListId) {
		String id = String.valueOf(attachmentListId);
		String paddedId = StringUtils.leftPad(id, 12, "0");
		List<String> parts = new ArrayList<>();
		parts.add(paddedId.substring(0, 3));
		parts.add(paddedId.substring(3, 6));
		parts.add(paddedId.substring(6, 9));
		parts.add(paddedId.substring(9, 12));
		String path = StringUtils.join(parts, "/");
		path = repoPath + path;
		return StringUtils.appendIfMissing(path, "/");
	}

	@PostConstruct
	public void init(){
		repoPath = env.getRequiredProperty("squash.path.file.repository");
		repoPath = StringUtils.appendIfMissing(repoPath, "/");
		LOGGER.info("Squash File Repository configured to : " + repoPath);
	}
}
