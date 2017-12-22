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
package org.squashtest.tm.service.internal.batchimport.testcase.excel;

import org.squashtest.tm.core.foundation.lang.PathUtils;
import org.squashtest.tm.service.importer.EntityType;
import org.squashtest.tm.service.importer.Target;

public class CoverageTarget implements Target {

	private String reqPath;
	private int reqVersion;
	private String tcPath;

	public String getReqPath() {
		return reqPath;
	}

	public void setReqPath(String reqPath) {
		String sanitizedPath = reqPath.trim();
		this.reqPath = PathUtils.cleanMultipleSlashes(sanitizedPath);
	}

	public int getReqVersion() {
		return reqVersion;
	}

	public void setReqVersion(int reqVersion) {
		this.reqVersion = reqVersion;
	}

	public String getTcPath() {
		return tcPath;
	}

	public void setTcPath(String tcPath) {
		String sanitizedPath = tcPath.trim();
		this.tcPath = PathUtils.cleanMultipleSlashes(sanitizedPath);
	}

	public boolean isReqPathWellFormed() {
		return PathUtils.isPathWellFormed(reqPath);
	}

	public boolean isTcPathWellFormed() {
		return PathUtils.isPathWellFormed(tcPath);
	}

	@Override
	public EntityType getType() {
		return EntityType.COVERAGE;
	}

	@Override
	public boolean isWellFormed() {
		return isReqPathWellFormed() && isTcPathWellFormed();
	}

}
