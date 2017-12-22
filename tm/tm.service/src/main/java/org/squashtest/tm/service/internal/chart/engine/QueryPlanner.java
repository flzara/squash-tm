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
package org.squashtest.tm.service.internal.chart.engine;

import java.util.*;

import com.querydsl.core.types.dsl.*;
import org.squashtest.tm.domain.EntityType;
import org.squashtest.tm.domain.campaign.QCampaign;
import org.squashtest.tm.domain.campaign.QIteration;
import org.squashtest.tm.domain.chart.*;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.QCustomFieldValue;
import org.squashtest.tm.domain.customfield.QCustomFieldValueOption;
import org.squashtest.tm.domain.customfield.QTagsValue;
import org.squashtest.tm.domain.execution.QExecution;
import org.squashtest.tm.domain.jpql.ExtendedHibernateQuery;
import org.squashtest.tm.domain.requirement.QRequirementVersion;
import org.squashtest.tm.domain.testcase.QTestCase;
import org.squashtest.tm.service.internal.chart.engine.PlannedJoin.JoinType;

import com.querydsl.core.types.Ops;
import com.querydsl.core.types.Predicate;

/**
 * <p>
 * 	This class will plan which table must be joined together and return the result as a ExtendedHibernateQuery.
 * 	Whenever possible the natural joins will be used; however we are dependent on the way the entities were mapped : when no natural join
 * 	is available a where clause will be used.
 * </p>
 *
 * <p>
 * 	For the main query, the entities are all aliased with the camel case version of the class name. Explicitly : testCase, requirementVersion etc.
 *  If there are any inlined subqueries, the relevant entities will also be joined and attached to the main query via their respective root entity.
 *  To do so a new instance of QueryPlanner will be created and invoked in "append mode" using a subquery.
 *  The extra entities joined that way will be aliased with a deterministic suffix that depend on the ColumnPrototype id that stands for the
 *  inlined subquery.
 * </p>
 *
 * <p>
 * 	Remember that the query created is detached from the session, don't forget to attach it via query.clone(session)
 * </p>
 *
 * <p>See javadoc on {@link ChartDataFinder}</p>
 *
 *
 * @author bsiri
 *
 */

class QueryPlanner {

	private DetailedChartQuery definition;

	private QuerydslToolbox utils;

	// ******** work variables ************

	private Set<String> aliases = new HashSet<>();

	// may be either the normal root entity, either the measured entity
	// (see createQueryPlan() and comments within)
	private InternalEntityType actualRootEntity;


	// ***** optional argument, you may specify them if using a ChartQuery with strategy INLINED ****
	// ***** see section "configuration builder" to check what they do *************

	private ExtendedHibernateQuery<?> query;

	// for test purposes
	QueryPlanner(){
		super();
	}

	QueryPlanner(DetailedChartQuery definition){
		super();
		this.definition = definition;
		this.utils = new QuerydslToolbox();
	}


	QueryPlanner(DetailedChartQuery definition, QuerydslToolbox utils){
		this.definition = definition;
		this.utils = utils;
	}


	// ====================== configuration builder =================

	/**
	 * Use if you intend to use the queryplanner to append on a mainquery,
	 * before invoking {@link #modifyQuery()}. This method supplies the
	 * said main query.
	 *
	 * @param existingQuery
	 * @return
	 */
	QueryPlanner appendToQuery(ExtendedHibernateQuery<?> existingQuery){
		this.query = existingQuery;
		return this;
	}

	/**
	 * Use if you intend to use the queryplanner to append on a mainquery,
	 * before invoking {@link #modifyQuery()}. This method supplies
	 * the root entity, where the join should be made between the main query and
	 * this query
	 *
	 * @param
	 * @return
	 */
	QueryPlanner joinRootEntityOn(EntityPathBase<?> axeEntity){
		utils.forceAlias(definition.getRootEntity(), axeEntity.getMetadata().getName());
		return this;
	}


