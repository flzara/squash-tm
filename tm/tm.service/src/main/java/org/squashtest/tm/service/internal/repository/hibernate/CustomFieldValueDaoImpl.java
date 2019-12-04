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
import org.jooq.SelectOrderByStep;
import org.jooq.SelectSelectStep;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.squashtest.tm.domain.EntityReference;
import org.squashtest.tm.domain.EntityType;
import org.squashtest.tm.domain.customreport.CustomExportColumnLabel;
import org.squashtest.tm.service.internal.repository.CustomCustomFieldValueDao;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
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
import static org.squashtest.tm.jooq.domain.Tables.DENORMALIZED_FIELD_VALUE;
import static org.squashtest.tm.jooq.domain.Tables.DENORMALIZED_FIELD_VALUE_OPTION;
import static org.squashtest.tm.jooq.domain.Tables.EXECUTION;
import static org.squashtest.tm.jooq.domain.Tables.EXECUTION_EXECUTION_STEPS;
import static org.squashtest.tm.jooq.domain.Tables.EXECUTION_STEP;
import static org.squashtest.tm.jooq.domain.Tables.ITEM_TEST_PLAN_EXECUTION;
import static org.squashtest.tm.jooq.domain.Tables.ITEM_TEST_PLAN_LIST;
import static org.squashtest.tm.jooq.domain.Tables.ITERATION;
import static org.squashtest.tm.jooq.domain.Tables.ITERATION_TEST_PLAN_ITEM;
import static org.squashtest.tm.jooq.domain.Tables.TEST_CASE;
import static org.squashtest.tm.jooq.domain.Tables.TEST_STEP;
import static org.squashtest.tm.jooq.domain.Tables.TEST_SUITE;
import static org.squashtest.tm.jooq.domain.Tables.TEST_SUITE_TEST_PLAN_ITEM;

public class CustomFieldValueDaoImpl implements CustomCustomFieldValueDao {

	@Inject
	private DSLContext Dsl;

	private final static Field CUSTOM_FIELD_VALUE_TAG_VALUE =
		groupConcat(CUSTOM_FIELD_VALUE_OPTION.LABEL).separator(", ");

	private final static Field DENORMALIZED_FIELD_VALUE_TAG_VALUE =
		groupConcat(DENORMALIZED_FIELD_VALUE_OPTION.LABEL).separator(", ");

	private final static Field CUSTOM_FIELD_VALUE_COMPUTED =
		DSL.when(CUSTOM_FIELD_VALUE.FIELD_TYPE.eq("CF"), CUSTOM_FIELD_VALUE.VALUE)
			.when(CUSTOM_FIELD_VALUE.FIELD_TYPE.eq("RTF"), CUSTOM_FIELD_VALUE.LARGE_VALUE)
			.when(CUSTOM_FIELD_VALUE.FIELD_TYPE.eq("NUM"), cast(CUSTOM_FIELD_VALUE.NUMERIC_VALUE, SQLDataType.VARCHAR))
			.when(CUSTOM_FIELD_VALUE.FIELD_TYPE.eq("TAG"), CUSTOM_FIELD_VALUE_TAG_VALUE)
			.as("COMPUTED_VALUE");

	private final static Field DENORMALIZED_FIELD_VALUE_COMPUTED =
		DSL.when(DENORMALIZED_FIELD_VALUE.FIELD_TYPE.eq("CF"), DENORMALIZED_FIELD_VALUE.VALUE)
			.when(DENORMALIZED_FIELD_VALUE.FIELD_TYPE.eq("RTF"), DENORMALIZED_FIELD_VALUE.LARGE_VALUE)
			.when(DENORMALIZED_FIELD_VALUE.FIELD_TYPE.eq("NUM"), cast(DENORMALIZED_FIELD_VALUE.NUMERIC_VALUE, SQLDataType.VARCHAR))
			.when(DENORMALIZED_FIELD_VALUE.FIELD_TYPE.eq("MFV"), DENORMALIZED_FIELD_VALUE_TAG_VALUE)
			.as("COMPUTED_VALUE");

