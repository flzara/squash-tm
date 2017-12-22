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
package org.squashtest.tm.service.internal.batchimport

import org.squashtest.tm.domain.NamedReference
import org.squashtest.tm.domain.customfield.BindableEntity
import org.squashtest.tm.domain.customfield.CustomField
import org.squashtest.tm.domain.library.structures.LibraryGraph
import org.squashtest.tm.domain.library.structures.LibraryGraph.SimpleNode
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.domain.requirement.RequirementLibrary
import org.squashtest.tm.domain.testcase.ParameterAssignationMode
import org.squashtest.tm.domain.testcase.TestCaseLibrary
import org.squashtest.tm.service.internal.batchimport.Model.InternalStepModel
import org.squashtest.tm.service.internal.repository.CustomFieldDao
import org.squashtest.tm.service.internal.testcase.TestCaseCallTreeFinder
import org.squashtest.tm.service.milestone.MilestoneMembershipFinder
import org.squashtest.tm.service.testcase.TestCaseLibraryFinderService
import spock.lang.Specification

import javax.persistence.EntityManager
import javax.persistence.Query

import static org.squashtest.tm.service.internal.batchimport.Existence.*

public class ModelTest extends Specification{

	EntityManager em
	CustomFieldDao cufDao
	TestCaseLibraryFinderService finderService
	TestCaseCallTreeFinder calltreeFinder
	MilestoneMembershipFinder milestoneMemberFinder

	Model model

	def setup(){

		em = Mock(EntityManager)
		cufDao = Mock(CustomFieldDao)
		finderService = Mock(TestCaseLibraryFinderService)
		calltreeFinder = Mock(TestCaseCallTreeFinder)
		milestoneMemberFinder = Mock(MilestoneMembershipFinder)

		model = new Model()
		model.em = em
		model.cufDao = cufDao
		model.finderService = finderService
		model.calltreeFinder = calltreeFinder
		model.callGraph = new TestCaseCallGraph()
		model.milestoneMemberFinder = milestoneMemberFinder

	}


	// ****************** utils **********************

	// note : implicitly this test also tests TestCaseTargets.equals(_)
	def "should eliminate redundant values"(){

		expect :
		model.uniqueList(targets) == filtered

		where :
		targets = targets("/project/a", "/project/b", "/project/a", "/project/c")
		filtered = targets("/project/a", "/project/b", "/project/c")
	}

	def "should collect projects names from targets"(){

		expect :
		model.collectProjects(targets) as Set == ["project a", "project b"] as Set

		where :
		targets = targets("/project a/test1", "/project a/test2", "/project b/test 3")

	}

	def "should collect the paths of a bunch of targets"(){

		expect :
		model.collectPaths(targets) == ["/project/a", "/project/B"]

		where :
		targets = targets("/project/a", "/project/B")
	}

	// ************* core mechanics ******************



	def "should init the model with some test cases"(){
		given :
		def targets = targets("/project/folder/test1", "/project/<nonexistant>/test2", "/project/folder/test3")

		and :
		finderService.findNodeIdsByPath(_) >> [10l, null, 20l]
		milestoneMemberFinder.isTestCaseMilestoneDeletable(_) >> false

		when :
		model.initTestCases(targets)

		TargetStatus status1 = model.testCaseStatusByTarget[targets[0]]
		TargetStatus status2 = model.testCaseStatusByTarget[targets[1]]
		TargetStatus status3 = model.testCaseStatusByTarget[targets[2]]

		then :
		status1.status == EXISTS
		status2.status == NOT_EXISTS
		status3.status == EXISTS

		status1.id == 10l
		status2.id == null
		status3.id == 20l


	}


	def "should init the test steps"(){

		given :
		def targets = targets("/project/notok", "/project/ok", "/project/tobedeleted")

		and :
		model.testCaseStatusByTarget[targets[0]] = new TargetStatus(NOT_EXISTS, null)
		model.testCaseStatusByTarget[targets[1]] = new TargetStatus(EXISTS, 10l)
		model.testCaseStatusByTarget[targets[2]] = new TargetStatus(TO_BE_DELETED, 20l)
		and :
		mockSessionQuery([
			["ACTION", null, true] as Object[],
			["CALL", 17l, true] as Object[],
			["ACTION", null, true] as Object[]
		])
		finderService.getPathAsString(17l) >> "/project/bob"

		when :
		model.initTestSteps(targets)


		then :
		model.testCaseStepsByTarget[targets[0]] == []

		Collection<InternalStepModel> stepsTC1 = model.testCaseStepsByTarget[targets[1]]
		stepsTC1.collect {it.type} == [StepType.ACTION, StepType.CALL, StepType.ACTION]
		stepsTC1.collect {it.calledTC} == [null, new TestCaseTarget("/project/bob"), null]
		stepsTC1.collect {it.delegates} == [false, true, false]

		model.testCaseStepsByTarget[targets[2]] == []



	}


