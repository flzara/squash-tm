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
package org.squashtest.tm.web.internal.controller.search;

import java.math.BigInteger;
import java.util.Map;

import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.search.IndexMonitor;
import org.squashtest.tm.domain.testcase.TestCase;


public class IndexingProgressModel {


	private IndexMonitor tcMon;
	private IndexMonitor reqVMon;
	private IndexMonitor itpiMon;
	private IndexMonitor total;

	public IndexingProgressModel() {
		Map<Class<?>, IndexMonitor> allMonitor = IndexMonitor.monitors;
		tcMon = allMonitor.get(TestCase.class);
		reqVMon = allMonitor.get(RequirementVersion.class);
		itpiMon = allMonitor.get(IterationTestPlanItem.class);
		total = IndexMonitor.total;
	}

	public BigInteger getProgressPercentage() {
		return total.getPercentComplete();
	}

	public BigInteger getWrittenEntities() {
		return total.getDocumentsBuilt();
	}

	public BigInteger getTotalEntities() {
		return total.getTotalCount();
	}

	public BigInteger getProgressPercentageForTestcases() {
		return tcMon.getPercentComplete();
	}

	public BigInteger getWrittenEntitiesForTestcases() {
		return tcMon.getDocumentsBuilt();
	}

	public BigInteger getTotalEntitiesForTestcases() {
		return tcMon.getTotalCount();
	}

	public BigInteger getProgressPercentageForRequirementVersions() {
		return reqVMon.getPercentComplete();
	}

	public BigInteger getWrittenEntitiesForRequirementVersions() {
		return reqVMon.getDocumentsBuilt();
	}

	public BigInteger getTotalEntitiesForRequirementVersions() {
		return reqVMon.getTotalCount();
	}


	public BigInteger getProgressPercentageForCampaigns() {
		return itpiMon.getPercentComplete();
	}


	public BigInteger getWrittenEntitiesForCampaigns() {
		return itpiMon.getDocumentsBuilt();
	}


	public BigInteger getTotalEntitiesForCampaigns() {
		return itpiMon.getTotalCount();
	}



}
