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

import java.util.*;

public class CampaignDto {
	private Long id;
	private Date scheduledStartDate;
	private Date scheduledEndDate;
	private Date actualStartDate;
	private Date actualEndDate;
	private Map<Long, IterationDto> iterationMap = new HashMap<>();
	private List<IterationDto> iterationList = new ArrayList<>();

	public CampaignDto(Long id, Date scheduledStartDate, Date scheduledEndDate, Date actualStartDate, Date actualEndDate) {
		super();
		this.id = id;
		this.scheduledStartDate = scheduledStartDate;
		this.scheduledEndDate = scheduledEndDate;
		this.actualStartDate = actualStartDate;
		this.actualEndDate = actualEndDate;
	}

	public CampaignDto(){
		super();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public Map<Long, IterationDto> getIterationMap() {
		return iterationMap;
	}

	public void setIterationMap(Map<Long, IterationDto> iterationMap) {
		this.iterationMap = iterationMap;
	}

	public IterationDto getIteration(Long iterationId){
		return iterationMap.get(iterationId);
	}

	public void addIteration(IterationDto iteration){
		iterationMap.put(iteration.getId(), iteration);
		iterationList.add(iteration);
	}

	public List<IterationDto> getIterationList(){
		return iterationList;
	}
}
