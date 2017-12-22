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
package org.squashtest.tm.service.deletion

import java.util.List;

import org.squashtest.tm.service.internal.deletion.SubRequirementRewiringTree;
import org.squashtest.tm.service.internal.deletion.SubRequirementRewiringTree.Movement;

import spock.lang.Specification


/*
 * 1 +- 11 +- 21
 * 	 |	   +- 22
 * 	 |
 * 	 +- 12 +- 23
 * 	 |	   +- 24 +- 31
 * 	 |			 +- 32
 * 	 |
 * 	 +- 13
 *
 * 2 +- 14
 *
 */
class SubRequirementRewiringTreeTest extends Specification {

	private List<Long[]> defaultTreeHierarchy(){
		return  toListOfArrays([ [null, 1], [null, 2],
			[1, 11], [1, 12],[1, 13],[2, 14] ,
			[11, 21], [11, 22], [12, 23], [12, 24],
			[24, 31], [24, 32]
		])
	}

	private List<Long[]> toListOfArrays(List<List<Long>> l){
		List<Long[]> res = new LinkedList<Long[]>();
		for (List<Long> longs : l){
			Long[] array = new Long[2]
			array[0] = longs[0]
			array[1] = longs[1]
			res.add(array)
		}
		return res;
	}



	def "should say no movement is necessary because everything is deletable"(){

		given :
		SubRequirementRewiringTree tree = new SubRequirementRewiringTree()
		tree.build(defaultTreeHierarchy())
		tree.resolveMovements()

		when :
		Collection<Movement> movements = tree.getNodeMovements()

		then :
		movements == []

	}


	def "should say move node 32 to node 12 and node 12 to top"(){

		given :
		SubRequirementRewiringTree tree = new SubRequirementRewiringTree()
		tree.build(defaultTreeHierarchy())
		tree.markDeletableNodes([1l,2l,11l,13l,14l,21l,22l,23l,24l,31l])
		tree.resolveMovements()

		when :
		Collection<Movement> movements = tree.getNodeMovements()

		then :
		// node 32 was moved to a node known to the graph
		def mov32 = movements[0]
		mov32.id == 12l
		mov32.theParentOf == false
		mov32.newChildren == [32l] as Set

		// node 12 was itself moved to the parent of 1
		def mov12 = movements[1]
		mov12.id == 1l
		mov12.theParentOf == true
		mov12.newChildren == [12l] as Set



	}

	def "should move node 32 to top"(){
		given :
		SubRequirementRewiringTree tree = new SubRequirementRewiringTree()
		tree.build(defaultTreeHierarchy())
		tree.markDeletableNodes([1l,2l,11l,12l,13l,14l,21l,22l,23l,24l,31l])
		tree.resolveMovements()

		when :
		Collection<Movement> movements = tree.getNodeMovements()

		then :
		// node 32 was moved to a node known to the graph
		def mov32 = movements[0]
		mov32.theParentOf == true
		mov32.id == 1l
		mov32.newChildren == [32l] as Set

	}

}
