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

import org.jooq.Record2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.attachment.*;
import org.squashtest.tm.domain.audit.AuditableMixin;
import org.squashtest.tm.domain.execution.ExecutionStep;
import org.squashtest.tm.security.UserContextHolder;
import org.squashtest.tm.service.attachment.AttachmentManagerService;
import org.squashtest.tm.service.attachment.RawAttachment;
import org.squashtest.tm.service.internal.repository.AttachmentContentDao;
import org.squashtest.tm.service.internal.repository.AttachmentDao;
import org.squashtest.tm.service.internal.repository.AttachmentListDao;
import org.squashtest.tm.service.internal.repository.CampaignDao;
import org.squashtest.tm.service.internal.repository.RequirementVersionDao;
import org.squashtest.tm.service.internal.repository.TestCaseDao;

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

	@Inject
	private TestCaseDao testCaseDao;

	@Inject
	private CampaignDao campaignDao;

	@Inject
	private RequirementVersionDao requirementVersionDao;

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

		updateAuditableRelatedEntity(attachmentListId);

		return attachment.getId();
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
		return attachmentDao.findAllByListId(attachmentListId);
	}

	@Override
	public void removeAttachmentFromList(long attachmentListId, long attachmentId) throws IOException {
		Attachment attachment = findAttachment(attachmentId);
		//save for FileSystemRepository
		Long attachmentContentId = attachment.getContent().getId();

		attachmentDao.deleteById(attachment.getId());
		ExternalContentCoordinates externalContentCoordinates = new ExternalContentCoordinates(attachmentListId, attachmentContentId);
		deleteContents(Collections.singletonList(externalContentCoordinates));

		updateAuditableRelatedEntity(attachmentListId);
	}

	@Override
	public void removeListOfAttachments(long attachmentListId, List<Long> attachmentIds) throws IOException {
		for (Long attachmentId : attachmentIds) {
			removeAttachmentFromList(attachmentListId, attachmentId);
		}
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
	public void copyContentsOnExternalRepository(AttachmentHolder attachmentHolder) {
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
	public void removeContent(long attachmentListId, long attachmentContentId) {
		if (attachmentRepository.getClass().getSimpleName().equals("FileSystemAttachmentRepository")) {
			removeContentFromFileSystem(attachmentListId, attachmentContentId);
		}
	}


	private void removeContentFromFileSystem(long attachmentListId, long attachmentContentId) {
		attachmentRepository.removeContent(attachmentListId, attachmentContentId);
	}

	@Override
	public List<ExternalContentCoordinates> getListIDbyContentIdForAttachmentLists(List<Long> attachmentsList) {
		return attachmentContentDao.getListPairContentIDListIDFromAttachmentLists(attachmentsList);
	}

	@Override
	public void deleteContents(List<ExternalContentCoordinates> contentIdListIdList) {
		List<Long> contentIds = new ArrayList<>();

		for (ExternalContentCoordinates coord: contentIdListIdList) {
			contentIds.add(coord.getContentId());
		}

		//remove Db Orphans
		removeOrphanAttachmentContents(contentIds);

		// remove from FileSystem
		if (attachmentRepository.getClass().getSimpleName().contains("FileSystemAttachmentRepository")) {
			for (ExternalContentCoordinates externalCoord:contentIdListIdList) {
				removeContentFromFileSystem(externalCoord.getAttachmentListId(), externalCoord.getContentId());
			}
		}
	}

	private void removeOrphanAttachmentContents (List<Long> contentIds) {
		if (!contentIds.isEmpty()) {
			Set<Long> notOrphans = attachmentContentDao.findNotOrphanAttachmentContent(contentIds);
			contentIds.removeAll(notOrphans);
			if (!contentIds.isEmpty()) {
				attachmentContentDao.deleteByIds(contentIds);
			}
		}
	}

	@Override
	public void removeAttachmentsAndLists(List<Long> attachmentListIds) {
		if (!attachmentListIds.isEmpty()) {
			Set<Long> attachmentIds = attachmentDao.findAllAttachmentsFromLists(attachmentListIds);
			if (!attachmentIds.isEmpty()) {
				attachmentDao.removeAllAttachments(attachmentIds);
			}
			attachmentDao.removeAllAttachmentsLists(attachmentListIds);
		}
	}

	@Override
	public List<Long> getAttachmentsListsFromRequirementFolders(List<Long> requirementLibraryNodeIds) {
		return attachmentDao.findAttachmentsListsFromRequirementFolder(requirementLibraryNodeIds);
	}

	@Override
	public List<ExternalContentCoordinates> getListPairContentIDListIDForRequirementVersions(List<Long> requirementVersionIds) {
		return attachmentDao.getListPairContentIDListIDForRequirementVersions(requirementVersionIds);
	}

	@Override
	public List<ExternalContentCoordinates> getListPairContentIDListIDForExecutionSteps(Collection<ExecutionStep> executionSteps) {
		List<Long> executionStepsIds = new ArrayList<>();
		for(ExecutionStep executionStep:executionSteps) {
			executionStepsIds.add(executionStep.getId());
		}
		return attachmentDao.getListPairContentIDListIDForExecutionSteps(executionStepsIds);
	}

	private List<Long[]> constructList(Long contentID, Long ListId) {
		List<Long[]> result = new ArrayList<>();
		Long[] tab = new Long[2];
		tab[0] = contentID;
		tab[1] = ListId;
		result.add(tab);
		return result;
	}

	/**
	 * Update last modified on and las modified by attribute of related auditable (for now test case, campaign or requirement version)
	 * @param attachmentListId the modified attachment list id
	 */
	private void updateAuditableRelatedEntity(long attachmentListId){
		Record2<String, Long> result = attachmentListDao.findAuditableAssociatedEntityIfExists(attachmentListId);

		if(result != null){
			String entityName = result.get("entity_name", String.class);
			long entityId = result.get("entity_id", Long.class);

			AuditableMixin auditable = null;
			switch (entityName){
				case "test_case":
					auditable = (AuditableMixin) testCaseDao.findById(entityId);
					break;
				case "campaign":
					auditable = (AuditableMixin) campaignDao.findById(entityId);
					break;
				case "requirement_version":
					auditable = (AuditableMixin) requirementVersionDao.findById(entityId).get();
					break;
			}
			if(auditable != null){
				auditable.setLastModifiedOn(new Date());
				auditable.setLastModifiedBy(UserContextHolder.getUsername());
			}
		}
	}
}


