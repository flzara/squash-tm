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

public final class Node{
	private Long resid;
	private String rel;
	public Node(Long id, String nodetype){
		this.resid = id;
		this.rel = nodetype;
	}
	public Node() {
		super();
	}
	public Long getResid() {
		return resid;
	}
	public void setId(Long id) {
		this.resid = id;
	}
	public String getRel() {
		return rel;
	}
	public void setRel(String nodetype) {
		this.rel = nodetype;
	}
	
}
