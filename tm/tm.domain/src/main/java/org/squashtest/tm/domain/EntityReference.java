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
package org.squashtest.tm.domain;

import org.squashtest.tm.core.foundation.lang.Assert;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class EntityReference {
	/**
	 * Type of the referenced entity.
	 */
	@Enumerated(EnumType.STRING)
	private EntityType type;
	/**
	 * identifier of the referenced entity.
	 */
	private Long id;


	private EntityReference() {
		// for hibernate
	}

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
	public String toString() {
		return "[" + type.toString() + ":" + id + "]";
	}

	/**
	 * Gets the "workspace-prefs" cookie from a "...SearchResultTable.js" and converts it to an EntityReference
	 * group 1 is the name of the entity
	 * group 2 is its id
	 */

	public static EntityReference fromString(String asString) {
		Pattern p = Pattern.compile("(.+)-(\\d+)");
		Matcher m = p.matcher(asString);

		if (m.matches()) {
			String type = m.group(1);
			Long id = Long.valueOf(m.group(2));

			EntityType etype = EntityType.valueOf(type.toUpperCase());
			return new EntityReference(etype, id);
		} else {
			throw new RuntimeException();
		}
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
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		EntityReference other = (EntityReference) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (type != other.type) {
			return false;
		}
		return true;
	} // GENERATED:END
}
