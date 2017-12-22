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

import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.project.ProjectTemplate;

public interface ProjectsPermissionManagementService extends ProjectsPermissionFinder {

	void deleteUserProjectOldPermission(String userLogin, long projectId);

	void addNewPermissionToProject(long userId, long projectId, String permissionName);

	@Override
	void removeProjectPermission(long userId, long projectId);
	
	/**
	 * Will copy all user permissions of template and apply them to the project and it's libraries.
	 * 
	 * @param project
	 *            : the {@link Project} to copy the permissions to
	 * @param projectTemplate
	 *            : the {@link ProjectTemplate} to copy the permissions from
	 */
	void copyAssignedUsersFromTemplate(Project project, ProjectTemplate projectTemplate);

	/**
	 * Same as {@link #copyAssignedUsersFromTemplate(Project, ProjectTemplate)} using the template's id
	 * 
	 * @see #copyAssignedUsersFromTemplate(Project, ProjectTemplate)
	 * @param project
	 * @param templateId
	 */
	void copyAssignedUsersFromTemplate(Project project, long templateId);

	/**
	 * Removes all the permissions from the given template (not its libraries).
	 * 
	 * @param templateId
	 */
	void removeAllPermissionsFromProjectTemplate(long templateId);
	/**
	 * Removes all the permissions from the given object.
	 * 
	 * @param clazz : the object's class
	 * @param id : the object's id
	 */
	void removeAllPermissionsFromObject(Class<?> clazz, long id);

	void removeProjectPermissionForAllProjects(long partyId);

	/**
	 * Will copy all user permissions of source generic project and apply them to the target generic project and it's libraries.
	 * 
	 * @param target
	 *            : the {@link GenericProject} to copy the permissions to
	 * @param source
	 *            : the {@link GenericProject} to copy the permissions from
	 */
	void copyAssignedUsers(GenericProject target, GenericProject source);

}
