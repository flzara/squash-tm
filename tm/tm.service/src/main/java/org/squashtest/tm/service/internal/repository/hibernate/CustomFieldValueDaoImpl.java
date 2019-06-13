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
package org.squashtest.tm.service.internal.repository.hibernate;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SelectJoinStep;
import org.jooq.SelectSelectStep;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.squashtest.tm.domain.EntityReference;
import org.squashtest.tm.domain.EntityType;
import org.squashtest.tm.service.internal.repository.CustomCustomFieldValueDao;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.jooq.impl.DSL.cast;
import static org.jooq.impl.DSL.groupConcat;
import static org.squashtest.tm.jooq.domain.Tables.CAMPAIGN;
import static org.squashtest.tm.jooq.domain.Tables.CAMPAIGN_ITERATION;
import static org.squashtest.tm.jooq.domain.Tables.CUSTOM_FIELD_VALUE;
import static org.squashtest.tm.jooq.domain.Tables.CUSTOM_FIELD_VALUE_OPTION;
import static org.squashtest.tm.jooq.domain.Tables.EXECUTION;
import static org.squashtest.tm.jooq.domain.Tables.EXECUTION_EXECUTION_STEPS;
import static org.squashtest.tm.jooq.domain.Tables.EXECUTION_STEP;
import static org.squashtest.tm.jooq.domain.Tables.ITEM_TEST_PLAN_EXECUTION;
import static org.squashtest.tm.jooq.domain.Tables.ITEM_TEST_PLAN_LIST;
import static org.squashtest.tm.jooq.domain.Tables.ITERATION;
import static org.squashtest.tm.jooq.domain.Tables.ITERATION_TEST_PLAN_ITEM;
import static org.squashtest.tm.jooq.domain.Tables.TEST_CASE;
import static org.squashtest.tm.jooq.domain.Tables.TEST_SUITE;
import static org.squashtest.tm.jooq.domain.Tables.TEST_SUITE_TEST_PLAN_ITEM;

public class CustomFieldValueDaoImpl implements CustomCustomFieldValueDao {

	@Inject
	private DSLContext Dsl;

	private final static Field CUSTOM_FIELD_VALUE_TAG_VALUE =
		groupConcat(CUSTOM_FIELD_VALUE_OPTION.LABEL).separator(", ");

	private final static Field CUSTOM_FIELD_VALUE_COMPUTED =
		DSL.when(CUSTOM_FIELD_VALUE.FIELD_TYPE.eq("CF"), CUSTOM_FIELD_VALUE.VALUE)
			.when(CUSTOM_FIELD_VALUE.FIELD_TYPE.eq("RTF"), CUSTOM_FIELD_VALUE.LARGE_VALUE)
			.when(CUSTOM_FIELD_VALUE.FIELD_TYPE.eq("NUM"), cast(CUSTOM_FIELD_VALUE.NUMERIC_VALUE, SQLDataType.VARCHAR))
			.when(CUSTOM_FIELD_VALUE.FIELD_TYPE.eq("TAG"), CUSTOM_FIELD_VALUE_TAG_VALUE);

