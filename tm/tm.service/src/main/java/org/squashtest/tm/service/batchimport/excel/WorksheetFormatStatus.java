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
package org.squashtest.tm.service.batchimport.excel;

import java.util.*;
import java.util.Map.Entry;

import org.squashtest.tm.service.internal.batchimport.testcase.excel.TemplateColumn;
import org.squashtest.tm.service.internal.batchimport.testcase.excel.TemplateWorksheet;

/**
 * Indicates if the {@link #worksheet} is conform to the expected format ({@link #isFormatOk()}. <br/>
 * If not, will hold the {@link #columnMismatches} infos.
 *
 * @author Gregory Fouquet
 *
 */
public class WorksheetFormatStatus {
	private Map<ColumnMismatch, Set<TemplateColumn>> columnMismatches = new EnumMap<>(ColumnMismatch.class);
	private List<WorksheetMismatch> worksheetMismatches = new ArrayList<>();
	private TemplateWorksheet worksheet;

	public WorksheetFormatStatus(TemplateWorksheet worksheet) {
		this.worksheet = worksheet;
	}

	public TemplateWorksheet getWorksheet() {
		return worksheet;
	}

	/**
	 * Add mismatches to the worksheed format status.
	 *
	 * @param mismatchType
	 *            : the concerned{@link ColumnMismatch}
	 * @param columnsMismatched
	 *            : a collection of the concerned {@link TemplateColumn}
	 */
	public void addMismatches(ColumnMismatch mismatchType, Collection<TemplateColumn> columnsMismatched) {
		if (!columnsMismatched.isEmpty()) {
			Set<TemplateColumn> columns = findOrAddColumnListForMismatch(mismatchType);
			columns.addAll(columnsMismatched);
			this.columnMismatches.put(mismatchType, columns);
		}
	}

	/**
	 * Will add all given columnMismatches to this.{@link #columnMismatches}
	 *
	 * @param columnMismatches
	 *            : the mismatches to store here
	 */
	public void addAllMismatches(Map<ColumnMismatch, Set<TemplateColumn>> columnMismatches) {
		for (Entry<ColumnMismatch, Set<TemplateColumn>> mismatch : columnMismatches.entrySet()) {
			addMismatches(mismatch.getKey(), mismatch.getValue());
		}
	}

	/**
	 *
	 * @return true if there is no column mismatches.
	 */
	public boolean isFormatOk() {
		return columnMismatches.isEmpty() && worksheetMismatches.isEmpty();
	}

	/**
	 *
	 * @return the column full names ( see {@link TemplateColumn#getFullName()}) by the {@link ColumnMismatch}
	 */
	public Map<ColumnMismatch, Set<String>> getColumnNamesByMismatches() {
		Map<ColumnMismatch, Set<String>> result = new EnumMap<>(ColumnMismatch.class);
		for (Entry<ColumnMismatch, Set<TemplateColumn>> mismatch : columnMismatches.entrySet()) {
			Set<String> columns = extractColumnFullNames(mismatch.getValue());
			Set<String> alreadyStoredColumns = result.get(mismatch.getKey());
			if (alreadyStoredColumns == null) {
				alreadyStoredColumns = new HashSet<>();
			}
			alreadyStoredColumns.addAll(columns);
			result.put(mismatch.getKey(), alreadyStoredColumns);
		}
		return result;
	}

	private Set<String> extractColumnFullNames(Set<TemplateColumn> columns) {
		Set<String> columnNames = new HashSet<>(columns.size());
		for (TemplateColumn column : columns) {
			columnNames.add(column.getFullName());
		}
		return columnNames;
	}

	/**
	 * Store the {@link WorksheetMismatch}.
	 * @param worksheetMismatch
	 */
	public void addWorksheetMismatch(WorksheetMismatch worksheetMismatch) {
		this.worksheetMismatches.add(worksheetMismatch);

	}
	/**
	 * Will store the column mismatch.
	 *
	 * @param mismatchType
	 * @param colType
	 */
	public void addMismatch(ColumnMismatch mismatchType, TemplateColumn colType) {
		Set<TemplateColumn> columns = findOrAddColumnListForMismatch(mismatchType);
		columns.add(colType);
		this.columnMismatches.put(mismatchType, columns);

	}

	private Set<TemplateColumn> findOrAddColumnListForMismatch(ColumnMismatch mismatchType) {
		Set<TemplateColumn> columns = this.columnMismatches.get(mismatchType);
		if (columns == null) {
			columns = new HashSet<>();
		}
		return columns;
	}
}