	// ====================== main API ===================================

	/**
	 * Will create a new query from scratch, based on the ChartQuery. No conf asked.
	 *
	 * @return
	 */
	ExtendedHibernateQuery<?> createQuery(){
		query = new ExtendedHibernateQuery();
		doTheJob();
		appendCufJoins();
		return query;
	}




	/**
	 * Used when an existing HibernateQuery must now join on more columns. Will append to the query (configured with {@link #appendToQuery(ExtendedHibernateQuery)})
	 * the joins defined in the ChartQuery. This new set of joins will be attached to the
	 * main query on the root entity of this ChartQuery.
	 *
	 *
	 */

	void modifyQuery(){
		doTheJob();
	}

	// *********************** internal job **************************

	private void doTheJob(){

		// get the query plan : the orderly set of joins this
		// planner must now put together

		QueryPlan plan = createQueryPlan();

		// init metadata required for the query
		init();

		// now get the query done

		for (Iterator<PlannedJoin> iter = plan.joinIterator(); iter.hasNext();) {

			PlannedJoin join = iter.next();

			addJoin(join);

		}

		// now process the inlined subqueries and append their table to the
		// join clauses as well.
		for (ColumnPrototypeInstance column : definition.getInlinedColumns()){

			EntityPathBase<?> subRootpath = utils.getQBean(column.getColumn().getSpecializedType());

			DetailedChartQuery detailedSub = new DetailedChartQuery(column);

			QuerydslToolbox toolbox = new QuerydslToolbox(column);

			QueryPlanner subPlanner = new QueryPlanner(detailedSub, toolbox).appendToQuery(query).joinRootEntityOn(subRootpath);

			subPlanner.modifyQuery();
		}
	}

	private QueryPlan createQueryPlan(){

		// first, try a normal query plan
		actualRootEntity = definition.getRootEntity();
		DomainGraph domain = new DomainGraph(definition);
		QueryPlan plan = domain.getQueryPlan();

		// test whether the "left where join" corner case occurs
		// if so, generate a reverse query plan instead.
		// for now this is enough. Later on if really we are stuck,
		// consider mapping the missing relationships instead.
		// (eg TestCase -> IterationTestPlanItem)
		if (hasLeftWhereJoin(plan)){
			actualRootEntity = definition.getMeasuredEntity();
			domain = new DomainGraph(definition);
			domain.reversePlan();
			plan = domain.getQueryPlan();
		}

		return plan;
	}

	@SuppressWarnings("rawtypes")
	private void init(){

		// register the content of the query,
		// it is useful mostly in the append mode.
		aliases = utils.getJoinedAliases(query);

		// initialize the query if needed
		EntityPathBase<?> rootPath = utils.getQBean(actualRootEntity);
		if (! isKnown(rootPath)){
			query.from(rootPath);
		}

	}


	private void addJoin(PlannedJoin joininfo){

		EntityPathBase<?> src = utils.getQBean(joininfo.getSrc());
		EntityPathBase<?> dest = utils.getQBean(joininfo.getDest());
		String attribute = joininfo.getAttribute();

		if (joininfo.getType() == JoinType.NATURAL){
			addNaturalJoin(src, dest, attribute);
		}
		else{
			addWhereJoin(src, dest, attribute);
		}

		registerAlias(src);
		registerAlias(dest);
	}


	@SuppressWarnings("rawtypes")
	private void addNaturalJoin(EntityPathBase<?> src, EntityPathBase<?> dest, String attribute){

		// check first that such join doesn't exist yet
		if (! isKnown(dest)){

			PathBuilder join = utils.makePath(src, dest, attribute);

			switch(definition.getJoinStyle()){
			case INNER_JOIN :
				query.innerJoin(join, dest);
				break;
			case LEFT_JOIN :
				query.leftJoin(join, dest);
				break;
			}
		}
	}

