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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.ClassBridge;
import org.hibernate.search.annotations.ClassBridges;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Parameter;
import org.hibernate.search.annotations.Store;
import org.squashtest.tm.domain.attachment.Attachment;
import org.squashtest.tm.domain.audit.AuditableMixin;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.BoundEntity;
import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.milestone.MilestoneHolder;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.resource.Resource;
import org.squashtest.tm.domain.search.CUFBridge;
import org.squashtest.tm.domain.search.CollectionSizeBridge;
import org.squashtest.tm.domain.search.InfoListItemBridge;
import org.squashtest.tm.domain.search.LevelEnumBridge;
import org.squashtest.tm.domain.testcase.RequirementVersionCoverage;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.exception.requirement.IllegalRequirementModificationException;
import org.squashtest.tm.exception.requirement.MilestoneForbidModificationException;
import org.squashtest.tm.exception.requirement.RequirementAlreadyVerifiedException;
import org.squashtest.tm.exception.requirement.RequirementVersionNotLinkableException;
import org.squashtest.tm.exception.requirement.link.UnlinkableLinkedRequirementVersionException;
import org.squashtest.tm.security.annotation.InheritsAcls;

/**
 * Represents a version of a requirement.
 *
 * @author Gregory Fouquet
 *
 */
@Entity
@Indexed
@PrimaryKeyJoinColumn(name = "RES_ID")
@InheritsAcls(constrainedClass = Requirement.class, collectionName = "versions")
@ClassBridges({
	@ClassBridge(name = "attachments", store = Store.YES, analyze = Analyze.NO, impl = RequirementVersionAttachmentBridge.class),
	@ClassBridge(name = "cufs", store = Store.YES, impl = CUFBridge.class, params = {
		@Parameter(name = "type", value = "requirement"),
		@Parameter(name = "inputType", value = "ALL")
	}),
	@ClassBridge(name = "cufs", store = Store.YES, analyze = Analyze.NO, impl = CUFBridge.class, params = {
		@Parameter(name = "type", value = "requirement"),
		@Parameter(name = "inputType", value = "DROPDOWN_LIST")
	}),
	@ClassBridge(name = "isCurrentVersion", store = Store.YES, analyze = Analyze.NO, impl = RequirementVersionIsCurrentBridge.class),
	@ClassBridge(name = "parent", store = Store.YES, analyze = Analyze.NO, impl = RequirementVersionHasParentBridge.class) })
public class RequirementVersion extends Resource implements BoundEntity, MilestoneHolder {

	public static final int MAX_REF_SIZE = 50;

	@NotNull
	@OneToMany(cascade = { CascadeType.REMOVE, CascadeType.REFRESH, CascadeType.DETACH }, mappedBy = "requirementVersion", fetch=FetchType.LAZY)
	private Set<RequirementVersionLink> requirementVersionLinks = new HashSet<>();

	@NotNull
	@OneToMany(cascade = { CascadeType.REMOVE, CascadeType.REFRESH, CascadeType.MERGE, CascadeType.DETACH }, mappedBy = "verifiedRequirementVersion", fetch=FetchType.LAZY)
	@Field(name = "testcases", analyze = Analyze.NO, store = Store.YES)
	@FieldBridge(impl = CollectionSizeBridge.class)
	private Set<RequirementVersionCoverage> requirementVersionCoverages = new HashSet<>();

	/***
	 * The requirement reference. It should usually be set by the Requirement.
	 */
	@NotNull
	@Field(analyze = Analyze.NO, store = Store.YES)
	@Size(min = 0, max = MAX_REF_SIZE)
	private String reference = "";

	@NotNull
	@Enumerated(EnumType.STRING)
	@Field(analyze = Analyze.NO, store = Store.YES)
	@FieldBridge(impl = LevelEnumBridge.class)
	private RequirementCriticality criticality = RequirementCriticality.UNDEFINED;