	def "should init the projects"(){

		given :
		def targets = targets("/project a/test1", "/sprololo/testB", "/project a/test2")

		and :
		Project p = mockProject("project a", 10l)

		TestCaseLibrary tcl = Mock(TestCaseLibrary)
		tcl.getId() >> 10l
		p.getTestCaseLibrary() >> tcl

		RequirementLibrary rl = Mock(RequirementLibrary)
		rl.getId() >> 10l
		p.getRequirementLibrary() >> rl

		mockSessionQuery([p])

		and :
		def tccufs = [Mock(CustomField), Mock(CustomField), Mock(CustomField)]
		def stcufs = [Mock(CustomField)]
		cufDao.findAllBoundCustomFields(10l, BindableEntity.TEST_CASE) >> tccufs
		cufDao.findAllBoundCustomFields(10l, BindableEntity.TEST_STEP) >> stcufs

		when :
		model.initProjects(targets)

		ProjectTargetStatus status1 = model.projectStatusByName["project a"]
		ProjectTargetStatus status2 = model.projectStatusByName["sprololo"]

		then :
		status1.status == EXISTS
		status1.id == 10l

		status2.status == NOT_EXISTS
		status2.id == null

		model.tcCufsPerProjectname["project a"] == tccufs
		model.stepCufsPerProjectname["project a"] == stcufs
	}


	// ************************** accessors ****************************

	// cannot test the case where the entry doesn't exist yet in the map
	// because it would trigger init() on that target
	// which cannot easily be mocked.
	def "should get the status of a test case"(){

		given :
		def target = new TestCaseTarget("/project/bob")
		def status = new TargetStatus(TO_BE_CREATED)
		model.testCaseStatusByTarget[target] = status

		when :
		def res = model.getStatus(target)

		then :
		res == status

	}

	def "should change the status of a TestCase to 'EXISTS'"(){
		given :
		def target = new TestCaseTarget("/project/mike")
		model.testCaseStatusByTarget[target] == new TargetStatus(TO_BE_CREATED)
		when :
		model.setExists(target, 10l)

		then :
		model.getStatus(target).status == EXISTS
	}

	def "should change the status of a TestCase to 'TO_BE_CREATED'"(){
		given :
		def target = new TestCaseTarget("/project/mike")
		model.testCaseStatusByTarget[target] == new TargetStatus(NOT_EXISTS)
		when :
		model.setToBeCreated(target)

		then :
		model.getStatus(target).status == TO_BE_CREATED
	}

	def "should flag a test case to TO_BE_DELETED and remove preemptively the step informations"(){
		given :
		def target = new TestCaseTarget("/project/robert")
		model.testCaseStatusByTarget[target] = new TargetStatus(EXISTS, 12l)
		model.testCaseStepsByTarget[target] = [StepType.ACTION, StepType.ACTION, StepType.ACTION]
		when :
		model.setToBeDeleted(target)

		then :
		model.getStatus(target).status == TO_BE_DELETED
		model.testCaseStepsByTarget[target] == []

	}



	def "should flag a test case as deleted and remove the step informations"(){
		given :
		def target = new TestCaseTarget("/project/daryl")
		model.testCaseStatusByTarget[target] = new TargetStatus(TO_BE_DELETED)
		model.testCaseStepsByTarget[target] = [StepType.ACTION, StepType.ACTION, StepType.ACTION]
		when :
		model.setDeleted(target)

		then :
		model.getStatus(target).status == NOT_EXISTS
		model.testCaseStepsByTarget[target] == []

	}


