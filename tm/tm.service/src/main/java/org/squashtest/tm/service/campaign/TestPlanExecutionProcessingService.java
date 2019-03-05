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
package org.squashtest.tm.service.campaign;

import org.squashtest.tm.domain.campaign.TestPlanOwner;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.exception.execution.EmptyTestSuiteTestPlanException;
import org.squashtest.tm.exception.execution.TestPlanItemNotExecutableException;

/**
 * Interface for service responsible to process execution of a list of {@link org.squashtest.tm.domain.campaign.IterationTestPlanItem}
 * @param <E> class of the entity owning the test plan. Use for injection differenciation.
 * @author aguilhem
 */
public interface TestPlanExecutionProcessingService<E extends TestPlanOwner> {
	/**
	 * <p>
	 * will delete all existing executions
	 * </p>
	 *
	 * @param testPlanOwnerId id of the {@link TestPlanOwner} for which all {@link Execution} of each {@link org.squashtest.tm.domain.campaign.IterationTestPlanItem}
	 *                        need to be deleted
	 */
	void deleteAllExecutions(long testPlanOwnerId);

	/**
	 * <p>
	 * Should start a new execution for the next executable test plan item of the given test plan owner. Or
	 * should return the execution to resume or null if :
	 * <ul>
	 * <li>all terminated, or</li>
	 * <li>no execution-step on executions, or</li>
	 * <li>no execution and test-case deleted</li>
	 * </ul>
	 * </p>
	 *
	 * @param testPlanOwnerId id of the {@link TestPlanOwner}
	 * @param testPlanItemId id of the {@link org.squashtest.tm.domain.campaign.IterationTestPlanItem}
	 * @return the execution to resume/restart or null if there is none
	 */
	Execution startResumeNextExecution(long testPlanOwnerId, long testPlanItemId);

	/**
	 * <p>
	 * returns the execution were to resume the test plan <br>
	 * or throw a @link{@linkplain EmptyTestSuiteTestPlanException} or {@link org.squashtest.tm.exception.execution.EmptyIterationTestPlanException}
	 * or {@linkplain TestPlanItemNotExecutableException}
	 * if no execution is to be resumed because :
	 * <ul>
	 * <li>all terminated, or</li>
	 * <li>no execution-step on executions, or</li>
	 * <li>no execution and test-case deleted</li>
	 * </ul>
	 * <p>
	 * if there is no execution should start a new execution for the given test plan owner, ie create an execution for the
	 * first test case of this owner's test plan
	 * </p>
	 *
	 * @param testPlanOwnerId id of the {@link TestPlanOwner}
	 * @return the {@linkplain Execution} where to resume or null
	 * @throws EmptyTestSuiteTestPlanException thrown if {@link TestPlanOwner} is a {@link org.squashtest.tm.domain.campaign.TestSuite} with an empty test plan
	 * @throws org.squashtest.tm.exception.execution.EmptyIterationTestPlanException thrown if {@link TestPlanOwner} is an {@link org.squashtest.tm.domain.campaign.Iteration} with an empty test plan
	 * @throws {@link TestPlanItemNotExecutableException}
	 */
	Execution startResume(long testPlanOwnerId);

	/**
	 * <p>
	 * tells if a test plan owner has at least one executable item in its test plan after the given item.
	 * </p>
	 *
	 * @param testPlanOwnerId id of the {@link TestPlanOwner}
	 * @param testPlanItemId id of the {@link org.squashtest.tm.domain.campaign.IterationTestPlanItem}
	 * @return true if test plan owner has at least one executable item in its test plan after the given item.
	 */
	boolean hasMoreExecutableItems(long testPlanOwnerId, long testPlanItemId);

	/**
	 * <p>
	 * tells if a test plan owner has at least one executable item in its test plan before the given item.
	 * </p>
	 *
	 * @param testPlanOwnerId id of the {@link TestPlanOwner}
	 * @param testPlanItemId id of the {@link org.squashtest.tm.domain.campaign.IterationTestPlanItem}
	 * @return true if a test plan owner has at least one executable item in its test plan before the given item.
	 */
	boolean hasPreviousExecutableItems(long testPlanOwnerId, long testPlanItemId);
}
