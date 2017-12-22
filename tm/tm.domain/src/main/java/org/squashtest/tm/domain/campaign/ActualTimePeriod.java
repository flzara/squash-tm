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

import javax.persistence.Basic;
import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.squashtest.tm.validation.constraint.IsActualPeriodValid;

/**
 * Embeddable value for an actual date period. Actual date periods have a manual / computed flag. This class only holds
 * data, the client of this class is responsible for the computation of dates.
 * 
 * @author Gregory Fouquet
 * 
 */
@Embeddable
@IsActualPeriodValid
public class ActualTimePeriod {
	@Temporal(TemporalType.TIMESTAMP)
	private Date actualStartDate;

	@Temporal(TemporalType.TIMESTAMP)
	private Date actualEndDate;
	@Basic(optional = false)
	private boolean actualStartAuto;

	@Basic(optional = false)
	private boolean actualEndAuto;

	public ActualTimePeriod() {
		super();
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

	public boolean isActualStartAuto() {
		return actualStartAuto;
	}

	public void setActualStartAuto(boolean actualStartAuto) {
		this.actualStartAuto = actualStartAuto;
	}

	public boolean isActualEndAuto() {
		return actualEndAuto;
	}

	public void setActualEndAuto(boolean actualEndAuto) {
		this.actualEndAuto = actualEndAuto;
	}

	public static ActualTimePeriod createAutoComputedPeriod() {
		ActualTimePeriod period = new ActualTimePeriod();
		period.actualStartAuto = true;
		period.actualEndAuto = true;
		return period;
	}
}
