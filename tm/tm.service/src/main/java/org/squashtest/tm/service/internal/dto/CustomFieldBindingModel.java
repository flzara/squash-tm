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



public class CustomFieldBindingModel {

	private long id;

	private long projectId;

	private BindableEntityModel boundEntity;

	private CustomFieldModel<?> customField;

	private RenderingLocationModel[] renderingLocations;

	private int position;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getProjectId() {
		return projectId;
	}

	public void setProjectId(long projectId) {
		this.projectId = projectId;
	}

	public BindableEntityModel getBoundEntity() {
		return boundEntity;
	}

	public void setBoundEntity(BindableEntityModel entityType) {
		this.boundEntity = entityType;
	}


	public CustomFieldModel<?> getCustomField() {
		return customField;
	}

	public void setCustomField(CustomFieldModel<?> customField) {
		this.customField = customField;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public RenderingLocationModel[] getRenderingLocations() {
		return renderingLocations;
	}

	public void setRenderingLocations(RenderingLocationModel[] renderingLocations) {
		this.renderingLocations = renderingLocations.clone();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		CustomFieldBindingModel that = (CustomFieldBindingModel) o;

		return id != 0 && id == that.id;
	}

	@Override
	public int hashCode() {
		return (int) (id ^ (id >>> 32));
	}
}
