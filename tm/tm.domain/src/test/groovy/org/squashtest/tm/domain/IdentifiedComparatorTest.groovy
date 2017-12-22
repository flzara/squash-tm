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

import org.squashtest.tm.domain.Identified;
import org.squashtest.tm.domain.IdentifiedComparator;

import spock.lang.Specification;
import spock.lang.Unroll;

/**
 * @author Gregory
 *
 */
class IdentifiedComparatorTest extends Specification {
	@Unroll("#id1.id is bigger than #id2.id should be #bigger")
	def "should compare identified objects"() {
		expect: 
		(IdentifiedComparator.instance.compare(id1, id2) > 0) == bigger
		
		where: 
		id1               | id2                | bigger
		new Id()          | new Id(ident: 1L)  | false
		new Id(ident: 1L) | new Id()           | true
		new Id(ident: 1L) | new Id(ident: 2L)  | false
		new Id(ident: 2L) | new Id(ident: 1L)  | true
		
	}
	
}

class Id implements Identified {
	public Long ident
	
	public Long getId() {
		return ident
	}
}


