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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.tm.service.testcase.CallStepManagerService;
import org.squashtest.tm.service.testcase.TestCaseLibraryFinderService;
import org.squashtest.tm.web.internal.model.builder.DriveNodeBuilder;
import org.squashtest.tm.service.internal.dto.json.JsTreeNode;

@Controller
public class CallStepManagerController {

	@Inject
	@Named("testCase.driveNodeBuilder")
	private Provider<DriveNodeBuilder<TestCaseLibraryNode>> driveNodeBuilder;

	private CallStepManagerService callStepManagerService;
	private TestCaseLibraryFinderService testCaseLibraryFinder;

	@Inject
	public void setCallStepManagerService(CallStepManagerService callStepManagerService) {
		this.callStepManagerService = callStepManagerService;
	}

	@RequestMapping(value = "/test-cases/{testCaseId}/called-test-cases/manager", method = RequestMethod.GET)
	public ModelAndView showManager(@PathVariable long testCaseId) {
		TestCase testCase = callStepManagerService.findTestCase(testCaseId);
		List<TestCaseLibrary> linkableLibraries = testCaseLibraryFinder.findLinkableTestCaseLibraries();

		List<JsTreeNode> linkableLibrariesModel = createLinkableLibrariesModel(linkableLibraries);

		ModelAndView mav = new ModelAndView("page/test-case-workspace/show-call-step-manager");
		mav.addObject("testCase", testCase);
		mav.addObject("rootModel", linkableLibrariesModel);

		return mav;
	}

	@RequestMapping(value = "/test-cases/{callingTestCaseId}/called-test-cases", method = RequestMethod.POST, params = "called-test-case[]")
	@ResponseBody
	public void addCallTestStep(@PathVariable("callingTestCaseId") long callingTestCaseId,
			@RequestParam("called-test-case[]") List<Long> calledTestCaseIds) {

			callStepManagerService.addCallTestSteps(callingTestCaseId, calledTestCaseIds);

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
