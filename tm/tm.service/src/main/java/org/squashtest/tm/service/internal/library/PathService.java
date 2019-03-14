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
package org.squashtest.tm.service.internal.library;

import java.util.List;

/**
 * This is usually not meant to be exposed through osgi
 *
 * @author Gregory Fouquet
 *
 */
public interface PathService {

	/**
	 * <p>
	 * Given an id of library NODE, return the path of this node. The path starts with /&lt;projectname&gt;.
	 * The path is slash-separated '/'. If one of the elements in the path uses a '/', it will be escaped as '\/'.
	 * </p>
	 *
	 * @param id
	 * @return
	 */
	String buildTestCasePath(long id);

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
	List<String> buildTestCasesPaths(List<Long> ids);

	/**
	 * Given an id of library NODE, returns its folders path (i.e. the path containing only the folders).
	 * For a test case in Project_1/main_folder/sub_folder_1/sub_folder_2/test_case, this method will return
	 * 'main_folder/sub_folder1/sub_folder_2'.
	 * If the given NODE is at the root of the library, the result will be null.
	 * @param id The id of the test case library node
	 * @return The folders path to the given node. Null if the node is at the root of the library.
	 */
	String buildTestCaseFoldersPath(long id);

	/**
	 * same thing than {@link #buildTestCasePath(long)}, but for requirement library nodes
	 *
	 * @param id
	 * @return
	 */
	String buildRequirementPath(long id);

	/**
	 * same thing than {@link #buildTestCasesPaths(List)}, but for requirement library nodes
	 *
	 * @param id
	 * @return
	 */
	List<String> buildRequirementsPaths(List<Long> ids);

	/**
	 * same thing than {@link #buildTestCasePath(long)}, but for campaign library nodes
	 *
	 * @param id
	 * @return
	 */
	String buildCampaignPath(long id);

	/**
	 * same thing than {@link #buildTestCasesPaths(List)}, but for requirement library nodes
	 *
	 * @param id
	 * @return
	 */
	List<String> buildCampaignPaths(List<Long> ids);

}
