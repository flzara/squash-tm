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
package org.squashtest.tm.web.internal.controller.testcase.parameters;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.squashtest.tm.domain.testcase.Parameter;

/**
 * Simple bean to pass information in ajax without all associated objects (param>testcase>steps etc)
 * @author mpagnon
 *
 */
public class SimpleParameter {
	private long id;
	private String name = "";
	private long testCaseId ;

	public SimpleParameter(long id, String name, long testCaseId) {
		this.id = id;
		this.name = name;
		this.testCaseId = testCaseId;
	}
	
	public SimpleParameter(Parameter parameter){
		this(parameter.getId(), parameter.getName(), parameter.getTestCase().getId());
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void setTestCaseId(long testCaseId){
		this.testCaseId = testCaseId;
	}

	public long getTestCaseId() {
		return testCaseId;
	}
	
	/**
	 * Converts the given list of {@link Parameter} into a list of {@link SimpleParameter}
	 * @param parameters : the list of Parameter to convert
	 * @param ownerId2 
	 * @param messageSource2 
	 * @param locale2 
	 * @return a list of SimpleParameter representing the given list of Parameter
	 */
	public static List<SimpleParameter> convertToSimpleParameters(List<Parameter> parameters, Long ownerId2, MessageSource messageSource2, Locale locale2){
		List<SimpleParameter> result = new ArrayList<>(parameters.size());
		for(Parameter param : parameters){
			String newName = ParametersModelHelper.buildParameterName(param, ownerId2, messageSource2, locale2);
			
			SimpleParameter simpleParameter = new SimpleParameter(param);
			simpleParameter.setName(newName);
			result.add(simpleParameter);			
		}
		return result;
	}

}
