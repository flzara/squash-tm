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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class IterationDto {
	private Long id = -1L;
	private String name="";
	private Date scheduledStartDate;
	private Date scheduledEndDate;
	private Date actualStartDate;
	private Date actualEndDate;
	private Set<String> milestoneSet = new HashSet<>();
	private Map<Long, ITPIDto> testPlans = new HashMap<>();
	private List<ITPIDto> testPlanList = new ArrayList<>();

	public IterationDto(Long id, String name, Date scheduledStartDate, Date scheduledEndDate, Date actualStartDate, Date actualEndDate) {
		super();
		this.id = id;
		this.name = name;
		this.scheduledStartDate = scheduledStartDate;
		this.scheduledEndDate = scheduledEndDate;
		this.actualStartDate = actualStartDate;
		this.actualEndDate = actualEndDate;
	}

	public IterationDto(){
		super();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getScheduledStartDate() {
		return scheduledStartDate;
	}

	public void setScheduledStartDate(Date scheduledStartDate) {
		this.scheduledStartDate = scheduledStartDate;
	}

	public Date getScheduledEndDate() {
		return scheduledEndDate;
	}

	public void setScheduledEndDate(Date scheduledEndDate) {
		this.scheduledEndDate = scheduledEndDate;
	}

	public Date getActualStartDate() {
		return actualStartDate;
	}

	public void setActualStartDate(Date actualStartDate) {
		this.actualStartDate = actualStartDate;
	}

	public Date getActualEndDate() {
		return actualEndDate;
	}

	public void setActualEndDate(Date actualEndDate) {
		this.actualEndDate = actualEndDate;
	}

	public Map<Long, ITPIDto> getTestPlans() {
		return testPlans;
	}

	public void setTestPlans(Map<Long, ITPIDto> testPlans) {
		this.testPlans = testPlans;
	}

	public ITPIDto getTestPlan(Long itpiId){
		return testPlans.get(itpiId);
	}

	public void addTestPlan(ITPIDto itpi){
		testPlans.put(itpi.getId(), itpi);
		testPlanList.add(itpi);
	}

	public Set<String> getMilestoneSet() {
		return milestoneSet;
	}

	public void setMilestoneSet(Set<String> milestoneSet) {
		this.milestoneSet = milestoneSet;
	}

	public void addMilestone(String milestoneLabel){
		milestoneSet.add(milestoneLabel);
	}

	public List<ITPIDto> getTestPlanList(){
		return testPlanList;
	}

}
