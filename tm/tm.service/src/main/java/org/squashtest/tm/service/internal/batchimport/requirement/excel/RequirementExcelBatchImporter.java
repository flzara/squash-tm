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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;
import org.squashtest.tm.service.importer.EntityType;
import org.squashtest.tm.service.internal.batchimport.Instruction;
import org.squashtest.tm.service.internal.batchimport.excel.ExcelBatchImporter;
import org.squashtest.tm.service.internal.batchimport.testcase.excel.ExcelWorkbookParser;

@Component
public class RequirementExcelBatchImporter extends ExcelBatchImporter {

	private static final List<EntityType> REQ_ENTITIES_ORDERED_BY_INSTRUCTION_ORDER = Arrays
			.asList(EntityType.REQUIREMENT_VERSION,
			EntityType.COVERAGE, 
			EntityType.REQUIREMENT_LINK);

	@Override
	public List<Instruction<?>> findInstructionsByEntity(ExcelWorkbookParser parser, EntityType entityType) {
		List<Instruction<?>> instructions = new ArrayList<>();

		switch (entityType) {

		case REQUIREMENT_VERSION:
			instructions.addAll(parser.getRequirementVersionInstructions());
			break;
		case COVERAGE:
			instructions.addAll(parser.getCoverageInstructions());
			break;
		case REQUIREMENT_LINK:
			instructions.addAll(parser.getRequirementLinkgsInstruction());
			break;
		default:

		}
		return instructions;
	}

	@Override
	public List<EntityType> getEntityType() {
		return REQ_ENTITIES_ORDERED_BY_INSTRUCTION_ORDER;
	}

}
