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

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.EntityReference;
import org.squashtest.tm.domain.EntityType;
import org.squashtest.tm.domain.campaign.QCampaign;
import org.squashtest.tm.domain.campaign.QCampaignPathEdge;
import org.squashtest.tm.domain.campaign.QIteration;
import org.squashtest.tm.domain.jpql.ExtendedHibernateQuery;
import org.squashtest.tm.domain.query.QueryColumnPrototype;
import org.squashtest.tm.domain.query.QueryModel;
import org.squashtest.tm.domain.query.QueryProjectionColumn;
import org.squashtest.tm.domain.requirement.QRequirement;
import org.squashtest.tm.domain.requirement.QRequirementPathEdge;
import org.squashtest.tm.domain.testcase.QTestCase;
import org.squashtest.tm.domain.testcase.QTestCasePathEdge;
import org.squashtest.tm.service.security.PermissionEvaluationService;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.squashtest.tm.domain.EntityType.CAMPAIGN;
import static org.squashtest.tm.domain.EntityType.CAMPAIGN_FOLDER;
import static org.squashtest.tm.domain.EntityType.CAMPAIGN_LIBRARY;
import static org.squashtest.tm.domain.EntityType.ITERATION;
import static org.squashtest.tm.domain.EntityType.PROJECT;
import static org.squashtest.tm.domain.EntityType.REQUIREMENT;
import static org.squashtest.tm.domain.EntityType.REQUIREMENT_FOLDER;
import static org.squashtest.tm.domain.EntityType.REQUIREMENT_LIBRARY;
import static org.squashtest.tm.domain.EntityType.TEST_CASE;
import static org.squashtest.tm.domain.EntityType.TEST_CASE_FOLDER;
import static org.squashtest.tm.domain.EntityType.TEST_CASE_LIBRARY;
import static org.squashtest.tm.service.security.Authorizations.READ;
import static org.squashtest.tm.service.security.Authorizations.ROLE_ADMIN;

/**
 * <p>
 * 	This class will stuff a InternalQueryModel with transient additional filters on which parts of the global repository should be considered.
 *	</p>
 *
 *	<h3>specification for TM 1.13</h3>
 *
 * <p>
 * 		A scope is defined as the combination of one or more of the following elements :
 *
 * 		<ol>
 * 			<li>a whole project ,</li>
 * 			<li>some testcase/requirement/campaign libraries,</li>
 * 			<li>some testcase/requirement/campaign folders,</li>
 * 			<li>or testcases/requirement/campaign/iterations</li>
 * 		</ol>
 *
 * 		All of these define a hierarchy of nodes, and only nodes that belong to that hierarchy will be accounted for in the chart. The user is also required to have the
 * 		READ permission on them.
 * </p>
 *
 *	<p>
 * 		Only entities that belong to the main queryModel are filtered that way. Entities part of subqueries won't be,
 * 		and we don't want to : that's why they are in a separated subquery in the first place.
 * </p>
 *
 * <p>
 * 		When one or several project are elected for a scope, then all test cases, campaign, requirement, executions etc must belong to that project.
 *              When the user selected specifically folders and alike in a custom scope, the content will be restricted only to those folders.
 * </p>
 *
 * <h3>Details on the semantic of a scope</h3>
 *
 * <p>
 *
 *
 *
 *	As of TM 1.14 the following rules apply :
 *
 *      <h4>A - custom scope (traumatic approach)</h4>
 *      When a custom scope is defined the main queryModel <b>will be required</b> to join with any entities in order to honor the scope.
 *      For example, if the user selected a test cases folder as a scope, and his chart is aimed for requirements, the implicit semantic
 *      of his chart is "chart on requirements verified by test cases from this folder". In that sense, this approach is said to be "traumatic"
 *      because the semantic of the chart has been altered by the inclusion of the scope.
 *
 *      <h4>B - project scope (gentle approach)</h4>
 *      When the scope is a project, the main queryModel will join with the scope <b>only when relevant</b>. For example, if the user
 *      selected a project as a scope, and his chart is aimed for requirements, the implicit semantic of his chart is "chart on requirements
 *      that belong to this project". Note that, as opposed to the above, those requirements do not have to be verified at all. In that sense,
 *      this approach is said to be "gentle".
 *
 *      <br/>
 *       More details in the main documentation (see {@link QueryProcessingServiceImpl} and issues #6260, #6275.
 * </p>
 * <p>
 * As of Squash 1.15 the rules for scope are following :
 * If the user consult a chart in tc-workspace, req-workspace or campaign workspace,
 * the chart definition scope will be changed by his dynamic selection in workspace tree (hence the second list of {@link EntityReference})
 *
 * For other place (custom report workspace, home workspace...) the rules are :
 * if scopeType = DEFAULT and the user isn't looking the chart through a dashboard, the scope will be the present project containing the chart definition NODE
 * if scopeType = DEFAULT and the user is looking the chart through a dashboard, the scope will be the present project containing the dashboard NODE
 * else, the chart scope will be the custom perimeter defined by the user (PROJECTS or CUSTOM)
 *
 * </p>
 *
 * <h3> How is this done </h3>
 *
 * <p>
 * Depending on the content of the scope the main queryModel may be appended with additional joins and/or subqueries, and additional where clauses will be added. The optional
 * join queries ensure that some key entities will be present in the main queryModel (they will be joined on) because some useful tests will be applied on them by the "where" clauses.
 *
 * For example, if the Scope says that "elements must belong to CampaignFolder 15" and the main queryModel only treat the Execution, in order to test whether this execution belong to
 * that folder we must then append to the main queryModel all required joins from Execution to Campaign because there are no other way to test the ancestry of the Executions.
 *
 * In a second phase, when this is done, some "where" clauses will be added. In our example, the "where" clause would test that the campaign (to which belong the executions)
 * is itself a child or grandchild of CampaignFolder 15.
 * </p>
 *
 * @author bsiri
 *
 */
