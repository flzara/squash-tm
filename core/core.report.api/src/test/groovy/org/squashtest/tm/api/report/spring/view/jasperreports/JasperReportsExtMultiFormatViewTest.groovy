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
package org.squashtest.tm.api.report.spring.view.jasperreports

import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Gregory Fouquet
 *
 */
class JasperReportsExtMultiFormatViewTest extends Specification {
	def "should load content disposition mappings properties file"() {
		when:
		new JasperReportsExtMultiFormatView()

		then:
		notThrown IllegalArgumentException
	}

	@Unroll
	def "[Issue 3927] should replace #placeholder with date"() {
		given :
		JasperReportsExtMultiFormatView view = new JasperReportsExtMultiFormatView();
		view.setReportFileName("whatever-${placeholder}")

		when:
		def name = view.addTimestampToFilename()['pdf']

		then:
		name ==~ /.*\"whatever-\d{6}\.pdf\"/;

		where:
		placeholder << ['${date:yyyyMM}', '{{date:yyyyMM}}']
	}

}
