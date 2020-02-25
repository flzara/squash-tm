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

import org.squashtest.tm.domain.customfield.BindableEntity;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import static org.squashtest.tm.domain.project.Project.PROJECT_TYPE;

@Entity
@DiscriminatorValue(PROJECT_TYPE)
@NamedQueries({
	@NamedQuery(name = "Project.fetchForAutomatedExecutionCreation", query = "select distinct p " +
		"from Project p " +
		"left join fetch p.bugtrackerBinding " +
		"left join fetch p.testAutomationProjects " +
		"left join fetch p.testAutomationServer " +
		"left join fetch p.testCaseNatures " +
		"where p.id=:projectId")
})
public class Project extends GenericProject {

	public static final String PROJECT_TYPE = "P";

	public Project() {
		super();
	}

	/**
	 * @see org.squashtest.tm.domain.project.GenericProject#accept(org.squashtest.tm.domain.project.ProjectVisitor)
	 */
	@Override
	public void accept(ProjectVisitor visitor) {
		visitor.visit(this);

	}

	@Override
	public Long getBoundEntityId() {
		return getId();
	}

	@Override
	public BindableEntity getBoundEntityType() {
		return BindableEntity.PROJECT;
	}

	@Override
	public Project getProject() {
		return this;
	}
}
