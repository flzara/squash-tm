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
package org.squashtest.tm.web.internal.controller.milestone;

import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Provider;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.service.project.GenericProjectFinder;
import org.squashtest.tm.web.internal.helper.ProjectHelper;

@Controller
@RequestMapping("administration/projects/{projectId}/milestone-binding")
public class MilestoneBindingManagerController {

	@Inject
	private GenericProjectFinder service;
	
	@Inject
	private Provider<MilestoneStatusComboDataBuilder> statusComboDataBuilderProvider;
	
	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView getManager(@PathVariable("projectId") Long projectId, Locale locale){
		
		ModelAndView mav = new ModelAndView("project-tabs/milestone-binding.html");
        GenericProject project = service.findById(projectId);
		mav.addObject("proj", project);
		mav.addObject("isTemplate", ProjectHelper.isTemplate(project));
		mav.addObject("milestoneStatus", statusComboDataBuilderProvider.get().useLocale(locale).buildMap());
		return mav;
	}
	
}
