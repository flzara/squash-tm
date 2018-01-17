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
package org.squashtest.tm.bugtracker.advanceddomain;


import java.util.Arrays;

public class Rendering {

	private String[] operations = new String[0];

	private InputType inputType;

	private boolean required=false;


	public Rendering(){
		super();
	}

	public Rendering(String[] operations, InputType inputType, boolean required) {
		super();
		setOperationsPrivately(operations);
		this.inputType = inputType;
		this.required = required;
	}

	public String[] getOperations() {
		return operations;
	}

	private void setOperationsPrivately(String[] operationsParam) {
		if(operationsParam == null) {
			this.operations = null;
		} else {
			this.operations = Arrays.copyOf(operationsParam, operationsParam.length);
		}
	}

	public void setOperations(String[] operationsParam) {
		setOperationsPrivately(operationsParam);
	}


	public InputType getInputType() {
		return inputType;
	}

	public void setInputType(InputType inputType) {
		this.inputType = inputType;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

}
