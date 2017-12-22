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
package org.squashtest.tm.domain;

import static org.junit.Assert.*;

import org.junit.Test;
import org.squashtest.tm.domain.Identified;
import org.squashtest.tm.domain.IdentifiersOrderComparator;

import spock.lang.Specification;
import spock.lang.Unroll;

/**
 * @author Gregory
 *
 */
class IdentifiersOrderComparatorTest extends Specification {
	@Unroll("#id1.id is bigger than #id2.id should be #bigger in the context of #ordered")
	def "should compare identified objects"() {
		expect: 
		(new IdentifiersOrderComparator(ordered).compare(id1, id2) > 0) == bigger
		
		where: 
		id1                    | id2                     | ordered  | bigger
		new DummyId(ident: 1L) | new DummyId(ident: 2L)  | [1L, 2L] | false
		new DummyId(ident: 1L) | new DummyId(ident: 2L)  | [2L, 1L] | true
		new DummyId(ident: 2L) | new DummyId(ident: 1L)  | [1L, 2L] | true
		new DummyId(ident: 1L) | new DummyId(ident: 1L)  | [1L, 2L] | false
		
	}
	
	def "should compare to 0"() {
		given:
		def id1 = new DummyId(ident: 1L)
		def secondId1 = new DummyId(ident: 1L)
		
		expect: 
		new IdentifiersOrderComparator([2L, 1L]).compare(id1, secondId1) == 0		
	}
}

class DummyId implements Identified {
	public Long ident
	
	public Long getId() {
		return ident
	}
}
