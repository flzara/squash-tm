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
package org.squashtest.tm.service.internal.batchimport;

import org.apache.commons.lang3.StringUtils;
import org.squashtest.tm.service.importer.EntityType;
import org.squashtest.tm.service.importer.Target;
import org.squashtest.tm.service.importer.WithPath;

public class ParameterTarget implements Target, WithPath {

	private TestCaseTarget owner;
	private String name;

	public ParameterTarget() {
		super();
		owner = new TestCaseTarget();
	}

	@Override
	public EntityType getType() {
		return EntityType.PARAMETER;
	}


	public ParameterTarget(TestCaseTarget owner, String name) {
		this.owner = owner;
		this.name = name;
	}

	public TestCaseTarget getOwner() {
		return owner;
	}

	public void setOwner(TestCaseTarget owner) {
		this.owner = owner;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	// GENERATED:START
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (name == null ? 0 : name.hashCode());
		result = prime * result + (owner == null ? 0 : owner.hashCode());
		return result;
	}
	// GENERATED:END

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
		ParameterTarget other = (ParameterTarget) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (owner == null) {
			if (other.owner != null) {
				return false;
			}
		} else if (!owner.equals(other.owner)) {
			return false;
		}
		return true;
	}
	// GENERATED:END

	@Override
	public boolean isWellFormed() {
		return owner.isWellFormed() && !StringUtils.isBlank(name);
	}

	@Override
	public String getProject() {
		return owner.getProject();
	}

	/**
	 * @return
	 * @see org.squashtest.tm.service.internal.batchimport.TestCaseTarget#getPath()
	 */
	@Override
	public String getPath() {
		return owner.getPath() + "/parameters/" + name;
	}

	/**
	 * @param path
	 * @see org.squashtest.tm.service.internal.batchimport.TestCaseTarget#setPath(java.lang.String)
	 */
	public void setPath(String path) {
		owner.setPath(path);
	}

	@Override
	public String toString() {
		return getPath();
	}

}
