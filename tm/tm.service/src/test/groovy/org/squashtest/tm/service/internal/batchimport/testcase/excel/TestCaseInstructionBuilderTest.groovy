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
package org.squashtest.tm.service.internal.batchimport.testcase.excel


import static org.squashtest.tm.service.internal.batchimport.testcase.excel.TestCaseSheetColumn.*

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.squashtest.tm.core.foundation.lang.DateUtils
import org.squashtest.tm.domain.infolist.ListItemReference
import org.squashtest.tm.domain.testcase.TestCaseImportance
import org.squashtest.tm.domain.testcase.TestCaseStatus
import org.squashtest.tm.service.importer.ImportMode
import org.squashtest.tm.service.importer.ImportStatus
import org.squashtest.tm.service.internal.batchimport.TestCaseInstruction
import org.squashtest.tm.service.internal.batchimport.excel.CannotCoerceException

import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Gregory Fouquet
 *
 */
class TestCaseInstructionBuilderTest extends Specification {
	WorksheetDef wd = Mock()
	Row row = Mock()
	TestCaseInstructionBuilder builder
	Cell pathCell = Mock()
	Cell orderCell = Mock()

	def setup() {
		wd.getWorksheetType() >> TemplateWorksheet.TEST_CASES_SHEET
		builder = new TestCaseInstructionBuilder(wd)
	}

	private setupTestCaseTargetSpec() {
		wd.getImportableColumnDefs() >> [
			new StdColumnDef(TestCaseSheetColumn.TC_PATH, 10),
			new StdColumnDef(TestCaseSheetColumn.TC_NUM, 20)
		]

		wd.getCustomFieldDefs() >> []

		row.getCell(10, _) >> pathCell
		row.getCell(20, _) >> orderCell
	}

	@Unroll
	def "should create target from row with path #path and order #order"() {
		given:
		setupTestCaseTargetSpec()

		and:
		pathCell.getCellType() >> Cell.CELL_TYPE_STRING
		pathCell.getStringCellValue() >> path
		orderCell.getNumericCellValue() >> order
		orderCell.getStringCellValue() >> order
		orderCell.getCellType() >> cellType

		when:
		TestCaseInstruction instruction = builder.build(row)

		then:
		instruction.target.path == path
		instruction.target.order == intOrder

		where:
		path 	| order 		| intOrder 	| cellType
		"foo" 	| 30.0 			| 29       	| Cell.CELL_TYPE_NUMERIC
		"foo" 	| 29.9999996	| 29 		| Cell.CELL_TYPE_NUMERIC
		"foo" 	| 75.359 		| 74     	| Cell.CELL_TYPE_NUMERIC
		"foo" 	| "30" 			| 29       	| Cell.CELL_TYPE_STRING
		"foo" 	| null 			| null     	| Cell.CELL_TYPE_BLANK
	}


	@Unroll
	def "should log fatal error on blank path from cell type #cellType"() {
		given:
		setupTestCaseTargetSpec()

		and:
		pathCell.getCellType() >> cellType

		when:
		TestCaseInstruction instruction = builder.build(row)

		then:
		instruction.logTrain.hasCriticalErrors()

		where:
		cellType << [Cell.CELL_TYPE_BLANK, Cell.CELL_TYPE_ERROR, Cell.CELL_TYPE_FORMULA, Cell.CELL_TYPE_STRING]
	}

	def "not sure what we should do when order not a number"() {
		given:
		setupTestCaseTargetSpec()

		and:
		pathCell.getStringCellValue() >> "foo"
		orderCell.getNumericCellValue() >> { throw new RuntimeException("not a number, lol") }
		orderCell.getStringCellValue() >> "not a number, lol"
		orderCell.getCellType() >> Cell.CELL_TYPE_STRING

		when:
		TestCaseInstruction target = builder.build(row)

		then: "no exceptions raised not to break processing"
		notThrown(CannotCoerceException)
		target != null
	}

