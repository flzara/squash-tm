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

import static org.squashtest.tm.service.importer.ImportMode.*

import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import org.squashtest.tm.core.foundation.lang.DateUtils
import org.squashtest.tm.exception.SheetCorruptedException
import org.squashtest.tm.service.batchimport.excel.TemplateMismatchException
import org.squashtest.tm.service.internal.batchimport.CallStepInstruction

import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Gregory Fouquet
 *
 */
class ExcelWorkbookParserTest extends Specification {
	@Unroll
	def "should create a parser for correct excel file #file"() {
		given:
		Resource xls = new ClassPathResource("batchimport/testcase/" + file)

		expect:
		ExcelWorkbookParser.createParser(xls.file)

		where:
		file << [
			"import-2269.xlsx",
			"ignored-headers.xlsx"
		]
	}

	@Unroll
	def "should raise exception #exception for corrupted sheet #file "() {
		given:
		Resource xls = new ClassPathResource(file)

		when:
		ExcelWorkbookParser.createParser(xls.file)

		then:
		thrown(exception)

		where:
		file										| exception
		"batchimport/testcase/garbage-file.xlsx"	| SheetCorruptedException
		"batchimport/testcase/no-header.xlsx"		| TemplateMismatchException // should be refined
		"batchimport/testcase/missing-headers.xlsx"	| TemplateMismatchException // should be refined
		//TODO		"batchimport/testcase/duplicate-ws.xlsx" | DuplicateWorksheetException
	}

