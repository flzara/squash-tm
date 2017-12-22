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

import java.net.URL;
import java.util.Date;

import org.squashtest.tm.core.foundation.lang.DateUtils;
import org.squashtest.tm.domain.execution.ExecutionStatus;

public class JsonExecutionInfo {

	private String executedOn;
	private String executedBy;
	private String executionStatus;
	private String automatedStatus;
	private String resultURL;

	public JsonExecutionInfo(){
		super();
	}

	public JsonExecutionInfo(String executedOn, String executedBy,
			String executionStatus, String automatedStatus, String resultURL) {
		super();
		this.executedOn = executedOn;
		this.executedBy = executedBy;
		this.executionStatus = executionStatus;
		this.automatedStatus = automatedStatus;
		this.resultURL = resultURL;
	}

	public JsonExecutionInfo(Date executedOn, String executedBy,
			ExecutionStatus executionStatus, ExecutionStatus automatedStatus, URL resultURL) {
		super();
		this.executedOn = DateUtils.formatIso8601DateTime(executedOn);
		this.executedBy = executedBy;
		this.executionStatus = executionStatus != null ? executionStatus.toString() : null;
		this.automatedStatus = automatedStatus != null ? automatedStatus.toString() : null;
		this.resultURL = resultURL != null ? resultURL.toExternalForm() : null;
	}

	public String getExecutedOn() {
		return executedOn;
	}

	public String getExecutedBy() {
		return executedBy;
	}

	public String getExecutionStatus() {
		return executionStatus;
	}

	public String getAutomatedStatus() {
		return automatedStatus;
	}

	public String getResultURL() {
		return resultURL;
	}


}
