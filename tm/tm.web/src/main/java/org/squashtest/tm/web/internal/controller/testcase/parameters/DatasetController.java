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
import org.squashtest.tm.domain.testcase.Dataset;
import org.squashtest.tm.service.testcase.DatasetModificationService;

/**
 * @author mpagnon
 * 
 */
@RequestMapping("/datasets")
@Controller
public class DatasetController {

	private static final String DATASET_ID_URL = "/{datasetId}";
	
	@Inject
	private DatasetModificationService datasetModificationService;

	/**
	 * Will delete the {@link Dataset} of the given id
	 * @param datasetId : the id of the Dataset to delete
	 */
	@RequestMapping(value = DATASET_ID_URL, method = RequestMethod.DELETE)
	@ResponseBody
	public void deleteDataset(@PathVariable long datasetId) {
		datasetModificationService.removeById(datasetId);
	}
	
	/**
	 * Will change the name of the {@link Dataset} of the given id with the given value
	 * 
	 * @param datasetId : id of the concerned Dataset
	 * @param value : value for the new name
	 * @return
	 */
	@RequestMapping(value= DATASET_ID_URL+"/name", method = RequestMethod.POST, params = {VALUE}, produces = "text/plain;charset=UTF-8")
	@ResponseBody
	public String changeName(@PathVariable long datasetId, @RequestParam(VALUE) String value){
		datasetModificationService.changeName(datasetId, value);
		 return  HtmlUtils.htmlEscape(value);
	}
	
	
}
