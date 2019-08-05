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
package org.squashtest.tm.service.internal.query;

import org.squashtest.tm.core.foundation.lang.Couple;

class PlannedJoin extends Couple<InternalEntityType, InternalEntityType>{

	/**
	 * Describe how the two entities should be joined.
	 * <ul>
	 * 	<li>MAPPED : navigates over the attribute (because the relation is mapped), nothing special about it</li>
	 * 	<li>UNMAPPED : should be used when navigation isn't possible (because the relation is not mapped). In this case the join uses the .join(other_entity).on(srcId, destId) form.</li>
	 * </ul>
	 * @author bsiri
	 *
	 * TODO : see comment on QueryPlanner#addWhereJoin about how we should evolve the WHERE join style
	 *
	 */
	enum JoinType {
		MAPPED,
		UNMAPPED
	}

	/**
	 * Name of the attribute of the source entity when the join type is NATURAL,
	 * or name of the foreign key of the dest entity when the join type is UNMAPPED
	 */
	private String attribute;

	// Note : this attribute is not part of the computation of Equals
	private JoinType type = JoinType.MAPPED;


	public PlannedJoin(InternalEntityType a1, InternalEntityType a2, String attribute) {
		super(a1, a2);
		this.attribute = attribute;
	}

	public PlannedJoin(InternalEntityType a1, InternalEntityType a2, String attribute, JoinType type) {
		super(a1, a2);
		this.attribute = attribute;
		this.type = type;
	}

	InternalEntityType getSrc(){
		return getA1();
	}

	InternalEntityType getDest(){
		return getA2();
	}

	String getAttribute(){
		return attribute;
	}

	JoinType getType(){
		return type;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (attribute == null ? 0 : attribute.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		PlannedJoin other = (PlannedJoin) obj;
		if (attribute == null) {
			if (other.attribute != null) {
				return false;
			}
		} else if (!attribute.equals(other.attribute)) {
			return false;
		}
		return true;
	}





}
