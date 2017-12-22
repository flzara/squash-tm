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

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.tm.domain.testcase.TestCaseFolder;
import org.squashtest.tm.service.library.FolderModificationService;
import org.squashtest.tm.service.testcase.TestCaseLibraryFinderService;
import org.squashtest.tm.web.internal.controller.generic.FolderModificationController;

@Controller
@RequestMapping("/test-case-folders/{folderId}")
public class TestCaseFolderModificationController extends FolderModificationController<TestCaseFolder> {

	private FolderModificationService<TestCaseFolder> folderModificationService;

	@Inject private TestCaseLibraryFinderService libraryFinderService;

	@Override
	protected FolderModificationService<TestCaseFolder> getFolderModificationService() {
		return folderModificationService;
	}

	@Inject @Named("squashtest.tm.service.TestCaseFolderModificationService")
	public final void setFolderModificationService(FolderModificationService<TestCaseFolder> folderModificationService) {
		this.folderModificationService = folderModificationService;
	}

	@Override
	protected String getWorkspaceName() {
		return "test-case";
	}


	@Override
	@RequestMapping(method = RequestMethod.GET)
	public final ModelAndView showFolder(@PathVariable long folderId, HttpServletRequest request) {

		ModelAndView mav = super.showFolder(folderId, request);

		mav.setViewName("fragment/test-cases/test-case-folder");

		return mav;
	}

	@RequestMapping(value="/test/{ids}", method = RequestMethod.GET)
	@ResponseBody
	public List<String> getPath(@PathVariable("ids") List<Long> ids){
		return libraryFinderService.getPathsAsString(ids);
	}

}
