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
package org.squashtest.tm.service.testcase


import javax.inject.Inject

import org.spockframework.util.NotThreadSafe
import org.springframework.transaction.annotation.Transactional
import org.squashtest.tm.domain.testcase.ParameterAssignationMode
import org.squashtest.tm.exception.CyclicStepCallException
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.squashtest.tm.service.internal.testcase.TestCaseCallTreeFinder
import org.squashtest.tm.service.testcase.CallStepManagerService
import org.squashtest.tm.service.testcase.TestCaseModificationService
import org.unitils.dbunit.annotation.DataSet

import spock.unitils.UnitilsSupport

@NotThreadSafe
@UnitilsSupport
@Transactional
class CallStepManagerServiceIT extends DbunitServiceSpecification {

	@Inject
	CallStepManagerService callStepService

	@Inject
	ParameterFinder paramService

	@Inject
	DatasetModificationService datasetService

	@Inject
	TestCaseModificationService testCaseService

	@Inject
	TestCaseCallTreeFinder callTreeFinder


	def setupSpec(){
		Collection.metaClass.matches ={ arg ->
			delegate.containsAll(arg) && arg.containsAll(delegate)
		}
	}

	@DataSet("CallStepManagerServiceIT.dataset.xml")
	def "should deny step call creation because the callse and calling test cases are the same"(){
		given :

		when :
		callStepService.addCallTestStep(-1L, -1L)	

		then :
		thrown(CyclicStepCallException)
	}



	@DataSet("CallStepManagerServiceIT.dataset.xml")
	def "should deny step call creation because the caller is somewhere in the test case call tree of the called test case"(){
		given :

		when :
		callStepService.addCallTestStep(-31L, -1L)

		then :
		thrown(CyclicStepCallException)
	}


	@DataSet("CallStepManagerServiceIT.dataset.xml")
	def "should successfully create a call step"(){

		given :
		def expectedTree = [-1L, -11L, -21L, -22L, -31L, -32L]


		when :
		callStepService.addCallTestStep(-10L, -1L)
		def callTree = callTreeFinder.getTestCaseCallTree(-10L)


		then :
		callTree.matches expectedTree

	}

	@DataSet("CallStepManagerServiceIT.dataset.xml")
	def "should throw CyclicStepCallException because the destination test case is somewhere in the test case call tree of the pasted steps"(){
		given :
		def pastedStepsIds = ['-11','-1000', '-101'] as String[]
		def destinationTestCaseid = -32L
		when :
		callStepService.checkForCyclicStepCallBeforePaste(destinationTestCaseid, pastedStepsIds)

		then :
		thrown(CyclicStepCallException)
	}

	@DataSet("CallStepManagerServiceIT.dataset.xml")
	def "should throw CyclicStepCallException because the destination test case is called by one of the pasted steps"(){
		given :
		def pastedStepsIds = ['-32','-1000'] as String[]
		def destinationTestCaseid = -32L
		when :
		callStepService.checkForCyclicStepCallBeforePaste(destinationTestCaseid, pastedStepsIds)

		then :
		thrown(CyclicStepCallException)
	}


	@DataSet("CallStepManagerServiceIT.dataset.xml")
	def "setting call step to parameter delegation should delegate more parameters to upstream test cases"(){

		given: 	"checking initial conditions"

		def initialTc1IsTrue = theTestCase(-1l).canAccessParameters ( [ -321l, -221l ] )
		def initialTc11IsTrue = theTestCase(-11l).canAccessParameters ( [-321l, -221l] )


		and : "the call step that will change that"

		def callstepId = -21l

		when : "changing the assignation mode of a step to DELEGATE"
		callStepService.setParameterAssignationMode callstepId, ParameterAssignationMode.DELEGATE, null

		then :

		initialTc1IsTrue
		initialTc11IsTrue

		theTestCase(-1l).canAccessParameters ( [-321l, -221l, -211l, -212l ] )
		theTestCase(-11l).canAccessParameters (  [-321l, -221l, -211l, -212l ] )

	}


	@DataSet("CallStepManagerServiceIT.dataset.xml")
	def "settings call step to nothing would remove parameter delegation from upstream test cases"(){
		given: 	"checking initial conditions"

		def initialTc1IsTrue = theTestCase(-1l).canAccessParameters ( [ -321l, -221l ] )
		def initialTc11IsTrue = theTestCase(-11l).canAccessParameters ( [-321l, -221l] )


		and : "the call step that will change that"

		def callstepId = -22l

		when : "changing the assignation mode of a step to NOTHING"
		callStepService.setParameterAssignationMode callstepId, ParameterAssignationMode.NOTHING, null

		then :

		initialTc1IsTrue
		initialTc11IsTrue

		theTestCase(-1l).canAccessParameters ( [] )
		theTestCase(-11l).canAccessParameters (  [] )
	}



	@DataSet("CallStepManagerServiceIT.dataset.xml")
	def "setting call step to parameter delegation should delegate more parameters to upstream test cases until a test case choose otherwise"(){

		given: 	"checking initial conditions"

		def initialTc1IsTrue = theTestCase(-1l).canAccessParameters ( [ -321l, -221l ] )
		def initialTc11IsTrue = theTestCase(-11l).canAccessParameters ( [-321l, -221l] )
		def initialTc21IsTrue = theTestCase(-21l).canAccessParameters ( [-211l, -212l] )


		and : "the call step that will change that"

		def callstepId = -31l

		when : "changing the assignation mode of a step to DELEGATE"
		callStepService.setParameterAssignationMode callstepId, ParameterAssignationMode.DELEGATE, null

		then :

		initialTc1IsTrue
		initialTc11IsTrue
		initialTc21IsTrue

		theTestCase(-1l).canAccessParameters ( [ -321l, -221l ]  )	// has not changed
		theTestCase(-11l).canAccessParameters (  [ -321l, -221l ]  ) // has not changed
		theTestCase(-21l).canAccessParameters (  [-211l, -212l, -311l, -312l, -313l] )	// has changed
	}



	def theTestCase = { id ->
		def bob = [
			tcId : id,
			canAccessParameters : { ids ->
				def params = paramService.findAllParameters(id)
				params.collect { it.id } as Set == ids as Set
			}
		]

		return bob
	}
}
