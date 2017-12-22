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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.EntityReference;
import org.squashtest.tm.domain.Workspace;
import org.squashtest.tm.domain.chart.*;
import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.domain.jpql.ExtendedHibernateQuery;
import org.squashtest.tm.service.internal.chart.engine.proxy.MilestoneAwareChartQuery;
import org.squashtest.tm.service.internal.repository.InfoListItemDao;

import com.querydsl.core.Tuple;


/**
 * <p>This is the class that will find the data matching the criteria supplied as a {@link ChartDefinition}, using the Querydsl engine.</p>
 *
 * <h1>What is this ?</h1>
 *
 * <p>A ChartDefinition defines what you need to formulate the query :
 *
 * <ul>
 * 	<li>What data you want to find (the {@link MeasureColumn}s)</li>
 * 	<li>On what basis you want them (the {@link AxisColumn}s)</li>
 * 	<li>How specific you need the data to be (the {@link Filter}s)</li>
 * </ul>
 * </p>
 *
 * <p>Based on this specification the {@link ChartDataFinder} will design the query plan and run it. The rest of this javadoc
 * is a technical documentation of its internal processes.</p>
 *
 * <h1>Column roles</h1>
 *
 * <p>Here we explain how these roles will be used in a query :</p>
 *
 * <h3>Filters</h3>
 *
 * <p>
 * 	In a query they naturally fit the role of a where clause. However a regular where clause is a simple case : sometimes you would have
 * 	it within a Having clause, and in other times it could be a whole subquery.
 * </p>
 *
 * <h3>AxisColumns</h3>
 *
 * <p>
 *	These columns will be grouped on (in the group by clause). They also appear in the select clause and should of course not
 *	be subject to any aggregation function. In the select clause, they will appear first, and keep the same order as in the list defined in the ChartDefinition.
 * </p>
 *
 * <h3>MeasureColumn</h3>
 *
 * <p>
 * 	These columns appear in the select clause and will be subject to an aggregation method. The aggregation is specified in the MeasureColumn.
 * 	They appear in the select clause, after the axis columns, and keep the same order as in the list defined in the ChartDefinition.
 * </p>
 * <p>
 * 	Most of the time no specific attribute will be specified : in this case the measure column defaults to the id of the observed entity.
 * 	For instance consider a measure which aggregation is 'sum' (count). If the user is interested to know how many test cases match the given filters,
 * 	the aggregation should be made on the test case ids. However if the user picked something more specific - like the test case labels -, the semantics
 * 	becomes how many different labels exist within the test cases that match the filter. This, of course, is a problem for the tool that will design
 * 	the ChartDefinition : the current class will just create and process the query.
 * </p>
 *
 * <h1>Query plan</h1>
 *
 * <p>
 * 	The global query is composed of one main query and several optional subqueries depending on the filters.
 * </p>
 *
 * <h3>Domain</h3>
 *
 * <p>
 * 	The total domain covered by any possible ChartDefinition is the following : </br></br>
 *
 * <table>
 * 	<tr>
 * 		<td>Campaign</td>
 * 		<td>&lt;-&gt;</td>
 * 		<td>Iteration</td>
 * 		<td>&lt;-&gt;</td>
 * 		<td>IterationTestPlanItem</td>
 * 		<td>&lt;-&gt;</td>
 * 		<td>TestCase</td>
 * 		<td>&lt;-&gt;</td>
 * 		<td>(RequirementVersionCoverage)</td>
 * 		<td>&lt;-&gt;</td>
 * 		<td>RequirementVersion</td>
 * 		<td>&lt;-&gt; </td>
 * 		<td>Requirement</td>
 * 	</tr>
 * 	<tr>
 * 		<td>Issue</td>
 * 		<td>&lt;-&gt;</td>
 * 		<td>Execution</td>
 * 		<td>&lt;--</td>
 * 		<td>^</td>
 * 		<td></td>
 * 		<td></td>
 * 		<td></td>
 * 		<td></td>
 * 		<td></td>
 * 		<td></td>
 * 	</tr>
 * </table>
 *
 *  </p>
 *
 * 	<p>Depending on the ChartDefinition, a main query will be generated as
 * 	a subset of this domain. The specifics of its construction depend on the "Root entity", "Target entities" and
 * 	"Support entities", those concepts are defined below. </p>
 *
 * 	<h3>Main query</h3>
 *
 * <p>
 *	see {@link QueryPlanner}.
 * 	The main building blocks that defines the main query are the following :
 *
 * 	<ul>
 * 		<li><b>Root Entity</b> : This is the entity from which the query plan begins the entity traversal. The root entity is the
 * 		entity targeted by the AxisColumn.  When multiple target entities are eligible, the one with the lowest rank will be the Root entity.</li>
 * 		<li><b>Target Entities</b> : entities on which apply at least one of the MeasureColumns, AxisColumns, Filters, or Scope (see <b>Scope and ACLs</b>)</li>
 * 		<li><b>Support Entities</b> : entities that aren't Target entities but must be joined on in order to join together all
 * 			the Target entities. For example if a ChartDefinition defines Execution as Root entity and Campaign as a TargetEntity,
 * 			then IterationTestPlanItem and Iteration are Support entities.
 *              </li>
 * 	</ul>
 * </p>
 *
 * <p>
 *  The main query is thus defined as the minimal subset of the domain that join all the Target entities together via
 *  Support Entities, starting with the Root entity. All joins in this query will be inner joins (no left nor right joins).
 * </p>
 *
 * <p>
 *      <b>Clarification about the Scope and the Main Query (custom scopes only, TM 1.14):</b>
 *      As of TM 1.14 the Scope is now included in order to force a natural joins on the scoped entity. Indeed, when the user defines a query on which the
 *      scoped entity is neither used in a Filter, Axis or Measure, the resulting data is void because the Root Entity or Support entities are indeed out of the scope.
 *      This decision is only half satisfactory : the definitive solution would be to actually reify and handle a Domain on which the query should innerjoin on,
 *      but for now this trick will avoid the main problem (ie the case of empty resultset). See more with tickets #6260 and #6275
 * </p>
 *
 *
 * <h3>Select clause generation</h3>
 *
 * <p>
 * 	The select clause must of course contain the MeasureColumns with their appropriate aggregation function (like count(distinct ), avg(distinct ) etc).
 * 	For technical reasons they must also include the AxisColumns, because theses are the column on which a row is grouped by.
 * </p>
 *
 * <h3>Filter application</h3>
 *
 * <p>
 * 	Filters are restriction applied on the tuples returned by the main query.
 * 	At the atomic level a filter is a combination of a column, an comparison operator, and
 * 	one/several operands. They are translated in the appropriate Querydsl expression,
 * 	bound together by appropriate logical operators then inserted in the main query.
 * </p>
 *
 * <p>
 * 	Mostly they are no more complex than "where" clauses, but in some cases
 * 	a subquery is required. Filters are treated according to the following process :
 *
 *  <ol>
 *  	<li>Filters are first grouped by Target Entity</li>
 *  	<li>Filters from each groups are then combined in a logical combination</li>
 *  	<li>For each filter happens one of theses :
 *  		<ul>
 *  			<li>inlined as a where clause in the main query</li>
 *  			<li>subquery + where or having clause</li>
 *  		 </ul>
 *  	</li>
 *  </ol>
 * </p>
 *
 *
 * <h4>logical combination</h4>
 *
 * <p>
 * 	Each Filter apply on one column (eg, TestCase.label). Typically, multiple filters will apply on several
 * 	columns. However one can stack multiple Filters on the same column, (eg TestCase.label = 'bob', TestCase.label = 'mike').
 * </p>
 *
 * <p>
 * 	For each entity, the filters are thus combined according to the following rules :
 * 	<ul>
 * 		<li>Filters applied to the same column will be OR'ed together</li>
 * 		<li>Filters applied to different columns will be AND'ed</li>
 * 	</ul>
 *
 * </p>
 *
 *
 * <h4>Inlined where clause strategy</h4>
 *
 * <p>
 * 	In the simplest cases the filters will be inlined in the main query, if the column is of type {@link ColumnType#ATTRIBUTE}
 * </p>
 *
 * <h4>Subquery strategy</h4>
 *
 * <p>In more complex cases a subquery will be required. The decision is driven by the attribute 'columnType' of the {@link ColumnPrototype}
 * 	referenced by the filters : if at least one of them is of type {@link ColumnType#CUF} or {@link ColumnType#CALCULATED}
 * then one/several subqueries will be used. We need them for the following reasons :
 *
 * 	<ol>
 * 		<li>Custom fields : joining on them in the main query would cause massive tuples growth and headaches about how grouping on what</li>
 * 		<li>Aggregation operations (calculated attributes) : count(), avg() etc + Having clauses would be incorrect here because the result would be affected by the
 * 			other filters applied on the main query.</li>
 * 	</ol>
 *
 * </p>
 *
 * <p>
 * 	Subqueries have them own Query plan, and are joined with the main query as follow : the bean of the outer query that defines the
 * calculated attribute will join with the root entity of the subquery (usually its axis).
 * 	We choose to use correlated subqueries (joining them as described above) even when an uncorellated would do fine, because in
 * practice a clause
 * 	<pre> where exists (select 1 from ... where ... and inner_col = outer_col) </pre>
 * will outperform
 * 	<pre> where bean.id in (select id from .... where ...) </pre>
 * by several order of magnitude (especially because in the former the DB can then use the indexed primary keys).
 * </p>
 *
 * <h3>Grouping</h3>
 *
 * <p>
 * 	Data will be grouped on each {@link AxisColumn} in the given order. Special care is given for columns of
 * 	type {@link DataType#DATE} : indeed the desired level of aggregation may be day, month etc. We must be watchful
 * 	of not grouping together every month of December accross the years. For this reason data grouped by Day will
 * 	actually grouped by (year,month,day). Same goes for grouping by month, which actually mean grouping by (year,month)
 * </p>
 *
 * <h1>Result </h1>
 *
 * <p>
 * 	The result will be an array of array of Object. Each row represents a tuple of (x+m) cells, where x = card(AxisColumn)
 * 	and y = card(MeasureColumn). The first batch of cells are those of the AxisColumns, the second batch are those of the
 * 	MeasureColumns.
 * </p>
 *
 * <p>
 * 	Note : a more appropriate representation would be one serie per MeasureColumn, with each tuple made of the x axis cells
 * 	and 1 measure cell. However we prefer to remain agnostic on how the resultset will be interpreted : series might not
 * 	be the preferred way to consume the data after all.
 * </p>
 *
 * <h1>Scope and ACLs</h1>
 *
 * <p>
 * 	Additionally, another special filter will be processed : the Scope, ie the subset of RootEntity on which
 *  the chart query will be applied to. At runtime it is refined into an Effective Scope, which is the conjunction of :
 * 	<ul>
 * 		<li>the content of the projects/folders/nodes selected by the user <b>who designed</b> the ChartDefinition (the scope part)</li>
 * 		<li>the nodes that can actually be READ by the user <b>who is running</b> the ChartDataFinder (the acl part)</li>
 * 	</ul>
 *
 * 	An amusing side effect of this is that the user may end up with no data available for plot.
 * </p>
 *
 * <p>
 * 	 The Effective Scope will be computed then added to the query after is has been generated. See {@link ScopePlanner} for details on what is going on.
 * </p>
 *
 * @author bsiri
 *
 */
