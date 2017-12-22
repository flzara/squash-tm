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
package org.squashtest.tm.internal.domain.report.common.dto;

import org.squashtest.tm.domain.requirement.RequirementCriticality;
import org.squashtest.tm.domain.requirement.RequirementStatus;

/**
 * this represent a requirement version mpagnon
 * 
 */
public class ReqCoverageByTestRequirementSingleDto {
	/***
	 * The name of the parent which contains the requirement. Default value is " - "
	 */
	private String parent = " - ";
	/***
	 * The requirement reference and name
	 */
	private String reference, label;
	/**
	 * The requirement version number.
	 */
	private Integer versionNumber = 0;
	/***
	 * Requirement criticality
	 */
	private RequirementCriticality criticality;

	/***
	 * Total number of Test case which verify this requirement
	 */
	private int associatedTestCaseNumber = 0;
	/**
	 * Requirement Status
	 */
	private RequirementStatus status;

	private String milestone;

	// ACCESSORS

	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public RequirementCriticality getCriticality() {
		return criticality;
	}

	public void setCriticality(RequirementCriticality criticality) {
		this.criticality = criticality;
	}

	public int getAssociatedTestCaseNumber() {
		return associatedTestCaseNumber;
	}

	public void setAssociatedTestCaseNumber(int associatedTestCaseNumber) {
		this.associatedTestCaseNumber = associatedTestCaseNumber;
	}

	public boolean hasAssociatedTestCases() {
		return getAssociatedTestCaseNumber() > 0;
	}

	public int getVersionNumber() {
		return versionNumber;
	}

	public void setVersionNumber(int versionNumber) {
		this.versionNumber = versionNumber;
	}

	public RequirementStatus getStatus() {
		return status;
	}

	public void setStatus(RequirementStatus status) {
		this.status = status;
	}

	public ReqCoverageByTestStatType convertCrit() {
		return ReqCoverageByTestStatType.valueOf(this.criticality.toString());
	}

	public ReqCoverageByTestStatType convertCritVerif() {
		return ReqCoverageByTestStatType.valueOf(this.criticality.toString() + "_VERIFIED");
	}

	public String getMilestone() {
		return milestone;
	}

	public void setMilestone(String milestone) {
		this.milestone = milestone;
	}


}
