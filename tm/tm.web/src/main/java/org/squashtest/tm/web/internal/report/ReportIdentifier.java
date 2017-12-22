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
package org.squashtest.tm.web.internal.report;

/**
 * @author Gregory
 *
 */
public class ReportIdentifier {
	private final String namespace;

	/**
	 * @param namespace
	 * @param index
	 */
	/* package */ReportIdentifier(String namespace) {
		super();
		this.namespace = namespace;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 7;
		result = prime * result + (namespace == null ? 0 : namespace.hashCode());
		return result;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {

		if (this == obj){return true;}
		if (obj == null){return false;}
		if (getClass() != obj.getClass()){return false;}
		ReportIdentifier other = (ReportIdentifier) obj;
		if (namespace == null) {
			if (other.namespace != null){return false;}
		} else if (!namespace.equals(other.namespace)){	return false;}
		return true;
	}

	/**
	 * @return the namespace
	 */
	public String getNamespace() {
		return namespace;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ReportIdentifier [namespace=" + namespace + "]";
	}
}
