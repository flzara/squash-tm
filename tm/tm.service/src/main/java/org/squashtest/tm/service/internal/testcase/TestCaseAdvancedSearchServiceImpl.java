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

import com.querydsl.core.Tuple;
import com.querydsl.jpa.hibernate.HibernateQuery;
import org.hibernate.Session;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.squashtest.tm.domain.IdentifiedUtil;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.search.AdvancedSearchFieldModelType;
import org.squashtest.tm.domain.search.AdvancedSearchQueryModel;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.internal.advancedsearch.AdvancedSearchColumnMappings;
import org.squashtest.tm.service.internal.advancedsearch.AdvancedSearchQueryModelToConfiguredQueryConverter;
import org.squashtest.tm.service.internal.advancedsearch.AdvancedSearchServiceImpl;
import org.squashtest.tm.service.internal.query.QueryProcessingServiceImpl;
import org.squashtest.tm.service.internal.repository.ProjectDao;
import org.squashtest.tm.service.internal.repository.TestCaseDao;
import org.squashtest.tm.service.requirement.RequirementVersionAdvancedSearchService;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.testcase.TestCaseAdvancedSearchService;
import org.squashtest.tm.service.testcase.VerifyingTestCaseManagerService;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.AUTOMATION_REQUEST_STATUS;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.EXECUTION_ISSUECOUNT;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.TEST_CASE_ATTCOUNT;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.TEST_CASE_AUTOMATABLE;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.TEST_CASE_CALLSTEPCOUNT;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.TEST_CASE_CREATED_BY;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.TEST_CASE_CREATED_ON;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.TEST_CASE_CUF_CHECKBOX;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.TEST_CASE_CUF_DATE;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.TEST_CASE_CUF_LIST;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.TEST_CASE_CUF_NUMERIC;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.TEST_CASE_CUF_TAG;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.TEST_CASE_CUF_TEXT;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.TEST_CASE_DATASETCOUNT;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.TEST_CASE_DESCRIPTION;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.TEST_CASE_EXECOUNT;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.TEST_CASE_ID;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.TEST_CASE_IMPORTANCE;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.TEST_CASE_ITERCOUNT;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.TEST_CASE_KIND;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.TEST_CASE_MILCOUNT;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.TEST_CASE_MILESTONE_END_DATE;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.TEST_CASE_MILESTONE_ID;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.TEST_CASE_MILESTONE_STATUS;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.TEST_CASE_MODIFIED_BY;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.TEST_CASE_MODIFIED_ON;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.TEST_CASE_NAME;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.TEST_CASE_NATURE;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.TEST_CASE_PARAMCOUNT;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.TEST_CASE_PREQUISITE;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.TEST_CASE_PROJECT_ID;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.TEST_CASE_PROJECT_NAME;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.TEST_CASE_REFERENCE;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.TEST_CASE_STATUS;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.TEST_CASE_STEPCOUNT;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.TEST_CASE_TYPE;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.TEST_CASE_VERSCOUNT;

