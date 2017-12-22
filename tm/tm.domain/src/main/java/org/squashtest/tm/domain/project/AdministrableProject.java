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
package org.squashtest.tm.domain.project;

import org.squashtest.tm.domain.campaign.CampaignLibrary;
import org.squashtest.tm.domain.requirement.RequirementLibrary;
import org.squashtest.tm.domain.testcase.TestCaseLibrary;
/**
 * Dto to decorate a generic project with functional informations such as "deletable" or is "template".
 * @author mpagnon
 *
 */
public class AdministrableProject {
	private final GenericProject project;
	private boolean deletable = false;
	private boolean template = false;

	public boolean isDeletable() {
		return deletable;
	}

	public void setDeletable(boolean deletable) {
		this.deletable = deletable;
	}

	public String getLabel() {
		return project.getLabel();
	}

	public Long getId() {
		return project.getId();
	}

	public String getDescription() {
		return project.getDescription();
	}

	public String getName() {
		return project.getName();
	}

	public boolean isActive() {
		return project.isActive();
	}

	public TestCaseLibrary getTestCaseLibrary() {
		return project.getTestCaseLibrary();
	}

	public RequirementLibrary getRequirementLibrary() {
		return project.getRequirementLibrary();
	}

	public CampaignLibrary getCampaignLibrary() {
		return project.getCampaignLibrary();
	}

	public GenericProject getProject() {
		return project;
	}

	public AdministrableProject(GenericProject project) {
		this.project = project;
	}

	public boolean isTemplate() {
		return template;
	}

	public void setTemplate(boolean template) {
		this.template = template;
	}

	public boolean allowTcModifDuringExec() {
		return project.allowTcModifDuringExec();
	}
}
