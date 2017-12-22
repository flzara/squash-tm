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

import org.apache.poi.ss.usermodel.Cell;
import org.squashtest.tm.service.internal.batchimport.Messages;

/**
 * Coerces a cell to an Integer value. As values are extracted as float, they are rounded to the closest integer.
 *
 * When a cell is of string type, this coercer shall try to parse the cell content as a number. When the cell is not
 * parseable, it throws TBD exception.
 *
 * When a cell is empty, this coercer returns null
 *
 *
 *
 * @author Gregory Fouquet
 *
 */
public class OptionalIntegerCellCoercer extends TypeBasedCellValueCoercer<Integer> implements CellValueCoercer<Integer> {
	public static final OptionalIntegerCellCoercer INSTANCE = new OptionalIntegerCellCoercer();

	protected OptionalIntegerCellCoercer() {
		super();
	}

	/**
	 *
	 * @see org.squashtest.tm.service.internal.batchimport.excel.TypeBasedCellValueCoercer#coerceStringCell(org.apache.poi.ss.usermodel.Cell)
	 */
	@Override
	protected Integer coerceStringCell(Cell cell) throws NumberFormatException {
		Integer res;
		String val = cell.getStringCellValue();
		try {
			res = Integer.valueOf(val, 10);
		} catch (NumberFormatException e) {
			try {
				res = round(Double.valueOf(val));
			} catch (NumberFormatException ex) {
				throw new CannotCoerceException(ex, Messages.ERROR_UNPARSABLE_INTEGER); // NOSONAR no need for actual call stack
			}
		}
		return res;
	}

	/**
	 *
	 * @see org.squashtest.tm.service.internal.batchimport.excel.TypeBasedCellValueCoercer#coerceNumericCell(org.apache.poi.ss.usermodel.Cell)
	 */
	@Override
	protected Integer coerceNumericCell(Cell cell) {
		Integer res;
		double val = cell.getNumericCellValue();
		res = round(val);
		return res;
	}

	/**
	 * @see org.squashtest.tm.service.internal.batchimport.excel.TypeBasedCellValueCoercer#coerceBlankCell(org.apache.poi.ss.usermodel.Cell)
	 */
	@Override
	protected Integer coerceBlankCell(Cell cell) {
		return null;
	}

}
