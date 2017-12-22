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
package org.squashtest.tm.web.internal.interceptor;

import javax.inject.Inject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.squashtest.tm.service.milestone.ActiveMilestoneHolder;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.WebUtils;
import org.squashtest.tm.annotation.WebComponent;

@WebComponent
public class ActiveMilestoneInterceptor implements HandlerInterceptor {

	private static final String MILESTONE = "milestones";

	@Inject
	private ActiveMilestoneHolder milestoneHolder;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

		Cookie cookie = WebUtils.getCookie(request, MILESTONE);
		
		Long milestoneId = null;
		if (cookie != null && !StringUtils.isBlank(cookie.getValue())){
			milestoneId = Long.parseLong(cookie.getValue());
		}
		else{
			// it's under 9000 ! just a fake id in case we don't find cookie.
			milestoneId = ActiveMilestoneHolder.NO_MILESTONE_ID;
		}
		
		milestoneHolder.setActiveMilestone(milestoneId);
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		milestoneHolder.clearContext();

	}

}
