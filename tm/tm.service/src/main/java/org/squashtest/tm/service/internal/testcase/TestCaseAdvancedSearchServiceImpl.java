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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service("squashtest.tm.service.TestCaseAdvancedSearchService")
public class TestCaseAdvancedSearchServiceImpl extends AdvancedSearchServiceImpl implements
	TestCaseAdvancedSearchService {

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
	public List<TestCase> searchForTestCases(AdvancedSearchModel model, Locale locale) {
		return new ArrayList<>();

	}

	@Override
	public List<TestCase> searchForTestCasesThroughRequirementModel(AdvancedSearchModel model, Locale locale) {
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
	public Page<TestCase> searchForTestCasesThroughRequirementModel(AdvancedSearchModel model,
																	Pageable sorting, Locale locale) {

		List<TestCase> testcases = searchForTestCasesThroughRequirementModel(model, locale);

		int countAll=0;
		return new PageImpl(testcases,sorting, countAll);
	}

	@Override
	public Page<TestCase> searchForTestCases(AdvancedSearchModel model,
											 Pageable sorting, Locale locale) {

		List<TestCase> testcases = searchForTestCasesThroughRequirementModel(model, locale);

		int countAll=0;

		return new PageImpl(testcases,sorting, countAll);
	}


}
