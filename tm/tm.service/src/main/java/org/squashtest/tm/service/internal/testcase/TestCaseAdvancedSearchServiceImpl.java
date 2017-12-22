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

import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.squashtest.tm.core.foundation.collection.*;
import org.squashtest.tm.domain.IdentifiedUtil;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.search.AdvancedSearchListFieldModel;
import org.squashtest.tm.domain.search.AdvancedSearchModel;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.internal.advancedsearch.AdvancedSearchServiceImpl;
import org.squashtest.tm.service.internal.infolist.InfoListItemComparatorSource;
import org.squashtest.tm.service.internal.repository.ProjectDao;
import org.squashtest.tm.service.internal.repository.TestCaseDao;
import org.squashtest.tm.service.requirement.RequirementVersionAdvancedSearchService;
import org.squashtest.tm.service.testcase.TestCaseAdvancedSearchService;
import org.squashtest.tm.service.testcase.VerifyingTestCaseManagerService;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;

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

	private static final SortField[] DEFAULT_SORT_TESTCASES = new SortField[]{
		new SortField("project.name", SortField.Type.STRING, false),
		new SortField("reference", SortField.Type.STRING, false), new SortField("importance", SortField.Type.STRING, false),
		new SortField("label", SortField.Type.STRING, false)};

	private static final List<String> LONG_SORTABLE_FIELDS = Arrays.asList("requirements", "steps", "id", "iterations",
		"attachments");

	private static final String FAKE_TC_ID = "-9000";

	@Override
	public List<String> findAllUsersWhoCreatedTestCases(List<Long> idList) {
		return projectDao.findUsersWhoCreatedTestCases(idList);
	}

	@Override
	public List<String> findAllUsersWhoModifiedTestCases(List<Long> idList) {
		return projectDao.findUsersWhoModifiedTestCases(idList);
	}


	/*
	 * That implementation is special because we cannot process the milestones as usual. Indeed, we need the test cases that belongs both directly and indirectly to the
	 * milestone. That's why we use the method noMilestoneLuceneQuery.
	 *
	 * (non-Javadoc)
	 * @see org.squashtest.tm.service.testcase.TestCaseAdvancedSearchService#searchForTestCases(org.squashtest.tm.domain.search.AdvancedSearchModel, java.util.Locale)
	 */
	/*
	 * TODO :
	 *
	 * This method is basically an override of "buildLuceneQuery" defined in the superclass -> thus we could rename it accordingly.
	 * However in method "searchForTestCasesThroughRequirementModel" we must use the super implementation of "buildLuceneQuery" -> thus renaming
	 * "searchTestCaseQuery" to "buildLuceneQuery" could lead to ambiguity.
	 *
	 * I don't know what to do about it.
	 */
	protected Query searchTestCasesQuery(AdvancedSearchModel model, FullTextEntityManager ftem, Locale locale) {

		QueryBuilder qb = ftem.getSearchFactory().buildQueryBuilder().forEntity(TestCase.class).get();

		/*
		 * we must not include the milestone criteria yet because
		 * it'll be the subject of a separate query.
		 *
		 * Let's save the search model and create a milestone-stripped
		 * version of it
		 */

		AdvancedSearchModel modelCopy = model.shallowCopy();
		removeMilestoneSearchFields(model);

		// create the main query (search test cases, no milestones)
		Query luceneQuery = buildCoreLuceneQuery(qb, model);

		// now add the test-cases specific milestones criteria
		if (shouldSearchByMilestones(modelCopy)) {
			luceneQuery = addAggregatedMilestonesCriteria(luceneQuery, qb, modelCopy, locale);
		}

		return luceneQuery;

	}


	@SuppressWarnings("unchecked")
	@Override
	public List<TestCase> searchForTestCases(AdvancedSearchModel model, Locale locale) {

		FullTextEntityManager ftem = Search.getFullTextEntityManager(entityManager);

		Query luceneQuery = searchTestCasesQuery(model, ftem, locale);

		FullTextQuery hibQuery = ftem.createFullTextQuery(luceneQuery, TestCase.class);

		return hibQuery.getResultList();

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
		result.addAll(testCaseDao.findAllByIds(callingTestCaseIds));
		return result;
	}

	private Sort getTestCaseSort(PagingAndMultiSorting multisorting) {

		Locale locale = LocaleContextHolder.getLocale();

		List<Sorting> sortings = multisorting.getSortings();

		if (sortings == null || sortings.isEmpty()) {
			return new Sort(DEFAULT_SORT_TESTCASES);
		}

		boolean isReverse = true;
		SortField[] sortFieldArray = new SortField[sortings.size()];

		for (int i = 0; i < sortings.size(); i++) {
			if (SortOrder.ASCENDING == sortings.get(i).getSortOrder()) {
				isReverse = false;
			}

			String fieldName = sortings.get(i).getSortedAttribute();
			fieldName = formatSortFieldName(fieldName);

			if (LONG_SORTABLE_FIELDS.contains(fieldName)) {
				sortFieldArray[i] = new SortField(fieldName, SortField.Type.LONG, isReverse);
			} else if ("nature".equals(fieldName) || "type".equals(fieldName)) {
				sortFieldArray[i] = new SortField(fieldName, new InfoListItemComparatorSource(source,
					locale), isReverse);
			} else {
				sortFieldArray[i] = new SortField(fieldName, SortField.Type.STRING, isReverse);
			}
		}

		return new Sort(sortFieldArray);
	}

	private String formatSortFieldName(String fieldName) {
		String result = fieldName;
		if (fieldName.startsWith("TestCase.")) {
			result = fieldName.replaceFirst("TestCase.", "");
		} else if (fieldName.startsWith("Project.")) {
			result = fieldName.replaceFirst("Project.", "project.");
		}
		return result;
	}

	@Override
	public PagedCollectionHolder<List<TestCase>> searchForTestCasesThroughRequirementModel(AdvancedSearchModel model,
		PagingAndMultiSorting sorting, Locale locale) {

		List<TestCase> testcases = searchForTestCasesThroughRequirementModel(model, locale);

		FullTextEntityManager ftem = Search.getFullTextEntityManager(entityManager);

		QueryBuilder qb = ftem.getSearchFactory().buildQueryBuilder().forEntity(TestCase.class).get();

		Query luceneQuery = super.buildLuceneQuery(qb, testcases);

		return fetchPagedResults(ftem, luceneQuery, sorting);
	}

	@Override
	public PagedCollectionHolder<List<TestCase>> searchForTestCases(AdvancedSearchModel model,
		PagingAndMultiSorting sorting, Locale locale) {

		FullTextEntityManager ftem = Search.getFullTextEntityManager(entityManager);

		Query luceneQuery = searchTestCasesQuery(model, ftem, locale);

		return fetchPagedResults(ftem, luceneQuery, sorting);
	}

	private PagedCollectionHolder<List<TestCase>> fetchPagedResults(FullTextEntityManager ftem, Query luceneQuery, PagingAndMultiSorting sorting) {
		List<TestCase> result = Collections.emptyList();
		int countAll = 0;
		if (luceneQuery != null) {
			Sort sort = getTestCaseSort(sorting);
			FullTextQuery hibQuery = ftem.createFullTextQuery(luceneQuery, TestCase.class).setSort(sort);

			// FIXME count + paged query if possible
			countAll = hibQuery.getResultList().size();
			if (!sorting.shouldDisplayAll()){
				hibQuery.setFirstResult(sorting.getFirstItemIndex()).setMaxResults(sorting.getPageSize());
			}
			result = hibQuery.getResultList();
		}
		return new PagingBackedPagedCollectionHolder<>(sorting, countAll, result);
	}

	public Query addAggregatedMilestonesCriteria(Query mainQuery, QueryBuilder qb, AdvancedSearchModel modelCopy, Locale locale) {

		// find the milestones
		addMilestoneFilter(modelCopy);

		List<String> strMilestoneIds = ((AdvancedSearchListFieldModel) modelCopy.getFields().get("milestones.id")).getValues();

		// now find the test cases
		Collection<Long> milestoneIds = new ArrayList<>(strMilestoneIds.size());
		for (String str : strMilestoneIds) {
			milestoneIds.add(Long.valueOf(str));
		}


		List<Long> lTestcaseIds = testCaseDao.findAllTestCasesLibraryNodeForMilestone(milestoneIds);
		List<String> testcaseIds = new ArrayList<>(lTestcaseIds.size());
		for (Long l : lTestcaseIds) {
			testcaseIds.add(l.toString());
		}

		//if no tc are found then use fake id so the lucene query will not find anything

		if (testcaseIds.isEmpty()) {
			testcaseIds.add(FAKE_TC_ID);
		}

		// finally, add a criteria that restrict the test case ids
		Query idQuery = buildLuceneValueInListQuery(qb, "id", testcaseIds, false);

		return qb.bool().must(mainQuery).must(idQuery).createQuery();

	}

}
