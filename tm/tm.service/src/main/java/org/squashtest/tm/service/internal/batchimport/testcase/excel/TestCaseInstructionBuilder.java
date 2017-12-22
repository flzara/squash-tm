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

import javax.validation.constraints.NotNull;

import org.apache.poi.ss.usermodel.Row;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.service.internal.batchimport.TestCaseInstruction;
import org.squashtest.tm.service.internal.batchimport.TestCaseTarget;

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
		ignoreImportanceIfAuto(instruction);
	}

	private void ignoreImportanceIfAuto(TestCaseInstruction instruction) {
		TestCase testCase = instruction.getTestCase();
		if (testCase != null && testCase.isImportanceAuto() != null && testCase.isImportanceAuto()) {
			testCase.setImportance(TestCaseImportance.defaultValue());
		}
	}
}
