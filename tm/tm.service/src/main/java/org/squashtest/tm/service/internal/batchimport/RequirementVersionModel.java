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
package org.squashtest.tm.service.internal.batchimport;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Pojo class for the requirement version import process
 * @author Julien
 *
 */
public class RequirementVersionModel {

	private TargetStatus status;
	private Set<String> linkedMilestones;
	private boolean milestoneLocked;

	public RequirementVersionModel(TargetStatus status) {
		super();
		this.status = status;
		this.linkedMilestones = new HashSet<>();
	}

	public TargetStatus getStatus() {
		return status;
	}

	public void setStatus(TargetStatus status) {
		this.status = status;
	}

	public Set<String> getLinkedMilestones() {
		return Collections.unmodifiableSet(linkedMilestones);
	}

	public void addMilestone(String milestone){
		linkedMilestones.add(milestone);
	}

	public boolean isMilestoneLocked() {
		return milestoneLocked;
	}

	public void setMilestoneLocked(boolean milestoneLocked) {
		this.milestoneLocked = milestoneLocked;
	}
}
