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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.squashtest.tm.exception.NameAlreadyInUseException;
import org.squashtest.tm.service.project.GenericProjectManagerService;
import org.squashtest.tm.service.project.ProjectManagerService;
import org.squashtest.tm.web.internal.model.json.JsonProjectFromTemplate;

import javax.inject.Inject;
import javax.validation.Valid;
import java.text.MessageFormat;
import java.util.Map;

// XSS OK
@Controller
@RequestMapping("/projects")
public class ProjectController {
	private static final Logger LOGGER = LoggerFactory.getLogger(ProjectController.class);

	@Inject
	private ProjectManagerService projectManager;

	@Inject
	private GenericProjectManagerService genericProjectManager;

	@ResponseBody
	@ResponseStatus(HttpStatus.CREATED)
	@RequestMapping(value = "/{projectId}", method = RequestMethod.PUT)
	public void coerceProjectIntoTemplate(@RequestBody Map<String, Object> payload, @PathVariable long projectId) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("PUTting project/{} with payload {}", projectId, payload);
		}
		long payloadProjectId = (int) payload.get("templateId");
		if (payloadProjectId != projectId) {
			throw new IllegalArgumentException(MessageFormat.format(
				"Cannot coerce Project into ProjectTemplate : project id {0} is not the same as template id {1}",
				projectId, payloadProjectId));
		}
		genericProjectManager.coerceProjectIntoTemplate(projectId);
	}

	@ResponseBody
	@ResponseStatus(value = HttpStatus.CREATED)
	@RequestMapping(value = "/new", method = RequestMethod.POST)
	public void createProjectFromTemplate(@Valid @RequestBody JsonProjectFromTemplate jsonProjectFromTemplate) {
		try {
			if (jsonProjectFromTemplate.isFromTemplate()) {
				projectManager.addProjectFromTemplate(jsonProjectFromTemplate.getProject(),
					jsonProjectFromTemplate.getTemplateId(), jsonProjectFromTemplate.getParams());
			} else {
				genericProjectManager.persist(jsonProjectFromTemplate.getProject());
			}
		} catch (NameAlreadyInUseException ex) {
			ex.setObjectName("add-project-from-template");
			throw ex;
		}
	}
}
