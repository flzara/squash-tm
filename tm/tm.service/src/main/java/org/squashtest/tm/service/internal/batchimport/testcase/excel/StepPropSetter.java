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
import org.squashtest.tm.service.internal.batchimport.CallStepInstruction;
import org.squashtest.tm.service.internal.batchimport.TestCaseTarget;
import org.squashtest.tm.service.internal.batchimport.excel.PropertySetter;

/**
 * Superclass for property setters which handle both {@link ActionTestStep} and {@link TestCaseTarget}.
 * 
 * @author Gregory Fouquet
 * 
 */
public abstract class StepPropSetter implements PropertySetter<String, Object> {
	public StepPropSetter() {
		super();
	}

	protected abstract void setOnStep(String value, ActionTestStep target);

	protected abstract void setOnTarget(String value, TestCaseTarget target);

	/**
	 * @see org.squashtest.tm.service.internal.batchimport.excel.PropertySetter#set(java.lang.Object, java.lang.Object)
	 */
	@Override
	public final void set(String value, Object target) {
		if (target instanceof ActionTestStep) {
			setOnStep(value, (ActionTestStep) target);
			return;
		}
		if (target instanceof CallStepInstruction) {
			if (value != null){
				CallStepInstruction targetCSI = (CallStepInstruction) target;
				setOnStep(value, targetCSI.getActionStepBackup());
				String path = value.replaceFirst("^[cC][aA][lL][lL]\\s*", "");
				setOnTarget(path, targetCSI.getCalledTC());
			}
			return;
		}
		throw new IllegalArgumentException("Target of type " + target.getClass().getSimpleName()
				+ " is illicit. It should either be ActionTestStep or CallStepInstruction");
	}

}