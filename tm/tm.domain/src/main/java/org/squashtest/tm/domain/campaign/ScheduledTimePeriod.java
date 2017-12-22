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
package org.squashtest.tm.domain.campaign;

import java.util.Date;

import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.squashtest.tm.validation.constraint.IsScheduledPeriodValid;

/**
 * Embeddable value for a scheduled (i.e. user defined) date period.
 * 
 * @author Gregory Fouquet
 * 
 */
@Embeddable
@IsScheduledPeriodValid
public class ScheduledTimePeriod {
	@Temporal(TemporalType.TIMESTAMP)
	private Date scheduledStartDate;

	@Temporal(TemporalType.TIMESTAMP)
	private Date scheduledEndDate;

	public void setScheduledStartDate(Date startDate) {
		this.scheduledStartDate = startDate;
	}

	public Date getScheduledStartDate() {
		return scheduledStartDate;
	}

	public void setScheduledEndDate(Date endDate) {
		this.scheduledEndDate = endDate;
	}

	public Date getScheduledEndDate() {
		return scheduledEndDate;
	}
}
