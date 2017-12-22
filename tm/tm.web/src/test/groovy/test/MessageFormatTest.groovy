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
package test

import java.text.MessageFormat;

import spock.lang.Specification;

/**
 * @author Gregory
 *
 */
class MessageFormatTest extends Specification {
	def "should format with thousand separator"() {
		expect: 
		"/foo/10000/bar/20000" != MessageFormat.format("/foo/{0}/bar/{1}", 10000L, 20000L);
	}
	def "should format numbers according to pattern"() {
		expect: 
		"/foo/10000/bar/20000" == MessageFormat.format("/foo/{0,number,#########}/bar/{1,number,#########}", 10000L, 20000L);
	}
	def "should format numbers according to short pattern"() {
		expect: 
		"/foo/2000000000/" == MessageFormat.format("/foo/{0,number,####}/", 2000000000L);
	}
}
