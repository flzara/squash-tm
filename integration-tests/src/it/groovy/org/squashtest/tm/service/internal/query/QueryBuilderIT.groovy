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
package org.squashtest.tm.service.internal.query

import org.hibernate.type.LongType
import org.spockframework.util.NotThreadSafe
import org.squashtest.it.basespecs.DbunitDaoSpecification
import org.squashtest.tm.domain.EntityType
import org.squashtest.tm.domain.jpql.ExtendedHibernateQuery
import org.squashtest.tm.domain.query.Operation
import org.squashtest.tm.domain.query.QueryAggregationColumn
import org.squashtest.tm.domain.query.QueryColumnPrototype
import org.squashtest.tm.domain.query.QueryFilterColumn
import org.squashtest.tm.domain.query.QueryModel
import org.squashtest.tm.domain.query.QueryOrderingColumn
import org.squashtest.tm.domain.query.QueryProjectionColumn
import org.squashtest.tm.domain.testcase.TestCaseImportance
import org.squashtest.tm.service.query.ConfiguredQuery
import org.unitils.dbunit.annotation.DataSet
import spock.unitils.UnitilsSupport


import static org.squashtest.tm.domain.query.ColumnType.*
import static org.squashtest.tm.domain.query.DataType.*
import static org.squashtest.tm.domain.query.Operation.*
import static org.squashtest.tm.service.internal.query.QueryEngineTestUtils.*
import javax.persistence.Query

@NotThreadSafe
@UnitilsSupport
class QueryBuilderIT extends DbunitDaoSpecification {

	// fix the requirementVersion - requirement relation
	def setup(){
		def session = getSession()

		[ "-1" : [-11l, -12l, -13l], "-2" : [-21l], "-3" : [-31l, -32l]].each {
			reqid, versids ->
			Query qu = session.createSQLQuery("update REQUIREMENT_VERSION set requirement_id = :reqid where res_id in (:vids)")
			qu.setParameter("reqid", Long.valueOf(reqid), LongType.INSTANCE)
			qu.setParameterList("vids", versids, LongType.INSTANCE)
			qu.executeUpdate()

		}
	}


	@DataSet("QueryPlanner.dataset.xml")
	def "should use a calculated column in a select clause"(){

		// goal : select requiremend.id, s_count(requirement.versions) from Requirement
		// in a convoluted way of course.

		given :
		def projProto = findByName("REQUIREMENT_NB_VERSIONS")

		and :
		def aggproj = mkProj(ATTRIBUTE, NUMERIC, NONE, EntityType.REQUIREMENT, "id")
		def projection = new QueryProjectionColumn(columnPrototype : projProto, operation : Operation.NONE)
		def aggregation = mkAggr(ATTRIBUTE, NUMERIC, NONE, EntityType.REQUIREMENT, "id")
		def ordering = mkOrder(ATTRIBUTE, NUMERIC, NONE, EntityType.REQUIREMENT, "id")

		QueryModel queryModel = new QueryModel(
			projectionColumns : [aggproj, projection],
			aggregationColumns : [aggregation],
			orderingColumns: [ordering]
		)

		when :


		def _q = new QueryBuilder(new InternalQueryModel(new ConfiguredQuery(queryModel))).createQuery()
		def query = _q.clone(getSession())
		def res = query.fetch();


		then :
		res.collect{it.a}  == [ [-3l,2] , [-2l,1], [-1l,3] ]


	}


