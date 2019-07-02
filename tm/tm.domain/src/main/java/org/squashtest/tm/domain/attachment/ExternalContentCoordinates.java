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
package org.squashtest.tm.domain.attachment;

/**
 * This class pairs together the attachment list id and the content id. Together they  help to locate a binary content
 * in an external repository (eg the {@link org.squashtest.tm.service.internal.attachment.FileSystemAttachmentRepository})
 *
 */
public class ExternalContentCoordinates {
	private Long attachmentListId;
	private Long contentId;

	public ExternalContentCoordinates(Long attachmentListId, Long contentId) {
		this.attachmentListId = attachmentListId;
		this.contentId = contentId;
	}

	public Long getAttachmentListId() {
		return attachmentListId;
	}

	public Long getContentId() {
		return contentId;
	}

}
