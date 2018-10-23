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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
		.mapAttribute(DataTableModelConstants.PROJECT_NAME_KEY, "name", Project.class)
		.mapAttribute("reference", "reference", TestCase.class)
		.mapAttribute(DataTableModelConstants.DEFAULT_ENTITY_NAME_KEY, "name", TestCase.class)
		.mapAttribute("format", "kind", TestCase.class)
		.mapAttribute(DataTableModelConstants.DEFAULT_ENTITY_ID_KEY, "id", AutomationRequest.class)
		.mapAttribute(DataTableModelConstants.DEFAULT_CREATED_BY_KEY, "createdBy", AutomationRequest.class)
		.mapAttribute("transmitted-by", "transmittedBy", AutomationRequest.class)
		.mapAttribute("transmitted-on", "transmissionDate", AutomationRequest.class)
		.mapAttribute("priority", "automationPriority", AutomationRequest.class)
		.mapAttribute("status", "requestStatus", AutomationRequest.class)
		.mapAttribute("assigned-to", "assignedTo", AutomationRequest.class)
		.mapAttribute("assigned-on", "assignmentDate", AutomationRequest.class)
		.map("entity-index", "index(AutomationRequest)");

	@RequestMapping(method = RequestMethod.GET)
	public String showWorkspace(Model model, Locale locale) {
		return getWorkspaceViewName();
	}


	protected String getWorkspaceViewName() {
		return "automation-workspace.html";
	}

	protected WorkspaceType getWorkspaceType() {
		return WorkspaceType.AUTOMATION_WORKSPACE;
	}

	@RequestMapping(value="automation-request", method= RequestMethod.GET)
	public DataTableModel getAutomationRequestModel(final DataTableDrawParameters params, final Locale locale) {
		Pageable pageable = SpringPagination.pageable(params);
		ColumnFiltering filtering = new DataTableColumnFiltering(params);
		Page<AutomationRequest> automationRequestPage = automationRequestFinderService.findRequestsAssignedToCurrentUser(pageable, filtering);
		LOGGER.info("CONTROLLER {}", automationRequestPage.getContent().size());
		return new AutomationRequestDataTableModelHelper(messageSource, locale, automationRequestFinderService).buildDataModel(automationRequestPage, "");
	}

	private static final class AutomationRequestDataTableModelHelper extends DataTableModelBuilder<AutomationRequest> {
		private InternationalizationHelper messageSource;
		private Locale locale;
		private AutomationRequestFinderService automationRequestFinderService;

		private AutomationRequestDataTableModelHelper(InternationalizationHelper messageSource, Locale locale, AutomationRequestFinderService automationRequestFinderService) {
			this.messageSource = messageSource;
			this.locale = locale;
			this.automationRequestFinderService = automationRequestFinderService;
		}

		@Override
		protected Object buildItemData(AutomationRequest item) {
			Map<String, Object> data = new HashMap<>(12);
			data.put(DataTableModelConstants.PROJECT_NAME_KEY, item.getTestCase().getProject().getLabel());
			data.put("reference", item.getTestCase().getReference());
			data.put(DataTableModelConstants.DEFAULT_ENTITY_NAME_KEY, item.getTestCase().getFullName());
			data.put("format", item.getTestCase().getKind());
			data.put(DataTableModelConstants.DEFAULT_ENTITY_ID_KEY, item.getId());
			data.put("transmitted-by", item.getTransmittedBy().getName());
			data.put("transmitted-on", item.getTransmissionDate());
			data.put("priority", item.getAutomationPriority());
			data.put("status", item.getRequestStatus().name());
			data.put("assigned-to", item.getAssignedTo().getName());
			data.put("assigned-on", item.getAssignmentDate());
			data.put("entity-index", getCurrentIndex());

			return data;
		}
	}
}
