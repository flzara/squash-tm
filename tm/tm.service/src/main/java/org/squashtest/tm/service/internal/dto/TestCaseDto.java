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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TestCaseDto {
	private Long id;
	private String reference="";
	private String name="";
	private String importance="";
	private String nature="";
	private String type="";
	private String status="";
	private Set<Long> requirementSet = new HashSet<>();
	private Long projectId;
	private String projectName="";
	private List<String> milestoneList = new ArrayList<>();

	public TestCaseDto(Long id, String reference, String name, String importance, String nature, String type, String status, Long projectId, String projectName) {
		super();
		this.id = id;
		this.reference = reference;
		this.name = name;
		this.importance = importance;
		this.nature = nature;
		this.type = type;
		this.status = status;
		this.projectId = projectId;
		this.projectName = projectName;
	}

	public TestCaseDto(){
		super();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getImportance() {
		return importance;
	}

	public void setImportance(String importance) {
		this.importance = importance;
	}

	public String getNature() {
		return nature;
	}

	public void setNature(String nature) {
		this.nature = nature;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public void addMilestone(String milestoneLabel){
		milestoneList.add(milestoneLabel);
	}

	public List<String> getMilestoneList() {
		return milestoneList;
	}

	public void setMilestoneList(List<String> milestoneList) {
		this.milestoneList = milestoneList;
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
}
