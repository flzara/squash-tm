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
package org.squashtest.tm.domain.customfield;

import org.squashtest.tm.domain.project.Project;

public class BoundEntityImpl implements BoundEntity {

	private Long boundEntityId;

	private BindableEntity bindableEntity;

	private Project project;

	public BoundEntityImpl(Long boundEntityId, BindableEntity bindableEntity) {
		this.boundEntityId = boundEntityId;
		this.bindableEntity = bindableEntity;
	}

	public BoundEntityImpl(Long boundEntityId, BindableEntity bindableEntity, Project project) {
		this.boundEntityId = boundEntityId;
		this.bindableEntity = bindableEntity;
		this.project = project;
	}

	@Override
	public Long getBoundEntityId() {
		return boundEntityId;
	}

	@Override
	public BindableEntity getBoundEntityType() {
		return bindableEntity;
	}

	@Override
	public Project getProject() {
		return project;
	}

	@Override
	public Long getId() {
		return boundEntityId;
	}

	public void setBoundEntityId(Long boundEntityId) {
		this.boundEntityId = boundEntityId;
	}

	public void setBindableEntity(BindableEntity bindableEntity) {
		this.bindableEntity = bindableEntity;
	}

	public void setProject(Project project) {
		this.project = project;
	}
}
