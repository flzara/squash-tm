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
package org.squashtest.tm.web.internal.controller.testcase.importer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.squashtest.tm.service.batchimport.excel.ColumnMismatch;
import org.squashtest.tm.service.batchimport.excel.TemplateMismatchException;
import org.squashtest.tm.service.batchimport.excel.WorksheetFormatStatus;

/**
 * Holds references to {@link ColumnMismatch} and build String list of concerned columns for each mismatch type. <br/>
 * see {@link #getMissingMandatoryColumns()} and {@link #getDuplicateColumns()}.
 *
 * @author mpagnon
 *
 */
public class ImportFormatFailure {
	private Map<ColumnMismatch, Set<String>> mismatches = new HashMap<>();

	public ImportFormatFailure(TemplateMismatchException tme) {
		fillMismatches(tme);

	}

	private void fillMismatches(TemplateMismatchException tme) {
		for (WorksheetFormatStatus wfs : tme.getWorksheetFormatStatuses()) {
			Map<ColumnMismatch, Set<String>> worksheetMismatches = wfs.getColumnNamesByMismatches();
			addAll(worksheetMismatches);
		}

	}

	private void addAll(Map<ColumnMismatch, Set<String>> worksheetMismatches) {
		for (Entry<ColumnMismatch, Set<String>> columnMismatch : worksheetMismatches.entrySet()) {
			Set<String> columnNames = columnMismatch.getValue();
			Set<String> alreadyStoredColumnNames = mismatches.get(columnMismatch.getKey());
			if (alreadyStoredColumnNames == null) {
				alreadyStoredColumnNames = new HashSet<>();
			}
			alreadyStoredColumnNames.addAll(columnNames);
			mismatches.put(columnMismatch.getKey(), alreadyStoredColumnNames);
		}

	}

	/**
	 * @return a set of column names concerned by the duplicate mismatch. The column names are formated like this :
	 *         {@code "<worksheet1>.<column1>}
	 */
	public Set<String> getDuplicateColumns() {
		return mismatches.get(ColumnMismatch.DUPLICATE);
	}

	/**
	 * @return a set of column names concerned by the mandatory missing column mismatch. The column names are formated
	 *         like this : {@code "<worksheet1>.<column1>}
	 */
	public Set<String> getMissingMandatoryColumns() {
		return mismatches.get(ColumnMismatch.MISSING_MANDATORY);
	}

	/**
	 * see workspace.import-popup.js "doSubmit"
	 *
	 * @return "Format KO"
	 */
	public String getStatus() {
		return "Format KO";
	}
}
