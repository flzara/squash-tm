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
package org.squashtest.tm.service.internal.batchimport.testcase.excel;

import org.apache.poi.ss.usermodel.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.tm.domain.testcase.KeywordTestCase;
import org.squashtest.tm.domain.testcase.ScriptedTestCase;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.domain.testcase.TestCaseKind;
import org.squashtest.tm.service.internal.batchimport.TestCaseInstruction;
import org.squashtest.tm.service.internal.batchimport.TestCaseTarget;
import org.squashtest.tm.service.internal.batchimport.excel.CannotCoerceException;

import javax.validation.constraints.NotNull;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.squashtest.tm.domain.testcase.TestCaseKind.STANDARD;

/**
 * This builder creates {@link TestCaseTarget}s by reading workbook rows according to its {@link WorkbookMetaData}
 *
 * @author Gregory Fouquet
 *
 */
class TestCaseInstructionBuilder extends InstructionBuilder<TestCaseSheetColumn, TestCaseInstruction> {
	private static final Logger LOGGER = LoggerFactory.getLogger(TestCaseInstructionBuilder.class);

	public TestCaseInstructionBuilder(@NotNull WorksheetDef<TestCaseSheetColumn> worksheetDef) {
		super(worksheetDef);
	}

	/**
	 * @see org.squashtest.tm.service.internal.batchimport.testcase.excel.InstructionBuilder#createInstruction(org.apache.poi.ss.usermodel.Row)
	 */
	@Override
	protected TestCaseInstruction createInstruction(Row row) {
		StdColumnDef<TestCaseSheetColumn> columnDef = worksheetDef.getColumnDef(TestCaseSheetColumn.TC_KIND);
		TestCaseKind testCaseKind = null;
		if (columnDef != null) {
			try {
				testCaseKind = getValue(row, columnDef);
			} catch (CannotCoerceException cce) {
				String testCaseKinds = Arrays.stream(TestCaseKind.values()).map(Enum::toString).collect(Collectors.joining(","));
				LOGGER.debug("The value for TC_KIND does not exist for the corresponded enum. Authorized values are : {}. Then, default value STANDARD will be assigned.", testCaseKinds, cce);
			}
		}
		if (testCaseKind == null) {
			testCaseKind = STANDARD;
		}
		TestCase testCase;
		switch (testCaseKind) {
			case GHERKIN:
				testCase = getBlankScriptedTestCase(row);
				break;
			case KEYWORD:
				testCase = KeywordTestCase.createBlankKeywordTestCase();
				break;
			default:
				//SQUASH-1562
				testCase = TestCase.createBlankTestCase();
		}
		return new TestCaseInstruction(new TestCaseTarget(), testCase);
	}

	private ScriptedTestCase getBlankScriptedTestCase(Row row) {
		String script = getValue(row, worksheetDef.getColumnDef(TestCaseSheetColumn.TC_SCRIPT));
		return ScriptedTestCase.createBlankScriptedTestCase(script);
	}

	/**
	 * @see org.squashtest.tm.service.internal.batchimport.testcase.excel.InstructionBuilder#postProcessInstruction(org.apache.poi.ss.usermodel.Row, org.squashtest.tm.service.internal.batchimport.Instruction)
	 */
	@Override
	protected void postProcessInstruction(Row row, TestCaseInstruction instruction) {
		ignoreImportanceIfAuto(instruction);
	}

	private void ignoreImportanceIfAuto(TestCaseInstruction instruction) {
		TestCase testCase = instruction.getTestCase();
		if (testCase != null && testCase.isImportanceAuto() != null && testCase.isImportanceAuto()) {
			testCase.setImportance(TestCaseImportance.defaultValue());
		}
	}
}
