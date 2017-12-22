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
package org.squashtest.tm.domain.testcase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;
import org.hibernate.search.annotations.DocumentId;
import org.squashtest.tm.domain.Identified;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.exception.requirement.RequirementAlreadyVerifiedException;
import org.squashtest.tm.exception.requirement.RequirementVersionNotLinkableException;
import org.squashtest.tm.exception.testcase.StepDoesNotBelongToTestCaseException;

/**
 * Entity representing a The coverage of a {@link RequirementVersion} by a {@link TestCase}. The {@link ActionTestStep}
 * responsible for the requirement coverage can be specified in the verifyingSteps property.
 *
 * @author mpagnon
 *
 */
@NamedQueries({
	@NamedQuery(name = "RequirementVersionCoverage.byRequirementVersionAndTestCase", query = "select rvc from RequirementVersionCoverage rvc join rvc.verifiedRequirementVersion rv join rvc.verifyingTestCase tc where rv.id = :rvId and tc.id = :tcId"),
	@NamedQuery(name = "RequirementVersionCoverage.byRequirementVersionAndTestCases", query = "select rvc from RequirementVersionCoverage rvc join rvc.verifiedRequirementVersion rv join rvc.verifyingTestCase tc where rv.id = :rvId and tc.id in :tcIds"),
	@NamedQuery(name = "RequirementVersionCoverage.byTestCaseAndRequirementVersions", query = "select rvc from RequirementVersionCoverage rvc join rvc.verifiedRequirementVersion rv join rvc.verifyingTestCase tc where tc.id = :tcId and rv.id in :rvIds"),
	@NamedQuery(name = "RequirementVersionCoverage.numberByTestCase", query = "select count(rvc) from RequirementVersionCoverage rvc join rvc.verifyingTestCase tc where tc.id = :tcId"),
	@NamedQuery(name = "RequirementVersionCoverage.numberByTestCases", query = "select count(rvc) from RequirementVersionCoverage rvc join rvc.verifyingTestCase tc where tc.id in :tcIds"),
	@NamedQuery(name = "RequirementVersionCoverage.numberDistinctVerifiedByTestCases", query = "select count(distinct rv) from RequirementVersionCoverage rvc join rvc.verifiedRequirementVersion rv join rvc.verifyingTestCase tc where tc.id in :tcIds"),
	@NamedQuery(name = "RequirementVersionCoverage.byRequirementVersionsAndTestStep", query = "select rvc from RequirementVersionCoverage rvc join rvc.verifiedRequirementVersion rv join rvc.verifyingSteps step where step.id = :stepId and rv.id in :rvIds"), })
