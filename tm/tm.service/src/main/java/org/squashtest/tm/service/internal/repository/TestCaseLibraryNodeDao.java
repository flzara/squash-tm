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
package org.squashtest.tm.service.internal.repository;

import java.util.List;

import org.squashtest.tm.domain.testcase.TestCaseLibraryNode;

/**
 * @author Gregory Fouquet
 * 
 */
public interface TestCaseLibraryNodeDao extends LibraryNodeDao<TestCaseLibraryNode> {

	/**
	 * <p>
	 * Given a list of paths of library NODE, return the ids of those nodes. The path starts with /&lt;projectname&gt;.
	 * Like in {@link #getPathsAsString(List)} a path is slash-separated '/', but this time names containing a '/' don't
	 * need escaping (you may use escaped or unescaped names as will).
	 * </p>
	 * 
	 * <p>
	 * The order of the result is consistent with the order of the input. If an element could not be found (an invalid
	 * path for instance), the corresponding id in the result is NULL.
	 * </p>
	 * 
	 * @param path
	 * @return
	 */
	@Override
	List<Long> findNodeIdsByPath(List<String> path);

	/**
	 * Same as {@link #findNodeIdsByPath(List)}, for one test case only.
	 * 
	 * @param path
	 * @return the node id or <code>null</code> when not found
	 */
	@Override
	Long findNodeIdByPath(String path);

	/**
	 * 
	 * @param path
	 * @return
	 */
	List<TestCaseLibraryNode> findNodesByPath(List<String> path);

	/**
	 * 
	 * @param path
	 * @return the node at the goven path or <code>null</code>
	 */
	TestCaseLibraryNode findNodeByPath(String path);


	int countSiblingsOfNode(long nodeId);


}


