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

import org.squashtest.tm.domain.testcase.ActionTestStep;

public class ActionStepInstruction extends StepInstruction {

	private final ActionTestStep testStep;

	public ActionStepInstruction(TestStepTarget target, ActionTestStep actionTestStep) {
		super(target);
		this.testStep = actionTestStep;
	}

	public ActionTestStep getTestStep() {
		return testStep;
	}

	/**
	 * @see org.squashtest.tm.service.internal.batchimport.Instruction#executeUpdate(org.squashtest.tm.service.internal.batchimport.Facility)
	 */
	@Override
	protected LogTrain executeUpdate(Facility facility) {
		return facility.updateActionStep(getTarget(), testStep, getCustomFields());
	}

	/**
	 * @see org.squashtest.tm.service.internal.batchimport.Instruction#executeCreate(org.squashtest.tm.service.internal.batchimport.Facility)
	 */
	@Override
	protected LogTrain executeCreate(Facility facility) {
		return facility.addActionStep(getTarget(), testStep, getCustomFields());
	}
}
