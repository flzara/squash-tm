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

import org.squashtest.tm.domain.campaign.IterationTestPlanItem;

import java.util.List;


public abstract class ExProgressAbstractDto {
	protected String name;
	protected Integer iCountStatusReady = 0;
	protected Integer iCountStatusRunning = 0;
	protected Integer iCountStatusBloqued = 0;
	protected Integer iCountStatusFailure = 0;
	protected Integer iCountStatusSuccess = 0;
	protected Integer iCountStatusUntestable = 0;
	protected Integer iCountStatusSettled = 0;

	public ExProgressAbstractDto() {
		super();
	}

	public ExProgressAbstractDto(List<IterationTestPlanItem> testPlans) {
		fillStatusInfos(testPlans);
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getiCountStatusReady() {
		return iCountStatusReady;
	}

	public void setiCountStatusReady(Integer iCountStatusReady) {
		this.iCountStatusReady = iCountStatusReady;
	}

	public Integer getiCountStatusRunning() {
		return iCountStatusRunning;
	}

	public void setiCountStatusRunning(Integer iCountStatusRunning) {
		this.iCountStatusRunning = iCountStatusRunning;
	}

	public Integer getiCountStatusBloqued() {
		return iCountStatusBloqued;
	}

	public void setiCountStatusBloqued(Integer iCountStatusBloqued) {
		this.iCountStatusBloqued = iCountStatusBloqued;
	}

	public Integer getiCountStatusFailure() {
		return iCountStatusFailure;
	}

	public void setiCountStatusFailure(Integer iCountStatusFailure) {
		this.iCountStatusFailure = iCountStatusFailure;
	}

	public Integer getiCountStatusSettled() {
		return iCountStatusSettled;
	}

	public void setiCountStatusSettled(Integer iCountStatusSettled) {
		this.iCountStatusSettled = iCountStatusSettled;
	}

	public Integer getiCountStatusSuccess() {
		return iCountStatusSuccess;
	}

	public void setiCountStatusSuccess(Integer iCountStatusSuccess) {
		this.iCountStatusSuccess = iCountStatusSuccess;
	}

	public Integer getiCountStatusUntestable() {
		return iCountStatusUntestable;
	}

	public void setiCountStatusUntestable(Integer iCountStatusUntestable) {
		this.iCountStatusUntestable = iCountStatusUntestable;
	}



	/* ****************************** computed properties **********************************/

	public Integer getNumberTestCase() {
		return iCountStatusReady
			+ iCountStatusRunning
			+ iCountStatusBloqued
			+ iCountStatusFailure
			+ iCountStatusSettled
			+ iCountStatusSuccess
			+ iCountStatusUntestable;
	}


	public float getfPercentageStatusReady() {
		Integer total = getNumberTestCase();
		if (total == 0) {
			return 0;
		} else {
			return (float) getiCountStatusReady() / (float) total;
		}
	}

	public float getfPercentageStatusRunning() {
		Integer total = getNumberTestCase();
		if (total == 0) {
			return 0;
		} else {
			return (float) getiCountStatusRunning() / (float) total;
		}
	}

	public float getfPercentageStatusBloqued() {
		Integer total = getNumberTestCase();
		if (total == 0) {
			return 0;
		} else {
			return (float) getiCountStatusBloqued() / (float) total;
		}
	}

	public float getfPercentageStatusFailure() {
		Integer total = getNumberTestCase();
		if (total == 0) {
			return 0;
		} else {
			return (float) getiCountStatusFailure() / (float) total;
		}
	}

	public float getfPercentageStatusSuccess() {
		Integer total = getNumberTestCase();
		if (total == 0) {
			return 0;
		} else {
			return (float) getiCountStatusSuccess() / (float) total;
		}
	}

	public float getfPercentageStatusUntestable() {
		Integer total = getNumberTestCase();
		if (total == 0) {
			return 0;
		} else {
			return (float) getiCountStatusUntestable() / (float) total;
		}
	}

	public float getfPercentageStatusSettled() {
		Integer total = getNumberTestCase();
		if (total == 0) {
			return 0;
		} else {
			return (float) getiCountStatusSettled() / (float) total;
		}
	}

	public float getfPercentageProgress() {
		return getfPercentageStatusBloqued()
			+ getfPercentageStatusUntestable()
			+ getfPercentageStatusFailure()
			+ getfPercentageStatusSuccess()
			+ getfPercentageStatusSettled();
	}

	private void fillStatusInfos(List<IterationTestPlanItem> testPlan) { // NOSONAR the switch is perfectly readable
		for (IterationTestPlanItem testPlanItem : testPlan) {
			switch (testPlanItem.getExecutionStatus()) {
				case READY:
					iCountStatusReady++;
					break;
				case RUNNING:
					iCountStatusRunning++;
					break;
				case BLOCKED:
					iCountStatusBloqued++;
					break;
				case FAILURE:
					iCountStatusFailure++;
					break;
				case SUCCESS:
					iCountStatusSuccess++;
					break;
				case UNTESTABLE:
					iCountStatusUntestable++;
					break;
				case SETTLED:
					iCountStatusSettled++;
					break;
				default:
					// NOOP
					break;
			}
		}
	}

	public ExProgressCampaignStatus getStatus() {
		if (iCountStatusBloqued + iCountStatusFailure + iCountStatusSuccess + iCountStatusUntestable == getNumberTestCase()) {
			return ExProgressCampaignStatus.CAMPAIGN_OVER;
		}
		return ExProgressCampaignStatus.CAMPAIGN_RUNNING;

	}

	public void fillStatusInfosWithChildren(List<? extends ExProgressAbstractDto> containedExProgressDto) {
		for (ExProgressAbstractDto dto : containedExProgressDto) {
			this.iCountStatusBloqued += dto.getiCountStatusBloqued();
			this.iCountStatusFailure += dto.getiCountStatusFailure();
			this.iCountStatusReady += dto.getiCountStatusReady();
			this.iCountStatusRunning += dto.getiCountStatusRunning();
			this.iCountStatusSuccess += dto.getiCountStatusSuccess();
			this.iCountStatusUntestable += dto.getiCountStatusUntestable();
			this.iCountStatusSettled += dto.getiCountStatusSettled();
		}
	}


}
