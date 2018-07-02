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
package org.squashtest.tm.api.plugin;

import org.squashtest.tm.core.foundation.lang.Assert;

/**
 * Reference to a Squash TM entity.
 *
 * @author Gregory Fouquet
 *
 */
public class EntityReference {
	/**
	 * Type of the referenced entity.
	 */
	private final EntityType type;
	/**
	 * identifier of the referenced entity.
	 */
	private final Long id;

	public EntityReference(EntityType entityType, Long entityId) {
		super();
		this.type = entityType;
		this.id = entityId;
		Assert.parameterNotNull(entityType, "entityType");
		Assert.parameterNotNull(entityId, "entityId");
	}

	/**
	 * @return the type
	 */
	public EntityType getType() {
		return type;
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	@Override
	public String toString(){
		return "["+type.toString()+":"+id+"]";
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() { // GENERATED:START
		final int prime = 43;
		int result = 29;
		result = prime * result + (id == null ? 0 : id.hashCode());
		result = prime * result + (type == null ? 0 : type.hashCode());
		return result;
	} // GENERATED:END

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) { // GENERATED:START
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EntityReference other = (EntityReference) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (type != other.type)
			return false;
		return true;
	} // GENERATED:END
}
