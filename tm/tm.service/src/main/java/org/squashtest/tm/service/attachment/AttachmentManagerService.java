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
package org.squashtest.tm.service.attachment;

import org.squashtest.tm.domain.attachment.Attachment;
import org.squashtest.tm.domain.attachment.AttachmentHolder;
import org.squashtest.tm.domain.attachment.AttachmentList;
import org.squashtest.tm.domain.attachment.ExternalContentCoordinates;
import org.squashtest.tm.domain.execution.ExecutionStep;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;

public interface AttachmentManagerService extends AttachmentFinderService {
	/**
	 * @param attachmentListId
	 * @param rawAttachment
	 * @return the ID of the newly created Attachment
	 */
	Long addAttachment(long attachmentListId, RawAttachment rawAttachment) throws IOException;

	void removeAttachmentFromList(long attachmentListId, long attachmentId) throws IOException;

	void removeListOfAttachments(long attachmentListId, List<Long> attachmentIds) throws IOException;

	void renameAttachment(long attachmentId, String newName);	
	/**
	 * Writes attachment content into the given stream.
	 * @param attachmentId
	 * @param os
	 * @throws IOException
	 */
	void writeContent(long attachmentId, OutputStream os) throws IOException;

	/**
	 * Copy content. Should only be used in case of file repository.
	 * Our nice rich domain model should do the copy when needed if database repo
	 * but in case of file repository we need service to do the stuff so....
	 * @param attachment the COPY not the source !!!. The source id is embedded as @Transient attribute in the attachment by the model at copy time.
	 *                   See {@link Attachment#attachmentToCopyId}
	 */
	void copyContent(Attachment attachment);

	void copyContentsOnExternalRepository(AttachmentHolder attachmentHolder);

	/**
	 * Remove the EXTERNAL content of an list of {@link AttachmentList}. It DO NOT REMOVE DATA FROM DATABASE
	 * It's a noop for intern storage, hibernate clean blobs automatically.
	 * However, for file system storage we need to do it manually...
	 * @param attachmentListIds Ids of the attachments list to delete
	 */
	void cleanContent(List<Long> attachmentListIds);

    void removeContent(long attachmentListId, long attachmentContentId);

	List<ExternalContentCoordinates>  getListIDbyContentIdForAttachmentLists(List<Long> attachmentsList);

	void deleteContents(List<ExternalContentCoordinates> ContentIListId);

	void removeAttachmentsAndLists(List<Long> AttachmentListIds);

	List<Long> getAttachmentsListsFromRequirementFolders(List<Long> requirementLibraryNodeIds);

	List<ExternalContentCoordinates> getListPairContentIDListIDForRequirementVersions(List<Long> requirementVersionIds);

	List<ExternalContentCoordinates> getListPairContentIDListIDForExecutionSteps(Collection<ExecutionStep> executionSteps);

}
