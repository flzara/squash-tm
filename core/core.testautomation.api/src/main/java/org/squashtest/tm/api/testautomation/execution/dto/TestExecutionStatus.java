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
package org.squashtest.tm.api.testautomation.execution.dto;

import java.io.Serializable;
import java.util.Date;

import javax.validation.constraints.NotNull;

/**
 * This class encapsulates test execution status updates.
 * 
 * @author edegenetais
 * 
 */
/*
 * TODO This is not per se a "status", we already have ExecutionStatus. It's more of a partial state. Rename as
 * something else. AutomatedExecutionState ?
 */
public class TestExecutionStatus implements Serializable {
	/**
	 * for serialization.
	 */
	private static final long serialVersionUID = 7818656596437427978L;
	/** Name of the test. */
	@NotNull
	private String testName;
	/** Name of the group the test belongs to. */
	@NotNull
	private String testGroupName;
	/** Start time. Mandatory. */
	@NotNull
	private Date startTime;
	/**
	 * End time. May be null (while the execution is still running for example).
	 */
	private Date endTime;
	/** New status of the test. */
	@NotNull
	private ExecutionStatus status;
	/** Explanation message if any (mostly, short versions of error messages). */
	@NotNull
	private String statusMessage = "";
	
	private String resultUrl = null;

	/**
	 * @return Name of the test.
	 */
	public String getTestName() {
		return testName;
	}

	/**
	 * @param testName
	 *            Name of the test.
	 */
	public void setTestName(String testName) {
		if (testName == null) {
			throw new IllegalArgumentException("test name cannot be null.");
		}
		this.testName = testName;
	}

	/**
	 * @return Name of the group the test belongs to.
	 */
	public String getTestGroupName() {
		return testGroupName;
	}

	/**
	 * @param testGroupName
	 *            Name of the group the test belongs to.
	 */
	public void setTestGroupName(String testGroupName) {
		if (testGroupName == null) {
			throw new IllegalArgumentException("test group name cannot be null.");
		}
		this.testGroupName = testGroupName;
	}

	/**
	 * @return Start time. Mandatory.
	 */
	public Date getStartTime() {
		return startTime;
	}

	/**
	 * @param startTime
	 *            Start time. Mandatory.
	 */
	public void setStartTime(Date startTime) {
		if (startTime == null) {
			throw new IllegalArgumentException("start time cannot be null.");
		}
		this.startTime = startTime;
	}

	/**
	 * @return End time. May be null (while the execution is still running for example)
	 */
	public Date getEndTime() {
		return endTime;
	}

	/**
	 * @param endTime
	 *            End time. May be null (while the execution is still running for example)
	 */
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	/**
	 * @return New status of the test.
	 */
	public ExecutionStatus getStatus() {
		return status;
	}

	/**
	 * @param status
	 *            New status of the test.
	 */
	public void setStatus(ExecutionStatus status) {
		if (status == null) {
			throw new IllegalArgumentException("status cannot be null.");
		}
		this.status = status;
	}

	/**
	 * @return Explanation message if any (mostly, short versions of error messages).
	 */
	public String getStatusMessage() {
		return statusMessage;
	}

	/**
	 * @param statusMessage
	 *            Explanation message if any (mostly, short versions of error messages).
	 */
	public void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage == null ? "" : statusMessage;
	}
	
	public void setResultUrl(String resultUrl){
		this.resultUrl = resultUrl;
	}
	
	public String getResultUrl(){
		return resultUrl;
	}
}