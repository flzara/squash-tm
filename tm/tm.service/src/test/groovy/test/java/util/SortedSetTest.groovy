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
package test.java.util;

import spock.lang.Specification;

/**
 * @author Gregory Fouquet
 *
 */
public class SortedSetTest extends Specification {
	class Item implements Comparable<Item>{
		String name;
		/**
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		@Override
		public int compareTo(Item o) {
			return this.name.compareTo(o.name);
		}
		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return 7 + 13 * name.hashCode();
		}
		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			return obj.name == this.name;
		}
		
	}
	
	def "treeset should sort on insert only"() {
		given:
		def b = new Item(name: "b")
		def a = new Item(name: "a")
		
		when:
		def sortedSet = new TreeSet()
		sortedSet << a
		sortedSet << b
		
		then:
		sortedSet.inject("") { agg, it -> agg += it.name } == "ab"
		
		when:
		a.name = "z"

		then: "set is not reesorted" 
		sortedSet.inject("") { agg, it -> agg += it.name } == "zb"
		
	}
}
