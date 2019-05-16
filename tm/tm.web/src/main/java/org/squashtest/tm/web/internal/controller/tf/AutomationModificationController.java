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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.domain.tf.automationrequest.AutomationRequestStatus;
import org.squashtest.tm.service.testautomation.model.TestAutomationProjectContent;
import org.squashtest.tm.service.testcase.TestCaseModificationService;
import org.squashtest.tm.service.tf.AutomationRequestModificationService;
import org.squashtest.tm.web.internal.model.testautomation.TATestNode;
import org.squashtest.tm.web.internal.model.testautomation.TATestNodeListBuilder;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.squashtest.tm.web.internal.controller.RequestParams.ITERATION_ID;
import static org.squashtest.tm.web.internal.controller.RequestParams.TEST_PLAN_ITEMS_IDS;
import static org.squashtest.tm.web.internal.controller.RequestParams.TEST_SUITE_ID;
import static org.squashtest.tm.web.internal.helper.JEditablePostParams.VALUE;
import static org.squashtest.tm.web.internal.http.ContentTypes.APPLICATION_JSON;

@Controller
@RequestMapping("/automation-requests")
public class AutomationModificationController {

	public static final Logger LOGGER = LoggerFactory.getLogger(AutomationModificationController.class);
	private static final String TEST_CASE_ID = "testCaseId";
	private static final String PATH = "path";

	private TestCaseModificationService testCaseModificationService;

	@Inject
	public void setTestCaseModificationService(TestCaseModificationService testCaseModificationService) {
		this.testCaseModificationService = testCaseModificationService;
	}

	@Inject
	private AutomationRequestModificationService automationRequestModificationService;

	@RequestMapping(method = RequestMethod.POST, value="/{tcIds}", params = {"id=automation-request-status", VALUE})
	@ResponseBody
	public void changeStatus(@PathVariable List<Long> tcIds, @RequestParam(VALUE) AutomationRequestStatus status) {
		automationRequestModificationService.changeStatus(tcIds, status);
	}

	@ResponseBody
	@RequestMapping(method = RequestMethod.POST, value="/unassigned", params = {"tcIds[]"})
	public void unassignedUser(@RequestParam("tcIds[]") List<Long> tcIds) {
		automationRequestModificationService.unassignRequests(tcIds);
	}

	@RequestMapping(value = "{testCaseId}/tests", method = RequestMethod.POST, params = {PATH})
	@ResponseBody
	public String bindAutomatedTest(@PathVariable(TEST_CASE_ID) long testCaseId, @RequestParam(PATH) String testPath) {
		testCaseModificationService.bindAutomatedTestByAutomationProgrammer(testCaseId, testPath);
		return testPath;
	}

	@RequestMapping(value = "{testCaseId}/tests", method = RequestMethod.GET)
	@ResponseBody
	public Collection<TATestNode> findAssignableAutomatedTests(@PathVariable(TEST_CASE_ID) Long testCaseId) {
		Collection<TestAutomationProjectContent> projectContents = testCaseModificationService.findAssignableAutomationTestsToAutomationProgramer(testCaseId);
		return new TATestNodeListBuilder().build(projectContents);

	}

	@RequestMapping(value = "/assignee", method = RequestMethod.POST, params = {"tcIds[]"})
	@ResponseBody
	public void assigneeToAutomationReq(@RequestParam("tcIds[]") List<Long> tcIds) {
		automationRequestModificationService.assignedToRequest(tcIds);
	}

	@RequestMapping(value = "/associate-TA-script", method = RequestMethod.POST, params = {"tcIds[]"})
	@ResponseBody
	public Map<Long, String> resolveTAScriptAssociation(@RequestParam("tcIds[]") List<Long> tcIds){
		return automationRequestModificationService.updateTAScript(tcIds);
	}

	/*TM-13: update automatic association */

	@RequestMapping(value = "/associate-TA-script", method = RequestMethod.POST, params = {ITERATION_ID}, produces = APPLICATION_JSON)
	@ResponseBody
	public Map<Long, String> resolveTAScriptAssociationForIteration(@RequestParam(ITERATION_ID) long iterationId) {

		return automationRequestModificationService.updateTAScriptForIteration(iterationId);
	}

	@RequestMapping(value = "/associate-TA-script", method = RequestMethod.POST, params = {TEST_SUITE_ID, "!testPlanItemsIds[]"}, produces = APPLICATION_JSON)
	@ResponseBody
	public Map<Long, String> resolveTAScriptAssociationForTestSuite(@RequestParam(TEST_SUITE_ID) long testSuiteId) {

		return automationRequestModificationService.updateTAScriptForTestSuite(testSuiteId);
	}

	@RequestMapping(value = "/associate-TA-script", method = RequestMethod.POST, params = {TEST_PLAN_ITEMS_IDS, ITERATION_ID}, produces = APPLICATION_JSON)
	@ResponseBody
	public Map<Long, String> resolveTAScriptAssociationForIterationItems
		(@RequestParam("testPlanItemsIds[]") List<Long> testPlanIds) {
		return automationRequestModificationService.updateTAScriptForItems(testPlanIds);
	}

	@RequestMapping(value = "/associate-TA-script", method = RequestMethod.POST, params = {TEST_PLAN_ITEMS_IDS, TEST_SUITE_ID}, produces = APPLICATION_JSON)
	@ResponseBody
	public Map<Long, String> resolveTAScriptAssociationForTestSuiteItems(
		@RequestParam("testPlanItemsIds[]") List<Long> testPlanIds) {
		return automationRequestModificationService.updateTAScriptForItems(testPlanIds);
	}
}
