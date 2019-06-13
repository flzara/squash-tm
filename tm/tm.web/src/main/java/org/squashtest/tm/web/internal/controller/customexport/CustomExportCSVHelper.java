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
import org.squashtest.tm.domain.EntityReference;
import org.squashtest.tm.domain.EntityType;
import org.squashtest.tm.domain.customreport.CustomExportColumnLabel;
import org.squashtest.tm.domain.customreport.CustomReportCustomExport;
import org.squashtest.tm.domain.customreport.CustomReportCustomExportColumn;
import org.squashtest.tm.service.customfield.CustomFieldFinderService;
import org.squashtest.tm.service.customfield.CustomFieldValueFinderService;
import org.squashtest.tm.service.customreport.CustomReportCustomExportCSVService;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.util.HTMLCleanupUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static org.squashtest.tm.domain.customreport.CustomExportColumnLabel.CAMPAIGN_DESCRIPTION;
import static org.squashtest.tm.domain.customreport.CustomExportColumnLabel.EXECUTION_COMMENT;
import static org.squashtest.tm.domain.customreport.CustomExportColumnLabel.EXECUTION_STEP_ACTION;
import static org.squashtest.tm.domain.customreport.CustomExportColumnLabel.EXECUTION_STEP_COMMENT;
import static org.squashtest.tm.domain.customreport.CustomExportColumnLabel.EXECUTION_STEP_RESULT;
import static org.squashtest.tm.domain.customreport.CustomExportColumnLabel.ITERATION_DESCRIPTION;
import static org.squashtest.tm.domain.customreport.CustomExportColumnLabel.TEST_CASE_DESCRIPTION;
import static org.squashtest.tm.domain.customreport.CustomExportColumnLabel.TEST_CASE_NATURE;
import static org.squashtest.tm.domain.customreport.CustomExportColumnLabel.TEST_CASE_PREREQUISITE;
import static org.squashtest.tm.domain.customreport.CustomExportColumnLabel.TEST_CASE_TYPE;
import static org.squashtest.tm.domain.customreport.CustomExportColumnLabel.TEST_SUITE_DESCRIPTION;
import static org.squashtest.tm.jooq.domain.Tables.CAMPAIGN;
import static org.squashtest.tm.jooq.domain.Tables.EXECUTION;
import static org.squashtest.tm.jooq.domain.Tables.EXECUTION_STEP;
import static org.squashtest.tm.jooq.domain.Tables.ITERATION;
import static org.squashtest.tm.jooq.domain.Tables.TEST_CASE;
import static org.squashtest.tm.jooq.domain.Tables.TEST_SUITE;

public class CustomExportCSVHelper {

	private static final char SEPARATOR = ';';

	private static final String CARRIAGE_RETURN = "\n";
	private static final String ESCAPED_QUOTE = "\"";
	private static final String NOT_AVAILABLE = "n/a;";
	private static final String SPACE_DASH_SPACE = " - ";

	private CustomReportCustomExportCSVService csvService;
	private CustomFieldFinderService cufService;
	private CustomFieldValueFinderService cufValueService;
	private InternationalizationHelper translator;
	private Locale locale;

	public CustomExportCSVHelper(CustomReportCustomExportCSVService csvService, CustomFieldFinderService cufService, CustomFieldValueFinderService cufValueService, InternationalizationHelper translator, Locale locale) {
		this.csvService = csvService;
		this.cufService = cufService;
		this.cufValueService = cufValueService;
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
			// A column ' CAMPAIGN_LABEL ' will be written ' "CAMPAIGN_LABEL"; ' in the .csv file
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
		Map<EntityReference, Map<Long, Object>> cufValuesMapByEntityReference = getCufValueMapByEntityRef(customExport);
		return buildResultString(rowsData, customExport.getColumns(), cufValuesMapByEntityReference);
	}

	/**
	 * Given a CustomExport, extract a Map which keys are all EntityReferences involved in the export
	 * and values are Maps of all the CustomFieldValues mapped by CustomField ids.
	 * @param customExport The CustomExport
	 * @return A Map<EntityReference, Map<Long, Object>> where the second Map gives CustomFieldValues
	 * mapped by CustomField ids.
	 */
	private Map<EntityReference, Map<Long, Object>> getCufValueMapByEntityRef(CustomReportCustomExport customExport) {
		List<CustomReportCustomExportColumn> selectedColumns = customExport.getColumns();
		// Extract Cuf Map group by EntityTypes
		Map<EntityType, List<Long>> cufIdsMapByEntityType = selectedColumns.stream()
			.filter(column -> column.getCufId() != null)
			.collect(
				Collectors.groupingBy(column->column.getLabel().getEntityType(),
					Collectors.mapping(CustomReportCustomExportColumn::getCufId, Collectors.toList())));
		// If no CustomFields were requested, nothing else is to do
		if(cufIdsMapByEntityType.isEmpty()) {
			return null;
		}
		EntityReference campaign = customExport.getScope().get(0);
		Long campaignId = campaign.getId();
		return cufValueService.getCufValueMapByEntityRef(campaignId, cufIdsMapByEntityType);
	}

