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

import java.util.List;
import java.util.Locale;

import javax.inject.Inject;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.Paging;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.core.foundation.collection.Pagings;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.service.execution.ExecutionFinder;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelConstants;
import org.squashtest.tm.web.internal.model.datatable.DataTableSorting;
import org.squashtest.tm.web.internal.model.viewmapper.DatatableMapper;
import org.squashtest.tm.web.internal.model.viewmapper.NameBasedMapper;

@Controller
@RequestMapping("/test-cases/{testCaseId}/executions")
public class TestCaseExecutionsController {
	/**
	 * 
	 */
	private static final String NAME = "name";


	private final DatatableMapper<String> execsTableMapper = new NameBasedMapper()
	.mapAttribute(DataTableModelConstants.PROJECT_NAME_KEY, NAME, Project.class)
	.mapAttribute("campaign-name", NAME, Campaign.class)
	.mapAttribute("iteration-name", NAME, Iteration.class)
	.mapAttribute("exec-name", NAME, Execution.class)
	.mapAttribute("exec-mode", "executionMode", Execution.class)
	.mapAttribute("test-suite-name", NAME, TestSuite.class)
	.mapAttribute("exec-status", "executionStatus", Execution.class)
	.mapAttribute("last-exec-by", "lastExecutedBy", Execution.class)
	.mapAttribute("last-exec-on", "lastExecutedOn", Execution.class)
	.mapAttribute("dataset", "datasetLabel", Execution.class);


	private ExecutionFinder executionFinder;

	@Inject
	private InternationalizationHelper internationalizationHelper;



	/**
	 * Returns the
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping( method = RequestMethod.GET, params = "tab")
	public String getExecutionsTab(@PathVariable long testCaseId, Model model) {
		Paging paging = Pagings.DEFAULT_PAGING;

		List<Execution> executions = executionFinder.findAllByTestCaseIdOrderByRunDate(testCaseId, paging);

		model.addAttribute("executionsPageSize", paging.getPageSize());
		model.addAttribute("testCaseId", testCaseId);
		model.addAttribute("execs", executions);

		return "test-cases-tabs/executions-tab.html";
	}

	/**
	 * @param executionFinder
	 *            the executionFinder to set
	 */
	@Inject
	public void setExecutionFinder(ExecutionFinder executionFinder) {
		this.executionFinder = executionFinder;
	}

	@RequestMapping( params = RequestParams.S_ECHO_PARAM)
	@ResponseBody
	public DataTableModel getExecutionsTableModel(@PathVariable long testCaseId, DataTableDrawParameters params,
			Locale locale) {
		PagingAndSorting pas = createPagingAndSorting(params);

		PagedCollectionHolder<List<Execution>> executions = executionFinder.findAllByTestCaseId(testCaseId, pas);

		return new ExecutionsTableModelBuilder(locale, internationalizationHelper).buildDataModel(executions,
				params.getsEcho());
	}

	private PagingAndSorting createPagingAndSorting(DataTableDrawParameters params) {
		return new DataTableSorting(params, execsTableMapper);
	}

}
