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
package org.squashtest.tm.service.internal.repository.hibernate

import javax.inject.Inject

import org.squashtest.tm.service.internal.repository.ExecutionDao
import org.unitils.dbunit.annotation.DataSet

import spock.lang.Unroll
import spock.unitils.UnitilsSupport
import org.squashtest.tm.core.foundation.collection.SortOrder
import org.squashtest.it.basespecs.DbunitDaoSpecification;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting
import org.squashtest.tm.domain.execution.ExecutionStatus

/**
 * Dataset "HibernateExecutionDaoIT.should find executions by test case.xml" explained
 * 3 Campaigns
 * <ul>
 * 	<li>Campaign A #-121 owned by Project B #-2
 * 		<ol><li>IT A #-132
 * 				<ol>
 * 					<li>a bunch of itp with no TC linked</li>
 * 					<li>ITP #-1109 linked to TC #-500 with one Exec #-494
 * 					</li>
 * 				</ol>
 * 			</li>
 * 		</ol>
 * 	</li>
 * 	<li>Campaign B #-199 owned by Project A #-1
 * 		<ol>
 * 			<li>IT B #-146
 * 				<ol>
 * 					<li>a bunch of itp with no TC linked</li>
 * 					<li>ITP #-1248linked to TC #-500 with one Exec #-580
 * 					</li>
 * 				</ol>
 * 			</li>
 * 		</ol>
 * 	</li>
 * 	<li>Campaign C #-200 not owned by any project
 * 		<ol>
 * 			<li>IT C #-151
 * 				<ol>
 * 					<li>a bunch of itp with no TC linked</li>
 * 					<li>ITP #-1291linked to TC #-500 with one Exec #-627
 * 					</li>
 * 				</ol>
 * 			</li>
 * 			<li>IT D #-152
 * 				<ol>
 * 					<li>a bunch of itp with no TC linked</li>
 * 					<li>ITP #-1324 linked to TC #-500 with no exec</li>
 * 				</ol>
 * 			</li>
 * 			<li>IT E #-158
 * 				<ol>
 * 					<li>a bunch of itp with no TC linked</li>
 * 					<li>ITP #-1491 linked to TC #-500 with no exec</li>
 * 				</ol>
 * 			</li>
 * 			<li>IT F #-161
 * 				<ol>
 * 					<li>a bunch of itp with no TC linked</li>
 * 					<li>ITP #-1568 linked to TC #-500 with one Exec #-953
 * 					</li>
 * 				</ol>
 * 			</li>
 * 			<li>IT G #-198
 * 				<ol>
 * 					<li>a bunch of itp with no TC linked</li>
 * 					<li>ITP #-1951 linked to TC #-500 with one Exec #-1110
 * 					</li>
 * 				</ol>
 * 			</li>
 * 			<li>IT H #-261
 * 				<ol>
 * 					<li>a bunch of itp with no TC linked</li>
 * 					<li>ITP #-2946 linked to TC #-500 with one Exec #-1556
 * 					</li>
 * 				</ol>
 * 			</li>
 * 			<li>IT I #-299
 * 				<ol>
 * 					<li>a bunch of itp with no TC linked</li>
 * 					<li>ITP #-3766 linked to TC #-500 with one Exec #-2150
 * 					</li>
 * 				</ol>
 * 			</li>
 * 			<li>IT J #-339
 * 				<ol>
 * 					<li>a bunch of itp with no TC linked</li>
 * 					<li>ITP #-4601 linked to TC #-500 with one Exec #-2562
 * 					</li>
 * 				</ol>
 * 			</li>
 * 			<li>IT K #-348
 * 				<ol>
 * 					<li>a bunch of itp with no TC linked</li>
 * 					<li>ITP #-4852 linked to TC #-500 with one  Exec #-2971
 * 					</li>
 * 				</ol>
 * 			</li>
 * 		</ol>
 * 	</li>
 * </ul>
 **/
@UnitilsSupport
class HibernateExecutionDaoIT extends DbunitDaoSpecification {
	@Inject ExecutionDao executionDao


	@DataSet("HibernateExecutionDaoIT.should find executions by test case.xml")
	@Unroll("should count #expectedCount executions for test case #testCaseId")
	def "should count #expectedCount executions for test case #testCaseId"() {
		when:
		def count = executionDao.countByTestCaseId(testCaseId)

		then:
		count == expectedCount

		where:
		testCaseId  | expectedCount
		-500        | 11
		-550        | 0
	}

	@DataSet("HibernateExecutionDaoIT.should find executions by test case.xml")
	def "should find 5 paged executions for test case 500"() {
		given:
		PagingAndSorting pas = Mock()
		pas.firstItemIndex >> 0
		pas.pageSize >> 5
		pas.sortedAttribute >> "Execution.lastExecutedOn"
		pas.sortOrder >> SortOrder.ASCENDING

		when:
		def res = executionDao.findAllByTestCaseId(-500L, pas)

		then:
		res*.id == [-494, -580, -627, -718, -752]
	}


	@DataSet("HibernateExecutionDaoIT.should find executions by test case.xml")
	@Unroll("should find executions #expectedIds sorted by #sortedAttribute")
	def "should find executions sorted by .."() {
		given:
		PagingAndSorting pas = Mock()
		pas.firstItemIndex >> 0
		pas.pageSize >> expectedIds.size()
		pas.sortedAttribute >> sortedAttribute
		pas.sortOrder >> sortOrder

		when:
		def res = executionDao.findAllByTestCaseId(-500L, pas)

		then:
		res*.id == expectedIds

		where:
		sortedAttribute             | sortOrder            | expectedIds
		"Project.name"              | SortOrder.DESCENDING | [-580,-494] // project a, project b, (null not shown here because has a long list of candidates that can be returned in different o:
		"Campaign.name"             | SortOrder.ASCENDING  | [-718, -494, -580] // null, camp a, camp b */
		"Iteration.name"            | SortOrder.ASCENDING  | [-718, -494, -580]
		"Execution.name"            | SortOrder.ASCENDING  | [-494, -580, -627]
		"Execution.executionMode"   | SortOrder.ASCENDING  | [-627, -718]
		"Execution.executionStatus" | SortOrder.ASCENDING  | [-953, -1110, -1556]
		"Execution.lastExecutedBy"  | SortOrder.ASCENDING  | [-2150, -2562, -2971]
		"Execution.lastExecutedOn"  | SortOrder.ASCENDING  | [-494, -580, -627]
	}


	@DataSet("HibernateExecutionDaoIT.should find if project uses exec status.xml")
	@Unroll("should find if project #projectId uses exec status #execStatus")
	def"should find if project uses exec status"(){
		when:
		def res = executionDao.projectUsesExecutionStatus(projectId, execStatus)

		then :
		res == expectedResult

		where :
		projectId  | execStatus                  | expectedResult
		-1         | ExecutionStatus.SETTLED  	 | true // in execution step
		-1         | ExecutionStatus.UNTESTABLE	 | true // in iteration test plan item
		-2		   | ExecutionStatus.UNTESTABLE  | false
		-2         | ExecutionStatus.SETTLED 	 | true // in iteration test plan


	}
}
