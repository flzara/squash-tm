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
 * Coerces a cell into an enum. Non coercible values throw a {@link CannotCoerceException}. Empty cells return
 * <code>null</code>.
 *
 * @author Gregory Fouquet
 * @param the
 *            target enum.
 */
public final class OptionalEnumCellCoercer<ENUM extends Enum<ENUM>> extends TypeBasedCellValueCoercer<ENUM> implements
CellValueCoercer<ENUM> {
	private final Class<ENUM> enumType;

	public static <E extends Enum<E>> OptionalEnumCellCoercer<E> forEnum(@NotNull Class<E> enumType) {
		return new OptionalEnumCellCoercer<>(enumType);
	}

	private OptionalEnumCellCoercer(Class<ENUM> enumType) {
		super();
		this.enumType = enumType;
	}

	/**
	 * @see org.squashtest.tm.service.internal.batchimport.excel.TypeBasedCellValueCoercer#coerceStringCell(org.apache.poi.ss.usermodel.Cell)
	 */
	@Override
	protected ENUM coerceStringCell(Cell cell) {
		String name = cell.getStringCellValue();
		try {
			return Enum.valueOf(enumType, name);
		} catch (IllegalArgumentException e) {
			throw new CannotCoerceException(e, Messages.ERROR_UNPARSABLE_OPTION, Messages.IMPACT_FIELD_NOT_CHANGED, Messages.IMPACT_DEFAULT_VALUE);
		}
	}

	/**
	 * @see org.squashtest.tm.service.internal.batchimport.excel.TypeBasedCellValueCoercer#coerceBlankCell(org.apache.poi.ss.usermodel.Cell)
	 */
	@Override
	protected ENUM coerceBlankCell(Cell cell) {
		return null;
	}

}
