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
package org.squashtest.tm.web.internal.controller.testcase;

import java.util.Set;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.tm.domain.Workspace;
import org.squashtest.tm.domain.attachment.Attachment;
import org.squashtest.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.tm.service.customreport.CustomReportDashboardService;
import org.squashtest.tm.service.testcase.TestCaseLibraryNavigationService;
import org.squashtest.tm.web.internal.controller.generic.ServiceAwareAttachmentTableModelHelper;

@Controller
@RequestMapping("/test-case-libraries/{libraryId}")
public class TestCaseLibraryModificationController {

	@Inject
	private TestCaseLibraryNavigationService service;

	@Inject
	private ServiceAwareAttachmentTableModelHelper attachmentsHelper;

	@Inject
	private CustomReportDashboardService customReportDashboardService;

	@RequestMapping(method = RequestMethod.GET)
	public final ModelAndView showTestCaseLibrary(@PathVariable long libraryId) {

		TestCaseLibrary lib = service.findLibrary(libraryId);

		ModelAndView mav = new ModelAndView("fragment/test-cases/test-case-library");
		Set<Attachment> attachments = attachmentsHelper.findAttachments(lib);

		mav.addObject("library", lib);
		mav.addObject("attachments", attachments);
		mav.addObject("workspaceName", "test-case");

		boolean shouldShowDashboard = customReportDashboardService.shouldShowFavoriteDashboardInWorkspace(Workspace.TEST_CASE);
		boolean canShowDashboard = customReportDashboardService.canShowDashboardInWorkspace(Workspace.TEST_CASE);

		mav.addObject("shouldShowDashboard",shouldShowDashboard);
		mav.addObject("canShowDashboard", canShowDashboard);

		return mav;
	}

	@RequestMapping(value = "/info", method = RequestMethod.GET)
	public final ModelAndView showTestCaseLibraryInfo(@PathVariable long libraryId){
		TestCaseLibrary lib = service.findLibrary(libraryId);

		ModelAndView mav = new ModelAndView("page/test-case-workspace/show-test-case-library");
		Set<Attachment> attachments = attachmentsHelper.findAttachments(lib);

		mav.addObject("library", lib);
		mav.addObject("attachments", attachments);
		mav.addObject("workspaceName", "test-case");

		return mav;
	}
}
