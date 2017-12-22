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
package org.squashtest.tm.service.internal.batchimport;

import org.squashtest.tm.tools.unittest.reflection.ReflectionCategory;
import org.apache.log4j.pattern.RelativeTimePatternConverter.CachedTimestamp
import org.squashtest.tm.domain.customfield.CustomFieldValue
import org.squashtest.tm.domain.customfield.InputType
import org.squashtest.tm.domain.testcase.Parameter;
import org.squashtest.tm.service.internal.batchimport.FacilityImplHelperTest.MockFacilitySupport

import spock.lang.Specification;

/**
 * @author Gregory Fouquet
 *
 */
class FacilityImplHelperTest extends Specification {

	def "should truncate to 5 characters"() {
		expect:
		new FacilityImplHelper().truncate("123456789", 5).size() == 5
	}

	def "should truncate param name to 255 characters"() {
		given:
		Parameter param = new Parameter();
		def name = ""
		300.times { name += "x" }
		param.name = name

		when:
		new FacilityImplHelper().truncate(param)

		then:
		param.name.size() == 255
	}

	
	def "shohuld fill parameter nulls with defaults"() {
		given: Parameter p = new Parameter()
		use (ReflectionCategory) {
			Parameter.set(field: "name", of: p, to: null)
			Parameter.set(field: "description", of: p, to: null)
		}

		when:
		new FacilityImplHelper().fillNullWithDefaults(p)

		then:
		p.name == ""
		p.description == ""
	}
	
	
	
	def "should truncate long custom field values unless they are for rich text"(){
		
		given :
			def longtext  = 'a' * 500
			def customfields = ['text' : longtext, 'rich' : longtext]
			
		and :
			MockFacilitySupport support = new MockFacilitySupport()
		
		when :
			def helper = new FacilityImplHelper(support)
			helper.truncateCustomfields(customfields)
		
		then :	
			customfields['text'].size() == CustomFieldValue.MAX_SIZE
			customfields['rich'].size() == 500
		
	}
	
	
	// ******************************************
	
	class MockFacilitySupport extends EntityFacilitySupport{
		MockFacilitySupport(){
			def customFieldTransator = new CustomFieldTransator()
			customFieldTransator.cufInfosCache['text'] = new CustomFieldInfos(1L, InputType.PLAIN_TEXT)
			customFieldTransator.cufInfosCache['rich'] = new CustomFieldInfos(2L, InputType.RICH_TEXT) 
			initializeCustomFieldTransator(customFieldTransator)
			
		}
	}
	
}
