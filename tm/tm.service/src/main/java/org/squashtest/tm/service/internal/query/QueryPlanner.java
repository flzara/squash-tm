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
package org.squashtest.tm.service.internal.query;

import com.querydsl.core.types.Ops;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.PathBuilder;
import org.squashtest.tm.domain.EntityType;
import org.squashtest.tm.domain.campaign.QCampaign;
import org.squashtest.tm.domain.campaign.QIteration;
import org.squashtest.tm.domain.query.QueryColumnPrototypeInstance;
import org.squashtest.tm.domain.query.DataType;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.QCustomFieldValue;
import org.squashtest.tm.domain.customfield.QCustomFieldValueOption;
import org.squashtest.tm.domain.customfield.QTagsValue;
import org.squashtest.tm.domain.execution.QExecution;
import org.squashtest.tm.domain.jpql.ExtendedHibernateQuery;
import org.squashtest.tm.domain.query.ColumnType;
import org.squashtest.tm.domain.query.NaturalJoinStyle;
import org.squashtest.tm.domain.query.QueryColumnPrototype;
import org.squashtest.tm.domain.requirement.QRequirementVersion;
import org.squashtest.tm.domain.testcase.QTestCase;
import org.squashtest.tm.service.internal.query.PlannedJoin.JoinType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

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
 * <p>See javadoc on {@link QueryProcessingServiceImpl}</p>
 *
 *
 * @author bsiri
 *
 */

class QueryPlanner {

	private ExpandedConfiguredQuery expandedQuery;

	private QuerydslToolbox utils;

	// ******** work variables ************

	private Set<String> aliases = new HashSet<>();

	// This type represents the initial node from which the
	// domain graph should start when computing the query plan
	private InternalEntityType graphSeed;

	// state variable used when searching for a valid graph seed
	private Iterator<InternalEntityType> seedIterator;


	// ***** optional argument, you may specify them if using a ChartQuery with strategy INLINED ****
	// ***** see section "configuration builder" to check what they do *************

	private ExtendedHibernateQuery<?> query;

	// for test purposes
	QueryPlanner(){
		super();
	}

	QueryPlanner(ExpandedConfiguredQuery expandedQuery){
		super();
		this.expandedQuery = expandedQuery;
		this.utils = new QuerydslToolbox();
	}


