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
package org.squashtest.tm.web.internal.controller.requirement;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.squashtest.tm.api.workspace.WorkspaceType;
import org.squashtest.tm.domain.EntityReference;
import org.squashtest.tm.domain.requirement.RequirementLibrary;
import org.squashtest.tm.domain.requirement.RequirementLibraryNode;
import org.squashtest.tm.service.library.WorkspaceService;
import org.squashtest.tm.service.requirement.RequirementLibraryNavigationService;
import org.squashtest.tm.service.workspace.WorkspaceDisplayService;
import org.squashtest.tm.web.internal.controller.generic.WorkspaceController;
import org.squashtest.tm.web.internal.model.builder.DriveNodeBuilder;
import org.squashtest.tm.web.internal.model.rest.RestLibrary;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.util.List;
import java.util.Locale;

@Controller
@RequestMapping("/requirement-workspace")
public class RequirementWorkspaceController extends WorkspaceController<RequirementLibraryNode<?>> {
	@Inject
	@Named("squashtest.tm.service.RequirementsWorkspaceService")
	private WorkspaceService<RequirementLibrary> workspaceService;

	@Inject
	@Named("requirement.driveNodeBuilder")
	private Provider<DriveNodeBuilder<RequirementLibraryNode<?>>> driveNodeBuilderProvider;

	@Inject
	private RequirementLibraryNavigationService requirementLibraryNavigationService;

	@Inject
	private WorkspaceDisplayService requirementWorkspaceDisplayService;


	@Override
	protected WorkspaceService<RequirementLibrary> getWorkspaceService() {
		return workspaceService;
	}

	@Override
	protected String getWorkspaceViewName() {
		return "requirement-workspace.html";
	}

	@Override
	protected void populateModel(Model model, Locale locale) {
		List<RestLibrary> libraries = getEditableLibraries(model);
		model.addAttribute("editableLibraries", libraries);
	}

	/**
	 * @see org.squashtest.tm.web.internal.controller.generic.WorkspaceController#getWorkspaceType()
	 */
	@Override
	protected WorkspaceType getWorkspaceType() {
		return WorkspaceType.REQUIREMENT_WORKSPACE;
	}

	/**
	 * @see org.squashtest.tm.web.internal.controller.generic.WorkspaceController#driveNodeBuilderProvider()
	 */
	@Override
	protected Provider<DriveNodeBuilder<RequirementLibraryNode<?>>> driveNodeBuilderProvider() {
		return driveNodeBuilderProvider;
	}

	@Override
	protected String[] getNodeParentsInWorkspace(EntityReference entityReference) {
		List<String> parents = requirementLibraryNavigationService.getParentNodesAsStringList(entityReference.getId());
		return parents.toArray(new String[parents.size()]);
	}

	@Override
	protected String getTreeElementIdInWorkspace(EntityReference entityReference) {
		return "Requirement-" + entityReference.getId();
	}

	@Override
	protected WorkspaceDisplayService workspaceDisplayService() {
		return requirementWorkspaceDisplayService;
	}

}
