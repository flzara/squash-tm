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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.tm.core.foundation.collection.ColumnFiltering;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndMultiSorting;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.testautomation.AutomatedSuite;
import org.squashtest.tm.service.testautomation.AutomatedSuiteManagerService;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableColumnFiltering;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.datatable.DataTableMultiSorting;
import org.squashtest.tm.web.internal.model.viewmapper.DatatableMapper;
import org.squashtest.tm.web.internal.model.viewmapper.NameBasedMapper;

import javax.inject.Inject;
import java.util.List;
import java.util.Locale;

@Controller
@RequestMapping("/iterations/{iterationId}/automated-suite")
public class IterationAutomatedSuiteManagerController {

	private static final String AUTOMATED_SUITE_ID = "automatedSuiteId";

	private static final Logger LOGGER = LoggerFactory.getLogger(IterationAutomatedSuiteManagerController.class);

	@Inject
	private AutomatedSuiteManagerService automatedSuiteManagerService;

	@Inject
	private InternationalizationHelper internationalizationHelper;

	private final DatatableMapper<String> automatedSuitesMapper = new NameBasedMapper()
		.map("entity-index", "index(AutomatedSuite)")
		// index is a special case which means : no sorting.
		.map("uuid", "id");

	@ResponseBody
	@GetMapping(params = RequestParams.S_ECHO_PARAM)
	public DataTableModel getAutomatedSuiteListModel(@PathVariable long iterationId, final DataTableDrawParameters params,
													 final Locale locale) {

		PagingAndMultiSorting paging = new DataTableMultiSorting(params, automatedSuitesMapper);
		ColumnFiltering filter = new DataTableColumnFiltering(params);
		PagedCollectionHolder<List<AutomatedSuite>> holder = automatedSuiteManagerService.getAutomatedSuitesByIterationID(iterationId, paging, filter);
		return new AutomatedSuiteTableModelHelper(locale, internationalizationHelper).buildDataModel(holder, params.getsEcho());
	}

	@GetMapping(value = "/{automatedSuiteId}/executions")
	public ModelAndView getExecutionsForTestPlan(@PathVariable(AUTOMATED_SUITE_ID) String automatedSuiteId) {
		LOGGER.debug("find model and view for executions of automated suite  #{}", automatedSuiteId);

		List<Execution> executionList = automatedSuiteManagerService.findExecutionsByAutomatedTestSuiteId(automatedSuiteId);

		ModelAndView mav = new ModelAndView("fragment/iterations/iteration-automated-suites-row");

		mav.addObject("executions", executionList);
		return mav;

	}
}
