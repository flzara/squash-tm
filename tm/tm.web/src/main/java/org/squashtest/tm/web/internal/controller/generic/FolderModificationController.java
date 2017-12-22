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

import static org.squashtest.tm.web.internal.helper.JEditablePostParams.VALUE;

import java.util.Set;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.tm.domain.Workspace;
import org.squashtest.tm.domain.attachment.Attachment;
import org.squashtest.tm.domain.library.Folder;
import org.squashtest.tm.service.customreport.CustomReportDashboardService;
import org.squashtest.tm.service.library.FolderModificationService;
import org.squashtest.tm.web.internal.model.jquery.RenameModel;

public abstract class FolderModificationController<FOLDER extends Folder<?>> {

	@Inject
	private ServiceAwareAttachmentTableModelHelper attachmentsHelper;

	@Inject
	protected CustomReportDashboardService customReportDashboardService;

	@RequestMapping(method = RequestMethod.GET)
	public  ModelAndView showFolder(@PathVariable long folderId, HttpServletRequest request) {
		FOLDER folder = getFolderModificationService().findFolder(folderId);

		ModelAndView mav = new ModelAndView("fragment/generics/edit-folder");
		mav.addObject("folder", folder);
		mav.addObject("updateUrl", getUpdateUrl(request.getServletPath()  + StringUtils.defaultString(request.getPathInfo())));
		mav.addObject("workspaceName", getWorkspaceName());
		mav.addObject("attachments", findAttachments(folder));

		//favorite dashboard part
		Workspace workspace = Workspace.getWorkspaceFromShortName(getWorkspaceName());
		boolean shouldShowDashboard = customReportDashboardService.shouldShowFavoriteDashboardInWorkspace(workspace);
		boolean canShowDashboard = customReportDashboardService.canShowDashboardInWorkspace(workspace);

		mav.addObject("shouldShowDashboard",shouldShowDashboard);
		mav.addObject("canShowDashboard", canShowDashboard);

		return mav;
	}

	protected abstract FolderModificationService<FOLDER> getFolderModificationService();
	protected abstract String getWorkspaceName();


	//might look like a bit of overhead but our class is now testable.
	protected Set<Attachment> findAttachments(FOLDER folder){
		return attachmentsHelper.findAttachments(folder);
	}

	@ResponseBody
	@RequestMapping(method = RequestMethod.DELETE)
	public
	String removeFolder(@PathVariable long folderId) {

		getFolderModificationService().removeFolder(folderId);
		return "ok";

	}

	@ResponseBody
	@RequestMapping(method = RequestMethod.POST, params = {"newName"})
	public
	Object renameFolder(@RequestParam("newName") String newName,
						@PathVariable long folderId) {

		getFolderModificationService().renameFolder(folderId, newName);
		return new RenameModel(newName);

	}

	@ResponseBody
	@RequestMapping(method = RequestMethod.POST, params = {"id", VALUE})
	public
	String updateDescription(@PathVariable long folderId, @RequestParam(VALUE) String newDescription) {
		getFolderModificationService().updateFolderDescription(folderId, newDescription);
		return newDescription;
	}

	/***
	 * This method clean the path info from useless characters and returns only the raw part, like
	 * requirement-folders...
	 *
	 * @param pathInfo
	 *            the original pathInfo from request
	 * @return the cleaned path (String)
	 */
	private String getUpdateUrl(String pathInfo) {
		// remove first slash
		String toReturn = pathInfo.substring(1);
		// detect the last one...
		int lastSlash = toReturn.lastIndexOf('/');
		return toReturn.substring(0, lastSlash);
	}

}
