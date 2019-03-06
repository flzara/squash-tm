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
package org.squashtest.tm.web.internal.controller.table;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.tm.domain.customreport.CustomReportCustomExport;
import org.squashtest.tm.domain.customreport.CustomReportLibraryNode;
import org.squashtest.tm.service.customreport.CustomReportLibraryNodeService;
import org.squashtest.tm.web.internal.http.ContentTypes;

import javax.inject.Inject;
import java.util.Locale;

@Controller
@RequestMapping("custom-exports")
public class CustomExportController {

	@Inject
	private CustomReportLibraryNodeService reportLibraryNodeService;

	@RequestMapping("/wizard/{parentId}")
	public ModelAndView getWizard(@PathVariable Long parentId, Locale locale) {
		ModelAndView mav = new ModelAndView("custom-exports/wizard/wizard.html");
		mav.addObject("parentId", parentId);
		return mav;
	}

	@ResponseBody
	@RequestMapping(value = "/new/{parentNodeId}", method = RequestMethod.POST, consumes = ContentTypes.APPLICATION_JSON)
	public String createNewCustomExport(@RequestBody CustomReportCustomExport customExport, @PathVariable("parentNodeId") long parentNodeId) {
		CustomReportLibraryNode newNode = reportLibraryNodeService.createNewNode(parentNodeId, customExport);
		return String.valueOf(newNode.getId());
	}
}
