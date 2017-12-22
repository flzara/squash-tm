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

import static org.squashtest.tm.service.internal.batchimport.testcase.excel.TemplateWorksheet.COVERAGE_SHEET;
import static org.squashtest.tm.service.internal.batchimport.testcase.excel.TemplateWorksheet.DATASETS_SHEET;
import static org.squashtest.tm.service.internal.batchimport.testcase.excel.TemplateWorksheet.DATASET_PARAM_VALUES_SHEET;
import static org.squashtest.tm.service.internal.batchimport.testcase.excel.TemplateWorksheet.PARAMETERS_SHEET;
import static org.squashtest.tm.service.internal.batchimport.testcase.excel.TemplateWorksheet.REQUIREMENT_SHEET;
import static org.squashtest.tm.service.internal.batchimport.testcase.excel.TemplateWorksheet.STEPS_SHEET;
import static org.squashtest.tm.service.internal.batchimport.testcase.excel.TemplateWorksheet.TEST_CASES_SHEET;
import static org.squashtest.tm.service.internal.batchimport.testcase.excel.TemplateWorksheet.REQUIREMENT_LINKS_SHEET;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.squashtest.tm.exception.SheetCorruptedException;
import org.squashtest.tm.service.batchimport.excel.TemplateMismatchException;
import org.squashtest.tm.service.importer.LogEntry;
import org.squashtest.tm.service.importer.Target;
import org.squashtest.tm.service.internal.batchimport.DatasetParamValueInstruction;
import org.squashtest.tm.service.internal.batchimport.DatasetTarget;
import org.squashtest.tm.service.internal.batchimport.Instruction;
import org.squashtest.tm.service.internal.batchimport.LogTrain;
import org.squashtest.tm.service.internal.batchimport.Messages;
import org.squashtest.tm.service.internal.batchimport.ParameterInstruction;
import org.squashtest.tm.service.internal.batchimport.ParameterTarget;
import org.squashtest.tm.service.internal.batchimport.RequirementLinkInstruction;
import org.squashtest.tm.service.internal.batchimport.RequirementLinkTarget;
import org.squashtest.tm.service.internal.batchimport.RequirementTarget;
import org.squashtest.tm.service.internal.batchimport.RequirementVersionInstruction;
import org.squashtest.tm.service.internal.batchimport.StepInstruction;
import org.squashtest.tm.service.internal.batchimport.TestCaseInstruction;
import org.squashtest.tm.service.internal.batchimport.TestCaseTarget;
import org.squashtest.tm.service.internal.batchimport.TestStepTarget;
import org.squashtest.tm.service.internal.batchimport.requirement.excel.RequirementInstructionBuilder;
import org.squashtest.tm.service.internal.batchimport.requirement.excel.RequirementLinkInstructionBuilder;
import org.squashtest.tm.service.internal.batchimport.requirement.excel.RequirementLinksSheetColumn;
import org.squashtest.tm.service.internal.batchimport.requirement.excel.RequirementSheetColumn;

/**
 * <p>
 * Parses an excel import workbook and creates instructions.
 * </p>
 *
 * <p>
 * Usage :
 *
 * <pre>
 * {
 * 	&#064;code
 * 	ExcelWorkbookParser parser = ExcelWorkbookParser.createParser(xlsxFile);
 * 	parser.parse().releaseResources();
 * 	List&lt;Instructions&gt; instructions = parser.getInstructions();
 * }
 * </pre>
 *
 * </p>
 *
 * @author Gregory Fouquet
 *
 */
public class ExcelWorkbookParser {
	private static final Logger LOGGER = LoggerFactory.getLogger(ExcelWorkbookParser.class);

	@Inject
	@Value("${uploadfilter.upload.import.maxLinesPerSheetForExcelImport:100}")
	private int maxLines;



	private Workbook workbook;
	private final WorkbookMetaData wmd;

	private final Map<TemplateWorksheet, List<Instruction<?>>> instructionsByWorksheet = new EnumMap<>(
		TemplateWorksheet.class);
	private final Map<TemplateWorksheet, Factory<?>> instructionBuilderFactoryByWorksheet = new EnumMap<>(
		TemplateWorksheet.class);

