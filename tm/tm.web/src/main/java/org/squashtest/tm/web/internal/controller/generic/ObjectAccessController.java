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
package org.squashtest.tm.web.internal.controller.generic;

import java.security.Principal;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.web.internal.interceptor.openedentity.OpenedEntities;

@Controller
public class ObjectAccessController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ObjectAccessController.class);

	@RequestMapping(value = "/test-cases/{id}/opened-entity", method = RequestMethod.DELETE)
	@ResponseBody
	public void leaveTestCase(@PathVariable("id") Long id, HttpServletRequest request) {
		String contextKey = TestCase.class.getSimpleName();
		removeViewForObject(id, request, contextKey);
	}

	@RequestMapping(value = "/requirements/{id}/opened-entity", method = RequestMethod.DELETE)
	@ResponseBody
	public void leaveRequirement(@PathVariable("id") Long id, HttpServletRequest request) {
		String contextKey = Requirement.class.getSimpleName();
		removeViewForObject(id, request, contextKey);
	}

	@RequestMapping(value = "/campaigns/{id}/opened-entity", method = RequestMethod.DELETE)
	@ResponseBody
	public void leaveCampaign(@PathVariable("id") Long id, HttpServletRequest request) {
		String contextKey = Campaign.class.getSimpleName();
		removeViewForObject(id, request, contextKey);
	}
	@RequestMapping(value = "/iterations/{id}/opened-entity", method = RequestMethod.DELETE)
	@ResponseBody
	public void leaveIteration(@PathVariable("id") Long id, HttpServletRequest request) {
		String contextKey = Iteration.class.getSimpleName();
		removeViewForObject(id, request, contextKey);
	}
	@RequestMapping(value = "/test-suites/{id}/opened-entity", method = RequestMethod.DELETE)
	@ResponseBody
	public void leaveTestSuite(@PathVariable("id") Long id, HttpServletRequest request) {
		String contextKey = TestSuite.class.getSimpleName();
		removeViewForObject(id, request, contextKey);
	}
	@RequestMapping(value = "/executions/{id}/opened-entity", method = RequestMethod.DELETE)
	@ResponseBody
	public void leaveExecution(@PathVariable("id") Long id, HttpServletRequest request) {
		String contextKey = Execution.class.getSimpleName();
		removeViewForObject(id, request, contextKey);
	}

	private void removeViewForObject(Long id, HttpServletRequest request, String contextKey) {
		Principal user = request.getUserPrincipal();
		HttpSession session = request.getSession();
		if (session != null) {
			ServletContext context = request.getSession().getServletContext();
			if (context != null && user != null) {
				LOGGER.debug("context = "+context);
				LOGGER.debug("leave "+contextKey+" #" + id);
								LOGGER.debug(user.getName());
				OpenedEntities openedEnities = (OpenedEntities) context.getAttribute(contextKey);
				if(openedEnities != null){
					openedEnities.removeView(user.getName(), id);
				}
			}
		}
	}


}
