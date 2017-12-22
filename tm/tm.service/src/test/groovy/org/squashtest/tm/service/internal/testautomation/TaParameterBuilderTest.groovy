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
package org.squashtest.tm.service.internal.testautomation;

import java.util.Map;

import org.junit.Test;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.customfield.CustomFieldBinding;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.internal.testautomation.TaParametersBuilder;

import spock.lang.Specification;

/**
 * @author Gregory Fouquet
 *
 */
class TaParameterBuilderTest extends Specification {
	TaParametersBuilder builder = new TaParametersBuilder()

	def "should populate params from test case"() {
		given:
		TestCase tc = Mock()
		tc.reference >> "Farewell, sweet Concorde!"

		when:
		Map params = builder.testCase().addEntity(tc).build()

		then:
		params["TC_REFERENCE"] == "Farewell, sweet Concorde!"
		params.size() == 1
	}

	def "should not populate null params from test case"() {
		given:
		TestCase tc = Mock()
		tc.reference >> null

		when:
		Map params = builder.testCase().addEntity(tc).build()

		then:
		params.size() == 0
	}

	def "should populate params from iteration"() {
		given:
		Iteration iter = Mock()

		when:
		Map params = builder.iteration().addEntity(iter).build()

		then:
		params.size() == 0
	}

	def "should populate params from campaign"() {
		given:
		Campaign camp = Mock()

		when:
		Map params = builder.campaign().addEntity(camp).build()

		then:
		params.size() == 0
	}

	def "should populate custom fields from #type"() {
		given:
		List fields = []

		2.times {
			fields << mockField("code" + it, "val" + it)
		}

		fields << mockField("codeNull", null)


		when:
		Map params = builder."$type"().addCustomFields(fields).build()

		then:
		params.size() == 2
		params[prefix + "CUF_code0"] == "val0"
		params[prefix + "CUF_code1"] == "val1"

		where:
		type		| prefix
		"testCase"	| "TC_"
		"iteration"	| "IT_"
		"campaign"	| "CPG_"
	}

	private CustomFieldValue mockField(code, value) {
		CustomFieldValue val = Mock()
		val.value>> value

		CustomFieldBinding bind = Mock()
		val.binding >> bind

		CustomField field = Mock()
		field.code >> code
		bind.customField >> field
		return val
	}

}
