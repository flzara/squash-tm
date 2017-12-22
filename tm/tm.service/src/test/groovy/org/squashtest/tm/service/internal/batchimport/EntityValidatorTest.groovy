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

import static org.squashtest.tm.service.importer.ImportStatus.*
import static Existence.*

import org.squashtest.tm.domain.testcase.ActionTestStep
import org.squashtest.tm.domain.testcase.CallTestStep
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.domain.testcase.TestStep
import org.squashtest.tm.service.importer.ImportMode
import spock.lang.Specification
import spock.lang.Unroll

class EntityValidatorTest extends Specification {

	EntityValidator validator
	Model model
	ValidationFacilitySubservicesProvider modelProvider = Mock()

	static def longstring =	"IMAHMODERATELYLONGSTRINGOVERFIFTYCHARACTERSSOIFAILTESTSFORTESTCASEREFERENCES"

	static def toolongstring = "BOUHAHAHAHAHAHAAHAHAIAMAVERYLONGSTRINGOVERTWOHUNDREDANDFIFTYFIVECHARACTERSSOIFAILMANYTESTSOFSIZELIMITINLUCINGNAMESANDCUFSANDALLYOUNAMEIT"+
	"BOUHAHAHAHAHAHAAHAHAIAMAVERYLONGSTRINGOVERTWOHUNDREDANDFIFTYFIVECHARACTERSSOIFAILMANYTESTSOFSIZELIMITINLUCINGNAMESANDCUFSANDALLYOUNAMEIT"

	def setup(){
		validator = new EntityValidator(modelProvider)
		model = Mock()
		modelProvider.getModel() >> model
	}


	// ******************** test case checks **********************************

	def "should say that a test case is good for the service"(){

		given :
		TestCaseTarget target = new TestCaseTarget("/project/test-case")
		TestCase testCase = new TestCase(name:"test-case")

		and :
		model.getProjectStatus("project") >> projectTargetStatus(status: EXISTS, id: 10l,  testCaseLibraryId: 10l)

		when :
		LogTrain train = validator.updateTestCaseChecks(target, testCase)


		then :
		!train.hasCriticalErrors()
		train.entries == []
	}

	def projectTargetStatus(args) {
		def pts = new ProjectTargetStatus(args.status, args.testCaseLibraryId)
		pts.testCaseLibraryId = args.testCaseLibraryId
		return pts
	}



	@Unroll("modified test case at #path should produce a #status log because #humanMsg")
	def "test case at #path should produce a #status log because #humanmsg"(){

		given :
		model.getProjectStatus(_) >> {
			return (it[0] == "project") ?
			projectTargetStatus(status: EXISTS, id: 10l, testCaseLibraryId: 10L) :
			new ProjectTargetStatus(NOT_EXISTS)
		}

		when :
		LogTrain train = validator.updateTestCaseChecks(tar(path), testCase)


		then :

		train.entries.size() == 1

		def pb = train.entries[0]
		pb.status == status
		pb.i18nError == msg

		where :
		testCase										|	path						|	 status	|	msg								|	humanMsg
		tc(name:"test-case")							| 	"project/test-case"			|	FAILURE	|	Messages.ERROR_MALFORMED_PATH	|	"malformed path"
		tc(name:"")										|	"/project/whatever"			|	FAILURE	|	Messages.ERROR_FIELD_MANDATORY	|	"name is empty"
		tc(name:"test-case")							|	"/unknown/test-case"		|	FAILURE	|	Messages.ERROR_PROJECT_NOT_EXIST|	"project doesn't exists"
		tc(name:toolongstring)							|	"/project/"+toolongstring	|	WARNING	|	Messages.ERROR_MAX_SIZE			|	"name is too long"
		tc(name:"test-case", reference:longstring)		|	"/project/test-case"		|	WARNING	|	Messages.ERROR_MAX_SIZE			|	"ref is too long"

	}

	@Unroll("new test case at #path should produce a #status log because #humanMsg")
	def "same name as above but slightly different"(){

		given :
		model.getProjectStatus(_) >> {
			return (it[0] == "project") ?
			projectTargetStatus(status: EXISTS, id: 10l, testCaseLibraryId: 10L) :
			new ProjectTargetStatus(NOT_EXISTS)
		}

		when :
		LogTrain train = validator.createTestCaseChecks(tar(path), testCase)


		then :

		train.entries.size() == 1

		def pb = train.entries[0]
		pb.status == status
		pb.i18nError == msg

		where :
		testCase										|	path						|	 status	|	msg								|	humanMsg
		tc(name:"test-case")							| 	"project/test-case"			|	FAILURE	|	Messages.ERROR_MALFORMED_PATH	|	"malformed path"
		tc(name:"test-case")							|	"/unknown/test-case"		|	FAILURE	|	Messages.ERROR_PROJECT_NOT_EXIST|	"project doesn't exists"
		tc(name:toolongstring)							|	"/project/"+toolongstring	|	WARNING	|	Messages.ERROR_MAX_SIZE			|	"name is too long"
		tc(name:"test-case", reference:longstring)		|	"/project/test-case"		|	WARNING	|	Messages.ERROR_MAX_SIZE			|	"ref is too long"

	}

