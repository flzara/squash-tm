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
package org.squashtest.tm.web.internal.controller.customexport;

import org.jooq.Field;
import org.jooq.Record;
import org.squashtest.tm.domain.customreport.CustomExportColumnLabel;
import org.squashtest.tm.domain.customreport.CustomReportCustomExport;
import org.squashtest.tm.domain.customreport.CustomReportCustomExportColumn;
import org.squashtest.tm.service.customfield.CustomFieldFinderService;
import org.squashtest.tm.service.customreport.CustomReportCustomExportCSVService;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import static org.squashtest.tm.domain.customreport.CustomExportColumnLabel.TEST_CASE_NATURE;
import static org.squashtest.tm.domain.customreport.CustomExportColumnLabel.TEST_CASE_TYPE;
import static org.squashtest.tm.jooq.domain.Tables.CUSTOM_FIELD_VALUE;

public class CustomExportCSVHelper {

	private static final char SEPARATOR = ';';

	private static final String CARRIAGE_RETURN = "\n";
	private static final String ESCAPED_QUOTE = "\"";
	private static final String NOT_AVAILABLE = "n/a;";
	private static final String SPACE_DASH_SPACE = " - ";

	private CustomReportCustomExportCSVService csvService;
	private CustomFieldFinderService cufService;
	private InternationalizationHelper translator;
	private Locale locale;

	public CustomExportCSVHelper(CustomReportCustomExportCSVService csvService, CustomFieldFinderService cufService, InternationalizationHelper translator, Locale locale) {
		this.csvService = csvService;
		this.cufService = cufService;
		this.translator = translator;
		this.locale = locale;
	}

	public String getInternationalizedHeaders(CustomReportCustomExport customExport) {
		StringBuilder builder = new StringBuilder();
		for(CustomReportCustomExportColumn column : customExport.getColumns()) {
			CustomExportColumnLabel columnLabel = column.getLabel();
			String headerLabel;
			if(column.getCufId() == null) {
				headerLabel = translator.internationalize(columnLabel.getI18nKey(), locale);
			} else {
				headerLabel = cufService.findById(column.getCufId()).getLabel();
			}
			// A column ' CAMPAIGN_LABEL ' will be written ' "CAMPAIGN_LABEL"; in the .csv file'
			builder.append(ESCAPED_QUOTE)
				.append(buildHeaderName(columnLabel.getShortenedEntityType(), headerLabel))
				.append(ESCAPED_QUOTE)
				.append(SEPARATOR);
		}
		builder.append(CARRIAGE_RETURN);
		return builder.toString();
	}

	private String buildHeaderName(String entityPrefix, String columnLabel) {
		return entityPrefix + SPACE_DASH_SPACE + columnLabel;
	}

	public String getWritableRowsData(CustomReportCustomExport customExport) {
		Iterator<Record> rowsData = csvService.getRowsData(customExport);
		return buildResultString(rowsData, customExport.getColumns());
	}

	private String buildResultString(Iterator<Record> resultSet, List<CustomReportCustomExportColumn> selectedColumns) {
		StringBuilder dataBuilder = new StringBuilder();
		resultSet.forEachRemaining(record -> {
				for (CustomReportCustomExportColumn column : selectedColumns) {
					CustomExportColumnLabel label = column.getLabel();
					Field columnField = label.getJooqTableField();
					Object value;
					if(label.equals(TEST_CASE_NATURE) || label.equals(TEST_CASE_TYPE)) {
						value = translator.internationalize(String.valueOf(record.get(columnField)), locale);
					} else if (columnField != null) {
						value = record.get(columnField);
					} else {
						// Custom field column
						value = record.get(CUSTOM_FIELD_VALUE.as(
							csvService.buildCufColumnName(label.getEntityType(), column.getCufId()))
							.VALUE);
					}
					// Append the value
					if(value != null) {
						dataBuilder.append(ESCAPED_QUOTE)
							.append(value)
							.append(ESCAPED_QUOTE)
							.append(SEPARATOR);
					} else {
						dataBuilder.append(NOT_AVAILABLE);
					}
				}
				dataBuilder.append("\n");
			}
		);
		return dataBuilder.toString();
	}
}
