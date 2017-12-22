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
package org.squashtest.tm.service.deletion;

import java.util.ArrayList;
import java.util.Collection;

public class OperationReport {

	private Collection<Node> removed = new ArrayList<>();
	private Collection<NodeRenaming> renamed = new ArrayList<>();
	private Collection<NodeMovement> moved = new ArrayList<>();
	private Collection<NodeReferenceChanged> refchanges = new ArrayList<>();
	
	
	public Collection<Node> getRemoved() {
		return removed;
	}

	public Collection<NodeRenaming> getRenamed() {
		return renamed;
	}

	public Collection<NodeMovement> getMoved() {
		return moved;
	}
	
	public Collection<NodeReferenceChanged> getReferenceChanges(){
		return refchanges;
	}

	public void mergeWith(OperationReport other){
		this.removed.addAll(other.getRemoved());
		this.renamed.addAll(other.getRenamed());
		this.moved.addAll(other.getMoved());
		this.refchanges.addAll(other.getReferenceChanges());
	}
	
	public void addRemoved(Node removednode){
		removed.add(removednode);
	}	
	
	public void addRemoved(String nodetype, Long nodeId){
		addRemoved(new Node(nodeId, nodetype));
	}
	
	public void addRemoved(Collection<Node> toRemove){
		removed.addAll(toRemove);
	}
	
	public void addRemoved(Collection<Long> ids, String nodeType){
		for (Long id : ids){
			addRemoved(new Node(id, nodeType));
		}
	}
	
	public void addRenamed(NodeRenaming renaming){
		renamed.add(renaming);
	}
	
	public void addRenamed(String nodetype, Long nodeid, String newName){
		addRenamed(new NodeRenaming(new Node(nodeid, nodetype), newName));
	}
	
	public void addRenamed(Collection<NodeRenaming> renamings){
		renamed.addAll(renamings);
	}
	
	public void addMoved(NodeMovement movement){
		moved.add(movement);
	}
	
	public void addMoved(Collection<NodeMovement> movements){
		moved.addAll(movements);
	}
	
	public void addReferenceChanges(NodeReferenceChanged rerefs){
		refchanges.add(rerefs);
	}
	
	public void addReferenceChanges(Collection<NodeReferenceChanged> rerefs){
		refchanges.addAll(rerefs);
	}
	
	

}