	@DataSet("QueryPlanner.dataset.xml")
	def "should use a calculated column in a where clause - via subquery having  "(){

		// goal : select requirement.id group by requirement having s_count(requirement.versions) > 1
		// but not as concisely

		given :
		def filterProto = findByName("REQUIREMENT_NB_VERSIONS")

		and :
		def projection = mkProj(ATTRIBUTE, NUMERIC, NONE, EntityType.REQUIREMENT, "id")

		def filter = new QueryFilterColumn(column: filterProto, operation : Operation.GREATER, values : ["1"])

		def aggregation = mkAggr(ATTRIBUTE, NUMERIC, NONE, EntityType.REQUIREMENT, "id")

		def ordering= mkOrder(ATTRIBUTE, NUMERIC, NONE, EntityType.REQUIREMENT, "id")

		QueryModel queryModel = new QueryModel(
			projectionColumns : [ projection],
			filterColumns : [filter],
			aggregationColumns : [aggregation],
			orderingColumns: [ordering]
		)

		when :

		def query = new QueryBuilder(new InternalQueryModel(new ConfiguredQuery(queryModel))).createQuery().clone(getSession())
		def res = query.fetch();


		then :
		res.collect{it.a} == [ [-3L], [-1L]  ]
		getTestableQuery(query) ==
				"""select distinct requirement.id as col_0_0_
from Requirement requirement
where exists (select 1
from Requirement requirement_sub
  inner join requirement_sub.versions as requirementVersion_sub
where requirement = requirement_sub
group by requirement_sub.id
having s_count(requirementVersion_sub.id) > ?1)
group by col_0_0_
order by col_0_0_ asc"""

	}

	@DataSet("QueryPlanner.dataset.xml")
	// this one demonstrates the "join().on()" in subqueries
	def "should select how many times a test case appears in an iteration"(){
		given :
		def projProto = findByName('TEST_CASE_ITERCOUNT');
		def aggProto = findByName("TEST_CASE_ID");

		and :
		def aggrproj = new QueryProjectionColumn(columnPrototype :  aggProto, operation : Operation.NONE)
		def projection = new QueryProjectionColumn(columnPrototype :  projProto, operation : Operation.SUM)
		def aggregation = new QueryAggregationColumn(columnPrototype :  aggProto, operation : Operation.NONE)
		def ordering = new QueryOrderingColumn(columnPrototype :  aggProto, operation : Operation.NONE)

		QueryModel queryModel = new QueryModel(
			projectionColumns : 	[aggrproj, projection],
			aggregationColumns : 	[aggregation],
			orderingColumns: 		[ordering]
		)


		when :
		def query = new QueryBuilder(new InternalQueryModel(new ConfiguredQuery(queryModel))).createQuery().clone(getSession())
		def res = query.fetch()

		then :
		res.collect {it.a}  == [[-3l, 0], [-2l, 1], [-1l, 2]]
		getTestableQuery(query) ==
				"""select distinct testCase.id as col_0_0_, s_sum((select distinct s_count(iteration_sub.id) as col_0_0_sub
from TestCase testCase_sub
  left join IterationTestPlanItem iterationTestPlanItem_sub with iterationTestPlanItem_sub.referencedTestCase = testCase_sub
  left join iterationTestPlanItem_sub.iteration as iteration_sub
where testCase = testCase_sub)) as col_1_0_
from TestCase testCase
group by col_0_0_
order by col_0_0_ asc"""

	}



	// ***************** Tests on attribute 'class' ***********************************

	@DataSet("QueryPlanner.dataset.xml")
	def "should count the call steps per test case "(){
		given :
		def projProto = findByName("TEST_CASE_CALLSTEPCOUNT")
		def aggrProto = findByName("TEST_CASE_ID")

		and :
		def aggrproj = new QueryProjectionColumn(columnPrototype : aggrProto, operation : Operation.NONE)
		def projection = new QueryProjectionColumn(columnPrototype :  projProto, operation : Operation.SUM)
		def aggregation = new QueryAggregationColumn(columnPrototype : aggrProto, operation : Operation.NONE)
		def ordering = new QueryOrderingColumn(columnPrototype : aggrProto, operation : Operation.NONE)

		QueryModel queryModel = new QueryModel(
			projectionColumns : [aggrproj, projection],
			aggregationColumns : [aggregation],
			orderingColumns: [ordering]
		)

		when :
		def query = new QueryBuilder(new InternalQueryModel(new ConfiguredQuery(queryModel))).createQuery()

		def clone = query.clone(getSession())
		def res = clone.fetch()

		then :
		res.collect {it.a} == [[-3l, 0], [-2l,1], [-1l,0] ]
		getTestableQuery(query) ==
				"""select distinct testCase.id as col_0_0_, s_sum((select distinct s_count(testStep_sub.id) as col_0_0_sub
from TestCase testCase_sub
  left join testCase_sub.steps as testStep_sub
where type(testStep_sub) in (?1) and testCase = testCase_sub)) as col_1_0_
from TestCase testCase
group by col_0_0_
order by col_0_0_ asc"""
	}


