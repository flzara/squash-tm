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
package org.squashtest.tm.internal.domain.report.common.dto;

import java.util.LinkedList;
import java.util.List;

import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.internal.domain.report.common.dto.ExProgressIterationDto;
import org.squashtest.tm.internal.domain.report.common.dto.ExProgressProjectDto;

public class ExProgressCampaignDto extends ExProgressScheduledAbstractDto {

	private ExProgressProjectDto project;

	private List<ExProgressIterationDto> iterations = new LinkedList<>();

	public ExProgressCampaignDto() {
		super();
	}

	public ExProgressCampaignDto(Campaign campaign) {
		doFillBasicInfos(campaign);
		doFillIterationsInfos(campaign);
	}

	public ExProgressProjectDto getProject() {
		return project;
	}

	public void setProject(ExProgressProjectDto project) {
		this.project = project;
	}

	public List<ExProgressIterationDto> getIterations() {
		return iterations;
	}

	public void setIterations(List<ExProgressIterationDto> iterations) {
		this.iterations = iterations;
	}

	public void addIterationDto(ExProgressIterationDto iterDto) {
		iterations.add(iterDto);
	}



	public ExProgressCampaignDto fillBasicInfos(Campaign campaign) {
		return doFillBasicInfos(campaign);
	}
	private ExProgressCampaignDto doFillBasicInfos(Campaign campaign){
		this.name = campaign.getName();
		this.scheduledStartDate = campaign.getScheduledStartDate();
		this.scheduledEndDate = campaign.getScheduledEndDate();
		this.actualStartDate = campaign.getActualStartDate();
		this.actualEndDate = campaign.getActualEndDate();

		return this;
	}

	public ExProgressCampaignDto fillIterationsInfos(Campaign campaign) {
		return doFillIterationsInfos(campaign);
	}
	private ExProgressCampaignDto doFillIterationsInfos(Campaign campaign) {
		for (Iteration iteration : campaign.getIterations()) {
			ExProgressIterationDto iterDto = new ExProgressIterationDto(iteration);
			iterations.add(iterDto);
		}
		return this;
	}

	public boolean isAllowsSettled() {
		return this.getProject().isAllowsSettled();
	}

	public boolean isAllowsUntestable() {
		return this.getProject().isAllowsUntestable();
	}
	public String getMilestone(){
		return project.getMilestone();
	}
}
