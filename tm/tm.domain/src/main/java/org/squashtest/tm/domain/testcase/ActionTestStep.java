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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;

import org.hibernate.annotations.Type;
import org.squashtest.tm.domain.attachment.Attachment;
import org.squashtest.tm.domain.attachment.AttachmentHolder;
import org.squashtest.tm.domain.attachment.AttachmentList;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.BoundEntity;
import org.squashtest.tm.domain.execution.ExecutionStep;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.requirement.RequirementVersion;

@Entity
@PrimaryKeyJoinColumn(name = "TEST_STEP_ID")
public class ActionTestStep extends TestStep implements BoundEntity, AttachmentHolder {
	@Lob
	@Type(type="org.hibernate.type.TextType")
	private String action = "";

	@Lob
	@Type(type="org.hibernate.type.TextType")
	private String expectedResult = "";

	@OneToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH })
	@JoinColumn(name = "ATTACHMENT_LIST_ID")
	private final AttachmentList attachmentList = new AttachmentList();

	@ManyToMany(cascade = {CascadeType.REFRESH, CascadeType.DETACH})
	@JoinTable(name = "VERIFYING_STEPS", joinColumns = @JoinColumn(name = "TEST_STEP_ID", updatable = false, insertable = false), inverseJoinColumns = @JoinColumn(name = "REQUIREMENT_VERSION_COVERAGE_ID", updatable = false, insertable = false))
	private Set<RequirementVersionCoverage> requirementVersionCoverages= new HashSet<>();

	public ActionTestStep() {
		super();
	}

	public ActionTestStep(String action, String expectedResult) {
		super();
		this.action = action;
		this.expectedResult = expectedResult;
	}

	public void setAction(String action) {
		this.action = action;
	}


	public String getAction() {
		return action;
	}

	public void setExpectedResult(String expectedResult) {
		this.expectedResult = expectedResult;
	}

	public String getExpectedResult() {
		return expectedResult;
	}

	@Override
	public ActionTestStep createCopy() {
		ActionTestStep newTestStep = new ActionTestStep();
		newTestStep.action = this.action;
		newTestStep.expectedResult = this.expectedResult;

		// copy the attachments
		for (Attachment tcAttach : this.getAttachmentList().getAllAttachments()) {
			Attachment clone = tcAttach.hardCopy();
			newTestStep.getAttachmentList().addAttachment(clone);
		}

		return newTestStep;
	}

	@Override
	public void accept(TestStepVisitor visitor) {
		visitor.visit(this);

	}

	@Override
	public AttachmentList getAttachmentList() {
		return attachmentList;
	}

	@Override
	public List<ExecutionStep> createExecutionSteps(Dataset dataset) {
		List<ExecutionStep> returnList = new ArrayList<>(1);
		ExecutionStep exec = new ExecutionStep(this, dataset);
		returnList.add(exec);
		return returnList;
	}

	public Set<Attachment> getAllAttachments() {
		return attachmentList.getAllAttachments();
	}

	// *************** BoundEntity implementation *************

	@Override
	public Long getBoundEntityId() {
		return getId();
	}

	@Override
	public BindableEntity getBoundEntityType() {
		return BindableEntity.TEST_STEP;
	}

	@Override
	public Project getProject() {
		return getTestCase().getProject();
	}

	/**
	 * Simply remove the RequirementVersionCoverage from this.requirementVersionCoverages.
	 * @param requirementVersionCoverage : the entity to remove from this step's {@linkplain RequirementVersionCoverage}s list.
	 */
	public void removeRequirementVersionCoverage(RequirementVersionCoverage requirementVersionCoverage) {
		this.requirementVersionCoverages.remove(requirementVersionCoverage);
	}

	/**
	 *
	 * @return an UNMODIFIABLE set of this {@linkplain ActionTestStep}'s {@linkplain RequirementVersionCoverage}s.
	 */
	public Set<RequirementVersionCoverage> getRequirementVersionCoverages() {
		return Collections.unmodifiableSet(this.requirementVersionCoverages);
	}

	/**
	 * will simply add the given {@linkplain RequirementVersionCoverage} to this {@linkplain ActionTestStep#requirementVersionCoverages}
	 * @param requirementVersionCoverage
	 */
	public void addRequirementVersionCoverage(RequirementVersionCoverage requirementVersionCoverage) {
		this.requirementVersionCoverages.add(requirementVersionCoverage);

	}

	/**
	 *
	 * @return UNMODIFIABLE VIEW of verified requirements.
	 */
	public Set<RequirementVersion> getVerifiedRequirementVersions() {
		Set<RequirementVersion> verified = new HashSet<>();
		for(RequirementVersionCoverage coverage : requirementVersionCoverages){
			verified.add(coverage.getVerifiedRequirementVersion());
		}
		return Collections.unmodifiableSet(verified);
	}

	public Set<String> findUsedParametersNames() {
		Set<String> result = new HashSet<>();
		if(this.action != null){
			result.addAll(Parameter.findUsedParameterNamesInString(this.action));
		}
		if(this.expectedResult != null){
			result.addAll(Parameter.findUsedParameterNamesInString(this.expectedResult));
		}
		return result;
	}

	public static ActionTestStep createBlankActionStep() {

		return new ActionTestStep(null, null);
	}
}
