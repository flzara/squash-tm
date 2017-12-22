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
 * Enumerates columns in the test case worksheet
 *
 * @author Gregory Fouquet
 *
 */
public enum TestCaseSheetColumn implements TemplateColumn {
	ACTION,
	PROJECT_ID(IGNORED),
	PROJECT_NAME(IGNORED),
	TC_PATH(MANDATORY),
	TC_NUM,
	TC_ID(IGNORED),
	TC_REFERENCE,
	TC_NAME,
	TC_MILESTONE,
	TC_WEIGHT_AUTO,
	TC_WEIGHT,
	TC_NATURE,
	TC_TYPE,
	TC_STATUS,
	TC_DESCRIPTION,
	TC_PRE_REQUISITE,
	TC_NB_REQ("TC_#_REQ", IGNORED),
	TC_NB_CALLED_BY("TC_#_CALLED_BY", IGNORED),
	TC_NB_ATTACHMENT("TC_#_ATTACHMENT", IGNORED),
	TC_CREATED_ON,
	TC_CREATED_BY,
	TC_LAST_MODIFIED_ON(IGNORED),
	TC_LAST_MODIFIED_BY(IGNORED),
	TC_NB_MILESTONES("#_MIL", IGNORED),
	TC_NB_STEPS("#_TEST_STEPS", IGNORED),
	TC_NB_ITERATION("#_ITERATIONS", IGNORED);

	public final String header; ; // NOSONAR immutable public field
	public final ColumnProcessingMode processingMode; ; // NOSONAR immutable public field

	private TestCaseSheetColumn() {
		this.header = name();
		processingMode = OPTIONAL;
	}


	private TestCaseSheetColumn(String header, ColumnProcessingMode processingMode) {
		this.header = header;
		this.processingMode = processingMode;
	}

	private TestCaseSheetColumn(ColumnProcessingMode processingMode) {
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
		return TemplateWorksheet.TEST_CASES_SHEET;
	}
	@Override
	public String getFullName() {
		return getWorksheet().sheetName +"."+header;
	}
}
