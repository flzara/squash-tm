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
package org.squashtest.tm.service.project;

import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.project.ProjectTemplate;
import org.squashtest.tm.exception.NameAlreadyInUseException;


/**
 * Project modification services which cannot be dynamically generated.
 * 
 * @author mpagnon
 * 
 */
public interface CustomProjectModificationService extends CustomProjectFinder {
	/**
	 * Will persist the new {@linkplain Project} and add settings copied from a given {@linkplain ProjectTemplate}.
	 * 
	 * @param newProject : the new {@link Project} entity to persist
	 * @param templateId : the id of the {@link ProjectTemplate} to copy the settings from
	 * @param params : conf object containing the following params : 
	 *  copyAssignedUsers : whether to copy the Template's assigned Users or not
	 *  copyCustomFieldsSettings : whether to copy the Template's CustomFields settings or not
	 *  copyBugtrackerSettings : whether to copy the Template's bug-tracker settings or not
	 *  copyTestAutomationSettings : whether to copy the Template's automation settings or not
	 *  copyInfolists : whether to use the Template's custom categories/natures/types
	 *  copyMilestone 
	 * @return the persisted new {@link Project}
	 */
	Project addProjectFromtemplate(Project newProject, long templateId, GenericProjectCopyParameter params) throws NameAlreadyInUseException;

	void deleteProject(long projectId);

}
