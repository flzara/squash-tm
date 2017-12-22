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
package org.squashtest.tm.web.internal.controller.generic;

import java.util.Set;

import javax.inject.Inject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.attachment.Attachment;
import org.squashtest.tm.domain.attachment.AttachmentHolder;
import org.squashtest.tm.service.attachment.AttachmentFinderService;
import org.squashtest.tm.web.internal.controller.attachment.AttachmentsTableModelHelper;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;

@Component
public class ServiceAwareAttachmentTableModelHelper {

	@Inject
	private InternationalizationHelper i18nHelper;
	
	@Inject
	private AttachmentFinderService attachmentFinderService;
	
	
	public DataTableModel findPagedAttachments(AttachmentHolder holder){
            Pageable pageable = new PageRequest(0, 50, new Sort("Attachment.name"));
            return findPagedAttachments(holder, pageable, "");
	}
	
	public DataTableModel findPagedAttachments(AttachmentHolder holder, Pageable pageable, String sEcho){
            Page<Attachment> attachments = attachmentFinderService.findPagedAttachments(holder, pageable);
            return new AttachmentsTableModelHelper(i18nHelper).buildDataModel(attachments, sEcho);
	}
	
	public DataTableModel findPagedAttachments(long attachmentListId){
            Pageable pageable = new PageRequest(0, 50, new Sort("Attachment.name"));
            return findPagedAttachments(attachmentListId, pageable, "");
	}
	
	public DataTableModel findPagedAttachments(long attachmentListId, Pageable pageable, String sEcho){
            Page<Attachment> attachments = attachmentFinderService.findPagedAttachments(attachmentListId, pageable);
            return new AttachmentsTableModelHelper(i18nHelper).buildDataModel(attachments, sEcho);		
	}
	
	public Set<Attachment> findAttachments(AttachmentHolder holder){
		return attachmentFinderService.findAttachments(holder.getAttachmentList().getId());
	}
	
	public Set<Attachment> findAttachments(long attachmentListId){
		return attachmentFinderService.findAttachments(attachmentListId);
	}
	
}