	@DataSet("QueryPlanner.dataset.xml")
	def "should count how many test case per requirements have at least 1 call step"(){
		given :
		def projProto = findByName("TEST_CASE_ID")
		def filterProto = findByName("TEST_CASE_CALLSTEPCOUNT")
		def aggProto = findByName("REQUIREMENT_ID")

		and :
		def aggrproj = new QueryProjectionColumn(columnPrototype : aggProto, operation : Operation.NONE)
		def projection = new QueryProjectionColumn(columnPrototype :  projProto, operation : Operation.COUNT)

		def filter = new QueryFilterColumn(column: filterProto, operation : GREATER, values : ["0"])

		def aggregation = new QueryAggregationColumn(columnPrototype : aggProto, operation : Operation.NONE)

		def ordering = new QueryOrderingColumn(columnPrototype : aggProto, operation : Operation.NONE)

		QueryModel queryModel = new QueryModel(
				projectionColumns : [aggrproj, projection],
				filterColumns : [filter],
				aggregationColumns : [aggregation],
				orderingColumns: [ordering]
		)

		when :
		def query = new QueryBuilder(new InternalQueryModel(new ConfiguredQuery(queryModel))).createQuery()
		def clone = query.clone(getSession())
		def res = clone.fetch()


		then :
		// the requirement -2l isn't verified by tc2 and thus
		// is filtered out because of inner join
		res.collect {it.a}  == [[-3l,1], [-1l, 1]]
		getTestableQuery(query) ==

			/*
                2019/05/16, following the refactoring in [TM-282] :

                the '(select 1 ' was created as '(select ' : the ' 1' was missing. Why it would work in earlier version
                is beyond me, but I added the missing ' 1' now the correct behavior.
             */
				"""select distinct requirement.id as col_0_0_, s_count(testCase.id) as col_1_0_
from Requirement requirement
  inner join requirement.versions as requirementVersion
  inner join requirementVersion.requirementVersionCoverages as requirementVersionCoverage
  inner join requirementVersionCoverage.verifyingTestCase as testCase
where exists (select 1
from TestCase testCase_sub
  left join testCase_sub.steps as testStep_sub
where type(testStep_sub) in (?1) and testCase = testCase_sub
group by testCase_sub.id
having s_count(testStep_sub.id) > ?2)
group by col_0_0_
order by col_0_0_ asc"""
	}

	@DataSet("QueryPlanner.should filter test cases by kind.xml")
	def "should filter on which test cases is a standard or a keyword test case"() {
		given :
			def projProto = findByName("TEST_CASE_ID")
			def filterProto = findByName("TEST_CASE_KIND")
			def orderProto = findByName("TEST_CASE_ID")
		and :
			def projection = new QueryProjectionColumn(columnPrototype :  projProto, operation : Operation.COUNT)
			def filter = new QueryFilterColumn(
				column: filterProto,
				operation : Operation.IN,
				values : ["org.squashtest.tm.domain.testcase.TestCase", "org.squashtest.tm.domain.testcase.KeywordTestCase"])
			def ordering = new QueryOrderingColumn(columnPrototype : orderProto, operation : Operation.NONE)

		QueryModel queryModel = new QueryModel(
			projectionColumns : [projection],
			filterColumns : [filter],
			orderingColumns: [ordering]
		)

		when :
		def query = new QueryBuilder(new InternalQueryModel(new ConfiguredQuery(queryModel))).createQuery()

		def clone = query.clone(getSession())
		def res = clone.fetch()

		then :
		res.collect {it.a} == [[3]]
		getTestableQuery(query) ==
			"""select distinct s_count(testCase.id) as col_0_0_
from TestCase testCase
where type(testCase) in (?1, ?2)
order by col_0_0_ asc"""
	}


