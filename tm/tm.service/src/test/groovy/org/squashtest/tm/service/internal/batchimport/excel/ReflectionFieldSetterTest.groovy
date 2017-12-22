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
package org.squashtest.tm.service.internal.batchimport.excel;

import org.squashtest.tm.service.internal.batchimport.testcase.excel.PropertySetterRepository

import spock.lang.Specification
import spock.lang.Unroll;

/**
 * @author Gregory Fouquet
 *
 */
class ReflectionFieldSetterTest extends Specification {
	class Foo {
		private String bar = "default"
	}

	def "should set foo.bar to #value"() {
		given:
		Foo foo = new Foo()

		when:
		ReflectionFieldSetter.forField("bar").set(value, foo)

		then:
		foo.bar == value

		where:
		value << ["baz"]
	}

	def "should set optional field"() {
		given:
		Foo foo = new Foo()

		when:
		ReflectionFieldSetter.forOptionalField("bar").set("optional", foo)

		then:
		foo.bar == "optional"
	}

	def "should not set optional field to null value"() {
		given:
		Foo foo = new Foo()

		when:
		ReflectionFieldSetter.forOptionalField("bar").set(null, foo)

		then:
		foo.bar == "default"
	}

	def "should refuse to set mandatory field to null value"() {
		given:
		Foo foo = new Foo()

		when:
		ReflectionFieldSetter.forField("bar").set(null, foo)

		then:
		thrown(NullMandatoryValueException)
	}
}