@Component()
@Scope("prototype")
class ScopePlanner {
	// infrastructure
	@PersistenceContext
	private EntityManager em;

	@Inject
	private PermissionEvaluationService permissionService;

	// work variables
	private InternalQueryModel queryModel;

	private List<EntityReference> scope;

	private ExtendedHibernateQuery<?> hibQuery;

	private ScopeUtils utils;    // created with @PostContruct

	private Set<JoinableColumns> extraJoins;   // computed later in the process

	// ************************ build the tool ******************************

	ScopePlanner() {
		super();
	}

	void setHibernateQuery(ExtendedHibernateQuery<?> hibQuery) {
		this.hibQuery = hibQuery;
	}

	void setQueryModel(InternalQueryModel chartQuery) {
		this.queryModel = chartQuery;
	}

	void setScope(List<EntityReference> scope) {
		this.scope = scope;
	}

	@PostConstruct
	void afterPropertiesSet() {
		utils = new ScopeUtils(em, permissionService);
	}

	// *********************** main methods **********************************

	protected void appendScope() {

		if (scope != null && !scope.isEmpty()) {

			// step 1 : test the ACLs
			filterByACLs();

			// step 2.a : find which join columns must be used.
			prepareExtraJoins();

			if (extraJoins.isEmpty()) {
				return;
			}

			// step 2.b : join the main queryModel with projects and/or libraries if some are specified
			addExtraJoins();

			// step 3 : add the filters
			addWhereClauses();
		}

	}



	// *********************** step 1 ***********************


	private void filterByACLs() {

		List<EntityReference> filtered = new ArrayList<>();

		for (EntityReference ref : scope) {
			if (utils.checkPermissions(ref)) {
				filtered.add(ref);
			}
		}

		scope = filtered;

	}


	// *********************** step 2.a ***********************

	private void prepareExtraJoins() {

		ScopedEntities scopedEntities = new ScopedEntitiesImpl(scope);
		QueriedEntities queriedEntities = new QueriedEntitiesImpl(queryModel);

		deduceExtraJoins(scopedEntities, queriedEntities);
	}

