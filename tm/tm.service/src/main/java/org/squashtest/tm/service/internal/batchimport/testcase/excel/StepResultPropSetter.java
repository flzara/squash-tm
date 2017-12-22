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
package org.squashtest.tm.service.internal.batchimport.testcase.excel;

import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.service.internal.batchimport.TestCaseTarget;
import org.squashtest.tm.service.internal.batchimport.excel.ReflectionFieldSetter;

/**
 * Setter for the expected result property of a test step. This handles both {@link ActionTestStep}s, which expected
 * result prop is set with the given value, and {@link TestCaseTarget}s, in which case the value is ignored.
 * 
 * @author Gregory Fouquet
 * 
 */
public class StepResultPropSetter extends StepPropSetter {
	public static final StepResultPropSetter INSTANCE = new StepResultPropSetter();

	private final ReflectionFieldSetter<String, ActionTestStep> resultSetter = ReflectionFieldSetter.forOptionalField("expectedResult");

	/**
	 * @see org.squashtest.tm.service.internal.batchimport.testcase.excel.StepPropSetter#setOnStep(java.lang.String, org.squashtest.tm.domain.testcase.ActionTestStep)
	 */
	@Override
	protected void setOnStep(String value, ActionTestStep target) {
		resultSetter.set(value, target);
	}

	/**
	 * @see org.squashtest.tm.service.internal.batchimport.testcase.excel.StepPropSetter#setOnTarget(java.lang.String, org.squashtest.tm.service.internal.batchimport.TestCaseTarget)
	 */
	@Override
	protected void setOnTarget(String value, TestCaseTarget target) {
		// NOOP

	}



}
