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
import org.springframework.web.util.HtmlUtils;
import org.squashtest.tm.service.testcase.DatasetModificationService;


/**
 * Controller to handle requests on  DatasetParamValues 
 * 
 * @author mpagnon
 * 
 */
@RequestMapping("/dataset-parameter-values")
@Controller
public class DatasetParamValueController {
	private static final String DATASTE_PARAM_VALUE_ID_URL = "/{datasetParamValueId}";
	
	@Inject
	private DatasetModificationService datasetModificationService;
	
	/**
	 * Will change the paramValue of the  DatasetParamValue of the given id with the given value
	 * 
	 * @param datasetParamValueId : id of the concerned DatasetParamValue
	 * @param value : value for the new paramValue
	 * @return
	 */
	@RequestMapping(value= DATASTE_PARAM_VALUE_ID_URL+"/param-value", method = RequestMethod.POST, params = {VALUE}, produces = "text/plain;charset=UTF-8")
	@ResponseBody
	public String changeParamValue(@PathVariable long datasetParamValueId, @RequestParam(VALUE) String value){
		this.datasetModificationService.changeParamValue(datasetParamValueId, value);
		return HtmlUtils.htmlEscape(value);
	}
	
}