	/*
	 * This will determine which columns are actually included in the final queryModel in order to add
     * the scope. See rules on the class-level javadoc: "Details on the semantic of a scope"
     */
	private void deduceExtraJoins(ScopedEntities scopedEntities, QueriedEntities queriedEntities) {

		this.extraJoins = new HashSet<>();

		Set<JoinableColumns> requiredColumns = scopedEntities.getRequiredJoinColumns();

		for (JoinableColumns scopeColumn : requiredColumns) {

			// case B - project scope
			if (scopeColumn == JoinableColumns.POSSIBLE_COLUMNS_ONLY) {

				// this is the "gentle" approach : joins columns are selected only with
				// those deemed acceptable by the queryModel
				Set<JoinableColumns> queriedColumns = queriedEntities.getPossibleJoinColumns();
				for (JoinableColumns queryColumn : queriedColumns) {
					extraJoins.add(queryColumn);
				}
			}
			// case A - custom scope
			// this is the "traumatic" approach: we don't ask the queryModel for permission
			else {
				extraJoins.add(scopeColumn);
			}
		}

	}

	// *********************** step 2.b ***********************

	/*
	 * In order to extend the main queryModel we will create a dummy queryModel, that will be merged
	 * with the main queryModel just like inlined subqueries do.
	 *
	 * The aim of that dummy queryModel is to make sure that the entities from which a project can be joined on
	 * will be present in the queryModel (even if in some occurences they are already present).
	 *
	 *
	 */
	private void addExtraJoins() {

		// create the dummy queryModel
		QueryModel dummy = createDummyQuery(extraJoins);
		InternalQueryModel detailDummy = InternalQueryModel.createFor(dummy);

		// ... and then run it in a QueryPlanner
		appendScopeToQuery(detailDummy);
	}


	/*
	 * The goal here is to create a Query with as little detail as possible, we just add what
	 * a QueryPlanner would need to add the join clauses to an existing queryModel.
	 *
	 * For that we forge that Query using the same axis than the HibernateQuery we want to extend,
	 * and fake measure columns that exists only to make the QueryPlanner join on them.
	 */
	private QueryModel createDummyQuery(Set<JoinableColumns> fakeMeasureColLabels) {

		QueryModel dummy = new QueryModel();

		// the axis
		dummy.setAggregationColumns(queryModel.getAggregationColumns());

		// now the dummy measures
		List<QueryProjectionColumn> fakeMeasures = new ArrayList<>();
		for (JoinableColumns fakeMeasure : fakeMeasureColLabels) {
			QueryColumnPrototype mProto = utils.findColumnPrototype(fakeMeasure.toString());
			QueryProjectionColumn meas = new QueryProjectionColumn();
			meas.setColumnPrototype(mProto);
			fakeMeasures.add(meas);
		}

		dummy.setProjectionColumns(fakeMeasures);

		// now we have defined the extension of our queryModel
		// we can return
		return dummy;
	}


	private void appendScopeToQuery(InternalQueryModel extraQuery) {
		QueryPlanner planner = new QueryPlanner(extraQuery);
		planner.appendToQuery(hibQuery);
		planner.modifyQuery();
	}

	// *********************** step 3 ***********************


	/*
	 * Once the main queryModel is properly extended, we can add the where clauses.
	 *
	 * According to the class-level documentation, those where clauses will take two forms :
	 *
	 * 1/ where entity.project.id in (....)
	 * 2/ where exists (select 1 from TestCasePathEdge edge where edge.ancestorId in (...) and edge.descendantId = entity.id)
	 *
	 *  All the conditions will be or'ed together within the same entity realm, and and'ed together between the different realms.
	 *
	 *  For example :
	 *  where (
	 *  	(
	 *  		campaign.project.id in (..) or
	 *  		exists (select 1 <blabla> = campaign.id)
	 *  	)
	 *  	and
	 *  	(
	 *  		exists (select 1 <blabla> = testcase.id)
	 *  	)
	 *  )
	 *
	 */
	private void addWhereClauses() {

		BooleanBuilder generalCondition = new BooleanBuilder();

		ScopedEntities scopedEntities = new ScopedEntitiesImpl(scope);

		if (extraJoins.contains(JoinableColumns.TEST_CASE_ID)) {
			BooleanBuilder testcaseClause = whereClauseForTestcases(scopedEntities);
			generalCondition.and(testcaseClause);
		}

		if (extraJoins.contains(JoinableColumns.REQUIREMENT_ID)) {
			BooleanBuilder requirementClause = whereClauseForRequirements(scopedEntities);
			generalCondition.and(requirementClause);
		}

		if (extraJoins.contains(JoinableColumns.CAMPAIGN_ID) ||
			extraJoins.contains(JoinableColumns.ITERATION_ID)) {
			BooleanBuilder campaignClause = whereClauseForCampaigns(scopedEntities);
			generalCondition.and(campaignClause);
		}

		hibQuery.where(generalCondition);

	}


