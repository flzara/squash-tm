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
package org.squashtest.tm.domain.library;

import java.util.Date;

public class IndexModel {

	private Date requirementIndexDate;
	private Date testCaseIndexDate;
	private Date campaignIndexDate;
	private String requirementIndexVersion;
	private String testcaseIndexVersion;
	private String campaignIndexVersion;
	private String currentSquashVersion;
	
	public IndexModel(){
		
	}

	public Date getRequirementIndexDate() {
		return requirementIndexDate;
	}

	public void setRequirementIndexDate(Date requirementIndexDate) {
		this.requirementIndexDate = requirementIndexDate;
	}

	public Date getTestCaseIndexDate() {
		return testCaseIndexDate;
	}

	public void setTestCaseIndexDate(Date testCaseIndexDate) {
		this.testCaseIndexDate = testCaseIndexDate;
	}

	public Date getCampaignIndexDate() {
		return campaignIndexDate;
	}

	public void setCampaignIndexDate(Date campaignIndexDate) {
		this.campaignIndexDate = campaignIndexDate;
	}

	public String getRequirementIndexVersion() {
		return requirementIndexVersion;
	}

	public void setRequirementIndexVersion(String requirementIndexVersion) {
		this.requirementIndexVersion = requirementIndexVersion;
	}

	public String getTestcaseIndexVersion() {
		return testcaseIndexVersion;
	}

	public void setTestcaseIndexVersion(String testcaseIndexVersion) {
		this.testcaseIndexVersion = testcaseIndexVersion;
	}

	public String getCampaignIndexVersion() {
		return campaignIndexVersion;
	}

	public void setCampaignIndexVersion(String campaignIndexVersion) {
		this.campaignIndexVersion = campaignIndexVersion;
	}

	public String getCurrentSquashVersion() {
		return currentSquashVersion;
	}

	public void setCurrentSquashVersion(String currentSquashVersion) {
		this.currentSquashVersion = currentSquashVersion;
	}
	
}
