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
package org.squashtest.tm.exception.bugtracker;

import org.squashtest.tm.core.foundation.exception.ActionException;

/**
 * Thrown when trying to delete a Bugtracker that is linked to at least one RemoteSynchronisation.
 *
 * @author Aubin Guilhem
 *
 */
public class CannotDeleteBugtrackerLinkedToSynchronisationException extends ActionException {
	private static final long serialVersionUID = -1469853024587962304L;
	private static final String NOT_DELETABLE_MESSAGE_KEY =  "squashtm.action.exception.bugtracker.notdeletable.label";

	private final long bugtrackerId;

	private final String bugtrackerName;

	private final String synchronisationsProjectsNames;

	public CannotDeleteBugtrackerLinkedToSynchronisationException(long bugTrackerId, String bugtrackerName, String synchronisationsProjectsNames) {
		super("Cannot delete BugTracker[id:" + bugTrackerId + "] because it is used in remote synchronisation");
		this.bugtrackerId = bugTrackerId;
		this.bugtrackerName = bugtrackerName;
		this.synchronisationsProjectsNames = synchronisationsProjectsNames;
	}

	public long getBugtrackerId() {
		return bugtrackerId;
	}

	public String getBugtrackerName() {
		return bugtrackerName;
	}

	public String getSynchronisationsProjectsNames() {
		return synchronisationsProjectsNames;
	}

	@Override
	public String getI18nKey() {
		return NOT_DELETABLE_MESSAGE_KEY;
	}

	public Object[] messageArgs() {
		return new Object[] {bugtrackerName, synchronisationsProjectsNames};
	}
}
