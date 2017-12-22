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
package org.squashtest.tm.domain.testcase;

import org.hibernate.dialect.function.TrimFunctionTemplate.Specification;
import org.squashtest.tm.domain.testcase.Dataset;
import org.squashtest.tm.domain.testcase.DatasetParamValue;
import org.squashtest.tm.domain.testcase.Parameter;
import org.squashtest.tm.domain.testcase.TestCase;

public class DatasetParamValueTest extends Specification {
	
	def "when creating a new value, dataset contains the new value"() {
		given:
		TestCase testCase = new TestCase()
		Dataset dataset = new Dataset("dataset", testCase)
		Parameter parameter = new Parameter("parameter", testCase)
		
		
	
		when:
		DatasetParamValue value = new DatasetParamValue(parameter, dataset, "paramValue");
		
		then:
		dataset.parameterValues.contains value
	}
}
