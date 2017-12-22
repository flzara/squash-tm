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

import org.squashtest.tm.service.importer.ImportStatus;
import org.squashtest.tm.service.internal.batchimport.CallStepInstruction;
import org.squashtest.tm.service.internal.batchimport.CallStepParamsInfo;
import org.squashtest.tm.service.internal.batchimport.Messages;
import org.squashtest.tm.service.internal.batchimport.StepInstruction;
import org.squashtest.tm.service.internal.batchimport.excel.InvalidTargetException;
import org.squashtest.tm.service.internal.batchimport.excel.PropertySetter;

public class ParamAssignationModeSetter implements PropertySetter<CallStepParamsInfo, StepInstruction> {

	public static final ParamAssignationModeSetter INSTANCE = new ParamAssignationModeSetter();

	@Override
	public void set(CallStepParamsInfo value, StepInstruction target) {
		if (target instanceof CallStepInstruction){
			((CallStepInstruction)target).setDatasetInfo(value);
		}
		else if ( ! value.equals(CallStepParamsInfo.DEFAULT)){
			throw new InvalidTargetException(ImportStatus.WARNING, Messages.ERROR_ACTION_STEP_HAS_DATASET, Messages.IMPACT_COLUMN_IGNORED);
		}
	}

}
