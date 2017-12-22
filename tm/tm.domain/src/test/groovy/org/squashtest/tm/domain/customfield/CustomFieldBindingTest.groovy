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
package org.squashtest.tm.domain.customfield

import org.squashtest.tm.domain.customfield.CustomFieldBinding
import org.squashtest.tm.domain.customfield.CustomFieldBinding.PositionAwareBindingList

import spock.lang.Specification




class CustomFieldBindingTest extends Specification {
	
	// ************* tests for PositionAwareBindingList *************************

	
	def "should correctly reorder a collection of bindings"(){
		
		given :
			def baseList = [ 
				createWith(1l, 1),
				createWith(2l, 2),
				createWith(3l, 3),
				createWith(4l, 4),
				createWith(5l, 5)				
			]
		
		
		when :
			PositionAwareBindingList list = new PositionAwareBindingList(baseList)
			list.reorderItems([3l,5l], 1)
		
		
		then :
			list.collect{it.id} == [1l, 3l, 5l, 2l, 4l]
			list.collect{it.position} == [1,2,3,4,5]
	}
	
	
	
	def createWith ={ id, position ->
		return new CustomFieldBinding(id : id, position : position)
	}
	
	
	
}