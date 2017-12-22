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

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.TestSuite;

public class ExProgressIterationDto extends ExProgressScheduledAbstractDto {

	private ExProgressCampaignDto campaign;

	private List<ExProgressTestPlanDto> testPlans = new LinkedList<>();
	private List<ExProgressTestSuiteDto> testSuites = new LinkedList<>();

	public ExProgressCampaignDto getCampaign() {
		return campaign;
	}

	public void setCampaign(ExProgressCampaignDto campaign) {
		this.campaign = campaign;
	}

	public ExProgressIterationDto(Iteration iteration) {
		super(iteration.getTestPlans());
		fillBasicInfos(iteration);
		fillTestSuiteInfos(iteration);
	}

	private ExProgressIterationDto fillBasicInfos(Iteration iteration) {
		super.name = iteration.getName();
		scheduledStartDate = iteration.getScheduledStartDate();
		scheduledEndDate = iteration.getScheduledEndDate();
		actualStartDate = iteration.getActualStartDate();
		actualEndDate = iteration.getActualEndDate();

		return this;
	}

	private void fillTestSuiteInfos(Iteration iteration) {
		for (TestSuite testSuite : iteration.getTestSuites()) {
			ExProgressTestSuiteDto testSuiteDto = new ExProgressTestSuiteDto(testSuite);
			testSuiteDto.setIteration(this);
			testSuites.add(testSuiteDto);
			Collections.sort(testSuites, new testSuiteComparator());
		}
	}

	private static class testSuiteComparator implements Comparator<ExProgressTestSuiteDto> {
		@Override
		public int compare(ExProgressTestSuiteDto suite1, ExProgressTestSuiteDto suite2) {
			return suite1.getName().compareTo(suite2.getName());
		}
	}

	public List<ExProgressTestPlanDto> getTestPlans() {
		return testPlans;
	}

	public void setTestPlans(List<ExProgressTestPlanDto> testPlans) {
		this.testPlans = testPlans;
	}

	public void addTestPlanDto(ExProgressTestPlanDto testPlanDto) {
		testPlans.add(testPlanDto);
	}

	public List<ExProgressTestSuiteDto> getTestSuites() {
		return testSuites;
	}

	public void setTestSuites(List<ExProgressTestSuiteDto> testSuites) {
		this.testSuites = testSuites;
	}

	public boolean isAllowsSettled() {
		return this.getCampaign().getProject().isAllowsSettled();
	}

	public boolean isAllowsUntestable() {
		return this.getCampaign().getProject().isAllowsUntestable();
	}

}
