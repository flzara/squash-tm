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
package org.squashtest.tm.web.internal.controller.tf;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.util.HtmlUtils;
import org.squashtest.tm.domain.audit.AuditableMixin;
import org.squashtest.tm.domain.tf.automationrequest.AutomationRequest;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelBuilder;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelConstants;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AutomationRequestDataTableModelHelper extends DataTableModelBuilder<AutomationRequest> {
	private InternationalizationHelper messageSource;
	private Locale locale = LocaleContextHolder.getLocale();

	public AutomationRequestDataTableModelHelper(InternationalizationHelper messageSource) {
		this.messageSource = messageSource;;
	}

	@Override
	protected Object buildItemData(AutomationRequest item) {
		final AuditableMixin auditable = (AuditableMixin) item.getTestCase();
		Map<String, Object> data = new HashMap<>(14);
		data.put(DataTableModelConstants.PROJECT_NAME_KEY, item.getTestCase() != null ? HtmlUtils.htmlEscape(item.getTestCase().getProject().getName()): null);
		data.put("reference", (item.getTestCase() != null && !item.getTestCase().getReference().isEmpty()) ? item.getTestCase().getReference(): "-");
		data.put(DataTableModelConstants.DEFAULT_ENTITY_NAME_KEY, item.getTestCase() != null ? HtmlUtils.htmlEscape(item.getTestCase().getName()): null);
		data.put("format", item.getTestCase() != null ? messageSource.internationalize(item.getTestCase().getKind().getI18nKey(), locale) : null);
		data.put(DataTableModelConstants.DEFAULT_ENTITY_ID_KEY, item.getTestCase() != null ? item.getTestCase().getId() : null);
		data.put(DataTableModelConstants.DEFAULT_CREATED_BY_KEY, item.getTransmittedBy() != null ? item.getTransmittedBy().getLogin(): item.getCreatedBy().getLogin());
		data.put("transmitted-on", messageSource.localizeShortDate(item.getTransmissionDate(), locale));
		data.put("priority", item.getAutomationPriority() != null ? item.getAutomationPriority() : "-");
		data.put("assigned-on", messageSource.localizeShortDate(item.getAssignmentDate(), locale));
		data.put("entity-index", getCurrentIndex());
		data.put("script", (item.getTestCase() != null && item.getTestCase().getAutomatedTest() != null) ? item.getTestCase().getAutomatedTest().getFullLabel(): null);
		data.put("checkbox", "");
		data.put("tc-id", item.getTestCase() != null ? item.getTestCase().getId(): null);
		data.put("requestId", item.getId());
		data.put("assigned-to", item.getAssignedTo() != null ? item.getAssignedTo().getLogin() : "-");
		data.put("status", messageSource.internationalize(item.getRequestStatus().getI18nKey(), locale));
		return data;
	}

}
