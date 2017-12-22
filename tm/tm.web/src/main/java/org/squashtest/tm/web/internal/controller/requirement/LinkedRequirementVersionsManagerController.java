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

import java.util.Optional;
import org.apache.commons.collections.MultiMap;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.squashtest.tm.core.foundation.collection.DefaultPagingAndSorting;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.requirement.*;
import org.squashtest.tm.exception.requirement.link.LinkedRequirementVersionException;
import org.squashtest.tm.service.internal.dto.UserDto;
import org.squashtest.tm.service.internal.dto.json.JsTreeNode;
import org.squashtest.tm.service.milestone.ActiveMilestoneHolder;
import org.squashtest.tm.service.milestone.MilestoneModelService;
import org.squashtest.tm.service.project.ProjectFinder;
import org.squashtest.tm.service.requirement.LinkedRequirementVersionManagerService;
import org.squashtest.tm.service.requirement.RequirementLibraryFinderService;
import org.squashtest.tm.service.requirement.RequirementLibraryNavigationService;
import org.squashtest.tm.service.requirement.RequirementVersionManagerService;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.security.PermissionsUtils;
import org.squashtest.tm.service.security.SecurityCheckableObject;
import org.squashtest.tm.service.user.UserAccountService;
import org.squashtest.tm.service.workspace.WorkspaceDisplayService;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.controller.milestone.MilestoneFeatureConfiguration;
import org.squashtest.tm.web.internal.controller.milestone.MilestoneUIConfigurationService;
import org.squashtest.tm.web.internal.helper.JsTreeHelper;
import org.squashtest.tm.web.internal.helper.LinkedRequirementVersionActionSummaryBuilder;
import org.squashtest.tm.web.internal.http.ContentTypes;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.builder.DriveNodeBuilder;
import org.squashtest.tm.web.internal.model.builder.JsTreeNodeListBuilder;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelConstants;
import org.squashtest.tm.web.internal.model.datatable.DataTableSorting;
import org.squashtest.tm.web.internal.model.viewmapper.DatatableMapper;
import org.squashtest.tm.web.internal.model.viewmapper.NameBasedMapper;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.util.*;

/**
 * Controller for the management of Requirement Versions linked to other Requirement Versions.
 *
 * Created by jlor on 11/05/2017.
 */
@Controller
@RequestMapping("/requirement-versions/{requirementVersionId}/linked-requirement-versions")
public class LinkedRequirementVersionsManagerController {

	@Inject
	private InternationalizationHelper i18nHelper;

	@Inject
	private RequirementVersionManagerService requirementVersionFinder;

	@Inject
	private LinkedRequirementVersionManagerService linkedReqVersionManager;

	@Inject
	private RequirementLibraryFinderService requirementLibraryFinder;

	@Inject
	private RequirementLibraryNavigationService requirementFinder;

	@Inject
	private MilestoneUIConfigurationService milestoneConfService;

	@Inject
	private PermissionEvaluationService permissionService;

	@Inject
	@Named("requirement.driveNodeBuilder")
	private Provider<DriveNodeBuilder<RequirementLibraryNode>> driveNodeBuilder;

	@Inject
	private ActiveMilestoneHolder activeMilestoneHolder;

	@Inject
	private WorkspaceDisplayService requirementWorkspaceDisplayService;

	@Inject
	protected UserAccountService userAccountService;

	@Inject
	protected MilestoneModelService milestoneModelService;

	@Inject
	protected ProjectFinder projectFinder;

	/*
	 * See VerifyingTestCaseManagerController.verifyingTCMapper
	 */
	private final DatatableMapper<String> linkedReqVersionMapper = new NameBasedMapper(6)
		.mapAttribute(DataTableModelConstants.PROJECT_NAME_KEY, "name", Project.class)
		.mapAttribute("rv-reference", "reference", RequirementVersion.class)
		.mapAttribute("rv-name", "name", RequirementVersion.class)
		.mapAttribute("rv-version", "versionNumber", RequirementVersion.class)
		.map("rv-role", "role")
		.map("milestone-dates", "endDate");

	@RequestMapping(value = "/table", method = RequestMethod.GET, params = RequestParams.S_ECHO_PARAM)
	@ResponseBody
	public DataTableModel getLinkedRequirementVersionsTableModel(@PathVariable long requirementVersionId, DataTableDrawParameters params) {

		PagingAndSorting pas = new DataTableSorting(params, linkedReqVersionMapper);

		return buildLinkedRequirementVersionsModel(requirementVersionId, pas, params.getsEcho());
	}

