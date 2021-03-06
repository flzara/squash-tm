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

import org.squashtest.tm.domain.scm.ScmRepository;
import org.squashtest.tm.domain.testcase.TestCase;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface CustomScmRepositoryDao {
	/**
	 * Retrieve the Test Cases grouped by ScmRepository they should be committed into.
	 * @param testCaseIds The Collection of Test Case ids.
	 * @return A Map of Test Case Sets mapped by ScmRepository
	 */
	Map<ScmRepository, Set<TestCase>> findScriptedAndKeywordTestCasesGroupedByRepoById(Collection<Long> testCaseIds);


	/**
	 * Find URLs of the repositories declared in the application.
	 * @return the list of scm repositories' URL declared in the application.
	 */
	List<String> findDeclaredScmRepositoriesUrl();
}
