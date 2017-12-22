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
import org.squashtest.tm.core.foundation.lang.PathUtils;
import org.squashtest.tm.service.importer.EntityType;
import org.squashtest.tm.service.importer.Target;
import org.squashtest.tm.service.importer.WithPath;

import java.util.Arrays;

/**
 * Bean that holds the property of a test case target.<br/>
 * Properties are : <br/>
 * {@link #path}<br/>
 * {@link #order}
 */
public class TestCaseTarget implements Target, WithPath {
	/**
	 * The path from the project name to the targeted test case.
	 */
	private String path;
	/**
	 * The order of the test case in it's container.
	 */
	private Integer order;

	public TestCaseTarget() {
		super();
	}

	public TestCaseTarget(String path) {
		super();
		setPath(path);
	}

	public TestCaseTarget(String path, Integer order) {
		super();
		setPath(path);
		this.order = order;
	}

	@Override
	public EntityType getType() {
		return EntityType.TEST_CASE;
	}

	@Override
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		//Issue 5480.
		//We must trim the path to avoid nasty null pointer exception
		String sanitizedPath = path.trim();
		this.path = PathUtils.cleanMultipleSlashes(sanitizedPath);
	}

	public Integer getOrder() {
		return order;
	}

	public void setOrder(Integer order) {
		this.order = order;
	}

	@Override
	// GENERATED:START
	public int hashCode() {
		final int prime = 31;
		int result = 47;
		result = prime * result + (path == null ? 0 : path.hashCode());
		return result;
	}

	// GENERATED:END

	@Override
	// GENERATED:START
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
		TestCaseTarget other = (TestCaseTarget) obj;
		if (path == null) {
			if (other.path != null) {
				return false;
			}
		} else if (!path.equals(other.path)) {
			return false;
		}
		return true;
	}

	// GENERATED:END

	@Override
	public String toString() {
		return path;
	}

	@Override
	public boolean isWellFormed() {
		return PathUtils.isPathWellFormed(path);
	}

	@Override
	public String getProject() {
		return PathUtils.extractProjectName(path);
	}

	public String getName() {
		return PathUtils.extractTestCaseName(path);
	}

	/**
	 * note : return the names of each folders, including the project, of this test case. Assumes that the path is well
	 * formed.
	 */
	public String getFolder() {

		String[] names = PathUtils.splitPath(path);
		String[] shortened = Arrays.copyOf(names, names.length - 1);

		return "/" + StringUtils.join(shortened, '/');
	}

	public boolean isRootTestCase() {
		String[] names = PathUtils.splitPath(path);
		return names.length == 2; // that is, composed of a project and a test case name only.
	}
}