@Component
@SuppressWarnings(value={"rawtypes", "unchecked"})
public class ChartDataFinder {

	private static final Logger LOGGER = LoggerFactory.getLogger(ChartDataFinder.class);

	@PersistenceContext
	private EntityManager em;

	@Inject
	private InfoListItemDao infoListItemDao;

	@Inject
	Provider<ScopePlanner> scopePlannerProvider;

	@Transactional(readOnly=true)
	public ChartSeries findData(ChartDefinition definition, List<EntityReference> dynamicScope, Long dashboardId, Long milestoneId, Workspace workspace){

		ChartQuery chartQuery = definition.getQuery();
		DetailedChartQuery enhancedDefinition;
		if(milestoneId != null && workspace!=null && Workspace.isWorkspaceMilestoneFilterable(workspace)){
			IChartQuery milestoneAwareChartQuery = new MilestoneAwareChartQuery(chartQuery,milestoneId,workspace);
			enhancedDefinition = new DetailedChartQuery(milestoneAwareChartQuery);
		}else{
			enhancedDefinition = new DetailedChartQuery(chartQuery);
		}


		// *********** step 1 : create the query ************************

		ExtendedHibernateQuery detachedQuery = new QueryBuilder(enhancedDefinition).createQuery();

		// *********** step 2 : determine scope and ACL **********************

		ScopePlanner scopePlanner = scopePlannerProvider.get();
		scopePlanner.setChartQuery(enhancedDefinition);
		scopePlanner.setHibernateQuery(detachedQuery);
		// *********** override the chart scope if needed ************************
		scopePlanner.setDynamicScope(definition,dynamicScope,dashboardId);
		scopePlanner.appendScope();

		// ******************* step 3 : run the query *************************

		ExtendedHibernateQuery finalQuery = (ExtendedHibernateQuery)detachedQuery.clone(em.unwrap(Session.class));

		try{
			List<Tuple> tuples = finalQuery.fetch();

			// ****************** step 6 : convert the data *********************

			return makeSeries(enhancedDefinition, tuples);
		}
		catch(Exception ex){
			LOGGER.error("attempted to execute a chart query and failed : ");
			LOGGER.error(finalQuery.toString());
			throw new RuntimeException(ex);
		}

	}



