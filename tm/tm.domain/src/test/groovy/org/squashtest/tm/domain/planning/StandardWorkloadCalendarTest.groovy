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
package org.squashtest.tm.domain.planning

import java.text.SimpleDateFormat;

import org.joda.time.LocalDate;

import spock.lang.Shared;
import spock.lang.Specification
import spock.lang.Unroll;

class StandardWorkloadCalendarTest extends Specification {

	@Shared
	SimpleDateFormat dateformatter = new SimpleDateFormat("dd/MM/yy");
	
	def date(strdate){
		dateformatter.parse(strdate)
	}
	
	@Unroll("should say that workload for #strday is #res")
	def "should say that workload is 1 or 0"(){
		
		expect : 
			res == new StandardWorkloadCalendar().getWorkload(zedate)
			
		where :
		strday		|	res		|	zedate
		"monday"	|	1.0f	|	date("28/10/13")
		"tuesday"	|	1.0f	|	date("29/10/13")
		"wednesday"	|	1.0f	|	date("30/10/13")
		"thursday"	|	1.0f	|	date("31/10/13")
		"friday"	|	1.0f	|	date("01/11/13")
		"saturday"	|	0.0f	|	date("02/11/13")
		"sunday"	|	0.0f	|	date("03/11/13")
	}
	

	
	def "should return a workload of 10.f, because of approximately two weeks"(){
		
		given :
			Date start = date("28/10/13");	//monday
			Date end = date("09/11/13");	//saturday two weeks later
			
		when :
			def res = new StandardWorkloadCalendar().getWorkload(start, end)
			
		then :
			res == 10.0f;
	}
	
	def "should return a workload of 0 because nobody (should) work the weekend"(){
		
		given :
			Date start = date("02/11/13");	//saturday
			Date end = start.plus(1);	//sunday
			
		when :
			def res = new StandardWorkloadCalendar().getWorkload(start, end)
			
		then :
			res == 0.0f;
		
	}
	
	def "should return a workload of 3 (skipping the weekend again)"(){
		given :
			Date start = date("01/11/13");	// a friday
			Date end = date("05/11/13");		// a wednesday
			
		when :
			def res = new StandardWorkloadCalendar().getWorkload(start, end);
			
		then :
			res == 3.0f; 
		
	}
	
	def "should return a workload of 1 because we're working only the monday"(){
		given :
			Date start = date("02/11/13");	//saturday
			Date end = start.plus(2);	//monday
			
		when :
			def res = new StandardWorkloadCalendar().getWorkload(start, end)
			
		then :
			res == 1.0f;
	}
	
	def "should return a workload of 1 because the period lasts for 1 day only"(){
		given :
			Date start = date("28/10/13");	//monday
			Date end = start;	//same monday
		
		when :
			def res = new StandardWorkloadCalendar().getWorkload(start, end)
			
		then :
			res == 1.0f;
	}
	
	def "should return a workload of 10 because this is the workload of a sprint"(){
		given :
			Date start = date("28/10/13");	//monday
			Date end = date("08/11/13");	//friday the week after
		
		when :
			def res = new StandardWorkloadCalendar().getWorkload(start, end)
			
		then :
			res == 10.0f;
	}
	
	def "should rant because the end date predate the start date"(){
		given :
			Date end = date("27/10/13") //sunday
			Date start = end.plus(1);	//monday
		
		when :
			new StandardWorkloadCalendar().getWorkload(start, end)
			
		then :
			thrown IllegalArgumentException
		
	}
	
	@Unroll("should say that workload from #startdate to #enddate is #workload")
	def "some more tests to be sure"(){
		
		expect :
			workload == new StandardWorkloadCalendar().getWorkload(date(startdate), date(enddate));
		
		where :
			startdate 				|	enddate					|	workload
			
			"30/09/13" /* mon */	|	"30/09/13" /* mon */	|	1.0f
			"04/10/13" /* fri */	|	"05/10/13" /* sat */	|	1.0f
			"30/09/13" /* mon */	|	"07/10/13" /* mon */	|	6.0f
			"30/09/13" /* mon */	|	"04/10/13" /* fri */	|	5.0f
			"01/10/13" /* tue */	|	"03/10/13" /* thu */	|	3.0f
			"05/10/13" /* sat */	|	"06/10/13" /* sun */	|	0.0f
			"05/10/13" /* sat */	|	"05/10/13" /* sat */	|	0.0f
			"05/10/13" /* sat */	|	"13/10/13" /* sun */	|	5.0f
			"06/10/13" /* sun */	|	"13/10/13" /* sun */	|	5.0f
			"07/10/13" /* mon */	|	"13/10/13" /* sun */	|	5.0f
			"09/10/13" /* wed */	|	"12/10/13" /* sat */	|	3.0f
			"30/12/13" /* mon */	|	"02/01/14" /* thu */	|	4.0f	// ahah, you get to work on january the 1st !
	}
}
