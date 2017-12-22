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
package org.squashtest.tm.web.internal.controller.campaign;

import static org.junit.Assert.*;

import org.squashtest.tm.domain.campaign.TestSuite;

import spock.lang.Specification;
import spock.lang.Unroll;

/**
 * @author Gregory Fouquet
 *
 */
class TestSuiteHelperTest extends Specification {
	@Unroll
	def "[#name1, #name2] should produce name list #result"() {
		given:
		List suites = []

		TestSuite ts = Mock()
		ts.getName() >> name1
		suites << ts
		
		ts = Mock()
		ts.getName() >> name2
		suites << ts
		
		expect:
		result == TestSuiteHelper.buildEllipsedSuiteNameList(suites, 20);
		
		where:
		name1 | name2 | result
		"1234567890" | "345678" | "1234567890, 345678"
		"1234567890" | "34567890" | "1234567890, 34567890"
		"1234567890" | "345678901" | "1234567890, 3456..."
	}
}
