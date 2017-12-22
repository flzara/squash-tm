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

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.tm.domain.campaign.CampaignLibraryNode;
import org.squashtest.tm.service.library.SearchService;
import org.squashtest.tm.web.internal.controller.RequestParams;

@Controller
@RequestMapping("/search")
public class SearchController {

	private static final String RESULT_LIST = "resultList";

	private static final String WORKSPACE = "workspace";

	private static final String NODE_NAME = "nodeName";

	private static final Logger LOGGER = LoggerFactory.getLogger(SearchController.class);
	private static final String NODE_NAME_REJEX = "-";

	@Inject
	private SearchService searchService;




	@RequestMapping(value = "campaigns/breadcrumb", method = RequestMethod.POST, params = {NODE_NAME})
	@ResponseBody
	public List<String> findBreadCrumbCampaign(@RequestParam(NODE_NAME) String nodeName){
		LOGGER.trace("search breadcrumb");

		String[] splitedNodeName = nodeName.split(NODE_NAME_REJEX);
		String className = splitedNodeName[0];
		Long nodeId = Long.parseLong(splitedNodeName[1]);
		return searchService.findBreadCrumbForCampaign(className, nodeId, NODE_NAME_REJEX);
	}



	@RequestMapping(value = "/campaigns", method = RequestMethod.GET, params = { "order" })
	public ModelAndView searchOrderedCampaigns(@RequestParam(RequestParams.NAME) String name, @RequestParam String order) {
		LOGGER.info("SQUASH INFO: TRY Campaign search with name : " + name);

		boolean isOrdered = Boolean.parseBoolean(order);

		List<CampaignLibraryNode> resultList = searchService.findCampaignByName(name, isOrdered);

		LOGGER.info("SQUASH INFO: DONE Campaign search with name : " + name);

		ModelAndView mav;

		if (isOrdered) {
			mav = new ModelAndView("fragment/campaigns/camp-search-result-ordered");
		} else {
			mav = new ModelAndView("fragment/campaigns/camp-search-result");
		}

		mav.addObject(RESULT_LIST, resultList);

		return mav;
	}


}
