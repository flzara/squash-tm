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
package org.squashtest.tm.service.internal.batchimport.requirement.excel;

import static org.squashtest.tm.service.internal.batchimport.testcase.excel.ColumnProcessingMode.IGNORED;
import static org.squashtest.tm.service.internal.batchimport.testcase.excel.ColumnProcessingMode.MANDATORY;
import static org.squashtest.tm.service.internal.batchimport.testcase.excel.ColumnProcessingMode.OPTIONAL;

import org.squashtest.tm.service.internal.batchimport.testcase.excel.ColumnProcessingMode;
import org.squashtest.tm.service.internal.batchimport.testcase.excel.TemplateColumn;
import org.squashtest.tm.service.internal.batchimport.testcase.excel.TemplateWorksheet;

/**
 * Enum of the columns which can be found in the requirement sheet of an import / export workbook.
 *
 * @author Julien Thebault
 *
 */
public enum RequirementSheetColumn implements TemplateColumn {
	ACTION,
	PROJECT_ID(IGNORED),
	PROJECT_NAME(IGNORED),
	REQ_PATH(MANDATORY),
	REQ_NUM,
	REQ_ID(IGNORED),
	REQ_VERSION_NUM,
	REQ_VERSION_ID(IGNORED),
	REQ_VERSION_REFERENCE,
	REQ_VERSION_NAME,
	REQ_VERSION_CRITICALITY,
	REQ_VERSION_CATEGORY,
	REQ_VERSION_STATUS,
	REQ_VERSION_DESCRIPTION,
	REQ_VERSION_NB_TC("REQ_VERSION_#_TC",IGNORED),
	REQ_VERSION_NB_ATTACHEMENT("REQ_VERSION_#_ATTACHEMENT",IGNORED),
	REQ_VERSION_CREATED_ON,
	REQ_VERSION_CREATED_BY,
	REQ_VERSION_LAST_MODIFIED_ON(IGNORED),
	REQ_VERSION_LAST_MODIFIED_BY(IGNORED),
	REQ_VERSION_MILESTONE,
	REQ_VERSIONS("#_VERSIONS",IGNORED),
	REQ_VERSION_NB_MILESTONE("#_MIL",IGNORED);

	public final String header; ; // NOSONAR immutable public field
	public final ColumnProcessingMode processingMode; ; // NOSONAR immutable public field

	private RequirementSheetColumn() {
		this.header = name();
		processingMode = OPTIONAL;
	}

	private RequirementSheetColumn(ColumnProcessingMode processingMode) {
		this.header = name();
		this.processingMode = processingMode;
	}

	private RequirementSheetColumn(String header, ColumnProcessingMode processingMode) {
		this.header = header;
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
