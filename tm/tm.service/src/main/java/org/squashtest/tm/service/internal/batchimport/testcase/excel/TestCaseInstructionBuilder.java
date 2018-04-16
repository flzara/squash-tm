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
import org.squashtest.tm.domain.testcase.*;
import org.squashtest.tm.service.internal.batchimport.TestCaseInstruction;
import org.squashtest.tm.service.internal.batchimport.TestCaseTarget;

import javax.validation.constraints.NotNull;

/**
 * This builder creates {@link TestCaseTarget}s by reading workbook rows according to its {@link WorkbookMetaData}
 *
 * @author Gregory Fouquet
 *
 */
class TestCaseInstructionBuilder extends InstructionBuilder<TestCaseSheetColumn, TestCaseInstruction> {
	public TestCaseInstructionBuilder(@NotNull WorksheetDef<TestCaseSheetColumn> worksheetDef) {
		super(worksheetDef);
	}

	/**
	 * @see org.squashtest.tm.service.internal.batchimport.testcase.excel.InstructionBuilder#createInstruction(org.apache.poi.ss.usermodel.Row)
	 */
	@Override
	protected TestCaseInstruction createInstruction(Row row) {
		return new TestCaseInstruction(new TestCaseTarget(), TestCase.createBlankTestCase());
	}

	/**
	 * @see org.squashtest.tm.service.internal.batchimport.testcase.excel.InstructionBuilder#postProcessInstruction(org.apache.poi.ss.usermodel.Row, org.squashtest.tm.service.internal.batchimport.Instruction)
	 */
	@Override
	protected void postProcessInstruction(Row row, TestCaseInstruction instruction) {
		createScriptedExtender(row, instruction);
		ignoreImportanceIfAuto(instruction);
	}

	/**
	 * Create a {@link ScriptedTestCaseExtender} for the {@link TestCase} if needed (ie if imported file provide the correct columns and values)
	 * @param row the excel row
	 * @param instruction the generated instruction
	 */
	private void createScriptedExtender(Row row, TestCaseInstruction instruction) {
		StdColumnDef<TestCaseSheetColumn> columnDef = worksheetDef.getColumnDef(TestCaseSheetColumn.TC_KIND);
		if (columnDef == null) {
			return;
		}
		TestCaseKind testCaseKind = getValue(row, columnDef);
		if (TestCaseKind.SCRIPTED.equals(testCaseKind)) {
			TestCase testCase = instruction.getTestCase();
			ScriptedTestCaseLanguage language = getValue(row, worksheetDef.getColumnDef(TestCaseSheetColumn.TC_SCRIPTING_LANGUAGE));
			ScriptedTestCaseExtender testCaseExtender = new ScriptedTestCaseExtender();
			testCaseExtender.setLanguage(language);
			testCaseExtender.setScript(getValue(row, worksheetDef.getColumnDef(TestCaseSheetColumn.TC_SCRIPT)));
			testCase.setKind(testCaseKind);
			testCase.setScriptedTestCaseExtender(testCaseExtender);
			testCaseExtender.setTestCase(testCase);
		}
	}

	private void ignoreImportanceIfAuto(TestCaseInstruction instruction) {
		TestCase testCase = instruction.getTestCase();
		if (testCase != null && testCase.isImportanceAuto() != null && testCase.isImportanceAuto()) {
			testCase.setImportance(TestCaseImportance.defaultValue());
		}
	}
}
