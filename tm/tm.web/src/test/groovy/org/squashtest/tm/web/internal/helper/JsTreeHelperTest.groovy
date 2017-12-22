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
package org.squashtest.tm.web.internal.helper

import org.apache.commons.collections.MultiMap;

import spock.lang.Specification

/**
 * @author Gregory
 *
 */
class JsTreeHelperTest extends Specification {
	def "should map openend nodes by type"() {
		when:
		MultiMap res = JsTreeHelper.mapIdsByType(["#TestCaseLibrary-10", "#TestCaseFolder-110", "#TestCaseFolder-120", "#Whatever-210"] as String[])
		
		then:
		res["TestCaseLibrary"] == [10]
		res["TestCaseFolder"] == [110, 120]
		res["Whatever"] == [210]
	}
	def "should map no nodes by type"() {
		when:
		MultiMap res = JsTreeHelper.mapIdsByType([] as String[])
		
		then:
		res.size() == 0
	}
}
