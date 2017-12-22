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

import static org.junit.Assert.*;

import org.springframework.context.MessageSource;
import org.squashtest.tm.api.report.jasperreports.JasperReportsView;
import org.squashtest.tm.api.report.query.ReportQuery;

import net.sf.jasperreports.engine.JRParameter;
import spock.lang.Specification;

class MultiFormatViewTest extends Specification {
	JasperReportsView view = new JasperReportsView();
	ReportQuery query = Mock()
	MessageSource messageSource = Mock()
	
	def setup() {
		view.query = query
		view.messageSource = messageSource
		
	}
	
	def "should add properties bundle and format to model"() {
		when: 
		def res = view.buildViewModel("foo", Collections.emptyMap())
		
		then:
		res.format == "foo"
		res[JRParameter.REPORT_RESOURCE_BUNDLE] != null
	}

	def "should invoke report query"() {
		when: 
		def res = view.buildViewModel("foo", Collections.emptyMap())
		
		then:
		1 * query.executeQuery(Collections.emptyMap(), Collections.emptyMap())
	}
}