	QueryPlanner(ExpandedConfiguredQuery definition, QuerydslToolbox utils){
		this.expandedQuery = definition;
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
	 * If you intend to use the queryplanner to append a subquery to a mainquery,
	 * use this method to define on which entity the subquery and the main query
	 * should be joined on.
	 * Set this up before invoking {@link #modifyQuery()}. The path should be a
	 * QueryDsl path on an entity (eg a testCase), and not an attribute (eg not
	 * testCase.id)
	 *
	 * @param
	 * @return
	 */
	QueryPlanner joinRootEntityOn(EntityPathBase<?> mainJoinEntity){
		utils.forceAlias(expandedQuery.getRootEntity(), mainJoinEntity.getMetadata().getName());
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

		// init the seed
		nextSeed();

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
		for (QueryColumnPrototypeInstance column : expandedQuery.getInlinedColumns()){

			EntityPathBase<?> subRootpath = utils.getQBean(column.getColumn().getSpecializedType());

			ExpandedConfiguredQuery detailedSub = ExpandedConfiguredQuery.createFor(column);

			QuerydslToolbox toolbox = new QuerydslToolbox(column);

			QueryPlanner subPlanner = new QueryPlanner(detailedSub, toolbox).appendToQuery(query).joinRootEntityOn(subRootpath);

			subPlanner.modifyQuery();
		}
	}

	/**
	 * Sets the graphSeed to the next possible value.
	 */
	private void nextSeed(){
		// will initialize the iterator if does not exist yet
		if (seedIterator == null){
			seedIterator = expandedQuery.getTargetEntities().iterator();
		}

		if (seedIterator.hasNext()) {
			graphSeed = seedIterator.next();
		}
		else{
			// well, we've exhausted all our seeds...
			// see #createQueryPlan about why.
			throw new RuntimeException("Could not find a suitable seed for generating the query plan : there is no way to generate " +
										   "a query that would not require a left outer join on an unmapped attribute (something that " +
										   "Hibernate doesn't support)." );
		}
	}

	/*
	 * Here we look for a valid QueryPlan. A QueryPlan is valid if
	 * there is no "left where" joins in it, see #hasLeftWhereJoin
	 * for an explanation of it.
	 *
	 * The method succeeds if such plan is found, and fails if
	 * all seeds were exhausted (see #nextSeed above)
	 */
	private QueryPlan createQueryPlan(){

		DomainGraph graph;
		QueryPlan plan;

		do{
			graph = new DomainGraph(expandedQuery, graphSeed);
			plan = graph.getQueryPlan();
		}
		// the stop condition is that the dreaded corner case
		// is not met.
		while(hasLeftWhereJoin(plan));

		return plan;

	}

	@SuppressWarnings("rawtypes")
	private void init(){

		// register the content of the query,
		// it is useful mostly in the append mode.
		aliases = utils.getJoinedAliases(query);

		// initialize the query if needed
		// Note : at this stage of execution the graph seed is set and valid.
		EntityPathBase<?> rootPath = utils.getQBean(graphSeed);
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

			switch(expandedQuery.getJoinStyle()){
			case INNER_JOIN :
				query.innerJoin(join, dest);
				break;
			case LEFT_JOIN :
				query.leftJoin(join, dest);
				break;
			default:
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
		//1 detecting all the cuf present in chart expandedQuery and get the ids of the cufs
		Map<QueryColumnPrototype,Set<Long>> cufPrototypesWithIds = extractAllCufPrototype();
		//2 create a where join (ie a cartesian product with where clause) for each cuf column needed in chart aliased by the column protoLabel and the cuf ID
		//We have to do it this way because no hibernate mapping exist between an entity and the cuf
		createCufJoins(cufPrototypesWithIds);
	}

	/**
	 * Extract all cuf columns proto in filters, axis and measures
	 * @return
     */
	private Map<QueryColumnPrototype, Set<Long>> extractAllCufPrototype() {
		Map<QueryColumnPrototype, Set<Long>> cufPrototypesWithIds= new HashMap<>();
		extractCufPrototype(cufPrototypesWithIds, expandedQuery.getFilterColumns());
		extractCufPrototype(cufPrototypesWithIds, expandedQuery.getAggregationColumns());
		extractCufPrototype(cufPrototypesWithIds, expandedQuery.getProjectionColumns());
		extractCufPrototype(cufPrototypesWithIds, expandedQuery.getOrderingColumns());
		return cufPrototypesWithIds;
	}

	private void extractCufPrototype(Map<QueryColumnPrototype, Set<Long>> cufPrototypesWithIds, List<? extends QueryColumnPrototypeInstance> prototypes) {
		for (QueryColumnPrototypeInstance prototypeInstance : prototypes) {
			QueryColumnPrototype columnPrototype = prototypeInstance.getColumn();
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

	private void createCufJoins(Map<QueryColumnPrototype, Set<Long>> cufPrototypes) {
		for (Map.Entry<QueryColumnPrototype,Set<Long>> entry :cufPrototypes.entrySet()) {
			Set<Long> cufIds = entry.getValue();
			QueryColumnPrototype columnPrototype = entry.getKey();
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

	private void createJoinForUniqueValue(QueryColumnPrototype columnPrototype, Long cufId, String alias) {
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

	private void createJoinForMultipleValues(QueryColumnPrototype columnPrototype, Long cufId, String alias, String cufValueOptionAlias) {
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
	private NumberPath<Long> getEntityIdForCufValue(QueryColumnPrototype columnPrototype) {
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

	private BindableEntity getBoundEntityType(QueryColumnPrototype columnPrototype) {
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
	 * actually implement.
	 *
	 * A "left where join" is when we need to left-outer join on an unmapped attributes,
	 * which is impossible to express in JPA. The 'where' part comes from the workaround
	 * when attempting to join on unmapped relations : since natural join is impossible
	 * we resort to the cartesian product + where clause.
	 */
	private boolean hasLeftWhereJoin(QueryPlan plan){

		boolean hasLeftJoin;
		boolean hasWhereJoin = false;

		// condition 1
		hasLeftJoin = expandedQuery.getJoinStyle() == NaturalJoinStyle.LEFT_JOIN;

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
