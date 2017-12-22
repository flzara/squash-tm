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
package org.squashtest.tm.service.statistics.campaign;

public class CampaignNonExecutedTestCaseImportanceStatistics {

	private int percentageVeryHigh;
	private int percentageHigh;
	private int percentageMedium;
	private int percentageLow;
	
	public int getPercentageVeryHigh() {
		return percentageVeryHigh;
	}
	public void setPercentageVeryHigh(int percentageVeryHigh) {
		this.percentageVeryHigh = percentageVeryHigh;
	}
	public int getPercentageHigh() {
		return percentageHigh;
	}
	public void setPercentageHigh(int percentageHigh) {
		this.percentageHigh = percentageHigh;
	}
	public int getPercentageMedium() {
		return percentageMedium;
	}
	public void setPercentageMedium(int percentageMedium) {
		this.percentageMedium = percentageMedium;
	}
	public int getPercentageLow() {
		return percentageLow;
	}
	public void setPercentageLow(int percentageLow) {
		this.percentageLow = percentageLow;
	}
}
