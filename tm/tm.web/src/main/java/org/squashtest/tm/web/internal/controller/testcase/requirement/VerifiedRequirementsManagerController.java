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
package org.squashtest.tm.web.internal.controller.testcase.requirement;

import java.util.Optional;
import org.apache.commons.collections.MultiMap;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.requirement.RequirementLibrary;
import org.squashtest.tm.domain.requirement.RequirementLibraryNode;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestStep;
import org.squashtest.tm.exception.requirement.VerifiedRequirementException;
import org.squashtest.tm.service.internal.dto.UserDto;
import org.squashtest.tm.service.internal.dto.json.JsTreeNode;
import org.squashtest.tm.service.milestone.ActiveMilestoneHolder;
import org.squashtest.tm.service.milestone.MilestoneModelService;
import org.squashtest.tm.service.requirement.RequirementLibraryFinderService;
import org.squashtest.tm.service.requirement.VerifiedRequirement;
import org.squashtest.tm.service.requirement.VerifiedRequirementsManagerService;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.security.PermissionsUtils;
import org.squashtest.tm.service.security.SecurityCheckableObject;
import org.squashtest.tm.service.testcase.TestCaseModificationService;
import org.squashtest.tm.service.testcase.TestStepModificationService;
import org.squashtest.tm.service.user.UserAccountService;
import org.squashtest.tm.service.workspace.WorkspaceDisplayService;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.controller.milestone.MilestoneFeatureConfiguration;
import org.squashtest.tm.web.internal.controller.milestone.MilestoneModelUtils;
import org.squashtest.tm.web.internal.controller.milestone.MilestoneUIConfigurationService;
import org.squashtest.tm.web.internal.helper.JsTreeHelper;
import org.squashtest.tm.web.internal.helper.VerifiedRequirementActionSummaryBuilder;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.builder.DriveNodeBuilder;
import org.squashtest.tm.web.internal.model.builder.JsTreeNodeListBuilder;
import org.squashtest.tm.web.internal.model.datatable.*;
import org.squashtest.tm.web.internal.model.viewmapper.DatatableMapper;
import org.squashtest.tm.web.internal.model.viewmapper.NameBasedMapper;
import org.squashtest.tm.web.internal.util.HTMLCleanupUtils;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.util.*;
import java.util.stream.Collectors;

import static org.squashtest.tm.web.internal.helper.JEditablePostParams.VALUE;

/**
 * Controller for verified requirements management page.
 *
 * @author Gregory Fouquet
 */
@Controller
public class VerifiedRequirementsManagerController {
	private static final String REQUIREMENTS_IDS = "requirementsIds[]";

	@Inject
	private InternationalizationHelper internationalizationHelper;

	@SuppressWarnings("rawtypes")
	@Inject
	@Named("requirement.driveNodeBuilder")
	private Provider<DriveNodeBuilder<RequirementLibraryNode>> driveNodeBuilder;

	@Inject
	private TestCaseModificationService testCaseModificationService;

	@Inject
	private TestStepModificationService testStepService;

	@Inject
	private VerifiedRequirementsManagerService verifiedRequirementsManagerService;

	@Inject
	private RequirementLibraryFinderService requirementLibraryFinder;

	@Inject
	private PermissionEvaluationService permissionService;

	@Inject
	private MilestoneUIConfigurationService milestoneConfService;

	@Inject
	private ActiveMilestoneHolder activeMilestoneHolder;

	@Inject
	@Named("requirementWorkspaceDisplayService")
	private WorkspaceDisplayService requirementWorkspaceDisplayService;

	@Inject
	protected UserAccountService userAccountService;

	@Inject
	protected MilestoneModelService milestoneModelService;

	private DatatableMapper<String> verifiedRequirementVersionsMapper = new NameBasedMapper(8)
		.mapAttribute(DataTableModelConstants.DEFAULT_ENTITY_ID_KEY, "id", RequirementVersion.class)
		.mapAttribute(DataTableModelConstants.DEFAULT_ENTITY_NAME_KEY, "name", RequirementVersion.class)
		.mapAttribute("project", "name", Project.class)
		.mapAttribute("reference", "reference", RequirementVersion.class)
		.mapAttribute("versionNumber", "versionNumber", RequirementVersion.class)
		.mapAttribute("criticality", "criticality", RequirementVersion.class)
		.mapAttribute("category", "category", RequirementVersion.class)
		.map("milestone-dates", "endDate");


