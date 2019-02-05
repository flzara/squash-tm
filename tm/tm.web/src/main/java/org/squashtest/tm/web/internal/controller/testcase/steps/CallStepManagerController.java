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
package org.squashtest.tm.web.internal.controller.testcase.steps;

import org.apache.commons.collections.MultiMap;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.tm.service.internal.dto.UserDto;
import org.squashtest.tm.service.internal.dto.json.JsTreeNode;
import org.squashtest.tm.service.milestone.ActiveMilestoneHolder;
import org.squashtest.tm.service.testcase.CallStepManagerService;
import org.squashtest.tm.service.testcase.TestCaseLibraryFinderService;
import org.squashtest.tm.service.user.UserAccountService;
import org.squashtest.tm.service.workspace.WorkspaceDisplayService;
import org.squashtest.tm.web.internal.helper.JsTreeHelper;
import org.squashtest.tm.web.internal.model.builder.DriveNodeBuilder;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// XSS OK
@Controller
public class CallStepManagerController {

	@Inject
	@Named("testCase.driveNodeBuilder")
	private Provider<DriveNodeBuilder<TestCaseLibraryNode>> driveNodeBuilder;

	@Inject
	private ActiveMilestoneHolder activeMilestoneHolder;

	@Inject
	@Named("testCaseWorkspaceDisplayService")
	private WorkspaceDisplayService testCaseWorkspaceDisplayService;

	@Inject
	protected UserAccountService userAccountService;

	private CallStepManagerService callStepManagerService;
	private TestCaseLibraryFinderService testCaseLibraryFinder;

	@Inject
	public void setCallStepManagerService(CallStepManagerService callStepManagerService) {
		this.callStepManagerService = callStepManagerService;
	}

	@RequestMapping(value = "/test-cases/{testCaseId}/called-test-cases/manager", method = RequestMethod.GET)
	public ModelAndView showManager(@PathVariable long testCaseId, @CookieValue(value = "jstree_open", required = false, defaultValue = "") String[] openedNodes) {
		TestCase testCase = callStepManagerService.findTestCase(testCaseId);

		MultiMap expansionCandidates = JsTreeHelper.mapIdsByType(openedNodes);
		UserDto currentUser = userAccountService.findCurrentUserDto();

		List<Long> linkableTestCaseLibraryIds = testCaseLibraryFinder.findLinkableTestCaseLibraries().stream()
			.map(TestCaseLibrary::getId).collect(Collectors.toList());
		Optional<Long> activeMilestoneId = activeMilestoneHolder.getActiveMilestoneId();
		Collection<JsTreeNode> linkableLibrariesModel = testCaseWorkspaceDisplayService.findAllLibraries(linkableTestCaseLibraryIds, currentUser, expansionCandidates, activeMilestoneId.get());


		ModelAndView mav = new ModelAndView("page/test-case-workspace/show-call-step-manager");
		mav.addObject("testCase", testCase);
		mav.addObject("rootModel", linkableLibrariesModel);

		return mav;
	}

	@RequestMapping(value = "/test-cases/{callingTestCaseId}/called-test-cases", method = RequestMethod.POST, params = "called-test-case[]")
	@ResponseBody
	public void addCallTestStep(@PathVariable("callingTestCaseId") long callingTestCaseId,
								@RequestParam("called-test-case[]") List<Long> calledTestCaseIds, @RequestParam("index") Integer index) {

		callStepManagerService.addCallTestSteps(callingTestCaseId, calledTestCaseIds, index);

	}

	private List<JsTreeNode> createLinkableLibrariesModel(List<TestCaseLibrary> linkableLibraries) {
		DriveNodeBuilder<TestCaseLibraryNode> builder = driveNodeBuilder.get();
		List<JsTreeNode> linkableLibrariesModel = new ArrayList<>();

		for (TestCaseLibrary library : linkableLibraries) {
			JsTreeNode libraryNode = builder.setModel(library).build();
			linkableLibrariesModel.add(libraryNode);
		}
		return linkableLibrariesModel;
	}

	/**
	 * @param testCaseLibraryFinder
	 *            the testCaseLibraryFinder to set
	 */
	@Inject
	public void setTestCaseLibraryFinder(TestCaseLibraryFinderService testCaseLibraryFinder) {
		this.testCaseLibraryFinder = testCaseLibraryFinder;
	}
}
