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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.api.workspace.WorkspaceType;
import org.squashtest.tm.core.foundation.collection.ColumnFiltering;
import org.squashtest.tm.domain.testcase.TestCaseKind;
import org.squashtest.tm.domain.tf.automationrequest.AutomationRequest;
import org.squashtest.tm.domain.tf.automationrequest.AutomationRequestStatus;
import org.squashtest.tm.service.configuration.ConfigurationService;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.tf.AutomationRequestFinderService;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableColumnFiltering;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelConstants;
import org.squashtest.tm.web.internal.model.datatable.SpringPagination;
import org.squashtest.tm.web.internal.model.viewmapper.DatatableMapper;
import org.squashtest.tm.web.internal.model.viewmapper.NameBasedMapper;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.squashtest.tm.domain.tf.automationrequest.AutomationRequestStatus.AUTOMATION_IN_PROGRESS;
import static org.squashtest.tm.domain.tf.automationrequest.AutomationRequestStatus.TRANSMITTED;

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

	@Inject
	private PermissionEvaluationService permissionEvaluationService;

	@Inject
	private ConfigurationService configurationService;

	private final DatatableMapper<String> automationRequestMapper = new NameBasedMapper()
		.map(DataTableModelConstants.PROJECT_NAME_KEY, "testCase.project.name")
		.map("reference", "testCase.reference")
		.map(DataTableModelConstants.DEFAULT_ENTITY_NAME_KEY, "testCase.name")
		.map("format", "testCase.kind")
		.map(DataTableModelConstants.DEFAULT_ENTITY_ID_KEY, "testCase.id")
		.map(DataTableModelConstants.DEFAULT_CREATED_BY_KEY, "testCase.audit.lastModifiedBy")
		.map("transmitted-on", "transmissionDate")
		.map("priority", "automationPriority")
		.map("status", "requestStatus")
		.map("conflictAssociation", "testCase.automationRequest.conflictAssociation")
		.map("assigned-on", "assignmentDate")
		.map("script", "testCase.automatedTest.name")
		.map("entity-index", "index(AutomationRequest)")
		.map("requestId", "id")
		.map("assigned-to", "assignedTo.login")
	    .map("uuid", "testCase.uuid")
		.map("listScriptConflict", "testCase.automationRequest.conflictAssociation")
		.map("scm-url", "testCase.sourceCodeRepositoryUrl")
		.map("automated-test-reference", "testCase.automatedTestReference");

	@RequestMapping(method = RequestMethod.GET)
	public String showWorkspace(Model model, Locale locale) {

		Map<String, String> automReqStatuses =
			Stream.of(AutomationRequestStatus.values())
				.collect(Collectors.toMap(Enum::name, e -> messageSource.internationalize(e.getI18nKey(), locale),(u, v) -> {
					throw new IllegalStateException(String.format("Duplicate key %s", u));
				}, LinkedHashMap::new));
		List<String> automReqStatusList = new ArrayList<>(automReqStatuses.keySet());
		Map<Long, String> assignableUsers = automationRequestFinderService.getTcLastModifiedByForCurrentUser(automReqStatusList);
		Map<Long, String> globalUsers = automationRequestFinderService
			.getTcLastModifiedByForAutomationRequests(automReqStatusList);

		Map<Long, String> traitmentUsers = automationRequestFinderService
			.getTcLastModifiedByToAutomationRequestNotAssigned(Arrays.asList(TRANSMITTED.name(), AUTOMATION_IN_PROGRESS.name()));

		Map<Long, String> assignableUsersGlobalView = automationRequestFinderService.getAssignedToForAutomationRequests();

		model.addAttribute("assignableUsers", assignableUsers);
		model.addAttribute("traitmentUsers", traitmentUsers);
		model.addAttribute("globalUsers", globalUsers);
		model.addAttribute("assignableUsersGlobalView", assignableUsersGlobalView);

		Map<String, String> tcKinds =
			Arrays.stream(TestCaseKind.values()).collect(Collectors.toMap(Enum::toString, e -> messageSource.internationalize(e.getI18nKey(), locale)));
		model.addAttribute("tcKinds", tcKinds);

		Map<String, String> autoReqStatusesTraitment =
			Stream.of(TRANSMITTED, AUTOMATION_IN_PROGRESS)
				  .collect(Collectors.toMap(Enum::toString, e -> messageSource.internationalize(e.getI18nKey(), locale)));
		model.addAttribute("autoReqStatusesTraitment", autoReqStatusesTraitment);

		model.addAttribute("autoReqStatuses", automReqStatuses);

		// License information
		String userLicenseInformation = configurationService.findConfiguration(ConfigurationService.Properties.ACTIVATED_USER_EXCESS);
		String dateLicenseInformation = configurationService.findConfiguration(ConfigurationService.Properties.PLUGIN_LICENSE_EXPIRATION);

		model.addAttribute("userLicenseInformation", userLicenseInformation);
		model.addAttribute("dateLicenseInformation", (dateLicenseInformation == null || dateLicenseInformation.isEmpty()) ? null : Integer.valueOf(dateLicenseInformation));

		return getWorkspaceViewName();
	}



	protected String getWorkspaceViewName() {
		return "automation-workspace.html";
	}

	protected WorkspaceType getWorkspaceType() {
		return WorkspaceType.AUTOMATION_WORKSPACE;
	}

	@RequestMapping(value="automation-requests", params = RequestParams.S_ECHO_PARAM)
	@ResponseBody
	public DataTableModel getAutomationRequestModel(final DataTableDrawParameters params, final Locale locale) {

		Pageable pageable = SpringPagination.pageable(params,automationRequestMapper);
		ColumnFiltering filtering = new DataTableColumnFiltering(params, automationRequestMapper);
		Page<AutomationRequest> automationRequestPage = automationRequestFinderService.findRequestsAssignedToCurrentUser(pageable, filtering);

		return new AutomationRequestDataTableModelHelper(messageSource, permissionEvaluationService).buildDataModel(automationRequestPage, "");
	}

	@RequestMapping(value="automation-requests/traitment", params = RequestParams.S_ECHO_PARAM)
	@ResponseBody
	public DataTableModel getAutomationRequestTraitmentModel(final DataTableDrawParameters params, final Locale locale) {

		Pageable pageable = SpringPagination.pageable(params,automationRequestMapper);
		ColumnFiltering filtering = new DataTableColumnFiltering(params, automationRequestMapper);
		Page<AutomationRequest> automationRequestPage = automationRequestFinderService.findRequestsWithTransmittedStatus(pageable, filtering);

		return new AutomationRequestDataTableModelHelper(messageSource, permissionEvaluationService).buildDataModel(automationRequestPage, "");
	}

	@RequestMapping(value="automation-requests/global", params = RequestParams.S_ECHO_PARAM)
	@ResponseBody
	public DataTableModel getAutomationRequestGlobalModel(final DataTableDrawParameters params, final Locale locale) {

		Pageable pageable = SpringPagination.pageable(params,automationRequestMapper);
		ColumnFiltering filtering = new DataTableColumnFiltering(params, automationRequestMapper);
		Page<AutomationRequest> automationRequestPage = automationRequestFinderService.findRequestsForGlobal(pageable, filtering);

		return new AutomationRequestDataTableModelHelper(messageSource, permissionEvaluationService).buildDataModel(automationRequestPage, "");
	}


	@RequestMapping(value = "count", method = RequestMethod.GET)
	@ResponseBody
	public Integer countAutomationRequestToCurrentUser() {
		return automationRequestFinderService.countAutomationRequestForCurrentUser();
	}

	@RequestMapping(value = "assigned/testers/{statuses}", method = RequestMethod.GET)
	@ResponseBody
	public Map<Long, String> getTestersForCurrentUser(@PathVariable List<String> statuses) {
		return automationRequestFinderService.getTcLastModifiedByForCurrentUser(statuses);
	}

	@RequestMapping(value = "global/testers/{statuses}", method = RequestMethod.GET)
	@ResponseBody
	public Map<Long, String> getTesters(@PathVariable List<String> statuses) {
		return automationRequestFinderService.getTcLastModifiedByForAutomationRequests(statuses);
	}

	@RequestMapping(value = "traitment/testers/{statuses}", method = RequestMethod.GET)
	@ResponseBody
	public Map<Long, String> getTestersForTraitment(@PathVariable List<String> statuses) {
		return automationRequestFinderService.getTcLastModifiedByToAutomationRequestNotAssigned(statuses);
	}

	@RequestMapping(value = "assignee", method = RequestMethod.GET)
	@ResponseBody
	public Map<Long, String> getAssignee() {
		return automationRequestFinderService.getAssignedToForAutomationRequests();
	}
}
