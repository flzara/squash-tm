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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.attachment.Attachment;
import org.squashtest.tm.domain.attachment.AttachmentContent;
import org.squashtest.tm.domain.attachment.AttachmentHolder;
import org.squashtest.tm.domain.attachment.AttachmentList;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.attachment.AttachmentManagerService;
import org.squashtest.tm.service.attachment.RawAttachment;
import org.squashtest.tm.service.internal.repository.AttachmentContentDao;
import org.squashtest.tm.service.internal.repository.AttachmentDao;
import org.squashtest.tm.service.internal.repository.AttachmentListDao;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

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
@Transactional
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
	private AttachmentContentDao attachmentContentDao;


	@Inject
	private AttachmentRepository attachmentRepository;

	@Override
	public Long addAttachment(long attachmentListId, RawAttachment rawAttachment) throws IOException {
		AttachmentContent content = getAttachmentRepository().createContent(rawAttachment, attachmentListId);

		Attachment attachment = new Attachment();

		AttachmentList list = attachmentListDao.getOne(attachmentListId);
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
			return; // lists can't be shared, don't bother looking up requirement
		}

		RequirementVersion requirementVersion = attachmentListDao
			.findAssociatedRequirementVersionIfExists(attachmentListId);
	}

	private AttachmentRepository getAttachmentRepository() {
		return attachmentRepository;
	}

	@Override
	public Attachment findAttachment(Long attachmentId) {
		return findById(attachmentId);
	}

	private Attachment findById(Long attachmentId) {
		return attachmentDao.getOne(attachmentId);
	}

	@Override
	public Set<Attachment> findAttachments(Long attachmentListId) {
		return attachmentDao.findAllAttachments(attachmentListId);
	}

	@Override
	public void removeAttachmentFromList(long attachmentListId, long attachmentId) throws IOException {
     /*
		AttachmentList list = attachmentListDao.getOne(attachmentListId);
		Attachment attachment = attachmentDao.getOne(attachmentId);

		list.removeAttachment(attachment);
		attachmentDao.removeAttachment(attachment.getId());

		getAttachmentRepository().removeContent(attachmentId);
*/

		Attachment attachment = findAttachment(attachmentId);
		//save for FileSystemRepository
		Long attachmentContentId = attachment.getContent().getId();

		//	attachmentDao.removeAttachment(attachment.getId()); // !!! Not compatible TM 362
		attachmentDao.deleteById(attachment.getId());

		Set<Long> notOrpheanContent = attachmentContentDao.findNotOrpheanAttachmentContent(Collections.singletonList(attachmentContentId));
		if (!notOrpheanContent.contains(attachmentContentId)) {
			attachmentContentDao.deleteById(attachmentContentId);
		}
		if (attachmentRepository instanceof FileSystemAttachmentRepository) {
			attachmentRepository.removeContent(attachmentListId, attachmentContentId);
		}

		reindexBoundEntities(attachmentListId);
	}

	@Override
	public void removeListOfAttachments(long attachmentListId, List<Long> attachmentIds) throws IOException {
		for (Long attachmentId : attachmentIds) {
			removeAttachmentFromList(attachmentListId, attachmentId);
		}

/*
		Iterator<Attachment> iterAttach = attachmentListDao.getOne(attachmentListId).getAllAttachments().iterator();

		while (iterAttach.hasNext()) {
			Attachment att = iterAttach.next();
			ListIterator<Long> iterIds = attachmentIds.listIterator();

			while (iterIds.hasNext()) {
				Long id = iterIds.next();
				if (id.equals(att.getId())) {
					iterAttach.remove();
					iterIds.remove();
					getAttachmentRepository().removeContent(att.getId());
					attachmentDao.removeAttachment(att.getId());
					break;
				}
			}
			if (attachmentIds.isEmpty()) {
				break;
			}
		}
*/
		reindexBoundEntities(attachmentListId);

	}

	@Override
	public void renameAttachment(long attachmentId, String newName) {
		Attachment attachment = attachmentDao.getOne(attachmentId);
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
	 * OutputStream)
	 */
	@Override
	public void writeContent(long attachmentId, OutputStream outStream) throws IOException {
		InputStream is = getAttachmentRepository().getContentStream(attachmentId);

		int readByte;

		do {
			readByte = is.read();

			if (readByte != EOF) {
				outStream.write(readByte);
			}
		} while (readByte != EOF);

		is.close();

	}

	@Override
	public void copyContent(Attachment attachment) {
		getAttachmentRepository().copyContent(attachment);
	}

	@Override
	public void copyAttachments(AttachmentHolder attachmentHolder) {
		em.flush();
		AttachmentList attachmentList = attachmentHolder.getAttachmentList();
		for (Attachment attachment : attachmentList.getAllAttachments()) {
			copyContent(attachment);
		}
	}

	@Override
	public void cleanContent(List<Long> attachmentListIds) {
		getAttachmentRepository().deleteContent(attachmentListIds);
	}

	@Override
	public void cleanContent(AttachmentHolder attachmentHolder) {
		if (attachmentHolder != null && attachmentHolder.getAttachmentList() != null) {
			cleanContent(Collections.singletonList(attachmentHolder.getAttachmentList().getId()));
		}
	}

	@Override
	public Map<Long, Long> removeAttachmentsFromLists(List<Long> attachmentsListso) {
		//attachmentDao.
		return null;
	}

	@Override
	public void removeContent(long attachmentListId, long attachmentContentId) {
		if (attachmentRepository.getClass().getSimpleName().equals("FileSystemAttachmentRepository")) {
			removeContentFromFileSystem(attachmentListId, attachmentContentId);
		}
	}


	private void removeContentFromFileSystem(long attachmentListId, long attachmentContentId) {
		attachmentRepository.removeContent(attachmentListId, attachmentContentId);
	}

	@Override
	public List<Long[]> /*List<Object[]> */ getListIDbyContentIdForAttachmentLists(List<Long> attachmentsList) {
		List<Object[]> rawResult = attachmentContentDao.getListPairContentIDListIDFromAttachmentLists(attachmentsList);
		List<Long[]> result = new ArrayList<>();

		for (Object[] row:rawResult) {
			Long[] tab = new Long[2];
			tab[0] =(Long) row[0];
			tab[1] =(Long) row[1];
			result.add(tab);
		}
		return result;
	//return attachmentContentDao.getListPairContentIDListIDFromAttachmentLists(attachmentsList);
	}

	@Override
	public void deleteContents(List<Long[]> contentIdListIdList) {
		//remove Db Orpheans
		List<Long> contentIds = new ArrayList<>();
		//Long[] tab = new Long[2];

		int size = contentIdListIdList.size();
		for (int i = 0; i < size; i++) {
			Long[] tab = contentIdListIdList.get(i);
			contentIds.add(tab[0]);
		}

		// remove from FileSystem
		if (attachmentRepository.getClass().getSimpleName().equals("FileSystemAttachmentRepository")) {
			for (Long[] tab: contentIdListIdList) {
				removeContentFromFileSystem(tab[1], tab[0]);
			}
		}


			removeOrpheanAttachmentContents(contentIds);
//		}
	}

	private void removeOrpheanAttachmentContents (List<Long> contentIds) {
//		List<Long> ids = new ArrayList<>();
//		ids.addAll(contentIds);
		if (!contentIds.isEmpty()) {
			Set<Long> notOrpheans = attachmentContentDao.findNotOrpheanAttachmentContent(contentIds);
			contentIds.removeAll(notOrpheans);
			if (!contentIds.isEmpty()) {
				attachmentContentDao.removeOrpheanAttachmentContents(contentIds);
			}
		}
	}

	public void removeAttachmentsAndLists(List<Long> testStepAttachmentIds) {
		Set<Long> attachmentIds = attachmentDao.findAllAttachmentsFromLists(testStepAttachmentIds);
		attachmentDao.removeAllAttachments(attachmentIds);
		attachmentDao.removeAllAttachmentsLists(testStepAttachmentIds);
	}
}


