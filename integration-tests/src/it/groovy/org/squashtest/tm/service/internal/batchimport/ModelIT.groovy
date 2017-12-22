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

import static org.squashtest.tm.service.internal.batchimport.Existence.*
import static org.squashtest.tm.service.internal.batchimport.StepType.*

import javax.inject.Inject
import javax.inject.Provider

import org.junit.runner.RunWith
import org.spockframework.runtime.Sputnik
import org.springframework.transaction.annotation.Transactional
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.unitils.dbunit.annotation.DataSet

import spock.unitils.UnitilsSupport


@UnitilsSupport
@Transactional
@RunWith(Sputnik)
class ModelIT  extends DbunitServiceSpecification {

	@Inject
	Provider<Model> modelProvider


	Model model

	def setup(){
		model = modelProvider.get()
	}

	/**
	 *
	 * See  {@link FacilityImplIT} for dataset description
	 *
	 */
	@DataSet("batchimport.sandbox.xml")
	def "should init the requested target, some targets already exists and some do not"(){

		given :
			def targets = createTestCaseTargets("/autre project/folder/TEST B", "/Test Project-1/dossier 2/0 test case \\/ with slash", "/nonexistant/unknown", "/Test Project-1/nonexistant")

		when :
			model.mainInitTestCase(targets)


		then :

			// check now the model for the test cases
			model.testCaseStatusByTarget[targets[0]].status == EXISTS
			model.testCaseStatusByTarget[targets[1]].status == EXISTS
			model.testCaseStatusByTarget[targets[2]].status == NOT_EXISTS
			model.testCaseStatusByTarget[targets[3]].status == NOT_EXISTS

			model.testCaseStatusByTarget[targets[0]].id == -248L
			model.testCaseStatusByTarget[targets[1]].id == -244L
			model.testCaseStatusByTarget[targets[2]].id == null
			model.testCaseStatusByTarget[targets[3]].id == null

			// now check the model for the steps
			model.testCaseStepsByTarget[targets[0]].collect{it.type} == [ACTION, ACTION]
			model.testCaseStepsByTarget[targets[1]].collect{it.type} == [ACTION, ACTION, CALL]
			model.testCaseStepsByTarget[targets[1]][2].calledTC == new TestCaseTarget("/Test Project-1/test 3");
			model.testCaseStepsByTarget[targets[2]] == []
			model.testCaseStepsByTarget[targets[3]] == []

			// now check the model for the projects
			model.projectStatusByName["Test Project-1"].status == EXISTS
			model.projectStatusByName["autre project"].status == EXISTS
			model.projectStatusByName["nonexistant"].status == NOT_EXISTS

			model.projectStatusByName["autre project"].id == -15L
			model.projectStatusByName["Test Project-1"].id == -14L
			model.projectStatusByName["nonexistant"].id == null

			// now check the custom fields
			model.tcCufsPerProjectname["Test Project-1"].collect{ it.code } as Set == ["TXT_TC", "CK_TC"] as Set
			model.tcCufsPerProjectname["autre project"].collect{ it.code }  as Set == ["CK_TC", "DATE"]  as Set
			model.stepCufsPerProjectname["Test Project-1"].collect{ it.code } as Set == ["LST_ST"]  as Set
			model.stepCufsPerProjectname["autre project"].collect{ it.code } as Set == ["DATE", "LST_ST"] as Set

			// now check the call graph
			model.isCalledBy(target("/Test Project-1/dossier 1/test case 2"), target("/autre project/TEST A"))
			model.isCalledBy(target("/Test Project-1/dossier 2/0 test case \\/ with slash"), target("/autre project/TEST A"))
			model.isCalledBy(target("/Test Project-1/test 3"), target("/Test Project-1/dossier 2/0 test case \\/ with slash"))
			model.isCalledBy(target("/Test Project-1/dossier 1/test case 1"), target("/Test Project-1/test 3"))

	}


	/**
	 *
	 * See  {@link FacilityImplIT} for dataset description
	 *
	 */
	@DataSet("batchimport.sandbox.xml")
	def "should init on the fly a test case that wasn't init-ed early on"(){

		given :
			def targets = createTestCaseTargets("/Test Project-1/test 3")

		when :
			def status = model.getStatus(targets[0])

		then :
			status.id == -245L
			status.status == EXISTS

			def steps = model.testCaseStepsByTarget.get(targets[0]);
			steps.collect{ it.type } == [ACTION, CALL, ACTION]
			steps[1].calledTC == new TestCaseTarget("/Test Project-1/dossier 1/test case 1")


	}

	/**
	 *
	 * See  {@link FacilityImplIT} for dataset description
	 *
	 */
	@DataSet("batchimport.sandbox.xml")
	def "should tell which custom fields are supported for a test case"(){

		given :
			def targets = createTestCaseTargets("/Test Project-1/test 3")

		when :
			def cufs = model.getTestCaseCufs(targets[0])

		then :
			cufs.collect{ it.code }  as Set == ["TXT_TC", "CK_TC"] as Set

	}
	/**
	 *
	 * See  {@link FacilityImplIT} for dataset description
	 *
	 */
	@DataSet("batchimport.sandbox.xml")
	def "should tell which custom fields are supported for a test step"(){

		given :
			def targets = createTestCaseTargets("/autre project/TEST A")

		when :
			def cufs = model.getTestStepCufs(new TestStepTarget(targets[0], 0))

		then :
			cufs.collect{ it.code }  as Set == ["DATE", "LST_ST"] as Set

	}


	def createTestCaseTargets(String... paths){
		return paths.collect{ return new TestCaseTarget(it) }
	}

	def target(path){
		return new TestCaseTarget(path)
	}
}
