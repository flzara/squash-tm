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
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.tf.AutomationRequestFinderService;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.*;
import org.squashtest.tm.web.internal.model.viewmapper.DatatableMapper;
import org.squashtest.tm.web.internal.model.viewmapper.NameBasedMapper;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@RequestMapping("/automation-tester-workspace")
public class AutomationTesterWorkspaceController {

	@Inject
	private InternationalizationHelper messageSource;

	@Inject
	private PermissionEvaluationService permissionEvaluationService;

	@Inject
	private AutomationRequestFinderService automationRequestFinderService;

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
		.map("assigned-on", "assignmentDate")
		.map("script", "testCase.automatedTest.name")
		.map("entity-index", "index(AutomationRequest)")
		.map("requestId", "id")
		.map("assigned-to", "assignedTo");

	@RequestMapping(method = RequestMethod.GET)
	public String showWorkspace(Model model, Locale locale) {
		Map<String, String> tcKinds =
			Arrays.stream(TestCaseKind.values()).collect(Collectors.toMap(Enum::toString, e -> messageSource.internationalize(e.getI18nKey(), locale)));
		Map<String, String> autoReqStatusesValidateView =
			Stream.of(AutomationRequestStatus.SUSPENDED, AutomationRequestStatus.WORK_IN_PROGRESS, AutomationRequestStatus.REJECTED)
				.collect(Collectors.toMap(Enum::toString, e -> messageSource.internationalize(e.getI18nKey(), locale)));
		Map<String, String> autoReqStatusesGlobalView =
			Stream.of(AutomationRequestStatus.values())
				.collect(Collectors.toMap(Enum::toString, e -> messageSource.internationalize(e.getI18nKey(), locale)));
		model.addAttribute("autoReqStatusesValidateView", autoReqStatusesValidateView);
		model.addAttribute("autoReqStatusesGlobalView", autoReqStatusesGlobalView);
		model.addAttribute("tcKinds", tcKinds);
		model.addAttribute("testerTransmitted", automationRequestFinderService.getTcLastModifiedByForAutomationRequests(Collections.singletonList(AutomationRequestStatus.READY_TO_TRANSMIT.name())));
		model.addAttribute("testerValidate",
			automationRequestFinderService.getTcLastModifiedByForAutomationRequests(Collections.singletonList(AutomationRequestStatus.WORK_IN_PROGRESS.name())));

		List<String> allStatus = new ArrayList<>();
		for (AutomationRequestStatus automationRequestStatus: AutomationRequestStatus.values()) {
			allStatus.add(automationRequestStatus.name());
		}
		model.addAttribute("testerGlobalView",
			automationRequestFinderService.getTcLastModifiedByForAutomationRequests(allStatus));

		return getWorkspaceViewName();
	}

	protected String getWorkspaceViewName() {
		return "automation-tester-workspace.html";
	}

	protected WorkspaceType getWorkspaceType() {
		return WorkspaceType.AUTOMATION_TESTER_WORKSPACE;
	}

	@RequestMapping(value="/automation-request/transmitted", params = RequestParams.S_ECHO_PARAM)
	@ResponseBody
	public DataTableModel getTransmittedModel(final DataTableDrawParameters params, final Locale locale) {
		Pageable pageable = SpringPagination.pageable(params, automationRequestMapper);
		ColumnFiltering filtering = new DataTableColumnFiltering(params, automationRequestMapper);
		Page<AutomationRequest> automationRequestPage = automationRequestFinderService.findRequestsToTransmitted(pageable, filtering);

		return new AutomationRequestDataTableModelHelper(messageSource, permissionEvaluationService).buildDataModel(automationRequestPage, "");
	}

	@RequestMapping(value="/automation-request/validate", params = RequestParams.S_ECHO_PARAM)
	@ResponseBody
	public DataTableModel getValidateModel(final DataTableDrawParameters params, final Locale locale) {
		Pageable pageable = SpringPagination.pageable(params, automationRequestMapper);
		ColumnFiltering filtering = new DataTableColumnFiltering(params, automationRequestMapper);
		Page<AutomationRequest> automationRequestPage = automationRequestFinderService.findRequestsToValidate(pageable, filtering);

		return new AutomationRequestDataTableModelHelper(messageSource, permissionEvaluationService).buildDataModel(automationRequestPage, "");
	}

	@RequestMapping(value="/automation-request/global", params = RequestParams.S_ECHO_PARAM)
	@ResponseBody
	public DataTableModel getAutomationRequestGlobalModel(final DataTableDrawParameters params, final Locale locale) {

		Pageable pageable = SpringPagination.pageable(params,automationRequestMapper);
		ColumnFiltering filtering = new DataTableColumnFiltering(params, automationRequestMapper);
		Page<AutomationRequest> automationRequestPage = automationRequestFinderService.findRequests(pageable, filtering);

		return new AutomationRequestDataTableModelHelper(messageSource, permissionEvaluationService).buildDataModel(automationRequestPage, "");
	}

	@RequestMapping(value = "/count", method = RequestMethod.GET)
	@ResponseBody
	public Integer countAutomationRequestValid() {
		return automationRequestFinderService.countAutomationRequestValid();
	}

	@RequestMapping(value = "/lastModifiedBy/{reqStatuses}", method = RequestMethod.GET)
	@ResponseBody
	public Map<Long, String> getLastModifiedBy(@PathVariable List<String> reqStatuses) {
		return automationRequestFinderService.getTcLastModifiedByForAutomationRequests(reqStatuses);
	}
}