	protected DataTableModel buildLinkedRequirementVersionsModel(long requirementVersionId, PagingAndSorting pas, String sEcho) {
		PagedCollectionHolder<List<LinkedRequirementVersion>> holder = linkedReqVersionManager.findAllByRequirementVersion(
			requirementVersionId, pas);

		return new LinkedRequirementVersionsTableModelHelper(i18nHelper).buildDataModel(holder, sEcho);
	}

	@ResponseBody
	@RequestMapping(value = "/{requirementVersionIdsToUnbind}", method = RequestMethod.DELETE)
	public void removeLinkedRequirementVersionsFromRequirementVersion(
		@PathVariable long requirementVersionId,
		@PathVariable List<Long> requirementVersionIdsToUnbind) {

		linkedReqVersionManager.removeLinkedRequirementVersionsFromRequirementVersion(requirementVersionId, requirementVersionIdsToUnbind);
	}

	@RequestMapping(value = "/manager", method = RequestMethod.GET)
	public String showManager(@PathVariable long requirementVersionId, Model model,
							  @CookieValue(value = "jstree_open", required = false, defaultValue = "") String[] openedNodes) {

		RequirementVersion requirementVersion = requirementVersionFinder.findById(requirementVersionId);
		PermissionsUtils.checkPermission(permissionService, new SecurityCheckableObject(requirementVersion, "LINK"));
		MilestoneFeatureConfiguration milestoneConf = milestoneConfService.configure(requirementVersion);
//		List<JsTreeNode> linkableLibrariesModel = createLinkableLibrariesModel(openedNodes);

		MultiMap expansionCandidates = JsTreeHelper.mapIdsByType(openedNodes);
		UserDto currentUser = userAccountService.findCurrentUserDto();

		List<Long> projectIds = projectFinder.findAllReadableIds(currentUser);
		Optional<Long> activeMilestoneId = activeMilestoneHolder.getActiveMilestoneId();
		Collection<JsTreeNode> linkableLibrariesModel = requirementWorkspaceDisplayService.findAllLibraries(projectIds, currentUser, expansionCandidates, activeMilestoneId.get());

		DefaultPagingAndSorting pas = new DefaultPagingAndSorting("Project.name");
		DataTableModel linkedReqVersionsModel = buildLinkedReqVersionsModel(requirementVersionId, pas, "");

		model.addAttribute("requirement", requirementVersion.getRequirement()); // this is done because of RequirementViewInterceptor
		model.addAttribute("requirementVersion", requirementVersion);
		model.addAttribute("linkableLibrariesModel", linkableLibrariesModel);
		model.addAttribute("linkedReqVersionsModel", linkedReqVersionsModel);
		model.addAttribute("milestoneConf", milestoneConf);

		return "page/requirement-workspace/show-linked-requirement-version-manager";
	}

	protected DataTableModel buildLinkedReqVersionsModel(long requirementVersionId, PagingAndSorting pas, String sEcho) {
		PagedCollectionHolder<List<LinkedRequirementVersion>> holder =
			linkedReqVersionManager.findAllByRequirementVersion(requirementVersionId, pas);

		return new LinkedRequirementVersionsTableModelHelper(i18nHelper).buildDataModel(holder, sEcho);
	}

	private List<JsTreeNode> createLinkableLibrariesModel(String[] openedNodes) {
		List<RequirementLibrary> linkableLibraries = requirementLibraryFinder.findLinkableRequirementLibraries();

		MultiMap expansionCandidates = JsTreeHelper.mapIdsByType(openedNodes);

		DriveNodeBuilder<RequirementLibraryNode> nodeBuilder = driveNodeBuilder.get();

		Optional<Milestone> milestone = activeMilestoneHolder.getActiveMilestone();

		if (milestone.isPresent()) {
			nodeBuilder.filterByMilestone(milestone.get());
		}

		return new JsTreeNodeListBuilder<RequirementLibrary>(nodeBuilder)
			.expand(expansionCandidates)
			.setModel(linkableLibraries)
			.build();

	}

