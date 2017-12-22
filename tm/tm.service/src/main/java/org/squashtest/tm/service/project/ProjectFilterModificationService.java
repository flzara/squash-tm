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

import java.util.List;

import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.projectfilter.ProjectFilter;

public interface ProjectFilterModificationService {
	
	/***
	 * This method returns the default project filter for the current user
	 * 
	 * @return a projectFilter
	 */
	ProjectFilter findProjectFilterByUserLogin();
	
	
	/***
	 * This method update the projects filter project white list and status (activated or not)
	 * @param projectIdList the list of project in the "white list" (to be display)
	 * @param isActive the status (activated or not)
	 */
	void saveOrUpdateProjectFilter(List<Long> projectIdList, boolean isActive);
	
	
	/***
	 * Method which update the project filter status
	 * 
	 * @param status the project filter status
	 */
	void updateProjectFilterStatus(boolean status);
	
	
	/***
	 * Get the list of all existing projects
	 * @return the list of all projects (List<Projects>)
	 */
	List<Project> getAllProjects();
	
}
