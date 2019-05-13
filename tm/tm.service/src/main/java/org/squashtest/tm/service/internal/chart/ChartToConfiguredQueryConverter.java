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
package org.squashtest.tm.service.internal.chart;


import com.querydsl.core.types.Order;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.EntityReference;
import org.squashtest.tm.domain.EntityType;
import org.squashtest.tm.domain.Workspace;
import org.squashtest.tm.domain.chart.AxisColumn;
import org.squashtest.tm.domain.chart.ChartDefinition;
import org.squashtest.tm.domain.chart.Filter;
import org.squashtest.tm.domain.chart.MeasureColumn;
import org.squashtest.tm.domain.chart.ScopeType;
import org.squashtest.tm.domain.customreport.CustomReportDashboard;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.query.NaturalJoinStyle;
import org.squashtest.tm.domain.query.Operation;
import org.squashtest.tm.domain.query.QueryAggregationColumn;
import org.squashtest.tm.domain.query.QueryColumnPrototype;
import org.squashtest.tm.domain.query.QueryFilterColumn;
import org.squashtest.tm.domain.query.QueryModel;
import org.squashtest.tm.domain.query.QueryOrderingColumn;
import org.squashtest.tm.domain.query.QueryProjectionColumn;
import org.squashtest.tm.domain.query.QueryStrategy;
import org.squashtest.tm.service.concurrent.EntityLockManager;
import org.squashtest.tm.service.internal.repository.ColumnPrototypeDao;
import org.squashtest.tm.service.internal.repository.CustomReportDashboardDao;
import org.squashtest.tm.service.milestone.ActiveMilestoneHolder;
import org.squashtest.tm.service.project.ProjectFinder;
import org.squashtest.tm.service.query.ConfiguredQuery;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.toList;

