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
package org.squashtest.tm.plugin.testautomation.jenkins.internal



import org.squashtest.tm.plugin.testautomation.jenkins.internal.JsonParser;

import spock.lang.Specification

class JsonParserTest extends Specification {

	 JsonParser parser;
	 
	 def setup(){
		 parser = new JsonParser();
	 } 
	
	 
	 def "should return a collection of projects"(){
		 
		 given :
		 	def json ='{"jobs":[{"name":"bob","color":"bob"},{"name":"mike","color":"mike"},{"name":"robert","color":"robert"}]}'
		 
		 when : 
		 	def res = parser.readJobListFromJson(json)
		 
		 then :
		 	res.collect{it.name} == ["bob", "mike", "robert"]
	 }

	 def "should return a collection of projects excluding the disabled one"(){
		 
		 given :
			 def json ='{"jobs":[{"name":"bob","color":"'+JsonParser.DISABLED_COLOR_STRING+'"},{"name":"mike","color":"mike"},{"name":"robert","color":"robert"}]}'
		 
		 when :
			 def res = parser.readJobListFromJson(json)
		 
		 then :
			 res.collect{it.name} == ["mike", "robert"]
	 }



}
