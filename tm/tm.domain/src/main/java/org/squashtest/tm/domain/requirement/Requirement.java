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

import static org.squashtest.tm.domain.requirement.RequirementStatus.APPROVED;
import static org.squashtest.tm.domain.requirement.RequirementStatus.OBSOLETE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.OrderColumn;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.validation.constraints.NotNull;

import org.hibernate.search.annotations.*;
import org.squashtest.tm.core.foundation.exception.NullArgumentException;
import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.domain.library.NodeContainer;
import org.squashtest.tm.domain.library.NodeContainerVisitor;
import org.squashtest.tm.domain.library.NodeVisitor;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.search.CollectionSizeBridge;
import org.squashtest.tm.exception.DuplicateNameException;
import org.squashtest.tm.exception.NoVerifiableRequirementVersionException;
import org.squashtest.tm.exception.requirement.CopyPasteObsoleteException;
import org.squashtest.tm.exception.requirement.IllegalRequirementVersionCreationException;

/**
 * Entity requirement
 *
 * Note that much of its setters will throw an IllegalRequirementModificationException if a modification is attempted
 * while the status does not allow it.
 *
 * @author bsiri
 *
 */

@Entity
@Indexed
@PrimaryKeyJoinColumn(name = "RLN_ID")
@ClassBridges({
	@ClassBridge(name = "children", analyze = Analyze.NO, store = Store.YES, impl=RequirementCountChildrenBridge.class)
})
public class Requirement extends RequirementLibraryNode<RequirementVersion> implements NodeContainer<Requirement> {

	/**
	 * The resource of this requirement is the latest version of the requirement.
	 */
	@OneToOne(cascade = {CascadeType.ALL})
	@JoinColumn(name = "CURRENT_VERSION_ID")
	private RequirementVersion resource;

	@OneToMany(mappedBy = "requirement", cascade = {CascadeType.ALL})
	@OrderBy("versionNumber DESC")
	private List<RequirementVersion> versions = new ArrayList<>();



	/*
        Note about cascading:
	CascadeType.PERSIST is desirable because it allows us to cascade-create a complete grape of object (useful when importing for instance)
	CascadeType.DELETE is not desirable, because we need to call custom code for proper deletion (see the deletion services)
	*/
	@OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	@OrderColumn(name = "CONTENT_ORDER")
	@JoinTable(name = "RLN_RELATIONSHIP", joinColumns = @JoinColumn(name = "ANCESTOR_ID"), inverseJoinColumns = @JoinColumn(name = "DESCENDANT_ID"))
	private List<Requirement> children = new ArrayList<>();

	@Column
	@Enumerated(EnumType.STRING)
	private ManagementMode mode = ManagementMode.NATIVE;

	@OneToOne(mappedBy = "requirement", cascade = {CascadeType.REMOVE, CascadeType.PERSIST}, optional = true)
	private RequirementSyncExtender syncExtender;

	protected Requirement() {
		super();
	}

	/**
	 * Creates a new requirement which "latest version" is the given {@link RequirementVersion}
	 *
	 */
	public Requirement(@NotNull RequirementVersion version) {
		resource = version;
		addVersion(version);
	}


	private void addVersion(RequirementVersion version) {
		/*
		 * prevent the requirement from having more than
		 * one version if this requirement is synchronized
		 */
		if (!versions.isEmpty() && isSynchronized()) {
			throw new IllegalRequirementVersionCreationException();
		}
		// else we can add the version normally
		else {
			versions.add(version);
			version.setRequirement(this);
		}
	}

	@Override
	public void setName(String name) {
		resource.setName(name);
	}

	@Override
	public void setDescription(String description) {
		resource.setDescription(description);
	}