	@RequestMapping(value = "/test-cases/{testCaseId}/verified-requirement-versions/manager", method = RequestMethod.GET)
	public String showTestCaseManager(@PathVariable long testCaseId, Model model,
									  @CookieValue(value = "jstree_open", required = false, defaultValue = "") String[] openedNodes) {

		TestCase testCase = testCaseModificationService.findById(testCaseId);
		PermissionsUtils.checkPermission(permissionService, new SecurityCheckableObject(testCase, "LINK"));
		MilestoneFeatureConfiguration milestoneConf = milestoneConfService.configure(testCase);
//		List<JsTreeNode> linkableLibrariesModel = createLinkableLibrariesModel(openedNodes);

		MultiMap expansionCandidates = JsTreeHelper.mapIdsByType(openedNodes);
		UserDto currentUser = userAccountService.findCurrentUserDto();

		List<Long> linkableRequirementLibraryIds = requirementLibraryFinder.findLinkableRequirementLibraries().stream()
			.map(RequirementLibrary::getId).collect(Collectors.toList());
		Optional<Long> activeMilestoneId = activeMilestoneHolder.getActiveMilestoneId();
		Collection<JsTreeNode> linkableLibrariesModel = requirementWorkspaceDisplayService.findAllLibraries(linkableRequirementLibraryIds, currentUser, expansionCandidates, activeMilestoneId.get());

		model.addAttribute("testCase", testCase);
		model.addAttribute("linkableLibrariesModel", linkableLibrariesModel);
		model.addAttribute("milestoneConf", milestoneConf);

		return "page/test-case-workspace/show-verified-requirements-manager";
	}

	@RequestMapping(value = "/test-steps/{testStepId}/verified-requirement-versions/manager", method = RequestMethod.GET)
	public String showTestStepManager(
		@PathVariable long testStepId,
		Model model,
		@CookieValue(value = "jstree_open", required = false, defaultValue = "") String[] openedNodes) {

		TestStep testStep = testStepService.findById(testStepId);
		PermissionsUtils.checkPermission(permissionService, new SecurityCheckableObject(testStep, "LINK"));
		MilestoneFeatureConfiguration milestoneConf = milestoneConfService.configure(testStep.getTestCase());


		List<JsTreeNode> linkableLibrariesModel = createLinkableLibrariesModel(openedNodes);

		model.addAttribute("testStep", testStep);
		model.addAttribute("linkableLibrariesModel", linkableLibrariesModel);
		model.addAttribute("milestoneConf", milestoneConf);

		return "page/test-case-workspace/show-step-verified-requirements-manager";

	}

	@SuppressWarnings("rawtypes")
	private List<JsTreeNode> createLinkableLibrariesModel(String[] openedNodes) {
		List<RequirementLibrary> linkableLibraries = requirementLibraryFinder.findLinkableRequirementLibraries();

		MultiMap expansionCandidates = JsTreeHelper.mapIdsByType(openedNodes);
		DriveNodeBuilder<RequirementLibraryNode> nodeBuilder = driveNodeBuilder.get();
		Optional<Milestone> activeMilestone = activeMilestoneHolder.getActiveMilestone();
		if (activeMilestone.isPresent()) {
			nodeBuilder.filterByMilestone(activeMilestone.get());
		}

		return new JsTreeNodeListBuilder<RequirementLibrary>(nodeBuilder)
			.expand(expansionCandidates)
			.setModel(linkableLibraries)
			.build();

	}

	@ResponseBody
	@RequestMapping(value = "/test-cases/{testCaseId}/verified-requirements", method = RequestMethod.POST, params = REQUIREMENTS_IDS)
	public Map<String, Object> addVerifiedRequirementsToTestCase(@RequestParam(REQUIREMENTS_IDS) List<Long> requirementsIds,
																 @PathVariable long testCaseId) {


		Collection<VerifiedRequirementException> rejections =
			verifiedRequirementsManagerService
				.addVerifiedRequirementsToTestCase(requirementsIds, testCaseId);

		return buildSummary(rejections);

	}

	@ResponseBody
	@RequestMapping(value = "/test-steps/{testStepId}/verified-requirements", method = RequestMethod.POST, params = REQUIREMENTS_IDS)
	public Map<String, Object> addVerifiedRequirementsToTestStep(@RequestParam(REQUIREMENTS_IDS) List<Long> requirementsIds,
																 @PathVariable long testStepId) {


		Collection<VerifiedRequirementException> rejections = verifiedRequirementsManagerService
			.addVerifiedRequirementsToTestStep(requirementsIds, testStepId);

		return buildSummary(rejections);

	}

	@ResponseBody
	@RequestMapping(value = "/test-steps/{testStepId}/verified-requirement-versions/{requirementVersionId}", method = RequestMethod.POST)
	public Map<String, Object> addVerifiedRequirementToTestStep(@PathVariable long requirementVersionId,
																@PathVariable long testStepId) {
		Collection<VerifiedRequirementException> rejections = verifiedRequirementsManagerService
			.addVerifiedRequirementVersionToTestStep(requirementVersionId, testStepId);

		return buildSummary(rejections);

	}

