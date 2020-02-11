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
package org.squashtest.tm.domain.testcase

import org.junit.runner.RunWith
import org.spockframework.runtime.Sputnik
import org.springframework.transaction.annotation.Transactional
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.squashtest.tm.domain.EntityType
import org.squashtest.tm.domain.chart.ChartDefinition
import org.squashtest.tm.domain.chart.ChartType
import org.squashtest.tm.domain.chart.Visibility
import org.squashtest.tm.domain.query.DataType
import org.squashtest.tm.domain.query.Operation
import org.unitils.dbunit.annotation.DataSet
import spock.unitils.UnitilsSupport

import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

@UnitilsSupport
@Transactional
@RunWith(Sputnik)
class TestCaseIT extends DbunitServiceSpecification{

	@PersistenceContext
	EntityManager em

	@DataSet
	def "Should find all different test case types"(){

		when :
		def res = em
			.createQuery("from TestCase")
			.getResultList()
		then :
		res.size() == 2
	}

	@DataSet
	def "Should find a single classic test case"(){

		when :
		def res = em.find(TestCase.class, -10L)
		then :
		res != null
		res.id == -10
		res.name == "test-10"
	}

	@DataSet
	def "Should find a single keyword test case as a test case"(){

		when :
		def res = em.find(TestCase.class, -20L)
		then :
		res != null
		res.id == -20
		res.name == "test-20"
	}

	@DataSet
	def "Should find a single keyword test case as a keyword test case"(){

		when :
		def res = em.find(KeywordTestCase.class, -20L)
		then :
		res != null
		res.id == -20
		res.name == "test-20"
	}

	@DataSet
	def "Should not find a single classic test case as a keyword test case"(){

		when :
		def res = em.find(KeywordTestCase.class, -10L)
		then :
		res == null
	}

	@DataSet
	def "Should correctly use a test case visitor"(){
		given:
		def res = ["hello", "goodbye"]
		def visitor = new TestCaseVisitor() {
			@Override
			void visit(TestCase testCase) {
				res[0] = "testCase"
			}

			@Override
			void visit(KeywordTestCase testCase) {
				res[1] = "keywordTestCase"
			}

			void visit(ScriptedTestCase scriptedTestCase) {
			}
		}
		and:
		def testCase = em.find(TestCase.class, -10L)
		def keywordTestCase = em.find(TestCase.class, -20L)
		when :
		visitor.visit(testCase)
		visitor.visit(keywordTestCase)
		then :
		res[0] == "testCase"
		res[1] == "keywordTestCase"
	}

}
