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
package org.squashtest.tm.web.internal.controller.search.advanced;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndMultiSorting;
import org.squashtest.tm.domain.EntityReference;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.search.AdvancedSearchModel;
import org.squashtest.tm.exception.customfield.CodeDoesNotMatchesPatternException;
import org.squashtest.tm.service.campaign.CampaignAdvancedSearchService;
import org.squashtest.tm.service.campaign.CampaignLibraryNavigationService;
import org.squashtest.tm.service.internal.dto.UserDto;
import org.squashtest.tm.service.internal.dto.json.JsTreeNode;
import org.squashtest.tm.service.milestone.MilestoneMembershipFinder;
import org.squashtest.tm.service.user.UserAccountService;
import org.squashtest.tm.service.workspace.WorkspaceDisplayService;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.controller.search.advanced.tablemodels.CampaignSearchResultDataTableModelBuilder;
import org.squashtest.tm.web.internal.helper.JsTreeHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelConstants;
import org.squashtest.tm.web.internal.model.datatable.DataTableMultiSorting;
import org.squashtest.tm.web.internal.model.viewmapper.DatatableMapper;
import org.squashtest.tm.web.internal.model.viewmapper.NameBasedMapper;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Created by jsimon on 04/05/2016.
 */
@Controller
public class CampaignSearchController extends GlobalSearchController {


	@Inject
	private CampaignAdvancedSearchService campaignAdvancedSearchService;

	@Inject
	private MilestoneMembershipFinder milestoneMembershipFinder;

	@Inject
	private UserAccountService userAccountService;

	@Inject
	private CampaignLibraryNavigationService campaignLibraryNavigationService;

	@Inject
	@Named("campaignWorkspaceDisplayService")
	private WorkspaceDisplayService workspaceDisplayService;

	private DatatableMapper<String> campaignSearchResultMapper = new NameBasedMapper(11)
		.mapAttribute(DataTableModelConstants.PROJECT_NAME_KEY, NAME, Project.class)
		.mapAttribute("campaign-name", "campaign.label", IterationTestPlanItem.class)
		.mapAttribute("iteration-name", "iteration.name", IterationTestPlanItem.class)
		.mapAttribute("itpi-id", "id", IterationTestPlanItem.class)
		.mapAttribute("itpi-label", "label", IterationTestPlanItem.class)
		.mapAttribute("itpi-mode", "executionMode", IterationTestPlanItem.class)
		.mapAttribute("itpi-testsuites", "", IterationTestPlanItem.class)
		.mapAttribute("itpi-status", "executionStatus", IterationTestPlanItem.class)
		.mapAttribute("itpi-executed-by", "lastExecutedBy", IterationTestPlanItem.class)
		.mapAttribute("itpi-executed-on", "lastExecutedOn", IterationTestPlanItem.class)
		.mapAttribute("itpi-datasets", "datasets", IterationTestPlanItem.class)
		.mapAttribute("tc-weight", "referencedTestCase.importance", IterationTestPlanItem.class)
		.mapAttribute("test-case-automatable", "referencedTestCase.automatable", IterationTestPlanItem.class);

	
	// ************** the search page handlers *******************
	

	
	@RequestMapping(method = RequestMethod.GET, params = "searchDomain="+CAMPAIGN)
	public String showCampaignSearchPage(Model pageModel, 
										@RequestParam(value = "jstree_open", required = false, defaultValue = "") String[] openedNodes,
										 @RequestParam(value = "workspace-prefs", required = false, defaultValue = "") String elementEntityReference,
										 @RequestParam(required = false, defaultValue = "") String associationType,
										 @RequestParam(required = false, defaultValue = "") Long associationId) {


		prepareSearchPageModel(pageModel, "", openedNodes, elementEntityReference, associationType, associationId);

		return "campaign-search-input.html";
	}
	
	
	@RequestMapping(method = RequestMethod.POST, params = "searchDomain="+CAMPAIGN)
	public String showCampaignSearchPageWithSearchModel(Model pageModel,
		 												 @RequestParam String searchModel, 
														 @RequestParam(value = "jstree_open", required = false, defaultValue = "") String[] openedNodes,
														 @RequestParam(value = "workspace-prefs", required = false, defaultValue = "") String elementEntityReference,
														 @RequestParam(required = false) String associationType,
														 @RequestParam(required = false) Long associationId) {

		prepareSearchPageModel(pageModel, searchModel, openedNodes, elementEntityReference, associationType, associationId);

		return "campaign-search-input.html";
	}

	
	private void prepareSearchPageModel(Model pageModel, String searchModel, String[] openedNodes, String elementEntityReference, String associationType, Long associationId) {
		
		String[] nodesToOpen = new String[0];

		if (StringUtils.isBlank(elementEntityReference) || "null".equals(elementEntityReference)) {
			nodesToOpen = openedNodes;
			pageModel.addAttribute("selectedNode", "");
		} else {
			try {
				EntityReference entityReference = EntityReference.fromString(elementEntityReference.toUpperCase());
				nodesToOpen = getNodeParentsInWorkspace(entityReference);
				pageModel.addAttribute("selectedNode", getTreeElementIdInWorkspace(entityReference.getId()));
				// WARNING! it was previously catching all Exceptions
			} catch (CodeDoesNotMatchesPatternException | IllegalArgumentException e) {
				LOGGER.warn("Error during conversion of the 'workspace-prefs' cookie to an EntityReference.", e);
			}
		}

		MultiMap expansionCandidates = mapIdsByType(nodesToOpen);

		initSearchPageModel(pageModel, "",  associationType, associationId, CAMPAIGN);
		
		List<Long> projectIds = campaignAdvancedSearchService.findAllReadablesId();
		UserDto user = userAccountService.findCurrentUserDto();
		
		Optional<Long> activeMilestoneId = getActiveMilestoneHolder().getActiveMilestoneId();
		Collection<JsTreeNode> rootNodes = workspaceDisplayService().findAllLibraries(projectIds, user, expansionCandidates, activeMilestoneId.get());

		boolean isCampaignAvailable = true;

		Optional<Milestone> activeMilestone = getActiveMilestoneHolder().getActiveMilestone();
		if (activeMilestone.isPresent()) {
			isCampaignAvailable = milestoneMembershipFinder.isMilestoneBoundToACampainInProjects(activeMilestone.get().getId(), projectIds);
		}

		pageModel.addAttribute("rootModel", rootNodes);

		pageModel.addAttribute("isCampaignAvailable", isCampaignAvailable);
	}
	
	
	

