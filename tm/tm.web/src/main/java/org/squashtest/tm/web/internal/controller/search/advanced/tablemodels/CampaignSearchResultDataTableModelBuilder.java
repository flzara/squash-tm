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
package org.squashtest.tm.web.internal.controller.search.advanced.tablemodels;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.web.util.HtmlUtils;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.testcase.TestCaseExecutionMode;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelBuilder;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelConstants;

public class CampaignSearchResultDataTableModelBuilder extends DataTableModelBuilder<IterationTestPlanItem> {
	private InternationalizationHelper messageSource;
	private Locale locale;
	private PermissionEvaluationService permissionService;

	public CampaignSearchResultDataTableModelBuilder(Locale locale, InternationalizationHelper messageSource,
			PermissionEvaluationService permissionService) {
		this.locale = locale;
		this.messageSource = messageSource;
		this.permissionService = permissionService;

	}

	private boolean isExecutionEditable(IterationTestPlanItem item) {
		// Milestone dependent ? Not for now.
		return permissionService.hasRoleOrPermissionOnObject("ROLE_ADMIN", "WRITE", item);
	}

	@Override
	public Map<String, Object> buildItemData(IterationTestPlanItem item) {

		Map<String, Object> res = new HashMap<>();
		res.put(DataTableModelConstants.PROJECT_NAME_KEY, HtmlUtils.htmlEscape(item.getProject().getName()));
		res.put("project-id", item.getProject().getId());
		res.put(DataTableModelConstants.DEFAULT_ENTITY_INDEX_KEY, getCurrentIndex());
		res.put("campaign-name", HtmlUtils.htmlEscape(item.getCampaign().getName()));
		res.put("iteration-name",  HtmlUtils.htmlEscape(item.getIteration().getName()));
		res.put("iteration-id", item.getIteration().getId());
		res.put("editable", isExecutionEditable(item));
		res.put("itpi-id", item.getId().toString());
		res.put("tc-weight",
				item.isTestCaseDeleted() ? "" : formatImportance(item.getReferencedTestCase().getImportance(), locale));
		res.put("itpi-isauto", item.isAutomated());
		res.put("itpi-label", item.isTestCaseDeleted() ? "" : HtmlUtils.htmlEscape(item.getReferencedTestCase().getName()));
		res.put("itpi-mode", formatMode(item.getExecutionMode(), locale));
		res.put("itpi-testsuites", item.getTestSuiteNames());
		res.put("itpi-status", formatExecutionStatus(item.getExecutionStatus(), locale));
		res.put("itpi-executed-by", formatUsername(item.getLastExecutedBy()));
		res.put("itpi-executed-on", formatDateItem(item));
		res.put("itpi-datasets", formatDatasetsItem(item));
		res.put("empty-opentree-holder", " ");
		res.put("empty-openinterface2-holder", " ");
		return res;
	}

	private String formatExecutionStatus(ExecutionStatus status, Locale locale) {
		return status.getLevel() + "-" + messageSource.internationalize(status, locale);
	}

	private String formatMode(TestCaseExecutionMode mode, Locale locale) {
		return messageSource.internationalize(mode, locale);
	}

	private String formatImportance(TestCaseImportance importance, Locale locale) {

		return importance.getLevel() + "-" + messageSource.internationalize(importance, locale);
	}

	private String formatDatasetsItem(IterationTestPlanItem item) {
		String dataset = "-";
		if (item.getReferencedDataset() != null) {
			dataset = HtmlUtils.htmlEscape(item.getReferencedDataset().getName());
		}
		return dataset;
	}

	private String formatDateItem(IterationTestPlanItem item) {
		String reportDate = "-";
		// Get the i18n thing
		/* Issue #6766 - the 'last executed on' date was only in the following format : MM/dd/yyyy HH:mm:ss */
		if (item.getLastExecutedOn() != null) {
			reportDate = messageSource.localizeDate(item.getLastExecutedOn(), locale);
		}
		return reportDate;
	}
}

