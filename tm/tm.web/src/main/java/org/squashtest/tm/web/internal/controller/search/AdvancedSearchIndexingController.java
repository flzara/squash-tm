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
package org.squashtest.tm.web.internal.controller.search;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.service.advancedsearch.IndexationService;

@Controller
@RequestMapping("/advanced-search")
public class AdvancedSearchIndexingController {

	@Inject
	private IndexationService indexationService;

	@RequestMapping(value = "/index-all", method = RequestMethod.POST)
	@ResponseBody
	public void indexAll(){
		indexationService.indexAll();
	}

	@RequestMapping(value = "/index-requirements", method = RequestMethod.POST)
	@ResponseBody
	public void indexRequirements(){
		indexationService.indexRequirementVersions();
	}


	@RequestMapping(value = "/index-testcases", method = RequestMethod.POST)
	@ResponseBody
	public void indexTestCases(){
		indexationService.indexTestCases();
	}

	@RequestMapping(value = "/index-campaigns", method = RequestMethod.POST)
	@ResponseBody
	public void indexCampaigns(){
		indexationService.indexIterationTestPlanItem();
	}

	@RequestMapping(value = "/refresh", method = RequestMethod.POST)
	@ResponseBody
	public IndexingProgressModel refreshIndexPage(){

		return new IndexingProgressModel();
	}
}
