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
package org.squashtest.tm.service.internal.batchimport

import javax.inject.Inject

import org.junit.runner.RunWith
import org.spockframework.runtime.Sputnik
import org.springframework.transaction.annotation.Transactional
import org.squashtest.tm.domain.testcase.TestCaseLibrary
import org.squashtest.tm.domain.testcase.TestStep
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.squashtest.it.stub.security.UserContextHelper;
import org.squashtest.tm.service.importer.ImportLog
import org.unitils.dbunit.annotation.DataSet

import spock.unitils.UnitilsSupport

@UnitilsSupport
@Transactional
@RunWith(Sputnik)
class TestCaseExcelBatchImporterIT extends DbunitServiceSpecification{
	
	def setup(){
		UserContextHelper.setUsername("Bob")
	}

	@Inject
	private TestCaseExcelBatchImporter importer
	@DataSet("TestCaseExcelBatchImporter.should delete a step.xml")
	def "should delete a step"(){
		given :
		URL url = TestCaseExcelBatchImporter.class.getClassLoader().getResource("import/delete-step-import.xls")
		File file = new File(url.toURI())

		when :
		ImportLog summary = importer.performImport(file)

		then :
		summary != null
		summary.recompute()
		summary.testStepSuccesses == 1
		!found(TestStep.class, -2L)
	}

	@DataSet("TestCaseExcelBatchImporter.should import test case in library.xml")
	def "should import test case in library"(){
		given :
		URL url = TestCaseExcelBatchImporter.class.getClassLoader().getResource("import/import test case in library.xls")
		File file = new File(url.toURI())

		when :
		ImportLog summary = importer.performImport(file)

		then :
		summary != null
		TestCaseLibrary library = findEntity(TestCaseLibrary.class, -10L)
	}
}
