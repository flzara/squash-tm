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
package org.squashtest.tm.service.testautomation

import org.squashtest.tm.domain.execution.ExecutionStatus
import org.squashtest.tm.service.internal.testautomation.AutomatedExecutionManagerServiceImpl

import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Gregory Fouquet
 *
 */
class AutomatedExecutionManagerServiceImplTest extends Specification {
	AutomatedExecutionManagerServiceImpl service = new AutomatedExecutionManagerServiceImpl()

	@Unroll("should coerce dto #dto into domain #status")
	def "should coerce shoot dto ExecutionStatus into domain ExecutionStatus"() {
		expect:
		status == service.coerce(dto)

		where:
		dto																		   | status
		org.squashtest.tm.api.testautomation.execution.dto.ExecutionStatus.ERROR   | ExecutionStatus.ERROR
		org.squashtest.tm.api.testautomation.execution.dto.ExecutionStatus.FAILURE | ExecutionStatus.FAILURE
		org.squashtest.tm.api.testautomation.execution.dto.ExecutionStatus.RUNNING | ExecutionStatus.RUNNING
		org.squashtest.tm.api.testautomation.execution.dto.ExecutionStatus.SUCCESS | ExecutionStatus.SUCCESS
		org.squashtest.tm.api.testautomation.execution.dto.ExecutionStatus.WARNING | ExecutionStatus.WARNING
		org.squashtest.tm.api.testautomation.execution.dto.ExecutionStatus.NOT_RUN | ExecutionStatus.NOT_RUN
		org.squashtest.tm.api.testautomation.execution.dto.ExecutionStatus.NOT_FOUND | ExecutionStatus.NOT_FOUND
	}
}