	private String buildResultString(Iterator<Record> resultSet, List<CustomReportCustomExportColumn> selectedColumns, Map<EntityReference, Map<Long, Object>> cufMap) {
		StringBuilder dataBuilder = new StringBuilder();
		resultSet.forEachRemaining(record -> {
				for (CustomReportCustomExportColumn column : selectedColumns) {
					Object value = computeOutputValue(record, column, cufMap);
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
				dataBuilder.append(CARRIAGE_RETURN);
			}
		);
		return dataBuilder.toString();
	}

	/**
	 * Get the value corresponding to the given CustomReportCustomExportColumn among the given Record.
	 * @param record The Record, representing a Row of fetched data
	 * @param column The CustomReportCustomExportColumn we want the corresponding value in the Record
	 * @param cufMap The Map containing the CustomFieldValues
	 * @return The value corresponding to the given column among the given Record
	 */
	private Object computeOutputValue(Record record, CustomReportCustomExportColumn column, Map<EntityReference, Map<Long, Object>> cufMap) {
		CustomExportColumnLabel label = column.getLabel();
		Field columnField = label.getJooqTableField();
		Object value = null;
		if(label.equals(TEST_CASE_NATURE) || label.equals(TEST_CASE_TYPE)) {
			// Translate i18n keys of the info list items
			Object i18nKey = record.get(columnField);
			// This can be null if left joined with no Test Case
			if(i18nKey != null) {
				value = translator.internationalize(String.valueOf(i18nKey), locale);
			}
		} else if (label.equals(CAMPAIGN_DESCRIPTION) || label.equals(ITERATION_DESCRIPTION) || label.equals(TEST_SUITE_DESCRIPTION) || label.equals(TEST_CASE_DESCRIPTION) ||
			label.equals(TEST_CASE_PREREQUISITE) || label.equals(EXECUTION_COMMENT)|| label.equals(EXECUTION_STEP_COMMENT) || label.equals(EXECUTION_STEP_ACTION) || label.equals(EXECUTION_STEP_RESULT)) {
			// Clean Html content
			Object rawValue = record.get(columnField);
			value = computeRichValue(rawValue);
		} else if (columnField != null) {
			// Standard content
			value = record.get(columnField);
		} else {
			// Custom fields content
			long cufId = column.getCufId();
			EntityType entityType = label.getEntityType();
			Long entityId;
			switch(entityType) {
				case CAMPAIGN:
					entityId = record.get(CAMPAIGN.CLN_ID);
					break;
				case ITERATION:
					entityId = record.get(ITERATION.ITERATION_ID);
					break;
				case TEST_SUITE:
					entityId = record.get(TEST_SUITE.ID);
					break;
				case TEST_CASE:
					entityId = record.get(TEST_CASE.TCLN_ID);
					break;
				case EXECUTION:
					entityId = record.get(EXECUTION.EXECUTION_ID);
					break;
				case EXECUTION_STEP:
					entityId = record.get(EXECUTION_STEP.EXECUTION_STEP_ID);
					break;
				default:
					throw new RuntimeException("Unknown EntityType : " + entityType);
			}
			// entityId can be null if left joined with a non-existent entity
			if(entityId != null) {
				EntityReference entityReference = new EntityReference(entityType, entityId);
				Map<Long, Object> cufValuesMap = cufMap.get(entityReference);
				value = cufValuesMap.get(cufId);
			}
		}
		return value;
	}

	private String computeRichValue(Object rawValue) {
		if(rawValue == null) {
			return null;
		} else {
			String htmlFreeValue = HTMLCleanupUtils.htmlToText(HTMLCleanupUtils.cleanHtml(String.valueOf(rawValue)));
			return  removeCarriageReturnsAndReplaceDoubleQuotes(htmlFreeValue);
		}
	}

	private String removeCarriageReturnsAndReplaceDoubleQuotes(String text) {
		return text
			.replaceAll("\r\n", "")
			.replaceAll("\n", "")
			.replaceAll("\"", "\'");
	}

}
