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
package org.squashtest.tm.web.internal.model.json;

import org.hibernate.validator.constraints.NotBlank;
import org.squashtest.tm.domain.project.ProjectTemplate;
import org.squashtest.tm.service.project.GenericProjectCopyParameter;

/**
 * @author Julien Thébault This class is used to deserialize the request body of "/project-templates/new"
 */

public class JsonTemplateFromProject {

	private ProjectTemplate projectTemplate;

	private long templateId;

	private GenericProjectCopyParameter params;

	public JsonTemplateFromProject() {
		projectTemplate = new ProjectTemplate();
		params = new GenericProjectCopyParameter();
	}

	//Wrapping projectTemplate getter and setter to deserialize correctly the request json
	public String getLabel() {
		return projectTemplate.getLabel();
	}

	public void setLabel(String label) {
		projectTemplate.setLabel(label);
	}

	public String getDescription() {
		return projectTemplate.getDescription();
	}

	public void setDescription(String description) {
		projectTemplate.setDescription(description);
	}

	@NotBlank
	public String getName() {
		return projectTemplate.getName();
	}

	public void setName(String name) {
		projectTemplate.setName(name.trim());
	}

	public ProjectTemplate getProjectTemplate() {
		return projectTemplate;
	}

	public void setProjectTemplate(ProjectTemplate projectTemplate) {
		this.projectTemplate = projectTemplate;
	}

	public long getTemplateId() {
		return templateId;
	}

	public void setTemplateId(long templateId) {
		this.templateId = templateId;
	}

	public boolean isCopyPermissions() {
		return params.isCopyPermissions();
	}
	public void setCopyPermissions(boolean copyPermissions) {
		params.setCopyPermissions(copyPermissions);
	}
	public boolean isCopyCUF() {
		return params.isCopyCUF();
	}
	public void setCopyCUF(boolean copyCUF) {
		params.setCopyCUF(copyCUF);
	}
	public boolean isCopyBugtrackerBinding() {
		return params.isCopyBugtrackerBinding();
	}
	public void setCopyBugtrackerBinding(boolean copyBugtrackerBinding) {
		params.setCopyBugtrackerBinding(copyBugtrackerBinding);
	}
	public boolean isCopyAutomatedProjects() {
		return params.isCopyAutomatedProjects();
	}
	public void setCopyAutomatedProjects(boolean copyAutomatedProjects) {
		params.setCopyAutomatedProjects(copyAutomatedProjects);
	}
	public boolean isCopyInfolists() {
		return params.isCopyInfolists();
	}
	public void setCopyInfolists(boolean copyInfolists) {
		params.setCopyInfolists(copyInfolists);
	}
	public boolean isCopyMilestone() {
		return params.isCopyMilestone();
	}
	public void setCopyMilestone(boolean copyMilestone) {
		params.setCopyMilestone(copyMilestone);
	}

	public boolean isCopyAllowTcModifFromExec() {
		return params.isCopyAllowTcModifFromExec();
	}

	public void setCopyAllowTcModifFromExec(boolean copyAllowTcModifFromExec) {
		params.setCopyAllowTcModifFromExec(copyAllowTcModifFromExec);
	}

	public GenericProjectCopyParameter getParams() {
		return params;
	}

	public void setParams(GenericProjectCopyParameter params) {
		this.params = params;
	}
}
