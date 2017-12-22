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

import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.project.Project;

import java.util.Set;

/**
 * This decorator represents a Requirement Version involved in a link with another Requirement Version.
 * The purpose is to display it in the linked Requirement Versions table.
 *
 * Created by jlor on 17/05/2017.
 */
public class LinkedRequirementVersion extends RequirementVersion {

	/**
	 * The decorated RequirementVersion.
	 */
	private final RequirementVersion decoratedRequirementVersion;
	/**
	 * The role the RequirementVersion occupies in the link.
	 */
	private final String role;

	public LinkedRequirementVersion(RequirementVersion decoratedRequirementVersion, String role) {
		this.decoratedRequirementVersion = decoratedRequirementVersion;
		this.role = role;
	}

	public RequirementVersion getDecoratedRequirementVersion() {
		return decoratedRequirementVersion;
	}

	public String getRole() {
		return role;
	}

	@Override
	public Long getId() {
		return decoratedRequirementVersion.getId();
	}

	@Override
	public Project getProject() {
		return decoratedRequirementVersion.getProject();
	}

	@Override
	public String getReference() {
		return decoratedRequirementVersion.getReference();
	}

	@Override
	public String getName() {
		return decoratedRequirementVersion.getName();
	}

	@Override
	public Set<Milestone> getMilestones() {
		return decoratedRequirementVersion.getMilestones();
	}

	@Override
	public int getVersionNumber() {
		return decoratedRequirementVersion.getVersionNumber();
	}

	@Override
	public String getDescription() {
		return decoratedRequirementVersion.getDescription();
	}
}
