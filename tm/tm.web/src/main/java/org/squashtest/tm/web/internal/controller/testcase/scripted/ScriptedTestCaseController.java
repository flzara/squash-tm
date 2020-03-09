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

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.domain.testcase.ScriptedTestCaseLanguage;
import org.squashtest.tm.service.testcase.scripted.ScriptedTestCaseService;
import org.squashtest.tm.web.internal.controller.AcceptHeaders;

import javax.inject.Inject;

@RequestMapping("/scripted-test-cases")
@Controller
public class ScriptedTestCaseController {

	@Inject
	private ScriptedTestCaseService scriptedTestCaseService;

	@ResponseBody
	@RequestMapping(value = "/{testCaseId}", method = RequestMethod.POST, headers = AcceptHeaders.CONTENT_JSON)
	public ScriptedTestCaseModel updateTcScript(@PathVariable Long testCaseId, @RequestBody ScriptedTestCaseModel scriptedTestCaseModel) {
		scriptedTestCaseService.updateTcScript(testCaseId, scriptedTestCaseModel.getScript());
		return scriptedTestCaseModel;
	}

	@ResponseBody
	@RequestMapping(path = "/validate", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, headers = AcceptHeaders.CONTENT_JSON)
	public ScriptedTestCaseModel validateTcScript(@RequestBody ScriptedTestCaseModel scriptedTestCaseModel) {
		scriptedTestCaseService.validateScript(scriptedTestCaseModel.getScript());
		return scriptedTestCaseModel;
	}
}
