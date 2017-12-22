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
import org.squashtest.tm.domain.chart.*;
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
public class MilestoneAwareChartQuery implements IChartQuery{

	@Inject
	private ColumnPrototypeDao columnPrototypeDao;

	private ChartQuery proxiedQuery;
	private Long milestoneId;
	private Workspace workspace;
	private List<Filter> filters =  new ArrayList<>();

	public MilestoneAwareChartQuery(ChartQuery proxiedQuery, Long milestoneId, Workspace workspace) {
		this.proxiedQuery = proxiedQuery;
		this.milestoneId = milestoneId;
		this.workspace = workspace;
	}

	@Override
	public List<Filter> getFilters() {
		Filter additionalFilter = getAdditionalFilter();
		filters.addAll(proxiedQuery.getFilters());
		filters.add(additionalFilter);
		return filters;
	}

	@Override
	public List<AxisColumn> getAxis() {
		return proxiedQuery.getAxis();
	}

	@Override
	public List<MeasureColumn> getMeasures() {
		return proxiedQuery.getMeasures();
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

	private Filter getAdditionalFilter(){
		Filter filter = new Filter();
		ColumnPrototype columnPrototype = null;
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
		}
		filter.setColumn(columnPrototype);
		filter.setOperation(Operation.EQUALS);
		filter.getValues().add(this.milestoneId.toString());
		return filter;
	}
}
