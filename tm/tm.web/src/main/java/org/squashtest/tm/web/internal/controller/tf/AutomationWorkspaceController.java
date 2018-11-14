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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;
import org.squashtest.tm.api.workspace.WorkspaceType;
import org.squashtest.tm.core.foundation.collection.ColumnFiltering;
import org.squashtest.tm.core.foundation.collection.DefaultPagingAndSorting;
import org.squashtest.tm.core.foundation.collection.Filtering;
import org.squashtest.tm.core.foundation.collection.PagingAndMultiSorting;
import org.squashtest.tm.domain.audit.AuditableMixin;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.testcase.Dataset;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.tf.automationrequest.AutomationRequest;
import org.squashtest.tm.domain.tf.automationrequest.AutomationRequestStatus;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.service.tf.AutomationRequestFinderService;
import org.squashtest.tm.service.user.UserManagerService;
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
	private UserManagerService userManagerService;

	@Inject
	private InternationalizationHelper messageSource;

	private final DatatableMapper<String> automationRequestMapper = new NameBasedMapper()
		.map(DataTableModelConstants.PROJECT_NAME_KEY, "testCase.project.name")
		.map("reference", "testCase.reference")
		.map(DataTableModelConstants.DEFAULT_ENTITY_NAME_KEY, "testCase.name")
		.map("format", "testCase.kind")
		.map(DataTableModelConstants.DEFAULT_ENTITY_ID_KEY, "testCase.id")
		.map(DataTableModelConstants.DEFAULT_CREATED_BY_KEY, "transmittedBy")
		.map("transmitted-on", "transmissionDate")
		.map("priority", "automationPriority")
		.map("status", "requestStatus")
		.map("assigned-on", "assignmentDate")
		.map("script", "testCase.automatedTest.name")
		.map("entity-index", "index(AutomationRequest)")
		.map("requestId", "id")
		.map("assigned-to", "assignedTo")
		.map("status", "requestStatus");

	@RequestMapping(method = RequestMethod.GET)
	public String showWorkspace(Model model, Locale locale) {
		Map<Long, String> assignableUsers = automationRequestFinderService.getCreatedByForCurrentUser
			(Arrays.asList(AutomationRequestStatus.WORK_IN_PROGRESS.toString()));
		Map<Long, String> traitmentUsers = automationRequestFinderService.getCreatedByForAutomationRequests
			(Arrays.asList(AutomationRequestStatus.TRANSMITTED.toString()));
		Map<Long, String> globalUsers = automationRequestFinderService.getCreatedByForAutomationRequests
			(Arrays.asList(AutomationRequestStatus.WORK_IN_PROGRESS.toString(), AutomationRequestStatus.TRANSMITTED.toString(), AutomationRequestStatus.EXECUTABLE.toString()));
		model.addAttribute("assignableUsers", assignableUsers);
		model.addAttribute("traitmentUsers", traitmentUsers);
		model.addAttribute("globalUsers", globalUsers);
		model.addAttribute("assignableUsersGlobalView",getAssignableUsersGlobalView());
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

	@RequestMapping(value="automation-request/traitment", params = RequestParams.S_ECHO_PARAM)
	@ResponseBody
	public DataTableModel getAutomationRequestTraitmentModel(final DataTableDrawParameters params, final Locale locale) {

		Pageable pageable = SpringPagination.pageable(params,automationRequestMapper);
		ColumnFiltering filtering = new DataTableColumnFiltering(params, automationRequestMapper);
		Page<AutomationRequest> automationRequestPage = automationRequestFinderService.findRequestsWithTransmittedStatus(pageable, filtering);

		return new AutomationRequestDataTableModelHelper(messageSource).buildDataModel(automationRequestPage, "");
	}

	@RequestMapping(value="automation-request/global", params = RequestParams.S_ECHO_PARAM)
	@ResponseBody
	public DataTableModel getAutomationRequestGlobalModel(final DataTableDrawParameters params, final Locale locale) {

		Pageable pageable = SpringPagination.pageable(params,automationRequestMapper);
		ColumnFiltering filtering = new DataTableColumnFiltering(params, automationRequestMapper);
		Page<AutomationRequest> automationRequestPage = automationRequestFinderService.findRequestsForGlobal(pageable, filtering);

		return new AutomationRequestDataTableModelHelper(messageSource).buildDataModel(automationRequestPage, "");
	}


	@RequestMapping(value = "count", method = RequestMethod.GET)
	@ResponseBody
	public Integer countAutomationRequestToCurrentUser() {
		return automationRequestFinderService.countAutomationRequestForCurrentUser();
	}

	@RequestMapping(value = "assigned/testers/{requestStatus}", method = RequestMethod.GET)
	@ResponseBody
	public Map<Long, String> getTestersForCurrentUser(@PathVariable List<String> requestStatus) {
		return automationRequestFinderService.getCreatedByForCurrentUser(requestStatus);
	}

	@RequestMapping(value = "testers/{requestStatus}", method = RequestMethod.GET)
	@ResponseBody
	public Map<Long, String> getTesters(@PathVariable List<String> requestStatus) {
		return automationRequestFinderService.getCreatedByForAutomationRequests(requestStatus);
	}

	private Map<String, String> getAssignableUsersGlobalView() {

		List<User> usersList = automationRequestFinderService.getAssignedToForAutomationRequests();

		Map<String, String> jsonUsers = new LinkedHashMap<>(usersList.size());

		for (User user : usersList) {
			jsonUsers.put(user.getId().toString(),HtmlUtils.htmlEscape( user.getLogin()));
		}

		return jsonUsers;
	}


	private static final class AutomationRequestDataTableModelHelper extends DataTableModelBuilder<AutomationRequest> {
		private InternationalizationHelper messageSource;
		private Locale locale = LocaleContextHolder.getLocale();

		private AutomationRequestDataTableModelHelper(InternationalizationHelper messageSource) {
			this.messageSource = messageSource;;
		}

		@Override
		protected Object buildItemData(AutomationRequest item) {
			final AuditableMixin auditable = (AuditableMixin) item.getTestCase();
			Map<String, Object> data = new HashMap<>(14);
			data.put(DataTableModelConstants.PROJECT_NAME_KEY, item.getTestCase() != null ? HtmlUtils.htmlEscape(item.getTestCase().getProject().getName()): null);
			data.put("reference", (item.getTestCase() != null && !item.getTestCase().getReference().isEmpty()) ? item.getTestCase().getReference(): "-");
			data.put(DataTableModelConstants.DEFAULT_ENTITY_NAME_KEY, item.getTestCase() != null ? HtmlUtils.htmlEscape(item.getTestCase().getName()): null);
			data.put("format", item.getTestCase() != null ? messageSource.internationalize(item.getTestCase().getKind().getI18nKey(), locale) : null);
			data.put(DataTableModelConstants.DEFAULT_ENTITY_ID_KEY, item.getTestCase() != null ? item.getTestCase().getId() : null);
			data.put(DataTableModelConstants.DEFAULT_CREATED_BY_KEY, item.getTransmittedBy() != null ? item.getTransmittedBy().getLogin(): item.getCreatedBy().getLogin());
			data.put("transmitted-on", messageSource.localizeShortDate(item.getTransmissionDate(), locale));
			data.put("priority", item.getAutomationPriority() != null ? item.getAutomationPriority() : "-");
			data.put("assigned-on", messageSource.localizeShortDate(item.getAssignmentDate(), locale));
			data.put("entity-index", getCurrentIndex());
			data.put("script", (item.getTestCase() != null && item.getTestCase().getAutomatedTest() != null) ? item.getTestCase().getAutomatedTest().getFullLabel(): null);
			data.put("checkbox", "");
			data.put("tc-id", item.getTestCase() != null ? item.getTestCase().getId(): null);
			data.put("requestId", item.getId());
			data.put("assigned-to", item.getAssignedTo() != null ? item.getAssignedTo().getLogin() : "-");
			data.put("status", messageSource.internationalize(item.getRequestStatus().getI18nKey(), locale));
			return data;
		}


	}

}
