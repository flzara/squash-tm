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

import static org.squashtest.tm.service.internal.batchimport.testcase.excel.TemplateCustomFieldPattern.REQUIREMENT_VERSION_CUSTOM_FIELD;
import static org.squashtest.tm.service.internal.batchimport.testcase.excel.TemplateCustomFieldPattern.STEP_CUSTOM_FIELD;
import static org.squashtest.tm.service.internal.batchimport.testcase.excel.TemplateCustomFieldPattern.TEST_CASE_CUSTOM_FIELD;

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.collections.map.MultiValueMap;
import org.squashtest.tm.service.internal.batchimport.requirement.excel.RequirementLinksSheetColumn;
import org.squashtest.tm.service.internal.batchimport.requirement.excel.RequirementSheetColumn;

/**
 * Enum of worksheet which are expected in the import file.
 *
 * @author Gregory Fouquet
 *
 */
public enum TemplateWorksheet {
	REQUIREMENT_SHEET("REQUIREMENT", RequirementSheetColumn.class, REQUIREMENT_VERSION_CUSTOM_FIELD),
	TEST_CASES_SHEET("TEST_CASES", TestCaseSheetColumn.class, TEST_CASE_CUSTOM_FIELD),
	STEPS_SHEET("STEPS", StepSheetColumn.class, STEP_CUSTOM_FIELD),
	PARAMETERS_SHEET("PARAMETERS", ParameterSheetColumn.class),
	DATASETS_SHEET("DATASETS", DatasetSheetColumn.class),
	// the same sheet is shared for both dataset and values
	DATASET_PARAM_VALUES_SHEET("DATASETS", DatasetParamValuesSheetColumn.class),

	COVERAGE_SHEET("LINK_REQ_TC", CoverageSheetColumn.class),
	REQUIREMENT_LINKS_SHEET("LINK_REQ_REQ", RequirementLinksSheetColumn.class);

	// MultiValueMap<String, TemplateWorksheet>
	private static final MultiValueMap ENUM_BY_SHEET_NAME = new MultiValueMap();

	public final String sheetName; // NOSONAR immutable public field
	public final Class<? extends Enum<?>> columnTypesClass; ; // NOSONAR immutable public field
	public final TemplateCustomFieldPattern customFieldPattern; ; // NOSONAR immutable public field

	private <E extends Enum<?> & TemplateColumn> TemplateWorksheet(String name, Class<E> columnEnumType) {
		this.sheetName = name;
		this.columnTypesClass = columnEnumType;
		this.customFieldPattern = TemplateCustomFieldPattern.NO_CUSTOM_FIELD;
	}

	private <E extends Enum<?> & TemplateColumn> TemplateWorksheet(String name, Class<E> columnEnumType,
			TemplateCustomFieldPattern customFieldPattern) {
		this.sheetName = name;
		this.columnTypesClass = columnEnumType;
		this.customFieldPattern = customFieldPattern;
	}

	/**
	 * Returns the enum value matching the given sheet name.
	 *
	 * @param name
	 * @return the matching enum, <code>null</code> when no match.
	 */
	/*
	 * Feat 3695
	 *
	 * For a same sheet in the workbook one can now define it by multiple templates
	 * Hint : as for now only the sheet DATASETS can be defined by : DATASET_SHEET
	 * and DATASET_PARAM_VALUES_SHEET
	 *
	 */
	@SuppressWarnings("unchecked")
	public static Collection<TemplateWorksheet> coerceFromSheetName(String name) {
		if (ENUM_BY_SHEET_NAME.isEmpty()) {
			synchronized (ENUM_BY_SHEET_NAME) {
				for (TemplateWorksheet e : TemplateWorksheet.values()) {
					ENUM_BY_SHEET_NAME.put(e.sheetName, e);
				}
			}
		}

		Collection<TemplateWorksheet> templates =  ENUM_BY_SHEET_NAME.getCollection(name);
		if (templates == null){
			templates = Collections.emptyList();
		}
		return templates;
	}

	@SuppressWarnings("unchecked")
	public <E extends TemplateColumn> E[] getColumnTypes() {
		return (E[]) TemplateColumnUtils.values(columnTypesClass);
	}

}
