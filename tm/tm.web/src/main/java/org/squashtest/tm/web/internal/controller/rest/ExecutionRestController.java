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
package org.squashtest.tm.web.internal.controller.rest;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStep;
import org.squashtest.tm.service.execution.ExecutionFinder;
import org.squashtest.tm.service.testcase.TestCaseLibraryFinderService;
import org.squashtest.tm.web.exception.ResourceNotFoundException;
import org.squashtest.tm.web.internal.model.rest.RestExecution;
import org.squashtest.tm.web.internal.model.rest.RestExecutionStep;

@Controller
@RequestMapping("/api/execution")
public class ExecutionRestController {

	@Inject
	private ExecutionFinder executionFinder;

	@Inject
	private TestCaseLibraryFinderService testCaseLibraryFinder;

	private Execution findExecution(Long id) {

		Execution execution = null;

		if(executionFinder.exists(id)){
			execution = executionFinder.findById(id);
		}

		if (execution == null) {
			throw new ResourceNotFoundException();
		}

		return execution;
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public RestExecution getExecutionById(@PathVariable Long id) {

		Execution execution;
		String path = "";
		execution = findExecution(id);
		if (execution.getReferencedTestCase() != null) {
			path = testCaseLibraryFinder.getPathAsString(execution.getReferencedTestCase().getId());
		}

		return new RestExecution(execution, path);
	}

	@RequestMapping(value = "/{id}/executionsteps", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public List<RestExecutionStep> getExecutionStepsById(@PathVariable Long id) {

		Execution execution = findExecution(id);
		List<ExecutionStep> steps = execution.getSteps();
		List<RestExecutionStep> restExecutionSteps = new ArrayList<>(steps.size());
		for (ExecutionStep step : steps) {
			restExecutionSteps.add(new RestExecutionStep(step));
		}

		return restExecutionSteps;
	}

}
