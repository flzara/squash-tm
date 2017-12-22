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
package org.squashtest.tm.core.foundation.lang;

import org.junit.Test;

import spock.lang.Specification;

/**
 * @author Gregory Fouquet
 *
 */
class DateUtilsTest extends Specification {
	def "format(parse(#date)) should be identity function"() {
		expect:
		DateUtils.formatIso8601Date(DateUtils.parseIso8601Date("2013-01-02")) == "2013-01-02"
	} 
	def "format(parse(#datetime)) should be identity function"() {
		given: 
		TimeZone.setDefault(new SimpleTimeZone(4*3600*1000, "CUSTOM"))
		
		expect:
		DateUtils.formatIso8601DateTime(DateUtils.parseIso8601DateTime("2013-01-02T12:59:20.125+0400")) == "2013-01-02T12:59:20.125+0400"
	} 
}
