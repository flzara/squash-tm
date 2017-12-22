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
package org.squashtest.tm.service.statistics.testcase;

import java.util.Collection;

public class TestCaseStatisticsBundle {

	private TestCaseBoundRequirementsStatistics boundRequirementsStatistics;
	private TestCaseImportanceStatistics importanceStatistics;
	private TestCaseStatusesStatistics statusesStatistics;
	private TestCaseSizeStatistics sizeStatistics;
	private Collection<Long> selectedIds;
	
	
	public Collection<Long> getSelectedIds() {
		return selectedIds;
	}

	public void setSelectedIds(Collection<Long> selectedIds) {
		this.selectedIds = selectedIds;
	}

	public TestCaseBoundRequirementsStatistics getBoundRequirementsStatistics() {
		return boundRequirementsStatistics;
	}

	public void setBoundRequirementsStatistics(
			TestCaseBoundRequirementsStatistics boundRequirementsStatistics) {
		this.boundRequirementsStatistics = boundRequirementsStatistics;
	}

	public TestCaseImportanceStatistics getImportanceStatistics() {
		return importanceStatistics;
	}

	public void setImportanceStatistics(
			TestCaseImportanceStatistics importanceStatistics) {
		this.importanceStatistics = importanceStatistics;
	}

	public TestCaseStatusesStatistics getStatusesStatistics() {
		return statusesStatistics;
	}

	public void setStatusesStatistics(TestCaseStatusesStatistics statusesStatistics) {
		this.statusesStatistics = statusesStatistics;
	}

	public TestCaseSizeStatistics getSizeStatistics() {
		return sizeStatistics;
	}

	public void setSizeStatistics(TestCaseSizeStatistics sizeStatistics) {
		this.sizeStatistics = sizeStatistics;
	}

	public TestCaseStatisticsBundle(
			TestCaseBoundRequirementsStatistics boundRequirementsStatistics,
			TestCaseImportanceStatistics importanceStatistics,
			TestCaseStatusesStatistics statusesStatistics,
			TestCaseSizeStatistics sizeStatistics, Collection<Long> selectedIds) {
		super();
		this.boundRequirementsStatistics = boundRequirementsStatistics;
		this.importanceStatistics = importanceStatistics;
		this.statusesStatistics = statusesStatistics;
		this.sizeStatistics = sizeStatistics;
		this.selectedIds = selectedIds;
	}
	
	public TestCaseStatisticsBundle() {
		super();
	}
	
		
}
