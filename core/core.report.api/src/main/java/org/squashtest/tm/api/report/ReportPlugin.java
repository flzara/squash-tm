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
package org.squashtest.tm.api.report;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * This class is used as the entrypoint to the plugin. There should be one {@link ReportPlugin} defined as an OSGi
 * service. Squash TM uses this service to fetch the provided reports.
 * 
 * A report plugin should expose at least one report but it can expose as many as it needs.
 * 
 * @author Gregory Fouquet
 * 
 */
public class ReportPlugin implements InitializingBean {
	private Report[] reports;

	/**
	 * Shortcut for a plugin containing only one report.
	 * 
	 * @param report
	 *            the report to set
	 */
	public void setReport(Report report) {
		reports = new Report[] { report };
	}

	/**
	 * @return the reports
	 */
	public Report[] getReports() {
		return reports;
	}

	/**
	 * @param reports
	 *            the reports to set
	 */
	public void setReports(Report[] reports) {
		Assert.notNull(reports, "Reports array should not be null");
		this.reports = reports;
	}

	/**
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public final void afterPropertiesSet() throws Exception {
		Assert.notNull(reports, "Reports property should not be null");
		Assert.notEmpty(reports, "Reports property should not be empty");
		Assert.noNullElements(reports, "Reports property should not contain null elements");
	}
}
