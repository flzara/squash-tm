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
package org.squashtest.tm.web.internal.controller.infolist;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.tm.domain.infolist.InfoList;
import org.squashtest.tm.domain.infolist.SystemInfoListCode;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.service.infolist.InfoListFinderService;
import org.squashtest.tm.service.project.GenericProjectFinder;
import org.squashtest.tm.web.internal.helper.JsonHelper;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;

@Controller
@RequestMapping("administration/projects/{projectId}/infoList-binding")
public class InfoListBindingManagerController {

	@Inject
	private GenericProjectFinder projectService;
	
	@Inject
	private InfoListFinderService infoListService;
	
	@Inject
	private InternationalizationHelper i18n;

	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView getManager(@PathVariable("projectId") Long projectId, Locale locale) {

		ModelAndView mav = new ModelAndView("project-tabs/info-list-binding.html");
		GenericProject project = projectService.findById(projectId);
		List<InfoList> infoLists = infoListService.findAllUserLists();
		mav.addObject("proj", project);
		mav.addObject("category", buildCategoryData(infoLists, locale));
		mav.addObject("nature",buildNatureData(infoLists, locale));
		mav.addObject("type",buildTypeData(infoLists, locale));
		return mav;
	}

	private String buildTypeData(List<InfoList> infoLists, Locale locale) {
		InfoList defaultList = infoListService.findByCode(SystemInfoListCode.TEST_CASE_TYPE.getCode());
		return buildComboData(infoLists, locale, defaultList);
	}

	private String buildNatureData(List<InfoList> infoLists, Locale locale) {
		InfoList defaultList = infoListService.findByCode(SystemInfoListCode.TEST_CASE_NATURE.getCode());
		return buildComboData(infoLists, locale, defaultList);
	}

	private String buildCategoryData(List<InfoList> infoLists, Locale locale) {
		InfoList defaultList = infoListService.findByCode(SystemInfoListCode.REQUIREMENT_CATEGORY.getCode());
		return buildComboData(infoLists, locale, defaultList);
	}
	
	private String buildComboData(List<InfoList> infoLists, Locale locale, InfoList defaultList){
		Map<String, String> result = new LinkedHashMap<>();
		//Add _ before the id so jeditable doesn't reorder our list
		result.put("_" + defaultList.getId().toString(), i18n.internationalize(defaultList.getLabel(), locale));
		for (InfoList list : infoLists){
			//Add _ before the id so jeditable doesn't reorder our list
			result.put("_" +list.getId().toString(), list.getLabel());
		}
		return 	JsonHelper.marshall(result);	
	}

}
