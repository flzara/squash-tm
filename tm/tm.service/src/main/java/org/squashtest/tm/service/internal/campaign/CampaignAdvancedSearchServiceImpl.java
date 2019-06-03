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

import com.querydsl.core.Tuple;
import org.jooq.DSLContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.search.AdvancedSearchFieldModel;
import org.squashtest.tm.domain.search.AdvancedSearchFieldModelType;
import org.squashtest.tm.domain.search.AdvancedSearchListFieldModel;
import org.squashtest.tm.domain.search.AdvancedSearchModel;
import org.squashtest.tm.domain.search.AdvancedSearchQueryModel;
import org.squashtest.tm.domain.search.QueryCufLabel;
import org.squashtest.tm.service.campaign.CampaignAdvancedSearchService;
import org.squashtest.tm.service.internal.advancedsearch.AdvancedSearchColumnMappings;
import org.squashtest.tm.service.internal.advancedsearch.AdvancedSearchQueryModelToConfiguredQueryConverter;
import org.squashtest.tm.service.internal.advancedsearch.AdvancedSearchServiceImpl;
import org.squashtest.tm.service.internal.query.QueryProcessingServiceImpl;
import org.squashtest.tm.service.internal.repository.IterationTestPlanDao;
import org.squashtest.tm.service.project.ProjectFinder;
import org.squashtest.tm.service.project.ProjectsPermissionManagementService;
import org.squashtest.tm.service.query.ConfiguredQuery;
import org.squashtest.tm.service.user.UserAccountService;

import javax.inject.Inject;
import javax.inject.Provider;
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