	private BooleanBuilder whereClauseForTestcases(ScopedEntities refmap) {

		BooleanBuilder builder = new BooleanBuilder();
		Collection<Long> ids;
		QTestCase testCase = QTestCase.testCase;

		// project
		ids = refmap.getIds(PROJECT);
		if (notEmpty(ids)) {
			builder.or(testCase.project.id.in(ids));
		}

		// library
		ids = refmap.getIds(TEST_CASE_LIBRARY);
		if (notEmpty(ids)) {
			builder.or(testCase.project.testCaseLibrary.id.in(ids));
		}

		// test case and test case folders
		ids = refmap.getIds(TEST_CASE, TEST_CASE_FOLDER);
		if (notEmpty(ids)) {
			QTestCasePathEdge edge = QTestCasePathEdge.testCasePathEdge;

			ExtendedHibernateQuery<QTestCasePathEdge> subq = new ExtendedHibernateQuery<>();
			subq.select(Expressions.constant(1))
				.from(edge)
				.where(edge.ancestorId.in(ids))
				.where(testCase.id.eq(edge.descendantId));

			Predicate predicate = Expressions.predicate(Ops.EXISTS, subq);

			builder.or(predicate);
		}

		return builder;

	}

	private BooleanBuilder whereClauseForRequirements(ScopedEntities refmap) {

		BooleanBuilder builder = new BooleanBuilder();
		Collection<Long> ids;
		QRequirement requirement = QRequirement.requirement;

		// project
		ids = refmap.getIds(PROJECT);
		if (notEmpty(ids)) {
			builder.or(requirement.project.id.in(ids));
		}

		// library
		ids = refmap.getIds(REQUIREMENT_LIBRARY);
		if (notEmpty(ids)) {
			builder.or(requirement.project.requirementLibrary.id.in(ids));
		}

		// requirement and requirement folders
		ids = refmap.getIds(REQUIREMENT, REQUIREMENT_FOLDER);
		if (notEmpty(ids)) {
			QRequirementPathEdge edge = QRequirementPathEdge.requirementPathEdge;

			ExtendedHibernateQuery<QRequirementPathEdge> subq = new ExtendedHibernateQuery<>();
			subq.select(Expressions.constant(1))
				.from(edge)
				.where(edge.ancestorId.in(ids))
				.where(requirement.id.eq(edge.descendantId));

			Predicate predicate = Expressions.predicate(Ops.EXISTS, subq);

			builder.or(predicate);
		}

		return builder;

	}


	private BooleanBuilder whereClauseForCampaigns(ScopedEntities refmap) {

		BooleanBuilder builder = new BooleanBuilder();
		Collection<Long> ids;
		QCampaign campaign = QCampaign.campaign;
		QIteration iteration = QIteration.iteration;

		// project
		ids = refmap.getIds(PROJECT);
		if (notEmpty(ids)) {
			builder.or(campaign.project.id.in(ids));
		}

		// library
		ids = refmap.getIds(CAMPAIGN_LIBRARY);
		if (notEmpty(ids)) {
			builder.or(campaign.project.requirementLibrary.id.in(ids));
		}

		// requirement and requirement folders
		ids = refmap.getIds(CAMPAIGN, CAMPAIGN_FOLDER);
		if (notEmpty(ids)) {
			QCampaignPathEdge edge = QCampaignPathEdge.campaignPathEdge;

			ExtendedHibernateQuery<QCampaignPathEdge> subq = new ExtendedHibernateQuery<>();
			subq.select(Expressions.constant(1))
				.from(edge)
				.where(edge.ancestorId.in(ids))
				.where(campaign.id.eq(edge.descendantId));

			Predicate predicate = Expressions.predicate(Ops.EXISTS, subq);

			builder.or(predicate);
		}

		// and also, iterations
		ids = refmap.getIds(ITERATION);
		if (notEmpty(ids)) {
			builder.or(iteration.id.in(ids));
		}

		return builder;

	}


