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
package org.squashtest.tm.core.foundation.lang;

/**
 * An immutable 2-tuple
 *
 * @author Gregory Fouquet
 *
 */
public class Couple<T1, T2> {
	private final T1 a1;
	private final T2 a2;

	public Couple(T1 a1, T2 a2) {
		super();
		this.a1 = a1;
		this.a2 = a2;
	}

	/**
	 * @return the a1
	 */
	public T1 getA1() {
		return a1;
	}

	/**
	 * @return the a2
	 */
	public T2 getA2() {
		return a2;
	}

	public Object[] toArray() {
		return new Object[] { a1, a2 };
	}

	// GENERATED:START
	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {

		final int prime = 31;
		int result = 7;
		result = prime * result + (a1 == null ? 0 : a1.hashCode());
		result = prime * result + (a2 == null ? 0 : a2.hashCode());
		return result;

	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@SuppressWarnings("rawtypes")
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
		Couple other = (Couple) obj;
		if (a1 == null) {
			if (other.a1 != null) {
				return false;
			}
		} else if (!a1.equals(other.a1)) {
			return false;
		}
		if (a2 == null) {
			if (other.a2 != null) {
				return false;
			}
		} else if (!a2.equals(other.a2)) {
			return false;
		}
		return true;
	}
	// GENERATED:END
}
