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

import com.querydsl.core.Tuple;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.EntityReference;
import org.squashtest.tm.domain.query.ColumnType;
import org.squashtest.tm.domain.query.DataType;
import org.squashtest.tm.domain.jpql.ExtendedHibernateQuery;
import org.squashtest.tm.service.query.ConfiguredQuery;
import org.squashtest.tm.service.query.QueryProcessingService;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;


/**
 * <p>This is the class that will find the data matching the criteria supplied as a {@link ConfiguredQuery}, that in turn will be turnde into a Querydsl query.</p>
 *
 * <h1>What is this ?</h1>
 *
 * <p>A {@link ConfiguredQuery} is made of  :
 *
 * <ul>
 * <li>a {@link org.squashtest.tm.domain.query.QueryModel}, that define the query per-se, </li>
 * <li>a Paging, that slices your data in digestible quantities</li>
 * <li>a Scope, that restrict the data that should be fetched according to the user permissions and preference.< </li>
 * </ul>
 *
 * Aside from this query description, the ConfiguredQuery let you defined if the result set should be limited in size (the paging)
 * or scope (the scope).
 * </p>
 *
 * <p>Based on this specification the {@link QueryProcessingServiceImpl} will design the query plan and run it. The rest of this javadoc
 * is a technical documentation of its internal processes.</p>
 *
 *
 * <h1>Query plan</h1>
 *
 * <p>
 * The global query is composed of one main query and several optional subqueries depending on the columns (namely the
 * CALCULATED columns).
 * </p>
 *
 * <h3>Domain</h3>
 *
 * <p>
 * The total queryable domain is the following : </br></br>
 *
 * <table>
 * <tr>
 * <td>Campaign</td>
 * <td>&lt;-&gt;</td>
 * <td>Iteration</td>
 * <td>&lt;-&gt;</td>
 * <td>IterationTestPlanItem</td>
 * <td>&lt;-&gt;</td>
 * <td>TestCase</td>
 * <td>&lt;-&gt;</td>
 * <td>(RequirementVersionCoverage)</td>
 * <td>&lt;-&gt;</td>
 * <td>RequirementVersion</td>
 * <td>&lt;-&gt; </td>
 * <td>Requirement</td>
 * </tr>
 * <tr>
 * <td>Issue</td>
 * <td>&lt;-&gt;</td>
 * <td>Execution</td>
 * <td>&lt;--</td>
 * <td>^</td>
 * <td></td>
 * <td></td>
 * <td></td>
 * <td></td>
 * <td></td>
 * <td></td>
 * </tr>
 * </table>
 *
 * </p>
 *
 * <p>The ConfiguredQuery will be derived into a query, that will cover a
 * a subset of this domain (or entirely) using joins. The subqueries are generated the same way, and then related to the main query.
 * The specifics of the query plan construction  depend on the "Seed entity", "Target entities" and "Support entities",
 * those concepts are defined below. </p>
 *
 * <h3>Main query</h3>
 *
 * <p>
 * see {@link QueryPlanner}.
 * The main building blocks that defines the main query are the following :
 *
 * <ul>
 * <li><b>Seed Entity</b> : This is the entity from which the query plan begins the entity traversal. If the seed entity
 * is not adequate (eg there is no way to find a query plan that joins over all the entities we need), another seed entity
 * will be tried, and again until a valid query plan is found. Initially the seed entity is set as the entity referenced by
 * the first Aggregation columns, if there are no aggregation columns the entity referenced by the first projection column
 * will be picked instead.
 * </li>
 * <li><b>Target Entities</b> : entities referenced by all the Projections/Aggregation/Filter/Order columns, and by
 * the Scope (see <b>Scope and ACLs</b>). This includes the Seed Entity.
 * </li>
 * <li><b>Support Entities</b> : entities that aren't Target entities but must be joined on in order to join together all
 * the Target entities. For example if a ConfiguredQuery defines Execution as Seed Entity and Campaign as a TargetEntity,
 * then IterationTestPlanItem and Iteration are Support entities.
 * </li>
 * </ul>
 * </p>
 *
 * <p>
 * The main query is thus defined as the minimal subset of the domain that join all the Target entities together via
 * Support Entities, starting with the Seed Entity. The query plan of the main query usually contains only inner joins
 * (either natural join or a JPA join().on() ), but left joins are allowed (see {@link org.squashtest.tm.domain.query.NaturalJoinStyle})
 * </p>
 *
 * <p>
 * <b>Clarification about the Scope and the Main Query (custom scopes only, TM 1.14):</b>
 * As of TM 1.14 the Scope is now included in order to force a natural joins on the scoped entity. Indeed, when the user
 * defines a query on which the entities that define the scope aren't part of the query itself, the resulting data is
 * void because the Seed Entity or Support Entities are indeed out of the scope.
 * This decision is only half satisfactory : the definitive solution would be to actually reify and handle a Domain on which the query should innerjoin on,
 * but for now this trick will avoid the main problem (ie the case of empty resultset). See more with tickets #6260 and #6275
 * </p>
 *
 *
 * <h3>Select clause generation</h3>
 *
 * <p>
 * The select is built according to the Projection columns. The Projection columns should of course include the data that
 * the user wishes to retrieve, but should also include all the columns that are part of the Order by clause.
 * </p>
 *
 * <h3>Filter application</h3>
 *
 * <p>
 * Filters are restriction applied on the tuples returned by the main query.
 * Each filter is a combination of a column, a comparison operator, and
 * one/several operands (the values against which the comparison is to be made).
 * They are translated in the appropriate Querydsl expression, bound together by appropriate logic operators
 * then inserted in the main query.
 * For now the operands are all values : an operand cannot be a column nor a subquery.
 * </p>
 *
 * <p>
 * Most of the time they are handled as plain "where" clauses, but in some cases
 * a subquery is required. Subqueries in a filter clause are eg "subquery that count the number of test steps for this test case> 3".
 * Filters are processed as follow :
 *
 * <ol>
 * <li>Filters are first grouped by their Target Entity</li>
 * <li>Within a group, filters are combined with a logical combination</li>
 * <li>The filters are then included in he main query :
 * <ul>
 * <li>either inlined as a where clause</li>
 * <li>or as a subquery attached to the main query by a where or having clause</li>
 * </ul>
 * </li>
 * </ol>
 * </p>
 *
 *
 * <h4>logical combination</h4>
 *
 * <p>
 * Each Filter apply to one column that belong to a Target entity (eg, TestCase.label).
 * Usually multiple filters will apply to several columns, but one can also stack multiple Filters on the
 * same column (eg TestCase.label = 'bob', TestCase.label = 'mike').
 * </p>
 *
 * <p>
 * The filters are combined according to the following rules :
 * <ol>
 * <li>Filters applied to the same column define a Filter group. Within a group filters are OR'ed together.</li>
 * <li>Then, filter groups are AND'ed.</li>
 * </ol>
 *
 * </p>
 *
 *
 * <h4>Inlined where clause strategy</h4>
 *
 * <p>
 * In the simplest cases the filters will be inlined in the main query, if the column is of type {@link ColumnType#ATTRIBUTE}.
 * This is your plain "where attribute = value" clause.
 * </p>
 *
 * <h4>Subquery strategy</h4>
 *
 * <p>In more complex cases a subquery will be required. The decision is driven by the attribute 'columnType' of the {@link org.squashtest.tm.domain.query.QueryColumnPrototype}
 * referenced by the filters : if at least one of them is of type {@link ColumnType#CUF} or {@link ColumnType#CALCULATED}
 * then one/several subqueries will be used. We need them for the following reasons :
 *
 * <ol>
 * <li>Custom fields : joining on them in the main query would cause massive tuples growth and headaches about how grouping on what</li>
 * <li>Aggregation operations (calculated attributes) : count(), avg() etc + Having clauses would be incorrect here because the result would be affected by the
 * other filters applied on the main query.</li>
 * </ol>
 *
 * </p>
 *
 * <p>
 * Subqueries have them own Query plan, and are joined with the main query as follow : the Target entity of the outer (main) query
 * of the calculated column will join on the Root entity of the subquery (usually its first aggreated entity, as explained above).
 * Entities are joined on their ids. We choose to use correlated subqueries (joining them as described above) even when an
 * uncorrelated query would do fine, because in practice a clause
 * <pre> where exists (select 1 from ... where ... and inner_col = outer_col) </pre>
 * will outperform
 * <pre> where entity.id in (select id from .... where ...) </pre>
 * by several order of magnitude (especially because in the former the DB can then use the indexed primary keys).
 * </p>
 *
 * <h3>Grouping</h3>
 *
 * <p>
 * Data will be grouped on each {@link org.squashtest.tm.domain.query.QueryAggregationColumn}, in the given order.
 * They are related to the Projection columns in the sense that if one of them define an aggregation operation (eg count(),
 * sum() etc), all the other columns should be aggregated on too.
 * </p>
 * <p>
 * Special care is given for columns of
 * type {@link DataType#DATE} : indeed the desired level of aggregation may be day, month etc. For instance one would never
 * want to group together every month of December across the years. For this reason data grouped by Day will
 * actually grouped by (year,month,day). Same goes for grouping by month, which actually mean grouping by (year,month).
 * </p>
 *
 * <h3>Ordering</h3>
 *
 * <p>
 *     The ordering - the sort by clause - is defined by the {@link org.squashtest.tm.domain.query.QueryOrderingColumn} of the
 *     ConfiguredQuery. Remember that a column that appear in the order clause must also appear in the projection clause.
 * </p>
 *
 * <h1>Result </h1>
 *
 * <p>
 * 	The result will be a list of {@link Tuple}, shaped according to the projection clause. Note that in some cases the tuple may
 * 	be larger than expected, if extra projection columns had to be appended for technical reasons (see {@link ProjectionPlanner}).
 * </p>
 *
 * <h1>Scope and ACLs</h1>
 *
 * <p>
 * Additionally, another special filter will be processed : the Scope, ie a further restriction on which instances of entities
 * the query can return data from. At query execution time it is refined into an Effective Scope, which is the conjunction of :
 * <ul>
 * <li>the content of the projects/folders/nodes selected by the user <b>who designed</b> the ConfiguredQuery (the scope part)</li>
 * <li>the nodes that can actually be READ by the user <b>who is running</b> the QueryProcessingServiceImpl (the acl part)</li>
 * </ul>
 *
 * An amusing side effect of this is that the user may end up with no data available for plot.
 * </p>
 *
 * <p>
 * The Effective Scope will be computed then added to the query after is has been generated. See {@link ScopePlanner}
 * for details on what is going on.
 * </p>
 *
 * @author bsiri
 */
