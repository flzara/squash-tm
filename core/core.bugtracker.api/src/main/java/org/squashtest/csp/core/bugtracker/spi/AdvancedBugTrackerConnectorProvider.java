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
package org.squashtest.csp.core.bugtracker.spi;

import org.squashtest.csp.core.bugtracker.domain.BugTracker;

/**
 * Interface for providers of AdvancedBugTrackerConnector instance. A provider will create instances of connectors suitable a
 * determined kind of bug tracker. The bug tracker kind should uniquely identify the connector plugin.
 *
 * @author bsiri
 *
 */
public interface AdvancedBugTrackerConnectorProvider {
	/**
	 *
	 * @return The kind of bug tracker this provider creates connectors for.
	 */
	String getBugTrackerKind();

	/**
	 *
	 * @return A readable representation of the bug tracker kind.
	 */
	String getLabel();

	/**
	 * Creates a connector for the given bug tracker.
	 *
	 * @param bugTracker
	 *            the bug tracker to connect to. should not be <code>null</code>
	 * @return
	 */
	AdvancedBugTrackerConnector createConnector(BugTracker bugTracker);

}
