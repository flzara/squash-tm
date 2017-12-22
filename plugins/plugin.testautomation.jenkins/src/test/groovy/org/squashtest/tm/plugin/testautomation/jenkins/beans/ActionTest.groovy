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
package org.squashtest.tm.plugin.testautomation.jenkins.beans

import org.squashtest.tm.plugin.testautomation.jenkins.beans.Action;
import org.squashtest.tm.plugin.testautomation.jenkins.beans.Parameter;

import spock.lang.Specification
import spock.lang.Unroll;

class ActionTest extends Specification {

	@Unroll("assert that this action having parameter (#name, #value) is #result")
	def "should test whether that action has a parameter"(){
		
		given :
			def needle = new Parameter(name, value)
		
		
		and :
			def haystack = makeHayStack()
					   
			def action = new Action(parameters:haystack);
			
		when :
			def response = action.hasParameter(needle)
		
		then :
			response == result
			
		where :
			name			|	value			|	result
			"some-name"		|	"some-value"	|	true
			"bob"			|	"mike"			|	false
	}

	
	def makeHayStack(){
		return [
				new Parameter("name", "value"), 
				new Parameter("some-name", "some-value"),
				new Parameter("noname", "novalue")
			   ] as Parameter[];
	}
	
}
