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
package org.squashtest.tm.service.testcase;

import java.util.Collection;
import java.util.List;

import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.tm.service.statistics.testcase.TestCaseStatisticsBundle;

/**
 * @author Gregory Fouquet
 * 
 */
public interface TestCaseLibraryFinderService {

	/**
	 * Returns the collection of {@link TestCaseLibrary}s which TestCases can be linked by a {@link TestCase} via a
	 * CallTestStep
	 * 
	 * @return
	 */
	List<TestCaseLibrary> findLinkableTestCaseLibraries();

	/**
	 * Returns the path of a TestCaseLibraryNode given its id. The format is standard, beginning with
	 * /&lt;project-name&gt; Item separator is '/'. If an item name contains a '\', it will be escaped as '\/'.
	 * 
	 * @param entityId
	 *            the id of the node.
	 * @return the path of that node.
	 */
	String getPathAsString(long entityId);

	/**
	 * <p>
	 * Given a list of ids of library NODE, return the path of those nodes. The path starts with /&lt;projectname&gt;.
	 * The path is slash-separated '/'. If one of the elements in the path uses a '/', it will be escaped as '\/'.
	 * </p>
	 * 
	 * <p>
	 * The order of the result is consistent with the order of the input. If an element could not be found (an invalid
	 * id for instance), the corresponding path in the result is NULL.
	 * </p>
	 * 
	 * @param ids
	 * @return
	 */
	List<String> getPathsAsString(List<Long> ids);

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
	List<Long> findNodeIdsByPath(List<String> path);

	/**
	 * Same as {@link #findNodeIdsByPath(List)}, for one test case only.
	 * 
	 * @param path
	 * @return the node id or <code>null</code>
	 */
	Long findNodeIdByPath(String path);

	/**
	 * Same than above, but returns the entities instead.
	 * 
	 * @param path
	 * @return
	 */
	List<TestCaseLibraryNode> findNodesByPath(List<String> path);

	/**
	 * Same than above, but for one path only.
	 * 
	 * @param path
	 * @return the matching node or <code>null</code>
	 */
	TestCaseLibraryNode findNodeByPath(String path);

	/**
	 * Passing the ids of some selected TestCaseLibrary and TestCaseLibraryNodes (in separate collections), will return
	 * the statistics covering all the TestCases encompassed by this selection. The test case ids that cannot be
	 * accessed for security reason will be filtered out.
	 * 
	 * 
	 * @param libraryIds
	 * @param nodeIds
	 * @return TestcaseStatisticsBundle
	 */
	TestCaseStatisticsBundle getStatisticsForSelection(Collection<Long> libraryIds, Collection<Long> nodeIds);
	


	/**
	 * Passing the ids of some selected TestCaseLibrary and TestCaseLibraryNodes (in separate collections), will return
	 * the ids of the TestCases encompassed by this selection. If includeCalledTests is true, every test cases being
	 * called directly or indirectly will be included.
	 * 
	 * If The test case ids that cannot be accessed for security reason will be filtered out.
	 * 
	 * @param libraryIds
	 * @param nodeIds
	 * @param includeCalledTests
	 * @return
	 */
	Collection<Long> findTestCaseIdsFromSelection(Collection<Long> libraryIds, Collection<Long> nodeIds,
			boolean includeCalledTests);

	/**
	 * same as {@link #findTestCaseIdsFromSelection(Collection, Collection)}, with includedCalledTests = false
	 * 
	 * @param libraryIds
	 * @param nodeIds
	 * @return
	 */
	Collection<Long> findTestCaseIdsFromSelection(Collection<Long> libraryIds, Collection<Long> nodeIds);



	/**
	 * returns how many nodes belongs to the same collection of the node being referenced by this id (this node is included in the count).
	 * 
	 * @param testCaseId
	 * @return
	 */
	int countSiblingsOfNode(long nodeId);
}