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
package org.squashtest.tm.service.internal.chart.engine

import org.squashtest.tm.domain.EntityType;
import org.squashtest.tm.domain.chart.AxisColumn;
import org.squashtest.tm.domain.chart.ChartQuery;
import org.squashtest.tm.domain.chart.SpecializedEntityType;
import org.squashtest.tm.domain.chart.QueryStrategy;
import org.squashtest.tm.domain.chart.ColumnPrototype;
import org.squashtest.tm.domain.chart.ColumnType;
import static org.squashtest.tm.domain.chart.ColumnType.*;
import static org.squashtest.tm.domain.chart.QueryStrategy.*;
import org.squashtest.tm.domain.chart.Filter;
import org.squashtest.tm.domain.chart.MeasureColumn;

import spock.lang.Specification;

class DetailedChartQueryTest extends Specification{

	def "should detect the subqueries for what they are"(){

		given :
		ChartQuery parent = new ChartQuery(
				measures : [
					mockColumn(ATTRIBUTE, MAIN, "measure", "meas attribute"),
					mockColumn(CALCULATED, SUBQUERY, "measure", "measure calculated subquery"),
					mockColumn(CUF, INLINED, "measure", "meas cuf"),
					mockColumn(CALCULATED, INLINED, "measure", "measure calculated inlined")
				],
				axis : [
					mockColumn(ATTRIBUTE, MAIN, "axis", "axis attribute"),
					mockColumn(CALCULATED, SUBQUERY, "axis", "axis calculated subquery"),
					mockColumn(CUF, INLINED, "axis", "axis cuf"),
					mockColumn(CALCULATED, INLINED, "axis", "axis calculated inlined")
				],
				filters: [
					mockColumn(ATTRIBUTE, MAIN, "filter", "filter attribute"),
					mockColumn(CALCULATED, SUBQUERY, "filter", "filter calculated subquery"),
					mockColumn(CUF, INLINED, "filter", "filter cuf"),
					mockColumn(CALCULATED, INLINED, "filter", "filter calculated inlined")
				]
				)

		and :
		DetailedChartQuery detailed = new DetailedChartQuery(parent)

		when :
		def inlined = detailed.getInlinedColumns();
		def subcolumns = detailed.getSubqueryColumns();

		then :
		inlined.collect{it.column.label} as Set == ["measure calculated inlined", "filter calculated inlined", "axis calculated inlined"] as Set
		subcolumns.collect{it.column.label} as Set == ["measure calculated subquery", "filter calculated subquery", "axis calculated subquery"] as Set
	}

	def mockColumn(ColumnType type, QueryStrategy strategy, String columnrole, String label){
		def col;
		ColumnPrototype proto = Mock(ColumnPrototype);
		ChartQuery query = Mock(ChartQuery);

		switch (columnrole){
			case "measure" :
					col = Mock(MeasureColumn); break;
			case "axis" :
					col = Mock(AxisColumn); break;
			case "filter" :
					col = Mock(Filter); break;
		}

		col.getSpecializedType() >> new SpecializedEntityType(entityType : EntityType.TEST_CASE)
		col.getColumn() >> proto
		proto.getColumnType() >> type
		proto.getLabel() >> label
		proto.getSubQuery() >> query
		query.getStrategy() >> strategy

		return col
	}

}