import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.AUTOMATION_REQUEST_STATUS;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.CAMPAIGN_MILESTONE_END_DATE;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.CAMPAIGN_MILESTONE_LABEL;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.CAMPAIGN_MILESTONE_STATUS;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.CAMPAIGN_NAME;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.CAMPAIGN_PROJECT_ID;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.CAMPAIGN_PROJECT_NAME;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.EXECUTION_EXECUTION_MODE;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.EXECUTION_ISAUTO;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.ITEM_TEST_PLAN_DSCOUNT;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.ITEM_TEST_PLAN_ID;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.ITEM_TEST_PLAN_LABEL;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.ITEM_TEST_PLAN_LASTEXECON;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.ITEM_TEST_PLAN_STATUS;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.ITEM_TEST_PLAN_SUITECOUNT;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.ITEM_TEST_PLAN_TC_DELETED;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.ITEM_TEST_PLAN_TESTER;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.ITERATION_CUF_CHECKBOX;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.ITERATION_CUF_DATE;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.ITERATION_CUF_LIST;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.ITERATION_CUF_NUMERIC;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.ITERATION_CUF_TAG;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.ITERATION_CUF_TEXT;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.ITERATION_NAME;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.ITERATION_TEST_PLAN_ASSIGNED_USER_LOGIN;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.TEST_CASE_AUTOMATABLE;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.TEST_CASE_ID;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.TEST_CASE_IMPORTANCE;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.TEST_CASE_NAME;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.TEST_CASE_REFERENCE;
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

	private static final AdvancedSearchColumnMappings MAPPINGS = new AdvancedSearchColumnMappings();

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

	@Inject
	private Provider<AdvancedSearchQueryModelToConfiguredQueryConverter> converterProvider;

	@Inject
	private QueryProcessingServiceImpl dataFinder;


	/*private static final String LAST_EXECUTE_ON_FIELD_NAME = "lastExecutedOn";
	private static final List<String> LONG_SORTABLE_FIELDS = Collections.singletonList(LAST_EXECUTE_ON_FIELD_NAME);

	private static final String TEST_SUITE_ID_FIELD_NAME = "testSuites.id";
	private static final String ITERATION_ID_FIELD_NAME = "iteration.id";
	private static final String CAMPAIGN_ID_FIELD_NAME = "campaign.id";
	private static final String PROJECT_ID_FIELD_NAME = "project.id";

	private static final String FAKE_ITPI_ID = "-9000";*/

	@Override
	public List<String> findAllAuthorizedUsersForACampaign(List<Long> idList) {
		return findUsersWhoCanAccessProject(idList);

	}

	private List<String> findUsersWhoCanAccessProject(List<Long> projectIds) {
		List<Long> partyIds = findPartyIdsCanAccessProject(projectIds);
		return findUserLoginsByPartyIds(partyIds);
	}

	@Override
	public Page<IterationTestPlanItem> searchForIterationTestPlanItem(AdvancedSearchQueryModel searchModel,
																	  Pageable paging, Locale locale) {

		AdvancedSearchQueryModelToConfiguredQueryConverter converter = converterProvider.get();

		ConfiguredQuery configuredQuery = converter.configureModel(searchModel).configureMapping(MAPPINGS).convert();

		List<Tuple> tuples = dataFinder.executeQuery(configuredQuery);
		int tupleSize = tuples.size();
		List<IterationTestPlanItem> result = Collections.emptyList();

		return new PageImpl(result, paging, tupleSize);

	}

	/*private boolean checkSearchModelPerimeterIsEmpty(AdvancedSearchModel searchModel) {
		Map<String, AdvancedSearchFieldModel> fields = searchModel.getFields();
		return checkParamNullOrEmpty(fields.get(PROJECT_ID_FIELD_NAME)) &&
			checkParamNullOrEmpty(fields.get(CAMPAIGN_ID_FIELD_NAME)) &&
			checkParamNullOrEmpty(fields.get(ITERATION_ID_FIELD_NAME)) &&
			checkParamNullOrEmpty(fields.get(TEST_SUITE_ID_FIELD_NAME));
	}*/

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


	/*private String formatSortFieldName(String fieldName) {
		String result = fieldName;
		if (fieldName.startsWith("IterationTestPlanItem.")) {
			result = fieldName.replaceFirst("IterationTestPlanItem.", "");
		} else if (fieldName.startsWith("Project.")) {
			result = fieldName.replaceFirst("Project.", "project.");
		} else if (fieldName.startsWith("Campaign.")) {
			result = fieldName.replaceFirst("Campaign.", "campaign.");
		}
		return result;
	}*/

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

	static {

		MAPPINGS.getResultMapping()
			.map("project-name", CAMPAIGN_PROJECT_NAME)
			.map("project-id", CAMPAIGN_PROJECT_ID)
			.map("campaign-name", CAMPAIGN_NAME)
			.map("iteration-name", ITERATION_NAME)
			.map("itpi-id", ITEM_TEST_PLAN_ID)
			.map("itpi-label", ITEM_TEST_PLAN_LABEL)
			.map("itpi-mode", EXECUTION_EXECUTION_MODE)
			.map("itpi-isauto", EXECUTION_ISAUTO)
			.map("itpi-testsuites", ITEM_TEST_PLAN_SUITECOUNT)
			.map("itpi-status", ITEM_TEST_PLAN_STATUS)
			.map("is-tc-deleted", ITEM_TEST_PLAN_TC_DELETED)
			.map("itpi-executed-by", ITEM_TEST_PLAN_TESTER)
			.map("itpi-executed-on", ITEM_TEST_PLAN_LASTEXECON)
			.map("itpi-datasets", ITEM_TEST_PLAN_DSCOUNT)
			.map("tc-weight", TEST_CASE_IMPORTANCE)
			.map("test-case-automatable", TEST_CASE_AUTOMATABLE);

		MAPPINGS.getFormMapping()
			.map("executionMode", EXECUTION_EXECUTION_MODE)
			.map("executionStatus", ITEM_TEST_PLAN_STATUS)
			.map("lastExecutedBy", ITEM_TEST_PLAN_TESTER)
			.map("lastExecutedOn", ITEM_TEST_PLAN_LASTEXECON)
			.map("milestone.endDate", CAMPAIGN_MILESTONE_END_DATE)
			.map("milestone.label", CAMPAIGN_MILESTONE_LABEL)
			.map("milestone.status", CAMPAIGN_MILESTONE_STATUS)
			.map("project.id", CAMPAIGN_PROJECT_ID)
			.map("referencedTestCase.automatable", TEST_CASE_AUTOMATABLE)
			.map("referencedTestCase.automationRequest.requestStatus", AUTOMATION_REQUEST_STATUS)
			.map("referencedTestCase.id", TEST_CASE_ID)
			.map("referencedTestCase.importance", TEST_CASE_IMPORTANCE)
			.map("referencedTestCase.name", TEST_CASE_NAME)
			.map("referencedTestCase.reference", TEST_CASE_REFERENCE)
			.map("user", ITERATION_TEST_PLAN_ASSIGNED_USER_LOGIN);

		MAPPINGS.getCufMapping()
			.map(QueryCufLabel.TAGS.toString(), ITERATION_CUF_TAG)
			.map(QueryCufLabel.CF_LIST.toString(), ITERATION_CUF_LIST)
			.map(QueryCufLabel.CF_SINGLE.toString(), ITERATION_CUF_TEXT)
			.map(QueryCufLabel.CF_TIME_INTERVAL.toString(), ITERATION_CUF_DATE)
			.map(QueryCufLabel.CF_NUMERIC.toString(), ITERATION_CUF_NUMERIC)
			.map(QueryCufLabel.CF_CHECKBOX.toString(), ITERATION_CUF_CHECKBOX);
	}

}
