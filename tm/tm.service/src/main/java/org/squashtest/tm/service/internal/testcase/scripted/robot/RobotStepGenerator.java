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
package org.squashtest.tm.service.internal.testcase.scripted.robot;

import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStep;
import org.squashtest.tm.domain.testcase.ScriptedTestCaseExtender;

/**
 * A class that generates SquashTM execution steps from a Robot script.
 */
public class RobotStepGenerator {

	public void populateExecution(Execution execution, ScriptedTestCaseExtender scriptExtender) {
		ExecutionStep newExecutionStep = new ExecutionStep();
		String script = scriptExtender.getScript();

		// There is no Robot parser for the moment so we just replace the carriage returns
		newExecutionStep.setAction(script.replace("\r\n", "<br/>"));

		execution.getSteps().add(newExecutionStep);
	}

}
