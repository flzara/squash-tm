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

import org.apache.commons.lang3.EnumUtils;
import org.squashtest.tm.core.foundation.i18n.Internationalizable;

public enum TestCaseKind implements Internationalizable {
	STANDARD, GHERKIN;

	public boolean isScripted() {
		return this.equals(GHERKIN);
	}

	public static TestCaseKind getFromString(String kind){
		TestCaseKind testCaseKind = EnumUtils.getEnum(TestCaseKind.class, kind);
		if (testCaseKind == null) {
			throw new IllegalArgumentException("Unknown scriptLanguage of scripted test case : " + kind);
		}
		return testCaseKind;
	}

	@Override
	public String getI18nKey() {
		return "test-case.format." + this.name().toLowerCase();
	}
}
