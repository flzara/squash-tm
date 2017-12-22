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
package org.squashtest.tm.web.internal.controller.customreport.folder;

import static org.squashtest.tm.web.internal.helper.JEditablePostParams.VALUE;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.service.customreport.CustomReportFolderService;

@Controller
public class CustomReportFolderController {

	@Inject
	CustomReportFolderService crfService;

	//--- CHANGE DESCRIPTION ---

		@ResponseBody
		@RequestMapping(method = RequestMethod.POST, value = "custom-report-workspace/description/{folderId}", params = {"id=folder-description", VALUE})
		public
		String updateDescription(@PathVariable long folderId, @RequestParam(VALUE) String newDescription) {
			crfService.updateDescription(folderId, newDescription);
			return newDescription;
		}
}
