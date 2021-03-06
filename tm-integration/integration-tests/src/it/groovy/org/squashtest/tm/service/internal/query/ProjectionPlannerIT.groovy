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

import com.querydsl.core.types.Order
import org.hibernate.type.LongType
import org.spockframework.util.NotThreadSafe
import org.springframework.transaction.annotation.Transactional
import org.squashtest.it.basespecs.DbunitDaoSpecification
import org.squashtest.tm.domain.execution.QExecution
import org.squashtest.tm.domain.jpql.ExtendedHibernateQuery
import org.squashtest.tm.domain.requirement.QRequirementVersion
import org.squashtest.tm.domain.testcase.QRequirementVersionCoverage
import org.squashtest.tm.domain.testcase.QTestCase
import org.squashtest.tm.domain.testcase.TestCaseImportance
import org.unitils.dbunit.annotation.DataSet
import spock.lang.Unroll
import spock.unitils.UnitilsSupport

import javax.persistence.Query

import static org.squashtest.tm.domain.EntityType.EXECUTION
import static org.squashtest.tm.domain.EntityType.ITERATION
import static org.squashtest.tm.domain.EntityType.REQUIREMENT
import static org.squashtest.tm.domain.EntityType.REQUIREMENT_VERSION
import static org.squashtest.tm.domain.EntityType.TEST_CASE
import static org.squashtest.tm.domain.query.ColumnType.ATTRIBUTE
import static org.squashtest.tm.domain.query.DataType.DATE
import static org.squashtest.tm.domain.query.DataType.LEVEL_ENUM
import static org.squashtest.tm.domain.query.DataType.NUMERIC
import static org.squashtest.tm.domain.query.Operation.BY_MONTH
import static org.squashtest.tm.domain.query.Operation.BY_YEAR
import static org.squashtest.tm.domain.query.Operation.COUNT
import static org.squashtest.tm.domain.query.Operation.NONE
import static org.squashtest.tm.service.internal.query.QueryEngineTestUtils.cov
import static org.squashtest.tm.service.internal.query.QueryEngineTestUtils.createInternalModel
import static org.squashtest.tm.service.internal.query.QueryEngineTestUtils.exec
import static org.squashtest.tm.service.internal.query.QueryEngineTestUtils.ite
import static org.squashtest.tm.service.internal.query.QueryEngineTestUtils.itp
import static org.squashtest.tm.service.internal.query.QueryEngineTestUtils.mkAggr
import static org.squashtest.tm.service.internal.query.QueryEngineTestUtils.mkOrder
import static org.squashtest.tm.service.internal.query.QueryEngineTestUtils.mkProj
import static org.squashtest.tm.service.internal.query.QueryEngineTestUtils.r
import static org.squashtest.tm.service.internal.query.QueryEngineTestUtils.tc
import static org.squashtest.tm.service.internal.query.QueryEngineTestUtils.v

@NotThreadSafe
@UnitilsSupport
@Transactional
class ProjectionPlannerIT extends DbunitDaoSpecification{

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
	def "should generate simple select and group by clauses"(){

		given : "basequery"

		QTestCase testCase = QTestCase.testCase
		QRequirementVersionCoverage cov = QRequirementVersionCoverage.requirementVersionCoverage
		QRequirementVersion versions = QRequirementVersion.requirementVersion

		ExtendedHibernateQuery query = new ExtendedHibernateQuery().from(testCase)
				.join(testCase.requirementVersionCoverages, cov)
				.join(cov.verifiedRequirementVersion, versions)


		and : "definition"

		InternalQueryModel definition = createInternalModel(
				mkProj(ATTRIBUTE, NUMERIC, NONE, TEST_CASE, "id"),
				mkProj(ATTRIBUTE, NUMERIC, COUNT, REQUIREMENT_VERSION, "id"),

				mkAggr(ATTRIBUTE, NUMERIC, NONE, TEST_CASE, "id"),

				mkOrder(ATTRIBUTE, NUMERIC, NONE, TEST_CASE, "id", Order.ASC)
		)

		when :
		ProjectionPlanner planner = new ProjectionPlanner(definition, query)
		planner.modifyQuery()

		ExtendedHibernateQuery concrete = query.clone(getSession())
		def res = concrete.fetch()

		then :
		def formatedRes = res.collect{it.a } as Set
		formatedRes == [ [-1l, 3] , [-2l, 2]] as Set

	}

	@DataSet("QueryPlanner.dataset.xml")
	def "should count executions by yearmonth"(){

		given : "query"
		ExtendedHibernateQuery query = new ExtendedHibernateQuery()
		QExecution exec = QExecution.execution

		query.from(exec)

		and : "definition"

		InternalQueryModel definition = createInternalModel(
			mkProj(ATTRIBUTE, DATE, BY_MONTH, EXECUTION, "lastExecutedOn"),
			mkProj(ATTRIBUTE, NUMERIC, COUNT, EXECUTION, "id"),

			mkAggr(ATTRIBUTE, DATE, BY_MONTH, EXECUTION, "lastExecutedOn"),

			mkOrder(ATTRIBUTE, DATE, BY_MONTH, EXECUTION, "lastExecutedOn", Order.ASC)
		)

		when :
		ProjectionPlanner planner = new ProjectionPlanner(definition, query)
		planner.modifyQuery()

		ExtendedHibernateQuery concrete = query.clone(getSession())
		def res = concrete.fetch()

		then :
		def formatedRes = res.collect{ it.a } as Set
		formatedRes == [ [201510, 3] , [201511, 2]] as Set


	}

