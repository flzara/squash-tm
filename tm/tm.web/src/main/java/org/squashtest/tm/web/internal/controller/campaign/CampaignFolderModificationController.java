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
package org.squashtest.tm.web.internal.controller.campaign;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.tm.domain.campaign.CampaignFolder;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.service.campaign.CampaignModificationService;
import org.squashtest.tm.service.library.FolderModificationService;
import org.squashtest.tm.service.statistics.campaign.ManyCampaignStatisticsBundle;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.controller.generic.FolderModificationController;
import org.squashtest.tm.web.internal.http.ContentTypes;

@Controller
@RequestMapping("/campaign-folders/{"+RequestParams.FOLDER_ID+"}")
public class CampaignFolderModificationController extends FolderModificationController<CampaignFolder> {


	private FolderModificationService<CampaignFolder> folderModificationService;

	/*
	 * We need this service to expose the statistics-provider methods for campaign folder.
	 *
	 * TODO : move this method to the folder modification service, once the small problem
	 * of having a specific method exposed in a generic class resolved.
	 *
	 * OR
	 *
	 * TODO : just inject the CampaignStatisticsService directly. But it is not secured yet (hence
	 * the need to channel this via another secured service).
	 */
	@Inject
	private CampaignModificationService campaignModificationService;

	@Override
	protected FolderModificationService<CampaignFolder> getFolderModificationService() {
		return folderModificationService;
	}

	@Inject @Named("squashtest.tm.service.CampaignFolderModificationService")
	public final void setFolderModificationService(FolderModificationService<CampaignFolder> folderModificationService) {
		this.folderModificationService = folderModificationService;
	}


	@Override
	@RequestMapping(method = RequestMethod.GET)
	public final ModelAndView showFolder(@PathVariable long folderId, HttpServletRequest request) {

		CampaignFolder folder = folderModificationService.findFolder(folderId);

		ModelAndView mav = super.showFolder(folderId, request);

		mav.setViewName("fragment/campaigns/campaign-folder");

		populateOptionalExecutionStatuses(folder, mav);

		return mav;
	}


	@Override
	protected String getWorkspaceName() {
		return "campaign";
	}

	// *************************** statistics ********************************

	// URL should have been /statistics, but that was already used by another method in this controller
	@ResponseBody
	@RequestMapping(value = "/dashboard-statistics", method = RequestMethod.GET, produces = ContentTypes.APPLICATION_JSON)
	public
 ManyCampaignStatisticsBundle getStatisticsAsJson(
			@PathVariable(RequestParams.FOLDER_ID) long folderId) {
		return campaignModificationService.gatherFolderStatisticsBundle(folderId);
	}

	@RequestMapping(value = "/dashboard", method = RequestMethod.GET, produces = ContentTypes.TEXT_HTML, params="printmode")
	public ModelAndView getDashboard(
			Model model,
			@PathVariable(RequestParams.FOLDER_ID) long folderId,
			@RequestParam(value="printmode", defaultValue="false") Boolean printmode) {

		CampaignFolder folder= folderModificationService.findFolder(folderId);

		ManyCampaignStatisticsBundle bundle = campaignModificationService.gatherFolderStatisticsBundle(folderId);

		ModelAndView mav = new ModelAndView("page/campaign-workspace/show-campaign-folder-dashboard");
		mav.addObject("folder", folder);
		mav.addObject("dashboardModel", bundle);
		mav.addObject("printmode", printmode);

		populateOptionalExecutionStatuses(folder, model);

		return mav;

	}

	private void populateOptionalExecutionStatuses(CampaignFolder folder, Model model){
		model.addAttribute("allowsSettled",
				folder.getProject().getCampaignLibrary().allowsStatus(ExecutionStatus.SETTLED));
		model.addAttribute("allowsUntestable",
				folder.getProject().getCampaignLibrary().allowsStatus(ExecutionStatus.UNTESTABLE));
	}

	private void populateOptionalExecutionStatuses(CampaignFolder folder, ModelAndView model){
		model.addObject("allowsSettled",
				folder.getProject().getCampaignLibrary().allowsStatus(ExecutionStatus.SETTLED));
		model.addObject("allowsUntestable",
				folder.getProject().getCampaignLibrary().allowsStatus(ExecutionStatus.UNTESTABLE));
	}

}
