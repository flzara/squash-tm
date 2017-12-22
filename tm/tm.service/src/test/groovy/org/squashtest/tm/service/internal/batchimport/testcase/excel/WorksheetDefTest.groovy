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



import static org.squashtest.tm.service.internal.batchimport.testcase.excel.TemplateWorksheet.*
import static org.squashtest.tm.service.internal.batchimport.testcase.excel.TestCaseSheetColumn.*

import org.squashtest.tm.service.batchimport.excel.TemplateMismatchException

import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Gregory Fouquet
 *
 */
class WorksheetDefTest extends Specification {
	@Unroll
	def "colset #cols should produce a valid worksheet"() {
		given:
		WorksheetDef wd = new WorksheetDef(TemplateWorksheet.TEST_CASES_SHEET)
		cols.each { wd.addColumnDef(new StdColumnDef(it, 1)) }

		when:
		def worksheetStatus = wd.validate()

		then:
		worksheetStatus.isFormatOk()

		where:
		cols << [
			TestCaseSheetColumn.values(),
			[TestCaseSheetColumn.TC_NAME, TestCaseSheetColumn.TC_PATH],
			[TestCaseSheetColumn.TC_PATH]
		]

	}

	@Unroll
	def "colset #cols should produce an invalid worksheet"() {
		given:
		WorksheetDef wd = new WorksheetDef(TemplateWorksheet.TEST_CASES_SHEET)
		cols.each { wd.addColumnDef(new StdColumnDef(it, 1)) }

		when:
		def worksheetStatus = wd.validate()

		then:
		!worksheetStatus.isFormatOk()

		where:
		cols << [
			[],
			[TestCaseSheetColumn.TC_NAME],
			[TestCaseSheetColumn.TC_ID]
		]

	}

	@Unroll
	def "header #header should be a custom field : #isField"() {
		given:
		WorksheetDef wd = new WorksheetDef(TEST_CASES_SHEET)

		expect:
		wd.isCustomFieldHeader(header) == isField

		where:
		header                | isField
		"TC_CUF_  "           | false
		"TC_CUF_"             | false
		null                  | false
		"    "                | false
		"TC_CUF_s'çz_gîobn x" | true
		"TC_CUF_FOO"          | true
	}

	def "should add a std column"() {
		given:
		WorksheetDef wd = new WorksheetDef(TEST_CASES_SHEET)

		when:
		def col = wd.addColumnDef("TC_NAME", 10)

		then:
		col.type == TC_NAME
		col.index == 10
		wd.stdColumnDefs[TC_NAME] == col
		wd.customFieldDefs.isEmpty()

	}

	def "should add a custom field column"() {
		given:
		WorksheetDef wd = new WorksheetDef(TEST_CASES_SHEET)

		when:
		def col = wd.addColumnDef("TC_CUF_HANDCUF", 10)

		then:
		col.code == "HANDCUF"
		col.index == 10
		wd.customFieldDefs == [col]
		wd.stdColumnDefs.isEmpty()

	}

	def "should not add any column"() {
		given:
		WorksheetDef wd = new WorksheetDef(TEST_CASES_SHEET)

		when:
		def col = wd.addColumnDef("NOT A KNOWN COLUMN", 10)

		then:
		col == null
		wd.customFieldDefs.isEmpty()
		wd.stdColumnDefs.isEmpty()

	}
	def "should return importable columns"() {
		given:
		WorksheetDef wd = new WorksheetDef(TEST_CASES_SHEET)

		when:
		wd.addColumnDef("ACTION", 10) // optional
		wd.addColumnDef("TC_PATH", 20) // mandatory
		wd.addColumnDef("PROJECT_ID", 30) // ignored
		def col = wd.addColumnDef("NOT A KNOWN COLUMN", 40) // ditched

		then:
		wd.importableColumnDefs.size() == 2
		wd.importableColumnDefs*.type.containsAll([ACTION, TC_PATH])

	}
}
