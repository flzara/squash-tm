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
package org.squashtest.tm.validation.validator

import org.squashtest.tm.domain.customfield.CustomField
import org.squashtest.tm.validation.validator.CustomFieldHasDefaultAsRequiredValidator

import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Gregory
 *
 */
class CustomFieldHasDefaultAsRequiredValidatorTest extends Specification {
	CustomFieldHasDefaultAsRequiredValidator validator = new CustomFieldHasDefaultAsRequiredValidator() 
	
	def "optional custom field should be valid"() {
		given:
		CustomField cf = new CustomField()
		cf.optional = true
		cf.defaultValue = null
		
		expect:
		validator.isValid(cf, null)
	}
	
	def "required custom field with default value should be valid"() {
		given:
		CustomField cf = new CustomField()
		cf.optional = false
		cf.defaultValue = "default value"
		
		expect:
		validator.isValid(cf, null)
	}

	@Unroll("required custom field with '#blank' default value should not be valid")
	def "required custom field with blank default value should not be valid"() {
		given:
		CustomField cf = new CustomField()
		cf.optional = false
		cf.defaultValue = blank
		
		expect:
		!validator.isValid(cf, null)
		
		where:
		blank << [ null, "", " ", "  " ]
	}
}
