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
package org.squashtest.tm.web.internal.controller.testcase.keyword;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.domain.bdd.ActionWord;
import org.squashtest.tm.domain.testcase.KeywordTestCase;
import org.squashtest.tm.service.actionword.ActionWordService;
import org.squashtest.tm.service.testcase.bdd.KeywordTestCaseFinder;
import org.squashtest.tm.service.testcase.bdd.KeywordTestCaseService;

import javax.inject.Inject;
import javax.websocket.server.PathParam;
import java.util.Collection;

@Controller
@RequestMapping("/keyword-test-cases")
public class KeywordTestCaseController {

	@Inject
	KeywordTestCaseFinder keywordTestCaseFinder;

	@Inject
	private KeywordTestCaseService keywordTestCaseService;

	@Autowired(required = false)
	private ActionWordService actionWordService;

	@ResponseBody
	@RequestMapping("/autocomplete")
	public Collection<String> findAllMatchingActionWords(
		@RequestParam Long projectId,
		@RequestParam String searchInput) {
		return actionWordService.findAllMatchingActionWords(projectId, searchInput);
	}

	@ResponseBody
	@RequestMapping("/{testCaseId}/generated-script")
	public String getGeneratedScript(@PathVariable long testCaseId) {
		KeywordTestCase keywordTestCase = keywordTestCaseFinder.findById(testCaseId);
		return keywordTestCaseService.writeScriptFromTestCase(keywordTestCase, true);
	}

}