	@Unroll("new test case at #path should not produce log")
	def "new test case at #path should not produce log"(){

		given :
		model.getProjectStatus(_) >> {
			return (it[0] == "project") ?
			projectTargetStatus(status: EXISTS, id: 10l, testCaseLibraryId: 10L) :
			new ProjectTargetStatus(NOT_EXISTS)
		}

		when :
		LogTrain train = validator.createTestCaseChecks(tar(path), testCase)


		then :
		!train.hasCriticalErrors()
		train.entries.size() == 0

		where :
		testCase										|	path
		tc(name:"")										|	"/project/blank name"
		tc(name:null)									|	"/project/null name"

	}

	// ******************** test steps checks **********************************

	def "should say that this test step is good for the service"(){

		given :
		TestStepTarget target  = new TestStepTarget(new TestCaseTarget("/project/test-case"), 2)
		TestStep astep = new ActionTestStep(action:"ready for action", expectedResult : "expected")

		and :
		model.getProjectStatus("project") >> projectTargetStatus(status: EXISTS, id: 10l, testCaseLibraryId: 10L)

		and :
		model.getStatus(_) >> new TargetStatus(EXISTS, 10l)

		when :
		LogTrain train = validator.basicTestStepChecks(target, astep)


		then :
		!train.hasCriticalErrors()
		train.entries == []

	}

	@Unroll("should say nay to an action step because #humanmsg")
	def "should say nay for action step for various reasons"(){

		given :
		model.getProjectStatus(_) >> {
			return (it[0] == "project") ?
			projectTargetStatus(status: EXISTS, id: 10l, testCaseLibraryId: 10L) :
			new ProjectTargetStatus(NOT_EXISTS)
		}

		and :
		model.getStatus(_) >> {
			return (it[0].path ==~ /.*test-case$/) ?
			new TargetStatus(EXISTS, 10l) :
			new TargetStatus(NOT_EXISTS, null)
		}
		when :
		LogTrain train = validator.basicTestStepChecks(target, astep)


		then :

		train.entries.size() == 1

		def pb = train.entries[0]
		pb.status == status
		pb.i18nError == msg

		where :
		astep					|	target									|	 status	|	msg								|	humanmsg
		ast(action:"action")	| 	steptarget("project/test-case", null)	|	FAILURE	|	Messages.ERROR_MALFORMED_PATH	|	"malformed path"
		ast(action:"action")	|	steptarget("/project/whatever", null)	|	FAILURE	|	Messages.ERROR_TC_NOT_FOUND		|	"test case doesn't exist"
		ast(action:"action")	|	steptarget("/unknown/test-case", null)	|	FAILURE	|	Messages.ERROR_PROJECT_NOT_EXIST|	"project doesn't exists"

	}

	@Unroll("should say that call step is fine  #humanmsg")
	def "should say that call step is fine"(){

		given :
		TestStepTarget target  = new TestStepTarget(new TestCaseTarget("/project/test-case"), 2)
		TestCaseTarget called = new TestCaseTarget("/project/called")
		TestStep cstep = new CallTestStep()
		CallStepParamsInfo info = new CallStepParamsInfo()

		and :
		model.getStatus(_) >> new TargetStatus(EXISTS, 10l)
		model.wouldCreateCycle(_) >> false



		when :
		LogTrain train = validator.validateCallStep(target, cstep, called, info, mode)


		then :
		!train.hasCriticalErrors()
		train.entries == []

		where :
		humanmsg |mode
		"CREATE" |ImportMode.CREATE
		"UPDATE" |ImportMode.UPDATE
	}


	@Unroll("should say nay to call step because #humanmsg")
	def "should say nay to call step for various reasons"(){

		given :
		model.getStatus(_) >> calledstatus
		model.wouldCreateCycle(_, _) >> calledcycle

		and :
		def target = steptarget("/project/test-case", 5)
		def called = tar("/project/autre")
		def cstep = cst()
		CallStepParamsInfo info = new CallStepParamsInfo()


		when :
		LogTrain train = validator.validateCallStep(target, cstep, called, info, mode)

		then :

		train.entries.size() == 1

		def pb = train.entries[0]
		pb.status == status
		pb.i18nError == msg
		pb.i18nImpact == impct

		where :
		mode              |calledstatus 				|	calledcycle		|	status	|	msg									|  impct                              |	humanmsg
		ImportMode.UPDATE |status(NOT_EXISTS,null)		|	false			|	FAILURE	|	Messages.ERROR_CALLED_TC_NOT_FOUND	|  null                               |	"UPDATE : called test doesn't exist"
		ImportMode.UPDATE |status(EXISTS, 12l)			|	true			|	FAILURE	|	Messages.ERROR_CYCLIC_STEP_CALLS	|  null                               |	"UPDATE : such calls would induce cycles"
		ImportMode.CREATE |status(NOT_EXISTS,null)		|	false			|	WARNING	|	Messages.ERROR_CALLED_TC_NOT_FOUND	|  Messages.IMPACT_CALL_AS_ACTION_STEP|	"CREATE : called test doesn't exist"
		ImportMode.CREATE |status(EXISTS, 12l)			|	true			|	FAILURE	|	Messages.ERROR_CYCLIC_STEP_CALLS	|  null                               |	"CREATE :such calls would induce cycles"

	}


