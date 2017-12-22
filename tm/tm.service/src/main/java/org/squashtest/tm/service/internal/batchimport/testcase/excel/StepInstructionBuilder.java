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
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.service.internal.batchimport.ActionStepInstruction;
import org.squashtest.tm.service.internal.batchimport.CallStepInstruction;
import org.squashtest.tm.service.internal.batchimport.CallStepParamsInfo;
import org.squashtest.tm.service.internal.batchimport.StepInstruction;
import org.squashtest.tm.service.internal.batchimport.TestCaseTarget;
import org.squashtest.tm.service.internal.batchimport.TestStepTarget;

/**
 * @author Gregory Fouquet
 *
 */
class StepInstructionBuilder extends InstructionBuilder<StepSheetColumn, StepInstruction> {
	private final StdColumnDef<StepSheetColumn> stepTypeColDef;

	/**
	 * @param tcwd
	 */
	public StepInstructionBuilder(@NotNull WorksheetDef<StepSheetColumn> swd) {
		super(swd);
		stepTypeColDef = worksheetDef.getColumnDef(StepSheetColumn.TC_STEP_IS_CALL_STEP);
	}

	@Override
	protected StepInstruction createInstruction(Row row) {
		StepInstruction instruction;
		if (isActionStepRow(row)) {
			instruction = new ActionStepInstruction(new TestStepTarget(), ActionTestStep.createBlankActionStep());
		} else {
			instruction = new CallStepInstruction(new TestStepTarget(), new TestCaseTarget(), ActionTestStep.createBlankActionStep(), new CallStepParamsInfo());
		}
		return instruction;
	}

	/**
	 * @param row
	 * @return
	 */
	private boolean isActionStepRow(Row row) {
		if (stepTypeColDef == null) {
			return true;
		}
		Boolean callStep = getValue(row, stepTypeColDef);
		return callStep == null || !callStep;
	}

}
