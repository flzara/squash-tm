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
package org.squashtest.tm.domain.event;

import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementVersion;

/**
 * Interface of an event which notifies the modification of a
 * {@link Requirement}.
 * 
 * @author Gregory Fouquet
 * 
 */
public interface RequirementVersionModification {
	/**
	 * 
	 * @return the modified event. Should never return <code>null</code>
	 */
	RequirementVersion getRequirementVersion();

	/**
	 * 
	 * @return the modified property, should never return <code>null</code>
	 */
	String getPropertyName();

}