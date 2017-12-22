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
package org.squashtest.tm.service.internal.chart.engine;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;

import org.squashtest.tm.domain.library.structures.LibraryTree;
import org.squashtest.tm.domain.library.structures.TreeNode;

/**
 * <p>
 * 	This class represent which entities (tables) should be traversed, and in which direction (this last detail matters when a left join occurs)
 * 	This class is meant to be created via {@link DomainGraph#getQueryPlan(DetailedChartQuery)};
 *</p>
 *
 * @author bsiri
 *
 */
class QueryPlan extends LibraryTree<InternalEntityType, QueryPlan.TraversedEntity> {

	QueryPlan(){
		super();
	}

	TraversedEntity getRoot(){
		return getRootNodes().get(0);
	}

	void addNode(InternalEntityType parentKey, TraversedEntity childNode, PlannedJoin joinMeta){
		addNode(parentKey, childNode);
		TraversedEntity parent = getNode(parentKey);
		parent.addJoinInfo(childNode.getKey(), joinMeta);
	}

	public Iterator<PlannedJoin> joinIterator() {
		return new QueryPlanJoinIterator(this);
	}


	void trim(DetailedChartQuery definition){

		Collection<InternalEntityType> targets = definition.getTargetEntities();

		Queue<TraversedEntity> fifo = new LinkedList<>(getLeaves());

		while (! fifo.isEmpty()){

			TraversedEntity current = fifo.remove();

			if (current == null){
				continue;
			}

			InternalEntityType curType = current.getKey();

			// 1/ if that node is not one of the targets,
			// 2/ has no children (because not yet processed, or have a target in their own children)
			// -> prune it and enqueue the parent
			if (! targets.contains(curType) && current.getChildren().isEmpty()){
				TraversedEntity parent = current.getParent();
				if (! fifo.contains(parent)){
					fifo.add(parent);
				}
				remove(curType);
			}

		}
	}

	/**
	 * A node in the QueryPlan : it represents an entity type that WILL be traversed.
	 *
	 * @author bsiri
	 *
	 */
	static final class TraversedEntity extends TreeNode<InternalEntityType, TraversedEntity>{

		private Map<InternalEntityType, PlannedJoin> joinInfos = new HashMap<>();

		TraversedEntity(InternalEntityType type) {
			super(type);
		}

		@Override
		protected void updateWith(TraversedEntity newData) {
			// NOOP
		}


		@Override
		public String toString(){
			return getKey().toString();
		}

		void addJoinInfo(InternalEntityType outboundType, PlannedJoin joininfo){
			joinInfos.put(outboundType, joininfo);
		}

		void removeJoinInfo(InternalEntityType outboundType){
			joinInfos.remove(outboundType);
		}

		PlannedJoin getJoinInfo(InternalEntityType outboundType){
			return joinInfos.get(outboundType);
		}
	}


	private static final class QueryPlanJoinIterator implements Iterator<PlannedJoin>{

		private QueryPlan plan;

		// state variables
		private Queue<TraversedEntity> toProcess = new LinkedList<>();
		private TraversedEntity currentParent = null;
		private TraversedEntity currentChild = null;
		private Queue<TraversedEntity> remainingChildren = new LinkedList<>();
		private boolean hasNext = false;


		QueryPlanJoinIterator(QueryPlan plan){
			this.plan = plan;
			toProcess.add(plan.getRoot());
			armNext();
		}

		@Override
		public boolean hasNext() {
			return hasNext;
		}

		@Override
		public PlannedJoin next() {

			if (hasNext()){

				// create the next pair
				PlannedJoin res = currentParent.getJoinInfo(currentChild.getKey());

				// move to next
				armNext();

				// return
				return res;

			}else{
				throw new NoSuchElementException("no more joins to see");
			}

		}

		private void armNext(){
			// find the next node who has children until found or no more parent
			while (remainingChildren.isEmpty() && ! toProcess.isEmpty()){
				currentParent = toProcess.remove();
				remainingChildren.addAll(currentParent.getChildren());
			}

			// a pair is possible if both a parent and at least 1 child exist
			hasNext = !remainingChildren.isEmpty() ;

			if (hasNext){
				// arm the child
				currentChild = remainingChildren.remove();
				toProcess.add(currentChild);
			}
		}


		@Override
		public void remove() {
			throw new UnsupportedOperationException("don't do that");
		}

	}


}