	// ******************** parameter checks **********************************


	def "should say that the parameter is good to go"(){

		given :
		ParameterTarget target = ptarget("/project/test-case", "param")

		and :
		model.getStatus(_) >> status(EXISTS, 10l)
		model.getProjectStatus("project") >> projectTargetStatus(status: EXISTS, id: 15l, testCaseLibraryId: 15L)

		when :
		LogTrain train = validator.basicParameterChecks(target)

		then :
		train.hasCriticalErrors() == false
		train.entries == []

	}


	@Unroll("should say nay to a parameter because #humanmsg")
	def "should reject the parameter for various reasons"(){

		given :
		model.getStatus(_) >> {
			return (it[0].path ==~ /.*test-case$/) ?
			new TargetStatus(EXISTS, 10l) :
			new TargetStatus(NOT_EXISTS, null)
		}

		model.getProjectStatus(_) >> {
			return (it[0] == "project") ?
			projectTargetStatus(status: EXISTS, id: 10l, testCaseLibraryId: 10L) :
			new ProjectTargetStatus(NOT_EXISTS)
		}


		when :
		LogTrain train = validator.basicParameterChecks(target)

		then :
		System.out.println(humanmsg)
		train.entries.each { println it.i18nError }

		train.entries.size() == 1

		def pb = train.entries[0]
		pb.status == status
		pb.i18nError == msg

		where :
		target 										|	status	|	msg									|	humanmsg
		ptarget("project/test-case", "param")		|	FAILURE	|	Messages.ERROR_MALFORMED_PATH		|	"malformed path"
		ptarget("/project/unknown", "param")		|	FAILURE	|	Messages.ERROR_PARAMETER_OWNER_NOT_FOUND	|	"test case doesn't exists"
		ptarget("/unknown/test-case", "param")		|	FAILURE	|	Messages.ERROR_PROJECT_NOT_EXIST	|	"project doesn't exists"
		ptarget("/project/test-case", "")			|	FAILURE	|	Messages.ERROR_FIELD_MANDATORY		|	"param has no name"
		ptarget("/project/test-case", "#??%")		|	FAILURE	|	Messages.ERROR_PARAMETER_CONTAINS_FORBIDDEN_CHARACTERS		|	"param contains forbidden characters"
		ptarget("/project/test-case", toolongstring)|	WARNING	|	Messages.ERROR_MAX_SIZE				|	"param name is too long"


	}



	// ******************** datasets checks **********************************

	@Unroll("should say nay to a dataset because #humanmsg")
	def "say nay to dataset for various reasons"(){
		given :
		model.getStatus(_) >> {
			return (it[0].path ==~ /.*test-case$/) ?
			new TargetStatus(EXISTS, 10l) :
			new TargetStatus(NOT_EXISTS, null)
		}

		model.getProjectStatus(_) >> {
			return (it[0] == "project") ?
			projectTargetStatus(status: EXISTS, id: 10l, testCaseLibraryId: 10L) :
			new ProjectTargetStatus(NOT_EXISTS)
		}


		when :
		LogTrain train = validator.basicDatasetCheck(target)

		then :
		System.out.println(humanmsg)
		train.entries.each { println it.i18nError }

		train.entries.size() == 1

		def pb = train.entries[0]
		pb.status == status
		pb.i18nError == msg

		where :
		target 											|	status	|	msg									|	humanmsg
		dstarget("owner/test-case", "ds")				|	FAILURE	|	Messages.ERROR_MALFORMED_PATH		|	"malformed path"
		dstarget("/project/unknown", "ds")				|	FAILURE	|	Messages.ERROR_TC_NOT_FOUND			|	"test case doesn't exists"
		dstarget("/unknown/test-case", "ds")			|	FAILURE	|	Messages.ERROR_PROJECT_NOT_EXIST	|	"project doesn't exists"
		dstarget("/project/test-case", "")				|	FAILURE	|	Messages.ERROR_FIELD_MANDATORY		|	"dataset has no name"
		dstarget("/project/test-case", toolongstring)	|	WARNING	|	Messages.ERROR_MAX_SIZE				|	"dataset name is too long"

	}


	// ******************** utils *****************************

	def status(ex, id){
		return new TargetStatus(ex, id)
	}

	def tc(args){
		return new TestCase(args)
	}

	def cst(){
		return new CallTestStep()
	}

	def ast(args){
		return new ActionTestStep(args)
	}

	def tar(arg){
		return new TestCaseTarget(arg)
	}

	def steptarget(path,idx){
		return new TestStepTarget(tar(path), idx)
	}

	def ptarget(path, name){
		return new ParameterTarget(tar(path), name)
	}

	def dstarget(path, name){
		return new DatasetTarget(tar(path), name)
	}

}
