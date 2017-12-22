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
package org.squashtest.tm.service.customreport;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.Workspace;
import org.squashtest.tm.domain.chart.ChartDefinition;
import org.squashtest.tm.domain.customreport.CustomReportChartBinding;
import org.squashtest.tm.domain.customreport.CustomReportDashboard;
import org.squashtest.tm.domain.customreport.CustomReportLibraryNode;

@Transactional
public interface CustomReportDashboardService {

	/**
	 * Update all bindings position given in argument.
	 * @param bindings
	 */
	void updateGridPosition(List<CustomReportChartBinding> bindings);

	CustomReportDashboard findById(Long id);

	/**
	 * Bind a chart to a dashboard
	 * @param newBinding
	 */
	void bindChart(CustomReportChartBinding newBinding);

	/**
	 * remove designed binding from database
	 * @param newBinding
	 */
	void unbindChart(Long id);

	/**
	 * Change the chartbinded by the {@link CustomReportChartBinding} designed by bindingId.
	 * WARNING: the chartNodeId param is the {@link CustomReportLibraryNode} id, not the {@link ChartDefinition} id.
	 * This is because the user manipulate the tree node not the entity directly
	 * @param bindingId
	 * @param chartNodeId
	 * @return
	 */
	CustomReportChartBinding changeBindedChart(long bindingId, long chartNodeId);

	/**
	 * Set the dashboard as favorite.
	 * @param nodeId This is the {@link CustomReportLibraryNode} id not the {@link CustomReportDashboard} id
	 * */
	void chooseFavoriteDashboardForCurrentUser(Workspace workspace, long nodeId);

	/**
	 * Check if user prefs said that we should show a dashboard on home page
     */
	boolean shouldShowFavoriteDashboardInWorkspace(Workspace workspace);

	/**
	 * Check if dashboard id in user prefs is valid
	 */
	boolean canShowDashboardInWorkspace(Workspace workspace);

}
