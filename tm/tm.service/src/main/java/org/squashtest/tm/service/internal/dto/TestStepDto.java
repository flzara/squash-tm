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

import java.util.HashSet;
import java.util.Set;

public class TestStepDto {

	private Long id;

	private Integer stepOrder;

	private Long calledTestCaseId = -1L;

	private Set<Long> requirementSet = new HashSet<>();

	public TestStepDto(Long id, Integer stepOrder) {
		super();
		this.id = id;
		this.stepOrder = stepOrder;
	}

	public TestStepDto(){
		super();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getCalledTestCaseId() {
		return calledTestCaseId;
	}

	public void setCalledTestCaseId(Long calledTestCaseId) {
		this.calledTestCaseId = calledTestCaseId;
	}

	public Set<Long> getRequirementSet() {
		return requirementSet;
	}

	public void setRequirementSet(Set<Long> requirementSet) {
		this.requirementSet = requirementSet;
	}

	public void addRequirement(Long requirementId){
		requirementSet.add(requirementId);
	}

	public boolean isCallStep(){
		return this.calledTestCaseId != -1L;
	}

	public Integer getStepOrder() {
		return stepOrder;
	}

	public void setStepOrder(Integer stepOrder) {
		this.stepOrder = stepOrder;
	}
}
