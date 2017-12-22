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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.util.HtmlUtils;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseExecutionMode;
import org.squashtest.tm.web.internal.controller.milestone.MilestoneModelUtils;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelBuilder;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelConstants;
import org.squashtest.tm.web.internal.util.HTMLCleanupUtils;

class VerifyingTestCasesTableModelHelper extends DataTableModelBuilder<TestCase> {

	private InternationalizationHelper helper;
	private Locale locale = LocaleContextHolder.getLocale();
	private static final int INT_MAX_DESCRIPTION_LENGTH = 50;

	public VerifyingTestCasesTableModelHelper(InternationalizationHelper helper){
		this.helper = helper;
	}

	@Override
	protected Object buildItemData(TestCase tc) {

		String type = formatExecutionMode(tc.getExecutionMode());

		Map<String, String> row = new HashMap<>(7);

		row.put("tc-id", tc.getId().toString());
		row.put("tc-index", Long.toString(getCurrentIndex()));
		row.put(DataTableModelConstants.PROJECT_NAME_KEY, tc.getProject().getName());
		row.put("tc-reference", tc.getReference());
		row.put("tc-name", tc.getName());
		row.put("tc-type", type);
		row.put("milestone-dates", MilestoneModelUtils.timeIntervalToString(tc.getMilestones(), helper, locale));
		row.put("empty-delete-holder", null);
		row.put("milestone", MilestoneModelUtils.milestoneLabelsOrderByDate(tc.getMilestones()));
		row.put("tc-description", HTMLCleanupUtils.getBriefText(tc.getDescription(), INT_MAX_DESCRIPTION_LENGTH));

		return row;
	}


	private String formatExecutionMode(TestCaseExecutionMode mode) {
		return helper.internationalize(mode, locale);
	}

}