	@Override
	public void accept(RequirementLibraryNodeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public void accept(NodeVisitor visitor) {
		visitor.visit(this);
	}

	/***
	 * @return the reference of the requirement
	 */
	public String getReference() {
		return resource.getReference();
	}

	/***
	 * Set the requirement reference
	 *
	 */
	public void setReference(String reference) {
		resource.setReference(reference);
	}

	/**
	 * Get the all the requirement versions numbers and status by the version Id
	 */
	public List<RequirementVersion> getRequirementVersions() {
		return Collections.unmodifiableList(versions);
	}

	/**
	 * Creates a copy usable in a copy / paste operation. The copy is associated to no version, it should be done by the
	 * caller (the latest version might not be eligible for copy / paste).
	 */
	@Override
	public Requirement createCopy() {
		if (!getCurrentVersion().isNotObsolete()) {
			throw new CopyPasteObsoleteException();
		}
		RequirementVersion latestVersionCopy = getCurrentVersion().createPastableCopy();
		Requirement copy = new Requirement(latestVersionCopy);
		copy.notifyAssociatedWithProject(this.getProject());
		return copy;
	}

	/**
	 * Will create copies for all non obsolete versions older than the current version, and add it to the copy.
	 *
	 * @param copy : The requirement copy
	 * @return a TreeMap of RequirementVersion copy by source ordered younger to older.
	 */
	public SortedMap<RequirementVersion, RequirementVersion> addPreviousVersionsCopiesToCopy(Requirement copy) {
		TreeMap<RequirementVersion, RequirementVersion> copyBySource = new TreeMap<>(new RequirementVersionNumberComparator());
		for (RequirementVersion sourceVersion : this.versions) {
			if (isNotLatestVersion(sourceVersion) && sourceVersion.isNotObsolete()) {
				RequirementVersion copyVersion = sourceVersion.createPastableCopy();
				copyBySource.put(sourceVersion, copyVersion);
				copy.addVersion(copyVersion);
			}
		}
		return copyBySource;
	}


	private boolean isNotLatestVersion(RequirementVersion sourceVersion) {
		return !getCurrentVersion().equals(sourceVersion);
	}

	/***
	 * @return the requirement criticality
	 */
	public RequirementCriticality getCriticality() {
		return resource.getCriticality();
	}

	/***
	 * Set the requirement criticality
	 *
	 */
	public void setCriticality(RequirementCriticality criticality) {
		resource.setCriticality(criticality);
	}

	/***
	 * @return the requirement category
	 */
	public InfoListItem getCategory() {
		return resource.getCategory();
	}

	/***
	 * Set the requirement category
	 *
	 */
	public void setCategory(InfoListItem category) {
		resource.setCategory(category);
	}

	public void setStatus(RequirementStatus status) {
		resource.setStatus(status);
	}

	public RequirementStatus getStatus() {
		return resource.getStatus();
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
		return getStatus().isRequirementModifiable();
	}

	@Override
	public String getName() {
		return resource.getName();
	}

	@Override
	public String getDescription() {
		return resource.getDescription();
	}

	public RequirementVersion getCurrentVersion() {
		return resource;
	}

	@Override
	public RequirementVersion getResource() {
		return resource;
	}

	public void setCurrentVersion(RequirementVersion version) {
		this.resource = version;
	}

	public void increaseVersion() {
		RequirementVersion previous = resource;
		RequirementVersion next = previous.createNextVersion();
		resource = next;
		versions.add(0, next);
		next.setRequirement(this);
	}

	public void increaseVersion(RequirementVersion newVersion) {
		newVersion.setVersionNumber(resource.getVersionNumber() + 1);
		resource = newVersion;
		versions.add(0, newVersion);
		newVersion.setRequirement(this);

	}

	/**
	 * returns this requirement's version which should be linked to a test case by default.
	 *
	 */
	public RequirementVersion getDefaultVerifiableVersion() {
		RequirementVersion verifiable = findLatestApprovedVersion();

		if (verifiable == null) {
			verifiable = findLatestNonObsoleteVersion();
		}

		if (verifiable == null) {
			throw new NoVerifiableRequirementVersionException(this);
		}

		return verifiable;
	}

	private RequirementVersion findLatestApprovedVersion() {
		for (RequirementVersion version : versions) {
			if (APPROVED == version.getStatus()) {
				return version;
			}
		}

		return null;
	}

	private RequirementVersion findLatestNonObsoleteVersion() {
		for (RequirementVersion version : versions) {
			if (version.isNotObsolete()) {
				return version;
			}
		}

		return null;
	}

	/**
	 *
	 * @return an unmodifiable view of this requirement's versions
	 */
	public List<RequirementVersion> getUnmodifiableVersions() {
		return Collections.unmodifiableList(versions);
	}

	/**
	 *
	 * @return false if all requirement versions are obsolete
	 */
	public boolean hasNonObsoleteVersion() {
		for (RequirementVersion version : this.versions) {
			if (version.getStatus() != OBSOLETE) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return the last non obsolete requirement version <br>
	 *         or null if all versions are obsolete
	 */
	public RequirementVersion findLastNonObsoleteVersion() {

		for (RequirementVersion version : this.versions) {
			if (version.getStatus() != OBSOLETE) {
				return version;
			}
		}

		return null;
	}

	/**
	 * Modified for [Feat 5085] as we can now have non successive RequirementVersion number
	 */
	public RequirementVersion findRequirementVersion(int versionNumber) {
		for (RequirementVersion requirementVersion : versions) {
			if (requirementVersion.getVersionNumber() == versionNumber) {
				return requirementVersion;
			}
		}
		return null;
	}

	// ****************************** implementation of NodeContainer ************************************


	@Override
	public void addContent(@NotNull Requirement child) throws DuplicateNameException,
		NullArgumentException {
		checkContentNameAvailable(child);
		children.add(child);
		children = new ArrayList<>(children);
		child.notifyAssociatedWithProject(this.getProject());
	}

	@Override
	public void addContent(@NotNull Requirement child, int position) throws DuplicateNameException,
		NullArgumentException {
		checkContentNameAvailable(child);
		if (position >= children.size()) {
			children.add(child);
		} else {
			children.add(position, child);
		}
		children = new ArrayList<>(children);
		child.notifyAssociatedWithProject(this.getProject());
	}

	private void checkContentNameAvailable(Requirement child) throws DuplicateNameException {
		if (!isContentNameAvailable(child.getName())) {
			throw new DuplicateNameException(child.getName(), child.getName());
		}
	}

	@Override
	public boolean isContentNameAvailable(String name) {
		for (Requirement child : children) {
			if (child.getName().equals(name)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public List<Requirement> getContent() {
		return children;
	}

	@Override
	public Collection<Requirement> getOrderedContent() {
		return children;
	}

	@Override
	public boolean hasContent() {
		return !children.isEmpty();
	}

	@Override
	public void removeContent(Requirement exChild)
		throws NullArgumentException {
		children.remove(exChild);
		children = new ArrayList<>(children);
	}

	@Override
	public List<String> getContentNames() {
		List<String> contentNames = new ArrayList<>(children.size());
		for (Requirement child : children) {
			contentNames.add(child.getName());
		}
		return contentNames;
	}

	@Override
	public void accept(NodeContainerVisitor visitor) {
		visitor.visit(this);
	}

	/**
	 * finds which version of this requirement is bound to the given milestone, or null
	 * if there is none
	 */
	public RequirementVersion findByMilestone(Milestone milestone) {
		for (RequirementVersion version : versions) {
			if (version.isMemberOf(milestone)) {
				return version;
			}
		}
		return null;
	}

	public boolean meOrMyChildHaveAVersionBoundToMilestone(Milestone milestone) {

		if (findByMilestone(milestone) != null) {
			return true;
		}

		for (Requirement child : children) {
			if (child.meOrMyChildHaveAVersionBoundToMilestone(milestone)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Add a requirement version which is not a copy of current version.
	 * Used by import [Feat 5085] because we need to create non successive RequirementVersion
	 */
	public void addExistingRequirementVersion(RequirementVersion requirementVersion) {
		Integer newVersionNumber = requirementVersion.getVersionNumber();
		if (findRequirementVersion(newVersionNumber) == null) {
			versions.add(0, requirementVersion);
			requirementVersion.setRequirement(this);
			//checking if the imported RequirementVersion has a versionNumber superior to the current version number
			if (newVersionNumber > resource.getVersionNumber()) {
				resource = requirementVersion;
			}
		} else {
			throw new IllegalArgumentException("RequirementVersion with version number " + newVersionNumber + " already exist in this Requirement, id : " + getId());
		}
	}

	/**
	 * @return the last non obsolete requirement version after an import [Feat 5085]<br>
	 *         or null if all versions are obsolete
	 */
	public RequirementVersion findLastNonObsoleteVersionAfterImport() {
		SortedMap<Integer, RequirementVersion> sortedVersions = new TreeMap<>();

		for (RequirementVersion version : this.versions) {
			if (version.getStatus() != OBSOLETE) {
				sortedVersions.put(version.getVersionNumber(), version);
			}
		}

		return sortedVersions.isEmpty() ? null : sortedVersions.get(sortedVersions.lastKey());
	}


	// **************** requirement sync section *****************

	public RequirementSyncExtender getSyncExtender() {
		return syncExtender;
	}

	public void setSyncExtender(RequirementSyncExtender syncExtender) {
		this.mode = ManagementMode.SYNCHRONIZED;
		this.syncExtender = syncExtender;
	}

	public void removeSyncExtender() {
		this.mode = ManagementMode.NATIVE;
		this.syncExtender = null;
	}

	public boolean isSynchronized() {
		return this.mode == ManagementMode.SYNCHRONIZED && syncExtender != null;
	}

	@Override
	public boolean allowContentWithIdenticalName() {
		return true;
	}
}
