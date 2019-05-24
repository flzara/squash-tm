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
package org.squashtest.tm.service.internal.testcase;

import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.Paging;
import org.squashtest.tm.core.foundation.collection.PagingAndMultiSorting;
import org.squashtest.tm.core.foundation.collection.PagingBackedPagedCollectionHolder;
import org.squashtest.tm.domain.IdentifiedUtil;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.search.AdvancedSearchModel;
import org.squashtest.tm.domain.search.AdvancedSearchQueryModel;
import org.squashtest.tm.domain.search.QueryCufLabel;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.internal.advancedsearch.AdvancedSearchServiceImpl;
import org.squashtest.tm.service.internal.repository.ProjectDao;
import org.squashtest.tm.service.internal.repository.TestCaseDao;
import org.squashtest.tm.service.requirement.RequirementVersionAdvancedSearchService;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.testcase.TestCaseAdvancedSearchService;
import org.squashtest.tm.service.testcase.VerifyingTestCaseManagerService;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service("squashtest.tm.service.TestCaseAdvancedSearchService")
public class TestCaseAdvancedSearchServiceImpl extends AdvancedSearchServiceImpl implements
	TestCaseAdvancedSearchService {

	private static final Map<String, String> COLUMN_PROTOTYPE_MAPPING = new HashMap() {{
		put("attachments","TEST_CASE_ATTCOUNT");
		put("callsteps","TEST_CASE_CALLSTEPCOUNT");
		put("createdBy","TEST_CASE_CREATED_BY");
		put("createdOn","TEST_CASE_CREATED_ON");
		put("datasets","TEST_CASE_DATASETCOUNT");
		put("description","TEST_CASE_DESCRIPTION");
		put("executions","TEST_CASE_EXECOUNT");
		put("id","TEST_CASE_ID");
		put("importance","TEST_CASE_IMPORTANCE");
		//TODO
		put("issues","");
		put("iterations","TEST_CASE_ITERCOUNT");
		put("kind","TEST_CASE_KIND");
		put("lastModifiedBy","TEST_CASE_MODIFIED_BY");
		put("lastModifiedOn","TEST_CASE_MODIFIED_ON");
		put("milestone.endDate","TEST_CASE_MILESTONE_END_DATE");
		put("milestone.status","TEST_CASE_MILESTONE_STATUS");
		put("name", "TEST_CASE_NAME");
		put("nature", "TEST_CASE_NATURE_LABEL");
		put("parameters","TEST_CASE_PARAMCOUNT");
		put("prerequisite","TEST_CASE_PREQUISITE");
		put("project.id", "TEST_CASE_PROJECT");
		put("reference", "TEST_CASE_REFERENCE");
		put("requirements", "TEST_CASE_VERSCOUNT");
		put("status", "TEST_CASE_STATUS");
		put("steps", "TEST_CASE_STEPCOUNT");
		put("type", "TEST_CASE_TYPE");
		put("automatable", "TEST_CASE_AUTOMATABLE");
		put("automationRequest.requestStatus","AUTOMATION_REQUEST_STATUS");
		put(QueryCufLabel.TAGS, "TEST_CASE_CUF_TAG");
		put(QueryCufLabel.CF_LIST, "TEST_CASE_CUF_LIST");
		put(QueryCufLabel.CF_SINGLE, "TEST_CASE_CUF_TEXT");
		put(QueryCufLabel.CF_TIME_INTERVAL, "TEST_CASE_CUF_DATE");
		put(QueryCufLabel.CF_NUMERIC, "TEST_CASE_CUF_NUMERIC");
		put(QueryCufLabel.CF_CHECKBOX, "TEST_CASE_CUF_CHECKBOX");
		//TODO
		put("project-name", "");
		put("test-case-id", "TEST_CASE_ID");
		put("test-case-ref", "TEST_CASE_REFERENCE");
		put("test-case-label", "TEST_CASE_NAME");
		put("test-case-weight", "TEST_CASE_IMPORTANCE");
		put("test-case-nature", "TEST_CASE_NATURE");
		put("test-case-type", "TEST_CASE_TYPE");
		put("test-case-status", "TEST_CASE_STATUS");
		put("test-case-automatable", "TEST_CASE_AUTOMATABLE");
		put("test-case-milestone-nb", "TEST_CASE_MILCOUNT");
		put("test-case-requirement-nb", "TEST_CASE_VERSCOUNT");
		put("test-case-teststep-nb", "TEST_CASE_STEPCOUNT");
		put("test-case-iteration-nb", "TEST_CASE_ITERCOUNT");
		put("test-case-attachment-nb", "TEST_CASE_ATTCOUNT");
		put("test-case-created-by", "TEST_CASE_CREATED_BY");
		put("test-case-modified-by", "TEST_CASE_MODIFIED_BY");
	}};
	private static final List<String> PROJECTIONS = Arrays.asList("entity-index", "empty-openinterface2-holder",
		"empty-opentree-holder", "editable", "test-case-weight-auto");
	@PersistenceContext
	protected EntityManager entityManager;

	@Inject
	private ProjectDao projectDao;

	@Inject
	private TestCaseDao testCaseDao;

	@Inject
	private RequirementVersionAdvancedSearchService requirementSearchService;

	@Inject
	private VerifyingTestCaseManagerService verifyingTestCaseManagerService;

	@Inject
	private TestCaseCallTreeFinder testCaseCallTreeFinder;

	@Inject
	private MessageSource source;

	@Inject
	private PermissionEvaluationService permissionEvaluationService;

	private static final List<String> LONG_SORTABLE_FIELDS = Arrays.asList();

	private static final String FAKE_TC_ID = "-9000";

	@Override
	public List<String> findAllUsersWhoCreatedTestCases(List<Long> idList) {
		return projectDao.findUsersWhoCreatedTestCases(idList);
	}

	@Override
	public List<String> findAllUsersWhoModifiedTestCases(List<Long> idList) {
		return projectDao.findUsersWhoModifiedTestCases(idList);
	}


	@SuppressWarnings("unchecked")
	@Override
	public List<TestCase> searchForTestCases(AdvancedSearchQueryModel model, Locale locale) {
		return new ArrayList<>();

	}

	@Override
	public List<TestCase> searchForTestCasesThroughRequirementModel(AdvancedSearchQueryModel model, Locale locale) {
		List<RequirementVersion> requirements = requirementSearchService.searchForRequirementVersions(model, locale);
		List<TestCase> result = new ArrayList<>();
		Set<TestCase> testCases = new HashSet<>();
		// Get testcases from found requirements
		for (RequirementVersion requirement : requirements) {
			List<TestCase> verifiedTestCases = verifyingTestCaseManagerService.findAllByRequirementVersion(requirement
				.getId());
			testCases.addAll(verifiedTestCases);
		}

		// Get calling testcases
		Set<Long> callingTestCaseIds = new HashSet<>();
		for (TestCase testcase : testCases) {
			callingTestCaseIds.addAll(testCaseCallTreeFinder.getTestCaseCallers(testcase.getId()));
		}
		// add callees ids
		callingTestCaseIds.addAll(IdentifiedUtil.extractIds(testCases));
		// get all test cases
		//[Issue 7901] Only look for test cases user is allowed to read.
		result.addAll(testCaseDao.findAllByIds(
			callingTestCaseIds.stream()
			.filter(testCaseId -> permissionEvaluationService.hasRoleOrPermissionOnObject("ROLE_ADMIN", "READ", testCaseId, TestCase.class.getName()))
			.collect(Collectors.toList())
		));
		return result;
	}

	private String formatSortFieldName(String fieldName) {
		String result = fieldName;
		if (fieldName.startsWith("TestCase.")) {
			result = fieldName.replaceFirst("TestCase.", "");
		} else if (fieldName.startsWith("Project.")) {
			result = fieldName.replaceFirst("Project.", "project.");
		} else if (fieldName.startsWith("AutomationRequest.")) {
			result = fieldName.replaceFirst("AutomationRequest.", "automationRequest.");
		}
		return result;
	}

	@Override
	public Page<TestCase> searchForTestCasesThroughRequirementModel(AdvancedSearchQueryModel model,
																	Pageable sorting, Locale locale) {

		List<TestCase> testcases = searchForTestCasesThroughRequirementModel(model, locale);

		int countAll=0;
		return new PageImpl(testcases,sorting, countAll);
	}

	@Override
	public Page<TestCase> searchForTestCases(AdvancedSearchQueryModel model,
											 Pageable sorting, Locale locale) {

		List<TestCase> testcases = searchForTestCasesThroughRequirementModel(model, locale);

		int countAll=0;

		return new PageImpl(testcases,sorting, countAll);
	}


}
