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

import static org.squashtest.tm.web.internal.helper.JEditablePostParams.VALUE;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.domain.testcase.Parameter;
import org.squashtest.tm.service.testcase.ParameterModificationService;

/**
 * Controller to handle requests on parameters regardless of their test case
 * 
 * @author mpagnon
 * 
 */
@RequestMapping("/parameters")
@Controller
public class ParameterController {

	private static final String PARAMETER_ID_URL = "/{parameterId}";
	
	@Inject
	private ParameterModificationService parameterModificationService;

	/**
	 * returns whether the {@link Parameter} is used in a ActionTestStep of it's TestCase
	 * 
	 * @param parameterId
	 * @return
	 */
	@RequestMapping(value = PARAMETER_ID_URL + "/used", method = RequestMethod.GET)
	@ResponseBody
	public boolean isUsedParameter(@PathVariable long parameterId) {
		return parameterModificationService.isUsed(parameterId);
	}

	/**
	 * Will delete the {@link Parameter} of the given id
	 * @param parameterId : the id of the Parameter to delete
	 */
	@RequestMapping(value = PARAMETER_ID_URL, method = RequestMethod.DELETE)
	@ResponseBody
	public void deleteParameter(@PathVariable long parameterId) {
		parameterModificationService.removeById(parameterId);
	}
	
	/**
	 * Will change the name of the {@link Parameter} of the given id with the given value
	 * 
	 * @param parameterId : id of the concerned Parameter
	 * @param value : value for the new name
	 * @return
	 */
	@RequestMapping(value= PARAMETER_ID_URL+"/name", method = RequestMethod.POST, params = {VALUE})
	@ResponseBody
	public String changeName(@PathVariable long parameterId, @RequestParam(VALUE) String value){
		parameterModificationService.changeName(parameterId, value);
		 return value;
	}
	
	/**
	 * Will change the description of the {@link Parameter} of the given id with the given value
	 * 
	 * @param parameterId : id of the concerned Parameter
	 * @param value : value for the new description
	 * @return
	 */
	@RequestMapping(value= PARAMETER_ID_URL+"/description", method = RequestMethod.POST, params = {VALUE})
	@ResponseBody
	public String changeDescription(@PathVariable long parameterId, @RequestParam(VALUE) String value){
		parameterModificationService.changeDescription(parameterId, value);
		return value;
	}
}
