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
package org.squashtest.tm.web.internal.controller.administration;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.tm.service.testautomation.AutomatedSuiteManagerService;
import org.squashtest.tm.service.testautomation.AutomationDeletionCount;

import javax.inject.Inject;

@Controller
@RequestMapping("/administration/cleaning")
public class CleaningController {

	@Inject
	private AutomatedSuiteManagerService automatedSuiteManagerService;

	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView showCleaningPage() {
		ModelAndView mav = new ModelAndView("page/administration/cleaning");
		return mav;
	}

	@ResponseBody
	@RequestMapping(value = "/count", method = RequestMethod.GET)
	public AutomationDeletionCount getOldAutomatedSuitesAndExecutionsCount() {
		return automatedSuiteManagerService.countOldAutomatedSuitesAndExecutions();
	}

	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	public void cleanAutomatedSuitesAndExecutions() {
		automatedSuiteManagerService.cleanOldSuites();
	}

}