	@Override
	public Map<EntityReference, Map<Long, Object>> getCufValuesMapByEntityReference(
		EntityReference scopeEntity,
		Map<EntityType, List<Long>> entityTypeToCufIdsListMap) {

		// Fetch all the Entities involved in the Campaign which cuf were requested
		Set<EntityReference> allRequestedEntitiesInCampaign = getEntityReferencesFromCampaign(scopeEntity, entityTypeToCufIdsListMap.keySet());
		if(allRequestedEntitiesInCampaign.isEmpty()) {
			return null;
		}
		// Build Standard Cuf Query
		SelectOrderByStep query = buildStandardCufQuery(entityTypeToCufIdsListMap, allRequestedEntitiesInCampaign);
		// Execute the Standard Cuf Query
		Iterator<Record> standardCufqueryResult = query.fetch().iterator();

		Iterator<Record> denormalizedCufQueryResult;

		if (entityTypeToCufIdsListMap.containsKey(EntityType.TEST_STEP)) {
			// Build Denormalized Cuf Query
			SelectOrderByStep denormQuery = buildDenormalizedCufQuery(entityTypeToCufIdsListMap, allRequestedEntitiesInCampaign);
			// Execute the Denormalized Cuf Query
			denormalizedCufQueryResult = denormQuery.fetch().iterator();
		} else {
			// Just create an empty iterator
			denormalizedCufQueryResult = Collections.emptyIterator();
		}

		// Build the resulting map
		return buildResultMapFromQueryResult(standardCufqueryResult, denormalizedCufQueryResult);
	}

	/**
	 * Exploit the Cuf Queries Results to build the Map of all CustomFieldValues by EntityReference.
	 * @param standardCufQueryResult The Results of the Standard Cuf Query
	 * @param denormalizedCufQueryResult The Results of the Denormalized Cuf Query
	 * @return The Map of all CustomFieldValues by EntityReferences
	 */
	private Map<EntityReference, Map<Long, Object>> buildResultMapFromQueryResult(Iterator<Record> standardCufQueryResult, Iterator<Record> denormalizedCufQueryResult) {
		Map<EntityReference, Map<Long, Object>> resultMap = new HashMap<>();
		standardCufQueryResult.forEachRemaining(record -> {
			//Create the EntityReference
			EntityReference entity = new EntityReference(
				EntityType.valueOf(record.get(CUSTOM_FIELD_VALUE.BOUND_ENTITY_TYPE)),
				record.get(CUSTOM_FIELD_VALUE.BOUND_ENTITY_ID));
			// Get the value
			Object cufValue = record.get(CUSTOM_FIELD_VALUE_COMPUTED);
			// Get the Cuf id
			Long cufId = record.get(CUSTOM_FIELD_VALUE.CF_ID);
			// populate the Map
			populateCustomFieldValuesMap(entity, cufId, cufValue, resultMap);
		});
		denormalizedCufQueryResult.forEachRemaining(record -> {
			// Create the EntityReference with denormalized Cufs
			EntityReference entity = new EntityReference(
				EntityType.TEST_STEP,
				record.get(DENORMALIZED_FIELD_VALUE.DENORMALIZED_FIELD_HOLDER_ID));
			// Get the value
			Object cufValue = record.get(DENORMALIZED_FIELD_VALUE_COMPUTED);
			// Get the Cuf id
			Long cufId = record.get(CUSTOM_FIELD_VALUE.CF_ID);
			// populate the Map
			populateCustomFieldValuesMap(entity, cufId, cufValue, resultMap);
		});
		return resultMap;
	}

	/**
	 * Given an EntityReference, the Id of the Cuf and the Value of the Cuf, populate the result Map of the CustomFields.
	 * @param entityReference The EntityReference
	 * @param cufValue The Cuf Value
	 * @param resultMap The Result Map to populate
	 */
	private void populateCustomFieldValuesMap(
		EntityReference entityReference,
		Long cufId, Object cufValue,
		Map<EntityReference, Map<Long, Object>> resultMap) {

		Map<Long, Object> currentEntityMap = resultMap.get(entityReference);
		if(currentEntityMap == null) {
			resultMap.put(entityReference, new HashMap<>());
			currentEntityMap = resultMap.get(entityReference);
		}
		currentEntityMap.put(cufId, cufValue);
	}

	/**
	 * Build the Query to retrieve all requested Standard CustomFieldValues of all requested Entities.
	 * @param entityTypeToCufIdsListMap The Map giving the List of CustomField ids to retrieve by EntityType
	 * @param allRequestedEntitiesInCampaign The Set of all the EntityReferences which CustomFieldValues are to retrieve
	 * @return The Query to retrieve all requested Standard CustomFieldValues of all requested Entities
	 */
	private SelectSelectStep buildStandardCufQuery(
		Map<EntityType, List<Long>> entityTypeToCufIdsListMap, Set<EntityReference> allRequestedEntitiesInCampaign) {

		SelectSelectStep query1 =
			Dsl.select(
				CUSTOM_FIELD_VALUE.BOUND_ENTITY_TYPE,
				CUSTOM_FIELD_VALUE.BOUND_ENTITY_ID,
				CUSTOM_FIELD_VALUE.CF_ID,
				CUSTOM_FIELD_VALUE_COMPUTED
			);

		query1.from(CUSTOM_FIELD_VALUE)
			.leftJoin(CUSTOM_FIELD_VALUE_OPTION).on(CUSTOM_FIELD_VALUE.CFV_ID.eq(CUSTOM_FIELD_VALUE_OPTION.CFV_ID));

		query1.where(
			buildWhereConditionOfCufQuery(entityTypeToCufIdsListMap, allRequestedEntitiesInCampaign));

		query1.groupBy(CUSTOM_FIELD_VALUE.CFV_ID);

		return query1;
	}

