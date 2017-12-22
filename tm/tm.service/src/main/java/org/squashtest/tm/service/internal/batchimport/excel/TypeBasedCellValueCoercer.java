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

import javax.validation.constraints.NotNull;

import org.apache.poi.ss.usermodel.Cell;
import org.squashtest.tm.service.internal.batchimport.Messages;

/**
 * Implementation of {@link CellValueCoercer} which tests the cell's type then invokes a specific method. Default
 * implementation of each method throw a {@link CannotCoerceException} and are meant to be overriden in subclasses.
 *
 * @author Gregory Fouquet
 *
 */
public abstract class TypeBasedCellValueCoercer<VAL> implements CellValueCoercer<VAL> {
	private final String errorI18nKey;

	protected TypeBasedCellValueCoercer() {
		this(Messages.ERROR_GENERIC_UNPARSABLE);

	}

	protected TypeBasedCellValueCoercer(@NotNull String errorI18nKey) {
		super();
		this.errorI18nKey = errorI18nKey;

	}

	/**
	 * Checks the cell type and callthe appropriate <code>coerceXxxCell(cell)</code> accordingly
	 *
	 * @see org.squashtest.tm.service.internal.batchimport.excel.CellValueCoercer#coerce(org.apache.poi.ss.usermodel.Cell)
	 */
	@Override
	public final VAL coerce(Cell cell) throws CannotCoerceException {
		int type = cell.getCellType();
		VAL res;

		switch (type) {
			case Cell.CELL_TYPE_BLANK:
				res = coerceBlankCell(cell);
				break;

			case Cell.CELL_TYPE_NUMERIC:
				res = coerceNumericCell(cell);
				break;

			case Cell.CELL_TYPE_STRING:
				res = coerceStringCell(cell);
				break;

			case Cell.CELL_TYPE_BOOLEAN:
				res = coerceBooleanCell(cell);
				break;

			case Cell.CELL_TYPE_FORMULA:
				res = coerceFormulaCell(cell);
				break;

			case Cell.CELL_TYPE_ERROR:
				res = coerceErrorCell(cell);
				break;

			default:
				// we should never get here, ex should be thrown above
				throw new CannotCoerceException("Funky cell type " + type + " is not coercible",
					Messages.ERROR_FUNKY_CELL_TYPE);
		}

		return res;
	}

	private VAL coerceErrorCell(Cell cell) {
		throw cannotCoerceFunky(cell);
	}

	private CannotCoerceException cannotCoerceFunky(Cell cell) {
		return new CannotCoerceException("Cannot coerce cell [R," + cell.getRowIndex() + " C" + cell.getColumnIndex()
			+ "] of unhandled type", Messages.ERROR_FUNKY_CELL_TYPE);

	}

	protected VAL coerceFormulaCell(Cell cell) {
		throw cannotCoerceFunky(cell);
	}

	protected VAL coerceBooleanCell(Cell cell) {
		throw cannotCoerce("BOOLEAN", cell);
	}

	protected VAL coerceBlankCell(Cell cell) {
		throw cannotCoerce("BLANK", cell);
	}

	protected VAL coerceStringCell(Cell cell) {
		throw cannotCoerce("STRING", cell);
	}

	protected VAL coerceNumericCell(Cell cell) {
		throw cannotCoerce("NUMERIC", cell);
	}

	private CannotCoerceException cannotCoerce(String type, Cell cell) {
		return cannotCoerce(type, cell, errorI18nKey);
	}

	private CannotCoerceException cannotCoerce(String type, Cell cell, String errorI18nKey) {
		return new CannotCoerceException("Cannot coerce cell [R," + cell.getRowIndex() + " C" + cell.getColumnIndex()
			+ "] of type " + type, errorI18nKey);
	}

	/**
	 * Parses a string into an int. When the string is the representation of a floating point number, it is parsed into
	 * the nearest int.
	 *
	 * @throws CannotCoerceException
	 */
	protected int liberallyParseInt(String s) throws CannotCoerceException {
		int res;
		try {
			res = Integer.valueOf(s, 10);
		} catch (NumberFormatException e) {
			try {
				res = round(Double.valueOf(s));
			} catch (NumberFormatException ex) {
				throw new CannotCoerceException(ex, Messages.ERROR_UNPARSABLE_INTEGER); // NOSONAR actual call stack unnecessary
			}
		}
		return res;
	}

	/**
	 * Utility method which rounds a floating point number to the nearest integer.
	 *
	 */
	protected int round(double val) {
		return (int) Math.round(val);
	}

}
