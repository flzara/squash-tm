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
package org.squashtest.tm.service.internal.campaign;


import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;
import org.squashtest.tm.core.foundation.collection.*;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.search.AdvancedSearchFieldModel;
import org.squashtest.tm.domain.search.AdvancedSearchFieldModelType;
import org.squashtest.tm.domain.search.AdvancedSearchListFieldModel;
import org.squashtest.tm.domain.search.AdvancedSearchModel;
import org.squashtest.tm.service.campaign.CampaignAdvancedSearchService;
import org.squashtest.tm.service.internal.advancedsearch.AdvancedSearchServiceImpl;
import org.squashtest.tm.service.internal.dto.UserDto;
import org.squashtest.tm.service.internal.repository.IterationTestPlanDao;
import org.squashtest.tm.service.internal.repository.ProjectDao;
import org.squashtest.tm.service.project.ProjectFinder;
import org.squashtest.tm.service.project.ProjectManagerService;
import org.squashtest.tm.service.project.ProjectsPermissionManagementService;
import org.squashtest.tm.service.user.UserAccountService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;
import static org.squashtest.tm.jooq.domain.Tables.*;

@Service("squashtest.tm.service.CampaignAdvancedSearchService")
public class CampaignAdvancedSearchServiceImpl extends AdvancedSearchServiceImpl implements
	CampaignAdvancedSearchService {

	@Inject
	protected ProjectFinder projectFinder;

	@PersistenceContext
	private EntityManager entityManager;

	@Inject
	private IterationTestPlanDao iterationTestPlanDao;

	@Inject
	protected UserAccountService userAccountService;

	@Inject
	DSLContext DSL;

	@Inject
	protected ProjectsPermissionManagementService permissionService;

	private static final SortField[] DEFAULT_SORT_EXECUTION = new SortField[]{
		new SortField("project.name", SortField.Type.STRING, false),
		new SortField("campaign-name", SortField.Type.STRING, false),
		new SortField("iteration-name", SortField.Type.STRING, false),
		new SortField("itpi-id", SortField.Type.STRING, false),
		new SortField("itpi-label", SortField.Type.STRING, false),
		new SortField("itpi-mode", SortField.Type.STRING, false),
		new SortField("itpi-status", SortField.Type.STRING, false),
		new SortField("itpi-executed-by", SortField.Type.STRING, false),
		new SortField("itpi-executed-on", SortField.Type.STRING, false),
		new SortField("itpi-datasets", SortField.Type.STRING, false)};

	// FIXME This list which contains sweet FA is used to decide when a field should be processed as a numeric field or a text field. Looks like horseshit to me.
	private static final List<String> LONG_SORTABLE_FIELDS = Collections.singletonList("");

	private static final String TEST_SUITE_ID_FIELD_NAME = "testSuites.id";
	private static final String ITERATION_ID_FIELD_NAME = "iteration.id";
	private static final String CAMPAIGN_ID_FIELD_NAME = "campaign.id";
	private static final String PROJECT_ID_FIELD_NAME = "project.id";

	private static final String FAKE_ITPI_ID = "-9000";

	@Override
	public List<String> findAllAuthorizedUsersForACampaign(List<Long> idList) {
		return findUsersWhoCanAccessProject(idList);

	}

	private List<String> findUsersWhoCanAccessProject(List<Long> projectIds) {
		List<String> list = findPartyPermissionsBeanByProject(projectIds);
		return list;
	}

	protected Query searchIterationTestPlanItemQuery(AdvancedSearchModel model, FullTextEntityManager ftem) {
		QueryBuilder qb = ftem.getSearchFactory().buildQueryBuilder().forEntity(IterationTestPlanItem.class).get();
		/* Creating a copy of the model to keep a model with milestones criteria */
		AdvancedSearchModel modelCopy = model.shallowCopy();
		/* Removing these criteria from the main model */
		removeMilestoneSearchFields(model);

		/* Building main Lucene Query with this main model */
		Query luceneQuery = buildCoreLuceneQuery(qb, model);
		/* If requested, add milestones criteria with the copied model */
		if(shouldSearchByMilestones(modelCopy)) {
			luceneQuery = addAggregatedMilestonesCriteria(luceneQuery, qb, modelCopy);
		}
		return luceneQuery;
	}

	public Query addAggregatedMilestonesCriteria(Query mainQuery, QueryBuilder qb, AdvancedSearchModel modelCopy) {

		addMilestoneFilter(modelCopy);

		/* Find the milestones ids. */
		List<String> strMilestoneIds =
				((AdvancedSearchListFieldModel) modelCopy.getFields().get("milestones.id")).getValues();
		List<Long> milestoneIds = new ArrayList<>(strMilestoneIds.size());
		for (String str : strMilestoneIds) {
			milestoneIds.add(Long.valueOf(str));
		}

		/* Find the ItereationTestPlanItems ids. */
		List<Long> lItpiIds = iterationTestPlanDao.findAllForMilestones(milestoneIds);
		List<String> itpiIds = new ArrayList<>(lItpiIds.size());
		for(Long l : lItpiIds) {
			itpiIds.add(l.toString());
		}

		/* Fake Id to find no result via Lucene if no Itpi found */
		if(itpiIds.isEmpty()) {
			itpiIds.add(FAKE_ITPI_ID);
		}

		/* Add Criteria to restrict Itpi ids */
		Query idQuery = buildLuceneValueInListQuery(qb, "id", itpiIds, false);

		return qb.bool().must(mainQuery).must(idQuery).createQuery();
	}

	@Override
	public PagedCollectionHolder<List<IterationTestPlanItem>> searchForIterationTestPlanItem(AdvancedSearchModel searchModel,
		PagingAndMultiSorting paging, Locale locale) {


		FullTextEntityManager ftSession = Search.getFullTextEntityManager(entityManager);

		Query luceneQuery = searchIterationTestPlanItemQuery(searchModel, ftSession);

		List<IterationTestPlanItem> result = Collections.emptyList();
		int countAll = 0;

		if (!checkSearchModelPerimeterIsEmpty(searchModel) && luceneQuery != null) {
			Sort sort = getExecutionSort(paging);

			FullTextQuery fullTextQuery = ftSession.createFullTextQuery(luceneQuery, IterationTestPlanItem.class).setSort(sort);

			// FIXME The 2 lines below seem to
			// FIXME 1. fetch all the data
			// FIXME 2. fetch the paged data
			// FIXME Looks like horseshit to me
			countAll = fullTextQuery.getResultList().size();
			result = fullTextQuery.setFirstResult(paging.getFirstItemIndex()).setMaxResults(paging.getPageSize()).getResultList();
		}

		// Please, don't return null there, it will explode everything. It did.
		return new PagingBackedPagedCollectionHolder<>(paging, countAll, result);

	}

	private boolean checkSearchModelPerimeterIsEmpty(AdvancedSearchModel searchModel) {
		Map<String, AdvancedSearchFieldModel> fields = searchModel.getFields();
		return checkParamNullOrEmpty(fields.get(PROJECT_ID_FIELD_NAME)) &&
			checkParamNullOrEmpty(fields.get(CAMPAIGN_ID_FIELD_NAME)) &&
			checkParamNullOrEmpty(fields.get(ITERATION_ID_FIELD_NAME)) &&
			checkParamNullOrEmpty(fields.get(TEST_SUITE_ID_FIELD_NAME));
	}

	private boolean checkParamNullOrEmpty(AdvancedSearchFieldModel field) {
		if (field == null) {
			return true;
		}
		if (field.getType() != AdvancedSearchFieldModelType.LIST) {
			return false;
		}
		AdvancedSearchListFieldModel listField = (AdvancedSearchListFieldModel) field;
		return listField.getValues().isEmpty();
	}

	private Sort getExecutionSort(PagingAndMultiSorting multisorting) {


		List<Sorting> sortings = multisorting.getSortings();

		if (sortings == null || sortings.isEmpty()) {
			return new Sort(DEFAULT_SORT_EXECUTION);
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
			} else {
				sortFieldArray[i] = new SortField(fieldName, SortField.Type.STRING, isReverse);
			}
		}

		return new Sort(sortFieldArray);
	}

	private String formatSortFieldName(String fieldName) {
		String result = fieldName;
		if (fieldName.startsWith("IterationTestPlanItem.")) {
			result = fieldName.replaceFirst("IterationTestPlanItem.", "");
		} else if (fieldName.startsWith("Project.")) {
			result = fieldName.replaceFirst("Project.", "project.");
		} else if (fieldName.startsWith("Campaign.")) {
			result = fieldName.replaceFirst("Campaign.", "campaign.");
		}
		return result;
	}

	public List<String> findPartyPermissionsBeanByProject(List<Long> projectIds) {

		List<String> list = new ArrayList<>();
		List<String> result = DSL
			.select(CORE_USER.LOGIN)
			.from(CORE_USER)
			.join(CORE_PARTY).on(CORE_USER.PARTY_ID.eq(CORE_PARTY.PARTY_ID))
			.join(CORE_GROUP_MEMBER).on(CORE_GROUP_MEMBER.PARTY_ID.eq(CORE_USER.PARTY_ID))
			.join(ACL_RESPONSIBILITY_SCOPE_ENTRY).on(ACL_RESPONSIBILITY_SCOPE_ENTRY.PARTY_ID.eq(CORE_GROUP_MEMBER.PARTY_ID))
			.join(ACL_OBJECT_IDENTITY).on(ACL_OBJECT_IDENTITY.ID.eq(ACL_RESPONSIBILITY_SCOPE_ENTRY.OBJECT_IDENTITY_ID))
			.join(ACL_GROUP_PERMISSION).on(ACL_RESPONSIBILITY_SCOPE_ENTRY.ACL_GROUP_ID.eq(ACL_GROUP_PERMISSION.ACL_GROUP_ID))
			.join(ACL_CLASS).on(ACL_GROUP_PERMISSION.CLASS_ID.eq(ACL_CLASS.ID).and(ACL_CLASS.CLASSNAME.eq("org.squashtest.tm.domain.project.Project")))

			.where(ACL_OBJECT_IDENTITY.IDENTITY.in(projectIds))
			.fetch(CORE_USER.LOGIN, String.class);

		for(String r : result){
			if(!list.contains(r)){
				list.add((r));
			}
		}
		return list;
	}

}
