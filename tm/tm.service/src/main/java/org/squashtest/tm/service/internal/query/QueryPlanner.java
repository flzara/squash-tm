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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.squashtest.tm.domain.EntityType;
import org.squashtest.tm.domain.campaign.QCampaign;
import org.squashtest.tm.domain.campaign.QIteration;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.QCustomFieldValue;
import org.squashtest.tm.domain.customfield.QCustomFieldValueOption;
import org.squashtest.tm.domain.customfield.QTagsValue;
import org.squashtest.tm.domain.execution.QExecution;
import org.squashtest.tm.domain.jpql.ExtendedHibernateQuery;
import org.squashtest.tm.domain.query.ColumnType;
import org.squashtest.tm.domain.query.DataType;
import org.squashtest.tm.domain.query.QueryColumnPrototype;
import org.squashtest.tm.domain.query.QueryColumnPrototypeInstance;
import org.squashtest.tm.domain.requirement.QRequirementVersion;
import org.squashtest.tm.domain.testcase.QTestCase;
import org.squashtest.tm.service.internal.query.PlannedJoin.JoinType;

import com.querydsl.core.types.Ops;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.PathBuilder;

/**
 * <p>
 * 	Given a {@link QueryPlan}, this class will resolve which tables must be joined together and return the result as a ExtendedHibernateQuery.
 * 	Whenever possible the natural joins will be used (there is a navigable JPA mapping from the source entity to the
 * 	destination entity); when no navigable mapping is available a join().on() construct is used instead.
 * 	This is decided according to the {@link JoinType} of each {@link PlannedJoin} of the {@link QueryPlan#joinIterator()}.
 * </p>
 *
 * <p>
 * 	For the main query, the entities are all aliased with the camel case version of the class name. Explicitly : testCase, requirementVersion etc.
 *  If there are any inlined subqueries, the relevant entities will also be joined and attached to the main query via
 *  their respective Seed Entity.
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

	private InternalQueryModel internalQueryModel;

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

	QueryPlanner(InternalQueryModel internalQueryModel){
		super();
		this.internalQueryModel = internalQueryModel;
		this.utils = new QuerydslToolbox();
	}


	QueryPlanner(InternalQueryModel internalQueryModel, QuerydslToolbox utils){
		this.internalQueryModel = internalQueryModel;
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
		utils.forceAlias(internalQueryModel.getRootEntity(), mainJoinEntity.getMetadata().getName());
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
		for (QueryColumnPrototypeInstance column : internalQueryModel.getInlinedColumns()){

			EntityPathBase<?> subRootpath = utils.getQBean(column.getColumn().getSpecializedType());

			InternalQueryModel detailedSub = InternalQueryModel.createFor(column);

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
			seedIterator = internalQueryModel.getTargetEntities().iterator();
		}

		if (seedIterator.hasNext()) {
			graphSeed = seedIterator.next();
		}
		else{
			// well, we've exhausted all our seeds...
			// see #createQueryPlan about why.
			throw new RuntimeException("The query cannot be generated : could not find a query plan that would join together all the targeted entities" );
		}
	}

	/*
	 * Here we look for a valid QueryPlan. A QueryPlan is valid if
	 * all target entities can be reached.
	 *
	 * The method succeeds if such plan is found, and fails if
	 * all seeds were exhausted (see #nextSeed above)
	 */
	private QueryPlan createQueryPlan(){

		DomainGraph graph;
		QueryPlan plan;

		do{
			// try the next seed (initially null)
			nextSeed();
			graph = new DomainGraph(internalQueryModel, graphSeed);
			plan = graph.getQueryPlan();
		}
		// the stop condition is that the dreaded corner case
		// is not met.
		while(! isEveryEntityReachable(plan));

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

		if (joininfo.getType() == JoinType.MAPPED){
			addMappedJoin(src, dest, attribute);
		}
		else{
			addUnmappedJoin(src, dest, attribute);
		}

		registerAlias(src);
		registerAlias(dest);
	}


	/**
	 * Creates a join by joining on a mapped relation. The join is of the form :
	 *  .(inner|left)join(src.attribute, dest)
	 *
	 * @param src
	 * @param dest
	 * @param srcNavigableAttribute
	 */
	@SuppressWarnings("rawtypes")
	private void addMappedJoin(EntityPathBase<?> src, EntityPathBase<?> dest, String srcNavigableAttribute){

		// check first that such join doesn't exist yet
		if (! isKnown(dest)){

			PathBuilder join = utils.makePath(src, dest, srcNavigableAttribute);

			switch(internalQueryModel.getJoinStyle()){
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

	/*
	 *	Will create a join of the form .(inner|left)join(dest).on(dest.attribute.eq(src)). It is used
	 *	when the relation between the source and destination has no JPA mapping.
	 *
	 * Note that contrary to the "mapped" join, the navigable destination attribute is owned by the destination
	 * (ie it holds the "foreign key").
	 */
	private void addUnmappedJoin(EntityPathBase<?> src, EntityPathBase<?> dest, String destNavigableAttribute){

		// check first that such join doesn't exist yet
		if (! isKnown(dest)){

			PathBuilder join = utils.makePath(dest, src, destNavigableAttribute);

			switch(internalQueryModel.getJoinStyle()){
				case INNER_JOIN :
					query.innerJoin(dest).on(join.eq(src));
					break;
				case LEFT_JOIN :
					query.leftJoin(dest).on(join.eq(src));
					break;
				default:
					break;
			}
		}
	}

	private void appendCufJoins() {
		//1 detecting all the cuf present in chart internalQueryModel and get the ids of the cufs
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
		extractCufPrototype(cufPrototypesWithIds, internalQueryModel.getFilterColumns());
		extractCufPrototype(cufPrototypesWithIds, internalQueryModel.getAggregationColumns());
		extractCufPrototype(cufPrototypesWithIds, internalQueryModel.getProjectionColumns());
		extractCufPrototype(cufPrototypesWithIds, internalQueryModel.getOrderingColumns());
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
			case TEST_CASE_STEP:
				bindableEntity = BindableEntity.TEST_STEP;
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
			case TEST_SUITE:
				bindableEntity = BindableEntity.TEST_SUITE;
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
	 * Detects that the query plan contains all the entities we need to 
	 * reach.
	 */
	private boolean isEveryEntityReachable(QueryPlan plan){

		Set<InternalEntityType> targetEntities = internalQueryModel.getTargetEntities();
		
		List<InternalEntityType> planedEntities = plan.collectKeys();
		
		return planedEntities.containsAll(targetEntities);

	}

	private boolean isKnown(EntityPathBase<?> path){
		return aliases.contains(path.getMetadata().getName());
	}


	private void registerAlias(EntityPathBase<?> path){
		aliases.add(path.getMetadata().getName());
	}

}
