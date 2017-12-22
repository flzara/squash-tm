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

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.tm.service.importer.ImportStatus;
import org.squashtest.tm.service.internal.batchimport.CustomFieldHolder;
import org.squashtest.tm.service.internal.batchimport.Instruction;
import org.squashtest.tm.service.internal.batchimport.Messages;
import org.squashtest.tm.service.internal.batchimport.excel.CannotCoerceException;
import org.squashtest.tm.service.internal.batchimport.excel.InvalidTargetException;
import org.squashtest.tm.service.internal.batchimport.excel.NullMandatoryValueException;
import org.squashtest.tm.service.internal.batchimport.excel.PropertySetter;

/**
 * Generic superclass for instruction builders.
 *
 * @author Gregory Fouquet
 *
 */
public abstract class InstructionBuilder<COL extends Enum<COL> & TemplateColumn, INST extends Instruction<?>> {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	protected final CellValueCoercerRepository<COL> coercerRepository;
	protected final PropertyHolderFinderRepository<COL> propHolderFinderRepository;
	protected final PropertySetterRepository<COL> propertySetterRepository;
	protected final WorksheetDef<COL> worksheetDef;

	/**
	 *
	 */
	public InstructionBuilder(WorksheetDef<COL> worksheetDef) {
		super();
		this.worksheetDef = worksheetDef;

		TemplateWorksheet ws = worksheetDef.getWorksheetType();
		coercerRepository = CellValueCoercerRepository.forWorksheet(ws);
		propHolderFinderRepository = PropertyHolderFinderRepository.forWorksheet(ws);
		propertySetterRepository = PropertySetterRepository.forWorksheet(ws);
	}

	protected abstract INST createInstruction(Row row);

	/**
	 * Template method : instruction building can be customized in subclasses by
	 * overriding {@link #postProcessInstruction(Row, Instruction)}
	 *
	 * @param row
	 * @return
	 */
	public final INST build(Row row) {
		INST instruction = createInstruction(row);
		instruction.setLine(row.getRowNum());

		processStandardColumns(row, instruction);

		if (instruction instanceof CustomFieldHolder) {
			processCustomFieldColumns(row, instruction);
		}

		postProcessInstruction(row, instruction);

		return instruction;
	}

	/**
	 * Override this method in subclass to postprocess an instruction. Default
	 * implementation does nothing.
	 *
	 * @param row
	 * @param instruction
	 */
	protected void postProcessInstruction(Row row, INST instruction) {
		// NOOP;
	}

	private void processCustomFieldColumns(Row row, INST instruction) {
		for (CustomFieldColumnDef colDef : worksheetDef.getCustomFieldDefs()) {
			processCustomFieldColumn(row, colDef, instruction);
		}
	}

	private void processCustomFieldColumn(Row row, CustomFieldColumnDef colDef, INST instruction) {
		try {
			String value = getValue(row, colDef);
			((CustomFieldHolder) instruction).addCustomField(colDef.getCode(), value);

		} catch (CannotCoerceException e) {
			log(colDef, e, instruction);
		}
	}

	private void processStandardColumns(Row row, INST instruction) {
		for (StdColumnDef<COL> colDef : worksheetDef.getImportableColumnDefs()) {
			processStandardColumn(row, colDef, instruction);
		}
	}

	private void processStandardColumn(Row row, StdColumnDef<COL> colDef, INST instruction) {
		logger.trace("Parsing column {} of type {}", colDef.getIndex(), colDef.getType());

		COL col = colDef.getType();
		Object value = null;

		try {
			value = getValue(row, colDef);
		} catch (CannotCoerceException cce) {
			log(colDef, cce, instruction);

		}

		Object target = propHolderFinderRepository.findPropertyHolderFinder(col).find(instruction);
		PropertySetter<Object, Object> propSetter = propertySetterRepository.findPropSetter(col);

		try {
			propSetter.set(value, target);

		} catch (NullMandatoryValueException e) { // NOSONAR : this exception is part of the nominal use case
			log(colDef, instruction);
		} catch (InvalidTargetException e) { // NOSONAR : this exception is part of the nominal use case
			instruction.addLogEntry(e.getStatus(), e.getErrori18nMessage(), e.getImpacti18nMessage());
		}
	}

	/**
	 * @param colDef
	 * @param e
	 * @param instruction
	 */
	private void log(ColumnDef colDef, INST instruction) {
		instruction.addLogEntry(ImportStatus.FAILURE, Messages.ERROR_FIELD_MANDATORY, null, colDef.getHeader());

	}

	/**
	 * @param colDef
	 * @param e
	 */
	private void log(ColumnDef colDef, CannotCoerceException e, INST instruction) {
		String impactKey = null;
		ImportStatus status = ImportStatus.FAILURE;

		if (!colDef.is(ColumnProcessingMode.MANDATORY)) {
			status = ImportStatus.WARNING;
			impactKey = e.getImpactI18nKey(instruction.getMode());
		}

		instruction.addLogEntry(status, e.errorI18nKey, impactKey, colDef.getHeader());

	}

	/**
	 * Returns the asked cell
	 *
	 * @param row
	 * @param colDef
	 * @return the cell or null when the cell does not exist
	 */
	protected final Cell getCell(Row row, ColumnDef colDef) {
		return row.getCell(colDef.getIndex(), Row.CREATE_NULL_AS_BLANK);
	}

	@SuppressWarnings("unchecked")
	protected final <VAL> VAL getValue(Row row, StdColumnDef<COL> colDef) {
		Cell cell = getCell(row, colDef);
		return (VAL) coercerRepository.findCoercer(colDef.getType()).coerce(cell);
	}

	protected final String getValue(Row row, CustomFieldColumnDef colDef) {
		Cell cell = getCell(row, colDef);
		return coercerRepository.findCustomFieldCoercer().coerce(cell);
	}

}
