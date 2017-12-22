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

/**
 * This class liberally coerces a cell into a Boolean. Blank cells are coerced to null.
 *
 * @author Gregory Fouquet
 *
 */
public final class OptionalBooleanCellCoercer extends LiberalBooleanCellCoercer {
	public static final OptionalBooleanCellCoercer INSTANCE = new OptionalBooleanCellCoercer();

	private OptionalBooleanCellCoercer() {
		super();
	}

	/**
	 * @see org.squashtest.tm.service.internal.batchimport.excel.TypeBasedCellValueCoercer#coerceBlankCell(org.apache.poi.ss.usermodel.Cell)
	 */
	@Override
	protected Boolean coerceBlankCell(Cell cell) {
		return null; // NOSONAR absent optional boolean -> null value
	}

}
