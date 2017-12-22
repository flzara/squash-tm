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

/**
 * A class that justs conveniently ship together two named references. This is really a pair and the semantic depends on
 * what you do with it.
 *
 * @author bsiri
 *
 */
public final class NamedReferencePair {

	private NamedReference caller;
	private NamedReference called;

	public NamedReferencePair() {

	}

	public NamedReferencePair(Long callerId, String callerName, Long calledId, String calledName) {

		if (callerId != null) {
			caller = new NamedReference(callerId, callerName);
		}
		if (calledId != null) {
			called = new NamedReference(calledId, calledName);
		}
	}

	public NamedReference getCaller() {
		return caller;
	}

	public NamedReference getCalled() {
		return called;
	}

	@Override
	public int hashCode() { // NOSONAR generated
		final int prime = 31;
		int result = 1;
		result = prime * result + (called == null ? 0 : called.hashCode());
		result = prime * result + (caller == null ? 0 : caller.hashCode());
		return result;
	}

	// GENERATED:START
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
		NamedReferencePair other = (NamedReferencePair) obj;
		if (called == null) {
			if (other.called != null) {
				return false;
			}
		} else if (!called.equals(other.called)) {
			return false;
		}
		if (caller == null) {
			if (other.caller != null) {
				return false;
			}
		} else if (!caller.equals(other.caller)) {
			return false;
		}
		return true;
	}
	// GENERATED:END

}
