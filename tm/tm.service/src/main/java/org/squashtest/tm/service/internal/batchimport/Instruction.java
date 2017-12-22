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
package org.squashtest.tm.service.internal.batchimport;

import org.squashtest.tm.service.importer.ImportMode;
import org.squashtest.tm.service.importer.ImportStatus;
import org.squashtest.tm.service.importer.LogEntry;
import org.squashtest.tm.service.importer.Target;

public abstract class Instruction<T extends Target> {
	private final T target;
	protected final LogTrain logTrain;

	private int line;
	private ImportMode mode = ImportMode.getDefault();

	protected Instruction(T target) {
		this.logTrain = new LogTrain();
		this.target = target;
	}

	public int getLine() {
		return line;
	}

	public void setLine(int line) {
		this.line = line;
	}

	public ImportMode getMode() {
		return mode;
	}

	public void setMode(ImportMode mode) {
		if (mode != null) {
			this.mode = mode;
		}
	}

	public void addLogEntry(ImportStatus status, String messageKey, String impactKey, Object... messageArgs) {
		LogEntry entry = LogEntry.status(status)
			.forTarget(target)
			.atLine(line)
			.withMessage(messageKey, messageArgs)
			.withImpact(impactKey)
			.build();
		logTrain.addEntry(entry);
	}

	/**
	 * @return the target
	 */
	public T getTarget() {
		return target;
	}

	/**
	 * Must "execute" I agree, but more importantly must validate.
	 *
	 * @param facility
	 * @return
	 */
	public final LogTrain execute(Facility facility) {
		if (logTrain.hasCriticalErrors()) { // don't bother to execute, it's broken anyway.
			return logTrain;
		}

		LogTrain execLogTrain;

		switch (mode) {
			case CREATE:
				execLogTrain = executeCreate(facility);
				break;

			case DELETE:
				execLogTrain = executeDelete(facility);
				break;

			case UPDATE: // update is default mode when unspecified
				execLogTrain = executeUpdate(facility);
				break;

			default:
				throw new IllegalStateException("Unrecognized ImportMode " + mode
					+ ". One must have forgotten to handle new modes");
		}

		logTrain.append(execLogTrain);

		return logTrain;
	}

	protected abstract LogTrain executeUpdate(Facility facility);

	protected abstract LogTrain executeDelete(Facility facility);

	protected abstract LogTrain executeCreate(Facility facility);

}
