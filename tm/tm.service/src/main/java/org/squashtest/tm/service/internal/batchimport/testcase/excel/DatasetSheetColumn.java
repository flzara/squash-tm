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

import static org.squashtest.tm.service.internal.batchimport.testcase.excel.ColumnProcessingMode.IGNORED;
import static org.squashtest.tm.service.internal.batchimport.testcase.excel.ColumnProcessingMode.MANDATORY;
import static org.squashtest.tm.service.internal.batchimport.testcase.excel.ColumnProcessingMode.OPTIONAL;

/**
 * Enum of the columns which can be found in the datashet worksheet of an import / export workbook.
 * 
 * @author Gregory Fouquet
 * 
 */
public enum DatasetSheetColumn implements TemplateColumn {
	ACTION,
	TC_OWNER_PATH(MANDATORY),
	TC_OWNER_ID(IGNORED),
	TC_DATASET_ID(IGNORED),
	TC_DATASET_NAME(MANDATORY),
	TC_PARAM_OWNER_PATH,
	TC_PARAM_OWNER_ID(IGNORED),
	TC_DATASET_PARAM_NAME(MANDATORY),
	TC_DATASET_PARAM_VALUE;

	public final String header; ; // NOSONAR immutable public field
	public final ColumnProcessingMode processingMode; ; // NOSONAR immutable public field

	private DatasetSheetColumn() {
		this.header = name();
		processingMode = OPTIONAL;
	}

	private DatasetSheetColumn(ColumnProcessingMode processingMode) {
		this.header = name();
		this.processingMode = processingMode;
	}

	/**
	 * @see org.squashtest.tm.service.internal.batchimport.testcase.excel.TemplateColumn#getHeader()
	 */
	@Override
	public String getHeader() {
		return header;
	}

	/**
	 * @see org.squashtest.tm.service.internal.batchimport.testcase.excel.TemplateColumn#getProcessingMode()
	 */
	@Override
	public ColumnProcessingMode getProcessingMode() {
		return processingMode;
	}

	@Override
	public TemplateWorksheet getWorksheet() {
		return TemplateWorksheet.DATASETS_SHEET;
	}

	@Override
	public String getFullName() {
		return getWorksheet().sheetName +"."+header;
	}

}
