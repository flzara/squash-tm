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

import org.squashtest.tm.service.importer.EntityType;
import org.squashtest.tm.service.importer.Target;

public class RequirementLinkTarget implements Target{

	private RequirementVersionTarget sourceVersion;
	
	private RequirementVersionTarget destVersion;
		
	public RequirementLinkTarget() {
		super();
		sourceVersion = new RequirementVersionTarget();
		destVersion = new RequirementVersionTarget();
	}
	
	public RequirementLinkTarget(RequirementVersionTarget source, RequirementVersionTarget dest){
		super();
		this.sourceVersion = source;
		this.destVersion = dest;
	}
	
	
	@Override
	public EntityType getType() {
		return EntityType.REQUIREMENT_LINK;
	}

	@Override
	public boolean isWellFormed() {
		return sourceVersion.isWellFormed() && destVersion.isWellFormed();
	}

	public RequirementVersionTarget getSourceVersion() {
		return sourceVersion;
	}

	public void setSourceVersion(RequirementVersionTarget sourceVersion) {
		this.sourceVersion = sourceVersion;
	}

	public RequirementVersionTarget getDestVersion() {
		return destVersion;
	}

	public void setDestVersion(RequirementVersionTarget destVersion) {
		this.destVersion = destVersion;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((destVersion == null) ? 0 : destVersion.hashCode());
		result = prime * result + ((sourceVersion == null) ? 0 : sourceVersion.hashCode());
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
		RequirementLinkTarget other = (RequirementLinkTarget) obj;
		if (destVersion == null) {
			if (other.destVersion != null)
				return false;
		} else if (!destVersion.equals(other.destVersion))
			return false;
		if (sourceVersion == null) {
			if (other.sourceVersion != null)
				return false;
		} else if (!sourceVersion.equals(other.sourceVersion))
			return false;
		return true;
	}

	
	
}
