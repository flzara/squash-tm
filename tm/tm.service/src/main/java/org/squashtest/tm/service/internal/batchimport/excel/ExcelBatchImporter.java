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

public abstract class ExcelBatchImporter {

	@Inject
	private Provider<SimulationFacility> simulatorProvider;

	@Inject
	private Provider<FacilityImpl> facilityImplProvider;

	/*
		1/ The logger is set by constructor, check the subclasses.

		2/ It has protected scope so that subclasses can access it.

		3/ I need this logger to be accessible from the subclasses and I
		wasn't sure about having one single static property for receiving
		two loggers instances (one for the TestCase importer and the other
		one for Requirement importer). So I made the loggers non-static.
		I still use the static naming convention though.

	*/
	// NOSONAR see comment above
	protected final Logger LOGGER;


	protected ExcelBatchImporter(Logger logger){
		this.LOGGER = logger;
	}


	public ImportLog simulateImport(File excelFile) {
		LOGGER.debug("beginning import simulation");

		SimulationFacility simulator = simulatorProvider.get();

		LOGGER.trace("parsing excel file");
		ExcelWorkbookParser parser = ExcelWorkbookParser.createParser(excelFile);
		parser.parse().releaseResources();
		LOGGER.trace("parsing done");

		LogTrain unknownHeaders = parser.logUnknownHeaders();

		List<Instruction<?>> instructions = buildOrderedInstruction(parser);
		if (LOGGER.isTraceEnabled()){
			LOGGER.trace("{} instructions created", instructions.size());
		}


		LOGGER.trace("running import simulation");
		ImportLog importLog = run(instructions, simulator);

		importLog.appendLogTrain(unknownHeaders);

		LOGGER.trace("done");
		return importLog;

	}

	@CacheScope
	public ImportLog performImport(File excelFile) {
		LOGGER.debug("beginning import");

		FacilityImpl impl = facilityImplProvider.get();

		LOGGER.trace("parsing excel file");
		ExcelWorkbookParser parser = ExcelWorkbookParser.createParser(excelFile);
		parser.parse().releaseResources();
		LOGGER.trace("parsing done");

		LogTrain unknownHeaders = parser.logUnknownHeaders();

		List<Instruction<?>> instructions = buildOrderedInstruction(parser);
		if (LOGGER.isTraceEnabled()){
			LOGGER.trace("{} instructions created", instructions.size());
		}

		LOGGER.trace("running import");
		ImportLog importLog = run(instructions, impl);

		LOGGER.trace("post processing");
		impl.postprocess(instructions);

		importLog.appendLogTrain(unknownHeaders);

		LOGGER.trace("done");
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
			if (LOGGER.isTraceEnabled()) {
				LOGGER.debug("execution of instruction : (line {}) {} '{}'", instruction.getLine(), instruction.getMode(), instruction.getTarget());
			}
			LogTrain logs = instruction.execute(facility);
			LOGGER.debug("completed execution of instruction : (line {}) {} '{}'", instruction.getLine(), instruction.getMode(), instruction.getTarget());

			if (logs.hasNoErrorWhatsoever()) {
				LOGGER.debug("no errors");
				logs.addEntry(LogEntry.ok().forTarget(instruction.getTarget()).build());
			}
			else{
				LOGGER.debug("some errors where raised and will be reported to the user");
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
			LOGGER.debug("building instructions for entity type : '{}'", entity);

			List<Instruction<?>> entityInstructions = findInstructionsByEntity(parser, entity);

			LOGGER.trace("found {} instructions for entity type '{}'", entityInstructions.size(), entity);

			instructions.addAll(entityInstructions);
		}
		return instructions;
	}

	public abstract List<EntityType> getEntityType();

	public abstract List<Instruction<?>> findInstructionsByEntity(ExcelWorkbookParser parser, EntityType entityType);

}