	// ******** Tests on EXISTENCE datatype and usage of is null/not null*******************

	@DataSet("QueryPlanner.dataset.xml")
	def "should filter on which test cases that have automated scripts"(){

		given :
		def projProto = findByName("TEST_CASE_ID")
		def filterProto = findByName("TEST_CASE_HASAUTOSCRIPT")
		def orderProto = findByName("TEST_CASE_ID")

		and :
		def projection = new QueryProjectionColumn(columnPrototype :  projProto, operation : Operation.COUNT)

		def filter = new QueryFilterColumn(column: filterProto, operation : Operation.EQUALS, values : ["TRUE"])

		def ordering = new QueryOrderingColumn(columnPrototype : orderProto, operation : Operation.NONE)

		QueryModel queryModel = new QueryModel(
			projectionColumns : [projection],
			filterColumns : [filter],
			orderingColumns: [ordering]
		)

		when :
		def query = new QueryBuilder(new InternalQueryModel(new ConfiguredQuery(queryModel))).createQuery()

		def clone = query.clone(getSession())
		def res = clone.fetch()

		then :
		res.collect {it.a} == [[1]]
		getTestableQuery(query) ==
				"""select distinct s_count(testCase.id) as col_0_0_
from TestCase testCase
  left join testCase.automatedTest as automatedTest_subcolumn_sub
where case when automatedTest_subcolumn_sub.id is not null then true else false end  = ?1
order by col_0_0_ asc"""
	}


	@DataSet("QueryPlanner.dataset.xml")
	def "should filter on which test cases that don't have automated scripts"(){

		given :
		def projProto = findByName("TEST_CASE_ID")
		def filterProto = findByName("TEST_CASE_HASAUTOSCRIPT")
		def orderProto = findByName("TEST_CASE_ID")

		and :
		def projection = new QueryProjectionColumn(columnPrototype :  projProto, operation : Operation.COUNT)

		def filter = new QueryFilterColumn(column: filterProto, operation : Operation.EQUALS, values : ["FALSE"])

		def ordering = new QueryOrderingColumn(columnPrototype : orderProto, operation : Operation.NONE)

		QueryModel queryModel = new QueryModel(
			projectionColumns : [projection],
			filterColumns : [filter],
			orderingColumns: [ordering]
		)

		when :
		def query = new QueryBuilder(new InternalQueryModel(new ConfiguredQuery(queryModel))).createQuery()

		def clone = query.clone(getSession())
		def res = clone.fetch()

		then :
		res.collect {it.a}  == [[2]]
		getTestableQuery(query) ==
				"""select distinct s_count(testCase.id) as col_0_0_
from TestCase testCase
  left join testCase.automatedTest as automatedTest_subcolumn_sub
where case when automatedTest_subcolumn_sub.id is not null then true else false end  = ?1
order by col_0_0_ asc"""
	}





	@DataSet("QueryPlanner.dataset.xml")
	def "should count test cases grouped by whether automated or not"(){

		given :
		def projProto = findByName("TEST_CASE_ID")
		def aggrProto = findByName("TEST_CASE_HASAUTOSCRIPT")

		and :
		def aggrproj = new QueryProjectionColumn(columnPrototype : aggrProto, operation : Operation.NONE)
		def projection = new QueryProjectionColumn(columnPrototype :  projProto, operation : Operation.COUNT)

		def aggregation = new QueryAggregationColumn(columnPrototype : aggrProto, operation : Operation.NONE)

		def ordering = new QueryOrderingColumn(columnPrototype : aggrProto, operation : Operation.NONE)

		QueryModel queryModel = new QueryModel(
			projectionColumns : [aggrproj, projection],
			aggregationColumns : [aggregation],
			orderingColumns: [ordering]
		)

		when :
		def query = new QueryBuilder(new InternalQueryModel(new ConfiguredQuery(queryModel))).createQuery()

		def clone = query.clone(getSession())
		def res = clone.fetch()

		then :
		res.collect {it.a} as Set == [[true, 1], [false, 2]] as Set

		// note : the \n\ bullshit is because eclipse formatter would drop an important
		// whitespace at the end of the line, hence the trick
		getTestableQuery(query) ==

			"""select distinct case when automatedTest_subcolumn_sub.id is not null then true else false end  as col_0_0_, s_count(testCase.id) as col_1_0_
from TestCase testCase
  left join testCase.automatedTest as automatedTest_subcolumn_sub
group by col_0_0_
order by col_0_0_ asc"""

	}


