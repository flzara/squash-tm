package org.squashtest.tm.service.internal.query
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
import org.squashtest.tm.domain.EntityType
import org.squashtest.tm.domain.query.ColumnType
import org.squashtest.tm.domain.query.QueryAggregationColumn
import org.squashtest.tm.domain.query.QueryColumnPrototype
import org.squashtest.tm.domain.query.QueryFilterColumn
import org.squashtest.tm.domain.query.QueryModel
import org.squashtest.tm.domain.query.QueryProjectionColumn
import org.squashtest.tm.domain.query.QueryStrategy;
import org.squashtest.tm.domain.query.SpecializedEntityType
import org.squashtest.tm.service.query.ConfiguredQuery;

import static org.squashtest.tm.domain.query.ColumnType.*;
import static org.squashtest.tm.domain.query.QueryStrategy.*;
import org.squashtest.tm.domain.chart.Filter;
import org.squashtest.tm.domain.chart.MeasureColumn;

import spock.lang.Specification;

class InternalQueryModelTest extends Specification{

	def "should detect the subqueries for what they are"(){

		given :
		QueryModel parent = new QueryModel(
			projectionColumns : [
					mockColumn(ATTRIBUTE, MAIN, "projection", "meas attribute"),
					mockColumn(CALCULATED, SUBQUERY, "projection", "measure calculated subquery"),
					mockColumn(CUF, INLINED, "projection", "meas cuf"),
					mockColumn(CALCULATED, INLINED, "projection", "measure calculated inlined")
				],
			aggregationColumns : [
					mockColumn(ATTRIBUTE, MAIN, "aggregation", "axis attribute"),
					mockColumn(CALCULATED, SUBQUERY, "aggregation", "axis calculated subquery"),
					mockColumn(CUF, INLINED, "aggregation", "axis cuf"),
					mockColumn(CALCULATED, INLINED, "aggregation", "axis calculated inlined")
				],
			filterColumns: [
					mockColumn(ATTRIBUTE, MAIN, "filter", "filter attribute"),
					mockColumn(CALCULATED, SUBQUERY, "filter", "filter calculated subquery"),
					mockColumn(CUF, INLINED, "filter", "filter cuf"),
					mockColumn(CALCULATED, INLINED, "filter", "filter calculated inlined")
				]
				)

		and :
		InternalQueryModel detailed = new InternalQueryModel(new ConfiguredQuery(parent))

		when :
		def inlined = detailed.getInlinedColumns();
		def subcolumns = detailed.getSubqueryColumns();

		then :
		inlined.collect{it.column.label} as Set == ["measure calculated inlined", "filter calculated inlined", "axis calculated inlined"] as Set
		subcolumns.collect{it.column.label} as Set == ["measure calculated subquery", "filter calculated subquery", "axis calculated subquery"] as Set
	}




	// ************ more test code ************************


	def mockColumn(ColumnType type, QueryStrategy strategy, String columnrole, String label){
		def col;
		QueryColumnPrototype proto = Mock(QueryColumnPrototype);
		QueryModel query = Mock(QueryModel);

		switch (columnrole){
			case "projection" :
					col = Mock(QueryProjectionColumn); break;
			case "aggregation" :
					col = Mock(QueryAggregationColumn); break;
			case "filter" :
					col = Mock(QueryFilterColumn); break;
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
