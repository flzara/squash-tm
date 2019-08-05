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
package org.squashtest.tm.service.internal.customreport

import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.unitils.dbunit.annotation.DataSet
import spock.unitils.UnitilsSupport
import org.springframework.transaction.annotation.Transactional
import org.jooq.Record
import org.squashtest.tm.service.customreport.CustomReportCustomExportCSVService
import org.squashtest.tm.service.customreport.CustomReportLibraryNodeService
import java.util.Set
import org.squashtest.tm.domain.EntityType

import javax.inject.Inject

@UnitilsSupport
@DataSet("CustomReportCustomExportCSVServiceIT.sandbox.xml")
@Transactional
class CustomReportCustomExportCSVServiceIT extends DbunitServiceSpecification {

	@Inject
	CustomReportCustomExportCSVService customExportService

	@Inject
	CustomReportLibraryNodeService customReportLibraryNodeService

	Set<EntityType> emptyCufEntitySet = new HashSet<>()

	def "getRowsData(CustomReportCustomExport) - Should retrieve data of a Custom Export with only Campaign related columns with no CustomField"() {
		given:
		def customExport = customReportLibraryNodeService.findCustomExportByNodeId(-3L)
		when:
		Iterator<Record> result = customExportService.getRowsData(customExport, emptyCufEntitySet)
		then:
		getIteratorSize(result) == 1
	}

	def "getRowsData(CustomReportCustomExport) - Should retrieve data of a Custom Export with only Campaign, Iteration related columns with no CustomField"() {
		given:
		def customExport = customReportLibraryNodeService.findCustomExportByNodeId(-4L)
		when:
		Iterator<Record> result = customExportService.getRowsData(customExport, emptyCufEntitySet)
		then:
		getIteratorSize(result) == 2
	}

	def "getRowsData(CustomReportCustomExport) - Should retrieve some records of a Custom Export with Campaign, Iteration, TestSuite data no CustomField"() {
		given:
			def customExport = customReportLibraryNodeService.findCustomExportByNodeId(-5L)
		when:
			Iterator<Record> result = customExportService.getRowsData(customExport, emptyCufEntitySet)
		then:
			getIteratorSize(result) == 12
	}

	def "getRowsData(CustomReportCustomExport) - Should retrieve some records of a Custom Export with Campaign, Iteration, TestCase data no CustomField"() {
		given:
		def customExport = customReportLibraryNodeService.findCustomExportByNodeId(-6L)
		when:
		Iterator<Record> result = customExportService.getRowsData(customExport, emptyCufEntitySet)
		then:
		getIteratorSize(result) == 10
	}

	def "getRowsData(CustomReportCustomExport) - Should retrieve some records of a Custom Export with Campaign, Iteration, Execution data no CustomField"() {
		given:
		def customExport = customReportLibraryNodeService.findCustomExportByNodeId(-7L)
		when:
		Iterator<Record> result = customExportService.getRowsData(customExport, emptyCufEntitySet)
		then:
		getIteratorSize(result) == 12
	}

	def "getRowsData(CustomReportCustomExport) - Should retrieve some records of a Custom Export with Campaign, Iteration, ExecutionStep data no CustomField"() {
		given:
		def customExport = customReportLibraryNodeService.findCustomExportByNodeId(-8L)
		when:
		Iterator<Record> result = customExportService.getRowsData(customExport, emptyCufEntitySet)
		then:
		getIteratorSize(result) == 26
	}

	def "getRowsData(CustomReportCustomExport) - Should retrieve some records of a Custom Export with only ExecutionStep data with no CustomField"() {
		given:
		def customExport = customReportLibraryNodeService.findCustomExportByNodeId(-9L)
		when:
		Iterator<Record> result = customExportService.getRowsData(customExport, emptyCufEntitySet)
		then:
		getIteratorSize(result) == 26
	}

	def "getRowsData(CustomReportCustomExport) - Should retrieve many records of a Full Export with no CustomField"() {
		given:
		def customExport = customReportLibraryNodeService.findCustomExportByNodeId(-2L)
		when:
		Iterator<Record> result = customExportService.getRowsData(customExport, emptyCufEntitySet)
		then:
		getIteratorSize(result) == 30
	}

	def getIteratorSize(Iterator<Record> iterator) {
		int size = 0
		for(; iterator.hasNext(); size++) {
			iterator.next()
		}
		return size
	}

	@DataSet("CustomReportCustomExportCSVServiceIT.sandbox.huge.xml")
	def "getRowsData(CustomReportCustomExport) - Should retrieve many records of a Huge Full Export with no CustomField without Exceptions"() {
		given:
		def customExport = customReportLibraryNodeService.findCustomExportByNodeId(-10L)
		when:
		Iterator<Record> result = customExportService.getRowsData(customExport, emptyCufEntitySet)
		then:
		getIteratorSize(result) == 240
	}

}
