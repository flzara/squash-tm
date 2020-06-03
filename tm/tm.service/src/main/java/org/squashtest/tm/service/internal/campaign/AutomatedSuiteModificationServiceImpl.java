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
package org.squashtest.tm.service.internal.campaign;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.execution.ExecutionStatusReport;
import org.squashtest.tm.domain.testautomation.AutomatedSuite;
import org.squashtest.tm.service.campaign.AutomatedSuiteModificationService;
import org.squashtest.tm.service.internal.repository.AutomatedSuiteDao;

import javax.inject.Inject;

@Transactional
@Service("AutomatedSuiteModificationService")
public class AutomatedSuiteModificationServiceImpl implements AutomatedSuiteModificationService {

	@Inject
	private AutomatedSuiteDao automatedSuiteDao;

	@Override
	public void updateExecutionStatus(AutomatedSuite automatedSuite) {
		ExecutionStatusReport report = automatedSuiteDao.getStatusReport(automatedSuite.getId());
		ExecutionStatus newExecutionStatus = ExecutionStatus.computeNewStatus(report);
		automatedSuite.setExecutionStatus(newExecutionStatus);
	}
}
