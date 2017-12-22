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

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestStep;
import org.squashtest.tm.service.testcase.TestCaseFinder;
import org.squashtest.tm.service.testcase.TestStepFinder;
import org.squashtest.tm.web.internal.model.rest.RestTestCase;
import org.squashtest.tm.web.internal.model.rest.RestTestStep;

@Controller
@RequestMapping("/api/testcase")
public class TestCaseRestController {

	@Inject
	private TestCaseFinder testCaseFinder;

	@Inject
	private TestStepFinder testStepFinder;

	/**
	 *
	 * @deprecated consider using
	 *             {@link org.squashtest.tm.web.exception.ResourceNotFoundException}
	 *
	 */
	@Deprecated
	@ResponseStatus(value = HttpStatus.NOT_FOUND)
	private final class ResourceNotFoundException extends RuntimeException {

		private static final long serialVersionUID = -649887942614579558L;

		public ResourceNotFoundException() {
			super();
		}

		public ResourceNotFoundException(Throwable cause) {
			super(cause);
		}
	}

	private TestCase findTestCase(Long id){

		TestCase testCase = null;

		try {
			testCase = this.testCaseFinder.findById(id);
		} catch (RuntimeException e) {

			if(e.getCause().getClass().equals(java.lang.reflect.InvocationTargetException.class)) {
				throw new ResourceNotFoundException(e);
			}
		}


		if(testCase == null){
			throw new ResourceNotFoundException();
		}

		return testCase;
	}

	private List<TestStep> findTestSteps(Long id){

		List<TestStep> testSteps = new ArrayList<>();

		try {
			testSteps = this.testCaseFinder.findStepsByTestCaseId(id);
		} catch (java.lang.RuntimeException e) {
			if (e.getCause().getClass().equals(java.lang.reflect.InvocationTargetException.class)) {
				throw new ResourceNotFoundException();
			} else {
				throw e;
			}
		}

		return testSteps;
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public RestTestCase getTestCaseById(@PathVariable Long id) {
		TestCase testCase = findTestCase(id);
		return new RestTestCase(testCase);

	}

	@RequestMapping(value = "/{id}/teststeps", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public List<RestTestStep> getTestStepsByTestCaseId(@PathVariable Long id) {

		List<TestStep> testSteps = findTestSteps(id);
		List<RestTestStep> restTestSteps = new ArrayList<>(testSteps.size());
		for (TestStep testStep : testSteps) {
			restTestSteps.add(new RestTestStep(testStep));
		}

		return restTestSteps;
	}

}
