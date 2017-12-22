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
import org.squashtest.tm.core.dynamicmanager.factory.DynamicManagerInterfaceSpecification
import org.squashtest.tm.domain.infolist.SystemListItem;
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.domain.testcase.TestCaseExecutionMode
import org.squashtest.tm.domain.testcase.TestCaseImportance
import org.squashtest.tm.domain.testcase.TestCaseNature;
import org.squashtest.tm.domain.testcase.TestCaseStatus;
import org.squashtest.tm.domain.testcase.TestCaseType;
import org.squashtest.tm.service.testcase.TestCaseModificationService;

import spock.lang.Shared

/**
 * @author Gregory Fouquet
 *
 */
class TestCaseModificationDynamicServiceTest extends DynamicManagerInterfaceSpecification {
	@Shared Class entityType = TestCase
	@Shared Class managerType = TestCaseModificationService

	@Shared List changeServiceCalls = [{
			it.changeDescription(10L, "foo")
		}, {
			it.changeImportance(10L, TestCaseImportance.HIGH)
		}, {
			it.changeReference(10L, "ref")
		}, {
			it.changeStatus(10L, TestCaseStatus.TO_BE_UPDATED)
		}, {
			it.changePrerequisite(10L, "prq")
		}]
}