	def "should tell that a step exists"(){

		given :
		def tc = new TestCaseTarget("/project/bob")
		def st  = new TestStepTarget(tc, 1)

		model.testCaseStatusByTarget[tc] = new TargetStatus(EXISTS, 10l)
		model.testCaseStepsByTarget[tc] = [
			new InternalStepModel(StepType.ACTION, null),
			new InternalStepModel(StepType.ACTION, null),
			new InternalStepModel(StepType.ACTION, null)
		]


		when :
		def res = model.stepExists(st)

		then :
		res == true

	}

	def "should tell that a step do not exists (out of bound)"(){

		given :
		def tc = new TestCaseTarget("/project/bob")
		def st  = new TestStepTarget(tc, 10)

		model.testCaseStatusByTarget[tc] = new TargetStatus(EXISTS, 10l)
		model.testCaseStepsByTarget[tc] =  [
			new InternalStepModel(StepType.ACTION, null),
			new InternalStepModel(StepType.ACTION, null),
			new InternalStepModel(StepType.ACTION, null)
		]


		when :
		def res = model.stepExists(st)

		then :
		res == false

	}


	def "should tell that a step do not exists (null)"(){

		given :
		def tc = new TestCaseTarget("/project/bob")
		def st  = new TestStepTarget(tc, null)

		model.testCaseStatusByTarget[tc] = new TargetStatus(EXISTS, 10l)
		model.testCaseStepsByTarget[tc] =  [
			new InternalStepModel(StepType.ACTION, null),
			new InternalStepModel(StepType.ACTION, null),
			new InternalStepModel(StepType.ACTION, null)
		]


		when :
		def res = model.stepExists(st)

		then :
		res == false

	}


	def "should add a test step of a given type to the model at the specified index"(){
		given :
		def tc = new TestCaseTarget("/project/bob")
		def ctc = new TestCaseTarget("whatever")
		def st  = new TestStepTarget(tc, 1)

		def paramInfo = new CallStepParamsInfo()
		paramInfo.getParamMode() >> ParameterAssignationMode.DELEGATE


		model.testCaseStatusByTarget[tc] = new TargetStatus(EXISTS, 10l)
		model.testCaseStatusByTarget[ctc] = new TargetStatus(EXISTS, 20l)
		model.testCaseStepsByTarget[tc] =  [
			new InternalStepModel(StepType.ACTION, null),
			new InternalStepModel(StepType.ACTION, null),
			new InternalStepModel(StepType.ACTION, null)
		]

		and :
		calltreeFinder.getExtendedGraph(_) >> new LibraryGraph()
		finderService.getPathsAsString(_) >> []

		when :
		model.addCallStep(st, ctc, paramInfo)

		then :
		model.testCaseStepsByTarget[tc].collect{it.type} == [StepType.ACTION, StepType.CALL, StepType.ACTION, StepType.ACTION]
		model.testCaseStepsByTarget[tc][1].calledTC == ctc
	}


	def "should add a test step of a given type to the model at the end because of null index"(){

		given :
		def tc = new TestCaseTarget("/project/bob")
		def ctc = new TestCaseTarget("whatever")
		def st  = new TestStepTarget(tc, null)
		def paramInfo = new CallStepParamsInfo()
		paramInfo.getParamMode() >> ParameterAssignationMode.DELEGATE

		model.testCaseStatusByTarget[tc] = new TargetStatus(EXISTS, 10l)
		model.testCaseStatusByTarget[ctc] = new TargetStatus(EXISTS, 20l)
		model.testCaseStepsByTarget[tc] =  [
			new InternalStepModel(StepType.ACTION, null),
			new InternalStepModel(StepType.ACTION, null),
			new InternalStepModel(StepType.ACTION, null)
		]

		and :
		calltreeFinder.getExtendedGraph(_) >> new LibraryGraph()
		finderService.getPathsAsString(_) >> []

		when :
		model.addCallStep(st, ctc, paramInfo)

		then :
		model.testCaseStepsByTarget[tc].collect {it.type} == [StepType.ACTION, StepType.ACTION, StepType.ACTION, StepType.CALL]
		model.testCaseStepsByTarget[tc][3].calledTC == ctc
	}


