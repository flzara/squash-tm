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

import java.util.Collection;

import org.squashtest.tm.service.importer.LogEntry;

public final class FacilityUtils {
	private FacilityUtils() {
	}


	/**
	 * @param train
	 *            : the {@link LogTrain} to check if contains "import as action step" warning
	 * @return i18n error message associated to the "import as action step" log entry
	 */
	public static final String mustImportCallAsActionStep(LogTrain train) {
		Collection<LogEntry> entries = train.getEntries();
		for (LogEntry entry : entries) {
			if (entry.getI18nImpact().equals(Messages.IMPACT_CALL_AS_ACTION_STEP)) {
				return entry.getI18nError();
			}
		}
		return null;
	}
}
