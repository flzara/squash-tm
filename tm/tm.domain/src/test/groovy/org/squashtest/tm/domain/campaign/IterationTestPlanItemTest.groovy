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
package org.squashtest.tm.domain.campaign

import org.squashtest.tm.domain.campaign.Iteration
import org.squashtest.tm.domain.campaign.IterationTestPlanItem
import org.squashtest.tm.domain.execution.Execution
import org.squashtest.tm.domain.execution.ExecutionStatus
import org.squashtest.tm.domain.testcase.Dataset;
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.domain.users.User
import org.squashtest.tm.exception.CyclicStepCallException;
import org.squashtest.tm.exception.execution.TestPlanItemNotExecutableException;

import spock.lang.Specification
import spock.lang.Unroll;

public class IterationTestPlanItemTest extends Specification {
	IterationTestPlanItem copySource = new IterationTestPlanItem(iteration : Mock(Iteration), executionStatus: ExecutionStatus.FAILURE, label: "copy source")

	def setup() {
		copySource.referencedTestCase= new TestCase()

		Execution exec = new Execution()
		copySource.addExecution(exec)
	}


	def "copy of a test plan item should be in ready state"() {
		when:
		IterationTestPlanItem copy = copySource.createCopy()

		then:
		copy.executionStatus == ExecutionStatus.READY
	}

	def "copy of a test plan item should have no execution"() {
		when:
		IterationTestPlanItem copy = copySource.createCopy()

		then:
		copy.executions.isEmpty()
	}

	def "copy of a test plan item should reference the same test case"() {
		when:
		IterationTestPlanItem copy = copySource.createCopy()

		then:
		copy.referencedTestCase == copySource.referencedTestCase
	}

	def "copy of a test plan item should have the same label"() {
		when:
		IterationTestPlanItem copy = copySource.createCopy()

		then:
		copy.label == copySource.label
	}
	def "copy of a test plan item should copy the assigned user"() {
		given:
		User user = new User()
		copySource.setUser(user)
		when:
		IterationTestPlanItem copy = copySource.createCopy()

		then:
		copy.user == copySource.getUser()
	}

	def "should copying a  test plan item should not modify the source"() {
		given:
		IterationTestPlanItem source = new IterationTestPlanItem(iteration : Mock(Iteration), executionStatus: ExecutionStatus.FAILURE, label: "label")
		TestCase referencedTestCase = new TestCase()
		source.referencedTestCase= referencedTestCase

		Execution exec = new Execution()
		source.addExecution(exec)

		when:
		source.createCopy()

		then:
		source.label == "label"
		source.referencedTestCase == referencedTestCase
		source.executions == [exec]
	}

	def "copy should preserve parameterization data"() {
		given:
		Dataset dataset = Mock(Dataset)
		copySource.referencedDataset = dataset 

		when:
		IterationTestPlanItem copy = copySource.createCopy()

		then:
		copy.referencedDataset == dataset
	}

	def "should not add an execution if not executable"() {
		given:
		IterationTestPlanItem item = new IterationTestPlanItem()

		when:
		item.createExecution()

		then:
		thrown(TestPlanItemNotExecutableException)
	}

	def "item without test case should not be executable through suite"() {
		given:
		IterationTestPlanItem item = new IterationTestPlanItem()

		expect:
		!item.isExecutableThroughTestSuite()
	}

	def "item without running execution should not be executable through suite"() {
		given:
		TestCase testCase = Mock()
		IterationTestPlanItem item = new IterationTestPlanItem(testCase)

		and:
		Execution exec = Mock()
		exec.executionStatus >> ExecutionStatus.FAILURE
		item.executions << exec

		expect:
		!item.isExecutableThroughTestSuite()
	}

	def "item with running execution should not executable through suite"() {
		given:
		TestCase testCase = Mock()
		IterationTestPlanItem item = new IterationTestPlanItem(testCase)

		and:
		Execution exec = Mock()
		exec.executionStatus >> ExecutionStatus.RUNNING
		item.executions << exec

		expect:
		!item.isExecutableThroughTestSuite()
	}

	@Unroll
	def "should create items for given test case and datasets #datasets"() {
		given:
		TestCase testCase = Mock()

		when:
		Collection<IterationTestPlanItem> testPlan = IterationTestPlanItem.createTestPlanItems(testCase, datasets);
		def expectedSize = datasets ? datasets.size() : 1

		then:
		testPlan.size() == expectedSize
		testPlan*.referencedTestCase.inject(true) { res, it -> res && it == testCase } // all refd tesst cases should be testCase
		testPlan*.referencedDataset.containsAll(datasets ? datasets : [])

		where:
		datasets << [
			null,
			[],
			[Mock(Dataset)],
			[
				Mock(Dataset),
				Mock(Dataset)
			]
		]
	}
}
