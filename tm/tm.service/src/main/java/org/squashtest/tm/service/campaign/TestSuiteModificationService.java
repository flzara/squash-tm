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
package org.squashtest.tm.service.campaign;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.core.dynamicmanager.annotation.DynamicManager;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.execution.ExecutionStatus;

import static org.squashtest.tm.service.security.Authorizations.OR_HAS_ROLE_ADMIN;

@Transactional
@DynamicManager(name="squashtest.tm.service.TestSuiteModificationService", entity = TestSuite.class)
public interface TestSuiteModificationService extends CustomTestSuiteModificationService {

	@PreAuthorize("hasPermission(#arg0, 'org.squashtest.tm.domain.campaign.TestSuite', 'WRITE') "
			+ OR_HAS_ROLE_ADMIN)
	void changeDescription(long id, String newDescription);

	@PreAuthorize("hasPermission(#arg0, 'org.squashtest.tm.domain.campaign.TestSuite', 'WRITE') "
		+ OR_HAS_ROLE_ADMIN)
	void changeExecutionStatus(long id, ExecutionStatus executionStatus);

}
