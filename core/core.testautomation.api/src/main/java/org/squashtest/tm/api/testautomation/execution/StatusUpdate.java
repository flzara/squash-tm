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
package org.squashtest.tm.api.testautomation.execution;

import org.squashtest.tm.api.testautomation.execution.dto.TestExecutionStatus;
import org.squashtest.tm.api.testautomation.execution.dto.TestSuiteExecutionStatus;

/**
 * This interface defines operations for the execution API of Squash TM.
 * 
 * @author edegenetais
 * @deprecated Definition of TM "internal" service is defined internally in TM.
 */
@Deprecated
public interface StatusUpdate {
	/**
	 * Method to update a test suite execution status.
	 * 
	 * @param executionHostname
	 *            name of the execution host.
	 * @param jobName
	 *            name of the test job (automation project) in the host.
	 * @param externalId
	 *            external id given to the execution process when execution was required.
	 * @param status
	 *            new status of the test suite.
	 */
	void updateTestSuiteExecutionStatus(String executionHostname, String jobName, String externalId,
			TestSuiteExecutionStatus status);

	/**
	 * Method to update a test execution status.
	 * 
	 * @param executionHostname
	 *            name of the execution host.
	 * @param jobName
	 *            name of the test job (automation project) in the host.
	 * @param externalId
	 *            external id given to the execution process when execution was required.
	 * @param testGroupName
	 *            name of the test group the test belongs to.
	 * @param testName
	 *            name of the test.
	 * @param status
	 *            new status of the test.
	 */
	void updateTestExecutionStatus(String executionHostname, String jobName, String externalId, String testGroupName,
			String testName, TestExecutionStatus status);
}