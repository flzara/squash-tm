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

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface AttachmentManagerService extends AttachmentFinderService {
	/**
	 * @param attachmentListId
	 * @param rawAttachment
	 * @return the ID of the newly created Attachment
	 */
	Long addAttachment(long attachmentListId, RawAttachment rawAttachment);

	void removeAttachmentFromList(long attachmentListId, long attachmentId);

	void removeListOfAttachments(long attachmentListId, List<Long> attachmentIds);

	void renameAttachment(long attachmentId, String newName);

	/**
	 * Writes attachment content into the given stream.
	 * @param attachmentId
	 * @param os
	 * @throws IOException
	 */
	void writeContent(long attachmentId, OutputStream os) throws IOException;
}
