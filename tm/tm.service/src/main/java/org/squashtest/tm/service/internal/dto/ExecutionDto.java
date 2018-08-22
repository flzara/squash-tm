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
package org.squashtest.tm.service.internal.dto;

import java.util.HashMap;
import java.util.Map;

public class ExecutionDto {
	private Long id;
	private String status="";
	private Map<Long, ExecutionStepDto> steps = new HashMap<>();
	private boolean automated = false;

	public ExecutionDto(Long id, String status, boolean automated) {
		super();
		this.id = id;
		this.status = status;
		this.automated = automated;
	}

	public ExecutionDto(Long id) {
		super();
		this.id = id;
	}

	public ExecutionDto(){
		super();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Map<Long, ExecutionStepDto> getSteps() {
		return steps;
	}

	public void setSteps(Map<Long, ExecutionStepDto> steps) {
		this.steps = steps;
	}

	public void addStep(ExecutionStepDto step){
		this.steps.put(step.getId(), step);
	}

	public ExecutionStepDto getStep(Long id){
		return this.steps.get(id);
	}

	public boolean isAutomated() {
		return automated;
	}

	public void setAutomated(boolean automated) {
		this.automated = automated;
	}
}
