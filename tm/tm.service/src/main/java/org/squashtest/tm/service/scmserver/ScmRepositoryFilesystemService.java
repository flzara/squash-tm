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
package org.squashtest.tm.service.scmserver;


import java.util.Collection;

/**
 * This service provides several high-level methods for SCM content management.
 *
 */
public interface ScmRepositoryFilesystemService {

	/**
	 * Given a collection of test case ids, will create or update the script file
	 * in the relevant scm. The scm is the one bound to the projects in which the
	 * test cases exist. Only scripted test cases can also exist on the SCM, regular
	 * test cases will be ignored.
	 *
	 * @param testCaseIds
	 */
	void createOrUpdateScriptFile(Collection<Long> testCaseIds);

}