	/**
	 * Build the Query to retrieve all requested Denormalized CustomFieldValues of all requested Entities
	 * (but only the TestSteps will be relevant here).
	 * @param entityTypeToCufIdsListMap The Map giving the List of CustomField ids to retrieve by EntityType
	 * @param allRequestedEntitiesInScope The Set of all the EntityReferences which CustomFieldValues are to retrieve
	 * @return The Query to retrieve all requested Denormalized CustomFieldValues of all requested Entities.
	 */
	private SelectSelectStep buildDenormalizedCufQuery(
		Map<EntityType, List<Long>> entityTypeToCufIdsListMap, Set<EntityReference> allRequestedEntitiesInScope) {

		SelectSelectStep query2 = Dsl.select(
			DENORMALIZED_FIELD_VALUE.DENORMALIZED_FIELD_HOLDER_TYPE,
			DENORMALIZED_FIELD_VALUE.DENORMALIZED_FIELD_HOLDER_ID,
			CUSTOM_FIELD_VALUE.CF_ID,
			DENORMALIZED_FIELD_VALUE_COMPUTED
		);
		query2.from(DENORMALIZED_FIELD_VALUE)
			.leftJoin(DENORMALIZED_FIELD_VALUE_OPTION).on(DENORMALIZED_FIELD_VALUE_OPTION.DFV_ID.eq(DENORMALIZED_FIELD_VALUE.DFV_ID))
			.innerJoin(CUSTOM_FIELD_VALUE).on(DENORMALIZED_FIELD_VALUE.CFV_ID.eq(CUSTOM_FIELD_VALUE.CFV_ID));

		query2.where(
			buildWhereConditionOfDenormalizedCufQuery(entityTypeToCufIdsListMap, allRequestedEntitiesInScope));

		query2.groupBy(DENORMALIZED_FIELD_VALUE.DFV_ID, CUSTOM_FIELD_VALUE.CFV_ID);

		return query2;
	}

	/**
	 * Build the Where condition of the CustomFieldValues Query.
	 * @param entityTypeToCufIdsListMap The Map of Cuf ids List by EntityType
	 * @param allRequestedEntitiesInCampaign All the EntityReferences which CustomFieldValues are requested
	 * @return The Where condition of the CustomFieldValues Query
	 */
	private Condition buildWhereConditionOfCufQuery(
		Map<EntityType, List<Long>> entityTypeToCufIdsListMap, Set<EntityReference> allRequestedEntitiesInCampaign) {

		Condition whereCondition = null;
		for(EntityReference entityReference: allRequestedEntitiesInCampaign) {
			List<Long> cufIdList = entityTypeToCufIdsListMap.get(entityReference.getType());
			Condition currentCondition = null;
			if(cufIdList!= null){
				currentCondition =	CUSTOM_FIELD_VALUE.BOUND_ENTITY_TYPE.eq(entityReference.getType().toString())
					.and(CUSTOM_FIELD_VALUE.BOUND_ENTITY_ID.eq(entityReference.getId()))
					.and(CUSTOM_FIELD_VALUE.CF_ID.in(cufIdList));
			}
			if(whereCondition == null) {
				whereCondition = currentCondition;
			} else {
				whereCondition = whereCondition.or(currentCondition);
			}
		}
		return whereCondition;
	}