	// ******************* the result page handlers ****************
	
	@RequestMapping(method = RequestMethod.GET, value = RESULTS, params = "searchDomain="+CAMPAIGN)
	public String getCampaignSearchResultPage(Model pageModel, 
											  @RequestParam(required = false) String associationType, 
											  @RequestParam(required = false) Long associationId) {
		
		initResultModel(pageModel, "", associationType, associationId, CAMPAIGN);
		return "campaign-search-result.html";

	}



	@RequestMapping(method = RequestMethod.POST, value = RESULTS, params = "searchDomain="+CAMPAIGN)
	public String showCampaignResultSearchResultPageWithSearchModel(Model pageModel,
	                                                              @RequestParam String searchModel,
	                                                              @RequestParam(required = false) String associationType,
	                                                              @RequestParam(required = false) Long associationId) {

		initResultModel(pageModel, searchModel, associationType, associationId, CAMPAIGN);
		return "campaign-search-result.html";
	}
	
	
	// ********************* other methods **********************************



	@RequestMapping(value = TABLE, method = RequestMethod.POST, params = {RequestParams.MODEL, CAMPAIGN,
		RequestParams.S_ECHO_PARAM})
	@ResponseBody
	public DataTableModel getCampaignTableModel(final DataTableDrawParameters params, final Locale locale,
												@RequestParam(value = RequestParams.MODEL) String model)
		throws IOException {

		AdvancedSearchModel searchModel = new ObjectMapper().readValue(model, AdvancedSearchModel.class);

		addMilestoneToSearchModel(searchModel);

		PagingAndMultiSorting paging = new DataTableMultiSorting(params, campaignSearchResultMapper);

		PagedCollectionHolder<List<IterationTestPlanItem>> holder =
			campaignAdvancedSearchService.searchForIterationTestPlanItem(searchModel, paging, locale);

		return new CampaignSearchResultDataTableModelBuilder(locale, getMessageSource(), getPermissionService())
			.buildDataModel(holder, params.getsEcho());
	}


	@Override
	protected WorkspaceDisplayService workspaceDisplayService() {
		return workspaceDisplayService;
	}

	protected String getTreeElementIdInWorkspace(Long elementId) {
		return "Campaign-" + elementId;
	}

	protected String[] getNodeParentsInWorkspace(EntityReference entityReference) {
		List<String> parents = campaignLibraryNavigationService.getParentNodesAsStringList(entityReference);
		return parents.toArray(new String[parents.size()]);
	}

	protected MultiMap mapIdsByType(String[] openedNodes) {
		return JsTreeHelper.mapIdsByType(openedNodes);
	}
}
