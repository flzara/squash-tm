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

import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.chart.AxisColumn;
import org.squashtest.tm.domain.chart.Filter;
import org.squashtest.tm.domain.chart.MeasureColumn;
import org.squashtest.tm.domain.jpql.ExtendedHibernateQuery
import org.squashtest.tm.domain.jpql.ExtendedHibernateQueryFactory
import org.squashtest.tm.domain.query.NaturalJoinStyle
import org.squashtest.tm.domain.query.QueryAggregationColumn
import org.squashtest.tm.domain.query.QueryFilterColumn
import org.squashtest.tm.domain.query.QueryModel
import org.squashtest.tm.domain.query.QueryProjectionColumn
import org.squashtest.tm.domain.query.QueryStrategy
import org.squashtest.tm.domain.query.SpecializedEntityType;
import org.squashtest.tm.domain.query.SpecializedEntityType.EntityRole;
import org.squashtest.tm.domain.testcase.TestCase;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder
import org.squashtest.tm.service.query.ConfiguredQuery;
import spock.lang.Specification
import static org.squashtest.tm.service.internal.query.InternalEntityType.*;
import static org.squashtest.tm.domain.query.DataType.*;
import static org.squashtest.tm.domain.query.ColumnType.*;
import static org.squashtest.tm.domain.query.Operation.*;


import static org.squashtest.tm.service.internal.query.QueryEngineTestUtils.*

class QueryPlannerTest extends Specification {


	def "should add a natural inner join, if the join doesn't exist yet"(){

		given :
		ExtendedHibernateQuery hquery = Mock(ExtendedHibernateQuery)

		InternalQueryModel cquery = mockInternalModel(
			rootEntity: REQUIREMENT,
			targetEntities: [REQUIREMENT, REQUIREMENT_VERSION],
			joinStyle: NaturalJoinStyle.INNER_JOIN
		)

		QuerydslToolbox tools = Mock(QuerydslToolbox)

		def aliases = ['requirement'] as Set

		and :
		QueryPlanner planner = new QueryPlanner(
				aliases : aliases,
				utils : tools,
				query : hquery,
				internalQueryModel : cquery
				)

		when :
		planner.addNaturalJoin(r,v,"versions")

		then :
		1 * tools.makePath(r, v, "versions") >> Mock(PathBuilder)
		1 * hquery.innerJoin(_ as PathBuilder, v)

	}

	def "should add a natural left join, if the join doesn't exist yet"(){

		given :
		ExtendedHibernateQuery hquery = Mock(ExtendedHibernateQuery)

		InternalQueryModel cquery = mockInternalModel(
				rootEntity : REQUIREMENT,
				targetEntities : [REQUIREMENT, REQUIREMENT_VERSION],
				joinStyle : NaturalJoinStyle.LEFT_JOIN
				)

		QuerydslToolbox tools = Mock(QuerydslToolbox)

		def aliases = ['requirement'] as Set

		and :
		QueryPlanner planner = new QueryPlanner(
				aliases : aliases,
				utils : tools,
				query : hquery,
				internalQueryModel : cquery
				)

		when :
		planner.addNaturalJoin(r,v,"versions")

		then :
		1 * tools.makePath(r, v, "versions") >> Mock(PathBuilder)
		1 * hquery.leftJoin(_ as PathBuilder, v)

	}

	def "should not add a natural join if exists already"(){

		given :
		ExtendedHibernateQuery hquery = Mock(ExtendedHibernateQuery)

		InternalQueryModel cquery = mockInternalModel(
				rootEntity : REQUIREMENT,
				targetEntities : [REQUIREMENT, REQUIREMENT_VERSION],
				joinStyle : NaturalJoinStyle.INNER_JOIN
				)

		QuerydslToolbox tools = Mock(QuerydslToolbox)

		def aliases = ['requirement', 'requirementVersion'] as Set

		and :
		QueryPlanner planner = new QueryPlanner(
				aliases : aliases,
				utils : tools,
				query : hquery,
				internalQueryModel : cquery
				)

		when :
		planner.addNaturalJoin(r,v,"versions")

		then :
		0 * tools.makePath(r, v, "versions")
		0 * hquery.innerJoin(_ as PathBuilder, v)
	}


