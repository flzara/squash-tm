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
package org.squashtest.tm.domain.library.structures

import org.apache.commons.collections.Transformer
import org.squashtest.tm.domain.library.structures.LibraryTree
import org.squashtest.tm.domain.library.structures.LibraryTree.TreeNodePair

import spock.lang.Specification

class LibraryTreeTest extends Specification{


	//default groovy collections for the [] notation is an implementation of List. But I have specific needs for Long[], hence the
	//scaffolding code here


	def setupSpec(){
		LibraryTree.metaClass.toListOfPair = { List arg -> return toListOfPair(delegate, arg)  }
	}

	protected List<TreeNodePair> toListOfPair(LibraryTree<Long, SubTreeNode> tree, List<List<Long>> l){

		List<TreeNodePair> res = new LinkedList<TreeNodePair>();

		for (List<Long> subList : l){
			def parent = subList[0]
			def child = new SubTreeNode(subList[1], "yeah", "yeah")

			res.add(tree.newPair(parent, child))
		}

		return res
	}


	def "should throw out of bound exception (lower)"(){

		given :
		def tree = new LibraryTree()

		and :
		tree.layers.put(0, null)
		tree.layers.put(1, null)

		when :
		tree.getLayer(-1)


		then :
		thrown(IndexOutOfBoundsException )

	}


	def "should throw out of bound exception (upper)"(){

		given :
		def tree = new LibraryTree()

		and :
		tree.layers.put(0, null)
		tree.layers.put(1, null)

		when :
		tree.getLayer(2)


		then :
		thrown(IndexOutOfBoundsException )

	}


	def "should get the node based on its data"(){

		given :
		def tree = initBigTree()


		when :
		def node = tree.getNode(22l)


		then :
		node.key == 22
		node.name == "bob22"
		node.gun == "bob22"

	}


	def "should fail to fetch an inexistant node"(){

		given :
		def tree = initBigTree();

		when :
		def node = tree.getNode(2000l);


		then :
		thrown(NoSuchElementException)

	}



	def "should add a child in the right place (using nodes), creating a layer on the fly"(){

		given :
		def tree = initBigTree();

		and :
		def parent = tree.layers.get(1).get(2)


		and :
		def child = new SubTreeNode(100, "child", "none")

		when :
		tree.addNode(parent.key, child)


		then :
		parent.children.contains child
		child.parent == parent
		child.depth == 2

		tree.getLayer(2).contains child

	}



	def "should add a child in the right place (using data), creating a layer on the fly"(){

		given :
		def tree = initBigTree();

		and :
		def parentKey = 22l;


		and :
		def child = new SubTreeNode(100, "child", "none")

		when :
		tree.addNode(parentKey, child)


		then :
		def parent = tree.getNode(22l);


		def fetchChild = parent.children.get(0)

		fetchChild.parent == parent
		fetchChild.depth == 2

		tree.getLayer(2).contains fetchChild

	}


	def "should collect all the nodes"(){
		given :
		def tree = initBigTree();

		and :
		def expectedKeys = [11, 12, 13, 14, 21, 22, 23, 24, 31, 32, 33, 34]

		when :
		def allNodes = tree.getAllNodes();
		def result = allNodes.collect {it.key }

		then :

		result == expectedKeys;


	}

	def "should collect the node data names"(){

		given :
		def tree = initBigTree()

		and :

		org.apache.commons.collections.Transformer transformer = new org.apache.commons.collections.Transformer() {

					@Override
					public Object transform(Object input) {
						return ((SubTreeNode)input).name;
					}
				};


		and :
		def expected = ["bob11", "bob12","bob13","bob14",
			"bob21", "bob22","bob23","bob24",
			"bob31", "bob32","bob33","bob34"]

		when :

		def collected = tree.collect(transformer)


		then :
		collected == expected

	}



	def "should return the depth of that tree"(){

		given :
		def tree = initBigTree()

		when :
		def depth = tree.depth

		then :
		depth == 3


	}


	def "should apply a closure in a bottom-up manner"(){

		given :
		def tree = initBigTree()

		and :
		org.apache.commons.collections.Closure closure 	= new org.apache.commons.collections.Closure() {

					private static Integer order=0;

					@Override
					public void execute(Object input) {
						((SubTreeNode)input).name = order.toString();
						order++;
					}
				};

		and :
		def expected0 = ["8", "9", "10", "11"]
		def expected1 = ["4", "5", "6", "7"]
		def expected2 = ["0", "1", "2", "3"]

		when :

		tree.doBottomUp(closure)

		def layer0 = tree.getLayer(0)
		def layer1 = tree.getLayer(1)
		def layer2 = tree.getLayer(2)

		then :

		layer2.collect {it.name }.containsAll expected2
		layer1.collect {it.name }.containsAll expected1
		layer0.collect {it.name }.containsAll expected0
	}


	def "should apply a closure in a top-down manner"(){
		given :
		def tree = initBigTree()

		and :
		org.apache.commons.collections.Closure closure 	= new org.apache.commons.collections.Closure() {

					private static Integer order=0;

					@Override
					public void execute(Object input) {
						((SubTreeNode)input).name = order.toString()
						order++;
					}
				};

		and :
		def expected0 = ["0", "1", "2", "3"]
		def expected1 = ["4", "5", "6", "7"]
		def expected2 = ["8", "9", "10", "11"]

		when :

		tree.doTopDown(closure)

		def layer0 = tree.getLayer(0)
		def layer1 = tree.getLayer(1)
		def layer2 = tree.getLayer(2)

		then :

		layer2.collect {it.name }.containsAll expected2
		layer1.collect {it.name }.containsAll expected1
		layer0.collect {it.name }.containsAll expected0
	}


