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

public class DatasetTarget implements Target, WithPath {
	private final TestCaseTarget testCase;
	private String name;

	public DatasetTarget() {
		super();
		testCase = new TestCaseTarget();
	}

	public DatasetTarget(TestCaseTarget testCase, String name) {
		super();
		this.testCase = testCase;
		this.name = name;
	}

	@Override
	public EntityType getType() {
		return EntityType.DATASET;
	}

	public TestCaseTarget getTestCase() {
		return testCase;
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
		result = prime * result + (testCase == null ? 0 : testCase.hashCode());
		return result;
	}

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
		DatasetTarget other = (DatasetTarget) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (testCase == null) {
			if (other.testCase != null) {
				return false;
			}
		} else if (!testCase.equals(other.testCase)) {
			return false;
		}
		return true;
	}

	// GENERATED:END

	@Override
	public boolean isWellFormed() {
		return testCase.isWellFormed() && !StringUtils.isBlank(name);
	}

	@Override
	public String getProject() {
		return testCase.getProject();
	}


	@Override
	public String getPath() {
		return testCase.getPath() + "/datasets/" + name;
	}

	/**
	 * @param path
	 * @see org.squashtest.tm.service.internal.batchimport.TestCaseTarget#setPath(java.lang.String)
	 */
	public void setPath(String path) {
		testCase.setPath(path);
	}

	@Override
	public String toString() {
		return getPath();
	}

}
