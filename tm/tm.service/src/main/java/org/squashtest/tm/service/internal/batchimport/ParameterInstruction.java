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

import org.squashtest.tm.domain.testcase.Parameter;

public class ParameterInstruction extends Instruction<ParameterTarget> {

	private final Parameter parameter;

	public ParameterInstruction(@NotNull ParameterTarget target, @NotNull Parameter parameter) {
		super(target);
		this.parameter = parameter;
	}

	/**
	 * @return the parameter
	 */
	public Parameter getParameter() {
		return parameter;
	}

	/**
	 * @see org.squashtest.tm.service.internal.batchimport.Instruction#executeUpdate(org.squashtest.tm.service.internal.batchimport.Facility)
	 */
	@Override
	protected LogTrain executeUpdate(Facility facility) {
		fillMissingData();
		return facility.updateParameter(getTarget(), parameter);
	}

	/**
	 * @see org.squashtest.tm.service.internal.batchimport.Instruction#executeDelete(org.squashtest.tm.service.internal.batchimport.Facility)
	 */
	@Override
	protected LogTrain executeDelete(Facility facility) {
		fillMissingData();
		return facility.deleteParameter(getTarget());
	}

	/**
	 * @see org.squashtest.tm.service.internal.batchimport.Instruction#executeCreate(org.squashtest.tm.service.internal.batchimport.Facility)
	 */
	@Override
	protected LogTrain executeCreate(Facility facility) {
		fillMissingData();
		return facility.createParameter(getTarget(), parameter);
	}

	/*
	 * The point here is that the name of the parameter must appear in both
	 * the Parameter and the ParameterTarget. The problem is, when the sheet is parsed
	 * the PropertySetter that did the job only set the name for the Parameter.
	 * 
	 * A proper solution would be a PropertySetter that can update both the target and the
	 * bean, but it's way simpler to just call the method below when required to.
	 */
	private void fillMissingData(){
		getTarget().setName(parameter.getName());
	}

}
