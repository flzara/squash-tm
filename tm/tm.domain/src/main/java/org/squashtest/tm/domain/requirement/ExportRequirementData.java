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
package org.squashtest.tm.domain.requirement;

import org.squashtest.tm.domain.library.ExportData;
import org.squashtest.tm.domain.milestone.Milestone;

/**
 * 
 * Data support for jasper Requirement Export
 * 
 */
public class ExportRequirementData extends ExportData {

	private RequirementCriticality criticality;
	private String category;
	private Integer currentVersion;
	private RequirementStatus status;
	private String reference = "";
	private String milestone = "";
	private String requirementParentPath = "";
	private Long requirementParentId;
	public static final Long NO_REQUIREMENT_PARENT_ID = -1L;
	public static final String NO_REQUIREMENT_PARENT_PATH = "";

	public ExportRequirementData() {
		super();
	}

	public String getMilestone() {
		return milestone;
	}

	public void setMilestone(String milestone) {
		this.milestone = milestone;
	}

	public RequirementCriticality getCriticality() {
		return criticality;
	}

	public void setCriticality(RequirementCriticality criticality) {
		this.criticality = criticality;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public Integer getCurrentVersion() {
		return currentVersion;
	}

	public void setCurrentVersion(Integer currentVersion) {
		this.currentVersion = currentVersion;
	}

	public RequirementStatus getStatus() {
		return status;
	}

	public void setStatus(RequirementStatus status) {
		this.status = status;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		doSetReference(reference);
	}

	private void doSetReference(String reference) {
		if (reference != null) {
			this.reference = reference;
		}
	}

	public String getRequirementParentPath() {
		return requirementParentPath;
	}

	public void setRequirementParentId(Long requirementParentId) {
		this.requirementParentId = requirementParentId;
	}

	public void setRequirementParentPath(String requirementParentPath) {
		doSetRequirementParentPath(requirementParentPath);
	}

	private void doSetRequirementParentPath(String requirementParentPath) {
		this.requirementParentPath = requirementParentPath;
	}

	public Long getRequirementParentId() {
		return requirementParentId;
	}

	public ExportRequirementData(Requirement requirement, String requirementFolderPath, String requirementParentPath) {
		super(requirement);
		doSetReference(requirement.getReference());
		this.criticality = requirement.getCriticality();
		int index = 0;
		for (Milestone m : requirement.getCurrentVersion().getMilestones()) {
			if (index > 0) {
				this.milestone += " | ";
			}
			this.milestone += m.getLabel();
			index++;
		}
		this.category = requirement.getCategory().getCode();
		this.currentVersion = requirement.getCurrentVersion().getVersionNumber();
		this.status = requirement.getStatus();
		setFolderName(requirementFolderPath);
		doSetRequirementParentPath(requirementParentPath);
	}
}
