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
package org.squashtest.tm.service.internal.spring

import java.lang.reflect.Method;

import org.junit.runner.RunWith;
import org.spockframework.runtime.Sputnik;
import org.springframework.core.ParameterNameDiscoverer;
import org.squashtest.tm.service.internal.spring.CompositeDelegatingParameterNameDiscoverer;

import spock.lang.Specification;

/**
 * @author Gregory Fouquet
 *
 */
@RunWith(Sputnik)
class CompositeDelegatingParameterNameDiscovererTest extends Specification {
	ParameterNameDiscoverer firstDiscoverer = Mock()
	ParameterNameDiscoverer secondDiscoverer = Mock()
	CompositeDelegatingParameterNameDiscoverer discoverer

	def setup() {
		discoverer = new CompositeDelegatingParameterNameDiscoverer([firstDiscoverer, secondDiscoverer])
	}

	def "should resolve method param names using first discoverer"() {
		given:
		firstDiscoverer.getParameterNames(_) >> ["foo"]
		
		and:
		Method method = String.getMethod("substring", int.class)

		when:
		String[] names = discoverer.getParameterNames(method)

		then:
		names == ["foo"]
		
	}
	def "should resolve method param names using second discoverer"() {
		given:
		secondDiscoverer.getParameterNames(_) >> ["bar"]
		
		and:
		Method method = String.getMethod("substring", int.class)

		when:
		String[] names = discoverer.getParameterNames(method)

		then:
		names == ["bar"]
		
	}
	def "should not resolve method param names"() {
		given:
		Method method = String.getMethod("substring", int.class)

		when:
		String[] names = discoverer.getParameterNames(method)

		then:
		names == null
		
	}
}