	@DataSet("QueryPlanner.dataset.xml")
	def "should count how many test plans have been executed for each iteration"(){
		given :
		def projProto= findByName('ITEM_TEST_PLAN_ID')
		def filterProto = findByName("ITEM_TEST_PLAN_IS_EXECUTED")
		def aggrProto = findByName('ITERATION_ID')

		and :
		def aggrproj = new QueryProjectionColumn(columnPrototype : aggrProto, operation : Operation.NONE)
		def projection = new QueryProjectionColumn(columnPrototype :  projProto, operation : Operation.COUNT)

		def filter = new QueryFilterColumn(column: filterProto, operation : Operation.EQUALS, values : ["TRUE"])

		def aggregation = new QueryAggregationColumn(columnPrototype : aggrProto, operation : Operation.NONE)

		def ordering = new QueryOrderingColumn(columnPrototype : aggrProto, operation : Operation.NONE)

		QueryModel queryModel = new QueryModel(
			projectionColumns : [aggrproj, projection],
			filterColumns : [filter],
			aggregationColumns : [aggregation],
			orderingColumns: [ordering]
		)

		when :
		def query = new QueryBuilder(new InternalQueryModel(new ConfiguredQuery(queryModel))).createQuery()
		def clone = query.clone(getSession())
		def res = clone.fetch()

		then :
		res.collect {it.a} == [[-12l, 2], [-11l,1]]


		/*
            2019/05/16, following the refactoring in [TM-282] :

            The where clause in the subquery, which accepts two condition AND'ed together, seems now to put
            the conditions in random order. For that reason I've put the two versions of the expected query.

    		Sorry, it's ugly.
         */
		def actualQuery = getTestableQuery(query)
		(actualQuery ==
				"""select distinct iteration.id as col_0_0_, s_count(iterationTestPlanItem.id) as col_1_0_
from Iteration iteration
  inner join iteration.testPlans as iterationTestPlanItem
where exists (select 1
from IterationTestPlanItem iterationTestPlanItem_sub
  left join iterationTestPlanItem_sub.executions as execution_sub
where iterationTestPlanItem = iterationTestPlanItem_sub and case when execution_sub.id is not null then true else false end  = ?1
group by iterationTestPlanItem_sub.id)
group by col_0_0_
order by col_0_0_ asc""" ||

			actualQuery ==
			"""select distinct iteration.id as col_0_0_, s_count(iterationTestPlanItem.id) as col_1_0_
from Iteration iteration
  inner join iteration.testPlans as iterationTestPlanItem
where exists (select 1
from IterationTestPlanItem iterationTestPlanItem_sub
  left join iterationTestPlanItem_sub.executions as execution_sub
where case when execution_sub.id is not null then true else false end  = ?1 and iterationTestPlanItem = iterationTestPlanItem_sub
group by iterationTestPlanItem_sub.id)
group by col_0_0_
order by col_0_0_ asc"""
		)
	}




