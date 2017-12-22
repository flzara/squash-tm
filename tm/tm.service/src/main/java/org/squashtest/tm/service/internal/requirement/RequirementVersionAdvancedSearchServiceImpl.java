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
package org.squashtest.tm.service.internal.requirement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndMultiSorting;
import org.squashtest.tm.core.foundation.collection.PagingBackedPagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.SortOrder;
import org.squashtest.tm.core.foundation.collection.Sorting;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.search.AdvancedSearchListFieldModel;
import org.squashtest.tm.domain.search.AdvancedSearchModel;
import org.squashtest.tm.service.internal.advancedsearch.AdvancedSearchServiceImpl;
import org.squashtest.tm.service.internal.dto.UserDto;
import org.squashtest.tm.service.internal.infolist.InfoListItemComparatorSource;
import org.squashtest.tm.service.internal.repository.ProjectDao;
import org.squashtest.tm.service.internal.repository.RequirementVersionDao;
import org.squashtest.tm.service.requirement.RequirementVersionAdvancedSearchService;

@Service("squashtest.tm.service.RequirementVersionAdvancedSearchService")
public class RequirementVersionAdvancedSearchServiceImpl extends AdvancedSearchServiceImpl implements
	RequirementVersionAdvancedSearchService {

	@PersistenceContext
	private EntityManager entityManager;

	@Inject
	private ProjectDao projectDao;

	@Inject
	private RequirementVersionDao requirementVersionDao;



	private static final SortField[] DEFAULT_SORT_REQUIREMENTS = new SortField[]{
		new SortField("requirement.project.name", SortField.Type.STRING, false),
		new SortField("reference", SortField.Type.STRING, false), new SortField("criticality", SortField.Type.STRING, false),
		new SortField("category", SortField.Type.STRING, false), new SortField("status", SortField.Type.STRING, false),
		new SortField("labelUpperCased", SortField.Type.STRING, false)};

	private static final List<String> LONG_SORTABLE_FIELDS = Arrays.asList("requirement.id", "id",
		"requirement.versions", "testcases", "attachments");
	private static final List<String> INT_SORTABLE_FIELDS = Arrays.asList("versionNumber");

	private static final String FAKE_REQUIREMENT_VERSION_ID = "-9000";

	@Override
	public List<String> findAllUsersWhoCreatedRequirementVersions(List<Long> idList) {
		return projectDao.findUsersWhoCreatedRequirementVersions(idList);
	}

	@Override
	public List<String> findAllUsersWhoModifiedRequirementVersions(List<Long> idList) {
		return projectDao.findUsersWhoModifiedRequirementVersions(idList);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<RequirementVersion> searchForRequirementVersions(AdvancedSearchModel model, Locale locale) {


		FullTextEntityManager ftSession = Search.getFullTextEntityManager(entityManager);

		QueryBuilder qb = ftSession.getSearchFactory().buildQueryBuilder().forEntity(RequirementVersion.class).get();

		Query luceneQuery = buildLuceneQuery(qb, model);

		FullTextQuery hibQuery = ftSession.createFullTextQuery(luceneQuery, RequirementVersion.class);

		return hibQuery.getResultList();
	}

	private Sort getRequirementVersionSort(List<Sorting> sortings, MessageSource source, Locale locale) {

		if (sortings == null || sortings.isEmpty()) {
			return new Sort(DEFAULT_SORT_REQUIREMENTS);
		}

		boolean isReverse = true;
		SortField[] sortFieldArray = new SortField[sortings.size()];

		for (int i = 0; i < sortings.size(); i++) {

			if (SortOrder.ASCENDING == sortings.get(i).getSortOrder()) {
				isReverse = false;
			}

			String fieldName = sortings.get(i).getSortedAttribute();

			fieldName = formatSortedFieldName(fieldName);

			if (LONG_SORTABLE_FIELDS.contains(fieldName)) {
				sortFieldArray[i] = new SortField(fieldName, SortField.Type.LONG, isReverse);
			} else if (INT_SORTABLE_FIELDS.contains(fieldName)) {
				sortFieldArray[i] = new SortField(fieldName, SortField.Type.INT, isReverse);
			} else if ("category".equals(fieldName)) {
				sortFieldArray[i] = new SortField(fieldName, new InfoListItemComparatorSource(source, locale),
					isReverse);
			} else {
				sortFieldArray[i] = new SortField(fieldName, SortField.Type.STRING, isReverse);
			}
		}

		return new Sort(sortFieldArray);

	}

	private String formatSortedFieldName(String fieldName) {
		String result = fieldName;
		if (fieldName.startsWith("RequirementVersion.")) {
			result = fieldName.replaceFirst("RequirementVersion.", "");
		} else if (fieldName.startsWith("Requirement.")) {
			result = fieldName.replaceFirst("Requirement.", "requirement.");
		} else if (fieldName.startsWith("Project.")) {
			result = fieldName.replaceFirst("Project.", "requirement.project.");
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public PagedCollectionHolder<List<RequirementVersion>> searchForRequirementVersions(AdvancedSearchModel model,
		PagingAndMultiSorting sorting, MessageSource source, Locale locale) {

		FullTextEntityManager ftSession = Search.getFullTextEntityManager(entityManager);


		Query luceneQuery = searchForRequirementVersionQuery(model, ftSession, locale);

		List<RequirementVersion> result = Collections.emptyList();
		int countAll = 0;
		if (luceneQuery != null) {
			Sort sort = getRequirementVersionSort(sorting.getSortings(), source, locale);
			FullTextQuery hibQuery = ftSession.createFullTextQuery(luceneQuery, RequirementVersion.class)
				.setSort(sort);

			// FIXME ain't there a way to query for count instead of querying twice the whole resultset ?
			countAll = hibQuery.getResultList().size();
			result = hibQuery.setFirstResult(sorting.getFirstItemIndex()).setMaxResults(sorting.getPageSize()).getResultList();
		}
		return new PagingBackedPagedCollectionHolder<>(sorting, countAll, result);
	}

	protected Query searchForRequirementVersionQuery(AdvancedSearchModel model, FullTextEntityManager ftem, Locale locale) {
		QueryBuilder qb = ftem.getSearchFactory().buildQueryBuilder().forEntity(RequirementVersion.class).get();
		/* Creating a copy of the model to keep a model with milestones criteria */
		AdvancedSearchModel modelCopy = model.shallowCopy();
		/* Removing these criteria from the main model */
		removeMilestoneSearchFields(model);

		/* Building main Lucene Query with this main model */
		Query luceneQuery = buildCoreLuceneQuery(qb, model);
		/* If requested, add milestones criteria with the copied model */
		if(shouldSearchByMilestones(modelCopy)) {
			luceneQuery = addAggregatedMilestonesCriteria(luceneQuery, qb, modelCopy, locale);
		}
		return luceneQuery;
	}

	public Query addAggregatedMilestonesCriteria(Query mainQuery, QueryBuilder qb, AdvancedSearchModel modelCopy, Locale locale) {

		addMilestoneFilter(modelCopy);

		/* Find the milestones ids. */
		List<String> strMilestoneIds =
				((AdvancedSearchListFieldModel) modelCopy.getFields().get("milestones.id")).getValues();
		List<Long> milestoneIds = new ArrayList<>(strMilestoneIds.size());
		for (String str : strMilestoneIds) {
			milestoneIds.add(Long.valueOf(str));
		}

		/* Find the RequirementVersions ids. */
		List<Long> lReqVerIds = requirementVersionDao.findAllForMilestones(milestoneIds);
		List<String> itpiIds = new ArrayList<>(lReqVerIds.size());
		for(Long l : lReqVerIds) {
			itpiIds.add(l.toString());
		}

		/* Fake Id to find no result via Lucene if no Requirement Version found */
		if(itpiIds.isEmpty()) {
			itpiIds.add(FAKE_REQUIREMENT_VERSION_ID);
		}

		/* Add Criteria to restrict Requirement Versions ids */
		Query idQuery = buildLuceneValueInListQuery(qb, "id", itpiIds, false);

		return qb.bool().must(mainQuery).must(idQuery).createQuery();
	}



}
