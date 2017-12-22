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
package org.squashtest.tm.domain.campaign

import org.squashtest.tm.domain.campaign.TestPlanStatistics
import org.squashtest.tm.domain.campaign.TestPlanStatus
import org.squashtest.tm.domain.execution.ExecutionStatus

import spock.lang.Specification

class TestPlanStatisticsTest extends Specification {

	
	def "should return the good values and status RUNNING"(){
		
		when :
			Map<String, Integer> mapStats = new HashMap<String, Integer>(7)
			mapStats.put(ExecutionStatus.UNTESTABLE.name(), 0)
			mapStats.put(ExecutionStatus.BLOCKED.name(), 2)
			mapStats.put(ExecutionStatus.FAILURE.name(), 1)
			mapStats.put(ExecutionStatus.SUCCESS.name(), 4)
			mapStats.put(ExecutionStatus.RUNNING.name(), 2)
			mapStats.put(ExecutionStatus.READY.name(), 1)
			
			TestPlanStatistics stats = new TestPlanStatistics(mapStats)
			
		then :
			stats.nbTestCases == 10L
			stats.nbUntestable == 0
			stats.nbBlocked == 2
			stats.nbFailure == 1
			stats.nbSuccess == 4
			stats.nbRunning == 2
			stats.nbReady == 1
			stats.nbDone == 7
			stats.progression == 70
			stats.status == TestPlanStatus.RUNNING
	}
	
	def "should return the good values and status READY"(){
		
		when :
		Map<String, Integer> mapStats = new HashMap<String, Integer>(7)
		mapStats.put(ExecutionStatus.UNTESTABLE.name(), 0)
		mapStats.put(ExecutionStatus.BLOCKED.name(), 0)
		mapStats.put(ExecutionStatus.FAILURE.name(), 0)
		mapStats.put(ExecutionStatus.SUCCESS.name(), 0)
		mapStats.put(ExecutionStatus.RUNNING.name(), 0)
		mapStats.put(ExecutionStatus.READY.name(), 10)
		
			def stats = new TestPlanStatistics(mapStats)
			
		then :
			stats.nbTestCases == 10L
			stats.nbUntestable == 0
			stats.nbBlocked == 0
			stats.nbFailure == 0
			stats.nbSuccess == 0
			stats.nbRunning == 0
			stats.nbReady == 10
			stats.nbDone == 0
			stats.progression == 0
			stats.status == TestPlanStatus.READY
	}
	
	def "should return the good values and status DONE"(){
		
		when :
		Map<String, Integer> mapStats = new HashMap<String, Integer>(7)
		mapStats.put(ExecutionStatus.UNTESTABLE.name(), 0)
		mapStats.put(ExecutionStatus.BLOCKED.name(), 3)
		mapStats.put(ExecutionStatus.FAILURE.name(), 2)
		mapStats.put(ExecutionStatus.SUCCESS.name(), 5)
		mapStats.put(ExecutionStatus.RUNNING.name(), 0)
		mapStats.put(ExecutionStatus.READY.name(), 0)
		
		def stats = new TestPlanStatistics(mapStats)
			
		then :
			stats.nbTestCases == 10L
			stats.nbUntestable == 0
			stats.nbBlocked == 3
			stats.nbFailure == 2
			stats.nbSuccess == 5
			stats.nbRunning == 0
			stats.nbReady == 0
			stats.nbDone == 10
			stats.progression == 100
			stats.status == TestPlanStatus.DONE
	}
	
}