	@DataSet("QueryPlanner.dataset.xml")
	def "should count how many test plans have not been executed for each iteration"(){
		given :
		def projProto= findByName('ITEM_TEST_PLAN_ID')
		def filterProto = findByName("ITEM_TEST_PLAN_IS_EXECUTED")
		def aggrProto = findByName('ITERATION_ID')

		and :

		def aggrproj = new QueryProjectionColumn(columnPrototype : aggrProto, operation : Operation.NONE)
		def projection = new QueryProjectionColumn(columnPrototype :  projProto, operation : Operation.COUNT)

		def filter = new QueryFilterColumn(column: filterProto, operation : Operation.EQUALS, values : ["FALSE"])

		def aggregation = new QueryAggregationColumn(columnPrototype : aggrProto, operation : Operation.NONE)

		def ordering = new QueryOrderingColumn(columnPrototype : aggrProto, operation : Operation.NONE)

		QueryModel queryModel = new QueryModel(
			projectionColumns : [aggrproj, projection],
			filterColumns : [filter],
			aggregationColumns : [aggregation],
			orderingColumns: [ordering]
		)

		when :
		def query = new QueryBuilder(new InternalQueryModel(new ConfiguredQuery(queryModel))).createQuery()
		def clone = query.clone(getSession())
		def res = clone.fetch()


		then :
		// alas, the other iteration has no not-executed items so
		// it is filtered out because of of inner join mechanics
		res.collect {it.a} == [[-11l, 1]]

		/*
			2019/05/16, following the refactoring in [TM-282] :

			The where clause in the subquery, which accepts two condition AND'ed together, seems now to put
			the conditions in random order. For that reason I've put the two versions of the expected query.

			Sorry, it's ugly.
		 */
		def actualQuery = getTestableQuery(query)
		(actualQuery ==
				"""select distinct iteration.id as col_0_0_, s_count(iterationTestPlanItem.id) as col_1_0_
from Iteration iteration
  inner join iteration.testPlans as iterationTestPlanItem
where exists (select 1
from IterationTestPlanItem iterationTestPlanItem_sub
  left join iterationTestPlanItem_sub.executions as execution_sub
where case when execution_sub.id is not null then true else false end  = ?1 and iterationTestPlanItem = iterationTestPlanItem_sub
group by iterationTestPlanItem_sub.id)
group by col_0_0_
order by col_0_0_ asc"""	||

		actualQuery ==
			"""select distinct iteration.id as col_0_0_, s_count(iterationTestPlanItem.id) as col_1_0_
from Iteration iteration
  inner join iteration.testPlans as iterationTestPlanItem
where exists (select 1
from IterationTestPlanItem iterationTestPlanItem_sub
  left join iterationTestPlanItem_sub.executions as execution_sub
where iterationTestPlanItem = iterationTestPlanItem_sub and case when execution_sub.id is not null then true else false end  = ?1
group by iterationTestPlanItem_sub.id)
group by col_0_0_
order by col_0_0_ asc"""

		)
	}




	@DataSet("QueryPlanner.dataset.xml")
	def "should count how many automated executions by item"(){
		given :
		def projProto= findByName('ITEM_TEST_PLAN_AUTOEXCOUNT')
		def aggrProto = findByName('ITEM_TEST_PLAN_ID')

		and :
		def aggrproj = new QueryProjectionColumn(columnPrototype : aggrProto, operation : Operation.NONE)
		def projection = new QueryProjectionColumn(columnPrototype :  projProto, operation : Operation.SUM)
		def aggregation = new QueryAggregationColumn(columnPrototype : aggrProto, operation : Operation.NONE)
		def ordering = new QueryOrderingColumn(columnPrototype : aggrProto, operation : Operation.NONE)

		QueryModel queryModel = new QueryModel(
				projectionColumns : [aggrproj, projection],
				aggregationColumns : [aggregation],
				orderingColumns: [ordering]
				)

		when :
		def query = new QueryBuilder(new InternalQueryModel(new ConfiguredQuery(queryModel))).createQuery()
		def clone = query.clone(getSession())
		def res = clone.fetch()


		then :
		res.collect {it.a}  == [[-122l, 1], [-121l, 0], [-112l,0], [-111l, 0]]
		getTestableQuery(query) ==

				"""select distinct iterationTestPlanItem.id as col_0_0_, s_sum((select distinct s_count(execution_sub.id) as col_0_0_sub
from IterationTestPlanItem iterationTestPlanItem_sub
  left join iterationTestPlanItem_sub.executions as execution_sub
  left join execution_sub.automatedExecutionExtender as automatedExecutionExtender_sub
where automatedExecutionExtender_sub.id is not null and iterationTestPlanItem = iterationTestPlanItem_sub)) as col_1_0_
from IterationTestPlanItem iterationTestPlanItem
group by col_0_0_
order by col_0_0_ asc"""

	}




