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

import org.squashtest.tm.service.internal.deletion.LockedFolderInferenceTree;

import spock.lang.Specification;

class LockedFolderInferenceTreeTest extends Specification {

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



	def "should build the node hierarchy tree"(){
		given :
		def hierarchy = defaultTreeHierarchy()

		and :
		def expectedAllNodeSize = 12
		def expectedAllNodeIds = [2, 1, 14, 13, 12, 11, 24, 23, 22, 21, 32, 31]
		def expectedAllNodeDepth = [0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3]

		def expectedLayer0Size = 2
		def expectedLayer0Ids = [2, 1]
		def expectedLayer0Depth = [0, 0]

		def expectedLayer1Size = 4
		def expectedLayer1Ids = [14, 13, 12, 11]
		def expectedLayer1Depth = [1, 1, 1, 1]

		def expectedLayer2Size = 4
		def expectedLayer2Ids = [24, 23, 22, 21]
		def expectedLayer2Depth = [2, 2, 2, 2]

		def expectedLayer3Size = 2
		def expectedLayer3Ids = [32, 31]
		def expectedLayer3Depth = [3, 3]

		when :
		def tree = new LockedFolderInferenceTree();
		tree.build(hierarchy)

		then :
		tree.depth==4
		def allnodes =tree.allNodes
		def layer0 = tree.getLayer(0)
		def layer1 = tree.getLayer(1)
		def layer2 = tree.getLayer(2)
		def layer3 = tree.getLayer(3)

		allnodes.size == expectedAllNodeSize
		allnodes.collect{it.key} as Set == expectedAllNodeIds as Set
		allnodes.collect {it.depth } as Set == expectedAllNodeDepth as Set

		layer0.size == expectedLayer0Size
		layer0.collect{it.key} as Set == expectedLayer0Ids as Set
		layer0.collect{it.depth } as Set == expectedLayer0Depth as Set
		layer0.collect{it.children }.flatten() as Set == layer1 as Set

		layer1.size == expectedLayer1Size
		layer1.collect{it.key} as Set == expectedLayer1Ids as Set
		layer1.collect{it.depth } as Set == expectedLayer1Depth as Set
		layer1.collect{it.children }.flatten() as Set == layer2 as Set

		layer2.size == expectedLayer2Size
		layer2.collect{it.key}  as Set== expectedLayer2Ids as Set
		layer2.collect {it.depth } as Set == expectedLayer2Depth as Set
		layer2.collect{it.children }.flatten() as Set ==  layer3 as Set

		layer3.size == expectedLayer3Size
		layer3.collect{it.key} as Set == expectedLayer3Ids as Set
		layer3.collect{it.depth } as Set == expectedLayer3Depth as Set
		layer3.collect{it.children }.flatten() as Set == [] as Set

	}



	def "should infer that some folders are non deletable because of their children"(){

		given :
		def treeAsList = defaultTreeHierarchy();
		def tree = new LockedFolderInferenceTree();
		tree.build(treeAsList)


		when :
		tree.markLockedNodes([32l , 22l ])
		tree.resolveLockedFolders();


		then :

		tree.getNode(1l).deletable  == false
		tree.getNode(2l).deletable  == true
		tree.getNode(11l).deletable == false
		tree.getNode(12l).deletable == false
		tree.getNode(13l).deletable == true
		tree.getNode(14l).deletable == true
		tree.getNode(21l).deletable == true
		tree.getNode(22l).deletable == false
		tree.getNode(23l).deletable == true
		tree.getNode(24l).deletable == false
		tree.getNode(31l).deletable == true
		tree.getNode(32l).deletable == false

	}


	def "should collect only deletable ids in the tree"(){

		given :
		def treeAsList = defaultTreeHierarchy();

		def tree = new LockedFolderInferenceTree()
		tree.build(treeAsList)

		and :
		tree.getNode(1l).deletable=false;
		tree.getNode(11l).deletable=false;
		tree.getNode(12l).deletable=false;
		tree.getNode(22l).deletable=false;
		tree.getNode(24l).deletable=false;
		tree.getNode(32l).deletable=false;

		and :
		def expectedList = [2, 14, 13, 23, 21, 31]

		when :

		def result =tree.collectDeletableIds()


		then :
		result as Set == expectedList as Set


	}




}
