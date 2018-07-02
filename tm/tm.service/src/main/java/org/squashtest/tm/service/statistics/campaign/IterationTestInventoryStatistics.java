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
package org.squashtest.tm.service.statistics.campaign;

import java.util.HashMap;
import java.util.Map;

import org.squashtest.tm.domain.execution.ExecutionStatus;

public final class IterationTestInventoryStatistics {

	private String iterationName;
	private Map<ExecutionStatus, Integer> statistics;

	public IterationTestInventoryStatistics() {
		initStatistics();
	}

	private void initStatistics() {
		statistics = new HashMap<>(ExecutionStatus.values().length);
		for (ExecutionStatus status : ExecutionStatus.values()) {
			statistics.put(status, 0);
		}

	}

	public String getIterationName() {
		return iterationName;
	}

	public void setIterationName(String iterationName) {
		this.iterationName = iterationName;
	}

	public int getNbReady() {
		return this.statistics.get(ExecutionStatus.READY);
	}

	public int getNbRunning() {
		return this.statistics.get(ExecutionStatus.RUNNING);
	}

	public int getNbSuccess() {
		return this.statistics.get(ExecutionStatus.SUCCESS);
	}

	public int getNbFailure() {
		return this.statistics.get(ExecutionStatus.FAILURE);
	}

	public int getNbBlocked() {
		return this.statistics.get(ExecutionStatus.BLOCKED);
	}

	public int getNbUntestable() {
		return this.statistics.get(ExecutionStatus.UNTESTABLE);
	}

	public int getNbWarning() {
		return this.statistics.get(ExecutionStatus.WARNING);
	}

	public int getNbSettled() {
		return this.statistics.get(ExecutionStatus.SETTLED);
	}

	public int getNbError() {
		return this.statistics.get(ExecutionStatus.ERROR);
	}

	public int getNbNotRun() {
		return this.statistics.get(ExecutionStatus.NOT_RUN);
	}

	public void setNumber(int intValue, ExecutionStatus status) {

		this.statistics.put(status, intValue);

	}

}
