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
package org.squashtest.tm.exception;

import javax.validation.constraints.NotNull;

import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.exception.requirement.VerifiedRequirementException;

/**
 * Indicates no version of a requirement which matche the rules to be verified by a test case could be found.
 *
 * @author Gregory Fouquet
 *
 */
public class NoVerifiableRequirementVersionException extends VerifiedRequirementException {

	/**
	 *
	 */
	private static final long serialVersionUID = -3773133805010002843L;

	private final Requirement requirement;

	/**
	 * @param requirement
	 */
	public NoVerifiableRequirementVersionException(@NotNull Requirement requirement) {
		super();
		this.requirement = requirement;
	}

	public Requirement getRequirement() {
		return requirement;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.squashtest.tm.domain.VerifiedRequirementException#getShortName()
	 */
	@Override
	public String getShortName() {
		return "no-verifiable-requirement-version";
	}
}
