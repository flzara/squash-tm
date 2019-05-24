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

import org.jooq.DSLContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndMultiSorting;
import org.squashtest.tm.core.foundation.collection.PagingBackedPagedCollectionHolder;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.search.AdvancedSearchFieldModel;
import org.squashtest.tm.domain.search.AdvancedSearchFieldModelType;
import org.squashtest.tm.domain.search.AdvancedSearchListFieldModel;
import org.squashtest.tm.domain.search.AdvancedSearchModel;
import org.squashtest.tm.service.campaign.CampaignAdvancedSearchService;
import org.squashtest.tm.service.internal.advancedsearch.AdvancedSearchServiceImpl;
import org.squashtest.tm.service.internal.repository.IterationTestPlanDao;
import org.squashtest.tm.service.project.ProjectFinder;
import org.squashtest.tm.service.project.ProjectsPermissionManagementService;
import org.squashtest.tm.service.user.UserAccountService;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.squashtest.tm.jooq.domain.Tables.ACL_CLASS;
import static org.squashtest.tm.jooq.domain.Tables.ACL_GROUP_PERMISSION;
import static org.squashtest.tm.jooq.domain.Tables.ACL_OBJECT_IDENTITY;
import static org.squashtest.tm.jooq.domain.Tables.ACL_RESPONSIBILITY_SCOPE_ENTRY;
import static org.squashtest.tm.jooq.domain.Tables.CORE_PARTY;
import static org.squashtest.tm.jooq.domain.Tables.CORE_TEAM_MEMBER;
import static org.squashtest.tm.jooq.domain.Tables.CORE_USER;

