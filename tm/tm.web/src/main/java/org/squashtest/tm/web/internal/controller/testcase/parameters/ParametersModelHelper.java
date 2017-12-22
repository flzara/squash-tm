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
package org.squashtest.tm.web.internal.controller.testcase.parameters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.context.MessageSource;
import org.squashtest.tm.core.foundation.collection.SortOrder;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.testcase.Parameter;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelBuilder;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelConstants;
import org.squashtest.tm.web.internal.util.HTMLCleanupUtils;

/**
 * Helps create the datas (for the jQuery DataTable) for the parameters table in the test case view.
 *
 * @author mpagnon
 *
 */
public final class ParametersModelHelper extends DataTableModelBuilder<Parameter> {

	private long ownerId;
	private MessageSource messageSource;
	private Locale locale;

	public ParametersModelHelper(long ownerId, MessageSource messageSource, Locale locale) {
		super();

		this.ownerId = ownerId;
		this.messageSource = messageSource;
		this.locale = locale;
	}

	@Override
	public Map<String, Object> buildItemData(Parameter item) {
		Map<String, Object> res = new HashMap<>();

		Long tcId = item.getTestCase().getId();
		boolean isDirectParam = Long.valueOf(ownerId).equals(tcId);
		String testCaseName = buildTestCaseName(item, isDirectParam);

		res.put(DataTableModelConstants.DEFAULT_ENTITY_ID_KEY, item.getId());
		res.put(DataTableModelConstants.DEFAULT_ENTITY_INDEX_KEY, getCurrentIndex());
		res.put(DataTableModelConstants.DEFAULT_ENTITY_NAME_KEY, ParametersModelHelper.buildParameterName(item, ownerId, messageSource, locale));
		res.put("description", item.getDescription());
		res.put("test-case-name", testCaseName);
		res.put("tc-id", tcId);
		res.put("directly-associated", isDirectParam);
		res.put(DataTableModelConstants.DEFAULT_EMPTY_DELETE_HOLDER_KEY, "");
		return res;
	}

	public static String buildParameterName(Parameter item, Long ownerId2, MessageSource messageSource2, Locale locale2) {
		String tcSmall = messageSource2.getMessage("label.testCases.short", null, locale2);
		TestCase paramTC = item.getTestCase();
		if (!ownerId2.equals(paramTC.getId())) {
			return item.getName() + " (" + tcSmall + "_" + paramTC.getId() + ")";
		} else {
			return item.getName();
		}
	}

	/**
	 * Will build the test case name for display in the table. The name will be : tReference-tcName (tcProjectName)
	 *
	 * @param item
	 * @param isDirectParam : if the parameter is shown in it's direct test case owner
	 * @return
	 */
	public static String buildTestCaseName(Parameter item, boolean isDirectParam) {
		if(isDirectParam){
			return "";
		}
		TestCase testCase = item.getTestCase();
		Project project = testCase.getProject();
		String testCaseName = testCase.getName() + " (" + project.getName() + ')';
		if (!testCase.getReference().isEmpty()) {
			testCaseName = testCase.getReference() + '-' + testCaseName;
		}
		return testCaseName;
	}


	/**
	 * Returns the list of column headers names, descriptions and ids for parameters in the Datasets table ordered by parameter name.
	 *
	 *
	 * @param testCaseId
	 *            : the concerned test case id
	 * @param locale
	 *            : the browser's locale
	 * @param directAndCalledParameters
	 *            : the list of parameters directly associated or associated through call steps
	 * @param messageSource
	 *            : the message source to internationalize suffix
	 * @return
	 */
	public static List<HashMap<String, String>> findDatasetParamHeaders(long testCaseId, final Locale locale,
			List<Parameter> directAndCalledParameters, MessageSource messageSource) {
		Collections.sort(directAndCalledParameters, new ParameterNameComparator(SortOrder.ASCENDING));
		List<HashMap<String, String>> result = new ArrayList<>(directAndCalledParameters.size());
		for (Parameter param : directAndCalledParameters) {
			HashMap<String, String> map = new HashMap<>();
			map.put("name",  ParametersModelHelper.buildParameterName(param, testCaseId, messageSource, locale));
			map.put("description",  HTMLCleanupUtils.htmlToText(param.getDescription()));
			map.put("id", param.getId().toString());
			result.add(map);
		}
		return result;
	}


}
