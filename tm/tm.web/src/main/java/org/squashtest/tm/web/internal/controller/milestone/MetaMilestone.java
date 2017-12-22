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
package org.squashtest.tm.web.internal.controller.milestone;

import java.util.Date;

import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.milestone.MilestoneStatus;

public class MetaMilestone{

	private Milestone milestone;

	private boolean directMembership = false;



	public MetaMilestone(Milestone milestone, boolean directMembership) {
		super();
		this.milestone = milestone;
		this.directMembership = directMembership;
	}

	public String getDescription() {
		return milestone.getDescription();
	}

	public String getLabel() {
		return milestone.getLabel();
	}

	public MilestoneStatus getStatus() {
		return milestone.getStatus();
	}

	public Date getEndDate() {
		return milestone.getEndDate();
	}

	public Long getId() {
		return milestone.getId();
	}

	public boolean isDirectMembership() {
		return directMembership;
	}

	public boolean isStatusAllowUnbind(){
		return milestone.getStatus().isAllowObjectModification();
	}


}
