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
import org.squashtest.tm.service.internal.batchimport.excel.PropertySetter;
import org.squashtest.tm.service.internal.batchimport.excel.ReflectionFieldSetter;

/**
 * Setter for the action property of a test step. This handles both regular actions which has to be set on an
 * {@link ActionTestStep} and call reference which has to be set on a {@link TestCaseTarget}. <code>null</code> values
 * are considered as non-input optional value and are ignored.
 * 
 * @author Gregory Fouquet
 * 
 */
public class StepActionPropSetter extends StepPropSetter implements PropertySetter<String, Object> {
	public static final StepActionPropSetter INSTANCE = new StepActionPropSetter();

	private final ReflectionFieldSetter<String, ActionTestStep> actionSetter = ReflectionFieldSetter
			.forOptionalField("action");
	private final ReflectionFieldSetter<String, TestCaseTarget> pathSetter = ReflectionFieldSetter
			.forOptionalField("path");

	@Override
	protected void setOnTarget(String value, TestCaseTarget target) {
		pathSetter.set(value, target);
	}

	@Override
	protected void setOnStep(String value, ActionTestStep target) {
		actionSetter.set(value, target);
	}

}