	@NotNull
	@ManyToOne
	@JoinColumn(name = "CATEGORY")
	@Field(analyze = Analyze.NO, store = Store.YES)
	@FieldBridge(impl = InfoListItemBridge.class)
	private InfoListItem category;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "REQUIREMENT_STATUS")
	@Field(analyze = Analyze.NO, store = Store.YES)
	@FieldBridge(impl = LevelEnumBridge.class)
	private RequirementStatus status = RequirementStatus.WORK_IN_PROGRESS;

	@NotNull
	@ManyToOne
	@JoinColumn(name = "REQUIREMENT_ID")
	@IndexedEmbedded(includeEmbeddedObjectId = true)
	private Requirement requirement;

	@Field(analyze = Analyze.NO, store = Store.YES)
	private int versionNumber = 1;


	@IndexedEmbedded(includeEmbeddedObjectId = true)
	@ManyToMany
	@JoinTable(name = "MILESTONE_REQ_VERSION", joinColumns = @JoinColumn(name = "REQ_VERSION_ID"), inverseJoinColumns = @JoinColumn(name = "MILESTONE_ID"))
	private Set<Milestone> milestones = new HashSet<>();

	@Transient
	private PropertiesSetter propertiesSetter = new PropertiesSetter();

	public RequirementVersion() {
		super();
	}

	@Override
	public void setName(String name) {
		/*
		 * as of Squash 1.12, because renaming is sometimes mandatory when the requirement is
		 * moved around (see requirement deletion specs for details), we no longer fail
		 * when renaming an requirement that normally shouldn't.
		 *
		 * //checkModifiable();
		 *
		 */
		super.setName(name);
	}

	@Override
	public void setDescription(String description) {
		checkModifiable();
		super.setDescription(description);
	}

	/**
	 * Returns an UNMODIFIABLE VIEW of the verifying test cases.
	 */
	public Set<TestCase> getVerifyingTestCases() {
		Set<TestCase> testCases = new HashSet<>();
		for (RequirementVersionCoverage coverage : this.requirementVersionCoverages) {
			testCases.add(coverage.getVerifyingTestCase());
		}
		return Collections.unmodifiableSet(testCases);
	}

	/**
	 * @throws RequirementVersionNotLinkableException
	 */
	public void checkLinkable() {
		if (!status.isRequirementLinkable()) {
			throw new RequirementVersionNotLinkableException(this);
		}
	}

	/***
	 * @return the reference of the requirement
	 */
	public String getReference() {
		return reference;
	}

	/***
	 * Set the requirement reference
	 *
	 * @param reference
	 */
	public void setReference(String reference) {
		checkModifiable();
		this.reference = reference;
	}

	/**
	 * @return {reference} - {name} if reference is not empty, or {name} if it is
	 *
	 */
	public String getFullName() {
		if (StringUtils.isBlank(reference)) {
			return getName();
		} else {
			return getReference() + " - " + getName();
		}
	}

	/***
	 * @return the requirement criticality
	 */
	public RequirementCriticality getCriticality() {
		return criticality;
	}

	/***
	 * Set the requirement criticality
	 *
	 * @param criticality
	 */
	public void setCriticality(RequirementCriticality criticality) {
		checkModifiable();
		this.criticality = criticality;
	}

	/**
	 * @return the requirement category
	 */
	public InfoListItem getCategory() {
		return category;
	}

	/***
	 * Set the requirement category
	 *
	 * @param category
	 */
	public void setCategory(InfoListItem category) {
		checkModifiable();
		this.category = category;
	}

	/**
	 * Sets this object's status, following status transition rules.
	 *
	 * @param status
	 */
	public void setStatus(RequirementStatus status) {
		checkStatusAccess(status);
		this.status = status;
	}

	public RequirementStatus getStatus() {
		return status;
	}

	private void checkModifiable() {
		if(!milestonesAllowEdit()){
			throw new MilestoneForbidModificationException();
		}
		if (!isModifiable()) {
			throw new IllegalRequirementModificationException();
		}
	}

	private void checkStatusAccess(RequirementStatus newStatus) {
		if (!status.getAllowsStatusUpdate() || !status.isTransitionLegal(newStatus)) {
			throw new IllegalRequirementModificationException();
		}
	}

	/**
	 *
	 * @return <code>true</code> if this requirement can be (un)linked by new verifying testcases or new requirement version
	 */
	public boolean isLinkable() {
		return getStatus().isRequirementLinkable();
	}

	/**
	 * Tells if this requirement's "intrinsic" properties can be modified. The following are not considered as
	 * "intrinsic" properties" : {@link #verifyingTestCases} are governed by the {@link #isLinkable()} state,
	 * {@link #status} is governed by itself.
	 *
	 * @return <code>true</code> if this requirement's properties can be modified.
	 */
	public boolean isModifiable() {
		return getStatus().isRequirementModifiable() && milestonesAllowEdit();
	}

	/**
	 * @return the requirement
	 */
	public Requirement getRequirement() {
		return requirement;
	}

	/**
	 * @return the versionNumber
	 */
	public int getVersionNumber() {
		return versionNumber;
	}

	public void setVersionNumber(int versionNumber) {
		this.versionNumber = versionNumber;
	}

	/**
	 * Should be used once before this entity is persisted by the requirement to which this version is added.
	 *
	 * @param requirement
	 */
	/* package-private */void setRequirement(Requirement requirement) {
		this.requirement = requirement;
	}

	/**
	 * Will create a copy of the requirement version with all attributes, and attachments. Does not copy
	 * requirementVersionCoverages.
	 *
	 * @return the requirement-version copy.
	 */
	public RequirementVersion createPastableCopy() {
		RequirementVersion copy = createBaselineCopy();
		copy.status = this.status;
		copy.versionNumber = this.versionNumber;
		copy.requirement = null;
		attachCopiesOfAttachmentsTo(copy);
		copy.bindSameMilestones(this);

		return copy;
	}

	private void attachCopiesOfAttachmentsTo(RequirementVersion copy) {
		for (Attachment attachment : this.getAttachmentList().getAllAttachments()) {
			copy.getAttachmentList().addAttachment(attachment.hardCopy());
		}
	}

	private RequirementVersion createBaselineCopy() {
		RequirementVersion copy = new RequirementVersion();
		copy.setName(this.getName());
		copy.setDescription(this.getDescription());
		copy.criticality = this.criticality;
		copy.category = this.category;
		copy.reference = this.reference;
		return copy;
	}

	private void bindSameMilestones(RequirementVersion src) {
		for (Milestone m : src.getMilestones()) {
			bindMilestone(m);
		}
	}

	public boolean isNotObsolete() {
		return RequirementStatus.OBSOLETE != status;
	}

	/**
	 * Creates a {@link RequirementVersion} to be used as the one right after this RequirementVersion.
	 *
	 * @return
	 */
	/* package-private */RequirementVersion createNextVersion() {
		RequirementVersion nextVersion = createBaselineCopy();
		nextVersion.status = RequirementStatus.WORK_IN_PROGRESS;
		nextVersion.versionNumber = this.versionNumber + 1;
		nextVersion.requirement = null;

		attachCopiesOfAttachmentsTo(nextVersion);

		return nextVersion;
	}

	/**
	 * Factory methiod which creates a {@link RequirementVersion} from a memento objet which holds the new object's
	 * target state. This method overrides any {@link RequirementStatus} workflow check.
	 *
	 * @param memento
	 * @return
	 */
	public static RequirementVersion createFromMemento(@NotNull RequirementVersionImportMemento memento) {
		RequirementVersion res = new RequirementVersion();

		res.setName(memento.getName());
		res.setDescription(memento.getDescription());
		res.criticality = memento.getCriticality();
		res.category = memento.formatCategory();
		res.milestones = memento.getMilestones();
		res.reference = memento.getReference();
		res.status = memento.getStatus();

		AuditableMixin audit = (AuditableMixin) res;

		audit.setCreatedOn(memento.getCreatedOn());
		audit.setCreatedBy(memento.getCreatedBy());

		return res;
	}

	// ***************** (detached) custom field section *************

	@Override
	public Long getBoundEntityId() {
		return getId();
	}

	@Override
	public BindableEntity getBoundEntityType() {
		return BindableEntity.REQUIREMENT_VERSION;
	}

	@Override
	public Project getProject() {
		if (requirement != null) {
			return requirement.getProject();
		} else {
			return null;
		}
	}

	/**
	 * Simply add the coverage to this.requirementVersionCoverage
	 *
	 * THIS DOES NOT SET THE coverage->version SIDE OF THE ASSOCIATION ! ONE SHOULD RATHER CALL
	 * coverage.setVerifiedRequirementVersion(..)
	 *
	 * @param coverage
	 */
	public void addRequirementCoverage(RequirementVersionCoverage coverage) {
		this.requirementVersionCoverages.add(coverage);
	}

	public RequirementVersionCoverage getRequirementVersionCoverageOrNullFor(TestCase testCase) {
		for (RequirementVersionCoverage coverage : this.requirementVersionCoverages) {
			if (coverage.getVerifyingTestCase().getId().equals(testCase.getId())) {
				return coverage;
			}
		}
		return null;
	}

	/**
	 * Simply remove the RequirementVersionCoverage from this.requirementVersionCoverages.
	 *
	 * @param requirementVersionCoverage
	 *            : the entity to remove from this requirement version's {@link RequirementVersionCoverage}s list.
	 * @throws RequirementVersionNotLinkableException
	 */
	public void removeRequirementVersionCoverage(RequirementVersionCoverage requirementVersionCoverage) {
		checkLinkable();
		this.requirementVersionCoverages.remove(requirementVersionCoverage);

	}

	/**
	 * Will create a copy of this.requirementVersionCoverages. Each {@link RequirementVersionCoverage} having, instead
	 * of this the copyVersion param as their verifiedRequirementVersion.
	 *
	 * @param copyVersion
	 * @return the copies of {@link RequirementVersionCoverage}s
	 * @throws RequirementVersionNotLinkableException
	 * @throws RequirementAlreadyVerifiedException
	 */
	public List<RequirementVersionCoverage> createRequirementVersionCoveragesForCopy(RequirementVersion copyVersion) {
		List<RequirementVersionCoverage> copies = new ArrayList<>();
		for (RequirementVersionCoverage coverage : this.requirementVersionCoverages) {
			RequirementVersionCoverage verifyingCopy = coverage.copyForRequirementVersion(copyVersion);
			copies.add(verifyingCopy);
		}
		return copies;
	}

	/**
	 * Will create a copy of this.requirementVersionLinks. Each {@link RequirementVersionLink} having, instead
	 * of this the copyVersion param as their requirementVersion.
	 *
	 * @param copyVersion
	 * @return the copies of {@link RequirementVersionLink}s
	 */
	public List<RequirementVersionLink> createRequirementVersionLinksForCopy(RequirementVersion copyVersion) {
		List<RequirementVersionLink> copies = new ArrayList<>();
		for (RequirementVersionLink link : this.requirementVersionLinks) {
			RequirementVersionLink linkCopy = link.copyForRequirementVersion(copyVersion);
			copies.add(linkCopy);
		}
		return copies;
	}

	public Set<RequirementVersionCoverage> getRequirementVersionCoverages() {
		return Collections.unmodifiableSet(requirementVersionCoverages);
	}

	public Set<RequirementVersionLink> getRequirementVersionLinks() {
		return Collections.unmodifiableSet(requirementVersionLinks);
	}

	@Override
	public Set<Milestone> getMilestones() {
		return milestones;
	}

	@Override
	public boolean isMemberOf(Milestone milestone) {
		return milestones.contains(milestone);
	}

	@Override
	public void bindMilestone(Milestone milestone) {
		milestones.add(milestone);
	}

	@Override
	public void unbindMilestone(Milestone milestone) {
		unbindMilestone(milestone.getId());
	}

	@Override
	public void unbindMilestone(Long milestoneId) {
		Iterator<Milestone> iter = milestones.iterator();

		while (iter.hasNext()) {
			Milestone m = iter.next();
			if (m.getId().equals(milestoneId)) {
				iter.remove();
				break;
			}
		}
	}

	private boolean milestonesAllowEdit() {
		for (Milestone m : milestones) {
			if (!m.getStatus().isAllowObjectModification()) {
				return false;
			}
		}
		return true;
	}


	/**
	 * @see org.squashtest.tm.domain.milestone.MilestoneHolder#unbindAllMilestones()
	 */
	@Override
	public void unbindAllMilestones() {
		milestones.clear();

	}

	@Override
	public Boolean doMilestonesAllowCreation() {
		Boolean allowed=Boolean.TRUE;
		for (Milestone m : getMilestones()){
			if (! m.getStatus().isAllowObjectCreateAndDelete()){
				allowed = Boolean.FALSE;
				break;
			}
		}
		return allowed;
	}

	@Override
	public Boolean doMilestonesAllowEdition() {
		Boolean allowed=Boolean.TRUE;
		for (Milestone m : getMilestones()){
			if (! m.getStatus().isAllowObjectModification()){
				allowed = Boolean.FALSE;
				break;
			}
		}
		return allowed;
	};

	public PropertiesSetter getPropertySetter()

	{
		return propertiesSetter;
	}


	public class PropertiesSetter {



		public void setName(String name) {
			RequirementVersion.super.setName(name);
		}

		public void setDescription(String description) {
			RequirementVersion.super.setDescription(description);
		}

		public void setReference(String reference) {
			RequirementVersion.this.reference = reference;
		}

		public void setCriticality(RequirementCriticality criticality) {
			RequirementVersion.this.criticality = criticality;
		}

		public void setCategory(InfoListItem category) {
			RequirementVersion.this.category = category;
		}

		public void setStatus(RequirementStatus status) {
			RequirementVersion.this.status = status;
		}

		public void setVersionNumber(int versionNumber) {
			RequirementVersion.this.versionNumber = versionNumber;
		}

		public void setCreatedBy(String createdBy) {
			AuditableMixin audit = (AuditableMixin) RequirementVersion.this;
			audit.setCreatedBy(createdBy);
		}

		public void setCreatedOn(Date createdOn) {
			AuditableMixin audit = (AuditableMixin) RequirementVersion.this;
			audit.setCreatedOn(createdOn);

		}

		public void setLastModifiedBy(String lastModifiedBy) {
			AuditableMixin audit = (AuditableMixin) RequirementVersion.this;
			audit.setLastModifiedBy(lastModifiedBy);

		}

		public void setLastModifiedOn(Date lastModifiedOn) {
			AuditableMixin audit = (AuditableMixin) RequirementVersion.this;
			audit.setLastModifiedOn(lastModifiedOn);

		}
	}

}
