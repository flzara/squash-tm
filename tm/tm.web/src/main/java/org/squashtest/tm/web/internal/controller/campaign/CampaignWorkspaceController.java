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

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.squashtest.tm.api.workspace.WorkspaceType;
import org.squashtest.tm.domain.EntityReference;
import org.squashtest.tm.domain.EntityType;
import org.squashtest.tm.domain.campaign.CampaignLibrary;
import org.squashtest.tm.domain.campaign.CampaignLibraryNode;
import org.squashtest.tm.service.campaign.CampaignLibraryNavigationService;
import org.squashtest.tm.service.library.WorkspaceService;
import org.squashtest.tm.service.workspace.WorkspaceDisplayService;
import org.squashtest.tm.web.internal.controller.generic.WorkspaceController;
import org.squashtest.tm.web.internal.model.builder.DriveNodeBuilder;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.util.List;
import java.util.Locale;

@Controller
@RequestMapping("/campaign-workspace")
public class CampaignWorkspaceController extends WorkspaceController<CampaignLibraryNode> {

	@Inject
	private CampaignLibraryNavigationService campaignLibraryNavigationService;

	@Inject
	private WorkspaceService<CampaignLibrary> workspaceService;

	@Inject
	@Named("campaign.driveNodeBuilder")
	private Provider<DriveNodeBuilder<CampaignLibraryNode>> driveNodeBuilderProvider;

	@Inject
	@Named("campaignWorkspaceDisplayService")
	private WorkspaceDisplayService workspaceDisplayService;

	/**
	 *
	 * @see org.squashtest.tm.web.internal.controller.generic.WorkspaceController#getWorkspaceService()
	 */
	@Override
	protected WorkspaceService<CampaignLibrary> getWorkspaceService() {
		return workspaceService;
	}

	@Override
	protected String getWorkspaceViewName() {
		return "campaign-workspace.html";
	}

	@Override
	public WorkspaceType getWorkspaceType() {
		return WorkspaceType.CAMPAIGN_WORKSPACE;
	}

	@Override
	protected void populateModel(Model model, Locale locale) {
		// noop
	}

	/**
	 * @see org.squashtest.tm.web.internal.controller.generic.WorkspaceController#driveNodeBuilderProvider()
	 */
	@Override
	protected Provider<DriveNodeBuilder<CampaignLibraryNode>> driveNodeBuilderProvider() {
		return driveNodeBuilderProvider;
	}

	@Override
	protected WorkspaceDisplayService workspaceDisplayService() {
		return workspaceDisplayService;
	}

	@Override
	protected String[] getNodeParentsInWorkspace(EntityReference entityReference) {
		List<String> parents = campaignLibraryNavigationService.getParentNodesAsStringList(entityReference);
		return parents.toArray(new String[parents.size()]);
	}

	@Override
	protected String getTreeElementIdInWorkspace(EntityReference entityReference) {
		return entityReference.getType().equals(EntityType.CAMPAIGN) ? "Campaign-" + entityReference.getId() : "Iteration-" + entityReference.getId();
	}

}
