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
package org.squashtest.tm.domain.library.structures;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;


public class GraphNode<IDENT, T extends GraphNode<IDENT, T>>{

	protected IDENT key;

	/*
	 * implementation note : the use of a List is important here, not because
	 * we need an arbitrary order, but rather because a node can appear multiple times
	 * (edge cardinality)
	 */
	protected final Collection<T> inbounds = new LinkedList<>();
	protected final Collection<T> outbounds = new LinkedList<>();


	public GraphNode(){

	}

	public GraphNode(IDENT key){
		this.key=key;
	}


	public Collection<T> getInbounds() {
		return inbounds;
	}


	public Collection<T> getOutbounds() {
		return outbounds;
	}

	public void addInbound(T inbound){
		if (inbound!=null){
			inbounds.add(inbound);
		}
	}

	public void addOutbound(T outbound){
		if (outbound!=null){
			outbounds.add(outbound);
		}
	}

	public void removeInbound(T inbound){
		inbounds.remove(inbound);
	}

	public void removeOutbount(T outbound){
		outbounds.remove(outbound);
	}

	public IDENT getKey(){
		return key;
	}

	public void setKey(IDENT key){
		this.key=key;
	}


	@Override
	//GENERATED:START
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (key == null ? 0 : key.hashCode());
		return result;
	}
	//GENERATED:END

	@SuppressWarnings("unchecked")
	@Override
	//GENERATED:START
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		T other = (T) obj;
		if (key == null) {
			if (other.key != null) {
				return false;
			}
		} else if (!key.equals(other.key)) {
			return false;
		}
		return true;
	}
	//GENERATED:END

	/**
	 * Remove all outbound edges from this node to the other. One can still
	 * navigate from the other node to this node.
	 * @param node
	 */
	public void disconnect(T node){

		for (Iterator<T> iter = node.inbounds.iterator(); iter.hasNext();){
			if (iter.next().equals(this)){
				iter.remove();
			}
		}

		for (Iterator<T> iter = outbounds.iterator(); iter.hasNext();){
			if (iter.next().equals(node)){
				iter.remove();
			}
		}
	}


}
