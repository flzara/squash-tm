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
package org.squashtest.tm.domain.customreport

import org.squashtest.tm.domain.chart.ChartDefinition
import org.squashtest.tm.domain.report.ReportDefinition
import spock.lang.Specification

class GetCustomReportTreeDefinitionVisitorTest extends Specification {

	def "Should get CustomReportTreeDefinition of each visited CustomReportTreeEntity"() {
		given:
			def library = new CustomReportLibrary()
			def folder = new CustomReportFolder()
			def report = new ReportDefinition()
			def chart = new ChartDefinition()
			def export = new CustomReportCustomExport()
			def dashboard = new CustomReportDashboard()
		and:
			def visitor = new GetCustomReportTreeDefinitionVisitor()
		when:
			library.accept(visitor)
		then:
			visitor.getCustomReportTreeDefinition() == CustomReportTreeDefinition.LIBRARY
		when:
			folder.accept(visitor)
		then:
			visitor.getCustomReportTreeDefinition() == CustomReportTreeDefinition.FOLDER
		when:
			report.accept(visitor)
		then:
			visitor.getCustomReportTreeDefinition() == CustomReportTreeDefinition.REPORT
		when:
			chart.accept(visitor)
		then:
			visitor.getCustomReportTreeDefinition() == CustomReportTreeDefinition.CHART
		when:
			export.accept(visitor)
		then:
			visitor.getCustomReportTreeDefinition() == CustomReportTreeDefinition.CUSTOM_EXPORT
		when:
			dashboard.accept(visitor)
		then:
			visitor.getCustomReportTreeDefinition() == CustomReportTreeDefinition.DASHBOARD
	}
}

