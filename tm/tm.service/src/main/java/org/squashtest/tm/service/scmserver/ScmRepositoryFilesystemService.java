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


import org.squashtest.tm.domain.scm.ScmRepository;
import org.squashtest.tm.domain.testcase.TestCase;

import java.util.Collection;

/**
 * This service provides several high-level methods for SCM content management.
 *
 */
public interface ScmRepositoryFilesystemService {

	/**
	 * Will create the script files corresponding to the given Test Cases in the scm if they don't exist,
	 * then update the content.
	 * Only scripted test cases can exist on the SCM, regular test cases will be ignored.
	 * A lock must be acquired on the SCM for the whole operation beforehand.
	 * @param scm The ScmRepository on which the Test Cases will be created/updated
	 * @param testCases The Collection of Test Cases which script files are to create/update
	 */
	void createOrUpdateScriptFile(ScmRepository scm, Collection<TestCase> testCases);

	/**
	 * Check if the ScmRepository working folder exists and create it with all absent parent folders if not.
	 * @param scm The ScmRepository of which the working folder is to create.
	 */
	void createWorkingFolderIfAbsent(ScmRepository scm);

}
