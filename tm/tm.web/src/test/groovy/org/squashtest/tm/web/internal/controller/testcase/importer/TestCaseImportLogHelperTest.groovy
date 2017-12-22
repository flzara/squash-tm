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
package org.squashtest.tm.web.internal.controller.testcase.importer

import org.squashtest.tm.service.importer.EntityType
import org.squashtest.tm.service.internal.batchimport.testcase.excel.TemplateWorksheet

import java.io.FileInputStream

import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.ss.util.WorkbookUtil
import org.springframework.web.context.request.WebRequest
import org.squashtest.tm.service.importer.ImportLog
import org.squashtest.tm.service.importer.ImportMode
import org.squashtest.tm.service.importer.ImportStatus
import org.squashtest.tm.service.importer.LogEntry
import org.squashtest.tm.web.internal.controller.testcase.importer.TestCaseImportLogHelper
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper

import spock.lang.Specification

import java.lang.reflect.Method

/**
 * @author Gregory Fouquet
 *
 */
class TestCaseImportLogHelperTest extends Specification {
	InternationalizationHelper i18n = Mock()
	TestCaseImportLogHelper helper = new TestCaseImportLogHelper()

	def setup() {
		helper.messageSource = i18n

		i18n._ >> "chic happened"
	}

	def "should retrieve log"() {
		when:
		File xlsLog = helper.fetchLogFile("xxx")

		then:
		xlsLog.name.startsWith "xxx"

	}

	def "should write wb to file"() {
		given:
		Workbook wb = new HSSFWorkbook()
		Sheet ws = wb.createSheet("foo")

		10.times { it->
			Row row = ws.createRow(it)
			["bar", "", 10].eachWithIndex {jt, dx -> row.createCell(dx).setCellValue(jt + it)}

		}

		and:
		File tmp = File.createTempFile("should write wb to file", ".xls")

		when:
		helper.writeToFile(tmp, wb)

		then:
		WorkbookFactory.create(new FileInputStream(tmp))

		cleanup:
		tmp.deleteOnExit()

	}

	def "should create wb from log"() {
		given:
		LogEntry entry = Mock()
		entry.getLine() >> { Math.round(Math.random() * 10) }
		entry.getMode() >> ImportMode.UPDATE
		entry.getStatus() >> ImportStatus.WARNING

		and:
		ImportLog log = Mock()
		log.findAllFor(_) >> [entry, entry, entry]


		when:
		File f = helper.storeLogFile(log)

		then:
		WorkbookFactory.create(new FileInputStream(f))

		cleanup:
		f.deleteOnExit()

	}

	def "should create workbook with several sheet and several report rows"(){
		given:
		LogEntry entry = Mock()
		entry.getLine() >> { Math.round(Math.random() * 10) }
		entry.getMode() >> ImportMode.UPDATE
		entry.getStatus() >> ImportStatus.WARNING

		and:
		Set <String> sheetNames = new HashSet<String>()
		sheetNames << "TEST CASE"
		sheetNames << "TEST STEP"
		sheetNames << "PARAMETER"
		sheetNames << "DATASET"
		sheetNames << "LINK_REQ_TC"

		and:
		ImportLog log = Mock()
		log.findAllFor(_) >> [entry, entry, entry]

		when:
		def clazz = ImportLogHelper.class
		def method = clazz.getDeclaredMethod("buildWorkbook", ImportLog.class)
		method.setAccessible(true)
		Workbook workbook = method.invoke(helper,log)

		then:
		sheetNames.each {
			def sheet = workbook.getSheet(it)
			assert sheet != null
			sheet.getRow(3) != null
		}
	}

	def "should create workbook with several sheet even with nothing imported"(){
		given:
		LogEntry entry = Mock()
		entry.getLine() >> { Math.round(Math.random() * 10) }
		entry.getMode() >> ImportMode.UPDATE
		entry.getStatus() >> ImportStatus.WARNING

		and:
		Set <String> sheetNames = new HashSet<String>()
		sheetNames << "TEST CASE"
		sheetNames << "TEST STEP"
		sheetNames << "PARAMETER"
		sheetNames << "DATASET"
		sheetNames << "LINK_REQ_TC"

		and:
		ImportLog log = Mock()
		log.findAllFor(_) >> []

		when:
		def clazz = ImportLogHelper.class
		def method = clazz.getDeclaredMethod("buildWorkbook", ImportLog.class)
		method.setAccessible(true)
		def workbook = method.invoke(helper,log)

		then:
		sheetNames.each {
			def sheet = workbook.getSheet(it)
			assert sheet != null
			sheet.getRow(3) != null
		}
	}



}
