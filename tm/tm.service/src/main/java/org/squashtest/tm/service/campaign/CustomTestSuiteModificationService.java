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

import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.campaign.TestPlanStatistics;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.exception.DuplicateNameException;

/**
 * Test-Suite modification services which cannot be dynamically generated.
 *
 */
@Transactional
public interface CustomTestSuiteModificationService extends TestSuiteFinder{

	/**
	 * That method will update the name of the suite with newName, identified by suiteId. Will throw a {@link DuplicateNameException}
	 * if the suite could not rename itself due to name already used by another suite from the same owning Iteration (as as spec of TestSuite).
	 *
	 * @param suiteId
	 * @param newName
	 * @throws DuplicateNameException
	 */
	void rename(long suiteId, String newName) throws DuplicateNameException;


	void updateExecutionStatus(TestSuite testSuite);

	/**
	 * <p>That method will retrieve the data and fill the testSuiteStatistics DTO.</p>
	 *
	 * @param suiteId
	 */
	TestPlanStatistics findTestSuiteStatistics(long suiteId);


	Execution addExecution(long testPlanItemId);



}
