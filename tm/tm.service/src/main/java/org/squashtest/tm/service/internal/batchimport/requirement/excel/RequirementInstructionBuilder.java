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
package org.squashtest.tm.service.internal.batchimport.requirement.excel;

import org.apache.poi.ss.usermodel.Row;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.service.internal.batchimport.RequirementTarget;
import org.squashtest.tm.service.internal.batchimport.RequirementVersionInstruction;
import org.squashtest.tm.service.internal.batchimport.RequirementVersionTarget;
import org.squashtest.tm.service.internal.batchimport.testcase.excel.InstructionBuilder;
import org.squashtest.tm.service.internal.batchimport.testcase.excel.WorksheetDef;

public class RequirementInstructionBuilder extends InstructionBuilder<RequirementSheetColumn, RequirementVersionInstruction>{

	public RequirementInstructionBuilder(WorksheetDef<RequirementSheetColumn> worksheetDef) {
		super(worksheetDef);
	}

	@Override
	protected RequirementVersionInstruction createInstruction(Row row) {
		return new RequirementVersionInstruction(new RequirementVersionTarget(new RequirementTarget(), 0), new RequirementVersion());
	}



}
