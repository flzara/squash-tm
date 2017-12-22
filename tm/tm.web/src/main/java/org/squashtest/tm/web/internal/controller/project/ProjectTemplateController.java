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
package org.squashtest.tm.web.internal.controller.project;

import java.util.List;

import javax.inject.Inject;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;
import org.squashtest.tm.domain.NamedReference;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.exception.NameAlreadyInUseException;
import org.squashtest.tm.service.project.ProjectTemplateFinder;
import org.squashtest.tm.service.project.ProjectTemplateManagerService;
import org.squashtest.tm.web.internal.model.json.JsonTemplateFromProject;
import org.squashtest.tm.web.internal.model.json.JsonUrl;

@Controller
@RequestMapping("/project-templates")
public class ProjectTemplateController {

	@Inject
	private ProjectTemplateFinder projectFinder;

	@Inject
	private ProjectTemplateManagerService projectTemplateManagerService;

	private static final Logger LOGGER = LoggerFactory.getLogger(GenericProjectController.class);

	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, params = "dropdownList")
	public List<NamedReference> getTemplateDropdownModel() {
		return projectFinder.findAllReferences();
	}

	@ResponseBody
	@ResponseStatus(value = HttpStatus.CREATED)
	@RequestMapping(value = "/new", method = RequestMethod.POST)
	public
	JsonUrl createTemplateFromProject(@Valid @RequestBody JsonTemplateFromProject jsonTemplateFromProject) {
		try {
			projectTemplateManagerService.addTemplateFromProject(jsonTemplateFromProject.getProjectTemplate(),
					jsonTemplateFromProject.getTemplateId(), jsonTemplateFromProject.getParams());
		} catch (NameAlreadyInUseException ex) {
			ex.setObjectName("add-template-from-project");
			throw ex;
		}
		return getUrlToProjectInfoPage(jsonTemplateFromProject.getProjectTemplate());
	}

	private JsonUrl getUrlToProjectInfoPage(GenericProject project){
		UriComponents uri = ServletUriComponentsBuilder.fromCurrentContextPath().path("/administration/projects/{id}/info")
				.buildAndExpand(project.getId());
		return new JsonUrl(uri.toString());
	}
}
