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
package org.squashtest.tm.web.internal.helper;

import org.squashtest.tm.exception.requirement.link.AlreadyLinkedRequirementVersionException;
import org.squashtest.tm.exception.requirement.link.LinkedRequirementVersionException;
import org.squashtest.tm.exception.requirement.link.SameRequirementLinkedRequirementVersionException;
import org.squashtest.tm.exception.requirement.link.UnlinkableLinkedRequirementVersionException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper class which builds a summary for a linked-requirements related action. This summary is to be sent to the
 * browser as a JSON.
 *
 * @author jlor
 *
 */
public final class LinkedRequirementVersionActionSummaryBuilder {

	/**
	 *
	 */
	private LinkedRequirementVersionActionSummaryBuilder() {
		super();
	}

	/**
	 * Builds a summary for an addition of linked requirement versions from a list of rejections.
	 *
	 * @param rejections
	 * @return
	 */
	public static Map<String, Object> buildAddActionSummary(Collection<LinkedRequirementVersionException> rejections) {
		Map<String, Object> summary = new HashMap<>();

		for (LinkedRequirementVersionException rejection : rejections) {
			if (rejection instanceof UnlinkableLinkedRequirementVersionException) {
				summary.put("notLinkableRejections", Boolean.TRUE);
			} else if (rejection instanceof AlreadyLinkedRequirementVersionException) {
				summary.put("alreadyLinkedRejections", Boolean.TRUE);
			} else if (rejection instanceof SameRequirementLinkedRequirementVersionException) {
				summary.put("sameRequirementRejections", Boolean.TRUE);
			}
		}
		return summary;
	}

}
