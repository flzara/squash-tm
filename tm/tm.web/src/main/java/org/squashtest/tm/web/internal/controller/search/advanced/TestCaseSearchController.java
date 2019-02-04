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
package org.squashtest.tm.web.internal.controller.search.advanced;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndMultiSorting;
import org.squashtest.tm.domain.IdentifiedUtil;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.search.AdvancedSearchModel;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.campaign.CampaignTestPlanManagerService;
import org.squashtest.tm.service.campaign.IterationModificationService;
import org.squashtest.tm.service.campaign.IterationTestPlanManagerService;
import org.squashtest.tm.service.campaign.TestSuiteTestPlanManagerService;
import org.squashtest.tm.service.testcase.TestCaseAdvancedSearchService;
import org.squashtest.tm.service.testcase.VerifyingTestCaseManagerService;
import org.squashtest.tm.service.workspace.WorkspaceDisplayService;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.controller.search.advanced.tablemodels.TestCaseSearchResultDataTableModelBuilder;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelConstants;
import org.squashtest.tm.web.internal.model.datatable.DataTableMultiSorting;
import org.squashtest.tm.web.internal.model.viewmapper.DatatableMapper;
import org.squashtest.tm.web.internal.model.viewmapper.NameBasedMapper;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by jsimon on 04/05/2016.
 */

@Controller
public class TestCaseSearchController extends GlobalSearchController {


	// These are used by Lucene - Thus the columns are mapped to index
	// properties rather than class properties
	private DatatableMapper<String> testCaseSearchResultMapper = new NameBasedMapper(15)
		.mapAttribute(DataTableModelConstants.PROJECT_NAME_KEY, NAME, Project.class)
		.mapAttribute("test-case-id", "id", TestCase.class)
		.mapAttribute("test-case-ref", "reference", TestCase.class)
		.mapAttribute("test-case-label", "labelUpperCased", TestCase.class)
		.mapAttribute("test-case-weight", "importance", TestCase.class)
		.mapAttribute("test-case-nature", "nature", TestCase.class)
		.mapAttribute("test-case-type", "type", TestCase.class)
		.mapAttribute("test-case-status", "status", TestCase.class)
		.mapAttribute("test-case-automatable", "automatable", TestCase.class)
		.mapAttribute("test-case-milestone-nb", "milestones", TestCase.class)
		.mapAttribute("test-case-requirement-nb", "requirements", TestCase.class)
		.mapAttribute("test-case-teststep-nb", "steps", TestCase.class)
		.mapAttribute("test-case-iteration-nb", "iterations", TestCase.class)
		.mapAttribute("test-case-attachment-nb", "attachments", TestCase.class)
		.mapAttribute("test-case-created-by", "createdBy", TestCase.class)
		.mapAttribute("test-case-modified-by", "lastModifiedBy", TestCase.class);



	@Inject
	private TestCaseAdvancedSearchService testCaseAdvancedSearchService;

	@Inject
	private VerifyingTestCaseManagerService verifyingTestCaseManagerService;

	@Inject
	private CampaignTestPlanManagerService campaignTestPlanManagerService;

	@Inject
	private IterationTestPlanManagerService iterationTestPlanManagerService;

	@Inject
	private TestSuiteTestPlanManagerService testSuiteTestPlanManagerService;

	@Inject
	private IterationModificationService iterationService;

	@Inject
	@Named("testCaseWorkspaceDisplayService")
	private WorkspaceDisplayService testCaseWorkspaceDisplayService;
	
	
	// ************** the search test case page handlers *******************

	@RequestMapping(method = RequestMethod.GET, params="searchDomain="+TESTCASE)
	public String showTestCaseSearchPage(Model model,
								 @RequestParam(required = false, defaultValue = "") String associationType,
								 @RequestParam(required = false, defaultValue = "") Long associationId) {

		initSearchPageModel(model, "", associationType, associationId, TESTCASE);
		return  "test-case-search-input.html";
	}
	
	
	@RequestMapping(method = RequestMethod.POST, params="searchDomain="+TESTCASE)
	public String showTestCaseSearchPageWithSearchModel(Model model,
												 @RequestParam String searchModel, 
												 @RequestParam(required = false) String associationType,
												 @RequestParam(required = false) Long associationId) {

		initSearchPageModel(model, searchModel, associationType, associationId, TESTCASE);
		return  "test-case-search-input.html";
	}


	// ************** the search via requirements page handlers *******************

	@RequestMapping(method = RequestMethod.GET, params="searchDomain="+TESTCASE_VIA_REQUIREMENT)
	public String showTestCaseViaRequirementSearchPage(Model model,
								 @RequestParam(required = false, defaultValue = "") String associationType,
								 @RequestParam(required = false, defaultValue = "") Long associationId) {

		initSearchPageModel(model, "", associationType, associationId, TESTCASE_VIA_REQUIREMENT);
		return  "requirement-search-input.html";
	}

	@RequestMapping(method = RequestMethod.POST, params="searchDomain="+TESTCASE_VIA_REQUIREMENT)
	public String showTestCaseViaRequirementSearchPageWithSearchModel(Model model,
												 @RequestParam String searchModel,
												 @RequestParam(required = false) String associationType,
												 @RequestParam(required = false) Long associationId) {

		initSearchPageModel(model, searchModel, associationType, associationId, TESTCASE_VIA_REQUIREMENT);
		return  "requirement-search-input.html";
	}


	// ******************* the result page handlers ****************
	
	@RequestMapping(method = RequestMethod.POST, value = RESULTS, params="searchDomain="+TESTCASE)
	public String showTestCaseSearchResultPageFilledWithSearchModel(Model model,
	                                                              @RequestParam String searchModel,
	                                                              @RequestParam(required = false) String associationType,
	                                                              @RequestParam(required = false) Long associationId) {

		initResultModel(model, searchModel, associationType, associationId, TESTCASE);
		return "test-case-search-result.html";
	}

