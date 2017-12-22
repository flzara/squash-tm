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
package org.squashtest.tm.domain.chart;

/**
 * Created by jthebault on 19/09/2016.
 */
public enum ScopeType {
	// @formatter:off
	DEFAULT,//The perimeter will be the current project of the chart if user look just the chart or the dashboard's project id the chart is looked into a dashboard
	PROJECTS,//The perimeter will be a fix selection of project
	CUSTOM;//the perimeter is a custom selection of entities.
	// All joins on other entities will be performed on all database.
	//So the final perimeter is selected entities + all entities linked to them
	// @formatter:on
}
