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

import org.junit.runner.RunWith
import org.spockframework.runtime.Sputnik
import org.springframework.transaction.annotation.Transactional
import org.squashtest.tm.domain.requirement.RequirementCriticality
import org.squashtest.tm.domain.requirement.RequirementStatus
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.squashtest.tm.service.batchimport.excel.ColumnMismatch
import org.squashtest.tm.service.batchimport.excel.TemplateMismatchException
import org.squashtest.tm.service.importer.ImportStatus
import org.squashtest.tm.service.internal.batchimport.requirement.excel.RequirementSheetColumn
import org.squashtest.tm.service.internal.batchimport.testcase.excel.ExcelWorkbookParser
import org.squashtest.tm.service.internal.batchimport.testcase.excel.TemplateColumn

import spock.unitils.UnitilsSupport

@UnitilsSupport
@Transactional
@RunWith(Sputnik)
class RequirementExcelParserIT extends DbunitServiceSpecification{

	def createParser = { fileName ->
		URL url = ExcelWorkbookParser.class.getClassLoader().getResource(fileName)
		File file = new File(url.toURI())
		ExcelWorkbookParser parser = ExcelWorkbookParser.createParser(file);
		parser.parse().releaseResources();

		return parser
	}

	def "should not find error with ok file"(){

		given :
		def parser = createParser("import/requirements/ok.xls")

		when :
		LogTrain unknownHeaders = parser.logUnknownHeaders();

		then :
		unknownHeaders.entries == []
	}

	def "should log error when there are unkown headers"(){

		given :
		def parser = createParser("import/requirements/unknown_header.xls")

		when :
		LogTrain unknownHeaders = parser.logUnknownHeaders();

		then :

		unknownHeaders.entries*.line == [0, 0]
		unknownHeaders.entries*.status == [ImportStatus.FAILURE, ImportStatus.FAILURE]
		unknownHeaders.entries*.i18nError == [Messages.ERROR_UNKNOWN_COLUMN_HEADER, Messages.ERROR_UNKNOWN_COLUMN_HEADER]
		unknownHeaders.entries*.i18nImpact == [Messages.IMPACT_COLUMN_IGNORED, Messages.IMPACT_COLUMN_IGNORED]
		unknownHeaders.entries*.errorArgs == [["FAKE_COL"], ["ANOTHER_FAKE_COL"]]
	}


	def "should throw error when template is wrong"(){
		given :
		def exception
		Map<ColumnMismatch, Set<TemplateColumn>> columnsException = new HashMap<ColumnMismatch, Set<TemplateColumn>>()
		columnsException.put(mismatch, col)

		when :
		try {
			def parser = createParser(fileName)
		} catch (TemplateMismatchException tme){
			exception = tme
		}

		then :
		exception.worksheetFormatStatuses[0].columnMismatches == columnsException

		where :
		fileName                                            || mismatch                         | col
		"import/requirements/missing_mandatory_header.xls"  || ColumnMismatch.MISSING_MANDATORY | [RequirementSheetColumn.REQ_PATH] as Set
		"import/requirements/same_header_name.xls"          || ColumnMismatch.DUPLICATE         | [RequirementSheetColumn.REQ_PATH] as Set
	}


