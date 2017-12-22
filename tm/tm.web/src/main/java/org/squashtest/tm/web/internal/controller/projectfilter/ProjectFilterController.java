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
package org.squashtest.tm.web.internal.controller.projectfilter;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.projectfilter.ProjectFilter;
import org.squashtest.tm.service.project.ProjectFilterModificationService;
import org.squashtest.tm.service.internal.dto.FilterModel;

/*
 *
 *  To the future developpers that will work on that controller, check comment on updateProjectFilter first.
 *
 *
 *
 *
 *
 *
 *
 */

@Controller
@RequestMapping("/global-filter")
public class ProjectFilterController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProjectFilterController.class);

	@Inject
	private ProjectFilterModificationService projectFilterService;

	@ResponseBody
	@RequestMapping(value = "/filter", method = RequestMethod.GET)
	public
	FilterModel getProjects() {

		ProjectFilter filter = projectFilterService.findProjectFilterByUserLogin();
		List<Project> allProjects = projectFilterService.getAllProjects();

		return new FilterModel(filter, allProjects);
	}

	/*
	 * That method requires a workaround. The client cannot send a empty list so the param projectIds[] might never
	 * exist. Hence the request will never hit that method.
	 *
	 * Solution : make the parameter optional, and when projectIds[] do not exist consider it as an empty list.
	 *
	 * Note 1 : if in the future you need to map another method to the same RequestMapping, you'll probably have to
	 * rework the strategy to handle incoming empty (non existant) lists.
	 *
	 * Note 2 : why would an user set a filter 100% restrictive anyway.
	 */
	@ResponseBody
	@RequestMapping(value = "/filter", method = RequestMethod.POST)
	public
	void updateProjectFilter(@RequestBody ProjectFilterModel projectFilterModel) {
		List<Long> ids;
		if (projectFilterModel == null) {
			ids = new LinkedList<>(); // create an empty list instead
		} else {
			ids = projectFilterModel.getProjectIds();
		}

		LOGGER.trace("UserPreferenceController : {} projects selected", ids.size());
		projectFilterService.saveOrUpdateProjectFilter(ids, true);

	}

	public static class ProjectFilterModel {
		private List<Long> projectIds;

		public void setProjectIds(List<Long> projectIds) {
			this.projectIds = projectIds;
		}

		public List<Long> getProjectIds() {
			return projectIds;
		}
	}

	@ResponseBody
	@RequestMapping(value = "/filter-status", params = "isEnabled", method = RequestMethod.POST)
	public
	void setProjectFilterStatus(@RequestParam("isEnabled") boolean isEnabled) {
		LOGGER.trace("UserPreferenceController : filter enabled to " + isEnabled);
		projectFilterService.updateProjectFilterStatus(isEnabled);
	}

	@ResponseBody
	@RequestMapping(value = "/filter-status", method = RequestMethod.GET)
	public
	FilterModel getProjectFilterStatus() {
		ProjectFilter filter = projectFilterService.findProjectFilterByUserLogin();
		FilterModel model = new FilterModel();
		model.setEnabled(filter.getActivated());
		return model;
	}

}
