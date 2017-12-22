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
package org.squashtest.tm.domain.bugtracker;

import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStep;

/**
 * Mainly used to pair a Bugged entity with an issue together.
 *
 * @author bsiri
 *
 * @param <ISSUE>
 *            can be pretty much anything you need to pair with a bugged thing actually.
 */
public class IssueOwnership<ISSUE> {

	private final ISSUE issue;
	private final IssueDetector owner;

	public IssueOwnership(ISSUE issue, IssueDetector owner) {
		this.issue = issue;
		this.owner = owner;
	}

	public IssueDetector getOwner() {
		return owner;
	}

	public ISSUE getIssue() {
		return issue;
	}

	public Execution getExecution() {
		Execution execution = null;
		if (ExecutionStep.class.isAssignableFrom(owner.getClass())){
			ExecutionStep step = (ExecutionStep) owner;
			execution = step.getExecution();
		} else if (Execution.class.isAssignableFrom(owner.getClass())){
			execution = (Execution) owner;
		}
		return execution;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "IssueOwnership [issue=" + issue + ", owner=" + owner + "]";
	}

}
