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


import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.junit.Test;
import org.squashtest.tm.service.importer.ImportMode;
import org.squashtest.tm.service.internal.batchimport.ParameterInstruction;

import spock.lang.Specification;
import spock.lang.Unroll;

import static org.squashtest.tm.service.internal.batchimport.testcase.excel.ParameterSheetColumn.*

/**
 * @author Gregory Fouquet
 *
 */
class ParameterInstructionBuilderTest extends Specification {
	WorksheetDef wd = Mock();
	Row row = Mock()
	ParameterInstructionBuilder builder

	def setup() {
		wd.getWorksheetType() >> TemplateWorksheet.PARAMETERS_SHEET
		builder = new ParameterInstructionBuilder(wd)
	}

	private Cell mockCell(cellType, cellValue) {
		Cell cell = Mock()

		cell.getCellType() >> cellType

		cell.getNumericCellValue() >> cellValue
		cell.getStringCellValue() >> cellValue
		cell.getBooleanCellValue() >> cellValue
		cell.getDateCellValue() >> cellValue

		return cell
	}

	@Unroll
	def "should create test step target from row with this bunch of data : #col #cellType #cellValue #propName #propValue"() {
		given:
		Cell cell = mockCell(cellType, cellValue)
		row.getCell(30, _) >> cell

		and:
		wd.getImportableColumnDefs() >> [new StdColumnDef(col, 30)]
		wd.getCustomFieldDefs() >> []

		when:
		ParameterInstruction instruction = builder.build(row)

		then:
		instruction.target[propName] == propValue

		where:
		col				| cellType					| cellValue			| propName			| propValue
		TC_OWNER_PATH	| Cell.CELL_TYPE_STRING		| "here/i/am"		| "path"			| "here/i/am/parameters/null"
		TC_OWNER_PATH	| Cell.CELL_TYPE_BLANK		| null				| "path"			| "null/parameters/null"

	}

	@Unroll
	def "should create test step instruction from row with this bunch of data : #col #cellType #cellValue #propName #propValue"() {
		given:
		Cell cell = mockCell(cellType, cellValue)
		row.getCell(30, _) >> cell

		and:
		wd.getImportableColumnDefs() >> [new StdColumnDef(col, 30)]
		wd.getCustomFieldDefs() >> []

		when:
		ParameterInstruction instruction = builder.build(row)

		then:
		instruction[propName] == propValue

		where:
		col				| cellType					| cellValue			| propName			| propValue
		ACTION			| Cell.CELL_TYPE_STRING		| "CREATE"			| "mode"			| ImportMode.CREATE
		ACTION			| Cell.CELL_TYPE_STRING		| "C"				| "mode"			| ImportMode.CREATE
		ACTION			| Cell.CELL_TYPE_BLANK		| null				| "mode"			| ImportMode.UPDATE

	}

	@Unroll
	def "should create action test step from row with this bunch of data : #col #cellType #cellValue #propName #propValue"() {
		given:
		Cell cell = mockCell(cellType, cellValue)
		row.getCell(30, _) >> cell

		and:
		wd.getImportableColumnDefs() >> [new StdColumnDef(col, 30)]
		wd.getCustomFieldDefs() >> []

		when:
		ParameterInstruction instruction = builder.build(row)

		then:
		instruction.parameter[propName] == propValue

		where:
		col						| cellType					| cellValue								| propName			| propValue
		TC_PARAM_NAME			| Cell.CELL_TYPE_STRING		| "my name is luka"						| "name"			| "my name is luka"
		TC_PARAM_NAME			| Cell.CELL_TYPE_BLANK		| null									| "name"			| null

		TC_PARAM_DESCRIPTION	| Cell.CELL_TYPE_STRING		| "i live on the 2nd floor"				| "description"		| "i live on the 2nd floor"
		TC_PARAM_DESCRIPTION	| Cell.CELL_TYPE_BLANK		| null									| "description"		| null

	}

}
