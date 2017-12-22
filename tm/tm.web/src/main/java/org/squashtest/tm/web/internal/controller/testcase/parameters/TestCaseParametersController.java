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

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.validation.Valid;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.SinglePageCollectionHolder;
import org.squashtest.tm.core.foundation.collection.SortOrder;
import org.squashtest.tm.core.foundation.collection.Sorting;
import org.squashtest.tm.domain.IdentifiedUtil;
import org.squashtest.tm.domain.testcase.Parameter;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.exception.DomainException;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.testcase.ParameterModificationService;
import org.squashtest.tm.service.testcase.TestCaseFinder;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.controller.milestone.MilestoneFeatureConfiguration;
import org.squashtest.tm.web.internal.controller.milestone.MilestoneUIConfigurationService;
import org.squashtest.tm.web.internal.controller.widget.AoColumnDef;
import org.squashtest.tm.web.internal.helper.JsonHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelConstants;
import org.squashtest.tm.web.internal.model.datatable.DataTableSorting;
import org.squashtest.tm.web.internal.model.viewmapper.DatatableMapper;
import org.squashtest.tm.web.internal.model.viewmapper.NameBasedMapper;

/**
 * Controller to handle requests for parameters of a given test case.
 *
 * @author mpagnon
 *
 */
@RequestMapping("/test-cases/{testCaseId}/parameters")
@Controller
public class TestCaseParametersController {
	@Inject
	private TestCaseFinder testCaseFinder;
	@Inject
	private ParameterModificationService parameterModificationService;

	@Inject
	private PermissionEvaluationService permissionEvaluationService;

	@Inject
	private MessageSource messageSource;

	@Inject
	private MilestoneUIConfigurationService milestoneConfService;


	private DatatableMapper<String> parametersTableMapper =
			new NameBasedMapper(3)
	.mapAttribute(DataTableModelConstants.DEFAULT_ENTITY_NAME_KEY,"name", Parameter.class)
	.mapAttribute("test-case-name", "name", TestCase.class);

	/**
	 *
	 */
	private static final String TEST_CASE = "testCase";

	/**
	 * Will find all parameters concerned by the test case (directly and through call step) and return them as a list of
	 * {@link SimpleParameter} ordered by name.
	 *
	 * @param testCaseId
	 *            : the id of the concerned test case
	 * @return the list of all parameters ordered by name as {@link SimpleParameter}s
	 */
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public List<SimpleParameter> getParameters(@PathVariable("testCaseId") long testCaseId, Locale locale) {
		List<Parameter> parameters = parameterModificationService.findAllParameters(testCaseId);
		return SimpleParameter.convertToSimpleParameters(parameters,testCaseId, messageSource, locale );
	}

	/**
	 * Will return the test case's parameter tab on the test case view.
	 *
	 * @param testCaseId : the id of the viewed test case
	 * @param model : the {@link Model}
	 * @param locale : the browser's locale
	 * @return
	 */
	@RequestMapping(value = "/panel", method = RequestMethod.GET)
	public String getParametersPanel(@PathVariable("testCaseId") long testCaseId, Model model, Locale locale) {

		// the main entities
		TestCase testCase = testCaseFinder.findById(testCaseId);

		List<Parameter> directAndCalledParameters = parameterModificationService.findAllParameters(testCaseId);

		/*
		 * Issue 3871
		 *
		 * The problem comes from the configuration of the datatable, which is splitted in two parts :
		 * the aoColumnDefs (that binds the model to the columns ) and the paramHeaders (that binds
		 * some label to the columns). Those informations are extracted from the same original collection
		 * 'directAndCalledParameters'.
		 *
		 * It happens that 'paramHeaders' is sorted when generated, while 'aoColumnDefs' is not, hence
		 * the discrepency.
		 *
		 * A simple solution is then to sort 'directAndCalledParameters' before deriving the informations
		 * so that they remain consistent.
		 */
		Collections.sort(directAndCalledParameters, new ParameterNameComparator(SortOrder.ASCENDING));

		List<Long> paramIds = IdentifiedUtil.extractIds(directAndCalledParameters);
		boolean editable = permissionEvaluationService.hasRoleOrPermissionOnObject("ROLE_ADMIN", "WRITE", testCase);
		List<AoColumnDef> columnDefs = new DatasetsTableColumnDefHelper().getAoColumnDefs(paramIds, editable);
		List<HashMap<String, String>> paramHeaders = ParametersModelHelper.findDatasetParamHeaders(testCaseId, locale, directAndCalledParameters, messageSource);

		MilestoneFeatureConfiguration milestoneConf = milestoneConfService.configure(testCase);

		// populate the model
		model.addAttribute(TEST_CASE, testCase);
		model.addAttribute("datasetsAoColumnDefs", JsonHelper.serialize(columnDefs));
		model.addAttribute("paramHeaders", paramHeaders);
		model.addAttribute("milestoneConf", milestoneConf);

		// return
		return "test-cases-tabs/parameters-tab.html";

	}