	def "should add a test step of a given type to the model at the end because of out of upper bound index"(){

		given :
		def tc = new TestCaseTarget("/project/bob")
		def ctc = new TestCaseTarget("whatever")
		def st  = new TestStepTarget(tc, 18)
		def paramInfo = new CallStepParamsInfo()
		paramInfo.getParamMode() >> ParameterAssignationMode.DELEGATE

		model.testCaseStatusByTarget[tc] = new TargetStatus(EXISTS, 10l)
		model.testCaseStatusByTarget[ctc] = new TargetStatus(EXISTS, 20l)
		model.testCaseStepsByTarget[tc] =  [
			new InternalStepModel(StepType.ACTION, null),
			new InternalStepModel(StepType.ACTION, null),
			new InternalStepModel(StepType.ACTION, null)
		]

		and :
		calltreeFinder.getExtendedGraph(_) >> new LibraryGraph()
		finderService.getPathsAsString(_) >> []

		when :
		model.addCallStep(st, ctc, paramInfo)

		then :
		model.testCaseStepsByTarget[tc].collect{ it.type} == [StepType.ACTION, StepType.ACTION, StepType.ACTION, StepType.CALL]
		model.testCaseStepsByTarget[tc][3].calledTC == ctc
	}


	def "should remove a step if exists"(){

		given :
		def tc = new TestCaseTarget("/project/bob")
		def st  = new TestStepTarget(tc, 1)

		model.testCaseStatusByTarget[tc] = new TargetStatus(EXISTS, 10l)
		model.testCaseStepsByTarget[tc] =  [
			new InternalStepModel(StepType.ACTION, null),
			new InternalStepModel(StepType.CALL, new TestCaseTarget("/whatever")),
			new InternalStepModel(StepType.ACTION, null)
		]

		when :
		model.remove(st)

		then :
		model.testCaseStepsByTarget[tc].collect{it.type} == [StepType.ACTION, StepType.ACTION]

	}

	def "should fail to remove a step if not exists"(){

		given :
		def tc = new TestCaseTarget("/project/bob")
		def st  = new TestStepTarget(tc, 17)

		model.testCaseStatusByTarget[tc] = new TargetStatus(EXISTS, 10l)
		model.testCaseStepsByTarget[tc] =  [
			new InternalStepModel(StepType.ACTION, null),
			new InternalStepModel(StepType.CALL, null),
			new InternalStepModel(StepType.ACTION, null)
		]

		when :
		model.remove(st)

		then :
		thrown IllegalArgumentException

	}

	// ************************* call graph ****************************

	def "should replace names with path"(){

		given :
		def nodes = [
			new SimpleNode<NamedReference>(new NamedReference(1l, "bob")),
			new SimpleNode<NamedReference>(new NamedReference(2l, "robert")),
			new SimpleNode<NamedReference>(new NamedReference(3l, "mike"))
		]

		and :
		finderService.getPathsAsString([1l, 2l, 3l]) >> ["/project/bob", "/project/folder/robert", "/project/other/mike"]

		when :
		model.swapNameForPath(nodes)

		then :
		nodes.collect{ return [it.key.id, it.key.name] } as Set	== [
			[1l, "/project/bob"],
			[2l, "/project/folder/robert"],
			[3l, "/project/other/mike"],
		] as Set
	}


	def "should merge the call graph of a new node into an existing call graph"(){

		given :
		model.callGraph = Mock(TestCaseCallGraph)

		and:
		finderService.findNodeIdByPath("/project") >> 0L

		and :
		LibraryGraph fromdb = new LibraryGraph()

		def darry = new SimpleNode(new NamedReference(4l, "darry"))
		def srobert = new SimpleNode(new NamedReference(3l, "robert"))

		fromdb.addEdge srobert, darry
		calltreeFinder.getExtendedGraph(_) >> fromdb

		and :
		finderService.findNodeIdByPath("/project/darry") >> 4l
		finderService.getPathsAsString([3l, 4l]) >> ["/project/robert", "/project/darry" ]
		finderService.getPathsAsString([4l, 3l]) >> ["/project/darry", "/project/robert" ]


		when :
		model.initCallGraph(new TestCaseTarget("/project"))

		then :
		fromdb.nodes.collect { it.key.name } as Set == ["/project/robert", "/project/darry" ] as Set
		1 * model.callGraph.addGraph(fromdb)
	}

	// ************************** parameters ***************************

