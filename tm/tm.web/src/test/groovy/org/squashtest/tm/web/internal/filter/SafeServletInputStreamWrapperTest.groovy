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
package org.squashtest.tm.web.internal.filter

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import spock.lang.Specification
import spock.lang.Unroll

class SafeServletInputStreamWrapperTest extends Specification{

	@Unroll
	def "should clean basic json strings"() {
		given :
		ObjectMapper mapper = new ObjectMapper();
		JsonNode node = mapper.readTree(input);

		when :
		HtmlSanitizationFilter.SafeServletInputStreamWrapper.sanitizeJsonNode(node)

		then :
		node.toString() == output

		where :
		input 																									|| output
		"""{"string":"toto"}"""																					||"""{"string":"toto"}"""
		"""{"string":"to    to"}"""																				||"""{"string":"to    to"}"""
		"""{"string":"a   bb  to"}"""																			||"""{"string":"a   bb  to"}"""
		"""{"string":"é   &&  àà"}"""																			||"""{"string":"é   &&  àà"}"""
		"""{"string":"é&àç^_è-|{#?,:!abcedef§"}"""																||"""{"string":"é&àç^_è-|{#?,:!abcedef§"}"""
		"""{"string":"toto<script>alert(1)</script>"}"""														||"""{"string":"toto"}"""
		"""{"key1":"toto<script>alert(1)</script>","key2":"tutu<script>alert(1)</script>"}"""					||"""{"key1":"toto","key2":"tutu"}"""
	}

	@Unroll
	def "should clean json objects"() {
		given :
		ObjectMapper mapper = new ObjectMapper();
		JsonNode node = mapper.readTree(input);

		when :
		HtmlSanitizationFilter.SafeServletInputStreamWrapper.sanitizeJsonNode(node)

		then :
		node.toString() == output

		where :
		input 																															|| output
		"""{}"""																														||"""{}"""
		"""{"obj1":{"string":"toto"}}"""																								||"""{"obj1":{"string":"toto"}}"""
		"""{"obj1":{"string":"toto<script>alert(1)</script>"}}"""																		||"""{"obj1":{"string":"toto"}}"""
		"""{"obj1":{"string":"toto<script>alert(1)</script>"},"obj2":{"string":"toto<script>alert(1)</script>"}}"""						||"""{"obj1":{"string":"toto"},"obj2":{"string":"toto"}}"""
		"""{"obj1":{"subobj":{"string":"toto<script>alert(1)</script>"}},"obj2":{"string":"toto<script>alert(1)</script>"}}"""			||"""{"obj1":{"subobj":{"string":"toto"}},"obj2":{"string":"toto"}}"""
		"""{"obj1":{"subobj":{"string":"toto"}},"obj2":{"string":"toto"}}"""															||"""{"obj1":{"subobj":{"string":"toto"}},"obj2":{"string":"toto"}}"""
	}

	@Unroll
	def "should clean json arrays"() {
		given :
		ObjectMapper mapper = new ObjectMapper();
		JsonNode node = mapper.readTree(input);

		when :
		HtmlSanitizationFilter.SafeServletInputStreamWrapper.sanitizeJsonNode(node)

		then :
		node.toString() == output

		where :
		input 																															|| output
		"""[]"""																														||"""[]"""
		"""{"obj":[]}"""																												||"""{"obj":[]}"""
		"""{"obj":["string","toto"]}"""																									||"""{"obj":["string","toto"]}"""
		"""{"obj":["string","toto<script>alert(1)</script>"]}"""																		||"""{"obj":["string","toto"]}"""
		"""{"obj":["string","toto<script>alert(1)</script>","string","titi<script>alert(1)</script>"]}"""								||"""{"obj":["string","toto","string","titi"]}"""
		"""["string","toto<script>alert(1)</script>","string","titi<script>alert(1)</script>"]"""										||"""["string","toto","string","titi"]"""
		"""["string",{"obj1":{"key":"toto<script>alert(1)</script>"}},"string","titi<script>alert(1)</script>"]"""						||"""["string",{"obj1":{"key":"toto"}},"string","titi"]"""
	}


	@Unroll
	def "should clean json mixed objects"() {
		given :
		ObjectMapper mapper = new ObjectMapper();
		JsonNode node = mapper.readTree(input);

		when :
		HtmlSanitizationFilter.SafeServletInputStreamWrapper.sanitizeJsonNode(node)

		then :
		node.toString() == output

		where :
		input 																																												|| output
		"""{"obj":["string","toto"],"str":"toto<script>alert(1)</script>","array":[{"sobj1":{"str":"titi<script>alert(1)</script>"}},{"sobj1":{"str":"tutu<script>alert(1)</script>"}}]}"""	||"""{"obj":["string","toto"],"str":"toto","array":[{"sobj1":{"str":"titi"}},{"sobj1":{"str":"tutu"}}]}"""
	}


}
