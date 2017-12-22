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
package org.squashtest.tm.web.internal.controller.testcase

import org.springframework.context.MessageSource
import org.squashtest.tm.tools.unittest.reflection.ReflectionCategory
import org.squashtest.tm.domain.attachment.AttachmentList
import org.squashtest.tm.domain.testcase.*
import org.squashtest.tm.web.internal.controller.testcase.steps.TestStepsTableModelBuilder
import spock.lang.Specification

class TestStepsTableModelBuilderTest extends Specification {
	MessageSource messageSource = Mock()
	Locale locale = Locale.FRENCH
	TestStepsTableModelBuilder builder = new TestStepsTableModelBuilder()

	def "Should build model for an ActionTestStep"() {
		given:
		ActionTestStep step = new ActionTestStep(action: "action", expectedResult: "expected")
		int stepIndex = 0

		use(ReflectionCategory) {
			TestStep.set field: 'id', of: step, to: 10L
			AttachmentList.set field: 'id', of: step.attachmentList, to: 100L
		}

		when:
		def data = builder.buildItemData(step);

		then:

		data == [
			"step-id":10L,
			"empty-browse-holder":null,
			"customFields":[:],
			"nb-attachments":0,
			"empty-requirements-holder":null,
			"nb-requirements":0,
			"step-index":0,
			"step-type": "action",
			"attach-list-id":100L,
			"step-result": "expected",
			"has-requirements":false,
			"call-step-info":null,
			"empty-delete-holder":null,
			"step-action": "action"
		]



	}

	def "Should build model for a CallTestStep"() {
		given:
		TestCase callee = new TestCase(name: "callee")
		CallTestStep step = new CallTestStep(calledTestCase: callee)
		Dataset ds = new Dataset(name : "dataset")
		ds.testCase = callee
		callee.addDataset ds

		int stepIndex = 0

		use(ReflectionCategory) {
			TestCaseLibraryNode.set field: "id", of: callee, to: 100L
			TestStep.set field: 'id', of: step, to: 10L
			Dataset.set field : 'id', of: ds, to : 5L
		}

		step.delegateParameterValues = false
		step.calledDataset = ds

		when:
		def data = builder.buildItemData(step);

		then:

		data == ["step-id":10L,
			"empty-browse-holder":null,
			"customFields":[:],
			"nb-attachments":null,
			"empty-requirements-holder":null,
			"nb-requirements":null,
			"step-index":0,
			"step-type":"call",
			"attach-list-id":null,
			"step-result":null,
			"has-requirements":false,
			"call-step-info": data["call-step-info"],	// that one will be tested later
			"empty-delete-holder":null,
			"step-action": null
		]

		data["call-step-info"].calledTcId == 100L
		data["call-step-info"].calledTcName == "callee"
		data["call-step-info"].calledDatasetId == 5L
		data["call-step-info"].calledDatasetName == "dataset"
		data["call-step-info"].paramMode == "CALLED_DATASET"
	}
}
