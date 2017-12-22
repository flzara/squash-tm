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
package org.squashtest.tm.service.internal.dto.json;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsTreeNode {
	/**
	 * Enum items are lowercase because their serialized value is expected to be lowercase.
	 *
	 * @author Agnes Durand
	 *
	 */
	public enum State {
		open, closed, leaf
	}

	private JsTreeNodeData data = new JsTreeNodeData();

	// Attributes of the node (e.g. : id, class...)
	private Map<String, Object> attr = new HashMap<>();

	private State state = State.closed;

	private List<JsTreeNode> children = new ArrayList<>();

	public JsTreeNodeData getData() {
		return data;
	}

	public Map<String, Object> getAttr() {
		return attr;
	}

	public void addAttr(String key, String value) {
		this.attr.put(key, value);
	}

	public void addAttr(String key, Number value) {
		this.attr.put(key, value);
	}

	public void addAttr(String key, Collection<?> values) {
		this.attr.put(key, values);
	}

	public void setAttr(Map<String, Object> attr) {
		this.attr = attr;
	}

	public String getState() {
		return state.name();
	}

	public void setState(State state) {
		this.state = state;
	}

	public List<JsTreeNode> getChildren() {
		return children;
	}

	public void setChildren(List<JsTreeNode> children) {
		this.children = children;
	}

	public void addChild(JsTreeNode child) {
		this.children.add(child);
	}

	public void addAnchorAttribute(String key, String value) {
		this.data.addAttr(key, value);
	}

	public void setTitle(String title) {
		this.data.setTitle(title);
	}

	public String getTitle() {
		return data.getTitle();
	}

}
