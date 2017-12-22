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
package org.squashtest.tm.service.testcase;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.testcase.Parameter;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestStep;

@Transactional
public interface ParameterModificationService extends ParameterFinder{

	/**
	 * 
	 * @param parameter
	 * @param testCaseId
	 */
	void addNewParameterToTestCase(Parameter parameter, long testCaseId);
	
	/**
	 * 
	 * @param parameterId
	 * @param name
	 */
	void changeName(long parameterId, String name);
	
	/**
	 * 
	 * @param parameterId
	 * @param description
	 */
	void changeDescription(long parameterId, String description);

	/**
	 * 
	 * @param parameter
	 */
	void remove(Parameter parameter);
	/**
	 * 
	 * @param testCaseIds
	 */
	void removeAllByTestCaseIds(List<Long> testCaseIds);
	
	/**
	 *  Will create all parameters used in the step if they don't already exist.
	 * And will update all Datasets and calling test cases datasets in consequence.
	 * 
	 * @param stepId : the id of the concerned step
	 */
	void createParamsForStep(long stepId);
	
	/**
	 * Will create all parameters used in the step if they don't already exist.
	 * And will update all Datasets and calling test cases datasets in consequence.
	 * 
	 * @param step : the concerned step
	 */
	void createParamsForStep(TestStep step);
	/**
	 * 
	 * @param parameterId
	 */
	void removeById(long parameterId);
	
	/**
	 * Will go through the test case's steps and create the missing parameter.
	 * If the test case has datasets, will create the new datasetParamValues.
	 * @param testCase : the concerned test case
	 */
	void createParamsForTestCaseSteps(TestCase testCase);
}
