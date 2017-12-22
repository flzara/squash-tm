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
package org.squashtest.csp.core.bugtracker.domain

import spock.lang.Specification;

class BTProjectTest extends Specification{

	def "should say that we can assign a user"(){
		
		given :
			
			def project = createDefaultProject()
			project.addAllUsers([user(1), user(2), user(3)])
			
		expect :
			project.canAssignUsers() == true
		
	}
	
	def "should say that we can assign a user there too"(){
		given :
		
			def project = createDefaultProject()
			project.addAllUsers([user(1)])
			
		expect :
			project.canAssignUsers() == true
	}
	
	def "should say that we cannot assign a user (empty list)"(){
		
		given :
		
			def project = createDefaultProject()
			
		expect :
			project.canAssignUsers() == false
		
	}
	
	def "should say that we cannot assign a user (NO_USER)"(){
		
		given :
			def project = createDefaultProject()
			project.addAllUsers([User.NO_USER])
		
		expect :
			project.canAssignUsers() == false
	}
	
	def createDefaultProject(){
		return new BTProject("1", "proj1")
	}
	
	
	def user(num){
		return new User("$num", "user$num")
	}
	
}