	private ChartSeries makeSeries(DetailedChartQuery definition, List<Tuple> tuples){

		List<Object[]> abscissa = new ArrayList<>();

		// initialize temporary structures
		int axsize = definition.getAxis().size();
		int measize = definition.getMeasures().size();

		List[] series = new List[measize];

		for (int me=0; me < measize; me++){
			series[me] = new ArrayList<>(tuples.size());
		}

		// now (double) loop, lets hope the volume of data is not too large
		for (Tuple tuple : tuples){
			// create the entry for the abscissa
			Object[] axis = new Object[axsize];
			for (int ax = 0; ax < axsize; ax++){
				axis[ax] = tuple.get(ax, Object.class);
			}
			abscissa.add(axis);

			// create the entries for the series
			for (int m = 0; m < measize; m++){
				Object v = tuple.get(m+axsize, Object.class);
				series[m].add(v);
			}
		}

		// now build the serie
		ChartSeries chartSeries = new ChartSeries();
		postProcessAbsciss(abscissa, chartSeries,definition);

		for (int m=0; m < measize; m++){
			MeasureColumn measure = definition.getMeasures().get(m);
			chartSeries.addSerie(measure.getLabel(), series[m]);
		}

		return chartSeries;
	}

	private void postProcessAbsciss(List<Object[]> abscissa, ChartSeries chartSeries, DetailedChartQuery definition) {
		List<AxisColumn> columns = definition.getAxis();
		for (int i = 0; i < columns.size(); i++) {
			postProcessColumn(abscissa, columns, i);
		}
		chartSeries.setAbscissa(abscissa);
	}

	/**
	 * As 1.13.3 we only need to postprocess infolist items. If another fancy business rule appears,
	 * change the if to switch, and branch other absciss post process here
     */
	private void postProcessColumn(List<Object[]> abscissa, List<AxisColumn> columns, int i) {
		AxisColumn axisColumn =  columns.get(i);
		if (axisColumn.getDataType() == DataType.INFO_LIST_ITEM){
			postProcessInfoListItem(abscissa, i);
		}
	}

	/**
	 * [Issue 6047] When one the axis is INFOLIST_ITEM.LABEL we must adapt the absciss. The sql generator engine make a request like
	 * select count(*), CODE from INFOLIST_ITEM group by CODE, and we want the label :
	 * with i18n support if the INFOLIST_ITEM is in default system list
     */
	private void postProcessInfoListItem(List<Object[]> abscissa, int i) {
		for (Object[] obj : abscissa) {
            String code = obj[i].toString();
            InfoListItem infoListItem = infoListItemDao.findByCode(code);
            obj[i] = infoListItem.getLabel();
        }
	}


}
