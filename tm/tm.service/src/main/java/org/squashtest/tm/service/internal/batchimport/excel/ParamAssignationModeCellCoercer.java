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
package org.squashtest.tm.service.internal.batchimport.excel;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.squashtest.tm.domain.testcase.ParameterAssignationMode;
import org.squashtest.tm.service.importer.ImportMode;
import org.squashtest.tm.service.internal.batchimport.CallStepParamsInfo;

/**
 * This class will read from a String how a call step should handle the downstream parameters. Currently this is used for
 * the column TC_STEP_CALL_DATASET in sheet TEST_STEP only.
 * 
 * @author bsiri
 *
 */
public class ParamAssignationModeCellCoercer extends TypeBasedCellValueCoercer<CallStepParamsInfo>{

	private static final String STR_DELEGATE_MODE = "INHERIT";
	public static final ParamAssignationModeCellCoercer INSTANCE = new ParamAssignationModeCellCoercer();

	/**
	 * Blank cell means default value means {@link ImportMode#UPDATE}.
	 * 
	 * @see org.squashtest.tm.service.internal.batchimport.excel.TypeBasedCellValueCoercer#coerceBlankCell(org.apache.poi.ss.usermodel.Cell)
	 */
	@Override
	protected CallStepParamsInfo coerceBlankCell(Cell cell) {
		return CallStepParamsInfo.DEFAULT;
	}

	/**
	 * @see org.squashtest.tm.service.internal.batchimport.excel.TypeBasedCellValueCoercer#coerceStringCell(org.apache.poi.ss.usermodel.Cell)
	 */
	@Override
	protected CallStepParamsInfo coerceStringCell(Cell cell) {
		String val = cell.getStringCellValue();
		CallStepParamsInfo infos;

		if (StringUtils.isBlank(val)){
			infos = new CallStepParamsInfo(null, ParameterAssignationMode.NOTHING);
		}
		else if (STR_DELEGATE_MODE.equals(val)){
			infos = new CallStepParamsInfo(null, ParameterAssignationMode.DELEGATE);
		}
		else{
			infos = new CallStepParamsInfo(val, ParameterAssignationMode.CALLED_DATASET);
		}

		return infos;
	}

}
