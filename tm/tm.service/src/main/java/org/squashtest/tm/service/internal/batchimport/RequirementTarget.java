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

import org.squashtest.tm.core.foundation.lang.PathUtils;
import org.squashtest.tm.service.importer.EntityType;
import org.squashtest.tm.service.importer.Target;
import org.squashtest.tm.service.importer.WithPath;

public class RequirementTarget implements Target, WithPath {

	private String path;

	private Integer order;

	//convenient attribute to keep the id of the requirement in database
	private Long id;

	// for synchronized requirements, the remote key is also part of the key.
	private String remoteKey;

	// for synced requirement with a synchronisation id, we must also pass the sync id... i know
	private Long remoteSynchronisationId;

	public RequirementTarget() {
		super();
	}


	public RequirementTarget(String path) {
		super();
		setPath(path);
	}

	public RequirementTarget(String path, Integer order) {
		super();
		setPath(path);
		this.order = order;
	}

	@Override
	public EntityType getType() {
		return EntityType.REQUIREMENT;
	}

	@Override
	public boolean isWellFormed() {
		return PathUtils.isPathWellFormed(path);
	}

	@Override
	public String getProject() {
		return PathUtils.extractProjectName(path);
	}

	public void setPath(String path) {
		//Issue 5480.
		//We must trim the path to avoid nasty null pointer exception
		String sanitizedPath = path.trim();
		this.path = PathUtils.cleanMultipleSlashes(sanitizedPath);
	}

	@Override
	public String getPath() {
		return path;
	}

	public Integer getOrder() {
		return order;
	}


	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}

	public String getRemoteKey() {
		return remoteKey;
	}

	/**
	 * Don't remove that method even if your IDE tells you it's safe. It's used by req importer plugins...
	 * @param remoteKey
	 */
	public void setRemoteKey(String remoteKey) {
		this.remoteKey = remoteKey;
	}

	public boolean isSynchronized() {
		return this.remoteKey != null;
	}

	public Long getRemoteSynchronisationId() {
		return remoteSynchronisationId;
	}

	public void setRemoteSynchronisationId(Long remoteSynchronisationId) {
		this.remoteSynchronisationId = remoteSynchronisationId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (path == null ? 0 : path.hashCode());
		result = prime * result + (remoteKey == null ? 0 : remoteKey.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RequirementTarget other = (RequirementTarget) obj;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		if (remoteKey == null) {
			if (other.remoteKey != null)
				return false;
		} else if (!remoteKey.equals(other.remoteKey))
			return false;
		return true;
	}


	public boolean isRootRequirement() {
		String[] names = PathUtils.splitPath(path);
		return names.length == 2; // that is, composed of a project and a requirement name only.
	}

}
