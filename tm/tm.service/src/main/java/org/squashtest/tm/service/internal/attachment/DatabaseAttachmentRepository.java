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

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.attachment.Attachment;
import org.squashtest.tm.domain.attachment.AttachmentContent;
import org.squashtest.tm.service.attachment.RawAttachment;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Blob;
import java.util.List;

@Component("databaseAttachmentRepository")
@ConditionalOnProperty(name = "squashtm.feature.file.repository", havingValue = "false", matchIfMissing = true)
@Transactional
public class DatabaseAttachmentRepository implements AttachmentRepository {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public AttachmentContent createContent(RawAttachment rawAttachment, long attachmentListId) {
		AttachmentContent content = new AttachmentContent();
		Blob blob = currentSession().getLobHelper().createBlob(rawAttachment.getStream(),
			rawAttachment.getSizeInBytes());
		content.setContent(blob);
		currentSession().persist(content);
		return content;
	}

	@Override
	public InputStream getContentStream(Long attachmentId) throws FileNotFoundException {
		Attachment attachment = entityManager.find(Attachment.class, attachmentId);
		return attachment.getContent().getStream();
	}

	@Override
	public void removeContent(long attachmentId) {
		//NOOP Hibernate took care of deleting the blob
	}

	@Override
	public void copyContent(Attachment copy) {
		//NOOP Hibernate took care of copy the blob
	}

	@Override
	public void deleteContent(List<Long> attachmentListIds) {
		//NOOP Hibernate took care of deleting all lists contents
	}

	private Session currentSession() throws HibernateException {
		return entityManager.unwrap(Session.class);
	}
}