	// ************************ utilities ******************************


	private boolean notEmpty(Collection<?> collection) {
		return collection != null && !collection.isEmpty();
	}

	// *********************** JoinableColumns class *************************

	// most of those are the name of some column protototypes
	// see last one for the only exception.
	private enum JoinableColumns {
		TEST_CASE_ID,
		REQUIREMENT_ID,
		CAMPAIGN_ID,
		ITERATION_ID,

		/*
		 * the value POSSIBLE_COLUMNS_ONLY is only used when the scope is of type PROJECT :
         * it means that only columns compatible with the queryModel will be added, and thus we will
         * not force unrelated columns (see javadoc at the class-level)
         */
		POSSIBLE_COLUMNS_ONLY;

		// that method is used for Entities defined in the scope
		// it is defined only for elements a scope can be made of
		static JoinableColumns forScopedType(EntityType type) {
			JoinableColumns column;
			switch (type) {
				case REQUIREMENT_LIBRARY:
				case REQUIREMENT_FOLDER:
				case REQUIREMENT:
					column = REQUIREMENT_ID;
					break;

				case TEST_CASE_LIBRARY:
				case TEST_CASE_FOLDER:
				case TEST_CASE:
					column = TEST_CASE_ID;
					break;

				case CAMPAIGN_LIBRARY:
				case CAMPAIGN_FOLDER:
				case CAMPAIGN:
					column = CAMPAIGN_ID;
					break;

				case ITERATION:
					column = ITERATION_ID;
					break;

				case PROJECT:
					column = POSSIBLE_COLUMNS_ONLY;
					break;

				default:
					throw new IllegalArgumentException(type.toString() + " is not legal as a chart perimeter.");
			}
			return column;
		}

		// that method is used for Entities defined in the queryModel
		// it is defined only for elements a queryModel can be made of
		static JoinableColumns forQueriedType(InternalEntityType type) {
			JoinableColumns column;
			switch (type) {
				case REQUIREMENT:
				case REQUIREMENT_VERSION:
					column = REQUIREMENT_ID;
					break;

				case TEST_CASE:
					column = TEST_CASE_ID;
					break;

				case CAMPAIGN:
				case ITERATION:
				case ITEM_TEST_PLAN:
				case EXECUTION:
				case ISSUE:
					column = CAMPAIGN_ID;
					break;

				// default is probably nothing related : User, Milestone etc
				default:
					column = null;
					break;

			}
			return column;
		}

	}


	// ************************* class ScopeEntities ***************************

	// interface allows us easier mock in test
	private interface ScopedEntities {
		Collection<Long> getIds(EntityType... types);

		Set<JoinableColumns> getRequiredJoinColumns();
	}

	/*
     * This simple class presents the useful data we need to extract from the
     * raw Scope definition in a more convenient way.
     *
     * It basically puts together each EntityType in the scope and all the
     * ids that were selected for it.
     */
	private static final class ScopedEntitiesImpl extends EnumMap<EntityType, Collection<Long>> implements ScopedEntities {

		ScopedEntitiesImpl(List<EntityReference> scope) {
			super(EntityType.class);
			for (EntityReference ref : scope) {
				EntityType type = ref.getType();
				Collection<Long> list = this.get(type);
				if (list == null) {
					list = new ArrayList<>();
					this.put(type, list);
				}
				list.add(ref.getId());
			}
		}

		@Override
		public Collection<Long> getIds(EntityType... types) {
			Collection<Long> result = new ArrayList<>();
			for (EntityType type : types) {
				Collection<Long> ids = this.get(type);
				if (ids != null) {
					result.addAll(ids);
				}
			}
			return result;
		}

		/*
         * returns which columns the scope is required to join on
         */
		@Override
		public Set<JoinableColumns> getRequiredJoinColumns() {
			Set<JoinableColumns> extraJohns = new HashSet<>();
			for (EntityType type : keySet()) {
				extraJohns.add(JoinableColumns.forScopedType(type));
			}
			return extraJohns;
		}

	}


