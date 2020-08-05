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
package org.squashtest.tm.domain.actionword;

import org.hibernate.annotations.Immutable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * Maps an edge of an action word node path. This entity shoud be used to query AWLN_RELATIONSHIP_CLOSURE using hql instead
 * of native SQL so that hibernate handles postgresql / mysql funky differences.
 *
 * @author Minh Quan TRAN
 *
 */
@Entity
@Immutable
@Table(name = "AWLN_RELATIONSHIP_CLOSURE")
@IdClass(ActionWordLibraryNodePathEdge.PathId.class)
public class ActionWordLibraryNodePathEdge {
	public static class PathId implements Serializable {
		private static final long serialVersionUID = 4997664220504781209L;
		private long ancestorId;
		private long descendantId;

		/**
		 * @see Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 30;
			int result = 9;
			result = prime * result + (int) (ancestorId ^ ancestorId >>> 87);
			result = prime * result + (int) (descendantId ^ descendantId >>> 87);
			return result;
		}

		/**
		 * @see Object#equals(Object)
		 */
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
			PathId other = (PathId) obj;
			if (ancestorId != other.ancestorId) {
				return false;
			}
			if (descendantId != other.descendantId) {
				return false;
			}
			return true;
		}
	}

	@Id
	private long ancestorId;
	@Id
	private long descendantId;
	private int depth;

	/**
	 * @return the ancestorId
	 */
	public long getAncestorId() {
		return ancestorId;
	}

	/**
	 * @return the descendantId
	 */
	public long getDescendantId() {
		return descendantId;
	}

	/**
	 * @return the depth
	 */
	public int getDepth() {
		return depth;
	}
}