	/**
	 * Build the Where condition of the DenormalizedCustomFieldValues Query.
	 * @param entityTypeToCufIdsListMap The Map of Cuf ids List by EntityType
	 * @param allRequestedEntitiesInCampaign All the EntityReferences which CustomFieldValues are requested
	 * @return The Where condition of the DenormalizedCustomFieldValues Query
	 */
	private Condition buildWhereConditionOfDenormalizedCufQuery(
		Map<EntityType, List<Long>> entityTypeToCufIdsListMap, Set<EntityReference> allRequestedEntitiesInCampaign) {

		Condition whereCondition = null;

		for(EntityReference entityReference: allRequestedEntitiesInCampaign) {

			if(EntityType.EXECUTION_STEP.equals(entityReference.getType())) {
				Condition currentCondition =
					DENORMALIZED_FIELD_VALUE.DENORMALIZED_FIELD_HOLDER_TYPE.eq("EXECUTION_STEP")
						.and(DENORMALIZED_FIELD_VALUE.DENORMALIZED_FIELD_HOLDER_ID.eq(entityReference.getId()))
						.and(CUSTOM_FIELD_VALUE.CF_ID.in(entityTypeToCufIdsListMap.get(EntityType.TEST_STEP)));
				if(whereCondition == null) {
					whereCondition = currentCondition;
				} else {
					whereCondition = whereCondition.or(currentCondition);
				}
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
	 * @param scopeEntity The EntityReference representing the scope
	 * @param requestedEntityTypes The EntityType of the requested EntityReferences
	 * @return The Query to retrieve all the requested EntityReferences from the Campaign
	 */
	private SelectJoinStep buildEntityReferencesQuery(
		EntityReference scopeEntity, Set<EntityType> requestedEntityTypes) {

		int cufQueryDepth = getDepthOfEntitiesQuery(requestedEntityTypes);
		boolean isTestSuiteRequested = requestedEntityTypes.contains(EntityType.TEST_SUITE);
		boolean isIterationSelected = EntityType.ITERATION.equals(scopeEntity.getType());
		boolean isTestSuiteSelected = EntityType.TEST_SUITE.equals(scopeEntity.getType());

		List<Field<?>> fieldList = buildFieldsListOfEntitiesQuery(
			cufQueryDepth, isTestSuiteRequested, isIterationSelected, isTestSuiteSelected);

		SelectSelectStep selectQuery = Dsl.select(fieldList);
		SelectJoinStep fromQuery = buildFromClauseOfEntitiesQuery(selectQuery, cufQueryDepth, isTestSuiteRequested);
		switch(scopeEntity.getType()) {
			case CAMPAIGN: fromQuery.where(CAMPAIGN.CLN_ID.eq(scopeEntity.getId())); break;
			case ITERATION: fromQuery.where(ITERATION.ITERATION_ID.eq(scopeEntity.getId())); break;
			default: fromQuery.where(TEST_SUITE.ID.eq(scopeEntity.getId()));
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
	private Set<EntityReference> buildEntityReferencesSetFromQueryResult(
		EntityReference entity, Set<EntityType> entityTypes, Iterator<Record> queryResult) {

		Set<EntityReference> entityReferenceSet = new HashSet<>();

		Set<EntityType> entityTypesWithoutCampaign = new HashSet(entityTypes);

		// The campaign is the the same all the way and we already know its id
		if (entityTypes.contains(EntityType.CAMPAIGN)) {
			entityReferenceSet.add(new EntityReference(EntityType.CAMPAIGN, entity.getId()));
			// We can remove the campaign from the requested entities
			entityTypesWithoutCampaign.remove(EntityType.CAMPAIGN);
		}


		queryResult.forEachRemaining(record -> {
			for(EntityType entityType : entityTypesWithoutCampaign) {
				Long entityId = record.get(CustomExportColumnLabel.getEntityTypeToIdTableFieldMap().get(entityType));
				// The id could be null if left joined with a non existent entity
				if(entityId != null) {
					if(EntityType.TEST_STEP.equals(entityType)) {
						entityReferenceSet.add(new EntityReference(EntityType.EXECUTION_STEP, entityId));
					} else{
						entityReferenceSet.add(new EntityReference(entityType, entityId));

					}
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
	private SelectJoinStep buildFromClauseOfEntitiesQuery(
		SelectSelectStep selectQuery, int cufQueryDepth, boolean isTestSuiteRequested) {

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
				.leftJoin(EXECUTION_STEP).on(EXECUTION_STEP.EXECUTION_STEP_ID.eq(EXECUTION_EXECUTION_STEPS.EXECUTION_STEP_ID))
				.leftJoin(TEST_STEP).on(TEST_STEP.TEST_STEP_ID.eq(EXECUTION_STEP.TEST_STEP_ID));
		}
		return fromQuery;
	}

	/**
	 * Build the List of Id Fields that the Cuf Query will select.
	 * @param cufQueryDepth The depth of the Cuf Query
	 * @param isTestSuiteRequested If at least one TestSuite Cuf is requested
	 * @return The List of Fields the Cuf Query have to select
	 */
	private List<Field<?>> buildFieldsListOfEntitiesQuery(
		int cufQueryDepth, boolean isTestSuiteRequested, boolean isIterationSelected, boolean isTestSuiteSelected) {

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
			fieldList.add(EXECUTION_STEP.TEST_STEP_ID);
			fieldList.add(TEST_STEP.TEST_STEP_ID);
		}
		return fieldList;
	}

	/**
	 * Return the depth of the Query.
	 * @param entityTypeList The list of the EntityTypes involved in the Query
	 * @return The depth of the Query
	 */
	private int getDepthOfEntitiesQuery(Set<EntityType> entityTypeList) {
		if(entityTypeList.contains(EntityType.EXECUTION_STEP)||entityTypeList.contains(EntityType.TEST_STEP))
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
