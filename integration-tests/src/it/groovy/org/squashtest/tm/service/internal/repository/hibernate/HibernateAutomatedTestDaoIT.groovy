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
package org.squashtest.tm.service.internal.repository.hibernate;

import javax.inject.Inject;

import static org.squashtest.tm.domain.execution.ExecutionStatus.*

import org.squashtest.it.basespecs.DbunitDaoSpecification;
import org.squashtest.tm.domain.testautomation.AutomatedTest;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.domain.testautomation.TestAutomationServer;
import org.squashtest.tm.service.internal.repository.AutomatedSuiteDao;
import org.squashtest.tm.service.internal.repository.AutomatedTestDao;
import org.unitils.dbunit.annotation.DataSet;

import spock.lang.Unroll;
import spock.unitils.UnitilsSupport;


@UnitilsSupport
@DataSet("HibernateAutomatedTestDaoIT.sandbox.xml")
public class HibernateAutomatedTestDaoIT extends DbunitDaoSpecification {

	@Inject
	AutomatedTestDao testDao;

	def "should persist a new test"(){

		given :
		def newtest = new AutomatedTest("new test", getProject(-1L))

		when :
		def persisted = testDao.persistOrAttach(newtest)

		then :
		persisted.id != null

	}

	def "should attach a new test because another test matches it exactly"(){

		given :
		def exist= new AutomatedTest("both", getProject(-2L))

		when :
		def persisted = testDao.persistOrAttach(exist)

		then :
		persisted.id == -22L
	}


	@Unroll("for test #id, should find #cnt inbound references")
	def "should count references"(){

		expect :
		testDao.countReferences(id) == cnt

		where :
		id		| cnt
		-11L	| 1
		-12L	| 3
		-13L	| 1
		-21L	| 1
		-22L	| 2
		-23L	| 0
		-14L	| 0
	}

	@Unroll("should #_neg remove automated test #id because it is referenced by #_referers")
	def "should remove (or not) an AutomatedTest"(){

		given :
		def test = getTest(id)

		when  :
		testDao.removeIfUnused(test)

		then :
		getSession().flush()
		found("AUTOMATED_TEST", "TEST_ID", id) == res

		where :

		_neg	| 	res		|	id	|	_referers
		"not"	|	true	|	-11L	|	"1 execution"
		"not"	|	true	|	-12L	|	"1 execution and 2 test cases"
		"not"	|	true	|	-21L	|	"1 test case"
		""		|	false	|	-23L	|	"nothing"

	}


	def "should remove automated tests referenced by nothing"(){
		when :
		testDao.pruneOrphans()
		getSession().flush()
		then :
		! found("AUTOMATED_TEST", "TEST_ID", -23L)
		! found("AUTOMATED_TEST", "TEST_ID", -14L)
		getSession().createQuery("from AutomatedTest").list().collect{it.id} as Set == [-11L, -12L, -13L, -21L, -22L, ] as Set
	}


	def getServer(id){
		getSession().get(TestAutomationServer.class, id)
	}

	def getProject(id){
		getSession().get(TestAutomationProject.class, id)
	}

	def getTest(id){
		getSession().get(AutomatedTest.class, id)
	}

}
