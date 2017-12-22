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

/**
 * Interface for an object which coerces a {@link Cell} into a value of VAL type
 * 
 * @author Gregory Fouquet
 * 
 */
public interface CellValueCoercer<VAL> {
	/**
	 * Coerces the cell into a VAL typed value
	 * 
	 * @param cell
	 *            the cell to coerce
	 * @return
	 * @throws CannotCoerceException
	 *             when not possible to coerce.
	 */
	VAL coerce(@NotNull Cell cell) throws CannotCoerceException;
}
