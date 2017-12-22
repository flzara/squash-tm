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
package org.squashtest.tm.service.chart;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.EntityReference;
import org.squashtest.tm.domain.EntityType;
import org.squashtest.tm.domain.Workspace;
import org.squashtest.tm.domain.chart.ChartDefinition;
import org.squashtest.tm.domain.chart.ChartInstance;
import org.squashtest.tm.domain.chart.ColumnPrototype;

@Transactional
public interface ChartModificationService {

	void persist(ChartDefinition newChartDefinition);

	ChartDefinition findById(long id);

	boolean hasChart(List<Long> userIds);

	/**
	 * Returns all the ColumnPrototypes known in the database, indexed by EntityType.
	 * For 1.13 we filter the CUF in a where clause in the request.
	 * @return
	 */
	Map<EntityType, Set<ColumnPrototype>> getColumnPrototypes();


	/**
	 * Will update the chart definition in the persistence layer. The detached ChartDefinition argument must have a persisted
	 * counterpart (ie a non null ID that reference something in the database).
	 *
	 * @param chartDef a detached instance of a ChartDefinition
	 */
	void update(ChartDefinition chartDef);

	/**
	 * Generate a chart with the given definition for the given projectId.
	 * Use only for transient {@link ChartDefinition} which are not linked to project and aren't no persisted...
	 * @param definition
	 * @param projectId
	 * @return
	 */
	ChartInstance generateChart(ChartDefinition definition, Long projectId);

	/**
	 * Instanciate a ChartDefinition given its id.
	 *
	 * @param chartDefinitionId
	 * @return
	 */
	ChartInstance generateChart(long chartDefinitionId, List<EntityReference> dynamicScope, Long dashboardId);

	ChartInstance generateChart(ChartDefinition definition, List<EntityReference> dynamicScope, Long dashboardId);

	ChartInstance generateChart(ChartDefinition definition, List<EntityReference> dynamicScope, Long dashboardId, Long milestoneId, Workspace workspace);



	void updateDefinition(ChartDefinition definition, ChartDefinition oldDef);

	ChartInstance generateChartForMilestoneDashboard(ChartDefinition chart, Long milestoneId, Workspace workspace);

	ChartInstance generateChartInMilestoneMode(ChartDefinition chart, List<EntityReference> scope, Workspace workspace);
}
