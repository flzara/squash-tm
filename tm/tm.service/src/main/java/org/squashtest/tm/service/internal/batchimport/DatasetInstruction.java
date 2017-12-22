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

/**
 * As for 1.11.0 a DatasetInstruction just does handle datasets. The parameter values are handled by another kind of instruction
 * (see {@link DatasetParamValueInstruction} )
 * 
 * @author bsiri
 *
 */
public class DatasetInstruction extends Instruction<DatasetTarget> {


	public DatasetInstruction(@NotNull DatasetTarget target) {
		super(target);
	}


	/**
	 * @see org.squashtest.tm.service.internal.batchimport.Instruction#executeUpdate(org.squashtest.tm.service.internal.batchimport.Facility)
	 */
	@Override
	protected LogTrain executeUpdate(Facility facility) {
		/*
		 * NOOP
		 * 
		 * As of TM 1.11.0 the 'update' will be handled by the DatasetParamValueInstruction. Today there are no update on a Dataset
		 * (we can't rename a dataset for instance), only DatasetParamValueInstruction have a use for the 'update' action.
		 * 
		 */
		return new LogTrain();
	}

	/**
	 * @see org.squashtest.tm.service.internal.batchimport.Instruction#executeDelete(org.squashtest.tm.service.internal.batchimport.Facility)
	 */
	@Override
	protected LogTrain executeDelete(Facility facility) {
		return facility.deleteDataset(getTarget());
	}

	/**
	 * @see org.squashtest.tm.service.internal.batchimport.Instruction#executeCreate(org.squashtest.tm.service.internal.batchimport.Facility)
	 */
	@Override
	protected LogTrain executeCreate(Facility facility) {
		return facility.createDataset(getTarget());
	}


}