	@Unroll
	def "should create test case from row with this bunch of data : #col #cellType #cellValue #propName #propValue"() {
		given:
		Cell cell = mockCell(cellType, cellValue)
		row.getCell(30, _) >> cell

		and:
		wd.getImportableColumnDefs() >> [new StdColumnDef(col, 30)]
		wd.getCustomFieldDefs() >> []

		when:
		TestCaseInstruction instruction = builder.build(row)

		then:
		instruction.testCase[propName] == propValue

		where:
		col				| cellType					| cellValue			| propName			| propValue
		TC_REFERENCE	| Cell.CELL_TYPE_STRING		| "yeah"			| "reference"		| "yeah"
		TC_REFERENCE	| Cell.CELL_TYPE_BLANK		| ""				| "reference"		| null

		TC_NAME			| Cell.CELL_TYPE_STRING		| "yeah"			| "name"			| "yeah"
		TC_NAME			| Cell.CELL_TYPE_STRING		| ""				| "name"			| ""
		TC_NAME		 	| Cell.CELL_TYPE_BLANK		| ""				| "name"			| null

		TC_WEIGHT_AUTO	| Cell.CELL_TYPE_NUMERIC	| 1					| "importanceAuto"	| true
		TC_WEIGHT_AUTO	| Cell.CELL_TYPE_BOOLEAN	| true				| "importanceAuto"	| true
		TC_WEIGHT_AUTO	| Cell.CELL_TYPE_STRING		| "1"				| "importanceAuto"	| true
		TC_WEIGHT_AUTO	| Cell.CELL_TYPE_NUMERIC	| 0					| "importanceAuto"	| false
		TC_WEIGHT_AUTO	| Cell.CELL_TYPE_BOOLEAN	| false				| "importanceAuto"	| false
		TC_WEIGHT_AUTO	| Cell.CELL_TYPE_STRING		| "0"				| "importanceAuto"	| false
		TC_WEIGHT_AUTO	| Cell.CELL_TYPE_BLANK		| ""				| "importanceAuto"	| null

		TC_WEIGHT		| Cell.CELL_TYPE_STRING		| "VERY_HIGH"		| "importance"		| TestCaseImportance.VERY_HIGH
		TC_WEIGHT		| Cell.CELL_TYPE_BLANK		| ""				| "importance"		| null

		TC_STATUS		| Cell.CELL_TYPE_STRING		| "APPROVED"		| "status"			| TestCaseStatus.APPROVED
		TC_STATUS		| Cell.CELL_TYPE_BLANK		| ""				| "status"			| null

		TC_DESCRIPTION	| Cell.CELL_TYPE_STRING		| "yeah"			| "description"		| "yeah"
		TC_DESCRIPTION	| Cell.CELL_TYPE_BLANK		| ""				| "description"		| null

		TC_PRE_REQUISITE| Cell.CELL_TYPE_STRING		| "yeah"			| "prerequisite"	| "yeah"
		TC_PRE_REQUISITE| Cell.CELL_TYPE_BLANK		| ""				| "prerequisite"	| null

		TC_CREATED_ON	| Cell.CELL_TYPE_STRING		| "2010-01-12"		| "createdOn"		| DateUtils.parseIso8601Date("2010-01-12")
		TC_CREATED_ON	| Cell.CELL_TYPE_NUMERIC	| DateUtils.parseIso8601Date("2019-03-17") | "createdOn"		| DateUtils.parseIso8601Date("2019-03-17")
		TC_CREATED_ON	| Cell.CELL_TYPE_BLANK		| ""				| "createdOn"		| null

		TC_CREATED_BY	| Cell.CELL_TYPE_STRING		| "your mom"		| "createdBy"		| "your mom"
		TC_CREATED_BY	| Cell.CELL_TYPE_BLANK		| ""				| "createdBy"		| null
	}

