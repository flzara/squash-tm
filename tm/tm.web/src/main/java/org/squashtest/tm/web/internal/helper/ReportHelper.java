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
package org.squashtest.tm.web.internal.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.squashtest.tm.api.report.Report;
import org.squashtest.tm.api.report.criteria.Criteria;
import org.squashtest.tm.api.report.form.*;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.report.ReportDefinition;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.campaign.CampaignModificationService;
import org.squashtest.tm.service.campaign.IterationModificationService;
import org.squashtest.tm.service.milestone.MilestoneManagerService;
import org.squashtest.tm.service.project.GenericProjectManagerService;
import org.squashtest.tm.service.requirement.RequirementVersionManagerService;
import org.squashtest.tm.service.testcase.TestCaseModificationService;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.report.ReportsRegistry;
import org.squashtest.tm.web.internal.report.criteria.ConciseFormToCriteriaConverter;
import org.squashtest.tm.web.internal.report.criteria.MultiOptionsCriteria;
import org.squashtest.tm.web.internal.report.criteria.MultiValuesCriteria;
import org.squashtest.tm.web.internal.report.criteria.SimpleCriteria;

import javax.inject.Inject;
import java.io.IOException;
import java.util.*;

/**
 * @author zyang
 */

@Component
public class ReportHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReportHelper.class);

	private static final String REQUIREMENTS_IDS = "requirementsIds";
	private static final String TESTCASES_IDS = "testcasesIds";
	private static final String CAMPAIGN_IDS = "campaignIds";
	private static final String CAMPAIGN_ID = "campaignId";
	private static final String ITERATION_IDS = "iterationIds";
	private static final String ITERATION_ID = "iterationId";
	private static final String PROJECT_IDS = "projectIds";
	private static final String MILESTONES = "milestones";
	private static final String TAGS = "tags";
	private static final String OPTION = "option";

	@Inject
	private CampaignModificationService campaignModificationService;

	@Inject
	private IterationModificationService iterationModificationService;

	@Inject
	private RequirementVersionManagerService requirementVersionManagerService;

	@Inject
	private TestCaseModificationService testCaseModificationService;

	@Inject
	private GenericProjectManagerService projectManagerService;

	@Inject
	private MilestoneManagerService milestoneManagerService;

	@Inject
	private ReportsRegistry reportsRegistry;

	@Inject
	private InternationalizationHelper i18nHelper;


	public Map<String, List<String>> getAttributesFromReportDefinition(ReportDefinition def){

		Report report = reportsRegistry.findReport(def.getPluginNamespace());
		Map<String, Object> form = null;
		try {
			form = JsonHelper.deserialize(def.getParameters());
		} catch (IOException e) {
			LOGGER.error("the report : " + def.getName() + " has corrupted parameters.", e);
		}

		Map<String, Criteria> crit = new ConciseFormToCriteriaConverter(report, Collections.singletonList(def.getProject())).convert(form);
		return getAttributesForReport(report, crit);
	}

	public Map<String, List<String>> getAttributesForReport(Report report, Map<String, Criteria> criteriaMap) {

		Map<String, List<String>> attributes = new LinkedHashMap<>();

		Input[] inputs = report.getForm();
		for (Input input : inputs) {
			getAttributesFromInput(attributes, input, criteriaMap);
		}

		return attributes;
	}
	@SuppressWarnings({"squid:S00107","squid:MethodCyclomaticComplexity","squid:S00122"})
	private void getAttributesFromInput(Map<String, List<String>> attributes, Input input, Map<String, Criteria> criteriaMap) {
		SimpleCriteria sCrit;
		MultiValuesCriteria mvCrit;
		MultiOptionsCriteria moCrit;
		List<String> targets;

		switch (input.getType()) {
			case RADIO_BUTTONS_GROUP:
				getAttributesFromRadioButtonsGroup( attributes,  input,  criteriaMap);
				break;

			case CHECKBOXES_GROUP:
				getAttributesFromChexBoxesGroup( attributes, input, criteriaMap );
				break;
			case INPUTS_GROUP:
				InputsGroup inputsGroup = (InputsGroup) input;
				inputsGroup.getInputs().forEach(i -> getAttributesFromInput(attributes, i, criteriaMap));
				break;
			case CHECKBOX:
				CheckboxInput checkboxInput = (CheckboxInput) input;
				sCrit = (SimpleCriteria) criteriaMap.get(checkboxInput.getName());
				if ((boolean) sCrit.getValue()) { getAttributesFromOptionInput(attributes, checkboxInput, criteriaMap);	}
				break;
			case DATE:
				getAttributesFromDate( attributes, input, criteriaMap);
				break;
			case DROPDOWN_LIST:
				getAttributesFromDropdownList(  attributes,  input, criteriaMap);
				break;
			case TREE_PICKER:
				TreePicker treePicker = (TreePicker) input;
				mvCrit = (MultiValuesCriteria) criteriaMap.get(treePicker.getName());
				targets = new ArrayList(mvCrit.getValue().values());
				getAttributesFromPicker(attributes, targets, mvCrit.getName());
				break;
			case PROJECT_PICKER:
			case MILESTONE_PICKER:
			case TAG_PICKER:
				moCrit = (MultiOptionsCriteria) criteriaMap.get(input.getName());
				targets = new ArrayList<>();
				moCrit.getSelectedOptions().forEach(o -> targets.add(o.toString()));
				getAttributesFromPicker(attributes, targets, moCrit.getName());
				break;
			default:
				break;
		}
	}

	private void getAttributesFromRadioButtonsGroup(Map<String, List<String>> attributes, Input input, Map<String, Criteria> criteriaMap ){
		RadioButtonsGroup radioButtonsGroup = (RadioButtonsGroup) input;
		SimpleCriteria sCrit = (SimpleCriteria) criteriaMap.get(radioButtonsGroup.getName());
		SimpleCriteria finalSCrit = sCrit;
		radioButtonsGroup.getOptions().forEach(optionInput -> {
			if (optionInput.getValue().equalsIgnoreCase((String) finalSCrit.getValue())) {
				getAttributesFromOptionInput(attributes, optionInput, criteriaMap);
			}
		});
	}

	private void getAttributesFromChexBoxesGroup(Map<String, List<String>> attributes, Input input, Map<String, Criteria> criteriaMap ) {
		CheckboxesGroup checkboxesGroup = (CheckboxesGroup) input;
		MultiOptionsCriteria moCrit = (MultiOptionsCriteria) criteriaMap.get(checkboxesGroup.getName());
		List<String> targets = new ArrayList<>();
		checkboxesGroup.getOptions().forEach(optionInput ->
			moCrit.getSelectedOptions().forEach(selectedOption -> {
				if (optionInput.getValue().equalsIgnoreCase(selectedOption.toString())) {
					if (attributes.get(checkboxesGroup.getLabel()) == null) {
						attributes.put(checkboxesGroup.getLabel(), Arrays.asList(optionInput.getLabel()));
					} else {
						targets.addAll(attributes.get(checkboxesGroup.getLabel()));
						targets.add(optionInput.getLabel());
						attributes.put(checkboxesGroup.getLabel(), targets);
					}
				}
			})
		);
	}

	private void getAttributesFromDropdownList(Map<String, List<String>> attributes, Input input, Map<String, Criteria> criteriaMap ){
		DropdownList dropdownList = (DropdownList) input;
		SimpleCriteria sCrit = (SimpleCriteria) criteriaMap.get(dropdownList.getName());
		dropdownList.getOptions().forEach(optionInput -> {
			if (optionInput.getValue().equalsIgnoreCase((String) sCrit.getValue())) {
				attributes.put(dropdownList.getLabel(), Arrays.asList(optionInput.getLabel()));
			}});
	}

	private void getAttributesFromDate(Map<String, List<String>> attributes, Input input, Map<String, Criteria> criteriaMap ) {
		DateInput dateInput = (DateInput) input;
		Criteria crit = criteriaMap.get(dateInput.getName());
		Locale locale = LocaleContextHolder.getLocale();
		if (crit instanceof SimpleCriteria) {
			SimpleCriteria sCrit = (SimpleCriteria) crit;
			Date date = (Date) sCrit.getValue();
			attributes.put(dateInput.getLabel(), Arrays.asList(i18nHelper.localizeShortDate(date, locale)));
		} else {
			attributes.put(dateInput.getLabel(), Arrays.asList("-"));
		}
	}

		private void getAttributesFromOptionInput(Map<String, List<String>> attributes, OptionInput optionInput, Map<String, Criteria> criteriaMap) {
		List<String> options = new ArrayList<>();
		if (optionInput instanceof ContainerOption) {
			ContainerOption containerOption = (ContainerOption) optionInput;
			getAttributesFromInput(attributes, containerOption.getContent(), criteriaMap);
		} else {
			if (attributes.get(OPTION) == null) {
				attributes.put(OPTION, Arrays.asList(optionInput.getLabel()));
			} else {
				options.addAll(attributes.get(OPTION));
				options.add(optionInput.getLabel());
				attributes.put(OPTION, options);
			}
		}
	}

	private void getAttributesFromPicker(Map<String, List<String>> attributes, List<String> targetIds, String entity) {
		List<String> names = new ArrayList<>();
		List<Long> ids = new ArrayList<>();
		Locale locale = LocaleContextHolder.getLocale();
		
		switch (entity) {
			case CAMPAIGN_ID:
			case CAMPAIGN_IDS:
				targetIds.forEach(id -> ids.add(Long.parseLong(id)));
				List<Campaign> campaigns = campaignModificationService.findAllByIds(ids);
				campaigns.forEach(o -> names.add(o.getName()));
				attributes.put(i18nHelper.internationalize("label.campaigns",locale), names);
				break;
			case ITERATION_ID:
			case ITERATION_IDS:
				targetIds.forEach(id -> ids.add(Long.parseLong(id)));
				List<Iteration> iterations = iterationModificationService.findAllByIds(ids);
				iterations.forEach(o -> names.add(o.getName()));
				attributes.put(i18nHelper.internationalize("label.iterations",locale), names);
				break;
			case REQUIREMENTS_IDS:
				targetIds.forEach(id -> ids.add(Long.parseLong(id)));
				List<Requirement> requirements = requirementVersionManagerService.findRequirementsAllByIds(ids);
				requirements.forEach(o -> names.add(o.getName()));
				attributes.put(i18nHelper.internationalize("label.requirements",locale), names);
				break;
			case TESTCASES_IDS:
				targetIds.forEach(id -> ids.add(Long.parseLong(id)));
				List<TestCase> testCases = testCaseModificationService.findAllByIds(ids);
				testCases.forEach(o -> names.add(o.getName()));
				attributes.put(i18nHelper.internationalize("label.testCases",locale), names);
				break;
			case PROJECT_IDS:
				targetIds.forEach(id -> ids.add(Long.parseLong(id)));
				List<GenericProject> genericProjects = projectManagerService.findAllByIds(ids);
				genericProjects.forEach(o -> names.add(o.getName()));
				attributes.put(i18nHelper.internationalize("label.projects",locale), names);
				break;
			case MILESTONES:
				targetIds.forEach(id -> ids.add(Long.parseLong(id)));
				List<Milestone> milestones = milestoneManagerService.findAllByIds(ids);
				milestones.forEach(o -> names.add(o.getLabel()));
				attributes.put(i18nHelper.internationalize("label.Milestones",locale), names);
				break;
			case TAGS:
				targetIds.forEach(o -> names.add(o.toString()));
				attributes.put(TAGS, names);
				break;
			default:
				break;
		}
	}

}