	def "should add a where join, if the join doesn't exist yet"(){

		given :
		ExtendedHibernateQuery hquery = Mock(ExtendedHibernateQuery)

		InternalQueryModel cquery = mockInternalModel(
				rootEntity : TEST_CASE,
				targetEntities : [TEST_CASE, ITEM_TEST_PLAN]
				)

		QuerydslToolbox tools = Mock(QuerydslToolbox)

		def aliases = ['testCase'] as Set

		and :
		QueryPlanner planner = new QueryPlanner(
				aliases : aliases,
				utils : tools,
				query : hquery,
				internalQueryModel : cquery
				)

		when :
		planner.addWhereJoin(tc, itp,"referencedTestCase")

		then :
		1 * tools.makePath(itp, tc, "referencedTestCase") >> new PathBuilder(IterationTestPlanItem.class, "iterationTestPlanItem").get('referencedTestCase', TestCase.class)
		1 * hquery.where(_ as Predicate)

	}


	def "should not add a where join, if the join doesn't exist yet"(){

		given :
		ExtendedHibernateQuery hquery = Mock(ExtendedHibernateQuery)

		InternalQueryModel cquery = mockInternalModel(
				rootEntity : TEST_CASE,
				targetEntities : [TEST_CASE, ITEM_TEST_PLAN]
				)

		QuerydslToolbox tools = Mock(QuerydslToolbox)

		def aliases = ['testCase', 'iterationTestPlanItem'] as Set

		and :
		QueryPlanner planner = new QueryPlanner(
				aliases : aliases,
				utils : tools,
				query : hquery,
				internalQueryModel : cquery
				)

		when :
		planner.addWhereJoin(tc, itp,"referencedTestCase")

		then :
		0 * tools.makePath(itp, tc, "referencedTestCase")
		0 * hquery.where(_ as Predicate)

	}


	def "should create a query from a main chart query"(){

		given :
		InternalQueryModel cquery = mockInternalModel(
				rootEntity : TEST_CASE,
				targetEntities : [TEST_CASE, REQUIREMENT]
				)

		QueryPlanner planner = new QueryPlanner(cquery)

		when :
		ExtendedHibernateQuery res = planner.createQuery()
		res.select(r.id)

		then :
		res.toString() ==
				"""select requirement.id
from TestCase testCase
  inner join testCase.requirementVersionCoverages as requirementVersionCoverage
  inner join requirementVersionCoverage.verifiedRequirementVersion as requirementVersion
  inner join requirementVersion.requirement as requirement"""
	}



	def "should create a query plan that uses 'inner-'where join"(){

		given:
		InternalQueryModel cquery = mockInternalModel(
			rootEntity : TEST_CASE,
			// joining from testcase to iteration item requires a "where" join, because the relation testCase -> item is not mapped
			targetEntities : [TEST_CASE, ITEM_TEST_PLAN, ITERATION],
			// here the join style is 'inner join'
			joinStyle: NaturalJoinStyle.INNER_JOIN
		)

		QueryPlanner planner = new QueryPlanner(cquery)
		when :
		ExtendedHibernateQuery res = planner.createQuery()
		res.select(tc.id)

		then :
		// test that the builder still seeded the graph from the test case
		planner.graphSeed == TEST_CASE

		// the query cannot inner join from testcase to item, so the query plan is instead using cartesian-product + where clause
		res.toString() == """select testCase.id
from TestCase testCase, IterationTestPlanItem iterationTestPlanItem
  inner join iterationTestPlanItem.iteration as iteration
where iterationTestPlanItem.referencedTestCase = testCase"""


	}

