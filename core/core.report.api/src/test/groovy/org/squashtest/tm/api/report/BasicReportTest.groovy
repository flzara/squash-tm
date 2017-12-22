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
package org.squashtest.tm.api.report

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.squashtest.tm.api.report.criteria.Criteria;

import spock.lang.Specification;

/**
 * @author Gregory Fouquet
 *
 */
class BasicReportTest extends Specification {
	def "should build given view"() {
		given:
		BasicReport report = new BasicReport();
		
		and:
		ReportView view = Mock()
		report.views = [view]
		
		and:
		Criteria crit = Mock()
		
		when:
		report.buildModelAndView 0, "pdf", [foo: crit]
		
		then:
		1 * view.buildViewModel("pdf", [foo: crit])
	}
	def "should return model and view"() {
		given:
		BasicReport report = new BasicReport();
		
		and:
		ReportView view = Mock()
		report.views = [view]

		and:
		View viewBean = Mock()
		view.springView >> viewBean
		
		when:
		ModelAndView res = report.buildModelAndView(0, "pdf", Collections.emptyMap())
		
		then:
		res.view == viewBean
	}
}
