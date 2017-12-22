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
package org.squashtest.tm.domain.execution;

import org.squashtest.tm.core.foundation.exception.NullArgumentException
import org.squashtest.tm.domain.infolist.InfoList;
import org.squashtest.tm.domain.infolist.ListItemReference;
import org.squashtest.tm.domain.infolist.UserListItem;
import org.squashtest.tm.domain.testcase.ActionTestStep
import org.squashtest.tm.domain.testcase.CallTestStep
import org.squashtest.tm.domain.testcase.Dataset
import org.squashtest.tm.domain.testcase.DatasetParamValue
import org.squashtest.tm.domain.testcase.Parameter
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.domain.testcase.TestCaseImportance
import org.squashtest.tm.domain.testcase.TestCaseStatus

import spock.lang.Specification

class ExecutionTest extends Specification {
	def "should copy test steps as execution steps"() {
		given :
		Execution execution = new Execution()
		ActionTestStep ts1 = new ActionTestStep(action:"action1",expectedResult:"result1")
		ActionTestStep ts2 = new ActionTestStep(action:"action2",expectedResult:"result2")
		ActionTestStep ts3 = new ActionTestStep(action:"action3",expectedResult:"result3")

		when :
		ExecutionStep exs1 = new ExecutionStep(ts1, null)
		ExecutionStep exs2 = new ExecutionStep(ts2, null)
		ExecutionStep exs3 = new ExecutionStep(ts3, null)

		execution.addStep(exs1)
		execution.addStep(exs2)
		execution.addStep(exs3)

		List<ExecutionStep> list = execution.getSteps();

		then :
		list.collect { it.action } == [
			"action1",
			"action2",
			"action3"
		]
		list.collect { it.expectedResult } == [
			"result1",
			"result2",
			"result3"
		]
		list.collect { it.executionStatus } == [
			ExecutionStatus.READY,
			ExecutionStatus.READY,
			ExecutionStatus.READY
		]
	}

	def "should not find first unexecuted step because has no steps"(){
		given : Execution execution = new Execution()
		when : ExecutionStep executionStep = execution.findFirstUnexecutedStep()
		then :
		executionStep == null
	}

	def "should not find first unexecuted step because all executed"(){
		given : Execution execution = new Execution()
		ExecutionStep step1 = new ExecutionStep()
		step1.setExecutionStatus ExecutionStatus.SUCCESS
		execution.addStep step1
		ExecutionStep step2 = new ExecutionStep()
		step2.setExecutionStatus ExecutionStatus.FAILURE
		execution.addStep step2
		ExecutionStep step3 = new ExecutionStep()
		step3.setExecutionStatus ExecutionStatus.BLOCKED
		execution.addStep step3

		when :def executionStep = execution.findFirstUnexecutedStep()
		then :
		executionStep == null
	}

	def "should find first unexecuted step "(){
		given : Execution execution = new Execution()
		ExecutionStep step1 = new ExecutionStep()
		step1.setExecutionStatus ExecutionStatus.SUCCESS
		execution.addStep step1
		ExecutionStep step2 = new ExecutionStep()
		step2.setExecutionStatus ExecutionStatus.READY
		execution.addStep step2
		ExecutionStep step3 = new ExecutionStep()
		step3.setExecutionStatus ExecutionStatus.BLOCKED
		execution.addStep step3

		when :
		def executionStep = execution.findFirstUnexecutedStep()

		then :
		executionStep == step2
	}

	def "should create a valid execution from a test case without prerequisite"() {
		given:
		TestCase testCase = Mock()
		testCase.name >> "peter parker"
		testCase.steps >> []
		testCase.allAttachments >> []
		testCase.importance >> TestCaseImportance.LOW
		testCase.nature >> new UserListItem(code:"SOME_NATURE", infoList:Mock(InfoList))
		testCase.type >> new UserListItem(code:"SOME_TYPE", infoList:Mock(InfoList))
		testCase.status >> TestCaseStatus.WORK_IN_PROGRESS
		testCase.getDatasets() >> []

		when:
		Execution res = new Execution(testCase)

		then:
		notThrown NullArgumentException

	}

	// *************** DATASET TESTS ***************************

	def "should create the execution steps with the correct parameter values according to which dataset is used"(){

		given: "a simple test case b"

		TestCase tcB = new TestCase(name:"b")
		tcB.addStep new ActionTestStep(action:"\${login} / \${password}")

		def p1 = addParameter("login", tcB)
		def p2 = addParameter("password", tcB)

		def ds1 = addDataset("TCB-Dataset1", tcB, [ (p1) :"spongebob", (p2) :"glouglouglou"])
		def ds2 = addDataset("TCB-Dataset2", tcB, [ (p1) : "mclane", (p2) : "yippeekaiyay"])


		and : "some test case A calls it"

		TestCase tcA = new TestCase(name:"a",
		nature : new ListItemReference(code:"SOME_NATURE", infoList : Mock(InfoList)),
		type : new ListItemReference(code:"SOME_TYPE", infoList : Mock(InfoList))
		)

		tcA.addStep newCallStep(tcB,ds1, false)
		tcA.addStep newCallStep(tcB,ds2, false)
		tcA.addStep newCallStep(tcB, null, true)
		tcA.addStep newCallStep(tcB, null, false)

		def dsA = addDataset ("TCADS", tcA, [(p1) : "toxie", (p2) : "mopavenger"])

		when :

		Execution execWithDS = new Execution(tcA, dsA)
		Execution execNoDS = new Execution(tcA)

		then :

		def stepsWithDS = execWithDS.steps.collect{ it.action }.join(", ")

		println stepsWithDS
		stepsWithDS == "spongebob / glouglouglou, mclane / yippeekaiyay, toxie / mopavenger, &lt;no_value&gt; / &lt;no_value&gt;"

		execNoDS.steps.collect { it.action }.join(", ") ==
		"spongebob / glouglouglou, mclane / yippeekaiyay, &lt;no_value&gt; / &lt;no_value&gt;, &lt;no_value&gt; / &lt;no_value&gt;"

	}		
	
	Parameter addParameter(String name, TestCase tc){
		def p = new Parameter(name)
		tc.addParameter p
		p
	}

	Dataset addDataset (String dsname, TestCase tc, Map<Parameter, String> paramvalues){
		Dataset ds = new Dataset(dsname, tc);
		paramvalues.each {k, v ->  ds.addParameterValue(new DatasetParamValue(k, ds, v)) }
		tc.addDataset ds
		ds
	}

	CallTestStep newCallStep (TestCase tc, Dataset ds, Boolean delegate){
		new CallTestStep(calledTestCase:tc, calledDataset:ds, delegateParameterValues:delegate)
	}

}
