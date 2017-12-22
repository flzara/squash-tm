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
package org.squashtest.tm.service.statistics.requirement;

public final class RequirementStatusesStatistics {
	
	private int workInProgress;
	private int underReview;
	private int approved;
	private int obsolete;
	
	public RequirementStatusesStatistics() {
		super();
	}
	public RequirementStatusesStatistics(int workInProgress, int underReview,
			int approved, int obsolete) {
		super();
		this.workInProgress = workInProgress;
		this.underReview = underReview;
		this.approved = approved;
		this.obsolete = obsolete;
	}
	
	public int getWorkInProgress() {
		return workInProgress;
	}
	public void setWorkInProgress(int workInProgress) {
		this.workInProgress = workInProgress;
	}
	
	public int getUnderReview() {
		return underReview;
	}
	public void setUnderReview(int underReview) {
		this.underReview = underReview;
	}
	
	public int getApproved() {
		return approved;
	}
	public void setApproved(int approved) {
		this.approved = approved;
	}
	
	public int getObsolete() {
		return obsolete;
	}
	public void setObsolete(int obsolete) {
		this.obsolete = obsolete;
	}
	
}