	/**
	 * Should be used by ExcelWorkbookParserBuilder only.
	 *
	 * @param workbook
	 * @param wmd
	 */
	ExcelWorkbookParser(@NotNull Workbook workbook, @NotNull WorkbookMetaData wmd) {
		super();
		this.workbook = workbook;
		this.wmd = wmd;

		instructionsByWorksheet.put(TEST_CASES_SHEET, new ArrayList<Instruction<?>>());
		instructionsByWorksheet.put(STEPS_SHEET, new ArrayList<Instruction<?>>());
		instructionsByWorksheet.put(PARAMETERS_SHEET, new ArrayList<Instruction<?>>());
		instructionsByWorksheet.put(DATASETS_SHEET, new ArrayList<Instruction<?>>());
		instructionsByWorksheet.put(DATASET_PARAM_VALUES_SHEET, new ArrayList<Instruction<?>>());
		instructionsByWorksheet.put(REQUIREMENT_SHEET, new ArrayList<Instruction<?>>());
		instructionsByWorksheet.put(COVERAGE_SHEET, new ArrayList<Instruction<?>>());
		instructionsByWorksheet.put(REQUIREMENT_LINKS_SHEET, new ArrayList<Instruction<?>>());

		instructionBuilderFactoryByWorksheet.put(REQUIREMENT_SHEET, new Factory<RequirementSheetColumn>(){

			@Override
			public InstructionBuilder<?, ?> create(WorksheetDef<RequirementSheetColumn> wd) {
				return new RequirementInstructionBuilder(wd);
			}

		});

		instructionBuilderFactoryByWorksheet.put(TEST_CASES_SHEET, new Factory<TestCaseSheetColumn>() {
			@Override
			public InstructionBuilder<?, ?> create(WorksheetDef<TestCaseSheetColumn> wd) {
				return new TestCaseInstructionBuilder(wd);
			}
		});
		instructionBuilderFactoryByWorksheet.put(STEPS_SHEET, new Factory<StepSheetColumn>() {
			@Override
			public InstructionBuilder<?, ?> create(WorksheetDef<StepSheetColumn> wd) {
				return new StepInstructionBuilder(wd);
			}

		});
		instructionBuilderFactoryByWorksheet.put(PARAMETERS_SHEET, new Factory<ParameterSheetColumn>() {
			@Override
			public InstructionBuilder<?, ?> create(WorksheetDef<ParameterSheetColumn> wd) {
				return new ParameterInstructionBuilder(wd);
			}

		});
		instructionBuilderFactoryByWorksheet.put(DATASETS_SHEET, new Factory<DatasetSheetColumn>() {
			@Override
			public InstructionBuilder<?, ?> create(WorksheetDef<DatasetSheetColumn> wd) {
				return new DatasetInstructionBuilder(wd);
			}

		});
		instructionBuilderFactoryByWorksheet.put(DATASET_PARAM_VALUES_SHEET, new Factory<DatasetParamValuesSheetColumn>() {
			@Override
			public InstructionBuilder<?, ?> create(WorksheetDef<DatasetParamValuesSheetColumn> wd) {
				return new DatasetParamValueInstructionBuilder(wd);
			}

		});

		instructionBuilderFactoryByWorksheet.put(COVERAGE_SHEET, new Factory<CoverageSheetColumn>(){

			@Override
			public InstructionBuilder<?, ?> create(WorksheetDef<CoverageSheetColumn> wd) {
				return new CoverageInstructionBuilder(wd);
			}

		});
		
		instructionBuilderFactoryByWorksheet.put(REQUIREMENT_LINKS_SHEET, new Factory<RequirementLinksSheetColumn>(){
			@Override
			public InstructionBuilder<?, ?> create(WorksheetDef<RequirementLinksSheetColumn> wd) {
				return new RequirementLinkInstructionBuilder(wd);
			}
		});

	}

	public LogTrain logUnknownHeaders(){

		LogTrain logs = new LogTrain();

		for (WorksheetDef<?> wd : wmd.getWorksheetDefs()) {
			Collection<UnknownColumnDef> unknowns = wd.getUnknownColumns();
			for (UnknownColumnDef unknown : unknowns){
				LogEntry.Builder builder = LogEntry.failure()
						.atLine(0)
						.forTarget(createDummyTarget(wd))
						.withMessage(Messages.ERROR_UNKNOWN_COLUMN_HEADER, unknown.getHeader())
						.withImpact(Messages.IMPACT_COLUMN_IGNORED, (Object[])null);
				logs.addEntry(builder.build());
			}
		}

		return logs;

	}

	// quick and dirty. LogEntries need a target because ImportLog sorts the entries by the EntityType
	// of the target of the log entry.
	private Target createDummyTarget(WorksheetDef<?> def){
		Target target;
		switch (def.getWorksheetType()){
		case TEST_CASES_SHEET :
			target = new TestCaseTarget();
			break;

		case STEPS_SHEET :
			target = new TestStepTarget();
			break;

		case DATASET_PARAM_VALUES_SHEET :
		case DATASETS_SHEET:
			target = new DatasetTarget();
			break;

		case PARAMETERS_SHEET :
			target = new ParameterTarget();
			break;

		case REQUIREMENT_SHEET :
			target = new RequirementTarget();
			break;

		case COVERAGE_SHEET:
			target = new CoverageTarget();
			break;
			
		case REQUIREMENT_LINKS_SHEET :
			target = new RequirementLinkTarget();
			break;

		default : throw new IllegalArgumentException("sheet '"+def.getSheetName()+"' is unknown and contains errors in its column headers");
		}
		return target;
	}

