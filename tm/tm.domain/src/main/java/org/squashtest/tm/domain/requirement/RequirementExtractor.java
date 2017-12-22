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
package org.squashtest.tm.domain.requirement;

import java.util.ArrayList;
import java.util.List;

public class RequirementExtractor implements RequirementLibraryNodeVisitor {

	public RequirementExtractor() {
	}

	private List<Requirement> result;

	/**
	 * will go through the given nodes to return a list of the requirement ones (not folder).
	 * 
	 * @param nodes
	 * @return the requirements
	 */
	public List<Requirement> extract(List<? extends RequirementLibraryNode> nodes) {
		result = new ArrayList<>();
		for (RequirementLibraryNode node : nodes) {
			node.accept(this);
		}
		return result;
	}

	@Override
	public void visit(RequirementFolder folder) {
		// NOPE
	}

	@Override
	public void visit(Requirement requirement) {
		result.add(requirement);

	}

}
