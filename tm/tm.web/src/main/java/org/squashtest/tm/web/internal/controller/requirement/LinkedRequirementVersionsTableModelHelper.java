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
package org.squashtest.tm.web.internal.controller.requirement;

import org.springframework.context.i18n.LocaleContextHolder;
import org.squashtest.tm.domain.requirement.LinkedRequirementVersion;
import org.squashtest.tm.web.internal.controller.milestone.MilestoneModelUtils;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelBuilder;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelConstants;
import org.squashtest.tm.web.internal.util.HTMLCleanupUtils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

class LinkedRequirementVersionsTableModelHelper extends DataTableModelBuilder<LinkedRequirementVersion> {

	private InternationalizationHelper helper;
	private Locale locale = LocaleContextHolder.getLocale();
	private static final int INT_MAX_DESCRIPTION_LENGTH = 50;

	public LinkedRequirementVersionsTableModelHelper(InternationalizationHelper helper){
		this.helper = helper;
	}

	@Override
	protected Object buildItemData(LinkedRequirementVersion rv) {

		Map<String, String> row = new HashMap<>(11);

		row.put("rv-id", rv.getId().toString());
		row.put("rv-index", Long.toString(getCurrentIndex()));
		row.put(DataTableModelConstants.PROJECT_NAME_KEY, rv.getProject().getName());
		row.put("rv-reference", rv.getReference());
		row.put("rv-name", rv.getName());
		row.put("rv-version", Integer.toString(rv.getVersionNumber()));
		row.put("rv-role", formatRole(rv.getRole()));
		row.put("milestone-dates", MilestoneModelUtils.timeIntervalToString(rv.getMilestones(), helper, locale));
		row.put("empty-edit-holder", null);
		row.put("empty-delete-holder", null);
		row.put("milestone", MilestoneModelUtils.milestoneLabelsOrderByDate(rv.getMilestones()));
		row.put("rv-description", HTMLCleanupUtils.getBriefText(rv.getDescription(), INT_MAX_DESCRIPTION_LENGTH));

		return row;
	}

	private String formatRole(String role) {
		return helper.getMessage(role, null, role, locale);
	}


}