	@DataSet("QueryPlanner.dataset.xml")
	def "should count how many manual executions by item"(){
		given :
		def projProto= findByName('ITEM_TEST_PLAN_MANEXCOUNT')
		def aggrProto = findByName('ITEM_TEST_PLAN_ID')

		and :
		def aggrproj = new QueryProjectionColumn(columnPrototype : aggrProto, operation : Operation.NONE)
		def projection = new QueryProjectionColumn(columnPrototype :  projProto, operation : Operation.SUM)

		def aggregation = new QueryAggregationColumn(columnPrototype : aggrProto, operation : Operation.NONE)

		def ordering = new QueryOrderingColumn(columnPrototype : aggrProto, operation : Operation.NONE)



		QueryModel queryModel = new QueryModel(
				projectionColumns : [aggrproj, projection],
				aggregationColumns : [aggregation],
				orderingColumns: [ordering]
				)

		when :
		def query = new QueryBuilder(new InternalQueryModel(new ConfiguredQuery(queryModel))).createQuery()
		def clone = query.clone(getSession())
		def res = clone.fetch()


		then :
		res.collect {it.a}  == [[-122l, 0], [-121l, 1], [-112l,0], [-111l, 3]]
		getTestableQuery(query) ==
				"""select distinct iterationTestPlanItem.id as col_0_0_, s_sum((select distinct s_count(execution_sub.id) as col_0_0_sub
from IterationTestPlanItem iterationTestPlanItem_sub
  left join iterationTestPlanItem_sub.executions as execution_sub
  left join execution_sub.automatedExecutionExtender as automatedExecutionExtender_sub
where automatedExecutionExtender_sub.id is null and iterationTestPlanItem = iterationTestPlanItem_sub)) as col_1_0_
from IterationTestPlanItem iterationTestPlanItem
group by col_0_0_
order by col_0_0_ asc"""

	}




	@DataSet("QueryPlanner.dataset.xml")
	def "should count by iteration how many items have at least 1 manual execution"(){
		given :
		def projProto = findByName('ITEM_TEST_PLAN_ID')
		def filterProto= findByName('ITEM_TEST_PLAN_MANEXCOUNT')
		def aggrProto = findByName('ITERATION_ID')

		and :
		def aggrproj = new QueryProjectionColumn(columnPrototype : aggrProto, operation : Operation.NONE)
		def projection = new QueryProjectionColumn(columnPrototype :  projProto, operation : Operation.COUNT)

		def filter = new QueryFilterColumn(column: filterProto, operation : Operation.GREATER, values :["0"])

		def aggregation = new QueryAggregationColumn(columnPrototype : aggrProto, operation : Operation.NONE)

		def ordering = new QueryOrderingColumn(columnPrototype : aggrProto, operation : Operation.NONE)

		QueryModel queryModel = new QueryModel(
			projectionColumns : [aggrproj, projection],
			filterColumns : [filter],
			aggregationColumns : [aggregation],
			orderingColumns: [ordering]
		)

		when :
		def query = new QueryBuilder(new InternalQueryModel(new ConfiguredQuery(queryModel))).createQuery()
		def clone = query.clone(getSession())
		def res = clone.fetch()


		then :
		res.collect {it.a} as Set == [[-12l, 1], [-11l, 1]] as Set
		getTestableQuery(query) ==
				"""select distinct iteration.id as col_0_0_, s_count(iterationTestPlanItem.id) as col_1_0_
from Iteration iteration
  inner join iteration.testPlans as iterationTestPlanItem
where exists (select 1
from IterationTestPlanItem iterationTestPlanItem_sub
  left join iterationTestPlanItem_sub.executions as execution_sub
  left join execution_sub.automatedExecutionExtender as automatedExecutionExtender_sub
where automatedExecutionExtender_sub.id is null and iterationTestPlanItem = iterationTestPlanItem_sub
group by iterationTestPlanItem_sub.id
having s_count(execution_sub.id) > ?1)
group by col_0_0_
order by col_0_0_ asc"""

	}



