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

import org.squashtest.csp.core.bugtracker.domain.BTIssue;
import org.squashtest.tm.bugtracker.advanceddomain.AdvancedIssue;

import com.fasterxml.jackson.databind.ObjectMapper;

import spock.lang.Specification;

/**
 * @author Gregory Fouquet
 *
 */
class ObjectMapperTest extends Specification {
	ObjectMapper mapper = new ObjectMapper()
	def "should marshall linked map in entries order"() {
		given:
		LinkedHashMap map = new LinkedHashMap();
		map.put 2, "foo"
		map.put 1, "bar"

		when:
		def res = mapper.writeValueAsString(map);

		then:
		res == '{"2":"foo","1":"bar"}'
	}
	def "should marshall linked map in entries order, take 2"() {
		given:
		LinkedHashMap map = new LinkedHashMap();
		map.put 1, "foo"
		map.put 2, "bar"

		when:
		def res = mapper.writeValueAsString(map);

		then:
		res == '{"1":"foo","2":"bar"}'
	}



	def "should read an issue"(){

		given :
		def json = '{"category":{"id":"10000","name":"CORE"},"project":{"priorities":[{"dummy":false,"name":"Blocker","id":"1"},{"dummy":false,"name":"Critical","id":"2"},{"dummy":false,"name":"Major","id":"3"},{"dummy":false,"name":"Minor","id":"4"},{"dummy":false,"name":"Trivial","id":"5"}],"categories":[{"dummy":false,"name":"CORE","id":"10000"},{"dummy":false,"name":"UI","id":"10001"}],"versions":[{"dummy":false,"name":"1.0","id":"10000"},{"dummy":false,"name":"1.1","id":"10001"}],"users":[{"dummy":true,"name":"--","permissions":[],"id":"----"}],"dummy":false,"name":"mon projet","id":"MPROJ"},"summary":"damnit","createdOn":null,"reporter":null,"assignee":{"id":"----","name":"-- non assignable --"},"bugtracker":null,"id":"","priority":{"id":"1","name":"Blocker"},"version":{"id":"10000","name":"1.0"},"description":"no luck","comment":"","status":null}'

		when :
		def res = mapper.readValue(json, BTIssue.class)

		then :
		res.description=="no luck"
		res.version.id == "10000"
		res.summary=="damnit"
	}
}
