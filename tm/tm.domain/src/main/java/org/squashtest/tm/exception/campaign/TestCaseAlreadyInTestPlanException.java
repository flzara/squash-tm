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
package org.squashtest.tm.exception.campaign;

import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.testcase.TestCase;

/**
 * Indicates an attempt to put a test case in a test plan which already contains it.
 *
 * @author Gregory Fouquet
 *
 */
public class TestCaseAlreadyInTestPlanException extends RuntimeException {
	/**
	 *
	 */
	private static final long serialVersionUID = -8339912162503987165L;

	private final TestCase testCase;
	private final Campaign campaign;

	public TestCaseAlreadyInTestPlanException(TestCase testCase, Campaign campaign) {
		super();
		this.testCase = testCase;
		this.campaign = campaign;
	}

	public TestCase getTestCase() {
		return testCase;
	}

	public Campaign getCampaign() {
		return campaign;
	}

}
