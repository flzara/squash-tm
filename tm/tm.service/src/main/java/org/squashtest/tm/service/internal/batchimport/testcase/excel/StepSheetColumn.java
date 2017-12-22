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
 * Describes columns from test step import template.
 * 
 * @author Gregory Fouquet
 * 
 */
public enum StepSheetColumn implements TemplateColumn {
	ACTION,
	TC_OWNER_PATH(MANDATORY),
	TC_OWNER_ID(IGNORED),
	TC_STEP_ID(IGNORED),
	TC_STEP_NUM,
	TC_STEP_IS_CALL_STEP,
	TC_STEP_CALL_DATASET,
	TC_STEP_ACTION,
	TC_STEP_EXPECTED_RESULT,
	TC_STEP_NB_REQ("TC_STEP_#_REQ", IGNORED),
	TC_STEP_NB_ATTACHMENT("TC_STEP_#_ATTACHMENT", IGNORED);

	public final String header; ; // NOSONAR immutable public field
	public final ColumnProcessingMode processingMode; ; // NOSONAR immutable public field

	private StepSheetColumn() {
		this.header = name();
		processingMode = OPTIONAL;
	}

	private StepSheetColumn(String header, ColumnProcessingMode processingMode) {
		this.header = header;
		this.processingMode = processingMode;
	}

	private StepSheetColumn(ColumnProcessingMode processingMode) {
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
		return TemplateWorksheet.STEPS_SHEET;
	}
	@Override
	public String getFullName() {
		return getWorksheet().sheetName +"."+header;
	}
}
