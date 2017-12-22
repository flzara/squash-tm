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
package org.squashtest.tm.api.report.form;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * @author Gregory Fouquet
 *
 */
public class TreePicker extends BasicInput implements InitializingBean {
	private NodeType pickedNodeType;

	private int nodeSelectionLimit = -1;

	//if true, the tree picker will accept ONLY the designed type and no folders/libraries/whatever.
	//the js implementation is only made for CAMPAIGN and ITERATION nodes.
	private boolean strict = false;

	//allow configuration of a js tree with different type than NodeType.
	//ie we can fetch campaign library node and select iteration in tree.
	private String jsTreeNodeType;

	/**
	 * @see org.squashtest.tm.api.report.form.Input#getType()
	 */
	@Override
	public InputType getType() {
		return InputType.TREE_PICKER;
	}

	/**
	 * @param pickedNodeType
	 *            the pickedNodeType to set
	 */
	public void setPickedNodeType(NodeType pickedNodeType) {
		this.pickedNodeType = pickedNodeType;
	}

	/**
	 * @return the pickedNodeType
	 */
	public NodeType getPickedNodeType() {
		return pickedNodeType;
	}

	public int getNodeSelectionLimit() {
		return nodeSelectionLimit;
	}

	public void setNodeSelectionLimit(int nodeSelectionLimit) {
		this.nodeSelectionLimit = nodeSelectionLimit;
	}

	public boolean isStrict() {
		return strict;
	}

	public void setStrict(boolean strict) {
		this.strict = strict;
	}

	public String getJsTreeNodeType() {
		if(this.jsTreeNodeType !=null ){
			return jsTreeNodeType;
		}
		return pickedNodeType.name();
	}

	public void setJsTreeNodeType(String jsTreeNodeType) {
		this.jsTreeNodeType = jsTreeNodeType;
	}

	/**
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(pickedNodeType, "pickedNodeType was not initialized. You shoud set it with a value from the NodeType enum");

	}

}
