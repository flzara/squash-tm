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
package org.squashtest.tm.web.internal.controller.execution;

import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.testautomation.AutomatedExecutionExtender;
import org.squashtest.tm.domain.testautomation.AutomatedTest;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.domain.testautomation.TestAutomationServer;
import org.squashtest.tm.web.internal.controller.execution.AutomatedExecutionViewUtils.ExecutionAutoView;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;

import spock.lang.Specification;
import spock.lang.Unroll;

/**
 * @author Gregory Fouquet
 *
 */
class AutomatedExecutionViewUtilsTest extends Specification {
	InternationalizationHelper  i18n = Mock()

	@Unroll
	def "percent progression should be #prog"() {
		expect: AutomatedExecutionViewUtils.percentProgression(part, total) == prog
		where:
		part	| total	| prog
		0		| 0		| 100
		1		| 4		| 25
		2		| 4		| 50
		3		| 4		| 75
		4		| 4		| 100
	}

	def "should create populated ExecutionAutoView"() {
		given:
		AutomatedExecutionExtender autoExec = Mock()
		autoExec.nodeName >> "verser"

		and:
		Execution exec = Mock()
		exec.id >> 50
		exec.name >> "ordeal"
		exec.executionStatus >> ExecutionStatus.SUCCESS
		autoExec.execution >> exec

		and:
		TestAutomationProject tap = Mock()
		tap.label >> "drips drips drips drips"
		autoExec.automatedProject >> tap

		when:
		ExecutionAutoView res = AutomatedExecutionViewUtils.translateExecutionInView(autoExec, Locale.JAPAN, i18n);

		then:
		res.automatedProject == "drips drips drips drips"
		res.node == "verser"
		res.id == 50
		res.name == "ordeal"
		res.status == ExecutionStatus.SUCCESS

	}
}
