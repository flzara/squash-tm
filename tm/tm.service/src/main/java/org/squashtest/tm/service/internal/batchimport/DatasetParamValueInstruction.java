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


import javax.validation.constraints.NotNull;

public class DatasetParamValueInstruction extends Instruction<DatasetTarget> {

	private final DatasetValue datasetValue;

	public DatasetParamValueInstruction(@NotNull DatasetTarget target, @NotNull DatasetValue datasetValue) {
		super(target);
		this.datasetValue = datasetValue;
	}


	/**
	 * @return the datasetParamValue
	 */
	public DatasetValue getDatasetValue() {
		return datasetValue;
	}

	/**
	 * @see org.squashtest.tm.service.internal.batchimport.Instruction#executeUpdate(org.squashtest.tm.service.internal.batchimport.Facility)
	 */
	@Override
	protected LogTrain executeUpdate(Facility facility) {
		ParameterTarget parameterTarget = new ParameterTarget();
		setParameterOwnerPath(parameterTarget);
		parameterTarget.setName(datasetValue.getParameterName());

		return facility.failsafeUpdateParameterValue(getTarget(), parameterTarget, datasetValue.getValue(), true);
	}

	/**
	 * @see org.squashtest.tm.service.internal.batchimport.Instruction#executeDelete(org.squashtest.tm.service.internal.batchimport.Facility)
	 */
	@Override
	protected LogTrain executeDelete(Facility facility) {

		/*
		 * NOOP : As for TM 1.11.0 the DATASET sheet is now split in two phases : the datasets and the parameter values
		 * are now treated in two distinct phases. Please see Facility#ENTITIES_ORDERED_BY_INSTRUCTION_ORDER to see
		 * how.
		 *
		 * In particular the deletion of a dataset now happens in the DATASET phase so we don't double process it in the
		 * param value phase.
		 */
		return new LogTrain();
	}

	/**
	 * @see org.squashtest.tm.service.internal.batchimport.Instruction#executeCreate(org.squashtest.tm.service.internal.batchimport.Facility)
	 */
	@Override
	protected LogTrain executeCreate(Facility facility) {
		ParameterTarget parameterTarget = new ParameterTarget();
		setParameterOwnerPath(parameterTarget);
		parameterTarget.setName(datasetValue.getParameterName());

		return facility.failsafeUpdateParameterValue(getTarget(), parameterTarget, datasetValue.getValue(), false);
	}


	private void setParameterOwnerPath(ParameterTarget parameterTarget) {
		String parameterOwnerPath = datasetValue.getParameterOwnerPath();
		if(parameterOwnerPath == null){
			parameterOwnerPath = getTarget().getTestCase().getPath();
		}
		parameterTarget.setPath(parameterOwnerPath);
	}


}
