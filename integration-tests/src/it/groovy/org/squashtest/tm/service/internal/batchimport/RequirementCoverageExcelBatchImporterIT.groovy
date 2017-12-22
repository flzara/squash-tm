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
import org.squashtest.tm.domain.requirement.Requirement
import org.squashtest.tm.domain.requirement.RequirementVersion
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.squashtest.tm.service.importer.EntityType
import org.squashtest.tm.service.importer.ImportLog
import org.squashtest.tm.service.internal.batchimport.requirement.excel.RequirementExcelBatchImporter
import org.squashtest.tm.service.testcase.TestCaseLibraryNavigationService
import org.unitils.dbunit.annotation.DataSet

import spock.unitils.UnitilsSupport

@UnitilsSupport
@Transactional
@RunWith(Sputnik)
class RequirementCoverageExcelBatchImporterIT extends DbunitServiceSpecification{


	@Inject
	private RequirementExcelBatchImporter importer
	@Inject
	private TestCaseLibraryNavigationService navService
	def importFile = { fileName ->
		URL url = RequirementExcelBatchImporter.class.getClassLoader().getResource(fileName)
		File file = new File(url.toURI())
		ImportLog summary = importer.performImport(file)
		return summary
	}

	def findVersion = { id ->
		findEntity(RequirementVersion.class, id)
	}
	def findReq = { id ->
		findEntity(Requirement.class, id)
	}

	def setup(){


		def attachVersionToReq = { reqId, vId ->
			Requirement req = findReq(reqId); req.addVersion(findVersion(vId))
		}

		def reqVersionMap = [11:[11, 12, 13], 21:[21], 31:[31], 41:[41]]
		reqVersionMap.each{ reqId, versionIds ->
			versionIds.each{
				attachVersionToReq(-reqId, -it)
			}
		}
	}

	@DataSet("RequirementExcelBatchImportIT.should import coverage.xml")
	def "should import coverage"(){
		given :

		when :

		ImportLog summary = importFile("import/requirements/dataset_with_coverage.xls")
		summary.recompute()

		def coverages = findAll("RequirementVersionCoverage");

		then :
		summary.coverageSuccesses == 5
		summary.coverageWarnings == 0
		summary.coverageFailures == 0
		coverages.size == 5
		coverages*.verifiedRequirementVersion.name as Set == ["1", "V2", "V3", "2", "1"].collect{"req" + it} as Set
		coverages*.verifyingTestCase.name as Set == ["1", "1", "2", "3", "4"].collect{"tc" + it} as Set
	}


	@DataSet("RequirementExcelBatchImportIT.should import coverage.xml")
	def "should show errors during coverage import"(){
		given :

		def errors = [
			Messages.ERROR_FIELD_MANDATORY,
			Messages.ERROR_REQUIREMENT_NOT_EXISTS,
			Messages.ERROR_MALFORMED_PATH,
			Messages.ERROR_FIELD_MANDATORY,
			Messages.ERROR_UNPARSABLE_INTEGER,
			Messages.ERROR_FIELD_MANDATORY,
			Messages.ERROR_REQUIREMENT_VERSION_NOT_EXISTS,
			null,
			Messages.ERROR_FIELD_MANDATORY,
			Messages.ERROR_MALFORMED_PATH,
			Messages.ERROR_TC_NOT_FOUND,
			Messages.ERROR_COVERAGE_ALREADY_EXIST,
			Messages.ERROR_REQUIREMENT_VERSION_STATUS,
			Messages.ERROR_REQUIREMENT_NOT_EXISTS
		]

		when :
		ImportLog summary = importFile("import/requirements/dataset_with_coverage_errors.xls")
		summary.recompute()

		def coverages = findAll("RequirementVersionCoverage");

		then :
		summary.findAllFor(EntityType.COVERAGE).i18nError == errors
		summary.coverageSuccesses == 1
		summary.coverageWarnings == 0
		summary.coverageFailures == 11
		coverages.size == 1
		coverages*.verifiedRequirementVersion.name as Set == ["1"].collect{"req" + it} as Set
		coverages*.verifyingTestCase.name as Set == ["4"].collect{"tc" + it} as Set
	}
}
