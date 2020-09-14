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

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.core.dynamicmanager.annotation.DynamicManager;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseStatus;
import org.squashtest.tm.service.annotation.CheckLockedMilestone;
import org.squashtest.tm.service.annotation.Id;

import static org.squashtest.tm.service.security.Authorizations.OR_HAS_ROLE_ADMIN;

@Transactional
@DynamicManager(name="squashtest.tm.service.TestCaseModificationService", entity = TestCase.class)
public interface TestCaseModificationService extends CustomTestCaseModificationService, TestCaseFinder {
	/**
	 * chage description
	 * @param testCaseId test case id
	 * @param newDescription new description
	 */
	@PreAuthorize(TEST_CASE_IS_WRITABLE)
	@CheckLockedMilestone(entityType = TestCase.class)
	void changeDescription(@Id long testCaseId, String newDescription);


	@PreAuthorize(TEST_CASE_IS_WRITABLE)
	@CheckLockedMilestone(entityType = TestCase.class)
	void changeStatus(@Id long testCaseId, TestCaseStatus status);

	@PreAuthorize("hasPermission(#arg0, 'org.squashtest.tm.domain.testcase.TestCase' , 'WRITE')" + OR_HAS_ROLE_ADMIN)
	@CheckLockedMilestone(entityType = TestCase.class)
	void changePrerequisite(@Id long testCaseId, String newPrerequisite);

}
