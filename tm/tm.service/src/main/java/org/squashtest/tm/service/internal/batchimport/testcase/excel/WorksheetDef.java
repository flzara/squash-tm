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
import java.util.Map.Entry;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.tm.service.batchimport.excel.ColumnMismatch;
import org.squashtest.tm.service.batchimport.excel.WorksheetFormatStatus;

/**
 * Definition of a worksheet that is to be processd by the importer.
 *
 * @author Gregory Fouquet
 *
 */
public class WorksheetDef<COL extends TemplateColumn> {
	private static final Logger LOGGER = LoggerFactory.getLogger(WorksheetDef.class);

	private final TemplateWorksheet worksheetType;
	private final Map<COL, StdColumnDef<COL>> stdColumnDefs = new HashMap<>();
	private final List<CustomFieldColumnDef> customFieldDefs = new ArrayList<>();
	private final List<UnknownColumnDef> unknownColumnDefs = new ArrayList<>();

	public WorksheetDef(@NotNull TemplateWorksheet worksheetType) {
		super();
		this.worksheetType = worksheetType;
	}

	/**
	 * @return the worksheetType
	 */
	public TemplateWorksheet getWorksheetType() {
		return worksheetType;
	}

	/**
	 * @param columnDef
	 * @return {@code true} if the columnDef had already been stored
	 */
	private boolean addColumnDef(@NotNull StdColumnDef<COL> columnDef) {
		return stdColumnDefs.put(columnDef.getType(), columnDef) != null;
	}

	/**
	 * Validates this {@link WorksheetDef}. Unrecoverable mismatches from template will throw an exception.
	 *
	 * @returns {@link WorksheetFormatStatus} that holds the possible Column mismatches
	 */
	WorksheetFormatStatus validate() {

		List<TemplateColumn> missingMandatoryColumnMismatch = new ArrayList<>();

		for (TemplateColumn col : worksheetType.getColumnTypes()) {
			if (isMandatory(col) && noColumnDef(col)) {
				missingMandatoryColumnMismatch.add(col);
			}
		}
		WorksheetFormatStatus worksheetStatus = new WorksheetFormatStatus(worksheetType);
		worksheetStatus.addMismatches(ColumnMismatch.MISSING_MANDATORY, missingMandatoryColumnMismatch);
		return worksheetStatus;
	}


	public Collection<UnknownColumnDef> getUnknownColumns(){
		return unknownColumnDefs;
	}

	private boolean isMandatory(TemplateColumn col) {
		return ColumnProcessingMode.MANDATORY == col.getProcessingMode();
	}

	private boolean noColumnDef(TemplateColumn col) {
		return stdColumnDefs.get(col) == null;
	}

	public boolean isCustomFieldHeader(String header) {
		return parseCustomFieldHeader(header) != null;
	}

	/**
	 * Adds a column. This should not be used after build time / validation.
	 *
	 * @param header
	 * @param colIndex
	 * @return
	 * @throws ColumnMismatchException
	 */
	@SuppressWarnings("unchecked")
	ColumnDef addColumnDef(String header, int colIndex) throws ColumnMismatchException {
		ColumnDef res = null;

		COL colType = (COL) TemplateColumnUtils.coerceFromHeader(worksheetType.columnTypesClass, header);
		boolean duplicate = false;
		if (colType != null) {
			LOGGER.trace("Column named '{}' will be added to metamodel as standard column {}", header, colType);
			res = new StdColumnDef<>(colType, colIndex);
			duplicate = addColumnDef((StdColumnDef<COL>) res);

		} else if (isCustomFieldHeader(header)) {
			LOGGER.trace("Column named '{}' will be added to metamodel as custom field", header);
			res = new CustomFieldColumnDef(parseCustomFieldHeader(header), colIndex);
			List<CustomFieldColumnDef> cufDefs = getCustomFieldDefs();
			duplicate = cufDefs.contains(res);
			cufDefs.add((CustomFieldColumnDef) res);

		} else {
			LOGGER.trace("Column named '{}' is unknown", header);
			unknownColumnDefs.add(new UnknownColumnDef(colIndex, header));
		}
		if (duplicate) {
			throw new ColumnMismatchException(ColumnMismatch.DUPLICATE, colType);
		}

		return res;
	}

	private String parseCustomFieldHeader(String header) {
		return worksheetType.customFieldPattern.parseFieldCode(header);
	}

	public StdColumnDef<COL> getColumnDef(COL col) {
		return stdColumnDefs.get(col);
	}

	public List<StdColumnDef<COL>> getImportableColumnDefs() {
		List<StdColumnDef<COL>> res = new ArrayList<>(stdColumnDefs.size());

		for (Entry<COL, StdColumnDef<COL>> entry : stdColumnDefs.entrySet()) {
			if (!isIgnored(entry.getKey())) {
				if ("ACTION".equals(entry.getKey().getHeader())) {
					res.add(0, entry.getValue());
				} else {
					res.add(entry.getValue());
				}
			}
		}

		return res;
	}

	/**
	 * @param key
	 * @return
	 */
	private boolean isIgnored(COL col) {
		return ColumnProcessingMode.IGNORED == col.getProcessingMode();
	}

	/**
	 * Name of the worksheet in the workbook
	 *
	 * @return
	 */
	public String getSheetName() {
		return worksheetType.sheetName;
	}

	/**
	 * @return the customFieldDefs
	 */
	public List<CustomFieldColumnDef> getCustomFieldDefs() {
		return customFieldDefs;
	}
}
