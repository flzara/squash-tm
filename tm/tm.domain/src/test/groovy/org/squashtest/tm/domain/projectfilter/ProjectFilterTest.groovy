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
package org.squashtest.tm.domain.projectfilter

import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.domain.projectfilter.ProjectFilter

import spock.lang.Specification

class ProjectFilterTest extends Specification {
	ProjectFilter projectFilter = new ProjectFilter();
	List<Project> projectList = new ArrayList<Project>();

	
	def "should add a project"(){
		given: Project project = new Project()
		projectList.add(project)
		
		when: projectFilter.setProjects(projectList)
		
		then: projectFilter.projects.contains project 
	}
	
	def "should remove a project"(){
		given:
		Project project = new Project()
		//add project to the list
		projectList.add(project)
		//set the projectFilter's project list
		projectFilter.setProjects(projectList)
		
		when: 
		projectFilter.setProjects(new ArrayList<Project>())
		
		then: 
		!projectFilter.projects.contains(project)
	}

}
