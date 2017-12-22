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

import org.hibernate.Query;
import org.hibernate.type.LongType;
import org.spockframework.util.NotThreadSafe;
import org.springframework.transaction.annotation.Transactional
import org.squashtest.it.basespecs.DbunitDaoSpecification;
import org.squashtest.tm.domain.jpql.ExtendedHibernateQuery;
import org.squashtest.tm.domain.requirement.QRequirementVersion;
import org.squashtest.tm.domain.testcase.QTestCase
import org.unitils.dbunit.annotation.DataSet;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.PathBuilder;

import static org.squashtest.tm.service.internal.chart.engine.InternalEntityType.*

import spock.lang.Unroll;
import spock.unitils.UnitilsSupport;


/**
 * This class will test the {@link QueryPlanner}. It role is to create the
 * bulk of the query, which is made only of the required entities joined together.
 *
 * It has no projection, filter nor group by. so
 * each test here will include additional clauses for the purpose of the test.
 *
 *
 *
 * @author bsiri
 *
 */
@NotThreadSafe
@UnitilsSupport
@Transactional
class QueryPlannerIT extends DbunitDaoSpecification {

	// some abreviations

	static InternalEntityType REQ = REQUIREMENT
	static InternalEntityType RV = REQUIREMENT_VERSION
	static InternalEntityType COV = REQUIREMENT_VERSION_COVERAGE
	static InternalEntityType TC = TEST_CASE
	static InternalEntityType ITP = ITEM_TEST_PLAN
	static InternalEntityType IT = ITERATION
	static InternalEntityType CP = CAMPAIGN
	static InternalEntityType EX = EXECUTION
	static InternalEntityType ISS = ISSUE
	static InternalEntityType TATEST = AUTOMATED_TEST

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
	def "should fetch all requirement versions tied to test case 2"(){

		given :

		DetailedChartQuery definition = new DetailedChartQuery(rootEntity : REQUIREMENT_VERSION, targetEntities : [REQUIREMENT_VERSION, TEST_CASE])

		and :
		ExtendedHibernateQuery q = new QueryPlanner(definition).createQuery()


		when :
		q.select(QRequirementVersion.requirementVersion.id).where(QTestCase.testCase.id.eq(-1l))

		ExtendedHibernateQuery qq = q.clone(getSession())

		def res = qq.fetch()

		then :
		res as Set == [-12l, -21l, -31l] as Set

	}

	@DataSet("QueryPlanner.dataset.xml")
	def "should find all test cases because not joining on anything"(){

		given :

		DetailedChartQuery definition = new DetailedChartQuery(rootEntity : TEST_CASE, targetEntities : [TEST_CASE])

		and :
		ExtendedHibernateQuery q = new QueryPlanner(definition).createQuery()


		when :
		q.select(QTestCase.testCase.id)

		ExtendedHibernateQuery qq = q.clone(getSession())

		def res = qq.fetch()

		then :
		res as Set == [-1l, -2l, -3l] as Set

	}

	@Unroll
	@DataSet("QueryPlanner.dataset.xml")
	def "should find only test case 1 because test case 2 was never executed and test case 3 never planned"(){

		given :

		DetailedChartQuery definition = new DetailedChartQuery(rootEntity : TEST_CASE, targetEntities : [TEST_CASE, EXECUTION])

		and :
		ExtendedHibernateQuery q = new QueryPlanner(definition).createQuery()

		when :
		q.select(QTestCase.testCase.id)

		ExtendedHibernateQuery qq = q.clone(getSession())

		def res = qq.fetch()

		then :
		res as Set == [-1l] as Set

	}


	@Unroll
	@DataSet("QueryPlanner.dataset.xml")
	def "should test many possible queries"(){

		expect :
		DetailedChartQuery definition = new DetailedChartQuery(rootEntity : rootEntity, targetEntities : targetEntities)

		ExtendedHibernateQuery q = new QueryPlanner(definition).createQuery()
		q.where(wherePath)

		def joins = q.metadata.getJoins()
		def selec = q.metadata.getProjection()


		ExtendedHibernateQuery query = q.clone(getSession())
		def res = query.fetch()

		res.collect{it.id} as Set == expectedIds as Set


		where :

		rootEntity		|	targetEntities		|	wherePath						|	expectedIds
		TC				|	[TC, EX]			|	mkWherePath(EX, "id", -1113l)	|	[-1l]
		REQ				|	[REQ, CP]			|	mkWherePath(TC, "id", -2l)		|	[-1l, -3l]
		REQ				|	[REQ, EX]			|	mkWherePath(TC, "id", -2l)		|	[] // tc2 was never executed
		RV				|	[RV, CP]			|	mkWherePath(CP, "id", -1l)		|	[-12l, -21l, -31l, -11l, -32l]
		RV				|	[RV, CP, EX]		|	mkWherePath(CP, "id", -1l)		|	[-12l, -21l, -31l]


	}

	BooleanBuilder mkWherePath(InternalEntityType type, String ppt, Object value){

		EntityPathBase qbean = type.getQBean()
		def alias = qbean.metadata.name

		PathBuilder pbuilder = new PathBuilder(type.getEntityClass(), alias).get(ppt)

		return new BooleanBuilder(pbuilder.eq(value))


	}

}
