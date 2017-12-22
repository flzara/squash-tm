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
package org.squashtest.tm.service.requirement;

import java.util.HashSet;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.requirement.RequirementCriticality;
import org.squashtest.tm.domain.requirement.RequirementStatus;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.domain.testcase.RequirementVersionCoverage;
import org.squashtest.tm.domain.testcase.TestCase;

/**
 * Partial view of a {@link RequirementVersionCoverage} verified by some test case.
 *
 * @author Gregory Fouquet, mpagnon
 *
 */
public class VerifiedRequirement {
	/**
	 * In the context of a given test case, the test case directly verifies this requirement (ie not through a test case
	 * call).
	 */
	private final boolean directVerification;
	private final Set<ActionTestStep> verifyingSteps = new HashSet<>(0);		//to set an actual content, see #withVerifyingStepsFrom(TestCase)
	private final RequirementVersion verifiedRequirementVersion;

	public VerifiedRequirement(@NotNull RequirementVersionCoverage requirementVersionCoverage, boolean directVerification) {
		super();
		this.verifiedRequirementVersion = requirementVersionCoverage.getVerifiedRequirementVersion();
		this.directVerification = directVerification;

	}
	public VerifiedRequirement(@NotNull RequirementVersion version, boolean directlyVerified) {
		super();
		this.verifiedRequirementVersion  = version;
		this.directVerification = directlyVerified;
	}

	private RequirementVersion getVerifiedRequirementVersion(){
		return this.verifiedRequirementVersion;
	}

	public Project getProject() {
		return getVerifiedRequirementVersion().getRequirement().getProject();
	}

	public RequirementStatus getStatus(){
		return getVerifiedRequirementVersion().getStatus();
	}

	public String getName() {
		return getVerifiedRequirementVersion().getName();
	}

	public int getVersionNumber(){
		return getVerifiedRequirementVersion().getVersionNumber();
	}

	public String getDescription() {
		return getVerifiedRequirementVersion().getDescription();
	}

	public String getReference() {
		return getVerifiedRequirementVersion().getReference();
	}

	public RequirementCriticality getCriticality() {
		return getVerifiedRequirementVersion().getCriticality();
	}

	public InfoListItem getCategory() {
		return getVerifiedRequirementVersion().getCategory();
	}

	public boolean isDirectVerification() {
		return directVerification;
	}

	public Set<Milestone> getMilestones(){
		return getVerifiedRequirementVersion().getMilestones();
	}

	public Long getId() {
		return getVerifiedRequirementVersion().getId();
	}
	public Set<ActionTestStep> getVerifyingSteps(){
		return verifyingSteps;
	}
	public boolean hasStepAsVerifying(long stepId) {
		for (ActionTestStep step : this.verifyingSteps) {
			if (step.getId().equals(stepId)) {
				return true;
			}
		}
		return false;
	}

	public VerifiedRequirement withVerifyingStepsFrom(TestCase testCase){

		RequirementVersionCoverage coverage = this.verifiedRequirementVersion.getRequirementVersionCoverageOrNullFor(testCase);

		if (coverage != null){
			this.verifyingSteps.addAll(coverage.getVerifyingSteps());
		}

		return this;
	}


}