	def "should parse file and createinstructions"() {
		given:
		Resource xls = new ClassPathResource("batchimport/testcase/import-2269.xlsx")

		and:
		ExcelWorkbookParser parser = ExcelWorkbookParser.createParser(xls.file)

		when:
		parser.parse().releaseResources()

		and:
		def testCaseInstructions = parser.getTestCaseInstructions()

		and:
		def testCasePaths = (1..8).collect { "path/row$it" }
		def testCaseNums = (1..8).collect { it + (10-1) }	// minus 1 because indexes in the file are one-based
		def testCaseRefs = (1..8).collect { "ref$it" }
		def testCaseNames = (1..8).collect { "name$it" }
		def testCasePres = (1..8).collect { "pre$it" }
		def testCaseDescs = (1..8).collect { "desc$it" }
		def testCaseCreators = (1..8).collect { "creator$it" }
		def testCaseCreateds = (1..8).collect { "2003-02-0$it" }
		def testCaseActions = [
			CREATE,
			CREATE,
			UPDATE,
			UPDATE,
			DELETE,
			DELETE,
			UPDATE,
			UPDATE
		]

		// TODO add tests on milestones
		// TODO add tests on cufs
		// TODO add error case on enums
		// TODO add string nums
		// TODO add string dates

		then:
		testCaseInstructions*.target.path == testCasePaths
		testCaseInstructions*.target.order == testCaseNums
		testCaseInstructions*.testCase.reference == testCaseRefs
		testCaseInstructions*.testCase.name == testCaseNames
		testCaseInstructions*.testCase.prerequisite == testCasePres
		testCaseInstructions*.testCase.description == testCaseDescs
		testCaseInstructions.collect { DateUtils.formatIso8601Date(it.testCase.createdOn) }  == testCaseCreateds
		testCaseInstructions*.testCase.createdBy == testCaseCreators
		testCaseInstructions*.mode == testCaseActions

		when:
		def stepInstructions = parser.getTestStepInstructions()


		and:
		def stepPaths = (1..8).collect { "owner/path/$it/steps/"+(it+9) }
		def stepNums = (1..8).collect { it + 9 } // indexes are 0-based while 1-based in xls
		def stepActions = (1..8).collect { "action$it" }
		def stepReactions = (1..8).collect { "result$it" }
		def stepModes = [
			CREATE,
			CREATE,
			UPDATE,
			UPDATE,
			DELETE,
			DELETE,
			UPDATE,
			UPDATE
		]

		then:
		stepInstructions*.target.path == stepPaths
		stepInstructions*.target.index == stepNums
		stepInstructions*.testStep.action == stepActions
		stepInstructions*.testStep.expectedResult == stepReactions
		stepInstructions*.mode == stepModes

		when:
		def paramInstructions = parser.getParameterInstructions()

		and:
		def paramPaths = (1..8).collect { "owner/path/$it/parameters/null" }
		def paramNames = (1..8).collect { "name$it" }
		def paramDescs = (1..8).collect { "desc$it" }
		def paramActions = [
			CREATE,
			CREATE,
			UPDATE,
			UPDATE,
			DELETE,
			DELETE,
			UPDATE,
			UPDATE
		]

		then:
		paramInstructions*.target.path == paramPaths
		paramInstructions*.parameter.name == paramNames
		paramInstructions*.parameter.description == paramDescs
		paramInstructions*.mode == paramActions

		when:
		def datasetInstructions = parser.getDatasetInstructions()

		and:
		def datasetPaths = (1..8).collect { "owner/path/$it/datasets/name$it" }
		def datasetParamOwnerPaths = (1..8).collect { "param/owner/path/$it" }
		def datasetNames = (1..8).collect { "name$it" }
		def datasetParamNames = (1..8).collect { "paramName$it" }
		def datasetValues = (1..8).collect { "value$it" }
		def datasetActions = [
			CREATE,
			CREATE,
			UPDATE,
			UPDATE,
			DELETE,
			DELETE,
			UPDATE,
			UPDATE
		]

		then:
		datasetInstructions*.target.path == datasetPaths
		datasetInstructions*.target.name == datasetNames
		datasetInstructions*.mode == datasetActions

		when:
		def datasetParamValuesInstructions = parser.getDatasetParamValuesInstructions()

		and:
		def datasetParamValuesPaths = (1..8).collect { "owner/path/$it/datasets/name$it" }
		def datasetParamValuesParamOwnerPaths = (1..8).collect { "param/owner/path/$it" }
		def datasetParamValuesNames = (1..8).collect { "name$it" }
		def datasetParamValuesParamNames = (1..8).collect { "paramName$it" }
		def datasetParamValuesValues = (1..8).collect { "value$it" }
		def datasetParamValuesActions = [
			CREATE,
			CREATE,
			UPDATE,
			UPDATE,
			DELETE,
			DELETE,
			UPDATE,
			UPDATE
		]

		then:
		datasetParamValuesInstructions*.target.path == datasetPaths
		datasetParamValuesInstructions*.target.name == datasetNames
		datasetParamValuesInstructions*.datasetValue.parameterOwnerPath == datasetParamOwnerPaths
		datasetParamValuesInstructions*.datasetValue.parameterName == datasetParamNames
		datasetParamValuesInstructions*.datasetValue.value == datasetValues
		datasetParamValuesInstructions*.mode == datasetActions

	}

	def "should parse file call step instructions"() {
		given:
		Resource xls = new ClassPathResource("batchimport/testcase/call-steps.xlsx")

		and:
		ExcelWorkbookParser parser = ExcelWorkbookParser.createParser(xls.file)

		when:
		parser.parse().releaseResources()
		def stepInstructions = parser.getTestStepInstructions().findAll { it instanceof CallStepInstruction }

		and:
		def stepPaths = (1..3).collect { "owner/path/$it/steps/null" }
		def stepActions = (1..3).collect { "/path/$it" }

		then:
		stepInstructions*.target.path == stepPaths
		stepInstructions*.calledTC.path == stepActions
	}

	def "should not break on phantom (null) cells"() {
		given:
		Resource xls = new ClassPathResource("batchimport/testcase/phantom-cells.xlsx")

		and:
		ExcelWorkbookParser parser = ExcelWorkbookParser.createParser(xls.file)

		when:
		parser.parse().releaseResources()
		def instructions = parser.getTestCaseInstructions()

		and:
		def paths = ["null-action-cell", null]

		then:
		instructions*.target.path == paths
	}
}
