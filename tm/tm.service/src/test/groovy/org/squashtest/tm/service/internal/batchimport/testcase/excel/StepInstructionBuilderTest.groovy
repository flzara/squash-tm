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
import org.squashtest.tm.service.internal.batchimport.StepInstruction;

import spock.lang.Specification;
import spock.lang.Unroll;

import static org.squashtest.tm.service.internal.batchimport.testcase.excel.StepSheetColumn.*

/**
 * @author Gregory Fouquet
 *
 */
class StepInstructionBuilderTest extends Specification {
	WorksheetDef wd = Mock();
	Row row = Mock()
	StepInstructionBuilder builder

	def setup() {
		wd.getWorksheetType() >> TemplateWorksheet.STEPS_SHEET
		builder = new StepInstructionBuilder(wd)
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
		StepInstruction instruction = builder.build(row)

		then:
		instruction.target[propName] == propValue

		where:
		col				| cellType					| cellValue			| propName			| propValue
		TC_OWNER_PATH	| Cell.CELL_TYPE_STRING		| "here/i/am"		| "path"			| "here/i/am/steps/null"
		TC_OWNER_PATH	| Cell.CELL_TYPE_BLANK		| null				| "path"			| "null/steps/null"

		TC_STEP_NUM 	| Cell.CELL_TYPE_NUMERIC	| 20				| "index"			| 19
		TC_STEP_NUM 	| Cell.CELL_TYPE_STRING		| "20"				| "index"			| 19
		TC_STEP_NUM		| Cell.CELL_TYPE_BLANK		| null				| "index"			| null

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
		StepInstruction instruction = builder.build(row)

		then:
		instruction[propName] == propValue

		where:
		col				| cellType					| cellValue			| propName			| propValue
		ACTION			| Cell.CELL_TYPE_STRING		| "DELETE"			| "mode"			| ImportMode.DELETE
		ACTION			| Cell.CELL_TYPE_STRING		| "D"				| "mode"			| ImportMode.DELETE
		ACTION			| Cell.CELL_TYPE_BLANK		| null				| "mode"			| ImportMode.UPDATE

	}

	@Unroll
	def "should create action test step from row with this bunch of data : #col #cellType #cellValue #propName #propValue"() {
		given:
		Cell cell = mockCell(cellType, cellValue)
		row.getCell(30, _) >> cell

		and:
		Cell typeCell = mockCell(Cell.CELL_TYPE_NUMERIC, 0)
		row.getCell(40, _) >> typeCell
		def typeCellDef = new StdColumnDef(StepSheetColumn.TC_STEP_IS_CALL_STEP, 40)

		and:
		wd.getImportableColumnDefs() >> [new StdColumnDef(col, 30), typeCellDef]
		wd.getColumnDef(StepSheetColumn.TC_STEP_IS_CALL_STEP) >> typeCellDef
		wd.getCustomFieldDefs() >> []

		and: builder = new StepInstructionBuilder(wd);

		when:
		StepInstruction instruction = builder.build(row)

		then:
		instruction.testStep[propName] == propValue

		where:
		col						| cellType					| cellValue								| propName			| propValue
		TC_STEP_ACTION			| Cell.CELL_TYPE_STRING		| "i just want a lover like any other"	| "action"			| "i just want a lover like any other"
		TC_STEP_ACTION			| Cell.CELL_TYPE_BLANK		| null									| "action"			| null

		TC_STEP_EXPECTED_RESULT	| Cell.CELL_TYPE_STRING		| "what do i get"						| "expectedResult"	| "what do i get"
		TC_STEP_EXPECTED_RESULT	| Cell.CELL_TYPE_BLANK		| null									| "expectedResult"	| null

	}
	@Unroll
	def "should create call test step from row with this bunch of data : #col #cellType #cellValue #propName #propValue"() {
		given:
		Cell cell = mockCell(cellType, cellValue)
		row.getCell(30, _) >> cell

		and:
		Cell typeCell = mockCell(Cell.CELL_TYPE_BOOLEAN, true)
		row.getCell(40, _) >> typeCell
		def typeCellDef = new StdColumnDef(StepSheetColumn.TC_STEP_IS_CALL_STEP, 40)

		and:
		wd.getImportableColumnDefs() >> [new StdColumnDef(col, 30), typeCellDef]
		wd.getColumnDef(StepSheetColumn.TC_STEP_IS_CALL_STEP) >> typeCellDef
		wd.getCustomFieldDefs() >> []

		and: builder = new StepInstructionBuilder(wd);

		when:
		StepInstruction instruction = builder.build(row)

		then:
		instruction.calledTC[propName] == propValue

		where:
		col						| cellType					| cellValue		| propName			| propValue
		TC_STEP_ACTION			| Cell.CELL_TYPE_STRING		| "here/i/am"	| "path"			| "here/i/am"
		TC_STEP_ACTION			| Cell.CELL_TYPE_BLANK		| null			| "path"			| null

	}

	@Unroll
	def "should add custom field to instruction from row with this bunch of data : #cellType #cellValue #fieldCode"() {
		given:
		Cell cell = mockCell(cellType, cellValue)
		row.getCell(30, _) >> cell

		and:
		wd.getImportableColumnDefs() >> []
		wd.getCustomFieldDefs() >> [new CustomFieldColumnDef(fieldCode, 30)]

		when:
		StepInstruction instruction = builder.build(row)

		then:
		instruction.customFields[fieldCode] == cellValue

		where:
		cellType			 	| fieldCode	| cellValue
		Cell.CELL_TYPE_STRING	|"FOO"		| "bar"
		Cell.CELL_TYPE_BLANK	|"FOO"		| null
	}

}
