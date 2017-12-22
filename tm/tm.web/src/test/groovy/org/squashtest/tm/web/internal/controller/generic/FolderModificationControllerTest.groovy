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
package org.squashtest.tm.web.internal.controller.generic

import org.squashtest.tm.domain.Workspace
import org.squashtest.tm.service.customreport.CustomReportDashboardService

import java.util.Set;

import javax.servlet.http.HttpServletRequest

import org.springframework.web.servlet.ModelAndView
import org.squashtest.tm.domain.attachment.Attachment;
import org.squashtest.tm.domain.library.Folder
import org.squashtest.tm.domain.testcase.TestCaseFolder
import org.squashtest.tm.service.library.FolderModificationService

import spock.lang.Specification

class FolderModificationControllerTest extends Specification {
	DummyFolderModificationController controller = new DummyFolderModificationController()
	FolderModificationService service = Mock()
	CustomReportDashboardService customReportDashboardService = Mock();



	def setup() {
		controller.service = service
		controller.customReportDashboardService = customReportDashboardService;
	}

	def "should return folder page fragment"() {
		given:
		HttpServletRequest req = Mock()
		req.getPathInfo() >> "/dummy-something/1"
		TestCaseFolder f = Mock()
		service.findFolder(15) >> f


		when:
		ModelAndView res = controller.showFolder(15, req)

		then:
		res.viewName == "fragment/generics/edit-folder"
		res.modelMap['folder'] == f
	}
}


class DummyFolderModificationController extends FolderModificationController<TestCaseFolder> {
	FolderModificationService service
	ServiceAwareAttachmentTableModelHelper attachmentsHelper


	@Override
	protected FolderModificationService<TestCaseFolder> getFolderModificationService() {
		return service;
	}

	@Override
	protected String getWorkspaceName() {
		return "workspace";
	}

	@Override
	protected Set<Attachment> findAttachments(TestCaseFolder folder){
		return []
	}

}
