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
package org.squashtest.tm.domain.event;


import org.squashtest.tm.domain.event.RequirementLargePropertyChange
import org.squashtest.tm.domain.requirement.RequirementVersion

import spock.lang.Specification

class RequirementLargePropertyChangeTest extends Specification {
	def "should build a property change event"() {
		given:
		RequirementVersion rv = new RequirementVersion()
		
		when:
		RequirementLargePropertyChange event = RequirementLargePropertyChange.builder()
			.setModifiedProperty("name")
			.setOldValue("foo")
			.setNewValue("bar")
			.setSource(rv)
			.setAuthor("proust")
			.build()
			
		then:
		event.requirementVersion == rv
		event.oldValue == "foo"
		event.newValue == "bar"
		event.propertyName == "name"
		event.author == "proust"
	}
	
	def "should build event with null values"() {
		given:
		RequirementVersion rv = new RequirementVersion()
		
		when:
		RequirementLargePropertyChange event = RequirementLargePropertyChange.builder()
			.setModifiedProperty("name")
			.setSource(rv)
			.setAuthor("proust")
			.build()
			
		then:
		notThrown(NullPointerException)
		event.requirementVersion == rv
		event.oldValue == ""
		event.newValue == ""
		event.propertyName == "name"
		event.author == "proust"
	}

}
