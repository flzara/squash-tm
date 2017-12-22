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
 * Liberally coerces a cell into a boolean value.<br/>
 * Acceptable cell values for <code>true</code> are :
 * <ul>
 * <li>true
 * <li>"1"
 * <li>any decimal in [0.5, 1.5[
 * </ul>
 * Acceptable cell values for <code>false</code> are :
 * <ul>
 * <li>false
 * <li>"0"
 * <li>any decimal in ]-0.5, 0.5[
 * </ul>
 * 
 * Any other value generates a {@link CannotCoerceException}
 * 
 * @author Gregory Fouquet
 * 
 */
public class LiberalBooleanCellCoercer extends TypeBasedCellValueCoercer<Boolean> implements CellValueCoercer<Boolean> {
	public static final LiberalBooleanCellCoercer INSTANCE = new LiberalBooleanCellCoercer();

	protected LiberalBooleanCellCoercer() {
		super();
	}

	/**
	 * @see org.squashtest.tm.service.internal.batchimport.excel.TypeBasedCellValueCoercer#coerceBooleanCell(org.apache.poi.ss.usermodel.Cell)
	 */
	@Override
	protected Boolean coerceBooleanCell(Cell cell) {
		return cell.getBooleanCellValue();
	}

	/**
	 * @see org.squashtest.tm.service.internal.batchimport.excel.TypeBasedCellValueCoercer#coerceStringCell(org.apache.poi.ss.usermodel.Cell)
	 */
	@Override
	protected Boolean coerceStringCell(Cell cell) {
		int intValue = liberallyParseInt(cell.getStringCellValue());
		return coerceDouble(intValue);
	}

	/**
	 * @see org.squashtest.tm.service.internal.batchimport.excel.TypeBasedCellValueCoercer#coerceNumericCell(org.apache.poi.ss.usermodel.Cell)
	 */
	@Override
	protected Boolean coerceNumericCell(Cell cell) {
		double val = cell.getNumericCellValue();
		return coerceDouble(val);
	}

	private boolean coerceDouble(double val) throws CannotCoerceException {
		boolean res;

		switch (round(val)) {
		case 0:
			res = false;
			break;
		case 1:
			res = true;
			break;
		default:
			throw new CannotCoerceException("Cannot coerce cell value " + val
					+ " into a boolean. Rounded value should either be 0 or 1", Messages.ERROR_UNPARSABLE_CHECKBOX);
		}

		return res;
	}
}