	@ResponseBody
	@RequestMapping(value = "/{requirementNodesIds}", method = RequestMethod.POST)
	public Map<String, Object> addDefaultLinkWithVersionIdAndNodeId(
		@PathVariable("requirementVersionId") long requirementVersionId,
		@PathVariable("requirementNodesIds") List<Long> requirementNodesIds) {

		Collection<LinkedRequirementVersionException> rejections =
			linkedReqVersionManager.addLinkedReqVersionsToReqVersion(requirementVersionId, requirementNodesIds);
		return buildSummary(rejections);
	}

	@ResponseBody
	@RequestMapping(method = RequestMethod.POST, params = {"reqVersionNodeId", "relatedReqVersionNodeId"})
	public Map<String, Object> addDefaultLinkWithNodeIds(
		@RequestParam("reqVersionNodeId") long reqVersionNodeId,
		@RequestParam("relatedReqVersionNodeId") long relatedReqVersionNodeId) {

		Collection<LinkedRequirementVersionException> rejections =
			linkedReqVersionManager.addDefaultLinkWithNodeIds(reqVersionNodeId, relatedReqVersionNodeId);
		return buildSummary(rejections);
	}

	@ResponseBody
	@RequestMapping(value = "/{relatedId}", method = RequestMethod.POST, params = {"isRelatedIdANodeId", "reqVersionLinkTypeId", "reqVersionLinkTypeDirection"})
	public void updateLinkTypeAndDirection (
		@PathVariable("requirementVersionId") long requirementVersionId,
		@PathVariable("relatedId") long paramRelatedId,
		@RequestParam("isRelatedIdANodeId") boolean isRelatedIdANodeId,
		@RequestParam("reqVersionLinkTypeId") long reqVersionLinkTypeId,
		@RequestParam("reqVersionLinkTypeDirection") boolean reqVersionLinkTypeDirection) {

		long relatedId = paramRelatedId;

		linkedReqVersionManager.updateLinkTypeAndDirection(
			requirementVersionId, relatedId, isRelatedIdANodeId,
			reqVersionLinkTypeId, reqVersionLinkTypeDirection);
	}

	@ResponseBody
	@RequestMapping(value = "/{relatedId}", params = {"isRelatedIdANodeId"},method = RequestMethod.GET, produces = ContentTypes.APPLICATION_JSON)
	public Map<String, String> getRequirementVersionInformation(@PathVariable long relatedId, @RequestParam("isRelatedIdANodeId") boolean isRelatedIdANodeId) {

		Map<String, String> versionInfosMap = new HashMap<>();

		RequirementVersion latestVersion;
		if(isRelatedIdANodeId) {
			// If the relatedId is a node's one, we have to get the corresponding latest version.
			Requirement selectedRequirement = requirementFinder.findRequirement(relatedId);
			latestVersion = selectedRequirement.findLastNonObsoleteVersion();
		} else {
			latestVersion = requirementVersionFinder.findById(relatedId);
		}
		versionInfosMap.put("versionName", latestVersion.getName());
		versionInfosMap.put("versionDescription", latestVersion.getDescription());

		return versionInfosMap;
	}

	@ResponseBody
	@RequestMapping(value = "/requirement-versions-link-types", method = RequestMethod.GET, produces = ContentTypes.APPLICATION_JSON)
	public List<RequirementVersionLinkType> getAllRequirementVersionLinkTypes(Locale locale) {
		return internationalizeLinkTypesRoles(linkedReqVersionManager.getAllReqVersionLinkTypes(), locale);
	}

	private Map<String, Object> buildSummary(Collection<LinkedRequirementVersionException> rejections) {
		return LinkedRequirementVersionActionSummaryBuilder.buildAddActionSummary(rejections);
	}

	private List<RequirementVersionLinkType> internationalizeLinkTypesRoles(List<RequirementVersionLinkType> listToInternationalize, Locale locale) {
		List<RequirementVersionLinkType> internationalizedList = new ArrayList<>();
		for(RequirementVersionLinkType typeToCopy : listToInternationalize) {
			RequirementVersionLinkType internationalizedCopy = typeToCopy.createCopy();
			internationalizedCopy.setRole1(i18nHelper.getMessage(
				internationalizedCopy.getRole1(), null, internationalizedCopy.getRole1(), locale));
			internationalizedCopy.setRole2(i18nHelper.getMessage(
				internationalizedCopy.getRole2(), null, internationalizedCopy.getRole2(), locale));
			internationalizedList.add(internationalizedCopy);
		}
		return internationalizedList;
	}
}
