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
package org.squashtest.tm.web.internal.controller.testcase.executions;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.tm.core.foundation.i18n.Internationalizable;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.web.internal.controller.campaign.TestSuiteHelper;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelBuilder;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelConstants;

/**
 * Builder of {@link DataTableModel} for the table of a test case's executions.
 * 
 * @author Gregory Fouquet
 * 
 */
/* package-private */class ExecutionsTableModelBuilder extends
DataTableModelBuilder<Execution> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionsTableModelBuilder.class);
	/**
	 * The locale to use to format the labels.
	 */
	private final Locale locale;
	/**
	 * The source for localized label messages.
	 */
	private final InternationalizationHelper i18nHelper;

	public ExecutionsTableModelBuilder(@NotNull Locale locale,
			@NotNull InternationalizationHelper i18nHelper) {
		super();
		this.locale = locale;
		this.i18nHelper = i18nHelper;
	}

	@Override
	protected Object buildItemData(Execution item) {
		IterationTestPlanItem testPlanItem = item.getTestPlan();
		Iteration iteration = testPlanItem.getIteration();

		Map<String, Object> data = new HashMap<>(12);

		data.put("exec-id", 		item.getId());
		data.put(DataTableModelConstants.PROJECT_NAME_KEY, 	iteration.getProject().getName());
		data.put("campaign-name", 	iteration.getCampaign().getName());
		data.put("iteration-name", 	iteration.getName());
		data.put("exec-name", item.getName() + " (Exec.#" + (1 + item.getExecutionOrder()) + ")");
		data.put("exec-mode", 		translate(item.getExecutionMode()));
		data.put("test-suite-name", testSuiteNameList(testPlanItem));
		data.put("raw-exec-status", item.getExecutionStatus().name());
		data.put("exec-status", 	translate(item.getExecutionStatus()));
		data.put("last-exec-by", 	item.getLastExecutedBy());
		data.put("last-exec-on",	i18nHelper.localizeShortDate(item.getLastExecutedOn(), locale));
		data.put("dataset", 		formatDatasetName(item));

		return data;
	}

	private String testSuiteNameList(IterationTestPlanItem item) {
		return TestSuiteHelper.buildEllipsedSuiteNameList(item.getTestSuites(), 20);
	}

	private String formatDatasetName(Execution exec){

		String dsLabel = exec.getDatasetLabel();
		if (! StringUtils.isBlank(dsLabel)){
			return dsLabel;
		}
		else{
			return i18nHelper.internationalize("label.noneDS", locale);
		}

	}

	private String translate(Internationalizable i18nable ){
		return i18nHelper.internationalize(i18nable, locale);
	}

}
