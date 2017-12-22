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
package org.squashtest.tm.service.internal.chart.engine;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.squashtest.tm.domain.Level;
import org.squashtest.tm.domain.requirement.RequirementCriticality;
import org.squashtest.tm.domain.requirement.RequirementStatus;
import org.squashtest.tm.domain.testcase.TestCaseExecutionMode;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.domain.testcase.TestCaseStatus;

public class LevelEnumHelper {

	private LevelEnumHelper() {

	}

	private static final Map<String, Enum<? extends Level>> map = new HashMap<>();

	private static final List<Class<? extends Enum<? extends Level>>> enums = Arrays
			.<Class<? extends Enum<? extends Level>>> asList(
			TestCaseExecutionMode.class, RequirementCriticality.class, RequirementStatus.class, TestCaseStatus.class,
			TestCaseImportance.class);

	static {
		for (Class<? extends Enum<? extends Level>> c : enums) {
			for (Enum<? extends Level> val : c.getEnumConstants()) {
				map.put(val.name(), val);
			}
		}
	}

	public static Object valueOf(String val) {
		Enum<? extends Level> level = map.get(val);
		return Enum.valueOf(level.getDeclaringClass(), val);
	}

}
