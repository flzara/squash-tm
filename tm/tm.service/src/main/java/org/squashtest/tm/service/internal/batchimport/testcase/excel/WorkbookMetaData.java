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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.squashtest.tm.service.batchimport.excel.TemplateMismatchException;
import org.squashtest.tm.service.batchimport.excel.WorksheetFormatStatus;

/**
 * Metadata of a test case import workbook. It collects data about the worksheets and the columns we have to process.
 *
 * @author Gregory Fouquet
 *
 */
public class WorkbookMetaData {
	private Map<TemplateWorksheet, WorksheetDef<? extends TemplateColumn>> worksheetDefByType = new HashMap<>();

	/**
	 * should not be called after build time / validation
	 *
	 * @param worksheetDef
	 */
	void addWorksheetDef(WorksheetDef<? extends TemplateColumn> worksheetDef) {
		worksheetDefByType.put(worksheetDef.getWorksheetType(), worksheetDef);

	}

	/**
	 * Validates this {@link WorksheetDef}. Unrecoverable mismatches from template will throw an exception.
	 *
	 * @throws TemplateMismatchException
	 *             when the metadata does not match the expected template in an unrecoverable way. The exception holds
	 *             all encountered mismatches.
	 */
	public void validate() throws TemplateMismatchException {
		List<WorksheetFormatStatus> worksheetKOStatuses = new ArrayList<>();

		for (WorksheetDef<?> wd : worksheetDefByType.values()) {

			WorksheetFormatStatus worksheetStatus = wd.validate();
			if(!worksheetStatus.isFormatOk()){
				worksheetKOStatuses.add(worksheetStatus);
			}
		}

		if (!worksheetKOStatuses.isEmpty()) {
			throw new TemplateMismatchException(worksheetKOStatuses);
		}
	}

	/**
	 *
	 * @param ws
	 *            the ws template for which we want the ws def.
	 * @return the {@link WorksheetDef} matching the given ws template or <code>null</code>
	 */
	@SuppressWarnings("unchecked")
	public <COL extends TemplateColumn> WorksheetDef<COL> getWorksheetDef(TemplateWorksheet ws) {
		return (WorksheetDef<COL>) worksheetDefByType.get(ws);
	}

	public Collection<WorksheetDef<? extends TemplateColumn>> getWorksheetDefs() {
		return worksheetDefByType.values();
	}
}
