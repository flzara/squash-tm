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
package org.squashtest.tm.web.internal.model.json;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.squashtest.tm.domain.EntityReference;
import org.squashtest.tm.domain.chart.AxisColumn;
import org.squashtest.tm.domain.chart.Filter;
import org.squashtest.tm.domain.chart.MeasureColumn;
import org.squashtest.tm.domain.customreport.CustomReportChartBinding;
import org.squashtest.tm.domain.customreport.CustomReportLibrary;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.query.ColumnRole;
import org.squashtest.tm.domain.query.SpecializedEntityType;

import java.util.List;
import java.util.Map;
import java.util.Set;

@JsonAutoDetect
public abstract class ChartDefinitionMixin {


	@JsonDeserialize(contentAs = EntityReference.class)
	private List<EntityReference> scope;

	@JsonIgnore
	private Set<CustomReportChartBinding> chartBindings;

	@JsonIgnore
	private Project project;

	@JsonIgnore
	public abstract CustomReportLibrary getCustomReportLibrary();

	@JsonIgnore
	public abstract Map<ColumnRole, Set<SpecializedEntityType>> getInvolvedEntities();

	@JsonDeserialize(contentAs = Filter.class)
	private List<Filter> filters;

	@JsonDeserialize(contentAs = AxisColumn.class)
	private List<AxisColumn> axis;

	@JsonDeserialize(contentAs = MeasureColumn.class)
	private List<MeasureColumn> measures;


}