	def "should create a (degraded) query plan with an alternate graph seed because the nominal graph would have a left-where join in it"(){

		given:
		InternalQueryModel cquery = mockInternalModel(
			rootEntity : TEST_CASE,
			// joining from testcase to iteration item requires a "where" join, because the relation testCase -> item is not mapped
			targetEntities : [TEST_CASE, ITEM_TEST_PLAN],
			// also here we require a left join
			joinStyle: NaturalJoinStyle.LEFT_JOIN
		)

		QueryPlanner planner = new QueryPlanner(cquery)
		when :
		ExtendedHibernateQuery res = planner.createQuery()
		res.select(tc.id)

		then :
		// test that the builder reversed the test plan, seeding from the item instead of test case
		planner.graphSeed == ITEM_TEST_PLAN

		// the query was reversed (it initially selects from the item instead of the test case)
		// (it is also incorrect, because the left join is in the wrong direction)
		res.toString() == """select testCase.id
from IterationTestPlanItem iterationTestPlanItem
  left join iterationTestPlanItem.referencedTestCase as testCase"""

	}


	def "should append a subquery to a main query"(){

		given :
		InternalQueryModel cquery = mockInternalModel(
				rootEntity : REQUIREMENT_VERSION,
				targetEntities : [REQUIREMENT_VERSION, REQUIREMENT_VERSION_CATEGORY]
				)

		ExtendedHibernateQuery mainq =
				new ExtendedHibernateQuery().from(r)
				.innerJoin(r.versions, v)
				.innerJoin(v.requirementVersionCoverages, cov)
				.innerJoin(cov.verifyingTestCase, tc)
				.select(r.id)

		and :

		QuerydslToolbox tools = new QuerydslToolbox("sub")

		QueryPlanner planner =
				new QueryPlanner(cquery, tools)
				.appendToQuery(mainq)
				.joinRootEntityOn(v)

		when :
		planner.modifyQuery()

		then :
		mainq.toString() ==
				"""select requirement.id
from Requirement requirement
  inner join requirement.versions as requirementVersion
  inner join requirementVersion.requirementVersionCoverages as requirementVersionCoverage
  inner join requirementVersionCoverage.verifyingTestCase as testCase
  inner join requirementVersion.category as reqversionCategory_sub"""
	}

	def "should not append twice a subquery to a main query"(){

		given :
		InternalQueryModel cquery = mockInternalModel(
				rootEntity : REQUIREMENT_VERSION,
				targetEntities : [REQUIREMENT_VERSION, REQUIREMENT_VERSION_CATEGORY]
				)

		ExtendedHibernateQuery mainq =
				new ExtendedHibernateQuery().from(r)
				.innerJoin(r.versions, v)
				.innerJoin(v.requirementVersionCoverages, cov)
				.innerJoin(cov.verifyingTestCase, tc)
				.select(r.id)

		and :

		QuerydslToolbox tools = new QuerydslToolbox("sub")

		QueryPlanner planner =
				new QueryPlanner(cquery, tools)
				.appendToQuery(mainq)
				.joinRootEntityOn(v)

		when :
		planner.modifyQuery()
		planner.modifyQuery()


		then :
		mainq.toString() ==
				"""select requirement.id
from Requirement requirement
  inner join requirement.versions as requirementVersion
  inner join requirementVersion.requirementVersionCoverages as requirementVersionCoverage
  inner join requirementVersionCoverage.verifyingTestCase as testCase
  inner join requirementVersion.category as reqversionCategory_sub"""
	}