	def "should retrieve all the parameters available to a test case (1)"(){

		given :
		def bob = new TestCaseTarget("/project/bob")
		def robert = new TestCaseTarget("/project/robert")
		def mike = new TestCaseTarget("/project/mike")

		and :
		def p1 = new ParameterTarget(bob, "meal")
		def p2 = new ParameterTarget(bob, "couch")
		def p3 = new ParameterTarget(robert, "basement surface")
		def p4 = new ParameterTarget(mike, "tv size")
		def p5 = new ParameterTarget(mike, "beer stocks")

		and :
		model.callGraph.addEdge(bob, robert)
		model.callGraph.addEdge(robert, mike)
		model.callGraph.addEdge(bob, mike)

		and :
		model.testCaseStepsByTarget.put bob, [new InternalStepModel(StepType.CALL, robert, true), new InternalStepModel(StepType.CALL, mike, true)] as List
		model.testCaseStepsByTarget.put robert, [new InternalStepModel(StepType.CALL, mike, true)] as List


		and :

		model.parametersByTestCase.put	 bob, [ p1, p2 ] as Set
		model.parametersByTestCase.put	 robert, [ p3 ] as Set
		model.parametersByTestCase.put	 mike,  [p4, p5] as Set


		when :
		Collection<ParameterTarget> allparams = model.getAllParameters(bob)

		then :
		allparams as Set == [p1, p2, p3, p4, p5] as Set

	}

	def "should retrieve all the parameters available to a test case (2)"(){

		given :
		def bob = new TestCaseTarget("/project/bob")
		def robert = new TestCaseTarget("/project/robert")
		def mike = new TestCaseTarget("/project/mike")

		and :
		def p1 = new ParameterTarget(bob, "meal")
		def p2 = new ParameterTarget(bob, "couch")
		def p3 = new ParameterTarget(robert, "basement surface")
		def p4 = new ParameterTarget(mike, "tv size")
		def p5 = new ParameterTarget(mike, "beer stocks")

		and :
		model.callGraph.addEdge(bob, robert)
		model.callGraph.addEdge(robert, mike)
		model.callGraph.addEdge(bob, mike)

		and:
		model.testCaseStepsByTarget.put bob, [new InternalStepModel(StepType.CALL, robert, true), new InternalStepModel(StepType.CALL, mike, true)] as List
		model.testCaseStepsByTarget.put robert, [new InternalStepModel(StepType.CALL, mike, true)] as List

		and :
		model.parametersByTestCase.put	 bob, [ p1, p2 ] as Set
		model.parametersByTestCase.put	 robert, [ p3 ] as Set
		model.parametersByTestCase.put	 mike,  [p4, p5] as Set

		when :
		Collection<ParameterTarget> allparams = model.getAllParameters(robert)

		then :
		allparams as Set == [p3, p4, p5] as Set

	}

	def "should retrieve only inherited parameters available to a test case"(){

		given :
		def bob = new TestCaseTarget("/project/bob")
		def robert = new TestCaseTarget("/project/robert")
		def mike = new TestCaseTarget("/project/mike")

		and :
		def p1 = new ParameterTarget(bob, "meal")
		def p2 = new ParameterTarget(bob, "couch")
		def p3 = new ParameterTarget(robert, "basement surface")
		def p4 = new ParameterTarget(mike, "tv size")
		def p5 = new ParameterTarget(mike, "beer stocks")

		and :
		model.callGraph.addEdge(bob, robert)
		model.callGraph.addEdge(robert, mike)
		model.callGraph.addEdge(bob, mike)

		and:
		model.testCaseStepsByTarget.put bob, [new InternalStepModel(StepType.CALL, robert, true), new InternalStepModel(StepType.CALL, mike, false)] as List
		model.testCaseStepsByTarget.put robert, [new InternalStepModel(StepType.CALL, mike, false)] as List

		and :
		model.parametersByTestCase.put	 bob, [ p1, p2 ] as Set
		model.parametersByTestCase.put	 robert, [ p3 ] as Set
		model.parametersByTestCase.put	 mike,  [p4, p5] as Set

		when :
		Collection<ParameterTarget> allparams = model.getAllParameters(bob)

		then :
		allparams as Set == [p1, p2, p3] as Set

	}

	// **************************** utils ******************************

	def mockProject(name, id){
		Project proj = Mock(Project)
		proj.getName() >> name
		proj.getId() >> id
		return proj
	}

	def mockSessionQuery(queryResults){
		Query q = Mock(Query)
		em.createNamedQuery(_) >> q
		q.getResultList() >> queryResults
	}

	def targets(String... paths){
		return paths.collect{ new TestCaseTarget(it) }
	}
}