@Component
@SuppressWarnings(value = {"rawtypes", "unchecked"})
public class QueryProcessingServiceImpl implements QueryProcessingService {

	private static final Logger LOGGER = LoggerFactory.getLogger(QueryProcessingServiceImpl.class);
	@Inject
	Provider<ScopePlanner> scopePlannerProvider;

	@PersistenceContext
	private EntityManager em;

	@Override
	public ExtendedHibernateQuery prepareQuery(ConfiguredQuery configuredQuery) {

		// *********** step 1 : create the query ************************

		InternalQueryModel internalQueryModel = new InternalQueryModel(configuredQuery);

		ExtendedHibernateQuery detachedQuery = new QueryBuilder(internalQueryModel).createQuery();

		// *********** step 2 : determine scope and ACL **********************

		ScopePlanner scopePlanner = scopePlannerProvider.get();
		scopePlanner.setQueryModel(internalQueryModel);
		scopePlanner.setHibernateQuery(detachedQuery);
		scopePlanner.setScope((List<EntityReference>) internalQueryModel.getScope());
		scopePlanner.appendScope();

		// ********** step 3 : add paging ************************************

		if (internalQueryModel.getPaging() != null ){
			Pageable page = internalQueryModel.getPaging();
			detachedQuery.offset(page.getOffset());
			detachedQuery.limit(page.getPageSize());
		}

		return detachedQuery;

	}

	@Transactional(readOnly = true)
	@Override
	public List<Tuple> executeQuery(ConfiguredQuery configuredQuery) {

		ExtendedHibernateQuery detachedQuery = prepareQuery(configuredQuery);

		ExtendedHibernateQuery finalQuery = (ExtendedHibernateQuery) detachedQuery.clone(em.unwrap(Session.class));
		try {
			List<Tuple> tuples = finalQuery.fetch();

			return tuples;

		} catch (Exception ex) {
			LOGGER.error("attempted to execute a chart query and failed : ");
			LOGGER.error(finalQuery.toString());
			throw new RuntimeException(ex);
		}


	}


}
