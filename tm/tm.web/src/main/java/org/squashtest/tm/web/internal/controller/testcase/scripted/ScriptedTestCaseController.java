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
package org.squashtest.tm.web.internal.controller.testcase.scripted;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.squashtest.tm.service.testcase.ScriptedTestCaseService;
import org.squashtest.tm.web.internal.controller.AcceptHeaders;

import javax.inject.Inject;

@RequestMapping("/test-cases/{testCaseId}")
@Controller
public class ScriptedTestCaseController {

	@Inject
	private ScriptedTestCaseService scriptedTestCaseService;

	@ResponseBody
	@RequestMapping(path = "/scripted",method = RequestMethod.POST, headers = AcceptHeaders.CONTENT_JSON)
	public ScriptedTestCaseModel updateTcScript(@PathVariable Long testCaseId, @RequestBody ScriptedTestCaseModel scriptedTestCaseModel) {
		scriptedTestCaseService.updateTcScript(testCaseId,scriptedTestCaseModel.getScript());
		return scriptedTestCaseModel;
	}
}
