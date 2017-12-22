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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.squashtest.tm.core.foundation.collection.DefaultPagingAndSorting;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.core.foundation.collection.SinglePageCollectionHolder;
import org.squashtest.tm.domain.Level;
import org.squashtest.tm.domain.event.RequirementAuditEvent;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.requirement.LinkedRequirementVersion;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.audit.RequirementAuditTrailService;
import org.squashtest.tm.service.customfield.CustomFieldValueFinderService;
import org.squashtest.tm.service.milestone.ActiveMilestoneHolder;
import org.squashtest.tm.service.requirement.LinkedRequirementVersionManagerService;
import org.squashtest.tm.service.requirement.RequirementVersionManagerService;
import org.squashtest.tm.service.testcase.VerifyingTestCaseManagerService;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.controller.audittrail.RequirementAuditEventTableModelBuilder;
import org.squashtest.tm.web.internal.controller.milestone.MilestoneFeatureConfiguration;
import org.squashtest.tm.web.internal.controller.milestone.MilestoneModelUtils;
import org.squashtest.tm.web.internal.controller.milestone.MilestoneUIConfigurationService;
import org.squashtest.tm.web.internal.helper.LevelLabelFormatter;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.builder.JsonInfoListBuilder;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelBuilder;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelConstants;
import org.squashtest.tm.web.internal.model.datatable.DataTableSorting;
import org.squashtest.tm.web.internal.model.viewmapper.DatatableMapper;
import org.squashtest.tm.web.internal.model.viewmapper.NameBasedMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import org.squashtest.tm.core.foundation.collection.SpringPaginationUtils;
import org.squashtest.tm.web.internal.model.datatable.SpringPagination;

@Controller
@RequestMapping("/requirements/{requirementId}/versions")
public class RequirementVersionManagerController {


	@Inject
	private RequirementVersionManagerService versionService;


	@Inject
	private InternationalizationHelper i18nHelper;

	@Inject
	private Provider<LevelLabelFormatter> levelFormatterProvider;

	@Inject
	private CustomFieldValueFinderService cufValueService;

	@Inject
	private JsonInfoListBuilder infoListBuilder;

	@Inject
	private Provider<RequirementCriticalityComboDataBuilder> criticalityComboBuilderProvider;

	@Inject
	private VerifyingTestCaseManagerService verifyingTestCaseManager;

	@Inject
	private LinkedRequirementVersionManagerService linkedRequirementsManager;

	@Inject
	private RequirementAuditTrailService auditTrailService;

	@Inject
	private MilestoneUIConfigurationService milestoneConfService;

	@Inject
	private ActiveMilestoneHolder activeMilestoneHolder;


	private final DatatableMapper<String> versionMapper = new NameBasedMapper()
	.map("version-number", "versionNumber")
	.map("reference", "reference")
	.map(DataTableModelConstants.DEFAULT_ENTITY_NAME_KEY, "name")
	.map("status", "status")
	.map("criticality", "criticality")
	.map("category", "category");




	@RequestMapping(value = "/new", method = RequestMethod.POST, params = {"inheritReqLinks", "inheritTestcasesReqLinks"})
	@ResponseBody
	public void createNewVersion(@PathVariable long requirementId, @RequestParam("inheritReqLinks") boolean inheritReqLinks,  @RequestParam("inheritTestcasesReqLinks") boolean inheritTestcasesReqLinks) {

            Optional<Milestone> activeMilestone = activeMilestoneHolder.getActiveMilestone();
            if (activeMilestone.isPresent()) {
                // milestone mode creation
                ArrayList<Long> milestoneIds = new ArrayList<>();
                milestoneIds.add(activeMilestone.get().getId());
                versionService.createNewVersion(requirementId, milestoneIds, inheritReqLinks, inheritTestcasesReqLinks);
            }else{
                // normal mode creation
                versionService.createNewVersion(requirementId, inheritReqLinks, inheritTestcasesReqLinks);
            }
	}




