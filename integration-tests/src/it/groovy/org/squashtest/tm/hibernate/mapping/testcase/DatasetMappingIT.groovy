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
package org.squashtest.tm.hibernate.mapping.testcase


import javax.validation.ConstraintViolationException;
import org.squashtest.tm.domain.infolist.InfoListItem
import org.hibernate.JDBCException
import org.hibernate.SessionFactory
import org.squashtest.tm.tools.unittest.hibernate.HibernateOperationCategory
import org.squashtest.it.basespecs.DbunitMappingSpecification;
import org.squashtest.tm.domain.testcase.Dataset
import org.squashtest.tm.domain.testcase.TestCase

class DatasetMappingIT extends DbunitMappingSpecification {

	def "should not persist a nameless dataset"(){

		given:
                    // the nature and type might not be the correct system list items
                    // however it is good enough for our purposes
                    def nat = doInTransaction {it.load(InfoListItem.class, 1l)}
                    def type = doInTransaction {it.load(InfoListItem.class, 2L)}

                and :

		TestCase tc = new TestCase(name: "description", nature : nat, type : type)
                doInTransaction { it.persist tc }

		when:
		Dataset ds = new Dataset(testCase:tc)
		doInTransaction({ session ->
			session.persist(ds)
		})

		then:
		thrown(ConstraintViolationException)

		cleanup :
		deleteFixture tc
	}

}
