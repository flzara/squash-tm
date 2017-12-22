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
package org.squashtest.tm.service.statistics.campaign

import java.text.SimpleDateFormat;

import spock.lang.Specification

class CampaignProgressionStatisticsTest extends Specification {

	SimpleDateFormat dateformat = new SimpleDateFormat("dd/MM/yyyy")
	SimpleDateFormat timeformat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
	
	def "should compute the cumulative number of tests per date when there were no execution at all"(){
		
		when :
			CampaignProgressionStatistics progression = new CampaignProgressionStatistics();
			progression.computeCumulativeTestPerDate([])
		
		then :
			progression.cumulativeExecutionsPerDate == []
		
		
	}
	
	def "should compute the cumulative tests for a list of one date"(){
		
		given :
			def zetime = time("01/02/03 01:02:03")
		
		when :
			CampaignProgressionStatistics progression = new CampaignProgressionStatistics()
			progression.computeCumulativeTestPerDate([zetime])
			
		then : 
			progression.cumulativeExecutionsPerDate == [[date("01/02/03"), 1] as Object[]]
	}
	
	
	def "should compute the cumulative tests for a bunch of dates (1)"(){
		given :
			def dates = [
					time("01/01/01 01:01:01"),
					time("01/01/01 02:02:02"),
					time("02/02/02 01:01:01"),
					time("02/02/02 01:01:02")
				]		
		when :
			CampaignProgressionStatistics progression = new CampaignProgressionStatistics()
			progression.computeCumulativeTestPerDate(dates)
			
		then :
			progression.cumulativeExecutionsPerDate == [
				[date("01/01/01"), 2] as Object[],
				[date("02/02/02"), 4] as Object[]	
			]
	}
	
	def "should compute the cumulative tests for a bunch of dates (2)"(){
		given :
			def dates = [
					time("01/01/01 01:01:01"),
					time("02/02/02 01:01:01"),
					time("02/02/02 01:01:02"),
					time("02/02/02 01:01:03"),
					time("03/03/03 01:01:01")
				]
		when :
			CampaignProgressionStatistics progression = new CampaignProgressionStatistics()
			progression.computeCumulativeTestPerDate(dates)
			
		then :
			progression.cumulativeExecutionsPerDate == [
				[date("01/01/01"), 1] as Object[],
				[date("02/02/02"), 4] as Object[],
				[date("03/03/03"), 5] as Object[]
			]
	}
	
	def date(strdate){
		dateformat.parse(strdate)
	}
	
	def time(strtime){
		timeformat.parse(strtime)
	}
	
}
