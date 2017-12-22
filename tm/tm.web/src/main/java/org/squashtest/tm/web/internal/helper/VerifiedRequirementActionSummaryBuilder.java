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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.squashtest.tm.exception.NoVerifiableRequirementVersionException;
import org.squashtest.tm.exception.requirement.RequirementAlreadyVerifiedException;
import org.squashtest.tm.exception.requirement.RequirementVersionNotLinkableException;
import org.squashtest.tm.exception.requirement.VerifiedRequirementException;

/**
 * Helper class which builds a summary for a verified requirements related action. This summary is to be sent to the
 * browser as a JSON.
 *
 * @author Gregory Fouquet
 *
 */
public final class VerifiedRequirementActionSummaryBuilder {

	/**
	 *
	 */
	private VerifiedRequirementActionSummaryBuilder() {
		super();
	}

	/**
	 * Builds a summary for an addition of verified requirements / verifying test cases from a list of rejections.
	 *
	 * @param rejections
	 * @return
	 */
	public static Map<String, Object> buildAddActionSummary(Collection<VerifiedRequirementException> rejections) {
		Map<String, Object> summary = new HashMap<>();

		for (VerifiedRequirementException rejection : rejections) {
			if (rejection instanceof RequirementAlreadyVerifiedException) {
				summary.put("alreadyVerifiedRejections", Boolean.TRUE);
			} else if (rejection instanceof RequirementVersionNotLinkableException) {
				summary.put("notLinkableRejections", Boolean.TRUE);
			} else if (rejection instanceof NoVerifiableRequirementVersionException) {
				summary.put("noVerifiableVersionRejections", Boolean.TRUE);
			}
		}
		return summary;
	}

}
