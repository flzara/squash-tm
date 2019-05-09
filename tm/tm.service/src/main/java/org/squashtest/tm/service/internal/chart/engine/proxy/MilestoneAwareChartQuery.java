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
package org.squashtest.tm.service.internal.chart.engine.proxy;

import org.springframework.beans.factory.annotation.Configurable;
import org.squashtest.tm.domain.Workspace;
import org.squashtest.tm.domain.chart.AxisColumn;
import org.squashtest.tm.domain.chart.Filter;
import org.squashtest.tm.domain.chart.IChartQuery;
import org.squashtest.tm.domain.chart.MeasureColumn;
import org.squashtest.tm.domain.query.ColumnRole;
import org.squashtest.tm.domain.query.IQueryModel;
import org.squashtest.tm.domain.query.NaturalJoinStyle;
import org.squashtest.tm.domain.query.Operation;
import org.squashtest.tm.domain.query.QueryAggregationColumn;
import org.squashtest.tm.domain.query.QueryColumnPrototype;
import org.squashtest.tm.domain.query.QueryFilterColumn;
import org.squashtest.tm.domain.query.QueryModel;
import org.squashtest.tm.domain.query.QueryOrderingColumn;
import org.squashtest.tm.domain.query.QueryProjectionColumn;
import org.squashtest.tm.domain.query.QueryStrategy;
import org.squashtest.tm.domain.query.SpecializedEntityType;
import org.squashtest.tm.service.internal.repository.ColumnPrototypeDao;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by jthebault on 29/11/2016.
 */
@Configurable
public class MilestoneAwareChartQuery implements IQueryModel {

	@Inject
	private ColumnPrototypeDao columnPrototypeDao;

	private QueryModel proxiedQuery;
	private Long milestoneId;
	private Workspace workspace;
	private List<QueryFilterColumn> filters =  new ArrayList<>();

	public MilestoneAwareChartQuery(QueryModel proxiedQuery, Long milestoneId, Workspace workspace) {
		this.proxiedQuery = proxiedQuery;
		this.milestoneId = milestoneId;
		this.workspace = workspace;
	}

	@Override
	public QueryStrategy getStrategy() {
		return proxiedQuery.getStrategy();
	}

	@Override
	public NaturalJoinStyle getJoinStyle() {
		return proxiedQuery.getJoinStyle();
	}

	@Override
	public Map<ColumnRole, Set<SpecializedEntityType>> getInvolvedEntities() {
		return proxiedQuery.getInvolvedEntities();
	}

	private QueryFilterColumn getAdditionalFilter(){
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

	@Override
	public List<QueryAggregationColumn> getAggregationColumns() {
		return proxiedQuery.getAggregationColumns();
	}

	@Override
	public List<QueryFilterColumn> getFilterColumns() {
		QueryFilterColumn additionalFilter = getAdditionalFilter();
		filters.addAll(proxiedQuery.getFilterColumns());
		filters.add(additionalFilter);
		return filters;
	}

	@Override
	public List<QueryOrderingColumn> getOrderingColumns() {
		return proxiedQuery.getOrderingColumns();
	}

	@Override
	public List<QueryProjectionColumn> getProjectionColumns() {
		return proxiedQuery.getProjectionColumns();
	}
}