	@RequestMapping(value = "/test-cases/{testCaseId}/verified-requirement-versions/{oldVersionId}", method = RequestMethod.POST)
	@ResponseBody
	public int changeVersion(@PathVariable long testCaseId, @PathVariable long oldVersionId,
							 @RequestParam(VALUE) long newVersionId) {

		List<Long> oldVersion = new ArrayList<>();
		oldVersion.add(oldVersionId);
		List<Long> newVersion = new ArrayList<>();
		newVersion.add(newVersionId);

		int newVersionNumber = verifiedRequirementsManagerService.changeVerifiedRequirementVersionOnTestCase(
			oldVersionId, newVersionId, testCaseId);

		return newVersionNumber;
	}

	private Map<String, Object> buildSummary(Collection<VerifiedRequirementException> rejections) {
		return VerifiedRequirementActionSummaryBuilder.buildAddActionSummary(rejections);
	}

	@ResponseBody
	@RequestMapping(value = "/test-cases/{testCaseId}/verified-requirement-versions/{requirementVersionsIds}", method = RequestMethod.DELETE)
	public void removeVerifiedRequirementVersionsFromTestCase(@PathVariable List<Long> requirementVersionsIds,
															  @PathVariable long testCaseId) {
		verifiedRequirementsManagerService.removeVerifiedRequirementVersionsFromTestCase(requirementVersionsIds,
			testCaseId);

	}

	@ResponseBody
	@RequestMapping(value = "/test-steps/{testStepId}/verified-requirement-versions/{requirementVersionsIds}", method = RequestMethod.DELETE)
	public void removeVerifiedRequirementVersionsFromTestStep(@PathVariable List<Long> requirementVersionsIds,
															  @PathVariable long testStepId) {
		verifiedRequirementsManagerService.removeVerifiedRequirementVersionsFromTestStep(requirementVersionsIds,
			testStepId);

	}

	@RequestMapping(value = "/test-cases/{testCaseId}/verified-requirement-versions", params = {
		RequestParams.S_ECHO_PARAM, "includeCallSteps"})
	@ResponseBody
	public DataTableModel getTestCaseWithCallStepsVerifiedRequirementsTableModel(@PathVariable long testCaseId,
																				 final DataTableDrawParameters params, final Locale locale) {

		PagingAndSorting pas = new DataTableSorting(params, verifiedRequirementVersionsMapper);

		PagedCollectionHolder<List<VerifiedRequirement>> holder = verifiedRequirementsManagerService
			.findAllVerifiedRequirementsByTestCaseId(testCaseId, pas);

		return new TestCaseWithCalledStepsVerifiedRequirementsDataTableModelHelper(locale, internationalizationHelper)
			.buildDataModel(holder, params.getsEcho());

	}

	private static final class TestCaseWithCalledStepsVerifiedRequirementsDataTableModelHelper extends
		TestCaseVerifiedRequirementsDataTableModelHelper {

		public TestCaseWithCalledStepsVerifiedRequirementsDataTableModelHelper(Locale locale,
																			   InternationalizationHelper internationalizationHelper) {
			super(locale, internationalizationHelper);
		}

		@Override
		public Map<String, Object> buildItemData(VerifiedRequirement item) {
			Map<String, Object> resMap = super.buildItemData(item);
			resMap.put("directlyVerified", item.isDirectVerification());
			return resMap;
		}
	}

	@RequestMapping(value = "/test-cases/{testCaseId}/verified-requirement-versions", method = RequestMethod.GET, params = RequestParams.S_ECHO_PARAM)
	@ResponseBody
	public DataTableModel getTestCaseVerifiedRequirementsTableModel(@PathVariable long testCaseId,
																	final DataTableDrawParameters params, final Locale locale) {

		PagingAndSorting pagingAndSorting = new DataTableSorting(params,
			verifiedRequirementVersionsMapper);

		PagedCollectionHolder<List<VerifiedRequirement>> holder = verifiedRequirementsManagerService
			.findAllDirectlyVerifiedRequirementsByTestCaseId(testCaseId, pagingAndSorting);

		return new TestCaseVerifiedRequirementsDataTableModelHelper(locale, internationalizationHelper).buildDataModel(
			holder, params.getsEcho());
	}

