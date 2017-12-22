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
package org.squashtest.tm.service.internal.batchimport;

import java.util.HashMap;
import java.util.Map;

/**
 * Generic instruction for test steps.
 * 
 * @author Gregory Fouquet
 * 
 */
public abstract class StepInstruction extends Instruction<TestStepTarget> implements CustomFieldHolder {
	private final Map<String, String> customFields = new HashMap<>();

	protected StepInstruction(TestStepTarget target) {
		super(target);
	}

	/**
	 * @return the customFields
	 */
	public Map<String, String> getCustomFields() {
		return customFields;
	}

	@Override
	public void addCustomField(String code, String value) {
		customFields.put(code, value);
	}

	/**
	 * @see org.squashtest.tm.service.internal.batchimport.Instruction#executeDelete(org.squashtest.tm.service.internal.batchimport.Facility)
	 */
	@Override
	protected LogTrain executeDelete(Facility facility) {
		return facility.deleteTestStep(getTarget());
	}
}