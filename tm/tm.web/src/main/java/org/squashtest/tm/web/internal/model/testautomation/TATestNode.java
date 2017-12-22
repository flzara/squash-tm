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
package org.squashtest.tm.web.internal.model.testautomation;

import java.util.ArrayList;
import java.util.Collection;

import org.squashtest.tm.service.internal.dto.json.JsTreeNode.State;



public class TATestNode {

	private State state = State.leaf;
	private Attr attr;
	private Data data;
	private Collection<TATestNode> children = new ArrayList<>();


	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public Attr getAttr() {
		return attr;
	}

	public void setAttr(Attr attr) {
		this.attr = attr;
	}

	public Data getData() {
		return data;
	}

	public void setData(Data data) {
		this.data = data;
	}

	public Collection<TATestNode> getChildren() {
		return children;
	}

	public void setChildren(Collection<TATestNode> children) {
		this.children = children;
	}

	public TATestNode findChild(String name){
		for (TATestNode node : children){
			if (node.attr.name.equals(name)){
				return node;
			}
		}
		return null;
	}

	static class Attr{

		private String id;
		private String rel;
		private String name;
		private String restype;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getRel() {
			return rel;
		}

		public void setRel(String rel) {
			this.rel = rel;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getRestype() {
			return restype;
		}

		public void setRestype(String restype) {
			this.restype = restype;
		}



	}

	static class Data{

		private String title;

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}


	}

}
