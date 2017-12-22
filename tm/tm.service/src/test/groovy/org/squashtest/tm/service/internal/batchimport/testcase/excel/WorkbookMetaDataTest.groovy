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

import static org.junit.Assert.*

import org.squashtest.tm.service.batchimport.excel.ColumnMismatch
import org.squashtest.tm.service.batchimport.excel.TemplateMismatchException
import org.squashtest.tm.service.batchimport.excel.WorksheetFormatStatus

import spock.lang.Specification

/**
 * @author Gregory Fouquet
 *
 */
class WorkbookMetaDataTest extends Specification {
	def "should validate"() {
		given:
		WorkbookMetaData wmd = new WorkbookMetaData()

		wmd.addWorksheetDef(new WorksheetDef(TemplateWorksheet.TEST_CASES_SHEET) {
					WorksheetFormatStatus validate() {
						// validating sheet
						return new WorksheetFormatStatus(TemplateWorksheet.TEST_CASES_SHEET)
					}
				})

		when:
		wmd.validate()

		then:
		notThrown(TemplateMismatchException)
	}
	def "should NOT validate"() {
		given:
		WorkbookMetaData wmd = new WorkbookMetaData()

		wmd.addWorksheetDef(new WorksheetDef(TemplateWorksheet.TEST_CASES_SHEET) {
					WorksheetFormatStatus validate() {
						// non validation sheet
						WorksheetFormatStatus wfs = new WorksheetFormatStatus(TemplateWorksheet.TEST_CASES_SHEET)
						wfs.addMismatches(ColumnMismatch.DUPLICATE, Arrays.asList(TestCaseSheetColumn.TC_NAME))
						return wfs
					}
				})

		wmd.addWorksheetDef(new WorksheetDef(TemplateWorksheet.STEPS_SHEET) {
					WorksheetFormatStatus validate() {
						// validation sheet
						return new WorksheetFormatStatus(TemplateWorksheet.TEST_CASES_SHEET)
					}
				})

		when:
		wmd.validate()

		then:
		TemplateMismatchException e = thrown(TemplateMismatchException)
		e.getWorksheetFormatStatuses().size() == 1
	}
}
