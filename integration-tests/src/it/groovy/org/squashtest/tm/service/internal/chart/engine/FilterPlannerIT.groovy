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

import com.querydsl.core.types.Projections
import org.spockframework.util.NotThreadSafe
import org.springframework.transaction.annotation.Transactional
import org.squashtest.it.basespecs.DbunitDaoSpecification
import org.squashtest.tm.domain.jpql.ExtendedHibernateQuery
import org.unitils.dbunit.annotation.DataSet
import spock.unitils.UnitilsSupport

import static org.squashtest.tm.domain.EntityType.TEST_CASE
import static org.squashtest.tm.domain.chart.ColumnType.ATTRIBUTE
import static org.squashtest.tm.domain.chart.DataType.NUMERIC
import static org.squashtest.tm.domain.chart.Operation.EQUALS
import static org.squashtest.tm.service.internal.chart.engine.ChartEngineTestUtils.*

@NotThreadSafe
@UnitilsSupport
@Transactional
class FilterPlannerIT extends DbunitDaoSpecification {



	// TODO : test the AND/OR mechanism

	@DataSet("QueryPlanner.dataset.xml")
	def "should retain the requirement versions only for test case 1"(){

		given : "the query"

		ExtendedHibernateQuery query = new ExtendedHibernateQuery()
		query.from(v).join(v.requirementVersionCoverages, cov)
				.join(cov.verifyingTestCase, tc)
				.select(Projections.tuple(v.id,  v.name.countDistinct() ))
				.groupBy(v.id)

		and : "the definition"
		DetailedChartQuery definition = new DetailedChartQuery(
				filters : [mkFilter(ATTRIBUTE, NUMERIC, EQUALS, TEST_CASE, "id", ["-1"])]
				)

		when :
		FilterPlanner planner = new FilterPlanner(definition, query)
		planner.modifyQuery()
		ExtendedHibernateQuery concrete = query.clone(getSession())

		def res = concrete.fetch()

		then :

		def formatedRes = res.collect{ it.a } as Set
		formatedRes == [ [-12l, 1] , [-21l, 1], [-31l, 1]] as Set

	}



	def ExtendedHibernateQuery from(clz){
		return new ExtendedHibernateQuery().from(clz)
	}




	class ManyQueryPojo {
		ExtendedHibernateQuery query
		DetailedChartQuery definition
		Set<?> expected
	}
}