@Entity
public class RequirementVersionCoverage implements Identified {
	@Id
	@Column(name = "REQUIREMENT_VERSION_COVERAGE_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "requirement_version_coverage_requirement_version_coverage_i_seq")
	@SequenceGenerator(name = "requirement_version_coverage_requirement_version_coverage_i_seq", sequenceName = "requirement_version_coverage_requirement_version_coverage_i_seq", allocationSize = 1)
	@DocumentId
	private Long id;

	@NotNull
	@ManyToOne(cascade=CascadeType.DETACH)
	@JoinColumn(name = "VERIFYING_TEST_CASE_ID", referencedColumnName = "TCLN_ID")
	private TestCase verifyingTestCase;

	@ManyToOne
	@JoinColumn(name = "VERIFIED_REQ_VERSION_ID", referencedColumnName = "RES_ID")
	private RequirementVersion verifiedRequirementVersion;

	@NotNull
	@ManyToMany(mappedBy="requirementVersionCoverages", cascade=CascadeType.DETACH)
	private Set<ActionTestStep> verifyingSteps = new HashSet<>();

	/**
	 * @throws RequirementVersionNotLinkableException
	 * @param verifiedRequirementVersion
	 */
	public RequirementVersionCoverage(RequirementVersion verifiedRequirementVersion) {
		this(verifiedRequirementVersion, null);
	}

	/**
	 * @throws RequirementAlreadyVerifiedException
	 * @throws RequirementVersionNotLinkableException
	 * @param requirementVersion
	 * @param testCase
	 */
	public RequirementVersionCoverage(RequirementVersion requirementVersion, TestCase testCase) {
		// check - these can throw exception (not so good a practice) so they **must** be performed before we change the passed args state
		requirementVersion.checkLinkable();
		if (testCase != null) {
			testCase.checkRequirementNotVerified(requirementVersion);
		}

		// set
		this.verifiedRequirementVersion = requirementVersion;
		verifiedRequirementVersion.addRequirementCoverage(this);
		if (testCase != null) {
			testCase.addRequirementCoverage(this);
			this.verifyingTestCase = testCase;
		}
	}

	/**
	 * @throws RequirementAlreadyVerifiedException
	 * @throws RequirementVersionNotLinkableException
	 * @param requirement
	 * @param testCase
	 */
	public RequirementVersionCoverage(Requirement requirement, TestCase testCase) {
		this(requirement.getCurrentVersion(), testCase);
	}

	public TestCase getVerifyingTestCase() {
		return verifyingTestCase;
	}

	public void setVerifyingTestCase(TestCase verifyingTestCase) {
		if (this.verifiedRequirementVersion != null) {
			verifyingTestCase.checkRequirementNotVerified(this, verifiedRequirementVersion);
		}
		this.verifyingTestCase = verifyingTestCase;
	}

	public RequirementVersion getVerifiedRequirementVersion() {
		return verifiedRequirementVersion;
	}

	public void setVerifiedRequirementVersion(RequirementVersion verifiedRequirementVersion) {
		if (this.verifyingTestCase != null && this.verifiedRequirementVersion != null) {
			this.verifyingTestCase.checkRequirementNotVerified(this, verifiedRequirementVersion);
		}
		verifiedRequirementVersion.checkLinkable();
		this.verifiedRequirementVersion = verifiedRequirementVersion;
		verifiedRequirementVersion.addRequirementCoverage(this);

	}


	@Override
	public Long getId() {
		return id;
	}

	public Set<ActionTestStep> getVerifyingSteps() {
		return verifyingSteps;
	}

	/**
	 * Checks that all steps belong to this {@linkplain RequirementVersionCoverage#verifyingTestCase} and add them to
	 * this {@linkplain RequirementVersionCoverage#verifyingSteps}.
	 *
	 * @param steps
	 * @throws StepDoesNotBelongToTestCaseException
	 *
	 */
	public void addAllVerifyingSteps(Collection<ActionTestStep> steps) {
		checkStepsBelongToTestCase(steps);

		this.verifyingSteps.addAll(steps);
		for (ActionTestStep step : steps) {
			step.addRequirementVersionCoverage(this);
		}
	}

	/**
	 * Will check that all steps are found in this.verifyingTestCase.steps. The check is with
	 * {@link TestCase#hasStep(TestStep)}
	 *
	 * @param steps
	 * @throws StepDoesNotBelongToTestCaseException
	 *             if one step doesn't belong to this.verifyingTestCase.
	 */
	private void checkStepsBelongToTestCase(Collection<ActionTestStep> steps) {
		for (ActionTestStep step : steps) {
			if (!verifyingTestCase.hasStep(step)) {
				throw new StepDoesNotBelongToTestCaseException(verifyingTestCase.getId(), step.getId());
			}
		}

	}

	RequirementVersionCoverage() {
		super();
	}


	public RequirementVersionCoverage copyForRequirementVersion(RequirementVersion rvCopy) {
		RequirementVersionCoverage rvcCopy = new RequirementVersionCoverage();
		rvcCopy.verifyingTestCase = this.verifyingTestCase;
		rvcCopy.verifiedRequirementVersion = rvCopy;
		rvcCopy.verifyingSteps.addAll(this.verifyingSteps);
		return rvcCopy;
	}

	/**
	 * <p>
	 * Returns a copy of a RequirementVersionCoverage adapted to a given TestCase (
	 * this TestCase is usually different from the owner of the RequirementVersionCoverage)
	 *
	 * In short it means that the given TestCase will verify the target Requirement and if the
	 * original TestCase had steps verifying it, the corresponding steps in the given TestCase
	 * will too.
	 * This method is primarily used in the use-case 'copy a test case with all its stuffs'.
	 * </p>
	 *
	 * <p>
	 * 	In some case such copy is impossible because the requirement cannot be linked
	 * 	because the target Requirement has a status 'OBSOLETE' (or other reasons if
	 * 	more rules appears in the future).
	 *
	 *
	 * 	In such case NULL will be returned. Be sure to check for NULL.
	 * </p>
	 *
	 *
	 *
	 * @param tcCopy
	 * @return a copy of this RequirementVersionCoverage, or NULL if that was impossible.
	 */
	public RequirementVersionCoverage copyForTestCase(TestCase tcCopy) {
		if (! this.verifiedRequirementVersion.isLinkable()){
			return null;
		}
		// copy verified requirement

		RequirementVersionCoverage rvcCopy = new RequirementVersionCoverage();
		/*For performance issue it's better to construct an empty ReqVersionCoverage then set the verifiedRequirementVersion without using the setter
		If you use the setter or the constructor with verifiedRequirementVersion, you will call addRequirementCoverage on the requirementVersion
		It's a set so hibernate will load ALL requirementVersionCoverage just to add the new requirementVersionCoverage. It may take a quite
		long time for the request, and also load alot of entities in the session, resulting in a large increase in flushing time for every operation after
		this one. When performing copy/paste on data with alot of requirement coverage this increase dramaticaly the time needed to perform the operation
		See Issue 4943*/
		rvcCopy.verifiedRequirementVersion = this.verifiedRequirementVersion;
		// set verifying test case
		rvcCopy.setVerifyingTestCase(tcCopy);
		tcCopy.addRequirementCoverage(rvcCopy);
		// set verifying steps
		List<ActionTestStep> stepToVerify = new ArrayList<>(this.verifyingSteps.size());
		for (ActionTestStep step : this.verifyingSteps) {
			int indexInSource = this.verifyingTestCase.getPositionOfStep(step.getId());
			stepToVerify.add((ActionTestStep) tcCopy.getSteps().get(indexInSource));
		}
		rvcCopy.addAllVerifyingSteps(stepToVerify);
		return rvcCopy;
	}

	/**
	 * @throws RequirementVersionNotLinkableException
	 */
	public void checkCanRemoveTestCaseFromRequirementVersion() {
		this.verifiedRequirementVersion.checkLinkable();
	}

	/**
	 * Returns true if the given step id matches on of the verifying steps id.
	 *
	 * @param stepId
	 * @return
	 */
	public boolean hasStepAsVerifying(long stepId) {
		for (ActionTestStep step : this.verifyingSteps) {
			if (step.getId().equals(stepId)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Will remove the step matching the given id from this.verifyingSteps. If the step is not found nothing special
	 * happens.
	 *
	 * @param testStepId
	 *            : the id of the step to remove.
	 */
	public void removeVerifyingStep(long testStepId) {
		Iterator<ActionTestStep> iterator = this.verifyingSteps.iterator();
		while (iterator.hasNext()) {
			ActionTestStep step = iterator.next();
			if (step.getId().equals(testStepId)) {
				iterator.remove();
			}
		}

	}

	/**
	 * Check if this {@link RequirementVersionCoverage} is linked to one or more {@link TestStep}
	 * @return
	 */
	public boolean hasSteps(){
		return !verifyingSteps.isEmpty();
	}

}