	@RequestMapping(method = RequestMethod.GET, value = RESULTS, params="searchDomain="+TESTCASE)
	public String getTestCaseSearchResultPage(Model model, 
											  @RequestParam(required = false) String associationType, 
											  @RequestParam(required = false) Long associationId) {

		initResultModel(model,"", associationType, associationId, TESTCASE);
		return "test-case-search-result.html";
	}
	
	@RequestMapping(method = RequestMethod.POST, value = RESULTS, params="searchDomain="+TESTCASE_VIA_REQUIREMENT)
	public String showTestCaseViaRequirementSearchResultPageFilledWithSearchModel(Model model,
	                                                              @RequestParam String searchModel,
	                                                              @RequestParam(required = false) String associationType,
	                                                              @RequestParam(required = false) Long associationId) {

		initResultModel(model, searchModel, associationType, associationId, TESTCASE_VIA_REQUIREMENT);
		return "test-case-search-result.html";
	}

	@RequestMapping(method = RequestMethod.GET, value = RESULTS, params="searchDomain="+TESTCASE_VIA_REQUIREMENT)
	public String getTestCaseViaRequirementSearchResultPage(Model model, 
											  @RequestParam(required = false) String associationType, 
											  @RequestParam(required = false) Long associationId) {

		initResultModel(model,"", associationType, associationId, TESTCASE_VIA_REQUIREMENT);
		return "test-case-search-result.html";
	}

		
	
	// ********************* other methods **********************************


	@RequestMapping(value = TABLE, method = RequestMethod.POST, params = { RequestParams.MODEL,
		TESTCASE_VIA_REQUIREMENT, RequestParams.S_ECHO_PARAM })
	@ResponseBody
	public DataTableModel getTestCaseThroughRequirementTableModel(final DataTableDrawParameters params,
																  final Locale locale, @RequestParam(value = RequestParams.MODEL) String model,
																  @RequestParam(required = false) String associationType, 
																  @RequestParam(required = false) Long associationId)
		throws IOException {

		AdvancedSearchModel searchModel = new ObjectMapper().readValue(model, AdvancedSearchModel.class);

		addMilestoneToSearchModel(searchModel);

		PagingAndMultiSorting paging = new DataTableMultiSorting(params, testCaseSearchResultMapper);

		PagedCollectionHolder<List<TestCase>> holder = testCaseAdvancedSearchService
			.searchForTestCasesThroughRequirementModel(searchModel, paging, locale);

		boolean isInAssociationContext = isInAssociationContext(associationType);

		Set<Long> ids = null;

		if (isInAssociationContext) {
			ids = getIdsOfTestCasesAssociatedWithObjects(associationType, associationId);
		}

		return new TestCaseSearchResultDataTableModelBuilder(locale, getMessageSource(), getPermissionService(), iterationService,
			isInAssociationContext, ids).buildDataModel(holder, params.getsEcho());
	}

	@RequestMapping(value = TABLE, method = RequestMethod.POST, params = { RequestParams.MODEL, TESTCASE,
		RequestParams.S_ECHO_PARAM })
	@ResponseBody
	public DataTableModel getTestCaseTableModel(final DataTableDrawParameters params, final Locale locale,
												@RequestParam(value = RequestParams.MODEL) String model,
												@RequestParam(required = false) String associationType, 
												@RequestParam(required = false) Long associationId)
		throws IOException {

		AdvancedSearchModel searchModel = new ObjectMapper().readValue(model, AdvancedSearchModel.class);

		addMilestoneToSearchModel(searchModel);
		PagingAndMultiSorting paging = new DataTableMultiSorting(params, testCaseSearchResultMapper);

		PagedCollectionHolder<List<TestCase>> holder =
				testCaseAdvancedSearchService.searchForTestCases(searchModel, paging, locale);

		boolean isInAssociationContext = isInAssociationContext(associationType);

		Set<Long> ids = null;

		if (isInAssociationContext) {
			ids = getIdsOfTestCasesAssociatedWithObjects(associationType, associationId);
		}

		return new TestCaseSearchResultDataTableModelBuilder(locale, getMessageSource(), getPermissionService(), iterationService,
			isInAssociationContext, ids).buildDataModel(holder, params.getsEcho());
	}

	private Set<Long> getIdsOfTestCasesAssociatedWithObjects(String associationType, Long id) {

		Set<Long> ids = new HashSet<>();

		if (REQUIREMENT.equals(associationType)) {
			List<TestCase> testCases = verifyingTestCaseManagerService.findAllByRequirementVersion(id);
			List<Long> tcIds = IdentifiedUtil.extractIds(testCases);
			ids.addAll(tcIds);

		} else if ("campaign".equals(associationType)) {
			List<Long> referencedTestCasesIds = this.campaignTestPlanManagerService.findPlannedTestCasesIds(id);
			ids.addAll(referencedTestCasesIds);
		} else if ("iteration".equals(associationType)) {
			List<TestCase> testCases = this.iterationTestPlanManagerService.findPlannedTestCases(id);
			List<Long> tcIds = IdentifiedUtil.extractIds(testCases);
			ids.addAll(tcIds);
		} else if ("testsuite".equals(associationType)) {
			List<Long> referencedTestCasesIds = this.testSuiteTestPlanManagerService.findPlannedTestCasesIds(id);
			ids.addAll(referencedTestCasesIds);
		}

		return ids;
	}

	@Override
	protected WorkspaceDisplayService workspaceDisplayService() {
		return testCaseWorkspaceDisplayService;
	}



}
