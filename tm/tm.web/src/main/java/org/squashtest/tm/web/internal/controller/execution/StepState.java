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
package org.squashtest.tm.web.internal.controller.execution;

import org.squashtest.tm.domain.execution.ExecutionStep;

public class StepState {

	private long currentStepId;
	private String currentStepStatus;

	public long getCurrentStepId() {
		return currentStepId;
	}

	public void setCurrentStepId(long currentStepId) {
		this.currentStepId = currentStepId;
	}

	public String getCurrentStepStatus() {
		return currentStepStatus;
	}

	public void setCurrentStepStatus(String currentStepStatus) {
		this.currentStepStatus = currentStepStatus;
	}

	public StepState() {
		super();
	}

	public StepState(long currentStepId, String currentStepStatus) {
		super();
		this.currentStepId = currentStepId;
		this.currentStepStatus = currentStepStatus;
	}

	public StepState(ExecutionStep step) {
		this.currentStepId = step.getId();
		this.currentStepStatus = step.getExecutionStatus().name();
	}

}
