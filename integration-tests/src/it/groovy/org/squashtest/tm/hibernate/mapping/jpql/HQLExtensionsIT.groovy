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


import org.squashtest.it.basespecs.DbunitDaoSpecification
import spock.lang.Ignore

/**
 * No dataset, we'll use the content of QUERY_COLUMN_PROTOTYPE as example for our tests
 *
 */
class HQLExtensionsIT extends DbunitDaoSpecification{



	def "function 'matches' should retrieve data by regular expression"(){

		given:
		def query = 'select proto.label from QueryColumnPrototype proto where matches(proto.label, \'^TEST_.*ID$\') = true'

		when :

		def res = em.createQuery(query).getResultList()

		then :
		res as Set == [
			'TEST_CASE_ID',
			'TEST_CASE_STEP_ID',
			'TEST_CASE_NATURE_ID',
			'TEST_CASE_TYPE_ID',
			'TEST_CASE_PROJECT_ID',
			'TEST_CASE_MILESTONE_ID',
			'TEST_CASE_ATTLIST_ID',
			'TEST_CASE_ATTACHMENT_ID'
		] as Set

	}

	@Ignore("This feature is eventually not used")
	def "function 'matches' should retrieve nothing because of case sensitivity"(){

		given:
		def query = 'select proto.label from QueryColumnPrototype proto where matches(proto.label, \'^test_.*id$\') = true'

		when :

		def res = em.createQuery(query).getResultList()

		then :
		res.isEmpty()
	}


	def "function 'i_matches' should retrieve data by regular expression case insensitive"(){

		given:
		def query = 'select proto.label from QueryColumnPrototype proto where i_matches(proto.label, \'^TeSt_.*Id$\') = true'

		when :

		def res = em.createQuery(query).getResultList()

		then :
		res as Set == [
			'TEST_CASE_ID',
			'TEST_CASE_STEP_ID',
			'TEST_CASE_NATURE_ID',
			'TEST_CASE_TYPE_ID',
			'TEST_CASE_PROJECT_ID',
			'TEST_CASE_MILESTONE_ID',
			'TEST_CASE_ATTLIST_ID',
			'TEST_CASE_ATTACHMENT_ID'
		] as Set

	}
}