	/**
	 * gets the table model for step's verified requirement versions.
	 *
	 * @param params     : the {@link DataTableDrawParameters}
	 * @param testStepId : the id of the concerned {@link TestStep}
	 * @return a {@link DataTableModel} for the table of verified {@link RequirementVersion}
	 */
	@RequestMapping(value = "/test-steps/{testStepId}/verified-requirement-versions", method = RequestMethod.GET, params = RequestParams.S_ECHO_PARAM)
	@ResponseBody
	public DataTableModel getTestStepVerifiedRequirementTableModel(DataTableDrawParameters params,
																   @PathVariable long testStepId) {
		PagingAndSorting paging = new DataTableSorting(params, verifiedRequirementVersionsMapper);
		Locale locale = LocaleContextHolder.getLocale();
		PagedCollectionHolder<List<VerifiedRequirement>> holder = verifiedRequirementsManagerService
			.findAllDirectlyVerifiedRequirementsByTestStepId(testStepId, paging);

		TestCase testCase = testCaseModificationService.findTestCaseFromStep(testStepId);
		return new TestStepVerifiedRequirementsDataTableModelHelper(locale, internationalizationHelper, testStepId, testCase).buildDataModel(holder, params.getsEcho());
	}

	private static class VerifiedRequirementsDataTableModelHelper
		extends DataTableModelBuilder<VerifiedRequirement> { // NOSONAR no, it should not be declared final because it has subclasses in this very file
		private InternationalizationHelper internationalizationHelper;
		private Locale locale;
		private static final int INT_MAX_DESCRIPTION_LENGTH = 50;

		private VerifiedRequirementsDataTableModelHelper(Locale locale,
														 InternationalizationHelper internationalizationHelper) {
			this.locale = locale;
			this.internationalizationHelper = internationalizationHelper;
		}

		@Override
		public Map<String, Object> buildItemData(VerifiedRequirement item) {
			Map<String, Object> res = new HashMap<>();
			res.put(DataTableModelConstants.DEFAULT_ENTITY_ID_KEY, item.getId());
			res.put(DataTableModelConstants.DEFAULT_ENTITY_INDEX_KEY, getCurrentIndex());
			res.put(DataTableModelConstants.DEFAULT_ENTITY_NAME_KEY, item.getName());
			res.put("project", item.getProject().getName());
			res.put("reference", item.getReference());
			res.put("versionNumber", item.getVersionNumber());
			res.put("criticality",
				internationalizationHelper.internationalize(item.getCriticality(), locale));
			res.put("category", internationalizationHelper.getMessage(item.getCategory().getLabel(), null, item.getCategory().getLabel(), locale));
			res.put("status", internationalizationHelper.internationalize(item.getStatus(), locale));
			res.put("milestone-dates", MilestoneModelUtils.timeIntervalToString(item.getMilestones(), internationalizationHelper, locale));
			res.put(DataTableModelConstants.DEFAULT_EMPTY_DELETE_HOLDER_KEY, " ");
			res.put("milestone", MilestoneModelUtils.milestoneLabelsOrderByDate(item.getMilestones()));
			res.put("short-description", HTMLCleanupUtils.getBriefText(item.getDescription(), INT_MAX_DESCRIPTION_LENGTH));
			res.put("description", item.getDescription());
			res.put("category-icon", item.getCategory().getIconName());
			res.put("criticality-level", item.getCriticality().getLevel());
			res.put("status-level", item.getStatus().getLevel());
			return res;
		}
	}

	private static class TestCaseVerifiedRequirementsDataTableModelHelper extends
		VerifiedRequirementsDataTableModelHelper {

		private TestCaseVerifiedRequirementsDataTableModelHelper(Locale locale,
																 InternationalizationHelper internationalizationHelper) {
			super(locale, internationalizationHelper);
		}

		@Override
		public Map<String, Object> buildItemData(VerifiedRequirement item) {
			Map<String, Object> res = super.buildItemData(item);
			res.put("verifyingSteps", getVerifyingSteps(item));
			return res;
		}

		private String getVerifyingSteps(VerifiedRequirement item) {
			String result = "";
			Set<ActionTestStep> steps = item.getVerifyingSteps();
			if (!steps.isEmpty()) {
				if (steps.size() == 1) {
					ActionTestStep step = steps.iterator().next();
					result = "<span class='verifyingStep' dataId='" + step.getId() + "'>" + (step.getIndex() + 1)
						+ "</span>";
				} else {
					result = "&#42;";
				}
			}
			return result;
		}
	}

	private static final class TestStepVerifiedRequirementsDataTableModelHelper extends
		VerifiedRequirementsDataTableModelHelper {
		private long stepId;
		private TestCase testCase;

		private TestStepVerifiedRequirementsDataTableModelHelper(Locale locale,
																 InternationalizationHelper internationalizationHelper, long stepId, TestCase testCase) {
			super(locale, internationalizationHelper);
			this.stepId = stepId;
			this.testCase = testCase;
		}

		@Override
		public Map<String, Object> buildItemData(VerifiedRequirement item) {
			Map<String, Object> res = super.buildItemData(item);
			item.withVerifyingStepsFrom(testCase);
			res.put("verifiedByStep", item.hasStepAsVerifying(stepId));
			res.put("empty-link-checkbox", "");
			return res;
		}

	}

}
