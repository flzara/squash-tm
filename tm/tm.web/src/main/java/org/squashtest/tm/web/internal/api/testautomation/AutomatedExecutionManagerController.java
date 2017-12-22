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
package org.squashtest.tm.web.internal.api.testautomation;

import javax.inject.Inject;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.api.testautomation.execution.dto.TestExecutionStatus;
import org.squashtest.tm.service.testautomation.AutomatedExecutionManagerService;

/**
 * This controller receives callbacks from Squash TA which modify automated executions statuses.
 *
 * @author Gregory Fouquet
 *
 */
@Controller
@RequestMapping("/automated-executions")
public class AutomatedExecutionManagerController {
	@Inject
	private AutomatedExecutionManagerService automatedExecutionManager;

	/**
	 * Changes the status of the automated execution
	 *
	 * @param id
	 *            the automated exec extender id.
	 * @param stateChange
	 */
	@ResponseBody
	@RequestMapping(value = "/{id}/test-status", method = RequestMethod.POST)
	public
	void changeExecutionState(@PathVariable long id, @RequestBody @Valid TestExecutionStatus stateChange) {
		automatedExecutionManager.changeExecutionState(id, stateChange);
	}
}
