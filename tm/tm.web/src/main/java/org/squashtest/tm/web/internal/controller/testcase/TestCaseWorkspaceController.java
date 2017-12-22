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

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.squashtest.tm.api.workspace.WorkspaceType;
import org.squashtest.tm.domain.EntityReference;
import org.squashtest.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.tm.service.customreport.CustomReportDashboardService;
import org.squashtest.tm.service.library.WorkspaceService;
import org.squashtest.tm.service.testcase.TestCaseLibraryNavigationService;
import org.squashtest.tm.service.workspace.WorkspaceDisplayService;
import org.squashtest.tm.web.internal.controller.generic.WorkspaceController;
import org.squashtest.tm.web.internal.model.builder.DriveNodeBuilder;
import org.squashtest.tm.web.internal.model.rest.RestLibrary;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.util.*;

@Controller
@RequestMapping("/test-case-workspace")
public class TestCaseWorkspaceController extends WorkspaceController<TestCaseLibraryNode> {

	@Inject
	private TestCaseLibraryNavigationService testCaseLibraryNavigationService;

	@Inject
	@Named("squashtest.tm.service.TestCasesWorkspaceService")
	private WorkspaceService<TestCaseLibrary> workspaceService;

	@Inject
	@Named("testCaseWorkspaceDisplayService")
	private WorkspaceDisplayService testCaseWorkspaceDisplayService;

	@Inject
	@Named("testCase.driveNodeBuilder")
	private Provider<DriveNodeBuilder<TestCaseLibraryNode>> driveNodeBuilderProvider;

	@Inject
	private CustomReportDashboardService customReportDashboardService;

	@Override
	protected WorkspaceService<TestCaseLibrary> getWorkspaceService() {
		return workspaceService;
	}

	@Override
	protected String getWorkspaceViewName() {
		return "test-case-workspace.html";
	}

	/**
	 * @see org.squashtest.tm.web.internal.controller.generic.WorkspaceController#getWorkspaceType()
	 */
	@Override
	protected WorkspaceType getWorkspaceType() {
		return null;
	}

	@Override
	protected void populateModel(Model model, Locale locale) {
		List<RestLibrary> libraries = getEditableLibraries(model);

		model.addAttribute("editableLibraries", libraries);
	}

	/**
	 * @see org.squashtest.tm.web.internal.controller.generic.WorkspaceController#driveNodeBuilderProvider()
	 */
	@Override
	protected Provider<DriveNodeBuilder<TestCaseLibraryNode>> driveNodeBuilderProvider() {
		return driveNodeBuilderProvider;
	}

	@Override
	protected WorkspaceDisplayService workspaceDisplayService() {
		return testCaseWorkspaceDisplayService;
	}

	@Override
	protected String[] getNodeParentsInWorkspace(EntityReference entityReference) {
		List<String> parents = testCaseLibraryNavigationService.getParentNodesAsStringList(entityReference.getId());
		return parents.toArray(new String[parents.size()]);
	}

	@Override
	protected String getTreeElementIdInWorkspace(EntityReference entityReference) {
		return "TestCase-" + entityReference.getId();
	}

}
