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
package org.squashtest.tm.tools.unittest.assertions

/**
 * This helper class declares various methods on List objects which can be used as assertions in unit tests. 
 * 
 * @author Gregory Fouquet
 *
 */
class ListAssertions {
	/**
	* declares an idsEquals(List) method Collection objects which collects the id property of collection items and checks they exactly match the given list of id.
	* @return
	*/
   static def declareIdsEqual() {
	   Collection.metaClass.idsEqual { List expected ->
		   def ids = delegate.collect { it.id }
		   assert ids == expected
		   return true
	   }
   }

}