	@Override
	public Map<EntityReference, Map<Long, Object>> getCufValuesMapByEntityReference(long campaignId, Map<EntityType, List<Long>> cufIdMapByEntityType) {

		// Fetch all the Entities involved in the Campaign
		Set<EntityReference> entitiesOfCampaign = getEntityReferencesFromCampaign(campaignId, cufIdMapByEntityType.keySet());

		Map<EntityReference, Map<Long, Object>> resultMap = new HashMap<>();

		SelectSelectStep query =
			Dsl.select(
				CUSTOM_FIELD_VALUE.BOUND_ENTITY_TYPE,
				CUSTOM_FIELD_VALUE.BOUND_ENTITY_ID,
				CUSTOM_FIELD_VALUE.CF_ID,
				CUSTOM_FIELD_VALUE_COMPUTED);

		query.from(CUSTOM_FIELD_VALUE)
			.leftJoin(CUSTOM_FIELD_VALUE_OPTION).on(CUSTOM_FIELD_VALUE.CFV_ID.eq(CUSTOM_FIELD_VALUE_OPTION.CFV_ID));

		Condition whereCondition = null;
		for(EntityReference entityReference: entitiesOfCampaign) {
			Condition currentCondition =
				CUSTOM_FIELD_VALUE.BOUND_ENTITY_TYPE.eq(entityReference.getType().toString())
					.and(CUSTOM_FIELD_VALUE.BOUND_ENTITY_ID.eq(entityReference.getId()))
					.and(CUSTOM_FIELD_VALUE.CF_ID.in(cufIdMapByEntityType.get(entityReference.getType())));
			if(whereCondition == null) {
				whereCondition = currentCondition;
			} else {
				whereCondition = whereCondition.or(currentCondition);
			}
		}

		query.where(whereCondition);
		query.groupBy(CUSTOM_FIELD_VALUE.CFV_ID);

		Iterator<Record> result = query.fetch().iterator();
		result.forEachRemaining(record -> {
			EntityReference entity = new EntityReference(
				EntityType.valueOf(record.get(CUSTOM_FIELD_VALUE.BOUND_ENTITY_TYPE)),
				record.get(CUSTOM_FIELD_VALUE.BOUND_ENTITY_ID));
			Long cufId = record.get(CUSTOM_FIELD_VALUE.CF_ID);
			Object cufValue = record.get(CUSTOM_FIELD_VALUE_COMPUTED);
			Map<Long, Object> currentMap = resultMap.get(entity);
			if(currentMap == null) {
				resultMap.put(entity, new HashMap<>());
				currentMap = resultMap.get(entity);
			}
			currentMap.put(cufId, cufValue);
		});
		return resultMap;
	}

