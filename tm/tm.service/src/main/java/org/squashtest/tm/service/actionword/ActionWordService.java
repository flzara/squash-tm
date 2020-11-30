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
package org.squashtest.tm.service.actionword;

import java.util.Collection;
import java.util.Map;

public interface ActionWordService {
	Collection<String> findAllMatchingActionWords(long projectId, String searchInput);

	/**
	 * This method retrieves all duplicated action word for the inputActionWord, with its project name
	 * Only returns a result if any duplicated action word in other projects than the current one
	 * Else returns an empty map.
	 * @param currentProjectId the project id of the current project
	 * @param inputActionWord the action word which the user wants to add
	 * @return a map where the key is the project name and the value the action word id
	 */
	Map<String, Long> findAllDuplicatedActionWithProject(long currentProjectId, String inputActionWord);

	/**
	 * This method retrieves all duplicated action word for the inputActionWord, with its project name
	 * Only returns a result if any duplicated action word in other projects than the current one AND if the token has changed
	 * Else returns an empty map.
	 * @param currentProjectId the project id of the current project
	 * @param testStepId the keyword test step id
	 * @param inputActionWord the action word which the user wants to add
	 * @return a map where the key is the project name and the value the action word id
	 */
	Map<String, Long> findAllDuplicatedActionWithProjectWithChangingToken(long currentProjectId, long testStepId, String inputActionWord);

	String changeDescription(long actionWordId, String newDescription);
}