	// ****************** class QueryEntities **************************

	// interface will make testing easier
	private interface QueriedEntities {
		Set<JoinableColumns> getPossibleJoinColumns();
	}


	// Performs the same job than ScopeEntities but for the chart queryModel.
	// Note that it is relevant for the join columns only.

	private static final class QueriedEntitiesImpl implements QueriedEntities {

		private InternalQueryModel internalQuery;

		QueriedEntitiesImpl(InternalQueryModel internalQuery) {
			super();
			this.internalQuery = internalQuery;
		}


		/*
         * Returns the columns on which the queryModel can be joined on
         */
		@Override
		public Set<JoinableColumns> getPossibleJoinColumns() {
			Set<JoinableColumns> possibles = new HashSet<>();
			Set<InternalEntityType> types = internalQuery.getTargetEntities();

			for (InternalEntityType type : types) {
				JoinableColumns column = JoinableColumns.forQueriedType(type);
				if (column != null) {
					possibles.add(column);
				}
			}
			return possibles;
		}

	}


	// ****************** class ScopeUtils *****************************

	// this class exists because it is too close to the DB
	// so we'll need to mock it in the tests. That's also why
	// it is not final.


	private static class ScopeUtils {
		private static final Map<EntityType, String> CLASS_NAME_BY_ENTITY = new EnumMap<>(EntityType.class);

		static {
			CLASS_NAME_BY_ENTITY.put(PROJECT, "org.squashtest.tm.domain.project.Project");
			CLASS_NAME_BY_ENTITY.put(TEST_CASE_LIBRARY, "org.squashtest.tm.domain.testcase.TestCaseLibrary");
			CLASS_NAME_BY_ENTITY.put(TEST_CASE_FOLDER, "org.squashtest.tm.domain.testcase.TestCaseLibraryNode");
			CLASS_NAME_BY_ENTITY.put(TEST_CASE, "org.squashtest.tm.domain.testcase.TestCaseLibraryNode");
			CLASS_NAME_BY_ENTITY.put(REQUIREMENT_LIBRARY, "org.squashtest.tm.domain.requirement.RequirementLibrary");
			CLASS_NAME_BY_ENTITY.put(REQUIREMENT_FOLDER, "org.squashtest.tm.domain.requirement.RequirementLibraryNode");
			CLASS_NAME_BY_ENTITY.put(REQUIREMENT, "org.squashtest.tm.domain.requirement.RequirementLibraryNode");
			CLASS_NAME_BY_ENTITY.put(CAMPAIGN_LIBRARY, "org.squashtest.tm.domain.campaign.CampaignLibrary");
			CLASS_NAME_BY_ENTITY.put(CAMPAIGN_FOLDER, "org.squashtest.tm.domain.campaign.CampaignLibraryNode");
			CLASS_NAME_BY_ENTITY.put(CAMPAIGN, "org.squashtest.tm.domain.campaign.CampaignLibraryNode");
			CLASS_NAME_BY_ENTITY.put(ITERATION, "org.squashtest.tm.domain.campaign.Iteration");
		}

		private final PermissionEvaluationService permissionService;
		private final EntityManager em;

		ScopeUtils(EntityManager entityManager, PermissionEvaluationService permService) {
			super();
			this.permissionService = permService;
			this.em = entityManager;
		}

		boolean checkPermissions(EntityReference ref) {
			String classname = classname(ref);
			Long id = ref.getId();
			return permissionService.hasRoleOrPermissionOnObject(ROLE_ADMIN, READ, id, classname);
		}


		QueryColumnPrototype findColumnPrototype(String colName) {
			Query q = getSession().createQuery("select p from QueryColumnPrototype p where p.label = :label");
			q.setParameter("label", colName);
			return (QueryColumnPrototype) q.uniqueResult();
		}


		private Session getSession() {
			return em.unwrap(Session.class);
		}


		private String classname(EntityReference ref) {

			String className = CLASS_NAME_BY_ENTITY.get(ref.getType());

			if (className == null) {
				throw new IllegalArgumentException(ref.getType() + " is not a valid type for a chart perimeter. Please reconfigure the perimeter of your chart.");
			}
			return className;
		}


	}


}
