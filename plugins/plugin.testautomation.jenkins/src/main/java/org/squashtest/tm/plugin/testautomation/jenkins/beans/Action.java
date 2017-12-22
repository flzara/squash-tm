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
package org.squashtest.tm.plugin.testautomation.jenkins.beans;

import java.util.Arrays;

public class Action {
	
	private Parameter[] parameters;

	public Parameter[] getParameters() {
		return parameters;
	}

	public void setParameters(Parameter[] parameters) {	//NOSONAR that array is not stored directly
		this.parameters = Arrays.copyOf(parameters, parameters.length);
	}
	
	public Action(){
		super();
	}
	
	public boolean hasParameter(Parameter parameter){
		
		if (parameters == null) return false;
		
		for (Parameter param : parameters){
			
			if (param == null) continue;
			
			if (param.equals(parameter)){
				return true;
			}
			
		}
		return false;
	}
	
}