	@DataSet("QueryPlanner.dataset.xml")
	def "should count by iteration how many items have at least 1 manual execution and where execution label match description"(){
		given :
		def projProto = findByName('ITEM_TEST_PLAN_ID')
		def filterProto= findByName('ITEM_TEST_PLAN_MANEXCOUNT')
		def filter2Proto= findByName('EXECUTION_LABEL')
		def aggrProto = findByName('ITERATION_ID')

		and :
		def aggrproj = new QueryProjectionColumn(columnPrototype : aggrProto, operation : Operation.NONE)
		def projection = new QueryProjectionColumn(columnPrototype :  projProto, operation : Operation.COUNT)

		def filter = new QueryFilterColumn(column: filterProto, operation : Operation.GREATER, values :["0"])
		def filter2 = new QueryFilterColumn(column: filter2Proto, operation : Operation.LIKE, values :["cp 1 it1%"])

		def aggregation = new QueryAggregationColumn(columnPrototype : aggrProto, operation : Operation.NONE)

		def ordering = new QueryOrderingColumn(columnPrototype : aggrProto, operation : Operation.NONE)


		QueryModel queryModel = new QueryModel(
			projectionColumns : [aggrproj, projection],
			filterColumns : [filter, filter2],
			aggregationColumns : [aggregation],
			orderingColumns: [ordering]
		)

		when :
		def query = new QueryBuilder(new InternalQueryModel(new ConfiguredQuery(queryModel))).createQuery()
		def clone = query.clone(getSession())

		def res = clone.fetch()


		then :
		res.collect {it.a} as Set == [[-11l, 1]] as Set


	}


	@DataSet("QueryPlanner.dataset.xml")
	def "should return the test case ids ordered by importance"(){
		given :
		def projProtoId = findByName('TEST_CASE_ID')
		def projProtoImportance = findByName('TEST_CASE_IMPORTANCE')
		def orderProto = findByName('TEST_CASE_IMPORTANCE')

		and :
		def projectionId = new QueryProjectionColumn(columnPrototype :  projProtoId, operation : Operation.NONE)
		def projectionImportance = new QueryProjectionColumn(columnPrototype :  projProtoImportance, operation : Operation.NONE)

		def ordering = new QueryOrderingColumn(columnPrototype : orderProto, operation : Operation.NONE)


		QueryModel queryModel = new QueryModel(
			projectionColumns : [projectionId, projectionImportance],
			orderingColumns: [ordering]
		)

		when :
		def query = new QueryBuilder(new InternalQueryModel(new ConfiguredQuery(queryModel))).createQuery()
		def clone = query.clone(getSession())

		def res = clone.fetch()


		then :
		/*
		 * Note : the tuples are actually of length 3 : this method actually tests the sorting on level enums, which
		 * requires to include a sorting key based in the projection clause
		 */
		res.collect {it.a} as Set ==
			[[-2L, TestCaseImportance.VERY_HIGH, 1],
			 [-3L, TestCaseImportance.HIGH, 2],
			 [-1L, TestCaseImportance.LOW, 4],
			] as Set

		def actualQuery = getTestableQuery(query)
		actualQuery == """select distinct testCase.id as col_0_0_, testCase.importance as col_1_0_, (case when (testCase.importance = ?1) then ?2 when (testCase.importance = ?3) then ?4 when (testCase.importance = ?5) then ?6 when (testCase.importance = ?7) then ?8 else -1000 end) as col_2_0_
from TestCase testCase
order by col_2_0_ asc"""

	}

	// ********* utilities ***************************

	def getTestableQuery(query){
		// matches all numbers preceeded by an underscore '_' but not followed by another '_'
		query.toString().replaceAll(/_\d+(?!_)/, "_sub")
	}


	QueryColumnPrototype findByName(name){
		getSession().createQuery("from QueryColumnPrototype where label = '${name}'").uniqueResult();
	}

	def ExtendedHibernateQuery from(clz){
		return new ExtendedHibernateQuery().from(clz)
	}


	class ManyQueryPojo {
		ExtendedHibernateQuery query
		InternalQueryModel definition
		Set<?> expected
	}

}
