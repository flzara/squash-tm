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

import static org.squashtest.tm.domain.customreport.CustomExportColumnLabel.CAMPAIGN_PROGRESS_STATUS;
import static org.squashtest.tm.domain.customreport.CustomExportColumnLabel.TEST_CASE_NATURE;
import static org.squashtest.tm.domain.customreport.CustomExportColumnLabel.TEST_CASE_TYPE;

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

	/**
	 * Build the String which will compose the headers of the export csv file.
	 * @param customExport The CustomReportCustomExport that is to be exported
	 * @return A String representing the headers to print in the export csv file.
	 */
	public String getInternationalizedHeaders(CustomReportCustomExport customExport) {
		StringBuilder builder = new StringBuilder();
		for(CustomReportCustomExportColumn column : customExport.getColumns()) {
			// Ex: The column ' Label ' of a Campaign will be written ' "CPG - Label"; ' in the .csv file
			builder.append(ESCAPED_QUOTE)
				.append(buildInternationalizedHeaderLabel(column))
				.append(ESCAPED_QUOTE)
				.append(SEPARATOR);
		}
		builder.append(CARRIAGE_RETURN);
		return builder.toString();
	}
	/**
	 * Build the internationalized header label of a given CustomReportCustomExportColumn. The header label is
	 * composed of a prefix specific to the Entity, concatenated with the internationalized column name.
	 * @param column The CustomReportCustomExportColumn
	 * @return The internationalized header label of the given CustomReportCustomExportColumn
	 */
	private String buildInternationalizedHeaderLabel(CustomReportCustomExportColumn column) {
		CustomExportColumnLabel columnLabel = column.getLabel();
		String internationalizedHeaderName = getInternationalizedHeaderName(column);
		return columnLabel.getShortenedEntityType() + SPACE_DASH_SPACE + internationalizedHeaderName;
	}
	/**
	 * Given a CustomReportCustomExportColumn, get its translated header name. If the columns corresponds to a
	 * CustomField, the CustomField name is given without translation.
	 * @param column The CustomReportCustomExportColumn
	 * @return The translated name of the given CustomReportCustomExportColumn
	 */
	private String getInternationalizedHeaderName(CustomReportCustomExportColumn column) {
		if(column.getCufId() == null) {
			return translator.internationalize(column.getLabel().getI18nKey(), locale);
		} else {
			return cufService.findById(column.getCufId()).getLabel();
		}
	}


	/**
	 * Build the String which will compose the data of the export csv file.
	 * @param customExport The CustomReportCustomExport than is to be exported
	 * @return A String representing the data to print in the export csv file
	 */
	public String getWritableRowsData(CustomReportCustomExport customExport) {
		// Extract the Map of requested Cuf ids by EntityType
		Map<EntityType, List<Long>> entityTypeToCufIdsListMap = extractEntityTypeToCufIdsMap(customExport);
		// Main request
		Iterator<Record> rowsData = csvService.getRowsData(customExport, entityTypeToCufIdsListMap.keySet());
		// Side request for complex aggregate columns
		Object campaignSuccessRate = csvService.computeCampaignProgressRate(customExport);
		// Side request for all the CustomFieldValues
		Map<EntityReference, Map<Long, Object>> cufValuesMapByEntityReference = getEntityRefToCufValuesMapMap(customExport, entityTypeToCufIdsListMap);
		return buildResultString(rowsData, customExport.getColumns(), cufValuesMapByEntityReference, campaignSuccessRate);
	}

	/**
	 * Extract the Map of all requested CustomField ids by EntityType.
	 * @param customExport The CustomExport
	 * @return A Map of all requested CustomField ids by EntityType
	 */
	private Map<EntityType, List<Long>> extractEntityTypeToCufIdsMap(CustomReportCustomExport customExport) {
		// Extract Cuf ids Map group by EntityTypes
		return customExport.getColumns()
			.stream()
			.filter(column -> column.getCufId() != null)
			.collect(
				Collectors.groupingBy(column->column.getLabel().getEntityType(),
					Collectors.mapping(CustomReportCustomExportColumn::getCufId, Collectors.toList())));
	}

	/**
	 * Given a CustomExport, extract a Map which keys are all EntityReferences involved in the export
	 * and values are Maps of all the CustomFieldValues mapped by CustomField ids.
	 * @param customExport The CustomExport
	 * @return A Map<EntityReference, Map<Long, Object>> where the second Map gives CustomFieldValues
	 * mapped by CustomField ids.
	 */
	private Map<EntityReference, Map<Long, Object>> getEntityRefToCufValuesMapMap(CustomReportCustomExport customExport, Map<EntityType, List<Long>> entityTypeToCufIdsListMap) {
		// If no CustomFields were requested, nothing else is to do
		if(entityTypeToCufIdsListMap.isEmpty()) {
			return null;
		}
		EntityReference campaign = customExport.getScope().get(0);
		return cufValueService.getCufValueMapByEntityRef(campaign.getId(), entityTypeToCufIdsListMap);
	}

	/**
	 * Given all the data fetched previously in the process, build the String which will compose the data of the export
	 * csv file.
	 * @param resultSet The result of the main request containing all the fetched data
	 * @param selectedColumns The selected columns of the CustomExport
	 * @param cufMap The CustomFieldValues Map
	 * @param campaignProgressRate The scope Campaign progress rate
	 * @return
	 */
	private String buildResultString(Iterator<Record> resultSet, List<CustomReportCustomExportColumn> selectedColumns, Map<EntityReference, Map<Long, Object>> cufMap, Object campaignProgressRate) {
		StringBuilder dataBuilder = new StringBuilder();
		resultSet.forEachRemaining(record -> {
				for (CustomReportCustomExportColumn column : selectedColumns) {
					Object value = computeOutputValue(record, column, cufMap, campaignProgressRate);
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
	private Object computeOutputValue(Record record, CustomReportCustomExportColumn column, Map<EntityReference, Map<Long, Object>> cufMap, Object campaignSuccessRate) {
		CustomExportColumnLabel label = column.getLabel();
		Field columnField = label.getJooqTableField();
		Object value = null;
		if(label.equals(TEST_CASE_NATURE) || label.equals(TEST_CASE_TYPE)) {
			// Translate i18n keys of the info list items
			Object i18nKey = record.get(columnField);
			// This can be null if left joined with non-existent entity
			if(i18nKey != null) {
				String i18nKeyString = String.valueOf(i18nKey);
				value = translator.getMessage(i18nKeyString, null, i18nKeyString, locale);
			}
		} else if (CustomExportColumnLabel.getRichTextFieldsSet().contains(label)) {
			// Clean Html content
			Object rawValue = record.get(columnField);
			value = computeRichValue(rawValue);
		} else if(label.equals(CAMPAIGN_PROGRESS_STATUS)) {
			value = campaignSuccessRate;
		} else if (columnField != null) {
			// Standard content
			value = record.get(columnField);
		} else {
			// Custom fields content
			long cufId = column.getCufId();
			EntityType entityType = label.getEntityType();
			Long entityId = record.get(CustomExportColumnLabel.getEntityTypeToIdTableFieldMap().get(entityType));
			// entityId can be null if left joined with a non-existent entity
			if(entityId != null) {
				EntityReference entityReference = new EntityReference(entityType, entityId);
				Map<Long, Object> cufValuesMap = cufMap.get(entityReference);
				// this map can be null if the entityReference exists but can't have cuf
				// it happens for ExecutionSteps from gherkin TestCase executions
				if(cufValuesMap != null) {
					value = computeRichValue(cufValuesMap.get(cufId));
				}
			}
		}
		if(value != null && CustomExportColumnLabel.getCustomizableTextFieldsSet().contains(label)) {
			value = replaceDoubleQuotes(value.toString());
		}
		return value;
	}

	/**
	 * Format output value for a rich text.
	 * @param rawValue The raw value
	 * @return The cleaned output value of the given rich value.
	 */
	private String computeRichValue(Object rawValue) {
		if(rawValue == null) {
			return null;
		} else {
			return HTMLCleanupUtils.htmlToText(HTMLCleanupUtils.cleanHtml(String.valueOf(rawValue)));
		}
	}
	/**
	 * Replace all double quotes by simple ones.
	 * @param text The text.
	 * @return The text which double quotes were replaced by simple ones.
	 */
	private String replaceDoubleQuotes(String text) {
		return text.replaceAll("\"", "\'");
	}

}
