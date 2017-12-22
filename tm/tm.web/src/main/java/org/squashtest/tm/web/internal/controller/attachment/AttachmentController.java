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

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.tm.domain.attachment.Attachment;
import org.squashtest.tm.service.attachment.AttachmentManagerService;
import org.squashtest.tm.web.internal.fileupload.UploadContentFilterUtil;
import org.squashtest.tm.web.internal.fileupload.UploadSummary;

@Controller
@RequestMapping("/attach-list/{attachListId}/attachments")
public class AttachmentController {

	private static final String UPLOAD_URL = "/upload";
	private static final Logger LOGGER = LoggerFactory.getLogger(AttachmentController.class);

	@Inject
	private AttachmentManagerService attachmentManagerService;

	@Inject
	private MessageSource messageSource;

	@Inject
	private UploadContentFilterUtil filterUtil;

	private static final String STR_UPLOAD_STATUS_OK = "dialog.attachment.summary.statusok.label";
	private static final String STR_UPLOAD_STATUS_WRONGFILETYPE = "dialog.attachment.summary.statuswrongtype.label";

	@InitBinder
	public void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws ServletException {
		binder.registerCustomEditor(UploadedData.class, new UploadedDataPropertyEditorSupport());
	}

	/* ****************************** attachments *************************************** */

	@RequestMapping(value = "/display", method = RequestMethod.GET)
	public ModelAndView displayAttachments(@PathVariable("attachListId") long attachListId) {
		Set<Attachment> attachmentSet = attachmentManagerService.findAttachments(attachListId);

		ModelAndView mav = new ModelAndView("fragment/attachments/attachment-display");
		mav.addObject("attachmentSet", attachmentSet);
		mav.addObject("attachListId", attachListId);

		return mav;
	}


	// for IE (again), post from the regular popup : needs to return the response wrapped in some html
	@RequestMapping(value = UPLOAD_URL, method = RequestMethod.POST, produces="text/html")
	public String uploadAttachmentAsHtml(@RequestParam("attachment[]") List<UploadedData> attachments, @PathVariable long attachListId, Locale locale, Model model) throws IOException{
		List<UploadedData> nonEmpty = removeEmptyData(attachments);
		List<UploadSummary> summaries = uploadAttachmentAsJson(nonEmpty, attachListId, locale);
		model.addAttribute("summary" , summaries);
		return "fragment/import/upload-summary";
	}


	/* *********************************** upload ************************************** */


	// uploads the file themselves and build the upload summary on the fly
	@RequestMapping(value = UPLOAD_URL, method = RequestMethod.POST, produces="application/json")
	@ResponseBody
	public List<UploadSummary> uploadAttachmentAsJson(@RequestParam("attachment[]") List<UploadedData> attachments, @PathVariable long attachListId, Locale locale)
			throws IOException {

		List<UploadSummary> summary = new LinkedList<>();

		for (UploadedData upload : attachments) {

			LOGGER.trace("AttachmentController : adding attachment " + upload.getName());

			// file type checking
			boolean shouldProceed = filterUtil.isTypeAllowed(upload);
			if (!shouldProceed) {
				summary.add(new UploadSummary(upload.getName(), getUploadSummary(STR_UPLOAD_STATUS_WRONGFILETYPE, locale),
						UploadSummary.INT_UPLOAD_STATUS_WRONGFILETYPE));
			} else {
				attachmentManagerService.addAttachment(attachListId, upload);

				summary.add(new UploadSummary(upload.getName(), getUploadSummary(STR_UPLOAD_STATUS_OK, locale),
						UploadSummary.INT_UPLOAD_STATUS_OK));
			}
		}

		// by design the last file uploaded is empty and has no name. We'll strip that from the summary.
		summary = stripEmptySummary(summary);

		// now we can return
		return summary;
	}


	// by design the last file uploaded is empty and has no name. We'll strip that from the summary.
	private List<UploadSummary> stripEmptySummary(List<UploadSummary> summary) {
		int totalAttachment = summary.size();
		if (totalAttachment > 0 && summary.get(totalAttachment - 1).getName().isEmpty()) {
			summary.remove(totalAttachment - 1);
		}
		return summary;
	}

	private List<UploadedData> removeEmptyData(List<UploadedData> all){
		List<UploadedData> nonEmpty = new ArrayList<>();
		for (UploadedData dat : all){
			if (dat.getSizeInBytes() > 0){
				nonEmpty.add(dat);
			}
		}
		return nonEmpty;
	}

	/* ***************************** download ************************************* */

	@ResponseBody
	@RequestMapping(value = "/download/{attachemendId}", method = RequestMethod.GET)
	public
	void downloadAttachment(@PathVariable("attachemendId") long attachmentId, HttpServletResponse response) {

		try {
			Attachment attachment = attachmentManagerService.findAttachment(attachmentId);
			response.setContentType("application/octet-stream");
			response.setHeader("Content-Disposition", "attachment; filename=" + attachment.getName().replace(" ", "_"));

			ServletOutputStream outStream = response.getOutputStream();

			attachmentManagerService.writeContent(attachmentId, outStream);
		} catch (IOException e) {
			LOGGER.warn("Error happened during attachment download : " + e.getMessage(), e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

		}
	}

	private String getUploadSummary(String key, Locale locale) {
		return messageSource.getMessage(key, null, locale);
	}

}
