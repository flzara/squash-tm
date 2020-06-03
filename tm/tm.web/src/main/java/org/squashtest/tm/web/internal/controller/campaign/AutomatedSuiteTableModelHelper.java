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
package org.squashtest.tm.web.internal.controller.campaign;


import org.squashtest.tm.core.foundation.lang.DateUtils;
import org.squashtest.tm.domain.audit.AuditableMixin;
import org.squashtest.tm.domain.testautomation.AutomatedSuite;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelBuilder;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelConstants;
import org.squashtest.tm.web.internal.util.HTMLCleanupUtils;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AutomatedSuiteTableModelHelper extends DataTableModelBuilder<AutomatedSuite> {

	/**
	 * The source for localized label messages.
	 */
	private final InternationalizationHelper i18nHelper;

	/**
	 * The locale to use to format the labels.
	 */
	private final Locale locale;

	public AutomatedSuiteTableModelHelper(@NotNull Locale locale, @NotNull InternationalizationHelper i18nHelper) {
		super();
		this.locale = locale;
		this.i18nHelper = i18nHelper;
	}

	@Override
	protected Map<String, Object> buildItemData(AutomatedSuite suite) {

		Map<String, Object> res = new HashMap<>();
		res.put(DataTableModelConstants.DEFAULT_ENTITY_INDEX_KEY, getCurrentIndex());
		res.put("uuid", suite.getId());
		AuditableMixin audit = (AuditableMixin) suite;
		res.put("created-on", i18nHelper.localizeDate(audit.getCreatedOn(), locale));
		res.put("created-by", HTMLCleanupUtils.escapeOrDefault(audit.getCreatedBy(), null));
		res.put("last-modified-on", i18nHelper.localizeDate(audit.getLastModifiedOn(), locale));
		res.put("last-modified-by", HTMLCleanupUtils.escapeOrDefault(audit.getLastModifiedBy(), null));
		res.put("status", suite.getExecutionStatus().getCanonicalStatus());
		return res;
	}
}
