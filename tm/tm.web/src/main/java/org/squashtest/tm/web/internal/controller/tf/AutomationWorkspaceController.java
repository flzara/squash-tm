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
package org.squashtest.tm.web.internal.controller.tf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;
import org.squashtest.tm.api.workspace.WorkspaceType;
import org.squashtest.tm.core.foundation.collection.ColumnFiltering;
import org.squashtest.tm.core.foundation.collection.DefaultPagingAndSorting;
import org.squashtest.tm.core.foundation.collection.Filtering;
import org.squashtest.tm.core.foundation.collection.PagingAndMultiSorting;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.testcase.Dataset;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.tf.automationrequest.AutomationRequest;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.service.tf.AutomationRequestFinderService;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.*;
import org.squashtest.tm.web.internal.model.viewmapper.DatatableMapper;
import org.squashtest.tm.web.internal.model.viewmapper.NameBasedMapper;

import javax.inject.Inject;
import javax.xml.datatype.DatatypeConstants;
import java.util.*;

/**
 * This controller is dedicated to the initial page of Automation workspace
 */
@Controller
@RequestMapping("/automation-workspace")
public class AutomationWorkspaceController {

	public static final Logger LOGGER = LoggerFactory.getLogger(AutomationWorkspaceController.class);

	@Inject
	private AutomationRequestFinderService automationRequestFinderService;

	@Inject
	private InternationalizationHelper messageSource;

	private final DatatableMapper<String> automationRequestMapper = new NameBasedMapper()
		.map(DataTableModelConstants.PROJECT_NAME_KEY, "testCase.project.name")
		.map("reference", "testCase.reference")
		.map(DataTableModelConstants.DEFAULT_ENTITY_NAME_KEY, "testCase.name")
		.map("format", "testCase.kind")
		.map(DataTableModelConstants.DEFAULT_ENTITY_ID_KEY, "testCase.id")
		.map(DataTableModelConstants.DEFAULT_CREATED_BY_KEY, "createdBy")
		.map("transmitted-on", "transmissionDate")
		.map("priority", "automationPriority")
		.map("status", "requestStatus")
		.map("assigned-on", "assignmentDate")
		.map("script", "testCase.automatedTest.name")
		.map("entity-index", "index(AutomationRequest)");

	@RequestMapping(method = RequestMethod.GET)
	public String showWorkspace(Model model, Locale locale) {

		//model.addAttribute()

		return getWorkspaceViewName();
	}


	protected String getWorkspaceViewName() {
		return "automation-workspace.html";
	}

	protected WorkspaceType getWorkspaceType() {
		return WorkspaceType.AUTOMATION_WORKSPACE;
	}

	@RequestMapping(value="automation-request", params = RequestParams.S_ECHO_PARAM)
	@ResponseBody
	public DataTableModel getAutomationRequestModel(final DataTableDrawParameters params, final Locale locale) {

		Pageable pageable = SpringPagination.pageable(params,automationRequestMapper);
		ColumnFiltering filtering = new DataTableColumnFiltering(params, automationRequestMapper);
		Page<AutomationRequest> automationRequestPage = automationRequestFinderService.findRequestsAssignedToCurrentUser(pageable, filtering);

		return new AutomationRequestDataTableModelHelper(messageSource).buildDataModel(automationRequestPage, "");
	}

	private static final class AutomationRequestDataTableModelHelper extends DataTableModelBuilder<AutomationRequest> {
		private InternationalizationHelper messageSource;
		private Locale locale = LocaleContextHolder.getLocale();

		private AutomationRequestDataTableModelHelper(InternationalizationHelper messageSource) {
			this.messageSource = messageSource;;
		}

		@Override
		protected Object buildItemData(AutomationRequest item) {
			Map<String, Object> data = new HashMap<>(13);
			data.put(DataTableModelConstants.PROJECT_NAME_KEY, item.getTestCase() != null ? HtmlUtils.htmlEscape(item.getTestCase().getProject().getName()): null);
			data.put("reference", item.getTestCase() != null ? item.getTestCase().getReference(): null);
			data.put(DataTableModelConstants.DEFAULT_ENTITY_NAME_KEY, item.getTestCase() != null ? HtmlUtils.htmlEscape(item.getTestCase().getFullName()): null);
			data.put("format", item.getTestCase() != null ? messageSource.internationalize(item.getTestCase().getKind().getI18nKey(), locale) : null);
			data.put(DataTableModelConstants.DEFAULT_ENTITY_ID_KEY, item.getTestCase() != null ? item.getTestCase().getId() : null);
			data.put(DataTableModelConstants.DEFAULT_CREATED_BY_KEY, item.getCreatedBy() != null? item.getCreatedBy().getLogin(): null);
			data.put("transmitted-on", messageSource.localizeShortDate(item.getTransmissionDate(), locale));
			data.put("priority", item.getAutomationPriority());
			data.put("assigned-on", messageSource.localizeShortDate(item.getAssignmentDate(), locale));
			data.put("entity-index", getCurrentIndex());
			data.put("script", (item.getTestCase() != null && item.getTestCase().getAutomatedTest() != null) ? item.getTestCase().getAutomatedTest().getFullLabel(): null);
			data.put("checkbox", "");
			data.put("tc-id", item.getTestCase() != null ? item.getTestCase().getId(): null);
			return data;
		}
	}
}
