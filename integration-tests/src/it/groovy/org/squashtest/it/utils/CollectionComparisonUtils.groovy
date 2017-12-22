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
package org.squashtest.it.utils

import java.lang.reflect.Array
import java.util.List;

class CollectionComparisonUtils {

	/**
	 * <p>Will compare a collection of partially ordered items against a pattern. Example :</p>
	 * 	<ul>
	 * 		<li>[1, 2, 3, 4, 5] matchesPartialOrder [ 1, [3, 2, 4], 5 ]</li>
	 * 		<li>[1, 2, 4, 3, 5] matchesPartialOrder [ [4, 1, 2], 3, [ 5 ] ]</li>
	 * 		<li>[1, 2, 3, 4, 5] matchesPartialOrder [[5, 4, 3, 2, 1]]</li> 
	 * 	<ul>
	 * 
	 * @param List toTest  : a flattened list of items partially ordered, that we want to see if they match our pattern
	 * @param List&lt;Collection&gt; pattern : a ordered list of collection of non ordered items. 
	 * @return
	 */
	static def matchesPartialOrder = { toTest, pattern ->
		long processedSize = 0;
		
		for (int i=0;i<pattern.size();i++){
			
			def congruentItems;
			def congruentSize;
			
			def item = pattern[i];
			if (item instanceof Collection){
				congruentItems = item
			}
			else{
				congruentItems = [item] as List
			}
			
			congruentSize = congruentItems.size()
			
			// select the next items to be compared to the congruent items defined in the pattern
			def _subtest = toTest[processedSize ..< processedSize+congruentSize]
			
			if (_subtest as Set != congruentItems as Set){
				return false
			}
			processedSize += congruentSize
		}	
		
		return true;
	}

}
