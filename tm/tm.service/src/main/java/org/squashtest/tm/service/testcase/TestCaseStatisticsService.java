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

import java.util.Collection;

import org.squashtest.tm.service.statistics.testcase.TestCaseBoundRequirementsStatistics;
import org.squashtest.tm.service.statistics.testcase.TestCaseImportanceStatistics;
import org.squashtest.tm.service.statistics.testcase.TestCaseSizeStatistics;
import org.squashtest.tm.service.statistics.testcase.TestCaseStatisticsBundle;
import org.squashtest.tm.service.statistics.testcase.TestCaseStatusesStatistics;

public interface TestCaseStatisticsService {

	/**
	 * Given those test case ids, returns how many of them are bound to requirements and how many aren't. Warning : no security check will 
	 * be performed and the data will be returned regardless of who requested it.
	 * 
	 * @param testCaseIds
	 * @return
	 */
	TestCaseBoundRequirementsStatistics gatherBoundRequirementStatistics(Collection<Long> testCaseIds);
	
	/**
	 * Given those test case ids, sorts them by importance and returns how many of each were found. Warning : no security check will 
	 * be performed and the data will be returned regardless of who requested it.
	 * 
	 * @param testCaseIds
	 * @return
	 */
	TestCaseImportanceStatistics gatherTestCaseImportanceStatistics(Collection<Long> testCaseIds);
	
	/**
	 * Given those test case ids, sort them number of steps aggregated by slices of 10 (0,  0 &lt; x &lt;= 10, 10 &lt; x &lt;=20, 20 &lt; x), 
	 * then returns how many of each. Warning : no security check will be performed and the data will be returned regardless of who requested it.
	 * @param testCaseIds
	 * @return
	 */
	TestCaseSizeStatistics gatherTestCaseSizeStatistics(Collection<Long> testCaseIds);
	
	/**
	 * Given those test case ids, sort them by status and returns how many of each were found. Warning : no security check will 
	 * be performed and the data will be returned regardless of who requested it.
	 * 
	 * @param testCaseIds
	 * @return
	 */
	TestCaseStatusesStatistics gatherTestCaseStatusesStatistics(Collection<Long> testCaseIds);
	
	/**
	 * Returns all of the above bundled in one bean. 
	 * 
	 * @param testCaseIds
	 * @return
	 */
	TestCaseStatisticsBundle gatherTestCaseStatisticsBundle(Collection<Long> testCaseIds);
}