	private void addWhereJoin(EntityPathBase<?> src, EntityPathBase<?> dest, String attribute){

		// if both aliases are known, don't do it
		if (isKnown(src) && isKnown(dest)){
			return;
		}

		// else, add the missing entity(ies)
		if (! isKnown(src)){
			query.from(src);
		}

		if (! isKnown(dest)){
			query.from(dest);
		}

		// remember that the join is made from the dest to the source in this case
		PathBuilder<?> destForeignKey = utils.makePath(dest, src, attribute);

		Predicate condition = Expressions.booleanOperation(Ops.EQ, destForeignKey, src);

		query.where(condition);
	}

	private void appendCufJoins() {
		//1 detecting all the cuf present in chart definition and get the ids of the cufs
		Map<ColumnPrototype,Set<Long>> cufPrototypesWithIds = extractAllCufPrototype();
		//2 create a where join (ie a cartesian product with where clause) for each cuf column needed in chart aliased by the column protoLabel and the cuf ID
		//We have to do it this way because no hibernate mapping exist between an entity and the cuf
		createCufJoins(cufPrototypesWithIds);
	}

	/**
	 * Extract all cuf columns proto in filters, axis and measures
	 * @return
     */
	private Map<ColumnPrototype, Set<Long>> extractAllCufPrototype() {
		Map<ColumnPrototype, Set<Long>> cufPrototypesWithIds= new HashMap<>();
		extractCufPrototype(cufPrototypesWithIds, definition.getFilters());
		extractCufPrototype(cufPrototypesWithIds, definition.getAxis());
		extractCufPrototype(cufPrototypesWithIds, definition.getMeasures());
		return cufPrototypesWithIds;
	}

	private void extractCufPrototype(Map<ColumnPrototype, Set<Long>> cufPrototypesWithIds, List<? extends ColumnPrototypeInstance> prototypes) {
		for (ColumnPrototypeInstance prototypeInstance : prototypes) {
			ColumnPrototype columnPrototype = prototypeInstance.getColumn();
			if (columnPrototype.getColumnType() == ColumnType.CUF) {
				Set<Long> cufIds = cufPrototypesWithIds.get(columnPrototype);
				if (cufIds == null) {
					Set<Long> ids = new HashSet<>();
					ids.add(prototypeInstance.getCufId());
					cufPrototypesWithIds.put(columnPrototype,ids);
				}else{
					cufIds.add(prototypeInstance.getCufId());
				}
			}
		}
	}

	private void createCufJoins(Map<ColumnPrototype, Set<Long>> cufPrototypes) {
		for (Map.Entry<ColumnPrototype,Set<Long>> entry :cufPrototypes.entrySet()) {
			Set<Long> cufIds = entry.getValue();
			ColumnPrototype columnPrototype = entry.getKey();
			for (Long cufId : cufIds) {
				String alias = utils.getCustomFieldValueStandardTableAlias(columnPrototype, cufId);
				if(columnPrototype.getDataType().equals(DataType.TAG)){
					String cufValueOptionAlias = utils.getCustomFieldValueOptionTableAlias(columnPrototype, cufId);
					createJoinForMultipleValues(columnPrototype, cufId, alias, cufValueOptionAlias);
				}
				else {
					createJoinForUniqueValue(columnPrototype, cufId, alias);
				}
			}
		}
	}

	private void createJoinForUniqueValue(ColumnPrototype columnPrototype, Long cufId, String alias) {
		//now we join as cartesian product because we have no hibernate mapping between entities
		QCustomFieldValue qCustomFieldValue = new QCustomFieldValue(alias);
		query.from(qCustomFieldValue);
		//now we need to filter out this ugly cartesian product with three where clause.
		//but as CUF can be linked to different entity type, we need to create the good clause for our actual cuf.
		BindableEntity boundEntityType = getBoundEntityType(columnPrototype);
		query.where(qCustomFieldValue.boundEntityType.eq(boundEntityType));
		//join clause on cufValue.boundEntity.id = "boundEntity".id
		query.where(qCustomFieldValue.boundEntityId.eq(getEntityIdForCufValue(columnPrototype)));
		//now we filter on cuf ID so only the tuples with the good cuf will be kept.
		query.where(qCustomFieldValue.cufId.eq(cufId));
	}

