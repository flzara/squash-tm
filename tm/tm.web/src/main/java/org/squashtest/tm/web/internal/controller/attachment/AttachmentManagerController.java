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
package org.squashtest.tm.web.internal.controller.attachment;

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.tm.domain.attachment.Attachment;
import org.squashtest.tm.service.attachment.AttachmentManagerService;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.controller.generic.ServiceAwareAttachmentTableModelHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.datatable.SpringPagination;
import org.squashtest.tm.web.internal.model.jquery.RenameModel;
import org.squashtest.tm.web.internal.model.viewmapper.DatatableMapper;
import org.squashtest.tm.web.internal.model.viewmapper.NameBasedMapper;

@Controller
@RequestMapping("/attach-list/{attachListId}/attachments")
public class AttachmentManagerController {


	private static final Logger LOGGER = LoggerFactory.getLogger(AttachmentManagerController.class);

	@Inject
	private AttachmentManagerService attachmentManagerService;

	@Inject
	private ServiceAwareAttachmentTableModelHelper attachmentModelHelper;

	private static final String ATTACH_LIST_ID = "attachListId";


	@SuppressWarnings("rawtypes")
	private final DatatableMapper attachmentMapper = new NameBasedMapper()
	.mapAttribute("item-id", "name" , Attachment.class)
	.mapAttribute("hyphenated-name", "name", Attachment.class)
	.mapAttribute("size", "contentSize", Attachment.class)
	.mapAttribute("added-on", "addedOn", Attachment.class);




	/* ********************** data display *********************************** */



	@RequestMapping(value = "/manager", method = RequestMethod.GET)
	public ModelAndView showAttachmentManager(@PathVariable(ATTACH_LIST_ID) long attachListId,
			@RequestParam("workspace") String workspace, @RequestParam(value = "open",required = false, defaultValue = "false") boolean openNewWindow) {

		ModelAndView mav = new ModelAndView("page/attachments/attachment-manager");
		mav.addObject("workspace", workspace);
		mav.addObject(ATTACH_LIST_ID, attachListId);
		mav.addObject("attachmentsModel", attachmentModelHelper.findPagedAttachments(attachListId));
		mav.addObject("openNewWindow", openNewWindow);

		return mav;

	}

	@ResponseBody
	@RequestMapping(value = "/details", method = RequestMethod.GET)
	public
	DataTableModel displayAttachmentDetails(@PathVariable(ATTACH_LIST_ID) long attachListId, final DataTableDrawParameters params) {
            Pageable pageable = SpringPagination.pageable(params, attachmentMapper);
            return attachmentModelHelper.findPagedAttachments(attachListId, pageable, params.getsEcho());

	}


	/* ******************************* delete *********************************** */

	@RequestMapping(value = "/{attachmentIds}", method = RequestMethod.DELETE)
	@ResponseBody
	public void removeAttachment(@PathVariable long attachListId, @PathVariable("attachmentIds") List<Long> ids) {
		attachmentManagerService.removeListOfAttachments(attachListId, ids);
	}

	/* ******************************* modify *********************************** */


	@RequestMapping(value="/{attachmentId}/name",method = RequestMethod.POST, params = { RequestParams.NAME })
	@ResponseBody
	public Object renameAttachment(@PathVariable long attachmentId, @RequestParam(RequestParams.NAME) String newName) {

		attachmentManagerService.renameAttachment(attachmentId, newName);
		LOGGER.info("AttachmentController : renaming attachment " + attachmentId + " as " + newName);
		return new RenameModel(newName);

	}

}