	def "should return the root of the tree"(){
		given :
		def tree = initBigTree();

		when :
		def res = tree.getRootNodes()

		then :
		res.collect{it.name} as Set == ["bob11", "bob12", "bob13", "bob14"] as Set
	}

	def "should return the leaves"(){

		given :
		def tree = initBigTree()

		when :
		def res = tree.getLeaves()

		then :
		res.collect{it.name} as Set == ["bob31", "bob32", "bob33", "bob34"] as Set


	}

	def "should remove a childless node"(){

		given :
		def tree = initBigTree()

		when :
		tree.remove(32l)

		then :
		tree.collectKeys() as Set == [11l, 12l, 13l, 14l, 21l, 22l, 23l, 24l, 31l, 33l, 34l] as Set

	}

	def "should refuse to remove a non childess node"(){
		given :
		def tree = initBigTree()

		when :
		tree.remove(22l)

		then :
		thrown RuntimeException
	}

	def "should cut a subtree"(){

		given :
		def tree = initBigTree()

		when :
		tree.cut(22l)

		then :
		tree.collectKeys() as Set == [11l, 12l, 13l, 14l, 21l, 23l, 24l, 31l, 32l, 34l] as Set
	}


	def "should merge the data for the second layer only"(){

		given :
		def tree = initBigTree();

		and : "watergun for the second layer !"
		def mergeData = new ArrayList<SubTreeNode>();

		for (int i=24; i>20;i--){
			mergeData.add(new SubTreeNode(i,null, "watergun"))
		}


		and :
		def expected0 = ["bob11", "bob12", "bob13", "bob14"]
		def expected1 = ["watergun","watergun","watergun","watergun", ]
		def expected2 = ["bob31", "bob32", "bob33", "bob34"]


		when :
		tree.merge(mergeData)

		def layer0 = tree.getLayer(0).collect{it.gun}
		def layer1 = tree.getLayer(1).collect{it.gun}
		def layer2 = tree.getLayer(2).collect{it.gun}


		then :

		layer0 == expected0
		layer1 == expected1
		layer2 == expected2


	}


	def "should build a tree using a list of unsorted data (see LibraryTree#sortData comments for explanation)"(){
		given :
		def tree = new LibraryTree();

		and :
		List unsortedData = tree.toListOfPair([ [12l, 23l], [11l, 22l] , [1l, 12l], [11l, 21l], [null, 1l], [1l, 11l] ])

		when :
		tree.addNodes(unsortedData)



		then :

		tree.depth == 3

		def layer0 = tree.getLayer(0)
		def layer1 = tree.getLayer(1)
		def layer2 = tree.getLayer(2)


		def n1 = tree.getNode(1l)
		def n11 = tree.getNode(11l)
		def n12 = tree.getNode(12l)
		def n21 = tree.getNode(21l)
		def n22 = tree.getNode(22l)
		def n23 = tree.getNode(23l)



		n1.parent == null
		n1.children as Set == [n11, n12] as Set

		n11.parent == n1
		n11.children as Set == [n21, n22] as Set

		n12.parent == n1
		n12.children as Set == [n23 ] as Set

		n21.parent == n11
		n21.children as Set == [] as Set

		n22.parent == n11
		n22.children as Set == [] as Set

		n23.parent == n12
		n23.children as Set == [] as Set

		layer0.size == 1
		layer0 as Set == [n1] as Set

		layer1.size== 2
		layer1 as Set == [n11, n12] as Set

		layer2.size==3
		layer2 as Set == [n21, n22, n23] as Set



	}


	/* ***************** sort of a setup ************************** */

	private LibraryTree initBigTree(){

		def tree = new LibraryTree<Long, SubTreeNode>();


		tree.layers.put(0, new ArrayList())
		tree.layers.put(1, new ArrayList())
		tree.layers.put(2, new ArrayList())

		//layer 0

		def node11 = new SubTreeNode(11, "bob11", "bob11")
		node11.depth=0

		def node12 = new SubTreeNode(12, "bob12", "bob12")
		node12.depth=0

		def node13 = new SubTreeNode(13, "bob13", "bob13")
		node13.depth=0

		def node14 = new SubTreeNode(14, "bob14", "bob14")
		node14.depth=0

		tree.layers.get(0).add node11
		tree.layers.get(0).add node12
		tree.layers.get(0).add node13
		tree.layers.get(0).add node14


		//layer 1

		def node21 = new SubTreeNode(21, "bob21", "bob21")

		def node22 = new SubTreeNode(22, "bob22", "bob22")

		def node23 = new SubTreeNode(23, "bob23", "bob23")

		def node24 = new SubTreeNode(24, "bob24", "bob24")

		tree.layers.get(1).add node21
		tree.layers.get(1).add node22
		tree.layers.get(1).add node23
		tree.layers.get(1).add node24



		//layer 2

		def node31 = new SubTreeNode(31, "bob31", "bob31")

		def node32 = new SubTreeNode(32, "bob32", "bob32")

		def node33 = new SubTreeNode(33, "bob33", "bob33")

		def node34 = new SubTreeNode(34, "bob34", "bob34")

		tree.layers.get(2).add node31
		tree.layers.get(2).add node32
		tree.layers.get(2).add node33
		tree.layers.get(2).add node34


		//relationships
		node11.addChild node24
		node12.addChild node23
		node13.addChild node22
		node14.addChild node21

		node21.addChild node34
		node22.addChild node33
		node23.addChild node32
		node24.addChild node31


		return tree;

	}


}
