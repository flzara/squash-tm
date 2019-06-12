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
package org.squashtest.tm.hibernate.mapping.jpql

import org.hibernate.Session
import org.squashtest.it.basespecs.DbunitDaoSpecification
import org.squashtest.tm.domain.jpql.ExtOps
import org.squashtest.tm.domain.jpql.ExtendedHibernateQuery
import org.squashtest.tm.domain.query.QQueryColumnPrototype


/*
	No dataset here, the content of QUERY_COLUMN_PROTOTYPE is enough for our test needs
 */
class QueryDslExtensionsIT extends DbunitDaoSpecification{

	def proto = new QQueryColumnPrototype("proto")


	def createQuery(){
		new ExtendedHibernateQuery(getSession())
	}

	def "function 's_matches' should retrieve data by regular expression"(){

		given:
		def query = createQuery().from(proto).select(proto.label).where(proto.label.s_matches('^TEST_.*ID$').isTrue())

		when :

		def res = query.fetch()

		then :
		res as Set == [
			'TEST_CASE_ID',
			'TEST_CASE_STEP_ID',
			'TEST_CASE_NATURE_ID',
			'TEST_CASE_TYPE_ID',
			'TEST_CASE_MILESTONE_ID',
			'TEST_CASE_PROJECT_ID',
			'TEST_CASE_ATTLIST_ID',
			'TEST_CASE_ATTACHMENT_ID'
		] as Set

	}

	def "function 's_matches' should retrieve nothing because of case sensitivity"(){

		given:
		def query = createQuery().from(proto).select(proto.label).where(proto.label.s_matches('^test_.*id$').isTrue())

		when :

		def res = query.fetch()

		then :
		res.isEmpty()
	}


	def "function 's_i_matches' should retrieve data by regular expression case insensitive"(){

		given:
		def query = createQuery().from(proto).select(proto.label).where(proto.label.s_i_matches('^TeSt_.*Id$').isTrue())

		when :
		println(query.toString())
		def res = query.fetch()

		then :
		res as Set == [
			'TEST_CASE_ID',
			'TEST_CASE_STEP_ID',
			'TEST_CASE_NATURE_ID',
			'TEST_CASE_TYPE_ID',
			'TEST_CASE_MILESTONE_ID',
			'TEST_CASE_PROJECT_ID',
			'TEST_CASE_ATTLIST_ID',
			'TEST_CASE_ATTACHMENT_ID'
		] as Set

	}


	def "group concat"(){

		given:
		def query = createQuery().from(proto).select(proto.label.orderedGroupConcat(proto.label, ExtOps.ConcatOrder.DESC))


		when:
		println(query.toString())

		then:
		println(query.fetch())


	}

}
