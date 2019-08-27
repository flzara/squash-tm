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

import org.hibernate.Session
import org.squashtest.it.basespecs.DbunitMappingSpecification
import org.squashtest.tm.domain.testcase.ActionTestStep
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.domain.testcase.TestStep
import org.unitils.dbunit.annotation.DataSet
import spock.unitils.UnitilsSupport

import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import javax.transaction.Transactional

@UnitilsSupport
@Transactional
class TestStepMappingIT extends DbunitMappingSpecification {

	@PersistenceContext
	EntityManager em

	Session getSession() {
		em.unwrap(Session.class)
	}


	@DataSet("TestStepMappingIT.should persist and retrieve a test step.xml")
	def "shoud persist and retrieve a test step"() {
		given:
		TestCase tc = session.load(TestCase, -10L)

		when:
		ActionTestStep ts = new ActionTestStep(action: "my action", expectedResult: "my expected result")
		tc.steps << ts
		ts.testCase = tc

		session.persist ts
		session.flush()
		session.clear()

		and:
		TestStep obj = session.get(TestStep, ts.id)

		then:
		obj.action == ts.action
		obj.expectedResult == ts.expectedResult

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
