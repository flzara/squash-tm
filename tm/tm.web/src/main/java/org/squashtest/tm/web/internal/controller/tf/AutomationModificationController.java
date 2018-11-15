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
import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.domain.tf.automationrequest.AutomationRequestStatus;
import org.squashtest.tm.service.tf.AutomationRequestModificationService;

import javax.inject.Inject;
import java.util.List;
import java.util.Locale;

import static org.squashtest.tm.web.internal.helper.JEditablePostParams.VALUE;

@Controller
@RequestMapping("/automation-request")
public class AutomationModificationController {

	@Inject
	private AutomationRequestModificationService automationRequestModificationService;

	@RequestMapping(method = RequestMethod.POST, value="/{autoReqIds}", params = {"id=automation-request-status", VALUE})
	@ResponseBody
	public void changeStatus(@PathVariable List<Long> autoReqIds, @RequestParam(VALUE) AutomationRequestStatus status) {
		automationRequestModificationService.changeStatus(autoReqIds, status);
	}

	@ResponseBody
	@RequestMapping(method = RequestMethod.POST, value="desassigned/{autoReqIds}")
	public void desassignedUser(@PathVariable(value="autoReqIds") List<Long> autoReqIds) {
		automationRequestModificationService.unassignedUser(autoReqIds);
	}
}
