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
package org.squashtest.tm.service.internal.testautomation;

import java.net.URL;
import java.util.List;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.api.testautomation.execution.dto.TestExecutionStatus;
import org.squashtest.tm.core.foundation.exception.InvalidUrlException;
import org.squashtest.tm.core.foundation.lang.UrlUtils;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.testautomation.AutomatedExecutionExtender;
import org.squashtest.tm.service.execution.ExecutionProcessingService;
import org.squashtest.tm.service.internal.repository.AutomatedExecutionExtenderDao;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.testautomation.AutomatedExecutionManagerService;
import org.squashtest.tm.service.testautomation.AutomatedExecutionSetIdentifier;
import org.squashtest.tm.service.testautomation.TestAutomationCallbackService;

/**
 * @author Gregory Fouquet
 *
 */
@Service("squashtest.tm.service.testautomation.AutomatedExecutionManagerService")
@Transactional
public class AutomatedExecutionManagerServiceImpl implements AutomatedExecutionManagerService,
TestAutomationCallbackService {
	private static final Logger LOGGER = LoggerFactory.getLogger(AutomatedExecutionManagerServiceImpl.class);

	@Inject
	private AutomatedExecutionExtenderDao automatedExecutionDao;

	@Inject
	private PermissionEvaluationService permissionService;

	@Inject
	private ExecutionProcessingService execProcService;

	@Override
	public void updateExecutionStatus(AutomatedExecutionSetIdentifier execIdentifier, ExecutionStatus newStatus) {

		List<AutomatedExecutionExtender> execs = findExtendersFor(execIdentifier);

		for (AutomatedExecutionExtender exec : execs) {
			permissionService.hasRoleOrPermissionOnObject("ROLE_ADMIN", "EXECUTE", exec);
			exec.setExecutionStatus(newStatus);
			execProcService.updateExecutionMetadata(exec);
		}

	}

	@Override
	public void updateResultURL(AutomatedExecutionSetIdentifier execIdentifier, URL resultURL) {

		List<AutomatedExecutionExtender> execs = findExtendersFor(execIdentifier);

		for (AutomatedExecutionExtender exec : execs) {
			permissionService.hasRoleOrPermissionOnObject("ROLE_ADMIN", "EXECUTE", exec);
			exec.setResultURL(resultURL);
		}
	}

	@Override
	public void updateResultSummary(AutomatedExecutionSetIdentifier execIdentifier, String newSummary) {

		List<AutomatedExecutionExtender> execs = findExtendersFor(execIdentifier);

		for (AutomatedExecutionExtender exec : execs) {
			permissionService.hasRoleOrPermissionOnObject("ROLE_ADMIN", "EXECUTE", exec);
			exec.setResultSummary(newSummary);
		}

	}

	private List<AutomatedExecutionExtender> findExtendersFor(AutomatedExecutionSetIdentifier setIdentifier) {
		return automatedExecutionDao.findAllBySuiteIdAndTestName(setIdentifier.getAutomatedSuiteId(),
				setIdentifier.getAutomatedTestName(), setIdentifier.getTestAutomationProjectName());
	}

	/**
	 * @param exec
	 * @param stateChange
	 */
	private void changeState(AutomatedExecutionExtender exec, TestExecutionStatus stateChange) {
		exec.setResultSummary(stateChange.getStatusMessage());
		exec.setExecutionStatus(coerce(stateChange.getStatus()));

		try {
			URL result = UrlUtils.toUrl(stateChange.getResultUrl());
			exec.setResultURL(result);
		} catch (InvalidUrlException ex) {
			LOGGER.warn("Received a result url which does not math any valid url pattern : {}",
					stateChange.getResultUrl(), ex);
		}

		execProcService.updateExecutionMetadata(exec);

	}

	/**
	 * @param status
	 * @return
	 */
	private ExecutionStatus coerce(org.squashtest.tm.api.testautomation.execution.dto.ExecutionStatus status) {
		return ExecutionStatus.valueOf(status.name());
	}

	/**
	 * @see org.squashtest.tm.service.testautomation.AutomatedExecutionManagerService#changeExecutionState(long,
	 *      org.squashtest.tm.api.testautomation.execution.dto.TestExecutionStatus)
	 */
	@Override
	public void changeExecutionState(long id, @NotNull TestExecutionStatus stateChange) {
		changeState(automatedExecutionDao.findById(id), stateChange);

	}

}
