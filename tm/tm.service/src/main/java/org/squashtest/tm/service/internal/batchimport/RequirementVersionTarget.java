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

import org.squashtest.tm.domain.requirement.RequirementStatus;
import org.squashtest.tm.service.importer.EntityType;
import org.squashtest.tm.service.importer.Target;
import org.squashtest.tm.service.importer.WithPath;

public class RequirementVersionTarget implements Target, WithPath {

	private RequirementTarget requirement;

	private Integer version;

	private String unconsistentName;

	/**
	 * Used for update. Convenient boolean which store the fact that one of the
	 * milestone label present in import file isn't valid. Spec 5085 specify
	 * that in update mode if one of the milestone is unknown the milestone
	 * binding isn't modified.
	 */
	private boolean rejectedMilestone = false;

	private RequirementStatus importedRequirementStatus = RequirementStatus.WORK_IN_PROGRESS;

	public RequirementVersionTarget(RequirementTarget requirement, Integer version) {
		super();
		this.requirement = requirement;
		this.version = version;
	}


	public RequirementVersionTarget() {
		super();
		this.requirement = new RequirementTarget();
	}


	@Override
	public EntityType getType() {
		return EntityType.REQUIREMENT_VERSION;
	}

	@Override
	public boolean isWellFormed() {
		return requirement != null && requirement.isWellFormed();
	}

	@Override
	public String getProject() {
		return requirement.getProject();
	}

	@Override
	public String getPath() {
		return requirement.getPath();
	}
	
	public void setPath(String path){
		requirement.setPath(path);
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public RequirementTarget getRequirement() {
		return requirement;
	}

	public String getUnconsistentName() {
		return unconsistentName;
	}


	public void setUnconsistentName(String unconsistentName) {
		this.unconsistentName = unconsistentName;
	}


	public RequirementStatus getImportedRequirementStatus() {
		return importedRequirementStatus;
	}


	public void setImportedRequirementStatus(
		RequirementStatus importedRequirementStatus) {
		this.importedRequirementStatus = importedRequirementStatus;
	}


	public boolean isRejectedMilestone() {
		return rejectedMilestone;
	}


	/**
	 * @deprecated not used - o be pruned in 1.15 if still unused
	 */
	@Deprecated
	public void rejectedMilestone() {
		this.rejectedMilestone = true;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (requirement == null ? 0 : requirement.hashCode());
		result = prime * result + (version == null ? 0 : version.hashCode());
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
		RequirementVersionTarget other = (RequirementVersionTarget) obj;
		if (requirement == null) {
			if (other.requirement != null) {
				return false;
			}
		} else if (!requirement.equals(other.requirement)) {
			return false;
		}
		if (version == null) {
			if (other.version != null) {
				return false;
			}
		} else if (!version.equals(other.version)) {
			return false;
		}
		return true;
	}

}