	private void createJoinForMultipleValues(ColumnPrototype columnPrototype, Long cufId, String alias, String cufValueOptionAlias) {
		//if TAG we make a cross join on TagsValue and an inner joins on custom field value option as we need one tuple for each cuf value option
		QTagsValue qTagsValue = new QTagsValue(alias);
		QCustomFieldValueOption qCustomFieldValueOption = new QCustomFieldValueOption(cufValueOptionAlias);
		query.from(qTagsValue);
		BindableEntity boundEntityType = getBoundEntityType(columnPrototype);
		query.where(qTagsValue.boundEntityType.eq(boundEntityType));
		query.where(qTagsValue.boundEntityId.eq(getEntityIdForCufValue(columnPrototype)));
		query.where(qTagsValue.cufId.eq(cufId));
		query.innerJoin(qTagsValue.selectedOptions,qCustomFieldValueOption);
	}

	//return the path for the entity attribute id for the designed cuf column prototype
	private NumberPath<Long> getEntityIdForCufValue(ColumnPrototype columnPrototype) {
		EntityType entityType = columnPrototype.getEntityType();
		NumberPath<Long> id;
		switch (entityType){
			case TEST_CASE:
				id = QTestCase.testCase.id;
				break;
			case REQUIREMENT_VERSION:
				id = QRequirementVersion.requirementVersion.id;
				break;
			case CAMPAIGN:
				id = QCampaign.campaign.id;
				break;
			case ITERATION:
				id = QIteration.iteration.id;
				break;
			case EXECUTION:
				id = QExecution.execution.id;
				break;
			default:
				throw new IllegalArgumentException("This entity type couldn't have cuf bound to them or can't actually be in custom report chart.");
		}
		return id;
	}

	private BindableEntity getBoundEntityType(ColumnPrototype columnPrototype) {
		EntityType entityType = columnPrototype.getEntityType();
		BindableEntity bindableEntity;
		switch (entityType){
			case TEST_CASE:
				bindableEntity = BindableEntity.TEST_CASE;
				break;
			case REQUIREMENT_VERSION:
				bindableEntity = BindableEntity.REQUIREMENT_VERSION;
				break;
			case CAMPAIGN:
				bindableEntity = BindableEntity.CAMPAIGN;
				break;
			case ITERATION:
				bindableEntity = BindableEntity.ITERATION;
				break;
			case EXECUTION:
				bindableEntity = BindableEntity.EXECUTION;
				break;
			default:
				throw new IllegalArgumentException("This entity type couldn't have cuf bound to them or can't actually be in custom report chart.");
		}
		return bindableEntity;
	}


	/*
	 * Detects whether the given plan contains a "left where join", which is impossible to
	 * actually implement
	 */
	private boolean hasLeftWhereJoin(QueryPlan plan){

		boolean hasLeftJoin;
		boolean hasWhereJoin = false;

		// condition 1
		hasLeftJoin = definition.getJoinStyle() == NaturalJoinStyle.LEFT_JOIN;

		for (Iterator<PlannedJoin> iter = plan.joinIterator(); iter.hasNext();) {
			PlannedJoin join = iter.next();
			if (join.getType() == JoinType.WHERE){
				hasWhereJoin =true;
				break;
			}
		}

		return hasLeftJoin && hasWhereJoin;
	}

	private boolean isKnown(EntityPathBase<?> path){
		return aliases.contains(path.getMetadata().getName());
	}


	private void registerAlias(EntityPathBase<?> path){
		aliases.add(path.getMetadata().getName());
	}

}
