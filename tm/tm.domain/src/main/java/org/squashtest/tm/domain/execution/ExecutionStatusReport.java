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
package org.squashtest.tm.domain.execution;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.NotNull;

/**
 * Bean to hold the occurrence of execution steps status from a list of execution steps.
 *
 */
public class ExecutionStatusReport {
	private Map<ExecutionStatus, Integer> statusCount = new HashMap<>(
		ExecutionStatus.values().length);

	public ExecutionStatusReport() {
		super();
		for (ExecutionStatus status : ExecutionStep.LEGAL_EXEC_STATUS) {
			statusCount.put(status, 0);
		}
	}

	private int getTotal() {
		int total = 0;

		for (Integer partial : statusCount.values()) {
			total += partial;
		}

		return total;
	}

	public int get(@NotNull ExecutionStatus status) {
		return statusCount.get(status);
	}

	public void set(@NotNull ExecutionStatus status, int count) {
		statusCount.put(status, count);
	}

	/**
	 * Tells if there is at least 1 given status.
	 *
	 * @param status
	 * @return
	 */
	public boolean has(@NotNull ExecutionStatus status) {
		return statusCount.get(status) > 0;
	}

	/**
	 * Tells if all the counted statuses are of the given ones.
	 *
	 * @param statuses
	 * @return
	 */
	public boolean allOf(@NotNull ExecutionStatus... statuses) {
		Set<ExecutionStatus> uniqueStatuses = new HashSet<>(Arrays.asList(statuses));

		int expectedCount = 0;
		for (ExecutionStatus status : uniqueStatuses) {
			expectedCount += statusCount.get(status);
		}
		return expectedCount == getTotal();
	}

	public boolean anyOf(@NotNull ExecutionStatus... statuses) {
		boolean found = false;

		for (ExecutionStatus status : statuses) {
			if (has(status)) {
				found = true;
				break;
			}
		}

		return found;
	}
}
