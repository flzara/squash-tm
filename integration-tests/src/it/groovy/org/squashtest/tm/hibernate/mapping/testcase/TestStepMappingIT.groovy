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


import javax.inject.Inject

import org.hibernate.SessionFactory
import org.squashtest.tm.tools.unittest.hibernate.HibernateOperationCategory
import org.squashtest.it.basespecs.DbunitMappingSpecification;
import org.squashtest.tm.domain.testcase.ActionTestStep
import org.squashtest.tm.domain.testcase.TestCase

class TestStepMappingIT extends DbunitMappingSpecification {


	def "shoud persist and retrieve a test step"() {
		given:
		ActionTestStep ts = new ActionTestStep(action: "my action", expectedResult: "my expected result")

		when:
		doInTransaction({ session -> session.persist(ts) })
		def obj = doInTransaction({ session -> session.get(ActionTestStep, ts.id) })

		then:
		obj.action == ts.action
		obj.expectedResult == ts.expectedResult

		cleanup:
		deleteFixture ts
	}

	/*def "should cascade step persistence from test case"() {
	 given:
	 TestCase tc = new TestCase(name: "foo")
	 ActionTestStep s = new ActionTestStep(action: "do something")
	 tc.steps << s
	 when:
	 use (HibernateOperationCategory) {
	 sessionFactory.doInSession { it.persist tc }
	 }
	 def res = {
	 use (HibernateOperationCategory) {
	 sessionFactory.doInSession {
	 it.createQuery("select tc from TestCase tc join fetch tc.steps where tc.id = ${tc.id}").uniqueResult()
	 }
	 }
	 }
	 then:
	 res().steps.size() == 1
	 cleanup:
	 deleteFixture tc, s
	 }*/
}
