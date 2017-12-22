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
package org.squashtest.tm.service.internal.bugtracker;

import org.squashtest.tm.domain.bugtracker.Issue;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.requirement.RequirementVersion;

/**
 * Same as {@link Pair}, but specialized for the requirement issues case
 */
public class RequirementIssueSupport {
	private RequirementVersion requirementVersion;
	private Execution execution;
	private Issue issue;

	public RequirementIssueSupport(RequirementVersion requirementVersion, Execution execution, Issue issue) {
		this.requirementVersion = requirementVersion;
		this.execution = execution;
		this.issue = issue;
	}

	public RequirementVersion getRequirementVersion() {
		return requirementVersion;
	}

	public Execution getExecution() {
		return execution;
	}

	public Issue getIssue() {
		return issue;
	}
}
