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

import java.text.ParseException;
import java.util.Date;

import org.apache.poi.ss.usermodel.Cell;
import org.squashtest.tm.core.foundation.lang.DateUtils;
import org.squashtest.tm.service.internal.batchimport.Messages;

/**
 * Coerces an optional date cell into a {@link Date}. Handles both excel-style date cells and string cells containing an
 * iso-whatever formatted date.
 * 
 * @author Gregory Fouquet
 * 
 */
public final class OptionalDateCellCoercer extends TypeBasedCellValueCoercer<Date> implements CellValueCoercer<Date> {
	public static final OptionalDateCellCoercer INSTANCE = new OptionalDateCellCoercer();

	private OptionalDateCellCoercer() {
		super();
	}

	/**
	 * @see org.squashtest.tm.service.internal.batchimport.excel.TypeBasedCellValueCoercer#coerceBlankCell(org.apache.poi.ss.usermodel.Cell)
	 */
	@Override
	protected Date coerceBlankCell(Cell cell) {
		return null;
	}

	/**
	 * @see org.squashtest.tm.service.internal.batchimport.excel.TypeBasedCellValueCoercer#coerceStringCell(org.apache.poi.ss.usermodel.Cell)
	 */
	@Override
	protected Date coerceStringCell(Cell cell) {
		try {
			return DateUtils.parseIso8601Date(cell.getStringCellValue());
		} catch (ParseException e) {
			throw new CannotCoerceException(e, Messages.ERROR_UNPARSABLE_DATE, Messages.IMPACT_FIELD_NOT_CHANGED, Messages.IMPACT_USE_CURRENT_DATE);
		}
	}

	/**
	 * @see org.squashtest.tm.service.internal.batchimport.excel.TypeBasedCellValueCoercer#coerceNumericCell(org.apache.poi.ss.usermodel.Cell)
	 */
	@Override
	protected Date coerceNumericCell(Cell cell) {
		try {
			return cell.getDateCellValue();
		} catch (IllegalArgumentException e) {
			throw new CannotCoerceException(e, Messages.ERROR_UNPARSABLE_DATE, Messages.IMPACT_FIELD_NOT_CHANGED, Messages.IMPACT_USE_CURRENT_DATE);
		}
	}

}
