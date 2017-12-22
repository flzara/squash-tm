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

public class CallStepInstruction extends StepInstruction {

	private final TestCaseTarget calledTC;
	private final ActionTestStep actionStepBackup;
	private CallStepParamsInfo datasetInfo;


	public CallStepInstruction(TestStepTarget target, TestCaseTarget calledTestCase, ActionTestStep actionStepBackup, CallStepParamsInfo datasetInfo) {
		super(target);
		this.calledTC = calledTestCase;
		this.actionStepBackup = actionStepBackup;
		this.datasetInfo = datasetInfo;
	}

	public TestCaseTarget getCalledTC() {
		return calledTC;
	}
	public ActionTestStep getActionStepBackup() {
		return actionStepBackup;
	}
	/**
	 * @see org.squashtest.tm.service.internal.batchimport.Instruction#executeUpdate(org.squashtest.tm.service.internal.batchimport.Facility)
	 */
	@Override
	protected LogTrain executeUpdate(Facility facility) {
		return facility.updateCallStep(getTarget(), null, calledTC, datasetInfo, actionStepBackup);
	}

	/**
	 * @see org.squashtest.tm.service.internal.batchimport.Instruction#executeCreate(org.squashtest.tm.service.internal.batchimport.Facility)
	 */
	@Override
	protected LogTrain executeCreate(Facility facility) {
		return facility.addCallStep(getTarget(), null, calledTC, datasetInfo, actionStepBackup);
	}


	public CallStepParamsInfo getDatasetInfo() {
		return datasetInfo;
	}


	public void setDatasetInfo(CallStepParamsInfo datasetInfo) {
		this.datasetInfo = datasetInfo;
	}

}
