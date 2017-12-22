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
package org.squashtest.tm.service.internal.dto.json;

import java.util.Date;

import org.squashtest.tm.domain.milestone.MilestoneRange;
import org.squashtest.tm.domain.milestone.MilestoneStatus;

public class JsonMilestone {

	private long id;

	private String label;

	private MilestoneStatus status;

	private MilestoneRange range;

	private Date endDate;

	private String ownerLogin;

	private boolean canCreateDelete;

	private boolean canEdit;

	public JsonMilestone(){
		super();
	}

	public JsonMilestone(
			long id,
			String label,
			MilestoneStatus status,
			MilestoneRange range,
			Date endDate,
			String ownerLogin) {

		super();
		this.id = id;
		this.label = label;
		this.setStatus(status);
		this.range = range;
		this.endDate = endDate;
		this.ownerLogin = ownerLogin;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public MilestoneStatus getStatus() {
		return status;
	}

	public void setStatus(MilestoneStatus status) {
		this.status = status;
		this.canCreateDelete = status.isAllowObjectCreateAndDelete();
		this.canEdit = status.isAllowObjectModification();
	}

	public MilestoneRange getRange() {
		return range;
	}

	public void setRange(MilestoneRange range) {
		this.range = range;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public String getOwnerLogin() {
		return ownerLogin;
	}

	public void setOwnerLogin(String ownerLogin) {
		this.ownerLogin = ownerLogin;
	}

	public boolean isCanCreateDelete() {
		return canCreateDelete;
	}

	public void setCanCreateDelete(boolean canCreateDelete) {
		this.canCreateDelete = canCreateDelete;
	}

	public boolean isCanEdit() {
		return canEdit;
	}

	public void setCanEdit(boolean canEdit) {
		this.canEdit = canEdit;
	}


}