	/**
	 * Will return the data model for the DataTable of parameters in the parameter tab of the test case view.
	 *
	 * @param testCaseId
	 *            : the id of the viewed test case
	 * @param params
	 *            : the DataTable parameters
	 * @param locale
	 *            : the browser's locale
	 * @return the parameters tab view.
	 */
	@RequestMapping(method = RequestMethod.GET, params = RequestParams.S_ECHO_PARAM)
	@ResponseBody
	public DataTableModel getParametersTable(@PathVariable long testCaseId, final DataTableDrawParameters params,
			final Locale locale) {

		List<Parameter> parameters = parameterModificationService.findAllParameters(testCaseId);
		Sorting sorting = new DataTableSorting(params, parametersTableMapper);
		sortParams(sorting, parameters, testCaseId);
		PagedCollectionHolder<List<Parameter>> holder = new SinglePageCollectionHolder<>(parameters);

		return new ParametersModelHelper(testCaseId, messageSource, locale).buildDataModel(holder,
				params.getsEcho());
	}

	/**
	 * will sort the given list of parameters according to the given params. If params are not defined , will order by
	 * parameter name ascending.
	 *
	 * @param params
	 *            : the {@link DataTableDrawParameters}
	 * @param parameters
	 *            : a list of {@link Parameter}
	 *            @param testCaseId : the id of the test case that displays the table of parameters
	 */
	private void sortParams(Sorting sorting, List<Parameter> parameters, Long testCaseId) {
		String sortedAttribute = sorting.getSortedAttribute();
		SortOrder sortOrder = sorting.getSortOrder();

		if (sortedAttribute != null && "Parameter.name".equals(sortedAttribute)) {

			Collections.sort(parameters, new ParameterNameComparator(sortOrder));
		} else if (sortedAttribute != null && "TestCase.name".equals(sortedAttribute)) {
			Collections.sort(parameters, new ParameterTestCaseNameComparator(sortOrder, testCaseId));
		} else {
			Collections.sort(parameters, new ParameterNameComparator(SortOrder.ASCENDING));
		}

	}

	/**
	 * Will compare {@link Parameter} on their test case name in the given {@link SortOrder}. The compared test case
	 * name is the one displayed in the parameter table on the test case view.
	 *
	 * @see {@link ParametersModelHelper#buildTestCaseName(Parameter)}
	 *
	 * @author mpagnon
	 *
	 */
	@SuppressWarnings("serial")
	private static final class ParameterTestCaseNameComparator implements Comparator<Parameter>, Serializable {

		private SortOrder sortOrder;
		private Long testCaseId;

		private ParameterTestCaseNameComparator(SortOrder sortOrder, Long testCaseId) {
			this.sortOrder = sortOrder;
			this.testCaseId = testCaseId;
		}

		@Override
		public int compare(Parameter o1, Parameter o2) {
			boolean o1DirectParam = testCaseId.equals(o1.getTestCase().getId());
			boolean o2DirectParam = testCaseId.equals(o2.getTestCase().getId());
			int ascResult = ParametersModelHelper.buildTestCaseName(o1, o1DirectParam).compareTo(
					ParametersModelHelper.buildTestCaseName(o2, o2DirectParam));
			if (sortOrder == SortOrder.ASCENDING) {
				return ascResult;
			} else {
				return -ascResult;
			}
		}

	}

	/**
	 * Will add a new parameter to the test case
	 *
	 * @param testCaseId
	 *            : the id of the test case that will hold the new parameter
	 * @param parameter
	 *            : the parameter to add with it's set name and description
	 */
	@RequestMapping(value = "/new", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.CREATED)
	@ResponseBody
	public void newParameter(@PathVariable long testCaseId, @Valid @ModelAttribute("add-parameter") Parameter parameter) {

		try{
			parameterModificationService.addNewParameterToTestCase(parameter, testCaseId);
		}catch (DomainException e) {
			e.setObjectName("add-parameter");
			throw e;
		}
	}


}
