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
package org.squashtest.tm.web.internal.helper

import org.squashtest.tm.service.internal.helper.HyphenedStringHelper;

import spock.lang.Specification;

class HyphenedStringHelperTest  extends Specification{
	def "should camelcase 'foo-bar'"() {

		when:
		def res = HyphenedStringHelper.hyphenedToCamelCase('foo-bar')

		then:
		res == 'FooBar'
	}

	def "should turn 'foo-bar' into 'foo_bar'"() {

		when:
		def res = HyphenedStringHelper.hyphenedToUnderscored('foo-bar')

		then:
		res == 'foo_bar'
	}

	def "should turn 'fooBar' into 'foo-bar'"() {

		when:
		def res = HyphenedStringHelper.camelCaseToHyphened("fooBar");

		then:
		res == 'foo-bar'
	}

}