@Service("squashtest.tm.service.CampaignAdvancedSearchService")
public class CampaignAdvancedSearchServiceImpl extends AdvancedSearchServiceImpl implements
	CampaignAdvancedSearchService {

	private static final Map<String, String> COLUMN_PROTOTYPE_MAPPING = new HashMap() {{
		put("executionMode", "");
		put("executionStatus", "EXECUTION_STATUS");
		put("lastExecutedBy", "EXECUTION_TESTER_LOGIN");
		put("lastExecutedOn", "EXECUTION_LASTEXEC");
		put("milestone.endDate", "CAMPAIGN_MILESTONE_END_DATE");
		//TODO to create
		put("milestone.label", "");
		put("milestone.status", "CAMPAIGN_MILESTONE_STATUS");
		put("project.id", "CAMPAIGN_PROJECT");
		put("referencedTestCase.automatable", "TEST_CASE_AUTOMATABLE");
		put("referencedTestCase.automationRequest.requestStatus", "AUTOMATION_REQUEST_STATUS");
		put("referencedTestCase.id", "TEST_CASE_ID");
		put("referencedTestCase.importance", "TEST_CASE_IMPORTANCE");
		put("referencedTestCase.name", "TEST_CASE_NAME");
		put("referencedTestCase.reference", "TEST_CASE_REFERENCE");
		put("user", "ITERATION_TEST_PLAN_ASSIGNED_USER");
		//TODO to create
		put("project-name", "");
		//TODO to create
		put("campaign-name", "");
		//TODO to create
		put("iteration-name", "");
		put("itpi-id", "ITEM_TEST_PLAN_ID");
		put("itpi-label", "ITEM_TEST_PLAN_LABEL");
		//TODO to create
		put("itpi-mode", "");
		//TODO to create
		put("itpi-testsuites", "");
		put("itpi-status", "ITEM_TEST_PLAN_STATUS");
		//TODO to create
		put("itpi-executed-by", "ITEM_TEST_PLAN_TESTER");
		put("itpi-executed-on", "ITEM_TEST_PLAN_LASTEXECON");
		//TODO to create
		put("itpi-datasets", "");
		put("tc-weight", "TEST_CASE_IMPORTANCE");
		put("test-case-automatable", "TEST_CASE_AUTOMATABLE");
	}};

	private static final List<String> PROJECTIONS = Arrays.asList("entity-index", "empty-openinterface2-holder",
		"empty-opentree-holder");

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

	private static final String LAST_EXECUTE_ON_FIELD_NAME ="lastExecutedOn";
	private static final List<String> LONG_SORTABLE_FIELDS = Collections.singletonList(LAST_EXECUTE_ON_FIELD_NAME);

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
		List<Long> partyIds = findPartyIdsCanAccessProject(projectIds);
		return  findUserLoginsByPartyIds(partyIds);
	}

	/*protected Query searchIterationTestPlanItemQuery(AdvancedSearchModel model, FullTextEntityManager ftem) {
		QueryBuilder qb = ftem.getSearchFactory().buildQueryBuilder().forEntity(IterationTestPlanItem.class).get();
		*//* Creating a copy of the model to keep a model with milestones criteria *//*
		AdvancedSearchModel modelCopy = model.shallowCopy();
		*//* Removing these criteria from the main model *//*
		removeMilestoneSearchFields(model);


		*//* Building main Lucene Query with this main model *//*
		Query luceneQuery = buildCoreLuceneQuery(qb, model);
		*//* If requested, add milestones criteria with the copied model *//*
		if (shouldSearchByMilestones(modelCopy)) {
			luceneQuery = addAggregatedMilestonesCriteria(luceneQuery, qb, modelCopy);
		}

		if(shouldSearchByAutomationWorkflow(modelCopy)) {
			luceneQuery = addAllowAutomationWorkflow(luceneQuery,qb, modelCopy);
		}
		return luceneQuery;
	}

	public Query addAggregatedMilestonesCriteria(Query mainQuery, QueryBuilder qb, AdvancedSearchModel modelCopy) {

		*//* Find the milestones ids. *//*
		List<Long> milestoneIds = findMilestonesIds(modelCopy);

		*//* Find the ItereationTestPlanItems ids. *//*
		List<Long> lItpiIds = iterationTestPlanDao.findAllForMilestones(milestoneIds);

		*//* Create the query. *//*
		return fakeIdToFindNoResultViaLuceneForCreatingQuery(lItpiIds,  qb,  mainQuery,  FAKE_ITPI_ID);
	}

	public Query addAllowAutomationWorkflow(Query mainQuery, QueryBuilder qb, AdvancedSearchModel modelCopy) {
		addWorkflowAutomationFilter(modelCopy);
		Query query = buildLuceneQuery(qb,modelCopy);
		return qb.bool().must(mainQuery).must(query).createQuery();
	}*/

	@Override
	public Page<IterationTestPlanItem> searchForIterationTestPlanItem(AdvancedSearchModel searchModel,
																	  Pageable paging, Locale locale) {



		List<IterationTestPlanItem> result = Collections.emptyList();
		int countAll = 0;
		// Please, don't return null there, it will explode everything. It did.
		return new PageImpl(result, paging, countAll);

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

	private List<Long> findPartyIdsCanAccessProject(List<Long> projectIds) {

			return DSL
			.select(CORE_PARTY.PARTY_ID)
			.from(CORE_PARTY)
			.join(ACL_RESPONSIBILITY_SCOPE_ENTRY).on(ACL_RESPONSIBILITY_SCOPE_ENTRY.PARTY_ID.eq(CORE_PARTY.PARTY_ID))
			.join(ACL_OBJECT_IDENTITY).on(ACL_OBJECT_IDENTITY.ID.eq(ACL_RESPONSIBILITY_SCOPE_ENTRY.OBJECT_IDENTITY_ID))
			.join(ACL_GROUP_PERMISSION).on(ACL_RESPONSIBILITY_SCOPE_ENTRY.ACL_GROUP_ID.eq(ACL_GROUP_PERMISSION.ACL_GROUP_ID))
			.join(ACL_CLASS).on(ACL_GROUP_PERMISSION.CLASS_ID.eq(ACL_CLASS.ID).and(ACL_CLASS.CLASSNAME.eq("org.squashtest.tm.domain.project.Project")))

			.where(ACL_OBJECT_IDENTITY.IDENTITY.in(projectIds))
			.groupBy(CORE_PARTY.PARTY_ID)
			.fetch(CORE_PARTY.PARTY_ID, Long.class);
	}

	private List<String> findUserLoginsByPartyIds(List<Long> partyIds) {
		List<String> usersSolo = DSL
			.select(CORE_USER.LOGIN)
			.from(CORE_USER)
			.join(CORE_PARTY).on(CORE_PARTY.PARTY_ID.eq(CORE_USER.PARTY_ID))
			.where(CORE_PARTY.PARTY_ID.in(partyIds))
			.groupBy(CORE_USER.PARTY_ID)
			.fetch(CORE_USER.LOGIN, String.class);

		List<String> usersInTeam = DSL
			.select(CORE_USER.LOGIN)
			.from(CORE_USER)
			.join(CORE_TEAM_MEMBER).on(CORE_TEAM_MEMBER.USER_ID.eq(CORE_USER.PARTY_ID))
			.join(CORE_PARTY).on(CORE_TEAM_MEMBER.TEAM_ID.eq(CORE_PARTY.PARTY_ID))
			.where(CORE_PARTY.PARTY_ID.in(partyIds))
			.groupBy(CORE_USER.PARTY_ID)
			.fetch(CORE_USER.LOGIN, String.class);

		return Stream.concat(usersSolo.stream(), usersInTeam.stream()).distinct()
			.collect(Collectors.toList());
	}

}
