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

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class ExecutionStepDto {
	private Long id;
	private String status="";
	private Integer stepOrder;
	private String lastExecutedBy = "";
	private Date lastExecutedOn;
	private String comment = "";
	private Long testStepId;
	private Set<Long> requirementSet = new HashSet<>();
	private Set<Long> issueSet = new HashSet<>();

	public ExecutionStepDto(Long id, String status) {
		super();
		this.id = id;
		this.status = status;
	}

	public ExecutionStepDto(Long id, String status, Integer stepOrder, Long testStepId) {
		super();
		this.id = id;
		this.status = status;
		this.stepOrder = stepOrder;
		this.testStepId = testStepId;
	}

	public ExecutionStepDto(){
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

	public Integer getStepOrder() {
		return stepOrder;
	}

	public void setStepOrder(Integer stepOrder) {
		this.stepOrder = stepOrder;
	}

	public String getLastExecutedBy() {
		return lastExecutedBy;
	}

	public void setLastExecutedBy(String lastExecutedBy) {
		this.lastExecutedBy = lastExecutedBy;
	}

	public Date getLastExecutedOn() {
		return lastExecutedOn;
	}

	public void setLastExecutedOn(Date lastExecutedOn) {
		this.lastExecutedOn = lastExecutedOn;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Long getTestStepId() {
		return testStepId;
	}

	public void setTestStepId(Long testStepId) {
		this.testStepId = testStepId;
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

	public Set<Long> getIssueSet() {
		return issueSet;
	}

	public void setIssueSet(Set<Long> issueSet) {
		this.issueSet = issueSet;
	}

	public void addIssue(Long issueId){
		issueSet.add(issueId);
	}
}
