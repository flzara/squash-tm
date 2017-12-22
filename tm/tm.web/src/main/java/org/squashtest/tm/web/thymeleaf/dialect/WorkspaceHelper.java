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
package org.squashtest.tm.web.thymeleaf.dialect;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.tm.api.export.ExportPlugin;
import org.squashtest.tm.api.wizard.WorkspaceWizard;
import org.squashtest.tm.api.workspace.WorkspaceType;
import org.squashtest.tm.web.internal.plugins.manager.export.ExportPluginManager;
import org.squashtest.tm.web.internal.helper.JsonHelper;
import org.squashtest.tm.service.internal.dto.CustomFieldModel;
import org.squashtest.tm.service.internal.dto.FilterModel;
import org.squashtest.tm.web.internal.plugins.manager.wizard.WorkspaceWizardManager;

import javax.servlet.ServletContext;
import java.util.Collection;


/**
 * Workspace helper, as an expression extension for thymeleaf
 *
 * @author bsiri
 *
 */
public class WorkspaceHelper {

	private final ServletContext servletContext;

	public WorkspaceHelper(final ServletContext servletContext) {
		super();
		this.servletContext = servletContext;
	}

	public Collection<BugTracker> visibleBugtrackers() {
		return org.squashtest.tm.web.internal.helper.WorkspaceHelper.getVisibleBugtrackers(servletContext);
	}


	public FilterModel projectFilter() {
		return org.squashtest.tm.web.internal.helper.WorkspaceHelper.getProjectFilter(servletContext);
	}

	public Collection<ExportPlugin> exportPlugins(String workspaceName) {

		WebApplicationContext wac = WebApplicationContextUtils.getWebApplicationContext(servletContext);
		WorkspaceType workspace = WorkspaceType.valueOf(workspaceName);

		ExportPluginManager manager = wac.getBean(ExportPluginManager.class);
		return manager.findAllByWorkspace(workspace);
	}


	public Collection<WorkspaceWizard> wizardPlugins(String workspaceName) {
		WebApplicationContext wac = WebApplicationContextUtils.getWebApplicationContext(servletContext);
		WorkspaceWizardManager wizardManager = wac.getBean(WorkspaceWizardManager.class);

		WorkspaceType type = WorkspaceType.valueOf(workspaceName);
		return wizardManager.findAllByWorkspace(type);
	}


	public String jacksonSerializer(Object toSerialize) {
		return JsonHelper.serialize(toSerialize);
	}

	public String cufdefSerializer(Collection<CustomFieldModel> toSerialize) throws JsonProcessingException {
		return JsonHelper.serializeCustomfields(toSerialize);
	}


}
