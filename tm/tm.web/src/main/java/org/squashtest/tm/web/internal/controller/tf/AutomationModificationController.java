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
import org.springframework.web.bind.annotation.*;
import org.squashtest.tm.service.tf.AutomationRequestModificationService;

import javax.inject.Inject;

@Controller
@RequestMapping("/automation-request")
public class AutomationModificationController {

	private static final Logger LOGGER = LoggerFactory.getLogger(AutomationModificationController.class);

	@Inject
	private AutomationRequestModificationService automationRequestModificationService;

	@ResponseBody
	@RequestMapping(method = RequestMethod.POST, value="/{autoReqIds}")
	public void changeStatus(@PathVariable(value="autoReqIds") Long autoReqIds) {
		automationRequestModificationService.updateAutomationRequestsToExecutable(autoReqIds);
	}

	@ResponseBody
	@RequestMapping(method = RequestMethod.POST, value="desassigned/{autoReqIds}")
	public void desassignedUser(@PathVariable(value="autoReqIds") Long autoReqIds) {
		automationRequestModificationService.desassignedUser(autoReqIds);
	}

	@ResponseBody
	@RequestMapping(method = RequestMethod.POST, value="assigned/{autoReqIds}")
	public void assignedUser(@PathVariable(value="autoReqIds") Long autoReqIds) {
		automationRequestModificationService.assignedToAutomationRequest(autoReqIds);
	}

	@ResponseBody
	@RequestMapping(method = RequestMethod.POST, value="notautomatable/{autoReqIds}")
	public void requestNotAutomatable(@PathVariable(value="autoReqIds") Long autoReqIds) {
		automationRequestModificationService.updateAutomationRequestsToNotAutomatable(autoReqIds);
	}

}
