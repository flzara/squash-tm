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
package org.squashtest.tm.api.widget.access;

import java.util.HashSet;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.squashtest.tm.api.security.acls.AccessRule;
import org.squashtest.tm.core.foundation.lang.Assert;

/**
 * @author Gregory Fouquet
 *
 */
public class NodeSelection implements AccessRule {
	private final SelectionMode selectionMode;

	private final Set<AccessRule> rules = new HashSet<>();

	/**
	 *
	 */
	public NodeSelection(@NotNull SelectionMode selectionMode) {
		super();
		this.selectionMode = selectionMode;
		Assert.parameterNotNull(selectionMode, "selectionMode");
	}

	/**
	 * @param selectedNodePermission
	 */
	public void addRule(SelectedNodePermission selectedNodePermission) {
		getRules().add(selectedNodePermission);

	}

	/**
	 * @param anyNode
	 */
	public void addRule(AnyNode anyNode) {
		getRules().add(anyNode);

	}

	/**
	 * @return the selectionMode
	 */
	public SelectionMode getSelectionMode() {
		return selectionMode;
	}

	/**
	 * @return the rules
	 */
	public Set<AccessRule> getRules() {
		return rules;
	}

}
