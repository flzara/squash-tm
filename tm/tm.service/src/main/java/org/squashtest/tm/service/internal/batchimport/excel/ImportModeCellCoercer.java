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

import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.squashtest.tm.service.importer.ImportMode;
import org.squashtest.tm.service.internal.batchimport.Messages;

/**
 * @author Gregory Fouquet
 * 
 */
public final class ImportModeCellCoercer extends TypeBasedCellValueCoercer<ImportMode> {
	public static final ImportModeCellCoercer INSTANCE = new ImportModeCellCoercer();

	private final Map<String, ImportMode> modeByShortName = new HashMap<>(
			ImportMode.values().length);



	private ImportModeCellCoercer() {
		super();
		modeByShortName.put("C", ImportMode.CREATE);
		modeByShortName.put("U", ImportMode.UPDATE);
		modeByShortName.put("D", ImportMode.DELETE);
	}

	/**
	 * Blank cell means default value means {@link ImportMode#UPDATE}.
	 * 
	 * @see org.squashtest.tm.service.internal.batchimport.excel.TypeBasedCellValueCoercer#coerceBlankCell(org.apache.poi.ss.usermodel.Cell)
	 */
	@Override
	protected ImportMode coerceBlankCell(Cell cell) {
		return ImportMode.getDefault();
	}

	/**
	 * @see org.squashtest.tm.service.internal.batchimport.excel.TypeBasedCellValueCoercer#coerceStringCell(org.apache.poi.ss.usermodel.Cell)
	 */
	@Override
	protected ImportMode coerceStringCell(Cell cell) {
		String val = cell.getStringCellValue();

		ImportMode res = modeByShortName.get(val);

		if (res == null) {
			try {
				res = Enum.valueOf(ImportMode.class, val);
			} catch (IllegalArgumentException e) {
				throw new CannotCoerceException(e, Messages.ERROR_UNPARSABLE_OPTION, Messages.IMPACT_DEFAULT_ACTION);
			}
		}

		return res;
	}

}
