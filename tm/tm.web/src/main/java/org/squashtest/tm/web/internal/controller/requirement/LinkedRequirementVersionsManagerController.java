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

import org.apache.commons.collections.MultiMap;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;
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
import org.squashtest.tm.web.internal.util.HTMLCleanupUtils;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.util.*;

/**
 * Controller for the management of Requirement Versions linked to other Requirement Versions.
 * <p>
 * Created by jlor on 11/05/2017.
 */
// XSS OK
@Controller
@RequestMapping("/requirement-versions/{requirementVersionId}/linked-requirement-versions")
public class LinkedRequirementVersionsManagerController {

	private static final String IS_RELATED_ID_A_NODE_ID = "isRelatedIdANodeId";
	private static final String REQ_VERSION_LINK_TYPE_ID = "reqVersionLinkTypeId";
	private static final String REQ_VERSION_LINK_TYPE_DIRECTION = "reqVersionLinkTypeDirection";
	private static final String REQUIREMENT_VERSION_ID = "requirementVersionId";

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

	@ResponseBody
	@RequestMapping(value = "/{requirementNodesIds}", method = RequestMethod.POST, params = {REQ_VERSION_LINK_TYPE_ID, REQ_VERSION_LINK_TYPE_DIRECTION})
	public Map<String, Object> addLinkWithVersionIdAndNodeId(
		@PathVariable(REQUIREMENT_VERSION_ID) long requirementVersionId,
		@PathVariable("requirementNodesIds") List<Long> requirementNodesIds,
		@RequestParam(REQ_VERSION_LINK_TYPE_ID) long reqVersionLinkTypeId,
		@RequestParam(REQ_VERSION_LINK_TYPE_DIRECTION) boolean reqVersionLinkTypeDirection) {
		Map<String, Object> map = new HashMap<>();
		for (Long ids : requirementNodesIds) {
			Collection<LinkedRequirementVersionException> rejections = linkedReqVersionManager.addLinkWithNodeIds(requirementVersionId, ids, reqVersionLinkTypeId, reqVersionLinkTypeDirection);
			map.putAll(buildSummary(rejections));
		}

		return map;
	}

	@ResponseBody
	@RequestMapping(value = "/{relatedId}", method = RequestMethod.POST, params = {IS_RELATED_ID_A_NODE_ID, REQ_VERSION_LINK_TYPE_ID, REQ_VERSION_LINK_TYPE_DIRECTION})
	public void updateLinkTypeAndDirection(
		@PathVariable(REQUIREMENT_VERSION_ID) long requirementVersionId,
		@PathVariable("relatedId") List<Long> paramRelatedIds,
		@RequestParam(IS_RELATED_ID_A_NODE_ID) boolean isRelatedIdANodeId,
		@RequestParam(REQ_VERSION_LINK_TYPE_ID) long reqVersionLinkTypeId,
		@RequestParam(REQ_VERSION_LINK_TYPE_DIRECTION) boolean reqVersionLinkTypeDirection) {
		Map<String, Object> map = new HashMap<>();
		for (Long paramRelatedId : paramRelatedIds) {
			long relatedId = paramRelatedId;
			linkedReqVersionManager.updateLinkTypeAndDirection(
				requirementVersionId, relatedId, isRelatedIdANodeId,
				reqVersionLinkTypeId, reqVersionLinkTypeDirection);
		}
	}

	@ResponseBody
	@RequestMapping(value = "/{relatedIds}", params = {IS_RELATED_ID_A_NODE_ID}, method = RequestMethod.GET, produces = ContentTypes.APPLICATION_JSON)
	public Map<String, String> getRequirementVersionInformation(@PathVariable List<Long> relatedIds, @RequestParam(IS_RELATED_ID_A_NODE_ID) boolean isRelatedIdANodeId) {
		return linkedReqVersionManager.getRequirementVersionInformation(relatedIds);
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
		for (RequirementVersionLinkType typeToCopy : listToInternationalize) {
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
