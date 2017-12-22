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
package org.squashtest.tm.exception.requirement;

import javax.validation.constraints.NotNull;

import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.TestCase;

/**
 * @author Gregory Fouquet
 *
 */
public class RequirementAlreadyVerifiedException extends VerifiedRequirementException {
	/**
	 *
	 */
	private static final long serialVersionUID = -3470201668146454658L;
	/**
	 * requirement version candidate dto verification.
	 */
	private final RequirementVersion candidateVersion;
	private final TestCase verifyier;

	/**
	 * @param version
	 * @param verifier
	 */
	public RequirementAlreadyVerifiedException(@NotNull RequirementVersion version, @NotNull TestCase verifier) {
		this.candidateVersion = version;
		this.verifyier = verifier;
	}

	/**
	 * @return the version
	 */
	public RequirementVersion getCandidateVersion() {
		return candidateVersion;
	}

	/**
	 * @return the requirement verifier
	 */
	public TestCase getVerifyingTestCase() {
		return verifyier;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.squashtest.tm.domain.VerifiedRequirementException#getShortName()
	 */
	@Override
	public String getShortName() {
		return "requirement-already-verified";
	}
}
