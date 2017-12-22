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
package org.squashtest.tm.service.internal.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.projectfilter.ProjectFilter;

/*
 * Note : we set the NOSONAR flag on the setters for Array-type properties otherwise it rings because we don't clone them.
 * We can reasonably ignore those warnings because that class is meant to be serialized from/to json. Of course, that assumption holds
 * as long as no one uses that class for another purpose.
 *
 *
 * @author bsiri
 */

// made "final" because SONAR didn't like the overridable methods in the constructor
public final class FilterModel {
	private List<Object[]> projectData = new ArrayList<>();
	private boolean enabled;
	//used only for equals method...
	private Long id;

	public FilterModel(){
		super();
	}

	public FilterModel(ProjectFilter filter, List<Project> projects){

		setEnabled(filter.getActivated());

		Object[][] pData = new Object[projects.size()][4];
		int i = 0;

		for (Project project : projects){
			pData[i] = new Object[]{
					project.getId(),
					project.getName(),
					filter.isProjectSelected(project),
					project.getLabel()
			};

			i++;

		}

		//remember that projectData.toArray() actually returns an Object[][]
		setProjectData(pData);
	}

	public FilterModel(List<Project> projects) {
		for (Project project : projects) {
			addProject(project.getId(), project.getName(), project.getLabel());
		}
	}

	public Object[] getProjectData() {
		return projectData.toArray();
	}

	private void setProjectData(Object[][] projectData) {
		this.projectData = new ArrayList<>(projectData.length);
		Collections.addAll(this.projectData, projectData);
	}
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean activated) {
		this.enabled = activated;
	}

	public void addProject(long id, String name, String label) {
		projectData.add(new Object[] {id, name, false, label});
	}

	public void addProject(long id, String name, boolean selected, String label) {
		projectData.add(new Object[] {id, name, selected, label});
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		FilterModel that = (FilterModel) o;

		return id != null && id.equals(that.id);
	}

	@Override
	public int hashCode() {
		return id != null ? id.hashCode() : 0;
	}
}
