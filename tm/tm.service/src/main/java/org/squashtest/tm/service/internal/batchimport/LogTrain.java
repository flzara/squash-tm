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

import java.util.LinkedList;
import java.util.List;

import org.squashtest.tm.service.importer.ImportMode;
import org.squashtest.tm.service.importer.ImportStatus;
import org.squashtest.tm.service.importer.LogEntry;
import org.squashtest.tm.service.importer.Target;

public class LogTrain {

	private List<LogEntry> entries = new LinkedList<>();
	private boolean criticalErrors = false;

	public void addEntry(LogEntry entry){
		if (entry != null){
			entries.add(entry);
			if (entry.getStatus() == ImportStatus.FAILURE){
				criticalErrors = true;
			}
		}
	}

	void addEntries(List<LogEntry> entries){
		for (LogEntry entry : entries){
			addEntry(entry);
		}
	}

	void append(LogTrain train){
		addEntries(train.entries);
	}


	public List<LogEntry> getEntries(){
		return entries;
	}


	public boolean hasCriticalErrors(){
		return criticalErrors;
	}

	public boolean hasNoErrorWhatsoever() {
		return entries.isEmpty();
	}


	public void setForAll(int lineNumber) {
		for (LogEntry entry : entries){
			entry.setLine(lineNumber);
		}
	}

	public void setForAll(ImportMode mode) {
		for (LogEntry entry : entries) {
			entry.setMode(mode);
		}
	}

	void setForAll(Target target){
		for (LogEntry entry : entries){
			entry.setTarget(target);
		}
	}


}