	@DataSet("QueryPlanner.dataset.xml")
	def "should sort test cases by importance assuming level_num sort rules"(){

		given : "query"
		ExtendedHibernateQuery query = new ExtendedHibernateQuery()
		QTestCase testCase = QTestCase.testCase

		query.from(testCase)

		and : "definition"

		InternalQueryModel definition = createInternalModel(
			mkProj(ATTRIBUTE, NUMERIC, NONE, TEST_CASE, "id"),
			mkProj(ATTRIBUTE, LEVEL_ENUM, NONE, TEST_CASE, "importance"),

			mkOrder(ATTRIBUTE, LEVEL_ENUM, NONE, TEST_CASE, "importance", Order.ASC)
		)

		when :
		ProjectionPlanner planner = new ProjectionPlanner(definition, query)
		planner.modifyQuery()

		ExtendedHibernateQuery concrete = query.clone(getSession())
		def res = concrete.fetch()

		then :
		def sortedRes = res.collect{ it.a}
		sortedRes == [
						[-2, TestCaseImportance.VERY_HIGH, 1],
					  	[-3, TestCaseImportance.HIGH, 2],
					  	[-1, TestCaseImportance.LOW, 4]
					]

	}

	@Unroll
	@DataSet("QueryPlanner.dataset.xml")
	def "should perform many queries"(){

		expect :
		def q = conf.query
		def definition = conf.definition
		def expected = conf.expected

		ProjectionPlanner planner = new ProjectionPlanner(definition, q)
		planner.modifyQuery()

		ExtendedHibernateQuery query = q.clone(getSession())

		def res = query.fetch()
		def refined = res.collect{ it.a }

		refined as Set == expected as Set

		where :

		conf << [
			configureManyQuery(1),
			configureManyQuery(2),
			configureManyQuery(3)
		]

	}


	def ManyQueryPojo configureManyQuery(dsNum){

		def query
		def definition
		def expected

		switch (dsNum){
			case 1 : // case 1 -> select count(tc) and count(rv) by requirement id

				query = from(tc)
				.join(tc.requirementVersionCoverages, cov)
				.join(cov.verifiedRequirementVersion, v)
				.join(v.requirement, r);

				definition = createInternalModel(
					mkProj(ATTRIBUTE, NUMERIC, NONE, REQUIREMENT, "id"),
					mkProj(ATTRIBUTE, NUMERIC, COUNT, TEST_CASE, "id"),
					mkProj(ATTRIBUTE, NUMERIC, COUNT, REQUIREMENT_VERSION, "id"),

					mkAggr(ATTRIBUTE, NUMERIC, NONE, REQUIREMENT, "id"),

					mkOrder(ATTRIBUTE, NUMERIC, NONE, REQUIREMENT, "id", Order.ASC),
				)

				expected = [[-1l, 2, 2],  [-2l, 1, 1], [-3l, 2, 2]]
				break;

			case 2 : // case 2 -> select count(exec) group by it id and referenced tc id
				query = from(exec).join(exec.testPlan, itp).join(itp.iteration, ite)

				definition = createInternalModel(
					mkProj(ATTRIBUTE, NUMERIC, NONE, ITERATION, "id"),
					mkProj(ATTRIBUTE, NUMERIC, NONE, EXECUTION, "referencedTestCase.id"),
					mkProj(ATTRIBUTE, NUMERIC, COUNT, EXECUTION, "id"),

					mkAggr(ATTRIBUTE, NUMERIC, NONE, ITERATION, "id"),
					mkAggr(ATTRIBUTE, NUMERIC, NONE, EXECUTION, "referencedTestCase.id"),

					mkOrder(ATTRIBUTE, NUMERIC, NONE, ITERATION, "id", Order.ASC),
					mkOrder(ATTRIBUTE, NUMERIC, NONE, EXECUTION, "referencedTestCase.id", Order.ASC)
				)

				expected = [[-11l, -1l, 3], [-12l, -1l, 1], [-12l, null, 1]]
				break;

			case 3 : // case 3 -> select count(req) group by year of creation

				query = from(r)

				definition = createInternalModel(
					mkProj(ATTRIBUTE, DATE, BY_YEAR, REQUIREMENT, "audit.createdOn"),
					mkProj(ATTRIBUTE, NUMERIC, COUNT, REQUIREMENT, "id"),

					mkAggr(ATTRIBUTE, DATE, BY_YEAR, REQUIREMENT, "audit.createdOn"),

					mkOrder(ATTRIBUTE, DATE, BY_YEAR, REQUIREMENT, "audit.createdOn", Order.ASC),
				)

				expected = [[2015, 2], [2016, 1]]

		}

		return new ManyQueryPojo(query : query, definition : definition, expected : expected)


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