	def "should build a main query and also add inlined subqueries"(){

		given : " the first subquery"

		QueryProjectionColumn selectCateg = mkProj(ATTRIBUTE, STRING, NONE, org.squashtest.tm.domain.EntityType.INFO_LIST_ITEM, "label")
		selectCateg.specializedType.entityRole = EntityRole.REQUIREMENT_VERSION_CATEGORY

		QueryAggregationColumn aggReqversion = mkAggr(ATTRIBUTE, NUMERIC, NONE, org.squashtest.tm.domain.EntityType.REQUIREMENT_VERSION, "id")

		QueryModel categQuery = new QueryModel(
				projectionColumns : [selectCateg],
				aggregationColumns : [aggReqversion],
				strategy : QueryStrategy.INLINED,
				joinStyle : NaturalJoinStyle.INNER_JOIN
				)

		and : "the second subquery"

		QueryProjectionColumn selectMiles = mkProj(ATTRIBUTE, STRING, NONE, org.squashtest.tm.domain.EntityType.MILESTONE, "label")
		selectMiles.specializedType.entityRole = EntityRole.TEST_CASE_MILESTONE

		QueryAggregationColumn aggTC = mkAggr(ATTRIBUTE, NUMERIC, NONE, org.squashtest.tm.domain.EntityType.TEST_CASE, "id")

		QueryModel tcmilesQuery = new QueryModel(
				projectionColumns : [selectMiles],
				aggregationColumns : [aggTC],
				strategy : QueryStrategy.INLINED,
				joinStyle : NaturalJoinStyle.LEFT_JOIN
				)

		and : "the main internalQueryModel"

		QueryFilterColumn inlinedCateg = mkFilter(CALCULATED, STRING, NONE, org.squashtest.tm.domain.EntityType.REQUIREMENT_VERSION, "category", ["functional test"])
		inlinedCateg.column.subQuery = categQuery
		inlinedCateg.column.id = 5

		QueryProjectionColumn inlinedTCMiles = mkProj(CALCULATED, INFO_LIST_ITEM, COUNT, org.squashtest.tm.domain.EntityType.TEST_CASE, "milestones")
		inlinedTCMiles.column.subQuery = tcmilesQuery
		inlinedTCMiles.column.id = 7

		QueryAggregationColumn tcid = mkAggr(ATTRIBUTE, NUMERIC, NONE, org.squashtest.tm.domain.EntityType.TEST_CASE, "id")

		QueryModel mainquery = new QueryModel(
				projectionColumns : [ inlinedTCMiles	],
				filterColumns : [inlinedCateg],
				aggregationColumns : [tcid]
				)

		when :
		def internalModel = new InternalQueryModel(new ConfiguredQuery(mainquery))
		QueryPlanner planner = new QueryPlanner(internalModel)
		ExtendedHibernateQuery res = planner.createQuery()
		res.select(tc.id)

		then :
		res.toString() ==
				"""select testCase.id
from TestCase testCase
  inner join testCase.requirementVersionCoverages as requirementVersionCoverage
  inner join requirementVersionCoverage.verifiedRequirementVersion as requirementVersion
  left join testCase.milestones as testCaseMilestone_subcolumn_7
  inner join requirementVersion.category as reqversionCategory_subcolumn_5"""

	}

	def "Should generate correct request for chart def with cuf"(){
		given:
		QueryProjectionColumn selectId = mkProj(ATTRIBUTE, NUMERIC, COUNT, org.squashtest.tm.domain.EntityType.TEST_CASE, "id");
		QueryAggregationColumn cufTextAxis = mkAggr(CUF, STRING, NONE, org.squashtest.tm.domain.EntityType.TEST_CASE, "value")

		cufTextAxis.cufId = 12;

		QueryModel mainquery = new QueryModel(
			projectionColumns : [ selectId	],
			filterColumns : [],
			aggregationColumns : [cufTextAxis]
		)

		when:
		QueryPlanner planner = new QueryPlanner(new InternalQueryModel(new ConfiguredQuery(mainquery)))

		ExtendedHibernateQuery res = planner.createQuery()
		res.select(tc.id)

		then :
		res.toString() == """select testCase.id
from TestCase testCase, CustomFieldValue null_12
where null_12.boundEntityType = ?1 and null_12.boundEntityId = testCase.id and null_12.cufId = ?2""";
	}



	// *************** test utils *************

	def mockInternalModel(argMap){
		// create the mock and configure its behavior according to the arg map
		def model = Mock(InternalQueryModel)

		argMap.each { ppt, value ->
			model."get${ppt.capitalize()}"() >> value
		}

		// now provide default behavior for the rest
		// (if a getter was already mocked, the default behavior below will not apply)
		model.getJoinStyle() >> NaturalJoinStyle.INNER_JOIN
		model.getStrategy() >> QueryStrategy.MAIN
		model.getInlinedColumns() >> []
		model.getFilterColumns() >> []
		model.getAggregationColumns() >> []
		model.getProjectionColumns() >> []
		model.getOrderingColumns() >> []

		return model
	}


}
