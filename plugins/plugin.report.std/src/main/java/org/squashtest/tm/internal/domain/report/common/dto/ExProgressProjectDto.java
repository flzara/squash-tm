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

public class ExProgressProjectDto extends ExProgressAbstractDto implements HasMilestoneLabel {

	/*
	 * The milestone label is essentially a Report level parameter, not an attribute of the project. But still we have to store that
	 * information somewhere, and by default this DTO is the best place so far.
	 *
	 */
	private String milestone;

	private Long id;

	private boolean allowsUntestable;

	private boolean allowsSettled;

	private List<ExProgressCampaignDto> campaigns = new LinkedList<>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public List<ExProgressCampaignDto> getCampaigns() {
		return campaigns;
	}

	public void setCampaigns(List<ExProgressCampaignDto> campaigns) {
		this.campaigns = campaigns;
	}

	public void addCampaignDto(ExProgressCampaignDto campaignDto) {
		this.campaigns.add(campaignDto);
	}

	public boolean isAllowsSettled() {
		return allowsSettled;
	}

	public void setAllowsSettled(boolean allowsSettled) {
		this.allowsSettled = allowsSettled;
	}


	public boolean isAllowsUntestable() {
		return allowsUntestable;
	}

	public void setAllowsUntestable(boolean allowsUntestable) {
		this.allowsUntestable = allowsUntestable;
	}

	@Override
	public String getMilestone() {
		return milestone;
	}

	public void setMilestone(String milestone) {
		this.milestone = milestone;
	}

}
