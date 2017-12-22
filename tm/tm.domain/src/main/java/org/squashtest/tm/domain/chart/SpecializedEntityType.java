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
package org.squashtest.tm.domain.chart;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.squashtest.tm.domain.EntityType;

@Embeddable
public class SpecializedEntityType {
	/**
	 * Used in {@link ColumnPrototype} : when the EntityType referenced by this ColumnPrototype may support different roles depending
	 * on the context, an EntityRole will help to know which one.
	 *
	 * @author bsiri
	 *
	 */
	public enum EntityRole {
		TEST_CASE_NATURE,
		TEST_CASE_TYPE,
		REQUIREMENT_VERSION_CATEGORY,
		ITERATION_TEST_PLAN_ASSIGNED_USER,
		TEST_CASE_MILESTONE,
		REQUIREMENT_VERSION_MILESTONE,
		CUSTOM_FIELD,
		CAMPAIGN_MILESTONE
	}

	@Enumerated(EnumType.STRING)
	private EntityType entityType;

	@Enumerated(EnumType.STRING)
	private EntityRole entityRole;


	public EntityType getEntityType() {
		return entityType;
	}

	public EntityRole getEntityRole() {
		return entityRole;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (entityRole == null ? 0 : entityRole.hashCode());
		result = prime * result + (entityType == null ? 0 : entityType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		SpecializedEntityType other = (SpecializedEntityType) obj;
		if (entityRole != other.entityRole) {
			return false;
		}
		if (entityType != other.entityType) {
			return false;
		}
		return true;
	}

	protected SpecializedEntityType() {
	}

	public SpecializedEntityType(EntityType entityType, EntityRole entityRole) {
		super();
		this.entityType = entityType;
		this.entityRole = entityRole;
	}




}
