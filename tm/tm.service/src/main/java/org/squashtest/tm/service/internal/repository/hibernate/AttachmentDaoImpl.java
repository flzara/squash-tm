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
package org.squashtest.tm.service.internal.repository.hibernate;

import org.squashtest.tm.domain.attachment.Attachment;
import org.squashtest.tm.domain.attachment.AttachmentContent;
import org.squashtest.tm.service.internal.repository.CustomAttachmentDao;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

public class AttachmentDaoImpl implements CustomAttachmentDao {
	@PersistenceContext
	private EntityManager entityManager;
   
    @Override
    public void removeAttachment(Long attachmentId) {

		Attachment attachment = entityManager.find(Attachment.class, attachmentId);

	//[Issue 1456 problem with h2 database that will try to delete 2 times the same lob in lobs.db]
        AttachmentContent content = attachment.getContent();
        content.setContent(null);

		entityManager.flush();
	//End [Issue 1456]

		entityManager.remove(attachment);

    }

	@Override
	public void removeAll(List<Attachment> attachments) {
		for (Attachment attachment : attachments) {
			entityManager.remove(attachment);
}
	}

}
