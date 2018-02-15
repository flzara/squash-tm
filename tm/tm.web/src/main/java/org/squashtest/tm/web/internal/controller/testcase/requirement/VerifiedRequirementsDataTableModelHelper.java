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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.web.util.HtmlUtils;
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.requirement.VerifiedRequirement;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.web.internal.controller.milestone.MilestoneModelUtils;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelBuilder;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelConstants;
import org.squashtest.tm.web.internal.util.HTMLCleanupUtils;

/**
 * Base class for all {@link DataTableModelBuilder} that build models for
 * verified requirement tables. Subclasses are available as static classes of
 * this one
 * 
 * @author bsiri
 *
 */
abstract class VerifiedRequirementsDataTableModelHelper 
	extends DataTableModelBuilder<VerifiedRequirement> { // NOSONAR  no, it should not  be declared final  because  it has subclasses in this very file

	private InternationalizationHelper internationalizationHelper;
	private Locale locale;
	private PermissionEvaluationService permService;
	private static final int INT_MAX_DESCRIPTION_LENGTH = 50;

	VerifiedRequirementsDataTableModelHelper(Locale locale, InternationalizationHelper internationalizationHelper, PermissionEvaluationService permService) {
		this.locale = locale;
		this.internationalizationHelper = internationalizationHelper;
		this.permService = permService;
	}

	@Override
	public Map<String, Object> buildItemData(VerifiedRequirement item) {
		Map<String, Object> res = new HashMap<>();
		res.put(DataTableModelConstants.DEFAULT_ENTITY_ID_KEY, item.getId());
		res.put(DataTableModelConstants.DEFAULT_ENTITY_INDEX_KEY, getCurrentIndex());
		res.put(DataTableModelConstants.DEFAULT_ENTITY_NAME_KEY, HtmlUtils.htmlEscape(item.getName()));
		res.put("project", HtmlUtils.htmlEscape(item.getProject().getName()));
		res.put("reference", HtmlUtils.htmlEscape(item.getReference()));
		res.put("versionNumber", item.getVersionNumber());
		res.put("criticality", internationalizationHelper.internationalize(item.getCriticality(), locale));
		res.put("category", HtmlUtils.htmlEscape(internationalizationHelper.getMessage(item.getCategory().getLabel(),
				null, item.getCategory().getLabel(), locale)));
		res.put("status", internationalizationHelper.internationalize(item.getStatus(), locale));
		res.put("milestone-dates",
				MilestoneModelUtils.timeIntervalToString(item.getMilestones(), internationalizationHelper, locale));
		res.put(DataTableModelConstants.DEFAULT_EMPTY_DELETE_HOLDER_KEY, " ");
		res.put("milestone", MilestoneModelUtils.milestoneLabelsOrderByDate(item.getMilestones()));
		res.put("short-description",
				HTMLCleanupUtils.getCleanedBriefText(item.getDescription(), INT_MAX_DESCRIPTION_LENGTH));
		res.put("description", HtmlUtils.htmlEscape(item.getDescription()));
		res.put("category-icon", HtmlUtils.htmlEscape(item.getCategory().getIconName()));
		res.put("criticality-level", item.getCriticality().getLevel());
		res.put("status-level", item.getStatus().getLevel());
		
		//Issue 7142
		res.put("readable", permService.canRead(item.getVerifiedRequirementVersion()));
		return res;
	}

	/*
	 * **************************************
	 * 
	 * DEDICATED IMPLEMENTATIONS
	 * 
	 ***************************************/

	/**
	 * Implementation for the table shown in the test cases
	 * 
	 * @author bsiri
	 *
	 */
	static class TestCaseVerifiedRequirementsDataTableModelHelper
			extends VerifiedRequirementsDataTableModelHelper {

		TestCaseVerifiedRequirementsDataTableModelHelper(Locale locale,
				InternationalizationHelper internationalizationHelper, PermissionEvaluationService permService) {
			super(locale, internationalizationHelper, permService);
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
	

	
	/**
	 * Another implementation that also show the indirect requirement coverage
	 * 
	 * @author bsiri
	 *
	 */
	static final class TestCaseWithCalledStepsVerifiedRequirementsDataTableModelHelper
			extends TestCaseVerifiedRequirementsDataTableModelHelper {

		TestCaseWithCalledStepsVerifiedRequirementsDataTableModelHelper(Locale locale,
				InternationalizationHelper internationalizationHelper, PermissionEvaluationService permService) {
			super(locale, internationalizationHelper, permService);
		}

		@Override
		public Map<String, Object> buildItemData(VerifiedRequirement item) {
			Map<String, Object> resMap = super.buildItemData(item);
			resMap.put("directlyVerified", item.isDirectVerification());
			return resMap;
		}
	}

	

	/**
	 * Implementation for the test steps verified requirements
	 * 
	 * @author bsiri
	 *
	 */
	static final class TestStepVerifiedRequirementsDataTableModelHelper
			extends VerifiedRequirementsDataTableModelHelper {
		private long stepId;
		private TestCase testCase;

		TestStepVerifiedRequirementsDataTableModelHelper(Locale locale,
				InternationalizationHelper internationalizationHelper, PermissionEvaluationService permService, long stepId, TestCase testCase) {
			super(locale, internationalizationHelper, permService);
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