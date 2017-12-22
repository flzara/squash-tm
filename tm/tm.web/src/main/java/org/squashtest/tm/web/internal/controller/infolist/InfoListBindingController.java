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

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.service.infolist.InfoListBindingManagerService;
import org.squashtest.tm.web.internal.helper.JEditablePostParams;

@Controller
@RequestMapping("/info-list-binding")
public class InfoListBindingController {

	@Inject
	private InfoListBindingManagerService service;
	
	@RequestMapping(value="/project/{projectId}/category", method = RequestMethod.POST, params = JEditablePostParams.VALUE)
	@ResponseBody
	public void bindCategoryToProject(@PathVariable Long projectId, @RequestParam(JEditablePostParams.VALUE) Long infoListId) {
		service.bindListToProjectReqCategory(infoListId, projectId);
	}
	
	@RequestMapping(value="/project/{projectId}/nature", method = RequestMethod.POST, params = JEditablePostParams.VALUE)
	@ResponseBody
	public void bindNatureToProject(@PathVariable Long projectId, @RequestParam(JEditablePostParams.VALUE) Long infoListId) {
		service.bindListToProjectTcNature(infoListId, projectId);
	}
	
	@RequestMapping(value="/project/{projectId}/type", method = RequestMethod.POST, params = JEditablePostParams.VALUE)
	@ResponseBody
	public void bindTypeToProject(@PathVariable Long projectId, @RequestParam(JEditablePostParams.VALUE) Long infoListId) {
		service.bindListToProjectTcType(infoListId, projectId);
	}
}
