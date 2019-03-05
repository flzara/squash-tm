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
package org.squashtest.tm.domain.campaign;

import org.squashtest.tm.exception.execution.TestPlanItemNotExecutableException;

public interface TestPlanOwner {

	boolean isLastExecutableTestPlanItem(long itemId, String userLogin);

	/**
	 * Determines if the item is the first of the test plan of the test plan owner
	 *
	 * @param itemId      : the id of the item to determine if it is the first executable test plan item
	 * @param testerLogin : the id of the current user if he is a Test runner
	 */
	boolean isFirstExecutableTestPlanItem(long itemId, String testerLogin);

	/**
	 * finds next item (that last execution has unexecuted step) or (has no execution and is not test case deleted)
	 * <em>can return item linked to test-case with no step</em>
	 *
	 * @throws TestPlanItemNotExecutableException if no item is found
	 * @throws IllegalArgumentException           if id does not correspond to an item of the test suite
	 */
	IterationTestPlanItem findNextExecutableTestPlanItem(long testPlanItemId);

	/**
	 * finds next item (that last execution has unexecuted step) or (has no execution and is not test case deleted) and that is assigned to the current user if he is a tester.<br>
	 * <em>NB: can return item linked to test-case with no step</em>
	 *
	 * @param testerLogin : the login of the connected user if he is a Test Runner
	 * @throws TestPlanItemNotExecutableException if no item is found
	 * @throws IllegalArgumentException           if id does not correspond to an item of the test suite
	 */
	IterationTestPlanItem findNextExecutableTestPlanItem(long testPlanItemId, String testerLogin);

	IterationTestPlanItem findFirstExecutableTestPlanItem(String testerLogin);
}
