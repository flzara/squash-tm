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
package org.squashtest.tm.security.acls;

/**
 * This class represents an aggregation of permissions (read, write and so on) which can be given to a user and which
 * have a scope of object identities.
 *
 * @author Gregory Fouquet
 *
 */
public class PermissionGroup {
	private final long id;
	private final String qualifiedName;
	private transient String simpleName;


	public PermissionGroup(long id, String qualifiedName) {
		super();
		this.id = id;
		this.qualifiedName = qualifiedName;
		if (qualifiedName != null) {
			calculateSimpleName();
		}
	}

	public String getQualifiedName() {
		return qualifiedName;
	}

	public long getId() {
		return id;
	}

	public String getSimpleName() {
		return simpleName;
	}

	private void calculateSimpleName() {
		this.simpleName = qualifiedName.substring(qualifiedName.lastIndexOf('.') + 1);
	}

}
