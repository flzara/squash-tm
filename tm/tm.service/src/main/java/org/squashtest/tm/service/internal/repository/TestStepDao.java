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
package org.squashtest.tm.service.internal.repository;

import java.util.List;

import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.domain.testcase.TestStep;

public interface TestStepDao extends EntityDao<TestStep>{

	void removeById(long testStepId);

	List<TestStep> findListById(List<Long> testStepIds);

	/**
	 * returns the position (ie index) of a step within the
	 * list of step of its test case
	 *
	 * @param testStepId the id of the step
	 * @return
	 */
	int findPositionOfStep(Long testStepId);

	ActionTestStep findActionTestStepById(long testStepId);

	/**
	 * Will check if the string appears at least once in at least one step of the test case matching the given id.
	 *
	 * @param stringToFind : the string to look for in the step
	 * @param testCaseId : the id of the concerned TestCase
	 * @return true if the string is found in one step of the concerned test case
	 */
	boolean stringIsFoundInStepsOfTestCase(String stringToFind, long testCaseId);

	/**
	 * Find all {@link TestStep}, ordered by their index in the test case. This method MUST be used for steps that belong to the
	 * the same test case. Typical use case : select the steps in good order for a copy.
	 * @param testStepIds
	 * @return
     */
	List<TestStep> findByIdOrderedByIndex(List<Long> testStepIds);


}
