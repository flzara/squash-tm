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
package org.squashtest.tm.web.internal.model.json;

import java.util.Date;

import org.squashtest.tm.core.foundation.lang.DateUtils;


public class JsonIteration {
	
	private long id;	
	private String name;	
	private String scheduledStartDate;	
	private String scheduledEndDate;
	
	public JsonIteration(){
		super();
	}
	
	public JsonIteration(long id, String name, String scheduledStartDate, String scheduledEndDate) {
		super();
		this.id = id;
		this.name = name;
		this.scheduledStartDate = scheduledStartDate;
		this.scheduledEndDate = scheduledEndDate;
	}
	
	public JsonIteration(long id, String name,  Date scheduledStartDate, Date scheduledEndDate) {
		super();
		this.id = id;
		this.name = name;
		this.scheduledStartDate = toISO8601(scheduledStartDate);
		this.scheduledEndDate = toISO8601(scheduledEndDate);
	}
	
	

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getScheduledStartDate() {
		return scheduledStartDate;
	}

	public void setScheduledStartDate(String scheduledStartDate) {
		this.scheduledStartDate = scheduledStartDate;
	}

	public String getScheduledEndDate() {
		return scheduledEndDate;
	}

	public void setScheduledEndDate(String scheduledEndDate) {
		this.scheduledEndDate = scheduledEndDate;
	}
	
	private String toISO8601(Date date){
		if (date != null) {
			return DateUtils.formatIso8601DateTime(date);
		}
		else{
			return null;
		}
	}
	
}