	/**
	 * Extract a Set of all the EntityReferences contained in the given Campaign, limited to the EntityTypes contained
	 * in the Set given as parameter.
	 * @param campaignId The id of the Campaign concerned
	 * @param entityTypes The EntityTypes of the EntityReferences that must be returned
	 * @return A Set of all the EntityReferences contained in the Campaign which EntityType is contained in the given Set.
	 */
	private Set<EntityReference> getEntityReferencesFromCampaign(long campaignId, Set<EntityType> entityTypes) {
		Set<EntityReference> entityReferenceSet = new HashSet<>();

		int cufQueryDepth = getCufQueryDepth(entityTypes);

		List<Field<?>> fieldList = new ArrayList<>();
		if(cufQueryDepth > 1)
			fieldList.add(ITERATION.ITERATION_ID);
		if(cufQueryDepth > 2) {
			fieldList.add(ITERATION_TEST_PLAN_ITEM.ITEM_TEST_PLAN_ID);
			fieldList.add(TEST_CASE.TCLN_ID);
			if (entityTypes.contains(EntityType.TEST_SUITE)) {
				fieldList.add(TEST_SUITE.ID);
			}
		}
		if(cufQueryDepth > 3)
			fieldList.add(EXECUTION.EXECUTION_ID);
		if(cufQueryDepth > 4)
			fieldList.add(EXECUTION_STEP.EXECUTION_STEP_ID);

		SelectSelectStep selectQuery = Dsl.select(fieldList);

		SelectJoinStep fromQuery = selectQuery.from(CAMPAIGN);

		if(cufQueryDepth > 1) {
			fromQuery.leftJoin(CAMPAIGN_ITERATION).on(CAMPAIGN_ITERATION.CAMPAIGN_ID.eq(CAMPAIGN.CLN_ID))
				.leftJoin(ITERATION).on(ITERATION.ITERATION_ID.eq(CAMPAIGN_ITERATION.ITERATION_ID));
		}
		if(cufQueryDepth > 2) {
			fromQuery.leftJoin(ITEM_TEST_PLAN_LIST).on(ITEM_TEST_PLAN_LIST.ITERATION_ID.eq(ITERATION.ITERATION_ID))
				.leftJoin(ITERATION_TEST_PLAN_ITEM).on(ITERATION_TEST_PLAN_ITEM.ITEM_TEST_PLAN_ID.eq(ITEM_TEST_PLAN_LIST.ITEM_TEST_PLAN_ID))
				.leftJoin(TEST_CASE).on(TEST_CASE.TCLN_ID.eq(ITERATION_TEST_PLAN_ITEM.TCLN_ID));
			if(entityTypes.contains(EntityType.TEST_SUITE)) {
				fromQuery.leftJoin(TEST_SUITE_TEST_PLAN_ITEM).on(TEST_SUITE_TEST_PLAN_ITEM.TPI_ID.eq(ITERATION_TEST_PLAN_ITEM.ITEM_TEST_PLAN_ID))
					.leftJoin(TEST_SUITE).on(TEST_SUITE.ID.eq(TEST_SUITE_TEST_PLAN_ITEM.SUITE_ID));
			}
		}
		if(cufQueryDepth > 3) {
			fromQuery.leftJoin(ITEM_TEST_PLAN_EXECUTION).on(ITEM_TEST_PLAN_EXECUTION.ITEM_TEST_PLAN_ID.eq(ITERATION_TEST_PLAN_ITEM.ITEM_TEST_PLAN_ID))
				.leftJoin(EXECUTION).on(EXECUTION.EXECUTION_ID.eq(ITEM_TEST_PLAN_EXECUTION.EXECUTION_ID));
		}
		if(cufQueryDepth > 4) {
			fromQuery.leftJoin(EXECUTION_EXECUTION_STEPS).on(EXECUTION_EXECUTION_STEPS.EXECUTION_ID.eq(EXECUTION.EXECUTION_ID))
				.leftJoin(EXECUTION_STEP).on(EXECUTION_STEP.EXECUTION_STEP_ID.eq(EXECUTION_EXECUTION_STEPS.EXECUTION_STEP_ID));
		}

		fromQuery.where(CAMPAIGN.CLN_ID.eq(campaignId));

		Iterator<Record> queryResult = fromQuery.fetch().iterator();
		if(entityTypes.contains(EntityType.CAMPAIGN)) {
			entityReferenceSet.add(new EntityReference(EntityType.CAMPAIGN, campaignId));
		}
		queryResult.forEachRemaining(record -> {
			if(entityTypes.contains(EntityType.ITERATION)) {
				Long iterationId = record.get(ITERATION.ITERATION_ID);
				if (iterationId != null)
					entityReferenceSet.add(new EntityReference(EntityType.ITERATION, iterationId));
			}
			if(entityTypes.contains(EntityType.TEST_SUITE)) {
				Long testSuiteId = record.get(TEST_SUITE.ID);
				if (testSuiteId != null)
					entityReferenceSet.add(new EntityReference(EntityType.TEST_SUITE, testSuiteId));
			}
			if(entityTypes.contains(EntityType.TEST_CASE)) {
				Long testCaseId = record.get(TEST_CASE.TCLN_ID);
				if (testCaseId != null)
					entityReferenceSet.add(new EntityReference(EntityType.TEST_CASE, testCaseId));
			}
			if(entityTypes.contains(EntityType.EXECUTION)) {
				Long executionId = record.get(EXECUTION.EXECUTION_ID);
				if (executionId != null)
					entityReferenceSet.add(new EntityReference(EntityType.EXECUTION, executionId));
			}
			if(entityTypes.contains(EntityType.EXECUTION_STEP)) {
				Long execStepId = record.get(EXECUTION_STEP.EXECUTION_STEP_ID);
				if (execStepId != null)
					entityReferenceSet.add(new EntityReference(EntityType.EXECUTION_STEP, execStepId));
			}
		});

		return entityReferenceSet;
	}

	/**
	 * Return the depth of the Query.
	 * @param entityTypeList The list of the EntityTypes involved in the Query
	 * @return The depth of the Query
	 */
	private int getCufQueryDepth(Set<EntityType> entityTypeList) {
		if(entityTypeList.contains(EntityType.EXECUTION_STEP))
			return 5;
		if(entityTypeList.contains(EntityType.EXECUTION))
			return 4;
		if(entityTypeList.contains(EntityType.TEST_CASE) || entityTypeList.contains(EntityType.TEST_SUITE))
			return 3;
		if(entityTypeList.contains(EntityType.ITERATION))
			return 2;
		// default
		return 1;
	}
}
