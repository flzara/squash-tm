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
package org.squashtest.tm.service.testautomation;

import javax.validation.constraints.NotNull;

import org.springframework.security.access.prepost.PreAuthorize;
import org.squashtest.tm.api.testautomation.execution.dto.TestExecutionStatus;

/**
 * @author Gregory Fouquet
 * 
 */
public interface AutomatedExecutionManagerService {

	/**
	 * Changes the state of a given execution to a new value.
	 * 
	 * @param id
	 *            id of the automated exec extender
	 * @param stateChange
	 */
	@PreAuthorize("hasRole('ROLE_TA_API_CLIENT')")
	void changeExecutionState(long id, @NotNull TestExecutionStatus stateChange);

}
