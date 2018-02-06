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

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.squashtest.tm.core.foundation.lang.UrlUtils;
import org.squashtest.tm.service.testautomation.TestAutomationServerManagerService;
import org.squashtest.tm.web.internal.helper.JEditablePostParams;
import org.squashtest.tm.web.internal.model.testautomation.TAUsageStatus;
import org.squashtest.tm.web.internal.util.HTMLCleanupUtils;

import javax.inject.Inject;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

// XSS OK
@Controller
@RequestMapping("/test-automation-servers")
public class TestAutomationServerController {

	private static final String SERVER_ID = "serverId";

	@Inject
	private TestAutomationServerManagerService service;
	private static final Logger LOGGER = LoggerFactory.getLogger(TestAutomationServerController.class);

	@RequestMapping(value = "/{serverId}/name", method = RequestMethod.POST, params = JEditablePostParams.VALUE)
	@ResponseBody
	public String changeName(@PathVariable(SERVER_ID) long serverId,
							 @RequestParam(JEditablePostParams.VALUE) String newName) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Change name for test automation server of id #{}", serverId);
		}
		service.changeName(serverId, newName);
		return newName;
	}

	@RequestMapping(value = "/{serverId}/description", method = RequestMethod.POST, params = JEditablePostParams.VALUE)
	@ResponseBody
	public String changeDescription(@PathVariable(SERVER_ID) long serverId, @RequestParam(JEditablePostParams.VALUE) String newDescription) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Change description for test automation server of id #{}", serverId);
		}
		service.changeDescription(serverId, newDescription);
		return HTMLCleanupUtils.cleanHtml(newDescription);
	}


	@RequestMapping(value = "/{serverId}/baseURL", method = RequestMethod.POST, params = JEditablePostParams.VALUE)
	@ResponseBody
	public String changeURL(@PathVariable(SERVER_ID) long serverId, @RequestParam(JEditablePostParams.VALUE) String newURL) {
		URL url = UrlUtils.toUrl(newURL);
		service.changeURL(serverId, url);
		return newURL;
	}

	@RequestMapping(value = "/{serverId}/login", method = RequestMethod.POST, params = JEditablePostParams.VALUE)
	@ResponseBody
	public String changeLogin(@PathVariable(SERVER_ID) long serverId,
							  @RequestParam(JEditablePostParams.VALUE) String newLogin) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Change login for test automation server of id #{}", serverId);
		}
		service.changeLogin(serverId, newLogin);
		return newLogin;
	}

	@RequestMapping(value = "/{serverId}/password", method = RequestMethod.POST, params = JEditablePostParams.VALUE)
	@ResponseBody
	public String changePassword(@PathVariable(SERVER_ID) long serverId,
								 @RequestParam(JEditablePostParams.VALUE) String newPassword) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Change password for test automation server of id #{}", serverId);
		}
		service.changePassword(serverId, newPassword);
		return newPassword;
	}

	@RequestMapping(value = "/{serverId}/manualSelection", method = RequestMethod.POST, params = JEditablePostParams.VALUE)
	@ResponseBody
	public Boolean changeManualSelection(@PathVariable(SERVER_ID) long serverId,
										 @RequestParam(JEditablePostParams.VALUE) Boolean manualSelection) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Change manual slave selection for test automation server of id #{}", serverId);
		}
		service.changeManualSlaveSelection(serverId, manualSelection);
		return manualSelection;
	}

	@RequestMapping(value = "/{serverId}", method = RequestMethod.DELETE)
	@ResponseBody
	public void deleteTestAutomationServer(@PathVariable List<Long> serverId) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Delete test automation server of id #{}", serverId);
		}
		service.deleteServer(serverId);
	}

	@RequestMapping(value = "/new", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.CREATED)
	@ResponseBody
	public void createNew(@RequestBody NewTestAutomationServer server) {

		if (LOGGER.isDebugEnabled()) { // w/o this test string rep is always build
			LOGGER.debug("Add new Test automation server : {}", ToStringBuilder.reflectionToString(server));
		}

		service.persist(server.createTransientEntity());
	}

	@RequestMapping(value = "/{serverId}/usage-status", method = RequestMethod.GET)
	@ResponseBody
	public TAUsageStatus getTestAutomationUsageStatus(@PathVariable List<Long> serverId) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Delete test automation server of id #{}", serverId);
		}
		List<TAUsageStatus> liste = new ArrayList<>();
		for (Long id : serverId) {
			boolean hasBoundProject = service.hasBoundProjects(id);
			boolean hasExecutedTests = service.hasExecutedTests(id);
			TAUsageStatus taUsage = new TAUsageStatus(hasBoundProject, hasExecutedTests);
			liste.add(taUsage);
		}

		TAUsageStatus tABoolean = new TAUsageStatus(true);
		for (TAUsageStatus taUsageStatus : liste) {
			if (!taUsageStatus.isHasBoundProject() && !taUsageStatus.isHasExecutedTests()) {
				tABoolean = new TAUsageStatus(false);
			}
		}

		return tABoolean;
	}


}
