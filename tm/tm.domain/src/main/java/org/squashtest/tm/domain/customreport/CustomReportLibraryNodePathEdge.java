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
package org.squashtest.tm.domain.customreport;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import org.hibernate.annotations.Immutable;

/**
 * Maps an edge of a test case node path. This entity shoud be used to query TCLN_RELATIONSHIP_CLOSURE using hql instead
 * of native SQL so that hibernate handles postgresql / mysql funky differences.
 *
 * @author Gregory Fouquet
 *
 */
@Entity
@Immutable
@Table(name = "CRLN_RELATIONSHIP_CLOSURE")
@IdClass(CustomReportLibraryNodePathEdge.PathId.class)
public class CustomReportLibraryNodePathEdge {
	public static class PathId implements Serializable {
		private static final long serialVersionUID = 1462511274257146101L;
		private long ancestorId;
		private long descendantId;

		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 17;
			result = prime * result + (int) (ancestorId ^ ancestorId >>> 32);
			result = prime * result + (int) (descendantId ^ descendantId >>> 32);
			return result;
		}

		/**
		 * @see java.lang.Object#equals(java.lang.Object)
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
