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

import org.squashtest.tm.domain.EntityType;
import org.squashtest.tm.domain.customreport.CustomExportColumnLabel;
import org.squashtest.tm.domain.customreport.CustomReportCustomExport;
import org.squashtest.tm.domain.customreport.CustomReportCustomExportColumn;
import org.squashtest.tm.service.customfield.CustomFieldFinderService;
import org.squashtest.tm.service.customreport.CustomReportCustomExportCSVService;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;

import java.util.Locale;

public class CustomExportCSVHelper {

	private static final char SEPARATOR = ';';
	private static final char UNDERSCORE = '_';

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
			if(column.getCufId() != null) {
				headerLabel = cufService.findById(column.getCufId()).getLabel();
			} else {
				headerLabel = translator.internationalize(columnLabel.getI18nKey(), locale);
			}
			// A column ' CAMPAIGN_LABEL ' will be written ' "CAMPAIGN_LABEL"; '
			builder.append("\"")
				.append(buildHeader(columnLabel.getEntityType(), headerLabel))
				.append("\"")
				.append(SEPARATOR);
		}
		builder.append("\n");
		return builder.toString();
	}

	private String buildHeader(EntityType entityType, String columnLabel) {
		return entityType.toString() + UNDERSCORE + columnLabel;
	}

	public String getRowsData(CustomReportCustomExport customExport) {
		return csvService.getRowsData(customExport);
	}
}
