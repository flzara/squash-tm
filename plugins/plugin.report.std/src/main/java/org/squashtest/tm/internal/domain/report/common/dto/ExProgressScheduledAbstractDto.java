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
package org.squashtest.tm.internal.domain.report.common.dto;

import java.util.Date;
import java.util.List;

import org.squashtest.tm.domain.campaign.IterationTestPlanItem;

public class ExProgressScheduledAbstractDto extends ExProgressAbstractDto{


	protected Date scheduledStartDate;
	protected Date scheduledEndDate;
	protected Date actualStartDate;
	protected Date actualEndDate;


	public ExProgressScheduledAbstractDto(){
		super();
	}

	public ExProgressScheduledAbstractDto(List<IterationTestPlanItem> testPlans) {
		super(testPlans);
	}

	public Date getScheduledStartDate() {
		return scheduledStartDate;
	}

	public void setScheduledStartDate(Date scheduledStartDate) {
		this.scheduledStartDate = scheduledStartDate;
	}

	public Date getScheduledEndDate() {
		return scheduledEndDate;
	}

	public void setScheduledEndDate(Date scheduledEndDate) {
		this.scheduledEndDate = scheduledEndDate;
	}

	public Date getActualStartDate() {
		return actualStartDate;
	}

	public void setActualStartDate(Date actualStartDate) {
		this.actualStartDate = actualStartDate;
	}

	public Date getActualEndDate() {
		return actualEndDate;
	}

	public void setActualEndDate(Date actualEndDate) {
		this.actualEndDate = actualEndDate;
	}



}