/**
 * Turns a {@link org.squashtest.tm.domain.chart.ChartDefinition} into a {@link QueryModel}.
 * According to Chart's semantics :
 * <ul>
 *     <li>Axis become part of the Projection, Aggregation and Ordering clause</li>
 *     <li>Measures become part of the Projection, and appear after the axes</li>
 *     <li>Filters are filters</li>
 * </ul>
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class ChartToConfiguredQueryConverter {

	// --------- Spring-configurable properties ------------ //
	@Inject
	private ProjectFinder projectFinder;

	@Inject
	private ActiveMilestoneHolder milestoneHolder;

	@Inject
	private ColumnPrototypeDao columnPrototypeDao;

	@Inject
	private CustomReportDashboardDao dashboardDao;

	// --------- Per-instance configurable properties ----- //

	private ChartDefinition definition;

	private Long dashboardId;

	private List<EntityReference> customScope;

	// if the milestone is set, the workspace should be set too
	private Long milestoneId;

	private Workspace workspace;


	// --------- Builder-style configuration methods ------- //

	ChartToConfiguredQueryConverter withDefinition(ChartDefinition definition){
		this.definition = definition;
		return this;
	}

	ChartToConfiguredQueryConverter disableMilestones(){
		this.milestoneId = null;
		return this;
	}

	ChartToConfiguredQueryConverter forMilestone(Long milestoneId){
		this.milestoneId = milestoneId;
		return this;
	}

	ChartToConfiguredQueryConverter forWorkspace(Workspace workspace){
		this.workspace = workspace;
		return this;
	}

	ChartToConfiguredQueryConverter forCurrentActiveMilestone(){
		Optional<Milestone> maybeMilestone = milestoneHolder.getActiveMilestone();
		if (maybeMilestone.isPresent()){
			this.milestoneId = maybeMilestone.get().getId();
		}
		else{
			this.milestoneId = null;
		}
		return this;
	}

	ChartToConfiguredQueryConverter forDefaultChartScope(){
		// it just means that the scope defined in the ChartDefinition will be used
		this.customScope = null;
		return this;
	}

	ChartToConfiguredQueryConverter forCustomScope(List<EntityReference> customScope){
		this.customScope = customScope;
		return this;
	}

	ChartToConfiguredQueryConverter scopedForAllProjects(){
		List<Project> projects = projectFinder.findAllReadable();

		List<EntityReference> entityReferences = new ArrayList<>();
		for (Project project : projects) {
			entityReferences.add(new EntityReference(EntityType.PROJECT,project.getId()));
		}

		this.customScope = entityReferences;

		return this;
	}


	ConfiguredQuery convert(){
		// the base object
		QueryModel queryModel = createBaseQueryModel();

		// the milestones if any
		if (shouldFilterByMilestones()){
			QueryFilterColumn milestoneFilter = createMilestoneFilter();
			queryModel.getFilterColumns().add(milestoneFilter);
		}

		// the scope
		List<EntityReference> scope = resolveScope();

		// build the final object
		ConfiguredQuery newQuery = new ConfiguredQuery();
		newQuery.setQueryModel(queryModel);
		newQuery.setScope(scope);

		return newQuery;
	}



	// ***************** Base Query builder methods *******************

	private QueryModel createBaseQueryModel(){

		/*
			Generate a new instance. The QueryStrategy and the NaturalJoinStyle are left to their default,
			ie MAIN and INNER_JOIN respectively.
		 */
		QueryModel query = new QueryModel();
		query.setStrategy(QueryStrategy.MAIN);
		query.setJoinStyle(NaturalJoinStyle.INNER_JOIN);

		// gather the projections and add them to the query
		// don't forget the reverse-side of the relation
		List<QueryProjectionColumn> projections = extractProjections(definition);
		query.setProjectionColumns(projections);
		projections.forEach(projection -> projection.setQueryModel(query));

		// gather the aggregations now
		List<QueryAggregationColumn> aggregations = extractAggregations(definition, projections);
		query.setAggregationColumns(aggregations);

		// the filters
		List<QueryFilterColumn> filters = extractFilters(definition);
		query.setFilterColumns(filters);

		// the ordering
		List<QueryOrderingColumn> ordering = extractOrdering(definition, projections);
		query.setOrderingColumns(ordering);

		return query;
	}


	/**
	 * Extract projections from the chart definition. Projections will enqueue axis first, then the measures,
	 * ranks will be preserved with respect for each category.
	 *
	 * @param definition
	 * @return
	 */
	private List<QueryProjectionColumn> extractProjections(ChartDefinition definition){

		List<QueryProjectionColumn> projections = new ArrayList<>();

		List<AxisColumn> axes = definition.getAxis();
		axes.stream()
			.map(this::toProjectionColumn)
			.forEachOrdered(projections::add);

		List<MeasureColumn> measures = definition.getMeasures();
		measures.stream()
			.map(this::toProjectionColumn)
			.forEachOrdered(projections::add);

		return projections;
	}


	private List<QueryAggregationColumn> extractAggregations(ChartDefinition definition, List<QueryProjectionColumn> projections){

		List<AxisColumn> axes = definition.getAxis();
		// per construction, the X first elements of the projection are the axes, where X is the number of axes.
		List<QueryProjectionColumn> projectedAxes = projections.subList(0,axes.size());

		List<QueryAggregationColumn> aggregationColumns = projectedAxes.stream().map(this::toAggregationColumn).collect(toList());

		return aggregationColumns;

	}

	private List<QueryFilterColumn> extractFilters(ChartDefinition definition){

		List<Filter> chartFilters = definition.getFilters();

		List<QueryFilterColumn> queryFilters = chartFilters.stream().map(this::toQueryFilterColumn).collect(toList());

		return queryFilters;

	}

	private List<QueryOrderingColumn> extractOrdering(ChartDefinition definition, List<QueryProjectionColumn> projections){

		List<AxisColumn> axes = definition.getAxis();
		// per construction, the X first elements of the projection are the axes, where X is the number of axes.
		List<QueryProjectionColumn> projectedAxes = projections.subList(0,axes.size());

		List<QueryOrderingColumn> orderingColumns = projectedAxes.stream().map(this::toOrderingColumn).collect(toList());

		return orderingColumns;
	}


	private QueryProjectionColumn toProjectionColumn(AxisColumn axis){
		QueryProjectionColumn projection = new QueryProjectionColumn();

		projection.setLabel(axis.getLabel());
		projection.setOperation(axis.getOperation());
		projection.setColumnPrototype(axis.getColumn());
		projection.setCufId(axis.getCufId());

		return projection;
	}


	private QueryProjectionColumn toProjectionColumn(MeasureColumn measure){
		QueryProjectionColumn projection = new QueryProjectionColumn();

		projection.setLabel(measure.getLabel());
		projection.setColumnPrototype(measure.getColumn());
		projection.setOperation(measure.getOperation());
		projection.setCufId(measure.getCufId());

		return projection;
	}


	private QueryAggregationColumn toAggregationColumn(QueryProjectionColumn projection){
		QueryAggregationColumn aggregation = new QueryAggregationColumn();
		aggregation.setProjectionColumn(projection);
		return aggregation;
	}


	private QueryFilterColumn toQueryFilterColumn(Filter chartFilter){
		QueryFilterColumn queryFilter = new QueryFilterColumn();

		queryFilter.setColumnPrototype(chartFilter.getColumn());
		queryFilter.setOperation(chartFilter.getOperation());
		queryFilter.setCufId(chartFilter.getCufId());

		return queryFilter;
	}

	private QueryOrderingColumn toOrderingColumn(QueryProjectionColumn projection){
		QueryOrderingColumn ordering = new QueryOrderingColumn();

		ordering.setOrder(Order.ASC);
		ordering.setQueryProjectionColumn(projection);

		return ordering;

	}


	// ********************* Milestone handling methods ********************

	private boolean shouldFilterByMilestones(){
		return (milestoneId != null && workspace != null && Workspace.isWorkspaceMilestoneFilterable(workspace));
	}

	private QueryFilterColumn createMilestoneFilter(){
		QueryFilterColumn filter = new QueryFilterColumn();

		QueryColumnPrototype columnPrototype = null;
		switch (this.workspace){
			case TEST_CASE:
				columnPrototype = columnPrototypeDao.findByLabel("TEST_CASE_MILESTONE_ID");
				break;
			case REQUIREMENT:
				columnPrototype = columnPrototypeDao.findByLabel("REQUIREMENT_VERSION_MILESTONE_ID");
				break;
			case CAMPAIGN:
				columnPrototype = columnPrototypeDao.findByLabel("CAMPAIGN_MILESTONE_ID");
				break;
			default:
				break;
		}
		filter.setColumnPrototype(columnPrototype);
		filter.setOperation(Operation.EQUALS);
		filter.getValues().add(this.milestoneId.toString());

		return filter;

	}


	// ********************* Scope creation methods ************************

	// ------ predicates ----------
	private boolean hasDynamicScope(){
		return customScope!=null && ! customScope.isEmpty();
	}

	private boolean useChartProjectAsScope(){
		return (definition.getScopeType() == ScopeType.DEFAULT && dashboardId == null);
	}

	private boolean useDashboardProjectAsScope(){
		return (definition.getScopeType() == ScopeType.DEFAULT && dashboardId != null);
	}

	private List<EntityReference> resolveScope(){

		List<EntityReference> finalScope = new ArrayList<>();

		// if a dynamic scope is set, let's use it
		if (hasDynamicScope()){
			finalScope = customScope;
		}
		// else, maybe the scope is the dashboard project
		else if (useDashboardProjectAsScope()){
			CustomReportDashboard dashboard = dashboardDao.getOne(dashboardId);
			EntityReference projectReference = new EntityReference(EntityType.PROJECT, dashboard.getProject().getId());
			finalScope.add(projectReference);
		}
		// else, maybe the scope is the chart project
		else if (useChartProjectAsScope()){
			EntityReference projectReference = new EntityReference(EntityType.PROJECT, definition.getProject().getId());
			finalScope.add(projectReference);
		}
		// else, use the scope defined in the chart definition
		else{
			finalScope = definition.getScope();
		}

		return finalScope;
	}


}