@Service("squashtest.tm.service.TestCaseAdvancedSearchService")
public class TestCaseAdvancedSearchServiceImpl extends AdvancedSearchServiceImpl implements
	TestCaseAdvancedSearchService {

	private static final AdvancedSearchColumnMappings MAPPINGS = new AdvancedSearchColumnMappings("test-case-id");

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

	@Inject
	private Provider<AdvancedSearchQueryModelToConfiguredQueryConverter> converterProvider;

	@Override
	public List<String> findAllUsersWhoCreatedTestCases(List<Long> idList) {
		return projectDao.findUsersWhoCreatedTestCases(idList);
	}

	@Override
	public List<String> findAllUsersWhoModifiedTestCases(List<Long> idList) {
		return projectDao.findUsersWhoModifiedTestCases(idList);
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

	@Override
	public Page<TestCase> searchForTestCasesThroughRequirementModel(AdvancedSearchQueryModel model,
																	Pageable sorting, Locale locale) {

		List<TestCase> testcases = searchForTestCasesThroughRequirementModel(model, locale);

		int countAll = 0;
		return new PageImpl(testcases, sorting, countAll);
	}

	@Override
	public Page<TestCase> searchForTestCases(AdvancedSearchQueryModel model,
											 Pageable sorting, Locale locale) {

		Session session = entityManager.unwrap(Session.class);

		AdvancedSearchQueryModelToConfiguredQueryConverter converter = converterProvider.get();

		converter.configureModel(model).configureMapping(MAPPINGS);
		HibernateQuery<Tuple> query = converter.prepareFetchQuery();

		query = query.clone(session);
		List<Tuple> tuples = query.fetch();

		List<Long> testCaseIds = tuples.stream()
			.map(tuple -> tuple.get(0, Long.class))
			.collect(Collectors.toList());


		HibernateQuery<Long> countQuery = converter.prepareCountQuery();
		countQuery = countQuery.clone(session);
		long count = countQuery.fetchCount();

		List<TestCase> result = testCaseDao.findAllByIds(testCaseIds);

		// We should sort the result because findallbyids don't conserve the order
		//TODO : fix it
		Collections.sort(result, Comparator.comparing(item -> testCaseIds.indexOf(item.getId())));
		return new PageImpl(result, sorting, count);
	}

	static {

		MAPPINGS.getFormMapping()
			.map("attachments", TEST_CASE_ATTCOUNT)
			.map("callsteps", TEST_CASE_CALLSTEPCOUNT)
			.map("createdBy", TEST_CASE_CREATED_BY)
			.map("createdOn", TEST_CASE_CREATED_ON)
			.map("datasets", TEST_CASE_DATASETCOUNT)
			.map("description", TEST_CASE_DESCRIPTION)
			.map("executions", TEST_CASE_EXECOUNT)
			.map("id", TEST_CASE_ID)
			.map("importance", TEST_CASE_IMPORTANCE)
			.map("issues", EXECUTION_ISSUECOUNT)
			.map("iterations", TEST_CASE_ITERCOUNT)
			.map("kind", TEST_CASE_KIND)
			.map("lastModifiedBy", TEST_CASE_MODIFIED_BY)
			.map("lastModifiedOn", TEST_CASE_MODIFIED_ON)
			.map("milestone.label", TEST_CASE_MILESTONE_ID)
			.map("milestone.endDate", TEST_CASE_MILESTONE_END_DATE)
			.map("milestone.status", TEST_CASE_MILESTONE_STATUS)
			.map("name", TEST_CASE_NAME)
			.map("nature", TEST_CASE_NATURE)
			.map("parameters", TEST_CASE_PARAMCOUNT)
			.map("prerequisite", TEST_CASE_PREQUISITE)
			.map("project.id", TEST_CASE_PROJECT_ID)
			.map("reference", TEST_CASE_REFERENCE)
			.map("requirements", TEST_CASE_VERSCOUNT)
			.map("status", TEST_CASE_STATUS)
			.map("steps", TEST_CASE_STEPCOUNT)
			.map("type", TEST_CASE_TYPE)
			.map("automatable", TEST_CASE_AUTOMATABLE)
			.map("automationRequest.requestStatus", AUTOMATION_REQUEST_STATUS);

		MAPPINGS.getResultMapping()
			.map("project-name", TEST_CASE_PROJECT_NAME)
			.map("test-case-id", TEST_CASE_ID)
			.map("test-case-ref", TEST_CASE_REFERENCE)
			.map("test-case-label", TEST_CASE_NAME)
			.map("test-case-weight", TEST_CASE_IMPORTANCE)
			.map("test-case-nature", TEST_CASE_NATURE)
			.map("test-case-type", TEST_CASE_TYPE)
			.map("test-case-status", TEST_CASE_STATUS)
			.map("test-case-automatable", TEST_CASE_AUTOMATABLE)
			.map("test-case-milestone-nb", TEST_CASE_MILCOUNT)
			.map("test-case-requirement-nb", TEST_CASE_VERSCOUNT)
			.map("test-case-teststep-nb", TEST_CASE_STEPCOUNT)
			.map("test-case-iteration-nb", TEST_CASE_ITERCOUNT)
			.map("test-case-attachment-nb", TEST_CASE_ATTCOUNT)
			.map("test-case-created-by", TEST_CASE_CREATED_BY)
			.map("test-case-modified-by", TEST_CASE_MODIFIED_BY);

		MAPPINGS.getCufMapping()
			.map(AdvancedSearchFieldModelType.TAGS.toString(), TEST_CASE_CUF_TAG)
			.map(AdvancedSearchFieldModelType.CF_LIST.toString(), TEST_CASE_CUF_LIST)
			.map(AdvancedSearchFieldModelType.CF_SINGLE.toString(), TEST_CASE_CUF_TEXT)
			.map(AdvancedSearchFieldModelType.CF_TIME_INTERVAL.toString(), TEST_CASE_CUF_DATE)
			.map(AdvancedSearchFieldModelType.CF_NUMERIC_RANGE.toString(), TEST_CASE_CUF_NUMERIC)
			.map(AdvancedSearchFieldModelType.CF_CHECKBOX.toString(), TEST_CASE_CUF_CHECKBOX);
	}
}
