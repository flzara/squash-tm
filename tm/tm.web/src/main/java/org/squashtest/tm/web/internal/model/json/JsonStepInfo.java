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

import org.squashtest.tm.core.foundation.lang.DateUtils;

import java.util.Date;

public class JsonStepInfo {

	private String executedOn;
	private String executedBy;
	
	public JsonStepInfo(){
		super();
	}

	public JsonStepInfo(String executedOn, String executedBy) {
		super();
		this.executedOn = executedOn;
		this.executedBy = executedBy;
	}
	
	public JsonStepInfo(Date executedOn, String executedBy) {
		super();
		this.executedOn = DateUtils.formatIso8601DateTime(executedOn);
		this.executedBy = executedBy;
	}

	public String getExecutedOn() {
		return executedOn;
	}

	public String getExecutedBy() {
		return executedBy;
	}
	
	
	
}