	def "should create RequirementVersionInstructions"(){

		given :
		def parser = createParser("import/requirements/dataset1.xls")

		when :
		LogTrain unknownHeaders = parser.logUnknownHeaders()
		def results = parser.getRequirementVersionInstructions()

		then :
		unknownHeaders.entries == []
		results*.milestones == [["Milestone 1", "Milestone 2"], ["Milestone 1"], []]
		results*.customFields == [
			[cuf4:"", cuf2:"false", cuf5:"", cuf3:"", cuf6:"", cuf1:"1"],
			[cuf4:"<p><strong>aze</strong></p>\n", cuf2:"true", cuf5:"2015-07-14", cuf3:"2", cuf6:"ah|hi|oh", cuf1:"1"],
			[cuf4:"<p><strong>aze</strong></p>\n", cuf2:"false", cuf5:"2015-07-14", cuf3:"1", cuf6:"ah|hi", cuf1:"1"]
		]

		results*.requirementVersion.reference == ["1", "1", "1"]
		results*.requirementVersion.criticality == [RequirementCriticality.CRITICAL, RequirementCriticality.MINOR, RequirementCriticality.CRITICAL]
		results*.requirementVersion.category.code == ["CAT_BUSINESS", "CAT_UNDEFINED", "CAT_SECURITY"]
		results*.requirementVersion.status == [RequirementStatus.WORK_IN_PROGRESS, RequirementStatus.WORK_IN_PROGRESS, RequirementStatus.WORK_IN_PROGRESS]
		results*.target.version == [1, 1, 2]
		results*.target.requirement.path == ["/Test Project-1/Test Folder 1/Mother", "/Test Project-1/Test Folder 1/Mother/Child", "/Test Project-1/Test Folder 1/Mother/Child"]
		results*.target.requirement.order == [0, 0, 0]
	}

	def "should create RequirementVersionInstructions and log errors"(){

		given :
		def parser = createParser("import/requirements/dataset2.xls")
		def emptyCF = [cuf4:null, cuf2:null, cuf5:null, cuf3:null, cuf6:null, cuf1:null]

		when :
		LogTrain unknownHeaders = parser.logUnknownHeaders()
		def results = parser.getRequirementVersionInstructions()

		then :
		unknownHeaders.entries == []
		results*.milestones == [["Milestone 1", "Milestone 2"], ["Milestone 1"], [], [], [], [], [], []]

		results*.customFields  == [
			[  cuf4:"", cuf2:"false", cuf5:"", cuf3:"", cuf6:"", cuf1:"1"],
			[cuf4:"<p><strong>aze</strong></p>\n", cuf2:"true", cuf5:"2015-07-14", cuf3:"2", cuf6:"ah|hi|oh", cuf1:"1"],
			[cuf4:"<p><strong>aze</strong></p>\n", cuf2:"false", cuf5:"2015-07-14", cuf3:"1", cuf6:"ah|hi", cuf1:"1"],
			emptyCF,
			emptyCF,
			emptyCF,
			emptyCF,
			emptyCF,
		]

		results*.requirementVersion.reference == (1..8).collect{v -> "1"}
		results*.requirementVersion.criticality == (1..8).collect{v ->  v == 2 ? RequirementCriticality.UNDEFINED: RequirementCriticality.CRITICAL}
		results*.requirementVersion.category.code == (1..8).collect{v -> "CAT_UNDEFINED"}
		results*.requirementVersion.status == (1..8).collect{v -> RequirementStatus.WORK_IN_PROGRESS}
		results*.requirementVersion.name == (1..8).collect{v ->   v == 1 ? "Mother" : "Child"}
		results*.target.version == [1, 1, 2, 3, 4, 0, 5, 6]
		results*.target.requirement.path == (1..8).collect{v ->   v == 1 ?  "/Test Project-1/Test Folder 1/Mother" : "/Test Project-1/Test Folder 1/Mother/Child"}
		results*.target.requirement.order == [0, 0, 0, 0, null, 0, 0, 0]
	}

	def "should create Coverage Instructions"(){

		given :
		def parser = createParser("import/requirements/dataset_with_coverage.xls")

		when :
		LogTrain unknownHeaders = parser.logUnknownHeaders()
		def results = parser.getCoverageInstructions()
		then :
		unknownHeaders.entries == []
		results*.target.reqPath == ["req1", "req1", "req1", "req2", "folder/req1"].collect{"/project/" +it}
		results*.target.reqVersion == [1, 2, 3, 1, 1]
		results*.target.tcPath == ["tc1", "tc1", "tc2", "tc3", "tc4"].collect{"/project/" +it}
	}
}
