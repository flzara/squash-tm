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
package org.squashtest.tm.domain.jpql

import org.squashtest.tm.domain.jpql.ExtOps.ConcatOrder;
import org.squashtest.tm.domain.testcase.QTestCase;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.hibernate.HibernateQuery;

import spock.lang.Specification;

class QuerydslStringExtentionTest extends Specification{


	def "should generate a jpa with group_concat"(){

		given :
		QTestCase tc = new QTestCase("tc");

		ExtendedHibernateQuery q = new ExtendedHibernateQuery()
		q.select(tc.name.groupConcat()).from(tc)


		when :
		def asString = q.toString()

		then :
		asString ==
				"""select group_concat(tc.name)
from TestCase tc"""


	}

	def "should generate a jpa with group_concat and order"(){

		given :
		QTestCase tc = new QTestCase("tc");

		ExtendedHibernateQuery q = new ExtendedHibernateQuery()
		q.select(tc.name.orderedGroupConcat(tc.reference)).from(tc)


		when :
		def asString = q.toString()

		then :
		def projection = q.metadata.projection

		projection.operator == ExtOps.ORDERED_GROUP_CONCAT
		projection.args as List== [tc.name, Expressions.constant("order by"), tc.reference] as List

		asString ==
				"""select group_concat(tc.name,?1,tc.reference)
from TestCase tc"""
	}


	def "should generate a jpa with group_concat and order and direction"(){

		given :
		QTestCase tc = new QTestCase("tc");

		ExtendedHibernateQuery q = new ExtendedHibernateQuery()
		q.select(tc.name.orderedGroupConcat(tc.reference,ConcatOrder.ASC)).from(tc)


		when :
		def asString = q.toString()

		then :
		def projection = q.metadata.projection

		projection.operator == ExtOps.ORDERED_GROUP_CONCAT_DIR
		projection.args as List== [tc.name, Expressions.constant("order by"), tc.reference, Expressions.constant("asc")] as List

		asString ==
				"""select group_concat(tc.name,?1,tc.reference,?2)
from TestCase tc"""


	}

	def "should generate a jpa query with s_sum over a subquery"(){

		given : "the entity"
		QTestCase tc = new QTestCase("tc")
		QTestCase tc2 = new QTestCase("tc2")


		and : "the subquery"
		ExtendedHibernateQuery subquery = new ExtendedHibernateQuery()
		subquery.select(tc2.id.countDistinct()).from(tc2)


		and : "the main query"

		ExtendedHibernateQuery mainQuery = new ExtendedHibernateQuery()
		mainQuery.select(subquery.s_sum()).from(tc)

		when :
		def asString = mainQuery.toString()

		then :
		asString ==
				"""select s_sum((select count(distinct tc2.id)
from TestCase tc2))
from TestCase tc"""

	}

	def "should generate a jpa query with s_count over a subquery"(){

		given : "the entity"
		QTestCase tc = new QTestCase("tc")
		QTestCase tc2 = new QTestCase("tc2")


		and : "the subquery"
		ExtendedHibernateQuery subquery = new ExtendedHibernateQuery()
		subquery.select(tc2.id.countDistinct()).from(tc2)


		and : "the main query"

		ExtendedHibernateQuery mainQuery = new ExtendedHibernateQuery()
		mainQuery.select(subquery.s_count()).from(tc)

		when :
		def asString = mainQuery.toString()

		then :
		asString ==
				"""select s_count((select count(distinct tc2.id)
from TestCase tc2))
from TestCase tc"""

	}
}
