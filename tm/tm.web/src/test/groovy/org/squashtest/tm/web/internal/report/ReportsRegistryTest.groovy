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
package org.squashtest.tm.web.internal.report



import org.squashtest.tm.api.report.BasicReport;
import org.squashtest.tm.api.report.ReportPlugin;
import org.squashtest.tm.api.report.StandardReportCategory;
import org.squashtest.tm.web.internal.report.ReportsRegistry

import spock.lang.Ignore;
import spock.lang.Specification;

import static org.squashtest.tm.api.report.StandardReportCategory.*

/**
 * @author Gregory Fouquet
 *
 */
class ReportsRegistryTest extends Specification {
	ReportsRegistry registry = new ReportsRegistry()

	def "should register category"() {
		given:
		BasicReport report = new BasicReport()
		report.category = StandardReportCategory.PREPARATION_PHASE

		ReportPlugin plugin = new ReportPlugin()
		plugin.report = report

		when:
		registerReports([plugin])

		then:
		registry.categories == new HashSet([PREPARATION_PHASE])
	}

	def "should register report"() {
		given:
		BasicReport report = new BasicReport()
		report.labelKey = 'foo'
		report.category = StandardReportCategory.PREPARATION_PHASE

		ReportPlugin plugin = new ReportPlugin()
		plugin.report = report

		when:
		registerReports([plugin])

		then:
		registry.findReports(PREPARATION_PHASE)*.labelKey == ['foo']
	}

	def "should register reports"() {
		given:
		BasicReport report = new BasicReport()
		report.labelKey = 'foo'
		report.category = StandardReportCategory.PREPARATION_PHASE

		and:
		BasicReport otherReport = new BasicReport()
		otherReport.labelKey = 'foofoo'
		otherReport.category = StandardReportCategory.PREPARATION_PHASE

		and:
		ReportPlugin plugin = new ReportPlugin()
		plugin.reports = [report, otherReport]

		when:
		registerReports([plugin])

		then:
		registry.findReports(PREPARATION_PHASE)*.labelKey == ['foo', 'foofoo']
	}

	def registerReports(plugins) {
		registry.plugins = plugins
		registry.registerReports()
	}


	def "should decorate reports with identifier"() {
		given:
		BasicReport report = new BasicReport()
		report.labelKey = 'foo'
		report.category = StandardReportCategory.PREPARATION_PHASE

		ReportPlugin plugin = new ReportPlugin()
		plugin.report = report

		when:
		registerReports([plugin])

		then:
		registry.findReports(PREPARATION_PHASE)*.namespace != null
	}

	@Ignore("Report name is no longer predictible, test should be rewritten or report name made predictible again")
	def "should find report from its namespaxce"() {
		given:
		BasicReport report = new BasicReport()
		report.labelKey = 'foo'
		report.category = StandardReportCategory.PREPARATION_PHASE

		and:
		ReportPlugin plugin = new ReportPlugin()
		plugin.report = report

		and:
		registerReports([plugin])

		when:
		def found = registry.findReport('bar')

		then:
		found.labelKey == 'foo'
	}
}
