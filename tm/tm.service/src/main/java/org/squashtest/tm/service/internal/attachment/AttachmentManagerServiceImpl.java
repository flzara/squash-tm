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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.squashtest.tm.domain.attachment.Attachment;
import org.squashtest.tm.domain.attachment.AttachmentContent;
import org.squashtest.tm.domain.attachment.AttachmentHolder;
import org.squashtest.tm.domain.attachment.AttachmentList;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.advancedsearch.IndexationService;
import org.squashtest.tm.service.attachment.AttachmentManagerService;
import org.squashtest.tm.service.attachment.RawAttachment;
import org.squashtest.tm.service.internal.repository.AttachmentDao;
import org.squashtest.tm.service.internal.repository.AttachmentListDao;

/*
 * FIXME !
 *
 * we can't secure the operations on attachments yet, because they delegate the security permissions
 * to the owning entity. The problem being that the ownership of an AttachmentList is one way : from the
 * entity to the list.
 *
 * Therefore we can't perform a permission check using the AttachmentList id alone, one would need to fetch
 * the owning entity back first. That would require additional work Dao-side.
 *
 * See task #102 on ci.squashtest.org/mantis
 *
 */
@Service("squashtest.tm.service.AttachmentManagerService")
public class AttachmentManagerServiceImpl implements AttachmentManagerService {
	/**
	 *
	 */
	private static final int EOF = -1;

	@PersistenceContext
	private EntityManager em;

	@Inject
	private AttachmentDao attachmentDao;

	@Inject
	private AttachmentListDao attachmentListDao;

	@Inject
	private IndexationService indexationService;

	@Override
	public Long addAttachment(long attachmentListId, RawAttachment rawAttachment) {
		AttachmentContent content = new AttachmentContent();

		Blob blob = currentSession().getLobHelper().createBlob(rawAttachment.getStream(),
				rawAttachment.getSizeInBytes());
		content.setContent(blob);
		currentSession().persist(content);

		Attachment attachment = new Attachment();

		AttachmentList list = attachmentListDao.findOne(attachmentListId);
		list.addAttachment(attachment);

		attachment.setContent(content);
		attachment.setAddedOn(new Date());
		attachment.setName(rawAttachment.getName());
		attachment.setSize(rawAttachment.getSizeInBytes());
		attachmentDao.save(attachment);


		reindexBoundEntities(attachmentListId);

		return attachment.getId();
	}

	private void reindexBoundEntities(long attachmentListId) {
		TestCase testCase = attachmentListDao.findAssociatedTestCaseIfExists(attachmentListId);
		if (testCase != null) {
			indexationService.reindexTestCase(testCase.getId());
			return; // lists can't be shared, don't bother looking up requirement
		}

		RequirementVersion requirementVersion = attachmentListDao
				.findAssociatedRequirementVersionIfExists(attachmentListId);
		if (requirementVersion != null) {
			indexationService.reindexRequirementVersion(requirementVersion.getId());
		}
	}

	private Session currentSession() throws HibernateException {
		return em.unwrap(Session.class);
	}

	@Override
	public Attachment findAttachment(Long attachmentId) {
		return findById(attachmentId);
	}

	private Attachment findById(Long attachmentId) {
		return attachmentDao.findOne(attachmentId);
	}

	@Override
	public Set<Attachment> findAttachments(Long attachmentListId) {
		return attachmentDao.findAllAttachments(attachmentListId);
	}

	@Override
	public void removeAttachmentFromList(long attachmentListId, long attachmentId) {
		AttachmentList list = attachmentListDao.findOne(attachmentListId);
		Attachment attachment = attachmentDao.findOne(attachmentId);

		list.removeAttachment(attachment);
		attachmentDao.removeAttachment(attachment.getId());

		reindexBoundEntities(attachmentListId);
	}

	@Override
	public void removeListOfAttachments(long attachmentListId, List<Long> attachmentIds) {

		Iterator<Attachment> iterAttach = attachmentListDao.findOne(attachmentListId).getAllAttachments().iterator();

		while (iterAttach.hasNext()) {
			Attachment att = iterAttach.next();
			ListIterator<Long> iterIds = attachmentIds.listIterator();

			while (iterIds.hasNext()) {
				Long id = iterIds.next();
				if (id.equals(att.getId())) {
					iterAttach.remove();
					iterIds.remove();
					attachmentDao.removeAttachment(att.getId());
					break;
				}
			}
			if (attachmentIds.isEmpty()) {
				break;
			}
		}

		reindexBoundEntities(attachmentListId);

	}

	@Override
	public void renameAttachment(long attachmentId, String newName) {
		Attachment attachment = attachmentDao.findOne(attachmentId);
		attachment.setShortName(newName);
	}

	@Override
	public String findAttachmentShortName(Long attachmentId) {
		Attachment attachment = findById(attachmentId);
		return attachment.getShortName();
	}

	@Override
	public Page<Attachment> findPagedAttachments(long attachmentListId, Pageable pageable) {
		return attachmentDao.findAllAttachmentsPagined(attachmentListId, pageable);
	}

	@Override
	public Page<Attachment> findPagedAttachments(AttachmentHolder attached, Pageable pas) {
		return findPagedAttachments(attached.getAttachmentList().getId(), pas);
	}

	/**
	 * @see org.squashtest.tm.service.attachment.AttachmentManagerService#writeContent(long,
	 *      javax.servlet.ServletOutputStream)
	 */
	@Override
	public void writeContent(long attachmentId, OutputStream outStream) throws IOException {
		InputStream is = findById(attachmentId).getContent().getStream();

		int readByte;

		do {
			readByte = is.read();

			if (readByte != EOF) {
				outStream.write(readByte);
			}
		} while (readByte != EOF);

	}

}