	/**
	 * Parses the file and creates instructions accordingly.
	 *
	 * @return this
	 */
	public ExcelWorkbookParser parse() {
		LOGGER.info("Parsing test-cases excel workbook {}", workbook);

		if (workbook == null) {
			throw new IllegalStateException(
					"No workbook available for parsing. Maybe you released this parser's resources by mistake.");
		}

		for (WorksheetDef<?> wd : wmd.getWorksheetDefs()) {
			processWorksheet(wd);
		}

		LOGGER.debug("Done parsing test-cases workbook");

		return this;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void processWorksheet(WorksheetDef<?> worksheetDef) {
		LOGGER.debug("Processing worksheet {}", worksheetDef.getWorksheetType());

		Sheet sheet = workbook.getSheet(worksheetDef.getSheetName());

		InstructionBuilder<?, ?> instructionBuilder = instructionBuilderFactoryByWorksheet.get(
				worksheetDef.getWorksheetType()).create((WorksheetDef) worksheetDef); // useless (WorksheetDef) cast
		// required for compiler not to whine

		for (int i = 1; i <= sheet.getLastRowNum(); i++) {
			LOGGER.trace("Creating instruction for row {}", i);
			Row row = sheet.getRow(i);
			if (! isEmpty(row)) {
				Instruction instruction = instructionBuilder.build(row);
				instructionsByWorksheet.get(worksheetDef.getWorksheetType()).add(instruction);
			}

		}
	}

	/**
	 * Releases resources held by this parser. The result of parsing is still available but the {@link #parse()} method
	 * should no longer be called.
	 *
	 * @return this
	 */
	public ExcelWorkbookParser releaseResources() {
		// as per POI doc : workbook resources are released upon GC
		workbook = null;
		return this;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<TestCaseInstruction> getTestCaseInstructions() {
		return (List) instructionsByWorksheet.get(TEST_CASES_SHEET); // useless (List) cast required for compiler not to
		// whine
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<StepInstruction> getTestStepInstructions() {
		return (List) instructionsByWorksheet.get(STEPS_SHEET); // useless (List) cast required for compiler not to
		// whine
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<ParameterInstruction> getParameterInstructions() {
		return (List) instructionsByWorksheet.get(PARAMETERS_SHEET); // useless (List) cast required for compiler not to
		// whine
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<DatasetParamValueInstruction> getDatasetInstructions() {
		return (List) instructionsByWorksheet.get(DATASETS_SHEET); // useless (List) cast required for compiler not to
		// whine
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<DatasetParamValueInstruction> getDatasetParamValuesInstructions() {
		return (List) instructionsByWorksheet.get(DATASET_PARAM_VALUES_SHEET); // useless (List) cast required for compiler not to
		// whine
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<RequirementVersionInstruction> getRequirementVersionInstructions(){
		return (List) instructionsByWorksheet.get(REQUIREMENT_SHEET);// useless (List) cast required for compiler not to
		// whine
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<RequirementVersionInstruction> getCoverageInstructions() {
		return (List) instructionsByWorksheet.get(COVERAGE_SHEET);// useless (List) cast required for compiler not to
		// whine
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<RequirementLinkInstruction> getRequirementLinkgsInstruction() {
		return (List) instructionsByWorksheet.get(REQUIREMENT_LINKS_SHEET);// useless (List) cast required for compiler not to
		// whine
	}
	
	
	public boolean isEmpty(Row row) {
		boolean isEmpty = true;

		if (row != null) {
			Iterator<Cell> iterator = row.cellIterator();

			while (iterator.hasNext()) {
				Cell c = iterator.next();
				if (!StringUtils.isBlank(c.toString())) {
					isEmpty = false;
					break;
				}
			}
		}

		return isEmpty;
	}
	
	


	/**
	 * Can create an {@link InstructionBuilder} for a given {@link WorksheetDef<C>}
	 * @param <C> a TemplateColumn
	 */
	private static interface Factory<C extends Enum<C> & TemplateColumn> {
		InstructionBuilder<?, ?> create(WorksheetDef<C> wd);
	}

	/**
	 * Factory method which should be used to create a parser.
	 *
	 * @param xls
	 * @return
	 * @throws SheetCorruptedException
	 *             when the excel file is unreadable
	 * @throws TemplateMismatchException
	 *             when the workbook does not match the template in an unrecoverable way.
	 */
	public static final ExcelWorkbookParser createParser(File xls) throws SheetCorruptedException,
	TemplateMismatchException {
		return new ExcelWorkbookParserBuilder(xls).build();
	}

}
