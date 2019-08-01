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
package org.squashtest.tm.web.internal.model.builder;

import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.EntityReference;
import org.squashtest.tm.domain.EntityType;
import org.squashtest.tm.domain.audit.AuditableMixin;
import org.squashtest.tm.domain.customreport.CustomExportColumnLabel;
import org.squashtest.tm.domain.customreport.CustomReportCustomExport;
import org.squashtest.tm.domain.customreport.CustomReportCustomExportColumn;
import org.squashtest.tm.service.customfield.CustomFieldFinderService;
import org.squashtest.tm.service.customreport.CustomReportCustomExportService;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.json.JsonCustomReportCustomExport;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


@Component("customReport.customExportBuilder")
@Scope("prototype")
public class JsonCustomExportBuilder {

	private static final String I18N_KEY_DATE_FORMAT = "squashtm.dateformat";

	@Inject
	private InternationalizationHelper i18nHelper;

	@Inject
	private CustomReportCustomExportService customExportService;

	@Inject
	private CustomFieldFinderService cufFinder;

	public JsonCustomReportCustomExport build(CustomReportCustomExport customExport, Locale locale) {
		JsonCustomReportCustomExport jsonCustomExport = new JsonCustomReportCustomExport();
		AuditableMixin auditable = (AuditableMixin) customExport;
		// fill with base attributes
		jsonCustomExport.setName(customExport.getName());
		fillBasicAttributes(auditable, jsonCustomExport, locale);
		// fill the scope
		fillScope(customExport, jsonCustomExport);
		// fill the columns
		fillColumns(customExport, jsonCustomExport, locale);
		return jsonCustomExport;
	}
	private void fillBasicAttributes(AuditableMixin auditable, JsonCustomReportCustomExport jsonCustomExport, Locale locale) {
		jsonCustomExport.setCreatedBy(auditable.getCreatedBy());
		jsonCustomExport.setCreatedOn(i18nHelper.localizeDate(auditable.getCreatedOn(), locale));
		jsonCustomExport.setLastModifiedBy(auditable.getLastModifiedBy());
		jsonCustomExport.setLastModifiedOn(i18nHelper.localizeDate(auditable.getLastModifiedOn(), locale));
	}

	private void fillScope(CustomReportCustomExport customExport, JsonCustomReportCustomExport jsonCustomExport) {
		List<String> stringifiedScope = new ArrayList<>();
		for (EntityReference scopeEntity : customExport.getScope()) {
			String scopeEntityName = customExportService.getScopeEntityName(scopeEntity);
			String scope = scopeEntityName.isEmpty() ? i18nHelper.internationalize(
				"custom-export.scope.not-available-anymore", LocaleContextHolder.getLocale()) : scopeEntityName;

			stringifiedScope.add(scope);
		}
		jsonCustomExport.setScope(stringifiedScope);
	}

	private void fillColumns(CustomReportCustomExport customExport, JsonCustomReportCustomExport jsonCustomExport, Locale locale) {
		// A LinkedHashMap keeps the insertion order of the Keys (Campaign > Iteration > Suite > TestCase > Execution > ExecutionStep > Issue)
		Map<EntityType, List<String>> entityTypeToLabelList = new LinkedHashMap<>();
		for(CustomReportCustomExportColumn column : customExport.getColumns()) {
			EntityType entityType = column.getLabel().getEntityType();
			CustomExportColumnLabel label = column.getLabel();
			Long cufId = column.getCufId();
			if(entityTypeToLabelList.get(entityType) == null) {
				entityTypeToLabelList.put(entityType, new ArrayList<>());
			}
			if (cufId == null) {
				entityTypeToLabelList.get(entityType).add(i18nHelper.internationalize(label.getI18nKey(), locale));
			} else {
				entityTypeToLabelList.get(entityType).add(cufFinder.findById(column.getCufId()).getLabel());
			}
		}
		jsonCustomExport.setColumns(entityTypeToLabelList);
	}


}
