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
import org.squashtest.tm.domain.customreport.CustomExportColumnLabel;
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
			.when(CUSTOM_FIELD_VALUE.FIELD_TYPE.eq("TAG"), CUSTOM_FIELD_VALUE_TAG_VALUE)
			.as("COMPUTED_VALUE");

	@Override
	public Map<EntityReference, Map<Long, Object>> getCufValuesMapByEntityReference(EntityReference entity, Map<EntityType, List<Long>> entityTypeToCufIdsListMap) {
		// Fetch all the Entities involved in the Campaign which cuf were requested
		Set<EntityReference> allRequestedEntitiesInCampaign = getEntityReferencesFromCampaign(entity, entityTypeToCufIdsListMap.keySet());
		if(allRequestedEntitiesInCampaign.isEmpty()) {
			return null;
		}
		// Build cuf Query
		SelectSelectStep query = buildCufQuery(entityTypeToCufIdsListMap, allRequestedEntitiesInCampaign);
		// Execute the cuf Query
		Iterator<Record> queryResult = query.fetch().iterator();
		// Build the resulting map
		return buildResultMapFromQueryResult(queryResult);
	}

	/**
	 * Exploit the Cuf Query Results to build the Map of all CustomFieldValues by EntityReference.
	 * @param queryResult The Results of the Cuf Query
	 * @return The Map of all CustomFieldValues by EntityReferences
	 */
	private Map<EntityReference, Map<Long, Object>> buildResultMapFromQueryResult(Iterator<Record> queryResult) {
		Map<EntityReference, Map<Long, Object>> resultMap = new HashMap<>();
		queryResult.forEachRemaining(record -> {
			// Create the EntityReference
			EntityReference entity = new EntityReference(
				EntityType.valueOf(record.get(CUSTOM_FIELD_VALUE.BOUND_ENTITY_TYPE)),
				record.get(CUSTOM_FIELD_VALUE.BOUND_ENTITY_ID));
			// Get the Cuf id
			Long cufId = record.get(CUSTOM_FIELD_VALUE.CF_ID);
			// Get the value
			Object cufValue = record.get(CUSTOM_FIELD_VALUE_COMPUTED);
			// Store these data in the result Map
			Map<Long, Object> currentEntityMap = resultMap.get(entity);
			if(currentEntityMap == null) {
				resultMap.put(entity, new HashMap<>());
				currentEntityMap = resultMap.get(entity);
			}
			currentEntityMap.put(cufId, cufValue);
		});
		return resultMap;
	}

	/**
	 * Build the complete Query to retrieve all requested CustomFieldValues of all requested Entities.
	 * @param entityTypeToCufIdsListMap The Map giving the List of CustomField ids to retrieve by EntityType
	 * @param allRequestedEntitiesInCampaign The Set of all the EntityReferences which CustomFieldValues are to retrieve
	 * @return The Query to retrieve all requested CustomFieldValues of all requested Entities
	 */
	private SelectSelectStep buildCufQuery(Map<EntityType, List<Long>> entityTypeToCufIdsListMap, Set<EntityReference> allRequestedEntitiesInCampaign) {
		SelectSelectStep query =
			Dsl.select(
				CUSTOM_FIELD_VALUE.BOUND_ENTITY_TYPE,
				CUSTOM_FIELD_VALUE.BOUND_ENTITY_ID,
				CUSTOM_FIELD_VALUE.CF_ID,
				CUSTOM_FIELD_VALUE_COMPUTED);

		query.from(CUSTOM_FIELD_VALUE)
			.leftJoin(CUSTOM_FIELD_VALUE_OPTION).on(CUSTOM_FIELD_VALUE.CFV_ID.eq(CUSTOM_FIELD_VALUE_OPTION.CFV_ID));

		query.where(
			buildWhereConditionOfCufQuery(entityTypeToCufIdsListMap, allRequestedEntitiesInCampaign));

		query.groupBy(CUSTOM_FIELD_VALUE.CFV_ID);

		return query;
	}

	/**
	 * Build the Where condition of the CustomFieldValues Query.
	 * @param entityTypeToCufIdsListMap The Map of Cuf ids List by EntityType
	 * @param allRequestedEntitiesInCampaign All the EntityReferences which CustomFieldValues are requested
	 * @return The Where condition of the CustomFieldValues Query
	 */
	private Condition buildWhereConditionOfCufQuery(Map<EntityType, List<Long>> entityTypeToCufIdsListMap, Set<EntityReference> allRequestedEntitiesInCampaign) {
		Condition whereCondition = null;
		for(EntityReference entityReference: allRequestedEntitiesInCampaign) {
			Condition currentCondition =
				CUSTOM_FIELD_VALUE.BOUND_ENTITY_TYPE.eq(entityReference.getType().toString())
					.and(CUSTOM_FIELD_VALUE.BOUND_ENTITY_ID.eq(entityReference.getId()))
					.and(CUSTOM_FIELD_VALUE.CF_ID.in(entityTypeToCufIdsListMap.get(entityReference.getType())));
			if(whereCondition == null) {
				whereCondition = currentCondition;
			} else {
				whereCondition = whereCondition.or(currentCondition);
			}
		}
		return whereCondition;
	}

	/**
	 * Extract a Set of all the EntityReferences contained in the given Campaign, limited to the EntityTypes contained
	 * in the Set given as parameter.
	 * @param entity The id of the Campaign concerned
	 * @param entityTypes The EntityTypes of the EntityReferences that must be returned
	 * @return A Set of all the EntityReferences contained in the Campaign which EntityType is contained in the given Set.
	 */
	private Set<EntityReference> getEntityReferencesFromCampaign(EntityReference entity, Set<EntityType> entityTypes) {
		SelectJoinStep query = buildEntityReferencesQuery(entity, entityTypes);
		Iterator<Record> queryResult = query.fetch().iterator();
		return buildEntityReferencesSetFromQueryResult(entity, entityTypes, queryResult);
	}

	/**
	 * Build the complete Query to retrieve all the requested EntityReferences from a Campaign.
	 * @param entity The Campaign Id
	 * @param entityTypes The EntityType of the requested EntityReferences
	 * @return The Query to retrieve all the requested EntityReferences from the Campaign
	 */
	private SelectJoinStep buildEntityReferencesQuery(EntityReference entity, Set<EntityType> entityTypes) {
		int cufQueryDepth = getDepthOfEntitiesQuery(entityTypes);
		boolean isTestSuiteRequested = entityTypes.contains(EntityType.TEST_SUITE);
		boolean isIterationSelected = EntityType.ITERATION.equals(entity.getType());
		boolean isTestSuiteSelected = EntityType.TEST_SUITE.equals(entity.getType());

		List<Field<?>> fieldList = buildFieldsListOfEntitiesQuery(cufQueryDepth, isTestSuiteRequested, isIterationSelected, isTestSuiteSelected);

		SelectSelectStep selectQuery = Dsl.select(fieldList);
		SelectJoinStep fromQuery = buildFromClauseOfEntitiesQuery(selectQuery, cufQueryDepth, isTestSuiteRequested);
		switch(entity.getType()) {
			case CAMPAIGN: fromQuery.where(CAMPAIGN.CLN_ID.eq(entity.getId())); break;
			case ITERATION: fromQuery.where(ITERATION.ITERATION_ID.eq(entity.getId())); break;
			default: fromQuery.where(TEST_SUITE.ID.eq(entity.getId()));
		}

		return fromQuery;
	}

	/**
	 * Exploit the EntityReferences Query Results to build a Set of all the EntityReferences in the given Campaign.
	 * @param entity The Campaign id
	 * @param entityTypes The EntityTypes requested in the result
	 * @param queryResult The Results of the Query
	 * @return The Set of all EntityReferences in the Campaign based on the Query Results
	 */
	private Set<EntityReference> buildEntityReferencesSetFromQueryResult(EntityReference entity, Set<EntityType> entityTypes, Iterator<Record> queryResult) {
		Set<EntityReference> entityReferenceSet = new HashSet<>();

		Set<EntityType> entityTypesWithoutCampaign = new HashSet(entityTypes);

		// The campaign is the the same all the way and we already know its id
		if (entityTypes.contains(EntityType.CAMPAIGN)) {
			entityReferenceSet.add(new EntityReference(EntityType.CAMPAIGN, entity.getId()));
			// We can remove the campaign from the requested entities
			entityTypesWithoutCampaign.remove(EntityType.CAMPAIGN);
		}/* else if (entityTypes.contains(EntityType.ITERATION)) {
			entityReferenceSet.add(new EntityReference(EntityType.ITERATION, entity.getId()));
			// We can remove the campaign from the requested entities
			entityTypesWithoutCampaign.remove(EntityType.ITERATION);
		} else if (entityTypes.contains(EntityType.TEST_SUITE)) {
			entityReferenceSet.add(new EntityReference(EntityType.TEST_SUITE, entity.getId()));
			// We can remove the campaign from the requested entities
			entityTypesWithoutCampaign.remove(EntityType.TEST_SUITE);
		}*/


		queryResult.forEachRemaining(record -> {
			for(EntityType entityType : entityTypesWithoutCampaign) {
				Long entityId = record.get(CustomExportColumnLabel.getEntityTypeToIdTableFieldMap().get(entityType));
				// The id could be null if left joined with a non existent entity
				if(entityId != null) {
					entityReferenceSet.add(new EntityReference(entityType, entityId));
				}
			}
		});
		return entityReferenceSet;
	}

	/**
	 * Build the From clause of the Query
	 * @param selectQuery The previously built Select clause of the Query
	 * @param cufQueryDepth The depth of the Query
	 * @param isTestSuiteRequested If at least one TestSuite CustomField is requested
	 * @return The From clause of the Query
	 */
	private SelectJoinStep buildFromClauseOfEntitiesQuery(SelectSelectStep selectQuery, int cufQueryDepth, boolean isTestSuiteRequested) {
		SelectJoinStep fromQuery = selectQuery.from(CAMPAIGN);

		if(cufQueryDepth > 1) {
			fromQuery.leftJoin(CAMPAIGN_ITERATION).on(CAMPAIGN_ITERATION.CAMPAIGN_ID.eq(CAMPAIGN.CLN_ID))
				.leftJoin(ITERATION).on(ITERATION.ITERATION_ID.eq(CAMPAIGN_ITERATION.ITERATION_ID));
		}
		if(cufQueryDepth > 2) {
			fromQuery.leftJoin(ITEM_TEST_PLAN_LIST).on(ITEM_TEST_PLAN_LIST.ITERATION_ID.eq(ITERATION.ITERATION_ID))
				.leftJoin(ITERATION_TEST_PLAN_ITEM).on(ITERATION_TEST_PLAN_ITEM.ITEM_TEST_PLAN_ID.eq(ITEM_TEST_PLAN_LIST.ITEM_TEST_PLAN_ID))
				.leftJoin(TEST_CASE).on(TEST_CASE.TCLN_ID.eq(ITERATION_TEST_PLAN_ITEM.TCLN_ID));
			if(isTestSuiteRequested) {
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
		return fromQuery;
	}

	/**
	 * Build the List of Id Fields that the Cuf Query will select.
	 * @param cufQueryDepth The depth of the Cuf Query
	 * @param isTestSuiteRequested If at least one TestSuite Cuf is requested
	 * @return The List of Fields the Cuf Query have to select
	 */
	private List<Field<?>> buildFieldsListOfEntitiesQuery(int cufQueryDepth, boolean isTestSuiteRequested, boolean isIterationSelected, boolean isTestSuiteSelected) {
		List<Field<?>> fieldList = new ArrayList<>();
		if(cufQueryDepth > 1 || isIterationSelected) {
			fieldList.add(ITERATION.ITERATION_ID);
		}
		if(cufQueryDepth > 2) {
			fieldList.add(ITERATION_TEST_PLAN_ITEM.ITEM_TEST_PLAN_ID);
			fieldList.add(TEST_CASE.TCLN_ID);
			if (isTestSuiteRequested || isTestSuiteSelected) {
				fieldList.add(TEST_SUITE.ID);
			}
		}
		if(cufQueryDepth > 3) {
			fieldList.add(EXECUTION.EXECUTION_ID);
		}
		if(cufQueryDepth > 4) {
			fieldList.add(EXECUTION_STEP.EXECUTION_STEP_ID);
		}
		return fieldList;
	}

	/**
	 * Return the depth of the Query.
	 * @param entityTypeList The list of the EntityTypes involved in the Query
	 * @return The depth of the Query
	 */
	private int getDepthOfEntitiesQuery(Set<EntityType> entityTypeList) {
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
