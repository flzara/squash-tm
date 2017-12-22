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
package org.squashtest.tm.web.internal.helper;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.tm.api.export.ExportPlugin;
import org.squashtest.tm.api.wizard.WorkspaceWizard;
import org.squashtest.tm.api.workspace.WorkspaceType;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.service.bugtracker.BugTrackerFinderService;
import org.squashtest.tm.service.execution.ExecutionModificationService;
import org.squashtest.tm.service.project.ProjectFinder;
import org.squashtest.tm.service.testautomation.TestAutomationProjectFinderService;
import org.squashtest.tm.service.workspace.WorkspaceHelperService;
import org.squashtest.tm.web.internal.plugins.manager.export.ExportPluginManager;
import org.squashtest.tm.service.internal.dto.FilterModel;
import org.squashtest.tm.web.internal.plugins.manager.wizard.WorkspaceWizardManager;

import javax.servlet.ServletContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.net.URL;
import java.util.Collection;
import java.util.List;

/**
 * Warning : strongly tied to Spring
 *
 * @author bsiri
 *
 */
public class WorkspaceHelper extends SimpleTagSupport {

	public static Collection<BugTracker> getVisibleBugtrackers(ServletContext context) {

		WebApplicationContext wac = WebApplicationContextUtils.getWebApplicationContext(context);

		ProjectFinder projectFinder = wac.getBean(ProjectFinder.class);
		BugTrackerFinderService bugtrackerService = wac.getBean(BugTrackerFinderService.class);

		List<Long> projectsIds =  projectFinder.findAllReadableIds();

		return bugtrackerService.findDistinctBugTrackersForProjects(projectsIds);
	}

	public static URL getAutomatedJobURL(ServletContext context, Long executionId) {
		WebApplicationContext wac = WebApplicationContextUtils.getWebApplicationContext(context);

		ExecutionModificationService exService = wac.getBean(ExecutionModificationService.class);
		TestAutomationProjectFinderService projFinder = wac.getBean(TestAutomationProjectFinderService.class);

		Execution exec = exService.findById(executionId);

		if (exec.isAutomated() && !exec.getAutomatedExecutionExtender().isProjectDisassociated()) {
			return projFinder.findProjectURL(exec.getAutomatedExecutionExtender().getAutomatedProject());
		} else {
			return null;
		}
	}

	public static Collection<ExportPlugin> getExportPlugins(ServletContext context, String workspaceName) {

		WebApplicationContext wac = WebApplicationContextUtils.getWebApplicationContext(context);
		WorkspaceType workspace = WorkspaceType.valueOf(workspaceName);

		ExportPluginManager manager = wac.getBean(ExportPluginManager.class);
		return manager.findAllByWorkspace(workspace);
	}


	public static Collection<WorkspaceWizard> getWizardPlugins(ServletContext context, String workspaceName) {
		WebApplicationContext wac = WebApplicationContextUtils.getWebApplicationContext(context);
		WorkspaceWizardManager wizardManager = wac.getBean(WorkspaceWizardManager.class);

		WorkspaceType type = WorkspaceType.valueOf(workspaceName);
		return wizardManager.findAllByWorkspace(type);
	}

	public static FilterModel getProjectFilter(ServletContext context) {
		WebApplicationContext wac = WebApplicationContextUtils.getWebApplicationContext(context);
		WorkspaceHelperService service = wac.getBean(WorkspaceHelperService.class);
		return service.findFilterModel();
	}

}
