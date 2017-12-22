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
package org.squashtest.tm.web.internal.controller.report;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.squashtest.tm.domain.customreport.CustomReportLibraryNode;
import org.squashtest.tm.domain.customreport.CustomReportNodeType;
import org.squashtest.tm.domain.report.ReportDefinition;
import org.squashtest.tm.service.customreport.CustomReportLibraryNodeService;
import org.squashtest.tm.web.internal.helper.JsonHelper;
import org.squashtest.tm.web.internal.report.ReportsRegistry;

@Controller
@RequestMapping("/report-workspace")
public class ReportWorkspaceController {
	@Inject
	private ReportsRegistry reportsRegistry;

	@Inject
	private CustomReportLibraryNodeService customReportLibraryNodeService;

	@RequestMapping(method = RequestMethod.GET)
	public String showReportWorkspace(Model model) {
		populateModelWithReportsRegistry(model);
		return "report-workspace.html";
	}

	@RequestMapping(value = "/{parentId}", method = RequestMethod.GET)
	public String showReportWorkspaceFromCustomReport(@PathVariable Long parentId, Model model) {

		CustomReportLibraryNode crln = customReportLibraryNodeService.findCustomReportLibraryNodeById(parentId);

		if (crln.getEntityType().getTypeName().equals(CustomReportNodeType.REPORT_NAME)) {
			ReportDefinition def = (ReportDefinition) crln.getEntity();
			model.addAttribute("pluginNamespace", def.getPluginNamespace());
		}

		populateModelWithReportsRegistry(model);
		model.addAttribute("parentId", parentId);
		return "report-workspace.html";
	}

	private void populateModelWithReportsRegistry(Model model){
		model.addAttribute("categories", reportsRegistry.getSortedCategories());
		model.addAttribute("reports", reportsRegistry.getSortedReportsByCategory());
	}

	@ModelAttribute("hilightedWorkspace")
	String getHighlightedWorkspace() {
		return "report";
	}
}
