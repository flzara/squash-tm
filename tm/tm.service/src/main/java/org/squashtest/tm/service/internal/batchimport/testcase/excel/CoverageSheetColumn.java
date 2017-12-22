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

import static org.squashtest.tm.service.internal.batchimport.testcase.excel.ColumnProcessingMode.MANDATORY;

public enum CoverageSheetColumn implements TemplateColumn{
	
	REQ_PATH(MANDATORY),
	REQ_VERSION_NUM(MANDATORY),
	TC_PATH(MANDATORY);

	public final String header; ; // NOSONAR immutable public field
	public final ColumnProcessingMode processingMode; ; // NOSONAR immutable public field


	private CoverageSheetColumn(ColumnProcessingMode processingMode) {
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
		return TemplateWorksheet.COVERAGE_SHEET;
	}
	@Override
	public String getFullName() {
		return getWorksheet().sheetName +"."+header;
	}

}
