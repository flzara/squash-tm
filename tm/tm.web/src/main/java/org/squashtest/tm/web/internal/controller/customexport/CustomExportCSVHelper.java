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

import org.squashtest.tm.domain.customreport.CustomExportColumnLabel;
import org.squashtest.tm.domain.customreport.CustomReportCustomExport;
import org.squashtest.tm.domain.customreport.CustomReportCustomExportColumn;
import org.squashtest.tm.service.customreport.CustomReportCustomExportCSVService;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;

import java.util.Locale;

public class CustomExportCSVHelper {

	private static final char seperator = ';';

	private CustomReportCustomExportCSVService csvService;
	private InternationalizationHelper translator;
	private Locale locale;

	public CustomExportCSVHelper(CustomReportCustomExportCSVService csvService, InternationalizationHelper translator, Locale locale) {
		this.csvService = csvService;
		this.translator = translator;
		this.locale = locale;
	}

	public String getInternationalizedHeaders(CustomReportCustomExport customExport) {
		StringBuilder builder = new StringBuilder();
		for(CustomReportCustomExportColumn column : customExport.getColumns()) {
			// TODO: Temporaire pour avoir un tableau cohérent  pour la vérification de la requête
			if(column.getLabel().getJooqTableField() != null) {
				// A column ' Name ' will be written ' "Name"; '
				CustomExportColumnLabel columnLabel = column.getLabel();
				builder.append("\"")
					.append(columnLabel.getEntityType() + "_" + translator.internationalize(columnLabel.getI18nKey(), locale))
					.append("\"")
					.append(seperator);
			}
		}
		builder.append("\n");
		return builder.toString();
	}

	public String getRowsData(CustomReportCustomExport customExport) {
		return csvService.getRowsData(customExport);
	}
}
