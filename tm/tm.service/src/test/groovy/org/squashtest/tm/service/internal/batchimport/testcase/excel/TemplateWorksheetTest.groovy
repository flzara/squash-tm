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

import static org.squashtest.tm.service.internal.batchimport.testcase.excel.TemplateWorksheet.*


/**
 * @author Gregory Fouquet
 *
 */
class TemplateWorksheetTest extends Specification {

	@Unroll("ws name #énum should coerce as #humanmsg")
	def "ws name #énum.sheetName should coerce as enum #énum"() {
		expect:
		TemplateWorksheet.coerceFromSheetName(sheetname) as Set == templates as Set

		where:
		sheetname 		| humanmsg										|	templates
		"TEST_CASES"	|	"TEST_CASES_SHEET"							|	[TEST_CASES_SHEET]
		"STEPS"			|	"STEPS_SHEET"								|	[STEPS_SHEET]
		"PARAMETERS"	|	"PARAMETERS_SHEET"							|	[PARAMETERS_SHEET]
		"DATASETS"		|	"DATASETS_SHEET, DATASET_PARAM_VALUES_SHEET"|	[DATASETS_SHEET, DATASET_PARAM_VALUES_SHEET]

	}
}