	@RequestMapping(value = "/manager")
	public String showRequirementVersionsManager(@PathVariable long requirementId, Model model, Locale locale) {

		Requirement req = versionService.findRequirementById(requirementId);

		PagedCollectionHolder<List<RequirementVersion>> holder = new SinglePageCollectionHolder<>(req.getUnmodifiableVersions());

		DataTableModel tableModel = new RequirementVersionDataTableModel(locale, levelFormatterProvider, i18nHelper).buildDataModel(holder,
				"0");

		MilestoneFeatureConfiguration milestoneConf = milestoneConfService.configure(req.getCurrentVersion());

		model.addAttribute("requirement", req);
		model.addAttribute("versions", req.getUnmodifiableVersions());
		model.addAttribute("versionsTableModel", tableModel);
		model.addAttribute("selectedVersion", req.getCurrentVersion());
		model.addAttribute("criticalityList", buildMarshalledCriticalities(locale));
		model.addAttribute("categoryList", infoListBuilder.toJson(req.getProject().getRequirementCategories()));
		model.addAttribute("verifyingTestCasesModel", getVerifyingTCModel(req.getCurrentVersion()));
		model.addAttribute("linkedRequirementVersionsModel", getLinkedReqVersionsModel(req.getCurrentVersion()));
		model.addAttribute("auditTrailModel", getEventsTableModel(req));
		model.addAttribute("milestoneConf", milestoneConf);
		boolean hasCUF = cufValueService.hasCustomFields(req.getCurrentVersion());

		model.addAttribute("hasCUF", hasCUF);
		return "page/requirement-workspace/versions-manager";
	}


	@RequestMapping(value = "/table", params = RequestParams.S_ECHO_PARAM)
	@ResponseBody
	public DataTableModel getRequirementVersionsTableModel(@PathVariable long requirementId,
			DataTableDrawParameters params, final Locale locale) {
                Pageable pageable = SpringPagination.pageable(params, versionMapper);

		Page<RequirementVersion> page = versionService.findAllByRequirement(requirementId, pageable);

		return new RequirementVersionDataTableModel(locale, levelFormatterProvider, i18nHelper).buildDataModel(page,
				params.getsEcho());
	}




	private String buildMarshalledCriticalities(Locale locale) {
		return criticalityComboBuilderProvider.get().useLocale(locale).buildMarshalled();
	}

	private DataTableModel getVerifyingTCModel(RequirementVersion version){
		PagedCollectionHolder<List<TestCase>> holder = verifyingTestCaseManager.findAllByRequirementVersion(
				version.getId(), new DefaultPagingAndSorting("Project.name"));

		return new VerifyingTestCasesTableModelHelper(i18nHelper).buildDataModel(holder, "0");
	}

	private DataTableModel getLinkedReqVersionsModel(RequirementVersion version){
		PagedCollectionHolder<List<LinkedRequirementVersion>> holder = linkedRequirementsManager.findAllByRequirementVersion(
			version.getId(), new DefaultPagingAndSorting("Project.name"));

		return new LinkedRequirementVersionsTableModelHelper(i18nHelper).buildDataModel(holder, "0");
	}

	private DataTableModel getEventsTableModel(Requirement requirement){
		Page<RequirementAuditEvent> auditTrail = auditTrailService
				.findAllByRequirementVersionIdOrderedByDate(requirement.getCurrentVersion().getId(), SpringPaginationUtils.defaultPaging("date"));

		RequirementAuditEventTableModelBuilder builder = new RequirementAuditEventTableModelBuilder(LocaleContextHolder.getLocale(), i18nHelper);

		return builder.buildDataModel(auditTrail, "");

	}



	private static String internationalize(Level level, Locale locale,
			Provider<LevelLabelFormatter> levelFormatterProvider) {
		return levelFormatterProvider.get().useLocale(locale).formatLabel(level);
	}


	private static final class RequirementVersionDataTableModel extends DataTableModelBuilder<RequirementVersion> {
		private Locale locale;
		private Provider<LevelLabelFormatter> levelFormatterProvider;
		private InternationalizationHelper i18nHelper;

		private RequirementVersionDataTableModel(Locale locale, Provider<LevelLabelFormatter> levelFormatterProvider, InternationalizationHelper helper) {
			this.locale = locale;
			this.levelFormatterProvider = levelFormatterProvider;
			this.i18nHelper = helper;
		}

		@Override
		public Map<String, Object> buildItemData(RequirementVersion version) {

			Map<String, Object> row = new HashMap<>(7);

			row.put(DataTableModelConstants.DEFAULT_ENTITY_ID_KEY, version.getId());
			row.put("version-number", version.getVersionNumber());
			row.put("reference", version.getReference());
			row.put(DataTableModelConstants.DEFAULT_ENTITY_NAME_KEY, version.getName());
			row.put("status", internationalize(version.getStatus(), locale, levelFormatterProvider));
			row.put("criticality", internationalize(version.getCriticality(), locale, levelFormatterProvider));
			row.put("category", i18nHelper.getMessage(version.getCategory().getLabel(), null, version.getCategory().getLabel(), locale)  );
			row.put("milestone-dates", MilestoneModelUtils.timeIntervalToString(version.getMilestones(),i18nHelper, locale));
			row.put("milestone", MilestoneModelUtils.milestoneLabelsOrderByDate(version.getMilestones()));
			return row;

		}

	}

}
