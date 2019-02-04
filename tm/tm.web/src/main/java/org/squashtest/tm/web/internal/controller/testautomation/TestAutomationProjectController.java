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
package org.squashtest.tm.web.internal.controller.testautomation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.exception.DomainException;
import org.squashtest.tm.service.testautomation.TestAutomationProjectManagerService;
import org.squashtest.tm.web.internal.model.testautomation.TAUsageStatus;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

// XSS OK
@Controller
@RequestMapping("/test-automation-projects")
public class TestAutomationProjectController {

	@Inject
	private TestAutomationProjectManagerService service;

	private static final Logger LOGGER = LoggerFactory.getLogger(TestAutomationServerController.class);

	private static final String PROJECT_ID = "/{projectId}";

	@RequestMapping(value = PROJECT_ID, method = RequestMethod.DELETE)
	@ResponseBody
	public void deleteTestAutomationProject(@PathVariable long projectId) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Delete test automation project of id #{}", projectId);
		}
		service.deleteProject(projectId);
	}

	@RequestMapping(value = PROJECT_ID, method = RequestMethod.PUT)
	@ResponseBody
	public void editTestAutomationProject(@PathVariable long projectId, @RequestBody TestAutomationProject newValues) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Edit test automation project of id #{}", projectId);
		}
		try {
			service.editProject(projectId, newValues);
		} catch (DomainException de) {
			de.setObjectName("ta-project");
			throw de;
		}
	}

	@RequestMapping(value = PROJECT_ID + "/usage-status", method = RequestMethod.GET)
	@ResponseBody
	public TAUsageStatus getTestAutomationUsageStatus(@PathVariable List<Long> projectId) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Delete test automation server of id #{}", projectId);
		}
		List<TAUsageStatus> liste = new ArrayList<>();
		for (Long id : projectId) {
			boolean hasExecutedTests = service.hasExecutedTests(id);
			TAUsageStatus taUsage = new TAUsageStatus(hasExecutedTests);
			liste.add(taUsage);
		}

		TAUsageStatus tABoolean = new TAUsageStatus(true);
		for (TAUsageStatus taUsageStatus : liste) {
			if (!taUsageStatus.isHasExecutedTests()) {
				tABoolean = new TAUsageStatus(false);
			}
		}

		return tABoolean;
	}

}
