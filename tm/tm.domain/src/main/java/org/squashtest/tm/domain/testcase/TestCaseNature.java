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
package org.squashtest.tm.domain.testcase;

import org.squashtest.tm.domain.Level;

@Deprecated
public enum TestCaseNature implements Level{

	UNDEFINED(1),
	FUNCTIONAL_TESTING(2),
	BUSINESS_TESTING(3),
	USER_TESTING(4),
	NON_FUNCTIONAL_TESTING(5),
	PERFORMANCE_TESTING(6),
	SECURITY_TESTING(7),
	ATDD(8);

	private static final String I18N_KEY_ROOT = "test-case.nature.";

	private final int level;

	private TestCaseNature(int value) {
		this.level = value;
	}

	@Override
	public String getI18nKey() {
		return I18N_KEY_ROOT + this.name();
	}

	@Override
	public int getLevel() {
		return level;
	}

	public static TestCaseNature defaultValue() {
		return UNDEFINED;
	}
}
