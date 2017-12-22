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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.squashtest.tm.service.annotation.CacheScope;
import org.squashtest.tm.service.importer.EntityType;
import org.squashtest.tm.service.importer.ImportLog;
import org.squashtest.tm.service.importer.LogEntry;
import org.squashtest.tm.service.internal.batchimport.*;
import org.squashtest.tm.service.internal.batchimport.testcase.excel.ExcelWorkbookParser;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component
public abstract class ExcelBatchImporter {
	@Inject
	private Provider<SimulationFacility> simulatorProvider;

	@Inject
	private Provider<FacilityImpl> facilityImplProvider;

	private static final Logger LOGGER = LoggerFactory.getLogger(TestCaseExcelBatchImporter.class);

	public ImportLog simulateImport(File excelFile) {

		SimulationFacility simulator = simulatorProvider.get();

		ExcelWorkbookParser parser = ExcelWorkbookParser.createParser(excelFile);
		parser.parse().releaseResources();

		LogTrain unknownHeaders = parser.logUnknownHeaders();

		List<Instruction<?>> instructions = buildOrderedInstruction(parser);
		ImportLog importLog = run(instructions, simulator);

		importLog.appendLogTrain(unknownHeaders);
		return importLog;

	}

	@CacheScope
	public ImportLog performImport(File excelFile) {

		FacilityImpl impl = facilityImplProvider.get();

		ExcelWorkbookParser parser = ExcelWorkbookParser.createParser(excelFile);
		parser.parse().releaseResources();

		LogTrain unknownHeaders = parser.logUnknownHeaders();

		List<Instruction<?>> instructions = buildOrderedInstruction(parser);

		ImportLog importLog = run(instructions, impl);

		impl.postprocess(instructions);

		importLog.appendLogTrain(unknownHeaders);
		return importLog;

	}

	/*
	 *
	 * Feat 3695 :
	 *
	 * an additional step is required now that DATASET and PArameter values are processed separately : we still need to
	 * merge their logs.
	 *
	 */
	private ImportLog run(List<Instruction<?>> instructions, Facility facility) {

		ImportLog importLog = new ImportLog();

		for (Instruction<?> instruction : instructions) {
			LogTrain logs = instruction.execute(facility);

			if (logs.hasNoErrorWhatsoever()) {
				logs.addEntry(LogEntry.ok().forTarget(instruction.getTarget()).build());
			}

			logs.setForAll(instruction.getMode());
			logs.setForAll(instruction.getLine());

			importLog.appendLogTrain(logs);
		}

		// Feat 3695
		importLog.packLogs();

		return importLog;
	}

	public List<Instruction<?>> buildOrderedInstruction(ExcelWorkbookParser parser) {
		List<Instruction<?>> instructions = new ArrayList<>();
		for (EntityType entity : getEntityType()) {
			List<Instruction<?>> entityInstructions = findInstructionsByEntity(parser, entity);
			instructions.addAll(entityInstructions);
		}
		return instructions;
	}

	public abstract List<EntityType> getEntityType();

	public abstract List<Instruction<?>> findInstructionsByEntity(ExcelWorkbookParser parser, EntityType entityType);

}
