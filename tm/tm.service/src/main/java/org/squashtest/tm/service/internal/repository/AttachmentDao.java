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
package org.squashtest.tm.service.internal.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.squashtest.tm.domain.attachment.Attachment;
import org.squashtest.tm.domain.attachment.ExternalContentCoordinates;

import java.util.List;
import java.util.Set;

public interface AttachmentDao extends JpaRepository<Attachment, Long>, CustomAttachmentDao {

	/**
	 * Returns all the attachments that belong to the given AttachmentList
	 */
	@Query
	Set<Attachment> findAllByListId(@Param("id") Long attachmentListId);

	/**
	 * Same than above, paged version.
	 *
	 */

	@Query("select Attachment from AttachmentList AttachmentList join AttachmentList.attachments Attachment where AttachmentList.id = :id")
	Page<Attachment> findAllAttachmentsPagined(@Param("id") Long attachmentListId, Pageable pageable);

	@Query("select Attachment.id from AttachmentList AttachmentList join AttachmentList.attachments Attachment where AttachmentList.id in (:ids)")
	Set<Long> findAllAttachmentsFromLists(@Param("ids") List<Long> attachmentLists);

	@Modifying
	@Query("delete Attachment at where at.id in (:ids)")
	void removeAllAttachments(@Param("ids") Set<Long> attachments);


	@Modifying
	@Query("delete AttachmentList al where al.id in (:ids)")
	void removeAllAttachmentsLists(@Param("ids") List<Long> attachmentLists);

	@Query("select ListAttachment.id from RequirementFolder RequirementFolder inner join RequirementFolder.resource Resource" +
		" inner join Resource.attachmentList ListAttachment where RequirementFolder.id in (:ids)")
	List<Long> findAttachmentsListsFromRequirementFolder(@Param("ids") List<Long> requirementLibraryNodeIds);

	@Query("select new org.squashtest.tm.domain.attachment.ExternalContentCoordinates(v.attachmentList.id,Attachment.content.id) from RequirementVersion v inner join  v.attachmentList.attachments Attachment where v.id in (:ids)")
	List<ExternalContentCoordinates> getListPairContentIDListIDForRequirementVersions(@Param("ids") List<Long> requirementVersionIds);

	@Query("select new org.squashtest.tm.domain.attachment.ExternalContentCoordinates(Attachment.attachmentList.id,Attachment.content.id) from ExecutionStep exec inner join  exec.attachmentList attachmentList inner join attachmentList.attachments Attachment where exec.id in (:ids)")
	List<ExternalContentCoordinates> getListPairContentIDListIDForExecutionSteps(@Param("ids") List<Long> executionStepsIds);
}
