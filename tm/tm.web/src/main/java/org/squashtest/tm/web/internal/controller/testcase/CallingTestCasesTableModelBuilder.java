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
package org.squashtest.tm.web.internal.controller.testcase;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.context.i18n.LocaleContextHolder;
import org.squashtest.tm.domain.testcase.CallTestStep;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelBuilder;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelConstants;
import org.squashtest.tm.web.internal.util.HTMLCleanupUtils;

class CallingTestCasesTableModelBuilder extends DataTableModelBuilder<CallTestStep> {

	private InternationalizationHelper i18nHelper;
	private Locale locale = LocaleContextHolder.getLocale();
	private static final int INT_MAX_DESCRIPTION_LENGTH = 50;


	CallingTestCasesTableModelBuilder(InternationalizationHelper i18nHelper){
		this.i18nHelper = i18nHelper;
	}

	@Override
	protected Map<String, Object> buildItemData(CallTestStep step) {

		Map<String, Object> row = new HashMap<>(8);

		TestCase caller = step.getTestCase();
		String dsName = findDatasetName(step);

		String executionMode = i18nHelper.internationalize(caller.getExecutionMode(), locale);

		row.put("tc-id", Long.toString(caller.getId()));
		row.put("tc-index", Long.toString(getCurrentIndex()));
		row.put(DataTableModelConstants.PROJECT_NAME_KEY, caller.getProject().getName());
		row.put("tc-reference", caller.getReference());
		row.put("tc-name", caller.getName());
		row.put("tc-mode", executionMode);
		row.put("ds-name", dsName);
		row.put("step-no", step.getIndex()+1);
		row.put("tc-description", HTMLCleanupUtils.getBriefText(caller.getDescription(), INT_MAX_DESCRIPTION_LENGTH));

		return row;

	}

	protected String findDatasetName(CallTestStep step){
		String name;
		switch (step.getParameterAssignationMode()){
		case NOTHING :
			name = "--";
			break;
		case DELEGATE :
			name = i18nHelper.getMessage("label.callstepdataset.Delegate", null, "label.callstepdataset.Delegate", locale);
			break;
		case CALLED_DATASET :
			name = step.getCalledDataset().getName();
			break;
		default :
			throw new IllegalArgumentException("the ParameterAssignationMode '"+step.getParameterAssignationMode()+"' is not supported");
		}

		return name;
	}

}
