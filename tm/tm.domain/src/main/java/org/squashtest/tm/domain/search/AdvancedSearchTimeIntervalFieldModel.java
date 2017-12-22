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
package org.squashtest.tm.domain.search;

import java.util.Date;

public class AdvancedSearchTimeIntervalFieldModel implements AdvancedSearchFieldModel{


	private final AdvancedSearchFieldModelType type;

	private Date startDate;

	private Date endDate;

	private boolean ignoreBridge = false;

	public AdvancedSearchTimeIntervalFieldModel() {
		type = AdvancedSearchFieldModelType.TIME_INTERVAL;
	}

	protected AdvancedSearchTimeIntervalFieldModel(AdvancedSearchFieldModelType type) {
		this.type = type;
	}

	@Override
	public AdvancedSearchFieldModelType getType() {
		return this.type;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}



	@Override
	public boolean isIgnoreBridge() {
		return this.ignoreBridge;
	}
}
