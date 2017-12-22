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


import org.hibernate.JDBCException
import org.hibernate.SessionFactory
import org.squashtest.tm.tools.unittest.hibernate.HibernateOperationCategory
import org.squashtest.it.basespecs.DbunitMappingSpecification;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem
import org.squashtest.tm.domain.execution.Execution
import org.squashtest.tm.domain.requirement.Requirement
import org.squashtest.tm.domain.requirement.RequirementVersion
import org.squashtest.tm.domain.testcase.ActionTestStep
import org.squashtest.tm.domain.testcase.Dataset
import org.squashtest.tm.domain.testcase.DatasetParamValue
import org.squashtest.tm.domain.testcase.Parameter
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.domain.testcase.TestCaseExecutionMode
import org.squashtest.tm.domain.testcase.TestCaseImportance
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode

import spock.lang.Unroll

class TestCaseMappingIT extends DbunitMappingSpecification {

	/*def "should persist and retrieve a test case"(){
	 given: "a test case"
	 TestCase tc = new TestCase(name: "name", description: "description")
	 tc.reference = "ref"
	 tc.prerequisite = "prerequisite"
	 tc.importance = TestCaseImportance.HIGH
	 when:
	 doInTransaction({ session ->
	 session.persist(tc)
	 })
	 def obj = doInTransaction({ session ->
	 session.get(TestCase, tc.id)
	 })
	 then:
	 obj != null
	 obj.reference == tc.reference
	 obj.prerequisite == tc.prerequisite
	 obj.importance == tc.importance
	 obj.name == tc.name
	 obj.description == tc.description
	 obj.createdOn != null
	 obj.createdBy != null
	 obj.version == 1
	 obj.executionMode == TestCaseExecutionMode.MANUAL
	 cleanup:
	 doInTransaction({ session ->
	 session.delete(tc)
	 })
	 }
	 def "should not persist a nameless test case"(){
	 given: "a test case"
	 TestCase tc = new TestCase(description: "description")
	 when:
	 doInTransaction({ session ->
	 session.persist(tc)
	 })
	 then:
	 thrown(JDBCException)
	 }
	 @Unroll("should retrieve steps in persisted order, test ##testNr")
	 def "should retrieve steps in persisted order"() {
	 given: "a persistent test case with steps"
	 def step1 = new ActionTestStep(action: "step1")
	 def step2 = new ActionTestStep(action: "step2")
	 def step3 = new ActionTestStep(action: "step3")
	 def testCase = new TestCase(name: "with steps")
	 testCase.steps << step1
	 testCase.steps << step2
	 testCase.steps << step3
	 when:
	 doInTransaction { s -> s.persist testCase }
	 def readTestCase = doInTransaction({ s ->
	 s.createQuery("from TestCase tc left join fetch tc.steps where tc.id = " + testCase.id).uniqueResult()
	 })
	 def steps = readTestCase.steps
	 then:
	 steps != null
	 steps[0].action == step1.action
	 steps[1].action == step2.action
	 steps[2].action == step3.action
	 where:
	 testNr << [1, 2, 3, 4, 5]
	 }
	 def "should persist a test case and retrieve it as a TestCaseOrganization"() {
	 given:
	 TestCase tc = new TestCase(name: "polymorphic")
	 doInTransaction({ s -> s.persist tc })
	 when:
	 def content = doInTransaction({ s ->
	 s.get TestCaseLibraryNode, tc.id
	 })
	 then:
	 content.name == tc.name
	 }
	 def "should persist new steps order"() {
	 given:
	 TestCase tc = new TestCase(name: "reorder")
	 def s0 = new ActionTestStep(action: "s0")
	 def s1 = new ActionTestStep(action: "s1")
	 tc.steps << s0
	 tc.steps << s1
	 persistFixture tc
	 when:
	 doInTransaction({
	 TestCase t = it.get(TestCase, tc.id)
	 def s = t.steps[1]
	 t.steps.add 0, s
	 t.steps.remove 2
	 println "NOMBRE DE STEPS : " + t.steps.size()
	 println "STEPS : " + t.steps.collect { "action: ${it.action}" }
	 })
	 def res = doInTransaction ({
	 it.createQuery("from TestCase tc join fetch tc.steps where tc.id = " + tc.id).uniqueResult()
	 })
	 then:
	 res.steps.size() == 2
	 res.steps[0].action == "s1"
	 res.steps[1].action == "s0"
	 cleanup:
	 deleteFixture tc, s0, s1
	 }
	 def "should remove a step from steps collection"() {
	 given:
	 TestCase tc = new TestCase(name: "delete")
	 def s1 = new ActionTestStep(action: "s1")
	 def s2 = new ActionTestStep(action: "s2")
	 tc.steps << s1
	 tc.steps << s2
	 persistFixture tc
	 when:
	 doInTransaction({
	 TestCase t = it.get(TestCase, tc.id)
	 t.steps.remove 0
	 })
	 def res = doInTransaction ({
	 it.createQuery("from TestCase tc join fetch tc.steps where tc.id = " + tc.id).uniqueResult()
	 })
	 def removedStep = doInTransaction {
	 it.get ActionTestStep, s1.id
	 }
	 then:
	 res.steps.size() == 1
	 res.steps[0].action == "s2"
	 // removal from collection does not delete the entity !
	 removedStep != null
	 cleanup:
	 deleteFixture tc, s1, s2
	 }
	 def "should retrieve test cases with a creator"(){
	 given: "a test case"
	 TestCase tc = new TestCase(name: "with creator")
	 when:
	 doInTransaction({ session ->
	 session.persist(tc)
	 })
	 def obj = doInTransaction({ session ->
	 session.createQuery("from TestCase where audit.createdBy is not null and name = 'with creator'").uniqueResult()
	 })
	 then:
	 obj != null
	 obj.name == tc.name
	 cleanup:
	 doInTransaction({ session ->
	 session.delete(tc)
	 })
	 }
	 def "should get the latest modified execution"() {
	 given:
	 Execution e1 = new Execution(name: "foo")
	 persistFixture e1
	 Execution e2 = new Execution(name: "bar")
	 persistFixture e2
	 Date first = new Date(20000L)
	 Date second = new Date(80000L)
	 e1.audit.lastModifiedOn = first
	 e2.audit.lastModifiedOn = second
	 and :
	 TestCase tc = new TestCase(name: "baz")
	 persistFixture(tc)
	 IterationTestPlanItem itp = new IterationTestPlanItem(tc)
	 persistFixture(itp)
	 itp.addExecution(e1)
	 itp.addExecution (e2)
	 when:
	 Execution ex = doInTransaction ({
	 it.createQuery("select e from IterationTestPlanItem itp join itp.executions e where itp.referencedTestCase.id = "+ tc.id +" and e.audit.lastModifiedOn = (select max(ex.audit.lastModifiedOn) from itp.executions ex)").uniqueResult()
	 })
	 then:
	 //		ex?.lastModifiedOn == second
	 ex == null  // FIXME this test does not work !
	 cleanup:
	 deleteFixture e1, e2, itp, tc
	 }
	 def "should persist a test case with its steps "(){
	 given :
	 def testCase = new TestCase(name:"test-case", description:"<p>one more</p>")
	 def step1 = new ActionTestStep(action:"action1", expectedResult:"result1")
	 def step2 = new ActionTestStep(action:"action2", expectedResult:"result2")
	 testCase.addStep step1
	 testCase.addStep step2
	 when :
	 doInTransaction({
	 it.persist testCase
	 })
	 then :
	 notThrown Exception
	 }
	 def "should persist test case with parameters, dataset and datasetParamValues"(){
	 given: "a test case"
	 TestCase tc = new TestCase(name : "name", description: "description")
	 and : "a parameter"
	 Parameter param = new Parameter("param", tc)
	 and :"a dataset"
	 Dataset dataset = new Dataset("dataset", tc)
	 and : "a dataset param value"
	 new DatasetParamValue(param, dataset, "value");
	 when:
	 doInTransaction({ session ->
	 session.persist(tc)
	 })
	 then:
	 notThrown Exception
	 }*/


	//	def "should persist a test case verifying an existing requirement version"() {
	//		given:
	//		Requirement req = new Requirement(new RequirementVersion(name: "req"))
	//		persistFixture req
	//
	//		and:
	//		TestCase tc = new TestCase(name: "tc")
	//		ActionTestStep s = new ActionTestStep(action: "step")
	//		tc.steps << s
	//
	//		when:
	//		use (HibernateOperationCategory) {
	//			sessionFactory.doInSession {
	//				Requirement r = it.get(Requirement, req.id)
	//				r.description = "bar"
	//				r.currentVersion.addVerifyingTestCase(tc)
	//
	//			}
	//		}
	//
	//		def res = {
	//			use (HibernateOperationCategory) {
	//				sessionFactory.doInSession {
	//					it.createQuery("select tc from TestCase tc join fetch tc.steps join fetch tc.requirementVersionCoverages where tc.id = $tc.id").uniqueResult()
	//				}
	//			}
	//		}
	//
	//		then:
	//		res() != null
	//		res().steps.size() == 1
	//		res().verifiedRequirementVersions.size() == 1
	//
	//		cleanup:
	//		deleteFixture req, tc, s
	//	}
}
