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

import java.util.Set;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.tm.domain.attachment.Attachment;
import org.squashtest.tm.domain.campaign.CampaignLibrary;
import org.squashtest.tm.service.campaign.CampaignLibraryNavigationService;
import org.squashtest.tm.web.internal.controller.generic.ServiceAwareAttachmentTableModelHelper;

@Controller
@RequestMapping("/campaign-libraries/{libraryId}")
public class CampaignLibraryModificationController {
	@Inject
	private CampaignLibraryNavigationService campaignLibraryNavigationService;

	@Inject
	private ServiceAwareAttachmentTableModelHelper attachmentsHelper;

	@RequestMapping(method = RequestMethod.GET)
	public final ModelAndView showCampaignLibrary(@PathVariable long libraryId) {

		CampaignLibrary lib = campaignLibraryNavigationService.findLibrary(libraryId);

		ModelAndView mav = new ModelAndView("fragment/libraries/library");
		Set<Attachment> attachments = attachmentsHelper.findAttachments(lib);

		mav.addObject("library", lib);
		mav.addObject("attachments", attachments);
		mav.addObject("workspaceName", "campaign");


		return mav;
	}
}
