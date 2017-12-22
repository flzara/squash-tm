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
package org.squashtest.tm.web.internal.helper;

import org.apache.commons.lang3.StringUtils;
import org.squashtest.tm.web.internal.helper.JsonHelper

import com.fasterxml.jackson.databind.ObjectMapper;

import spock.lang.Specification;


class JsonHelperTest extends Specification {
	def setup() {
		new JsonHelper(new ObjectMapper())
	}
	
	def "should serialize a Dummy"() {
		when:
		def res = JsonHelper.serialize(new Dummy(foo: "foofoo", bar: "barbar"))

		then:
		StringUtils.remove(res, " ") == '{"foo":"foofoo","bar":"barbar"}'
	}
	def "should serialize a list of Dummy"() {
		given:
		def val = [new Dummy(foo: "f", bar: "b"), new Dummy(foo: "ff", bar: "bb")]

		when:
		def res = JsonHelper.serialize(val)

		then:
		StringUtils.remove(res, " ") == '[{"foo":"f","bar":"b"},{"foo":"ff","bar":"bb"}]'
	}
	def "should serialize a map"() {
		given:
		def val = ["foo" : "bar"]

		when:
		def res = JsonHelper.serialize(val)

		then:
		StringUtils.remove(res, " ") == '{"foo":"bar"}'
	}

	def "should unmarshall complex data"() {
		given:
		def json = """{"campaignStatus":[{"value":"CAMPAIGN_ALL","selected":true,"type":"DROPDOWN_LIST"},{"value":"CAMPAIGN_RUNNING","selected":false,"type":"DROPDOWN_LIST"},{"value":"CAMPAIGN_OVER","selected":false,"type":"DROPDOWN_LIST"}],"scheduledStart":{"value":"--","type":"DATE"},"actualStart":{"value":"--","type":"DATE"},"scheduledEnd":{"value":"--","type":"DATE"},"actualEnd":{"value":"--","type":"DATE"}}"""

		when:
		def res = JsonHelper.deserialize(json)

		then:
		res.campaignStatus[0].value == "CAMPAIGN_ALL"
	}


	def "should marshall null values"() {
		expect:
		JsonHelper.marshall([name: "foo", contents: null]) == """{"name":"foo","contents":null}"""
	}
	def "should unmarshall null values"() {
		when:
		def res = JsonHelper.unmarshall("""{"name":"foo","contents":null}""")

		then:
		res.keySet() == ["name", "contents"] as Set
		res.contents == null
	}
}

class Dummy {
	String foo;
	String bar;
}
