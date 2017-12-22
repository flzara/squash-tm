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

import org.springframework.web.context.request.WebRequest
import org.squashtest.tm.service.importer.ImportLog
import org.squashtest.tm.web.internal.controller.testcase.importer.TestCaseImportController
import org.squashtest.tm.web.internal.controller.testcase.importer.TestCaseImportLogHelper

import spock.lang.Specification

/**
 * @author Gregory Fouquet
 *
 */
class TestCaseImportControllerTest extends Specification {
	TestCaseImportController controller = new TestCaseImportController()
	TestCaseImportLogHelper logHelper = Mock()


	def setup() {
		controller.logHelper = logHelper
	}
	def "should create import log file"() {
		given:
		ImportLog log = new ImportLog()

		and:
		logHelper.storeLogFile(log) >> File.createTempFile("tmp", null)

		when:
		File f = controller.importLogToLogFile(log)

		then:
		f.exists()
	}
	def "should generate import log"() {
		given:
		ImportLog log = new ImportLog()

		and:
		WebRequest request = Mock()
		request.contextPath >> "/squashtm"

		when:
		controller.generateImportLog(request, log)

		then:
		log.reportUrl.startsWith "/squashtm/test-cases/import-logs/"
		1 * logHelper.storeLogFile(log) >> File.createTempFile("tmp", null)

	}

}