	@Unroll
	def "should create test case from row with this bunch of data (info lists) : #col #cellType #cellValue #propName #propValue"() {
		given:
		Cell cell = mockCell(cellType, cellValue)
		row.getCell(30, _) >> cell

		and:
		wd.getImportableColumnDefs() >> [new StdColumnDef(col, 30)]
		wd.getCustomFieldDefs() >> []

		when:
		TestCaseInstruction instruction = builder.build(row)

		then:
		propValue == null || propValue.references(instruction.testCase[propName])

		where:
		col				| cellType					| cellValue			| propName			| propValue

		TC_NATURE		| Cell.CELL_TYPE_STRING		| "USER_TESTING"	| "nature"			| new ListItemReference("NAT_USER_TESTING")
		TC_NATURE		| Cell.CELL_TYPE_BLANK		| ""				| "nature"			| null

		TC_TYPE			| Cell.CELL_TYPE_STRING		| "PARTNER_TESTING"	| "type"			| new ListItemReference("TYP_PARTNER_TESTING")
		TC_TYPE			| Cell.CELL_TYPE_BLANK		| ""				| "type"			| null


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
	def "should create instruction from row with this bunch of data : #col #cellType #cellValue #propValue"() {
		given:
		Cell cell = mockCell(cellType, cellValue)
		row.getCell(30, _) >> cell

		and:
		wd.getImportableColumnDefs() >> [new StdColumnDef(col, 30)]
		wd.getCustomFieldDefs() >> []

		when:
		TestCaseInstruction instruction = builder.build(row)

		then:
		instruction.mode == propValue

		where:
		col				| cellType					| cellValue			| propValue
		ACTION			| Cell.CELL_TYPE_STRING		| "CREATE"			| ImportMode.CREATE
		ACTION			| Cell.CELL_TYPE_STRING		| "C"				| ImportMode.CREATE
		ACTION			| Cell.CELL_TYPE_BLANK		| ""				| ImportMode.UPDATE
	}

	@Unroll
	def "should log error on illegal action value : #col #cellType #cellValue"() {
		given:
		Cell cell = mockCell(cellType, cellValue)
		row.getCell(30, _) >> cell

		and:
		wd.getImportableColumnDefs() >> [new StdColumnDef(col, 30)]
		wd.getCustomFieldDefs() >> []

		when:
		TestCaseInstruction instruction = builder.build(row)

		then:
		instruction.logTrain.entries[0].status == ImportStatus.WARNING
		instruction.mode == propValue

		where:
		col				| cellType					| cellValue		| propValue
		ACTION			| Cell.CELL_TYPE_STRING		| "PROBLEM?"	| ImportMode.getDefault()
		ACTION			| Cell.CELL_TYPE_FORMULA	| null			| ImportMode.getDefault()
	}

	@Unroll
	def "should add custom field to instruction from row with this bunch of data : #cellType #cellValue #fieldCode"() {
		given:
		Cell cell = mockCell(cellType, cellValue)
		row.getCell(30, _) >> cell

		and:
		wd.getImportableColumnDefs() >> []
		wd.getCustomFieldDefs() >> [
			new CustomFieldColumnDef(fieldCode, 30)
		]

		when:
		TestCaseInstruction instruction = builder.build(row)

		then:
		instruction.customFields[fieldCode] == cellValue

		where:
		cellType			 	| fieldCode	| cellValue
		Cell.CELL_TYPE_STRING	|"FOO"		| "bar"
		Cell.CELL_TYPE_BLANK	|"FOO"		| null
	}

	def "should set the row number on the instruction"() {
		given:
		wd.getImportableColumnDefs() >> []
		wd.getCustomFieldDefs() >> []

		and:
		row.getRowNum() >> 150

		when:
		TestCaseInstruction instruction = builder.build(row)

		then:
		instruction.line == 150
	}

	@Unroll
	def "should log warning for broken data : #col #cellType #cellValue #propName #propValue"() {
		given:
		Cell cell = mockCell(cellType, cellValue)
		row.getCell(30, _) >> cell

		and:
		wd.getImportableColumnDefs() >> [new StdColumnDef(col, 30)]
		wd.getCustomFieldDefs() >> []

		when:
		TestCaseInstruction instruction = builder.build(row)

		then:
		!instruction.logTrain.hasCriticalErrors()
		instruction.logTrain.entries.size == 1
		instruction.testCase[propName] == propValue

		where:
		col				| cellType					| cellValue			| propName			| propValue
		TC_WEIGHT_AUTO	| Cell.CELL_TYPE_NUMERIC	| 2					| "importanceAuto"	| null
		TC_WEIGHT_AUTO	| Cell.CELL_TYPE_STRING		| "PROBLEM?"		| "importanceAuto"	| null
		TC_WEIGHT_AUTO	| Cell.CELL_TYPE_FORMULA	| ""				| "importanceAuto"	| null

		TC_WEIGHT		| Cell.CELL_TYPE_STRING		| "PROBLEM?"		| "importance"		| null
		TC_WEIGHT		| Cell.CELL_TYPE_NUMERIC	| 1					| "importance"		| null

		TC_STATUS		| Cell.CELL_TYPE_STRING		| "PROBLEM?"		| "nature"			| null
		TC_STATUS		| Cell.CELL_TYPE_NUMERIC	| 1					| "nature"			| null
	}

}
