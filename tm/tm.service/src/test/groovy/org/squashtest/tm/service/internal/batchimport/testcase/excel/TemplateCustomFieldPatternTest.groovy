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
package org.squashtest.tm.service.internal.batchimport.testcase.excel;

import spock.lang.Specification;
import spock.lang.Unroll;

import static org.squashtest.tm.service.internal.batchimport.testcase.excel.TemplateCustomFieldPattern.*
/**
 * @author Gregory Fouquet
 *
 */
public class TemplateCustomFieldPatternTest extends Specification {
	@Unroll
	def "template #template should parse header #header as code #expectedCode"() {
		expect:
		template.parseFieldCode(header) == expectedCode

		where:
		template               | header                  | expectedCode
		TEST_CASE_CUSTOM_FIELD | "TC_CUF_"               | null
		TEST_CASE_CUSTOM_FIELD | "TC_CUF_    "           | null
		TEST_CASE_CUSTOM_FIELD | "TC_CUF_WHICHEVER_CODE" | "WHICHEVER_CODE"
		TEST_CASE_CUSTOM_FIELD | "TC_CUF_s'çz_gîobn x"   | "s'çz_gîobn x"
		TEST_CASE_CUSTOM_FIELD | "TC_CF_WHICHEVER_CODE"  | null
		TEST_CASE_CUSTOM_FIELD | null                    | null
		STEP_CUSTOM_FIELD      | "TC_STEP_CUF_WHICHEVER_CODE" | "WHICHEVER_CODE"
		STEP_CUSTOM_FIELD      | "TC_STEP_CF_WHICHEVER_CODE"  | null
		STEP_CUSTOM_FIELD      | null                    | null
		NO_CUSTOM_FIELD        | "TC_CUF_WHICHEVER_CODE" | null
		NO_CUSTOM_FIELD        | "TC_CF_WHICHEVER_CODE"  | null
		NO_CUSTOM_FIELD        | null                    | null
	}
}
