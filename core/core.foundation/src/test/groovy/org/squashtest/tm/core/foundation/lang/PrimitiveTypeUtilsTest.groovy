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
package org.squashtest.tm.core.foundation.lang;

import org.squashtest.tm.core.foundation.lang.PrimitiveTypeUtils;

import spock.lang.Specification;
import spock.lang.Unroll;


/**
 * @author Gregory Fouquet
 *
 */
class PrimitiveTypeUtilsTest extends Specification {
	@Unroll("#type should be a wrapper")
	def "type should be a wrapper"() {
		when: 
		def isWrapper = PrimitiveTypeUtils.isPrimitiveWrapper(type)
		
		then:
		isWrapper
		
		where:
		type << [Long, Integer, Double, Float, Boolean]
		
	}
	@Unroll("#type should be a wrapper")
	def "type should not be a wrapper"() {
		when: 
		def isWrapper = PrimitiveTypeUtils.isPrimitiveWrapper(type)
		
		then:
		!isWrapper
		
		where:
		type << [Object, String]
		
	}
	@Unroll("#wrapper should be coerced to #primitive")
	def "wrapper should be coerced to primitive"() {
		when: 
		def coercedType = PrimitiveTypeUtils.wrapperToPrimitive(wrapper)
		
		then:
		coercedType == primitive
		
		where:
		wrapper << [Long, Integer, Double, Float, Boolean]
		primitive << [long, int, double, float, boolean]
		
		
	}
	@Unroll("#nonWrapper should be coerced to null")
	def "non wrapper should be coerced to null"() {
		when: 
		def coercedType = PrimitiveTypeUtils.wrapperToPrimitive(nonWrapper)
		
		then:
		coercedType == null
		
		where:
		nonWrapper << [Object, String]
		
	}
}
