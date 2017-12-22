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
package org.squashtest.tm.domain.requirement;

import org.squashtest.tm.domain.Level;


// TODO does this need to implement LEvel ? I18nable not enough ?
@Deprecated
public enum RequirementCategory implements Level {
	SECURITY(10), USER_STORY(9), TECHNICAL(8), PERFORMANCE(7), ERGONOMIC(6), TEST_REQUIREMENT(5), BUSINESS(7), USE_CASE(
			3), NON_FUNCTIONAL(2), FUNCTIONAL(1), UNDEFINED(0);

	private static final String I18N_KEY_ROOT = "requirement.category.";
	private final int level;

	private RequirementCategory(int level) {
		this.level = level;
	}

	@Override
	public int getLevel() {
		return level;
	}

	public static RequirementCategory valueOf(int level) {
		for (RequirementCategory cat : RequirementCategory.values()) {
			if (cat.level == level) {
				return cat;
			}
		}

		throw new IllegalArgumentException("Does not match any category level : " + level);
	}

	@Override
	public String getI18nKey() {
		return I18N_KEY_ROOT + name();
	}

	public String getCode(){
		return name();
	}

}
