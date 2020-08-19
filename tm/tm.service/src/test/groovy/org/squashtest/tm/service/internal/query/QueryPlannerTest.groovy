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

import com.querydsl.core.types.Predicate
import com.querydsl.core.types.dsl.PathBuilder
import org.squashtest.tm.domain.campaign.IterationTestPlanItem
import org.squashtest.tm.domain.jpql.ExtendedHibernateQuery
import org.squashtest.tm.domain.query.NaturalJoinStyle
import org.squashtest.tm.domain.query.QueryAggregationColumn
import org.squashtest.tm.domain.query.QueryFilterColumn
import org.squashtest.tm.domain.query.QueryModel
import org.squashtest.tm.domain.query.QueryProjectionColumn
import org.squashtest.tm.domain.query.QueryStrategy
import org.squashtest.tm.domain.query.SpecializedEntityType.EntityRole
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.service.query.ConfiguredQuery
import spock.lang.Specification
import spock.lang.Unroll

import static org.squashtest.tm.domain.campaign.QIterationTestPlanItem.iterationTestPlanItem
import static org.squashtest.tm.domain.query.ColumnType.ATTRIBUTE
import static org.squashtest.tm.domain.query.ColumnType.CALCULATED
import static org.squashtest.tm.domain.query.ColumnType.CUF
import static org.squashtest.tm.domain.query.DataType.INFO_LIST_ITEM
import static org.squashtest.tm.domain.query.DataType.NUMERIC
import static org.squashtest.tm.domain.query.DataType.STRING
import static org.squashtest.tm.domain.query.Operation.COUNT
import static org.squashtest.tm.domain.query.Operation.NONE
import static org.squashtest.tm.domain.testcase.QTestCase.testCase
import static org.squashtest.tm.service.internal.query.InternalEntityType.CAMPAIGN
import static org.squashtest.tm.service.internal.query.InternalEntityType.ITEM_TEST_PLAN
import static org.squashtest.tm.service.internal.query.InternalEntityType.ITERATION
import static org.squashtest.tm.service.internal.query.InternalEntityType.REQUIREMENT
import static org.squashtest.tm.service.internal.query.InternalEntityType.REQUIREMENT_VERSION
import static org.squashtest.tm.service.internal.query.InternalEntityType.REQUIREMENT_VERSION_CATEGORY
import static org.squashtest.tm.service.internal.query.InternalEntityType.TEST_CASE
import static org.squashtest.tm.service.internal.query.InternalEntityType.TEST_CASE_STEP
import static org.squashtest.tm.service.internal.query.QueryEngineTestUtils.cov
import static org.squashtest.tm.service.internal.query.QueryEngineTestUtils.itp
import static org.squashtest.tm.service.internal.query.QueryEngineTestUtils.mkAggr
import static org.squashtest.tm.service.internal.query.QueryEngineTestUtils.mkFilter
import static org.squashtest.tm.service.internal.query.QueryEngineTestUtils.mkProj
import static org.squashtest.tm.service.internal.query.QueryEngineTestUtils.r
import static org.squashtest.tm.service.internal.query.QueryEngineTestUtils.tc
import static org.squashtest.tm.service.internal.query.QueryEngineTestUtils.v

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
		planner.addMappedJoin(r,v,"versions")

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
		planner.addMappedJoin(r,v,"versions")

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
		planner.addMappedJoin(r,v,"versions")

		then :
		0 * tools.makePath(r, v, "versions")
		0 * hquery.innerJoin(_ as PathBuilder, v)
	}


	def "should add an unmapped inner join, if the join doesn't exist yet"(){

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

		and:
		def joinPath = new PathBuilder(IterationTestPlanItem.class, "iterationTestPlanItem").get('referencedTestCase', TestCase.class)

		when :
		planner.addUnmappedJoin(tc, itp,"referencedTestCase")

		then :
		1 * tools.makePath(itp, tc, "referencedTestCase") >> joinPath
		1 * hquery.innerJoin(iterationTestPlanItem) >> hquery
		1 * hquery.on(iterationTestPlanItem.referencedTestCase.eq(testCase))

	}

	def "should add an unmapped left join, if the join doesn't exist yet"(){

		given :
		ExtendedHibernateQuery hquery = Mock(ExtendedHibernateQuery)

		InternalQueryModel cquery = mockInternalModel(
			rootEntity : TEST_CASE,
			targetEntities : [TEST_CASE, ITEM_TEST_PLAN],
			joinStyle: NaturalJoinStyle.LEFT_JOIN
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

		and:
		def joinPath = new PathBuilder(IterationTestPlanItem.class, "iterationTestPlanItem").get('referencedTestCase', TestCase.class)

		when :
		planner.addUnmappedJoin(tc, itp,"referencedTestCase")

		then :
		1 * tools.makePath(itp, tc, "referencedTestCase") >> joinPath
		1 * hquery.leftJoin(iterationTestPlanItem) >> hquery
		1 * hquery.on(iterationTestPlanItem.referencedTestCase.eq(testCase))

	}


	def "should not add an unmapped join, if the join aleady exists"(){

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
		planner.addUnmappedJoin(tc, itp,"referencedTestCase")

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



	def "should create a query plan that uses inner unmapped join"(){

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

		// the query cannot navigate from testcase to item, so it uses the join().on() form
		res.toString() == """select testCase.id
from TestCase testCase
  inner join IterationTestPlanItem iterationTestPlanItem with iterationTestPlanItem.referencedTestCase = testCase
  inner join iterationTestPlanItem.iteration as iteration"""


	}

	def "should reckon that all required target entities are reachable with the given query plan"(){

		given:
		InternalQueryModel cquery = mockInternalModel(
			targetEntities : [TEST_CASE_STEP, TEST_CASE]
		)

		and :
		QueryPlan plan = Mock(){
			collectKeys() >> [TEST_CASE_STEP, TEST_CASE]
		}

		and:
		QueryPlanner planner = new QueryPlanner(cquery)

		when:
		def res = planner.isEveryEntityReachable(plan)

		then:
		res == true
	}

	@Unroll("should reckon that #neg all required entities are reachable with the given query plan")
	def "should reckon that all required target entities are reachable with the given query plan, or not"(){

		given:
		InternalQueryModel cquery = mockInternalModel(
			targetEntities : targets
		)

		and :
		QueryPlan plan = Mock(){
			collectKeys() >> planned
		}

		and:
		QueryPlanner planner = new QueryPlanner(cquery)

		when:
		def res = planner.isEveryEntityReachable(plan)

		then:
		res == expected

		where:

		targets							|	planned													|	neg		|	expected
		[TEST_CASE, CAMPAIGN]			|	[TEST_CASE, ITEM_TEST_PLAN, ITERATION, CAMPAIGN]		|	""		|	true
		[TEST_CASE_STEP, TEST_CASE]		|	[TEST_CASE_STEP]										|	"not"	|	false
	}



	def "should create a (degraded) query plan with an alternate graph seed because the nominal graph wouldn't include all the target entities"(){

		given:
		InternalQueryModel cquery = mockInternalModel(
			rootEntity : TEST_CASE_STEP,
			// as of now the domain graph is not aware of any way to nagivate from a test step to a test case (unidirectional edge)
			targetEntities : [TEST_CASE_STEP, TEST_CASE],
			// also here we require a left join
			joinStyle: NaturalJoinStyle.LEFT_JOIN
		)

		QueryPlanner planner = new QueryPlanner(cquery)
		when :
		ExtendedHibernateQuery res = planner.createQuery()
		res.select(tc.id)

		then :
		// test that, after the initial attempt with TEST_CASE_STEP as a graph seed failed, the builder tried with TEST_CASE
		planner.graphSeed == TEST_CASE

		// the query was reversed (it initially selects from the item instead of the test case)
		// (it is also incorrect, because the left join is in the wrong direction)
		res.toString() == """select testCase.id
from TestCase testCase
  left join testCase.steps as testStep"""

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
from TestCase testCase, CustomFieldValue TEST_CASE.value_12
where TEST_CASE.value_12.boundEntityType = ?1 and TEST_CASE.value_12.boundEntityId = testCase.id and TEST_CASE.value_12.cufId = ?2""";
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
